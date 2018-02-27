package sx.cxc

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import sx.core.AppConfig
import sx.reports.ReportService
import sx.core.Sucursal


@Secured("hasRole('ROLE_CXC_USER')")
class CobroController extends RestfulController{

    def cobroService

    def ventaService

    ReportService reportService

    static responseFormats = ['json']

    CobroController() {
        super(Cobro)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('List {}', params)
        def query = Cobro.where {}
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }
        if(params.sucursal) {
            query = query.where {sucursal == Sucursal.get(params.sucursal)}
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            query = query.where { cliente.nombre =~ search  }
        }
        String hql = "from Cobro c where c where "
        return query.list(params)
    }


    protected Object createResource() {
        log.debug('Generando cobro: {}', params)
        Cobro cobro =  new Cobro()
        bindData cobro, getObjectToBind()
        cobro.sucursal = AppConfig.first().sucursal
        return cobro
    }

    def cobrosMonetariosEnCredito() {
        log.debug('Cobros monetarios CRE {}', params)

        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = Cobro.where { sucursal == AppConfig.first().sucursal }

        query = query.where{formaDePago == 'CHEQUE' || formaDePago == 'TRANSFERENCIA' || formaDePago == 'TARJETA_DEBITO'}
        if(params.term) {
            def search = '%' + params.term + '%'
            query = query.where { cliente.nombre =~ search  || referencia =~search}
        }

        respond query.list(params)
    }

    def reporteDeComisionesTarjeta(PorSucursalFechaRepCommand command){
        log.debug('Re: {}', command.fecha)
        // log.debug('Fecha: ', params.getDate('fecha'))
        def repParams = [FECHA_CORTE: command.fecha, SUCURSAL: command.sucursal.id]
        def pdf  = reportService.run('ComisionTarjetas.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ComisionTarjetas.pdf')
    }
}

class PorSucursalFechaRepCommand {
    Sucursal sucursal
    Date fecha
}


