package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio
import sx.cxc.Cobro
import sx.cxc.CuentaPorCobrar

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ClienteController extends RestfulController{

    static responseFormats = ['json']

    ClienteController(){
        super(Cliente)
    }
    
    @Override
    protected List listAllResources(Map params) {
        // log.debug('Buscando clientes: {}', params)
        def query = Cliente.where {}
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        if(params.cartera &&  params.cartera.startsWith('CRE') ){
            // log.debug('Clientes de credito')
            query = query.where {credito.lineaDeCredito != null}
        }

        if(params.term){
            def search = '%' + params.term + '%'
            //query = query.where { clave =~ search || nombre =~ search}
            query = query.where { nombre =~ search}
        }
        return query.list(params)
    }


    /**** Finders ****/
    def facturas(Cliente cliente){
        params.max = 100
        params.sort = 'fecha'
        params.order = 'desc'
        respond CuentaPorCobrar.where {cliente == cliente}.list(params)

    }
    def cxc(Cliente cliente){
        def rows = CuentaPorCobrar.findAll("from CuentaPorCobrar c  where c.cliente = ? and c.total - c.pagos > 0 ", [cliente])
        respond rows
    }

    def cobros(Cliente cliente){
        params.max = 100
        params.sort = 'fecha'
        params.order = 'desc'
        def rows = Cobro.where {cliente == cliente}.list(params)
        respond rows
    }



}
