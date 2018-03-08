package sx.cxc

import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import grails.web.databinding.WebDataBinding
import org.apache.commons.lang.builder.ToStringBuilder
import sx.core.AppConfig
import sx.reports.ReportService
import sx.core.Sucursal
import sx.tesoreria.PorFechaCommand


@Secured("hasRole('ROLE_CXC_USER')")
class CobroController extends RestfulController{

    CobroService cobroService

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
            query = query.where { cliente.nombre =~ search || formaDePago =~ search }
        }

        String hql = "from Cobro c where c where "
        return query.list(params)
    }

    def disponibles() {
        log.debug('Disponibles {}', params)
        String hql = 'from Cobro c where c.importe - c.aplicado > 0  and tipo like ? order by fecha asc'
        params.max = 100
        String cartera = params.cartera ?: '%'

        if(params.term) {
            def search = '%' + params.term + '%'
            hql = 'from Cobro c where c.importe - c.aplicado > 0  and tipo like ? ' +
                    ' and c.cliente.nombre like ? ' +
                    ' order by fecha asc'
            respond Cobro.findAll(hql, [cartera, search], params)
        } else {
            respond Cobro.findAll(hql, [cartera], params)
        }

    }


    protected Object createResource() {
        log.debug('Generando cobro: {}', params)
        Cobro cobro =  new Cobro()
        bindData cobro, getObjectToBind()
        cobro.sucursal = AppConfig.first().sucursal
        return cobro
    }

    def cobrosMonetarios(CobranzaPorFechaCommand command) {
        log.debug('Cobros monetarios{}', params)

        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = Cobro.where { sucursal == AppConfig.first().sucursal }
        if(command.getFecha()) {
            query = query.where {fecha == command.fecha}
        }

        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }

        query = query.where{formaDePago == 'CHEQUE' || formaDePago == 'EFECTIVO' || formaDePago == 'TARJETA_DEBITO' || formaDePago == 'TARJETA_CREDITO'}
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


    protected Object saveResource(Cobro resource) {
        return cobroService.save(resource)
    }


    protected Object updateResource(Cobro resource) {
        if (resource.pendientesDeAplicar) {
            // log.debug('Facturas por aplicar : {}', resource.pendientesDeAplicar)
            return cobroService.registrarAplicacion(resource)
        }
        return super.updateResource(resource)
    }

    def saldar(Cobro cobro){
        log.debug('Saldando cobro: {}', cobro)
        cobro = cobroService.saldar(cobro)
        respond cobro
    }

    def reporteDeCobranza(CobranzaPorFechaCommand command){
        def repParams = [FECHA: command.fecha]
        repParams.ORIGEN = params.cartera
        def pdf =  reportService.run('CobranzaCxc.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'CobranzaCxc.pdf')
    }

    def reporteDeRelacionDePagos(RelacionPagosCommand command){
        log.debug('Rep: {}', params)
        def repParams = [FECHA: command.fecha]
        repParams.ORIGEN = params.origen
        repParams.COBRADOR = command.cobrador == 0 ? '%': command.cobrador.toString()
        def pdf =  reportService.run('RelacionDePagos.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'RelacionDePagos.pdf')
    }
}

class PorSucursalFechaRepCommand {
    Sucursal sucursal
    Date fecha
}

class CobranzaPorFechaCommand {
    Date fecha
}

class RelacionPagosCommand {
    Date fecha
    String origen
    Integer cobrador
}


