package sx.cxc

import com.luxsoft.cfdix.v33.NotaPdfGenerator
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController

import grails.plugin.springsecurity.annotation.Secured

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

    // @Override
    protected Object saveResource(NotaDeCredito nota) {
        assert nota.tipo.startsWith('BON'), 'Debe ser nota de bonificacion: ' + nota
        nota = notaDeCreditoService.generarNotaBonificacion(nota)
        return nota
    }

    @Override
    protected Object createResource() {
        log.debug('Preprando persistencia de nota de crediot params: {}', params)
        NotaDeCredito nota = new NotaDeCredito()
        bindData nota, getObjectToBind()
        Sucursal sucursal = Sucursal.where { nombre == 'OFICINAS'}.find()
        nota.sucursal = sucursal
        log.debug('Nota preparada: {} ', nota.properties.entrySet())
        return nota
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 15
        params.sort = 'lastUpdated'
        params.order = 'desc'
        return super.listAllResources(params)
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
        log.debug('Buscando facturas {}', params)
        params.max = 500
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def cliente = params.cliente
        def facturas = [];
        if(params.term) {
            if(params.term.isInteger()) {
                log.info('Buscando factura: {}', params.term.toLong())
                facturas = CuentaPorCobrar.findAll(
                        "from CuentaPorCobrar c where c.cliente.id = ? and c.tipo = ? and c.documento > ?",
                        [cliente,'CRE', params.term.toLong()],params)
            }
        } else {
            facturas = CuentaPorCobrar.findAll(
                    "from CuentaPorCobrar c where c.cliente.id = ? and c.tipo = ? and (c.total - c.pagos) > 0",
                    [cliente,'CRE'],params)
        }

        log.debug('Facturas localizadas: ', facturas.size())
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

    def handleNotaDeCreditoException(NotaDeCreditoException sx) {
        respond ([message: sx.message], status: 422)
    }

}
