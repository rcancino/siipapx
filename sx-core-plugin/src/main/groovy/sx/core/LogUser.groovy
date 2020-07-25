package sx.core

import grails.plugin.springsecurity.SpringSecurityService

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Slf4j
trait LogUser {

    @Autowired
    @Qualifier('springSecurityService')
    SpringSecurityService springSecurityService

    void logEntity(def entity) {

        if(entity.hasProperty('createUser')) {
            def user = springSecurityService.getCurrentUser()
            String username = user ? user.username : 'ND'
            if(entity.id == null || entity.createUser == null)
                entity.createUser = username
            entity.updateUser = username
        }
    }

    def getCurrentUserName() {
        def user = springSecurityService.getCurrentUser()
        String username = user ? user.username : 'USUARIO_DESCONOCIDO'
        return username
    }

}