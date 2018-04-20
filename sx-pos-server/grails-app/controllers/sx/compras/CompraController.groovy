package sx.compras

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import sx.core.AppConfig
import sx.core.Folio
import sx.core.Sucursal
import sx.reports.ReportService

@Secured("ROLE_COMPRAS_USER")
class CompraController extends RestfulController{

    static responseFormats = ['json']

    ReportService reportService;

    CompraController(){
        super(Compra)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'fecha'
        params.order = 'desc'
        params.max = 20
        def query = Compra.where {}

        if(params.boolean('pendientes')) {
            query = query.where {cerrada == null}
        }
        if(params.boolean('transito')){
            query = query.where {cerrada != null && pendiente == true}
        }
        if( params.folio) {
            query = query.where {folio == params.int('folio') }
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            query = query.where { proveedor.nombre =~ search || comentario =~ search}
        }
        return query.list(params)
    }

    @Override
    protected Object createResource() {
        Compra compra = new Compra()
        bindData compra, getObjectToBind()
        compra.folio = 0
        compra.sucursal = AppConfig.first().sucursal
        compra.partidas.each { it.sucursal = compra.sucursal}
        return compra
    }

    @Override
    protected Object saveResource(Object resource) {
        resource.folio = Folio.nextFolio('COMPRA','OFICINAS')
        // resource.createdBy = getPrincipal().username
        resource.save flush: true
    }

    def print( ) {
        //params.ID = params.id;
        def pdf =  reportService.run('OrdenDeCompraSuc.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'OrdenDeCompraSuc.pdf')
    }

}

