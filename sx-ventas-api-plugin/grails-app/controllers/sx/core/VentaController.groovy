package sx.core

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
// import groovy.transform.CompileStatic
import groovy.transform.ToString
// import sx.inventario.SolicitudDeTrasladoService

@Secured("hasRole('ROLE_POS_USER')")
@GrailsCompileStatic
// @CompileStatic
class VentaController extends RestfulController{

    static responseFormats = ['json']

    // VentaService ventaService

    ReportService reportService

    VentaController(){
        super(Venta)
    }

    @Override
    protected List listAllResources(Map params) {
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
        if (params.facturados) {
            query = query.where {cuentaPorCobrar != null && tipo == params.facturados}
        }
        def list = query.list(params)

        return list
    }

    @Transactional
    def mandarFacturar(Venta venta) {
        if (venta == null) {
            notFound()
            return
        }
        if (venta.facturar != null ){
            respond venta
            return
        }
        venta.facturar = new Date()
        saveResource venta
        respond venta
    }

    protected Venta saveResource(Venta resource) {
        resource.partidas.each { VentaDet it ->
            if(it.corte)
                it.corte.ventaDet = it;
        }
        // def username = getPrincipal().username
        if(resource.id == null) {
            def serie = resource.sucursal.nombre + resource.tipo
            resource.documento = Folio.nextFolio('VENTAS','PEDIDOS')
            // resource.createUser = username
        }
        if(resource.cliente.rfc != 'XAXX010101000') resource.nombre = resource.cliente.nombre
        // resource.updateUser = username
        return super.saveResource(resource)
    }

    protected Venta updateResource(Venta resource) {
        if(resource.cliente.rfc != 'XAXX010101000') resource.nombre = resource.cliente.nombre
        // def username = getPrincipal().username
        // resource.updateUser = username
        return super.updateResource(resource)
    }

    def pendientes(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def ventas = Venta.where{ sucursal == sucursal && facturar == null}.list(params)
        respond ventas
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

    /*
    @Transactional
    def facturar(Venta pedido) {
        if(pedido == null ){
            notFound()
            return
        }
        assert !pedido.cuentaPorCobrar, 'Pedido ya facturado'
        pedido = ventaService.facturar(pedido);
        log.debug'Pedido facturado exitosamente: ' + pedido
        respond pedido
    }
    */

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

    /*
    @Transactional
    def timbrar(Venta venta) {
        if(venta == null ){
            notFound()
            return
        }
        def cfdi = ventaService.timbrar(venta)
        respond cfdi
    }
    */

    def print( Venta pedido) {
        params.ID = pedido.id
        ByteArrayOutputStream pdf =  reportService.run('Pedido.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pedido.pdf')

    }

    @Transactional
    def generarSolicitudAutomatica(Venta venta) {
        if (venta == null) {
            notFound()
            return
        }
        // solicitudDeTrasladoService.generarSolicitudAutomatica(venta)
        // TODO: Utilizar events para mandar a generar la solicitud
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
