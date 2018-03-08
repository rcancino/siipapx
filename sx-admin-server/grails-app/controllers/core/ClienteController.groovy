package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio
import sx.cxc.CobranzaPorFechaCommand
import sx.cxc.Cobro
import sx.cxc.CuentaPorCobrar
import sx.cxc.NotaDeCredito
import sx.reports.ReportService

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ClienteController extends RestfulController{

    static responseFormats = ['json']

    ReportService reportService

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
        def query = CuentaPorCobrar.where {cliente == cliente}
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where { documento == params.term.toInteger() }
            }
        }
        List res = query.list(params)
        respond res

    }
    def cxc(Cliente cliente){
        def rows = CuentaPorCobrar.findAll("from CuentaPorCobrar c  where c.cliente = ? and c.total - c.pagos > 0 ", [cliente])
        respond rows
    }

    def notas(Cliente cliente){
        params.max = 20
        params.sort = 'fecha'
        params.order = 'desc'
        def query = NotaDeCredito.where {cliente == cliente}
        if(params.tipo) {
            def tipo = params.tipo
            query = query.where {tipo == tipo}
        }
        respond query.list(params)
    }

    def cobros(Cliente cliente){
        params.max = 100
        params.sort = 'fecha'
        params.order = 'desc'
        def rows = Cobro.where {cliente == cliente}.list(params)
        respond rows
    }

    def estadoDeCuenta(CobranzaPorFechaCommand command){
        def repParams = [FECHA: command.fecha]
        repParams.ORIGEN = params.cartera
        repParams.CLIENTE = params.cliente
        def pdf =  reportService.run('EstadoDeCuentaCte.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'CobranzaCxc.pdf')
    }



}
