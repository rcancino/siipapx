package sx.cxc


import groovy.util.logging.Slf4j

import grails.events.annotation.Subscriber
import grails.compiler.GrailsCompileStatic

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.model.PersistentEntity

import org.apache.commons.lang3.exception.ExceptionUtils

import sx.core.Cliente
import sx.core.ClienteCredito
import sx.cloud.FirebaseService

/**
*
* Listener destinado a actualizar el saldo de los clientes y cuentas por cobrar en funcion de
* el alta y baja de Aplicaciones de cobro
*
*
*/
@Slf4j
@GrailsCompileStatic
class AplicacionDeCobroListenerService {

	

	String getId(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof AplicacionDeCobro ) {
            return ((AplicacionDeCobro) event.entityObject).id
        }
        return null
    }

    AplicacionDeCobro getAplicacion(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof AplicacionDeCobro ) {
            return (AplicacionDeCobro) event.entityObject
        }
        return null
    }

    @Subscriber
    void afterInsert(PostInsertEvent event) {
        AplicacionDeCobro aplicacion = getAplicacion(event)
        if ( aplicacion ) {
            log.info('Inserted aplicacion cxc: {} ', aplicacion.cuentaPorCobrar.id)
            CuentaPorCobrar.withNewSession {
                CuentaPorCobrar cxc = CuentaPorCobrar.get(aplicacion.cuentaPorCobrar.id)
                log.info('Saldo: {}', cxc.saldoReal)
            }
        }
    }

    @Subscriber
    void afterDelete(PostDeleteEvent event) {   
        AplicacionDeCobro aplicacion = getAplicacion(event)
        if ( aplicacion ) {
            log.info('Delete aplicacion cxc: {} ', aplicacion.cuentaPorCobrar.id)
            CuentaPorCobrar.withNewSession {
                CuentaPorCobrar cxc = CuentaPorCobrar.get(aplicacion.cuentaPorCobrar.id)
                log.info('Saldo: {}', cxc.saldoReal)
            }
            
        }
    }


}
