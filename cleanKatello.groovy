@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2' )
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

def katello = new RESTClient( 'https://localhost/katello/api/' )
katello.auth.basic 'username', 'password'

// perform a GET request, expecting JSON response data
try {
    println "Get consumers"
    def resp = katello.get( path: 'consumers' )
    println "Consumers:"
    println resp.data
    resp.data.each { consumer ->
        def uuid = consumer.uuid
        println "Deleting uuid: " + uuid
        def delresp = katello.delete( path: 'consumers/' + uuid )
    }
} catch ( ex ) { 
    println ex.response.status
    println ex.response
}

