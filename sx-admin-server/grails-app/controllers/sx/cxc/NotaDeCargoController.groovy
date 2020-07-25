package sx.cxc

import groovy.util.logging.Slf4j

import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import grails.web.servlet.mvc.GrailsParameterMap

import org.apache.commons.lang3.exception.ExceptionUtils

import com.luxsoft.cfdix.v33.NotaDeCargoPdfGenerator
import sx.reports.ReportService
import com.luxsoft.utils.Periodo


@Secured("hasRole('ROLE_CXC_USER')")
@Slf4j
class NotaDeCargoController extends RestfulController<NotaDeCargo> {
    
    static responseFormats = ['json']

    NotaDeCargoService notaDeCargoService

    ReportService reportService

    NotaDeCargoPdfGenerator notaDeCargoPdfGenerator

    NotaDeCargoController() {
        super(NotaDeCargo)
    }

    @Override
    protected NotaDeCargo saveResource(NotaDeCargo resource) {
        return notaDeCargoService.save(resource)
    }

    @Override
    protected NotaDeCargo updateResource(NotaDeCargo resource) {
        return notaDeCargoService.update(resource)
    }

    @Override
    protected NotaDeCargo createResource() {
        NotaDeCargo instance = new NotaDeCargo()
        instance.sucursal = notaDeCargoService.getSucursal()
        bindData instance, getObjectToBind()
        return instance
    }

    @Override
    protected void deleteResource(NotaDeCargo resource) {
        notaDeCargoService.delete(resource)
    }

    @Override
    Object index(Integer max) {
        GrailsParameterMap params = (GrailsParameterMap)this.params;

        params.max = Math.min(max ?: 20, 100)
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        log.debug('GET[List] {}', params)

        def query = NotaDeCargo.where {}

        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }
        
        if(params.periodo) {
            def periodo = params.periodo
            query = query.where{ fecha >= periodo.fechaInicial && fecha <= periodo.fechaFinal}
        }

        if(params.term) {
            log.debug('Term: {}', params['term'])
            if(params.term.isInteger()) {
                query = query.where{folio == params.term.toInteger()}
            } else {
                def search = '%' + params.term + '%'
                query = query.where { cliente.nombre =~ search}
            }
        }
        respond query.list(params)
    }

    /*
    @Override
    protected List<NotaDeCargo> listAllResources(Map ptm) {
        GrailsParameterMap params = (GrailsParameterMap)params
        log.debug('List {}', ptm)
        params.max = Math.min(max ?: 20, 100)
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = NotaDeCargo.where {}
        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }
        if(params.term) {
            log.debug('Term: {}', params.term)
            if(params.term.isInteger()) {
                query = query.where{folio == params.term.toInteger()}
            } else {
                def search = '%' + params.term + '%'
                query = query.where { cliente.nombre =~ search}
            }
        }
        return query.list(params)
    }

     */


    def print(NotaDeCargo nota) {
        assert nota.cfdi, 'Nota sin timbrar: ' + nota.id
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def data = notaDeCargoPdfGenerator.getReportData(nota)
        Map parametros = data['PARAMETROS']
        parametros.LOGO = realPath + '/PAPEL_CFDI_LOGO.jpg'
        def pdf  = reportService.run('PapelCFDI3Nota.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'NotaDeCredito.pdf')
    }

    @Transactional
    def timbrar(NotaDeCargo nota) {
        nota = notaDeCargoService.timbrar(nota)
        respond (nota, view: 'show')
    }

    @Transactional
    def cancelar(NotaDeCargo cargo) {
        String motivo = params.motivo
        log.info('Cancelando Nota de cargo: {}  motivo:{}', cargo.folio, motivo)
        cargo = notaDeCargoService.cancelar(cargo, motivo)
        respond (cargo, view: 'show')
    }

    def reporteDeNotasDeCargo() {
        // log.debug('Re: {}', params)
        Periodo periodo = new Periodo()
        bindData(periodo, params)
        def repParams = [
            FECHA_INI: periodo.fechaInicial,
            FECHA_FIN: periodo.fechaFinal,
            ORIGEN: params.ORIGEN,
        ]
        def pdf  = reportService.run('NotasDeCargo.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ComisionTarjetas.pdf')
    }

    def handleException(Exception e) {
        String message = ExceptionUtils.getRootCauseMessage(e)
        log.error(message, ExceptionUtils.getRootCause(e))
        respond([message: message], status: 500)
    }
}
