package sx.cxc

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

import sx.reports.ReportService

@Secured("hasRole('ROLE_POS_USER')")
class VentaCreditoController extends RestfulController<VentaCredito>{

    static responseFormats = ['json']

    ReportService reportService

    RevisionService revisionService

    VentaCreditoController(){
        super(VentaCredito)
    }

    @Override
    protected List<VentaCredito> listAllResources(Map params) {
        params.max = 50
        // def query = CuentaPorCobrar.where {credito != null}
        def query = VentaCredito.where{}
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        return query.list(params)
    }

    @Override
    protected VentaCredito updateResource(VentaCredito resource) {
        this.revisionService.actualizarRevision(resource)
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

    def print() {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        params.FECHA = new Date();
        params.COBRADOR_NOMBRE = '%'
        def pdf = reportService.run('FacturasAcobroYRevision.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Antiguead.pdf')
    }
}
