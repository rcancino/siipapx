package sx.core

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.inventario.Traslado

import sx.cloud.LxClienteCreditoService

@Transactional
class ClienteCreditoService {

    LxClienteCreditoService lxClienteCreditoService
    
    ClienteCredito updateCliente(ClienteCredito credito) {
        ClienteCredito target = credito.save failOnError: true, flush: true
        // lxClienteCreditoService.push(target)
        return target

    }

}
