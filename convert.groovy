@Grab(group='org.jfree', module='jfreechart', version='1.0.14')
import groovy.util.XmlSlurper
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
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

    def csv = new File("${threads}-results.csv")
    csv.withWriter('UTF-8') {
        it.writeLine('timeStamp,elapsed,success,label')
        def samples = results.'**'.findAll{ it.name() =~ /.*[Ss]ample/ && !(it.@lb == "Debug Sampler" || it.@lb ==~ /Create.*/ || it.@lb == "Clean up" || it.@lb == "Get API URIs") }
        samples.each { sample ->
            it.writeLine("${sample.@ts.text()},${sample.@t.text()},${sample.@s.text()},${sample.@lb.text()}")
        }
    }
}

