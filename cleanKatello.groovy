@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2' )
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import java.security.KeyStore
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

// Temp import til thin server fixed
import groovy.json.JsonSlurper

def output = """curl -k -N --user admin:admin https://bcrochet-katello.usersys.redhat.com/katello/api/consumers""".execute().text
def consumers = new JsonSlurper().parseText(output)
consumers.each { consumer ->
    def uuid = consumer.uuid
    println "Delete uuid: ${uuid}"
    println """curl -k -X DELETE -N --user admin:admin https://bcrochet-katello.usersys.redhat.com/katello/api/consumers/${uuid}""".execute().text
}





//def keystore = KeyStore.getInstance( KeyStore.defaultType )
//getClass().getResource( "truststore.jks" ).withInputStream {
//    keystore.load( it, "password".toCharArray() )
//}

//def katello = new RESTClient( 'https://bcrochet-katello.usersys.redhat.com/katello/api/' )
//katello.client.connectionManager.schemeRegistry.register( new Scheme("https", new SSLSocketFactory(keystore), 443) )
//katello.auth.basic 'admin', 'admin'

// When rubygem-thin gets the auth fixed, we can re-enable this
// 
// perform a GET request, expecting JSON response data
//try {
//    println "Get consumers"
//    def resp = katello.get( path: 'consumers' )
//    println "Consumers:"
//    println resp.data
//    resp.data.each { consumer ->
//        def uuid = consumer.uuid
//        println "Deleting uuid: " + uuid
//        def delresp = katello.delete( path: 'consumers/' + uuid )
//    }
//} catch ( ex ) { 
//    println "response status: ${ex.response.statusLine}"
//    ex.response.headers.each { h ->
//        println " ${h.name} : ${h.value}"
//    }
//}

