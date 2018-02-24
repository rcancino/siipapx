package core

import grails.rest.RestfulController
import sx.core.ClienteCredito

class ClienteCreditoController extends RestfulController{

    ClienteCreditoController(){
        super(ClienteCredito)
    }

    @Override
    protected Object updateResource(Object resource) {
        log.debug('Actualizando {}', resource)
        return super.updateResource(resource)
    }
}
