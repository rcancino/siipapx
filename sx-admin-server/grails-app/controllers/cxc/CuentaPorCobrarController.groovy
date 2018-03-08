package sx.cxc

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Cliente
import sx.core.Sucursal
import sx.core.Venta
import sx.reports.ReportService

@Secured("hasRole('ROLE_POS_USER')")
class CuentaPorCobrarController extends RestfulController{

    static responseFormats = ['json']

    ReportService reportService

    CuentaPorCobrarService cuentaPorCobrarService

    CuentaPorCobrarController() {
        super(CuentaPorCobrar)
    }

    @Override
    protected List listAllResources(Map params) {

        def query = CuentaPorCobrar.where {}
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        if(params.documento){
          int documento = params.int('documento')
          query = query.where { documento >= documento }
        }
        if(params.cliente){
            query = query.where { cliente.id == params.cliente}
        }
        return query.list(params)
    }

    def pendientes(Cliente cliente) {
        if (cliente == null) {
            notFound()
            return
        }
        // params.max = 100
        params.sort = params.sort ?:'fecha'
        params.order = params.order ?:'asc'
        def rows = CuentaPorCobrar.findAll("from CuentaPorCobrar c  where c.cliente = ? and c.total - c.pagos > 0 ", [cliente])
        respond rows
    }

    def saldar(CuentaPorCobrar cxc) {
        log.debug('Saldando cuenta por cobrar: {}', cxc.folio)
        cuentaPorCobrarService.saldar(cxc)
        cxc.refresh()
        respond cxc
    }

}
