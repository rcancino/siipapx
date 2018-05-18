package sx.cxc

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import groovy.transform.ToString
import sx.core.Cobrador
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


    def registrarRecepcionCxC(BatchUpdateCommand command){
        log.info('Registrando recepcion  de {} facturas ', command.facturas.size())
        List<VentaCredito> facturas = command.facturas;
        facturas.each {
            it.fechaRecepcionCxc = new Date()
            it = this.revisionService.actualizarRevision(it)
        }
        respond facturas
    }

    def cancelarRecepcionCxC(BatchUpdateCommand command){
        log.info('Cancelar recepcion en cxc   de {} facturas ', command.facturas.size())
        List<VentaCredito> facturas = command.facturas.findAll{ !it.revisada }
        facturas.each {
            it.fechaRecepcionCxc = null
            it = this.revisionService.actualizarRevision(it)
        }
        respond facturas
    }

    def registrarRvisada(BatchUpdateCommand command){
        log.info('Registrando recepcion  de {} facturas ', command.facturas.size())
        List<VentaCredito> facturas = command.facturas.findAll{ it.fechaRecepcionCxc &&  !it.revisada }
        facturas.each {
            it.revisada = true
            it.save flush: true
        }
        respond facturas
    }

    def batchUpdate(BatchUpdateCommand command){
        log.info('Batch update de {} facturas con {}', command.facturas.size(), command.template)
        List<VentaCredito> facturas = command.facturas;
        facturas.each {
            bindData(it, command.template)
            it = this.revisionService.actualizarRevision(it)
        }
        respond facturas
    }

    def print() {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        params.FECHA = new Date();
        params.COBRADOR_NOMBRE = '%'
        def pdf = reportService.run('FacturasAcobroYRevision.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Antiguead.pdf')
    }
}

public class BatchUpdateCommand {
    VentaCreditoCommand template;
    List<VentaCredito> facturas;

    static constraints = {
        template nullable: true
    }
}

@ToString(includeNames=true,includePackage=false)
public class VentaCreditoCommand {

    Integer plazo
    Integer diaRevision
    Integer diaPago
    Boolean vencimientoFactura
    Date fechaRevision
    Cobrador cobrador
    String comentario
    String comentarioReprogramarPago

    static constraints = {
        plazo nullable:true
        diaRevision nullable:true
        diaPago nullable:true
        vencimientoFactura nullable:true
        fechaRevision nullable:true
        cobrador nullable:true
        comentario nullable: true
        comentarioReprogramarPago nullable:  true
    }
}



/*

*/