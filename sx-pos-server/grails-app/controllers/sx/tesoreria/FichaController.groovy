package sx.tesoreria

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Sucursal

@Secured("hasRole('ROLE_CXC_USER')")
class FichaController extends RestfulController {

    static responseFormats = ['json']

    FichaController() {
        super(Ficha)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = Ficha.where {}
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        return query.list(params)
    }
}
