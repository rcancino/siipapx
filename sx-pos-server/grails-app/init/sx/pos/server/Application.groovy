package sx.pos.server

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
    /*
    void onStartup(java.util.Map<java.lang.String, java.lang.Object> event) {
    	println "Inicializando applicacion......" + event
    }
    */
}