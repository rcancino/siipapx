package sx.cxc

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

import sx.reports.ReportService

@Secured("hasRole('ROLE_POS_USER')")
class VentaCreditoController extends RestfulController{

    static responseFormats = ['json']

    ReportService reportService

    RevisionService revisionService

    VentaCreditoController(){
        super(VentaCredito)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('Buscando revisiones params: {}', params)
        params.max = 5000
        def query = CuentaPorCobrar.where {credito != null}
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

    def generar() {
        log.debug('Generando facturas a revision')
        revisionService.generar();
        respond status: 200
    }

    def recalcular() {
        log.debug('Recalculando fechas de revision y pago  para cuentas por cobrar')
        revisionService.recalcularPendientes()
        respond status: 200
    }
}
