package sx.core

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.inventario.Traslado

import sx.cloud.LxClienteService

@Transactional
class ClienteService {

    LxClienteService lxClienteService
    
    Cliente updateCliente(Cliente cliente) {
        Cliente target = cliente.save failOnError: true, flush: true
        lxClienteService.push(target)
        return target

    }

}
