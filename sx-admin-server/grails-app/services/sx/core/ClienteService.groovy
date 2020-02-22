package sx.core

import groovy.util.logging.Slf4j

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional

import grails.util.Environment

import sx.cloud.LxClienteService

@Transactional
@Slf4j
class ClienteService {

    LxClienteService lxClienteService
    
    Cliente updateCliente(Cliente cliente) {
        Cliente target = cliente.save failOnError: true, flush: true
        updateFirebase(target)
        return target

    }

    void updateFirebase(Cliente cliente) {
    	if(Environment.current == Environment.PRODUCTION) {
        	try {
    			lxClienteService.push(target)
    			log.debug('Firebase actualizado {}', cliente.nombre)
			}catch (Exception ex) {
				log.error(ex)
			}
        }
    	
    }

}
