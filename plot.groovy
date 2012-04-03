@Grab(group='org.jfree', module='jfreechart', version='1.0.14')
import groovy.util.XmlSlurper
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.LogarithmicAxis
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.util.Log
import org.jfree.util.LogContext
import org.jfree.chart.ChartUtilities
import java.awt.Font

def elapsed = [:]
def timestamps = [:]
def starttimes = [:]
def errors = [:]

new File('.').eachFileMatch ~/SubscriptionManagerResultsTree-\d+-\d+-\d+.jtl/, { file ->
    def threads = (file.name =~ /SubscriptionManagerResultsTree-\d+-\d+-(\d+).jtl/)[0][1]
    def results = new XmlSlurper().parse(file)

    def samples = results.'**'.findAll{ it.name() =~ /.*[Ss]ample/ && !(it.@lb == "Debug Sampler" || it.@lb ==~ /Create.*/ || it.@lb == "Clean up" || it.@lb == "Get API URIs") }
    samples.each { sample ->
        def label = sample.@lb.text()
        if ( ! elapsed.containsKey(label)) {
            elapsed[(label)] = [:]
            timestamps[(label)] = [:]
            starttimes[(label)] = [:]
            errors[(label)] = [:]
        }
        if ( ! elapsed[(label)].containsKey(threads) ) {
            elapsed[(label)][(threads)] = []
            timestamps[(label)][(threads)] = []
            starttimes[(label)][(threads)] = []
            errors[(label)][(threads)] = []
        }
        elapsed."${label}"."${threads}" << sample.@t.toLong()
        timestamps[(label)][(threads)] << sample.@ts.toLong()
        starttimes[(label)][(threads)] << sample.@ts.toLong() - sample.@t.toLong()
        if ( ! sample.@s ) {
            errors[(label)][(threads)] << sample.@t.toLong()
        }
    }
}

elapsed.keySet().each { label ->
    def throughput_data = [null]
    def dataset = new DefaultBoxAndWhiskerCategoryDataset()
    def error_x = []
    def error_y = []
    def column = 1
    elapsed[(label)].keySet().each { thread_count ->
        def plot_data = elapsed."${label}"."${thread_count}"
        def plot_label = thread_count
        def test_start = starttimes."${label}"."${thread_count}".min()
        def test_end =  timestamps."${label}"."${thread_count}".max()
        def test_length = (test_end - test_start) / 1000
        def num_requests = timestamps."${label}"."${thread_count}".size() - errors."${label}"."${thread_count}".size()
        if ( test_length > 0 ) throughput_data << num_requests / test_length.toFloat()
        else throughput_data << 0
        errors."${label}"."${thread_count}".each {
            error_x << column
            error_y << error
        }
        column += 1
        dataset.add(plot_data, "${plot_label}", "${label}")
    }
    
    def xAxis = new CategoryAxis("Threads")
    def yAxis = new LogarithmicAxis("Milliseconds")
    yAxis.autoRangeIncludesZero = true
    def renderer = new BoxAndWhiskerRenderer()
    renderer.fillBox = false
    renderer.toolTipGenerator = new BoxAndWhiskerToolTipGenerator()
    renderer.maximumBarWidth = 5.0
    renderer.meanVisible = true
    renderer.medianVisible = true
    renderer.fillBox = true
    def plot = new CategoryPlot(dataset, xAxis, yAxis, renderer)
    def chart = new JFreeChart("Box and Whisker", new Font("SansSerif", Font.BOLD, 14), plot, true)
    ChartUtilities.saveChartAsPNG(new File("plot-${label}.png"), chart, 1024, 768)
}
