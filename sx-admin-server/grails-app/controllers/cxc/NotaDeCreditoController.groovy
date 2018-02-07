package sx.cxc

import com.luxsoft.cfdix.v33.NotaPdfGenerator
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController

import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.reports.ReportService
import sx.core.Sucursal
import sx.inventario.DevolucionDeVenta

/**
 * Controlador central REST de notas de credito CxC
 *
 */
@Secured("hasRole('ROLE_CXC_USER')")
class NotaDeCreditoController extends RestfulController{

    static responseFormats = ['json']

    NotaDeCreditoService notaDeCreditoService

    ReportService reportService

    NotaDeCreditoController(){
        super(NotaDeCredito)
    }

    @Transactional
    def save() {
        if(handleReadOnly()) {
            return
        }
        NotaDeCredito nota = new NotaDeCredito()
        bindData nota, getObjectToBind()
        Sucursal sucursal = Sucursal.where { nombre == 'OFICINAS'}.find()
        nota.sucursal = sucursal
        // List facturas = nota.partidas.collect { it.cuentaPorCobrar }
        //log.debug('Facturas: {}', facturas.size())
        nota = notaDeCreditoService.generarBonificacion(nota)
        log.debug('Nota generada: {}', nota)
        respond nota

    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 10
        params.sort = 'lastUpdated'
        params.order = 'desc'
        // log.debug('Buscando notas: {}',params)
        def query = NotaDeCredito.where{ }
        if(params.tipo) {
            query = query.where{tipo == params.tipo}
        }
        if(params.term) {
            if(params.term.isInteger()) {
                query = query.where{folio == params.term.toLong()}
            }
        }
        respond query.list(params)
    }

    def buscarRmd() {
       // log.debug('Localizando rmd {}', params)
        params.max = 10
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def cliente = params.cliente
        def query = DevolucionDeVenta.where{ }
        if(params.boolean('pendientes')) {
            query = query.where{cobro == null}
        } else if (!params.boolean('pendientes')) {
            query = query.where{cobro != null}
        }
        if(params.term) {
            if(params.term.isInteger()) {
                query = query.where{documento == params.term.toInteger()}
            }
        }
        respond query.list(params)
    }

    def buscarFacturasPendientes() {
        // log.debug('Buscando facturas {}', params)
        params.max = 500
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def cliente = params.cliente
        def facturas = [];
        if(params.term) {
            if(params.term.isInteger()) {
                // log.info('Buscando factura: {}', params.term.toLong())
                facturas = CuentaPorCobrar.findAll(
                        "from CuentaPorCobrar c where c.cliente.id = ? and c.tipo = ? and c.documento >= ?",
                        [cliente,'CRE', params.term.toLong()],params)
            }
        } else {
            facturas = CuentaPorCobrar.findAll(
                    "from CuentaPorCobrar c where c.cliente.id = ? and c.tipo = ? and (c.total - c.pagos) > 0",
                    [cliente,'CRE'],params)
        }

        // log.debug('Facturas localizadas: ', facturas.size())
        respond facturas
    }

    @Transactional
    def generarConRmd(DevolucionDeVenta rmd){
        log.debug('Generando nota de credito para RMD: {}' , rmd)
        NotaDeCredito nota = new NotaDeCredito()
        nota.tipoCartera = params.cartera
        nota =  notaDeCreditoService.generarNotaDeDevolucion(nota, rmd)
        respond nota
    }

    def print(NotaDeCredito nota) {
        assert nota.cfdi, 'Nota sin timbrar: ' + nota.id
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def data = NotaPdfGenerator.getReportData(nota)
        Map parametros = data['PARAMETROS']
        parametros.LOGO = realPath + '/PAPEL_CFDI_LOGO.jpg'
        def pdf  = reportService.run('PapelCFDI3Nota.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'NotaDeCredito.pdf')
    }

    def timbrar(NotaDeCredito nota) {
        assert !nota.cfdi, 'Nota ya timbrada'
        nota = notaDeCreditoService.timbrar(nota)
        respond nota
    }

    def aplicar(NotaDeCredito nota){
        nota = notaDeCreditoService.aplicar(nota)
        respond nota
    }

    def handleNotaDeCreditoException(NotaDeCreditoException sx) {
        String msg = ExceptionUtils.getRootCauseMessage(sx)
        respond ([message: sx.message], status: 422)
    }

}
