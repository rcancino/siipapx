package sx.core

import grails.rest.RestfulController

class PreciosPorClienteController extends RestfulController{

    PreciosPorClienteController(){
        super(PreciosPorCliente)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 100
        return super.listAllResources(params)
    }
}
