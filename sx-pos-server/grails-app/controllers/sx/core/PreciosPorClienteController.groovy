package sx.core

import grails.rest.RestfulController

class PreciosPorClienteController extends RestfulController{

    PreciosPorClienteController(){
        super(PreciosPorCliente)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('Buscando precios por cliente ', params)
        params.max = 100
        Cliente cliente = Cliente.get(params.cliente)
        def list = PreciosPorCliente.where { cliente == cliente && activo }.last()
        render list
    }
}
