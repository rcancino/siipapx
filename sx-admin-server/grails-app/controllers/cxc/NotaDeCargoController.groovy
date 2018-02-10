package sx.cxc


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_CXC_USER')")
class NotaDeCargoController extends RestfulController {
    
    static responseFormats = ['json']

    NotaDeCargoController() {
        super(NotaDeCargo)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('List {}', params)
        def query = NotaDeCargo.where {}
        params.max = 30
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            query = query.where { cliente.nombre =~ search  }
        }
        return query.list(params)
    }
}
