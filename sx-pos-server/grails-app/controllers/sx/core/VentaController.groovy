package sx.core

import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import groovy.transform.ToString
import grails.plugin.springsecurity.annotation.Secured
import sx.reports.ReportService


@Secured("hasRole('ROLE_POS_USER')")
class VentaController extends RestfulController{

    static responseFormats = ['json']

    VentaService ventaService

    ReportService reportService

    VentaController(){
        super(Venta)
    }

    @Override
    protected List listAllResources(Map params) {
        // log.debug('Localizando ventas {}', params)
        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 100
        def query = Venta.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        if (params.facturables) {
            query = query.where {facturar !=  null  && cuentaPorCobrar == null}
            if(params.facturables == 'CRE'){
                query = query.where {tipo == params.facturables}
            } else {
                query = query.where {tipo != 'CRE'}
            }
        }
        /*
        if (params.facturados) {
            log.debug('Buscando pedidos facturados {}', params)
            query = query.where {cuentaPorCobrar != null}
        }
        */
        def list = query.list(params)
        return list
    }


    @Transactional
    def mandarFacturar() {
        log.debug('Mandando facturar: ' + params)
        def res = ventaService.mandarFacturar(params.id)
        respond res
    }


    @Override
    protected Object createResource() {
        Venta venta = new Venta()
        bindData venta, getObjectToBind()
        venta.partidas.each {
            if(it.corte)
                it.corte.ventaDet = it;
        }
        if(venta.envio) {
            venta.envio.venta = venta;
        }
        return venta
    }


    protected Venta saveResource(Venta resource) {
        return ventaService.save(resource)
    }

    protected Venta updateResource(Venta resource) {
        return ventaService.save(resource)
    }

    def pendientes(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:150
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = Venta.where{ sucursal == sucursal && cuentaPorCobrar == null && facturar == null}
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where{documento == params.term.toInteger()}
            } else {
                query = query.where { nombre =~ search }
            }
        }
        respond query.list(params)
    }

    def facturados(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = Venta.where{ sucursal == sucursal && cuentaPorCobrar != null}
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where{cuentaPorCobrar.documento == params.term.toInteger()}
            } else {
                query = query.where { nombre =~ search }
            }
        }
        respond query.list(params)
    }

    def findManiobra() {
        def clave = params.clave
        def found = Producto.where{ clave == clave}.find()
        if(found == null ){
            notFound()
            return
        }
        respond found
    }

    @Transactional
    def facturar(Venta pedido) {
        if(pedido == null ){
            notFound()
            return
        }
        assert !pedido.cuentaPorCobrar, 'Pedido ya facturado'
        pedido = ventaService.facturar(pedido);
        respond pedido
    }

    def cobradas(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def ventas = Venta.where{ sucursal == sucursal && cuentaPorCobrar != null}.list(params)
        respond ventas
    }

    @Transactional
    def timbrar(Venta venta) {
        if(venta == null ){
            notFound()
            return
        }
        def cfdi = ventaService.timbrar(venta)
        respond cfdi
    }

    def print( Venta pedido) {
        params.ID = pedido.id
        def pdf =  reportService.run('Pedido.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pedido.pdf')
    }

    /**
     * Cancela la venta, que implica eliminar la cuenta por cobrar pero deja el pedido  vivo
     *
     * @param venta
     * @return
     */
    def cancelar(Venta venta) {
        if(venta == null ){
            notFound()
            return
        }
        venta = ventaService.cancelar(venta)
        respond venta
    }

}

@ToString(includeNames=true,includePackage=false)
class VentasFiltro {

    Date fechaInicial
    Date fechaFinal
    Sucursal sucursal
    Cliente cliente
    int registros = 20


    static constraints = {
        fechaInicial nullable:true
        fechaFinal nullable: true
        sucursal nullable:true
        cliente nullable:true
        registros size:(1..500)
    }
}
