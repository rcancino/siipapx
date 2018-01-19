package sx.cxc


import grails.rest.RestfulController


import grails.plugin.springsecurity.annotation.Secured

/**
 * Controlador central REST de notas de credito CxC
 *
 */
@Secured("hasRole('ROLE_CXC_USER')")
class NotaDeCreditoController extends RestfulController{

    static responseFormats = ['json']

    NotaDeCreditoController(){
        super(NotaDeCredito)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 15
        params.sort = 'lastUpdated'
        params.order = 'desc'
        return super.listAllResources(params)
    }

    @Override
    protected Object saveResource(Object resource) {
        resource.save flush:true
    }

}
