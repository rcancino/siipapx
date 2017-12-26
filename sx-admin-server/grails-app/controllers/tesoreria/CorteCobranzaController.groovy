package sx.tesoreria

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_POS_USER')")
class CorteCobranzaController extends RestfulController {

    static responseFormats = ['json']

    CorteCobranzaController() {
      super(CorteCobranza)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'corte'
        params.order = 'asc'
        params.max = 100
        def query = CorteCobranza.where {}
        return query.list(params)
    }

    protected CorteCobranza saveResource(CorteCobranza resource) {
        def username = getPrincipal().username
        resource.createUser = username
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected CorteCobranza updateResource(CorteCobranza resource) {
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }


}
