package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class DescuentoPorVolumenController extends RestfulController{

    static responseFormats = ['json']

    public DescuentoPorVolumenController(){
        super(Clase)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 500
        return super.listAllResources(params)
    }
}
