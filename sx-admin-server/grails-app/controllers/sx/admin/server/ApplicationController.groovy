package sx.admin.server

import groovy.transform.CompileDynamic

import grails.plugin.springsecurity.annotation.Secured
import grails.compiler.GrailsCompileStatic
import grails.core.GrailsApplication
import grails.util.Environment
import grails.plugins.*


@GrailsCompileStatic
class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }

    @Secured("IS_AUTHENTICATED_ANONYMOUSLY")
    @CompileDynamic
    def session() {
        Map res = [:]

        Map info = [:]
        info.version = grailsApplication.metadata.getApplicationVersion()
        info.name = grailsApplication.metadata.getApplicationName()
        info.grailsVersion = grailsApplication.metadata.getGrailsVersion()
        info.environment = grailsApplication.metadata.getEnvironment()
        info.dataSourceUrl = grailsApplication.config.dataSource.url
        
        res.appInfo = info

        if (isLoggedIn()) {
            res.user = getAuthenticatedUser()
        }
        respond res


    }
}
