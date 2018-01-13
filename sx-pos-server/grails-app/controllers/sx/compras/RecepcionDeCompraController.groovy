package sx.compras

import grails.rest.RestfulController
import groovy.transform.ToString
import sx.core.Existencia
import sx.core.Proveedor
import sx.core.Sucursal
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Folio
import sx.core.Inventario
import sx.reports.ReportService


@Secured("ROLE_INVENTARIO_USER")
class RecepcionDeCompraController extends  RestfulController{

    static responseFormats = ['json']

    RecepcionDeCompraService recepcionDeCompraService

    ReportService reportService

    public RecepcionDeCompraController() {
        super(RecepcionDeCompra)
    }

   @Override
    protected List listAllResources(Map params) {
       params.sort = 'lastUpdated'
       params.order = 'desc'
       def query = RecepcionDeCompra.where {sucursal.id ==  params.sucursal}
       if(params.term) {
           if(params.term.isInteger()) {
               query = query.where{documento == params.term.toInteger() }
           } else {
               def search = '%' + params.term + '%'
               query = query.where { proveedor.nombre =~ search || remision =~ search}
           }
       }
       /*
        if(params.documento) {
            def documento = params.int('documento')
            query = query.where {documento >=  documento}
        }
        if(params.remision) {
            def remision = params.remision
            query = query.where {remision >=  remision}
        }
        */
        return query.list(params)
    }

    // @Override
    protected RecepcionDeCompra saveResource(RecepcionDeCompra resource) {
        def username = getPrincipal().username
        recepcionDeCompraService.save(resource, username)
    }

    protected RecepcionDeCompra updateResource(RecepcionDeCompra resource) {
        def username = getPrincipal().username
        resource.updateUser = username
        if(params.inventariar){
            recepcionDeCompraService.afectarInventario(resource)
        } else {
            resource.save flush: true
        }
        return resource
    }

    public buscarCompra(CompraParaComSearchCommand command){
       
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        def q = Compra.where{ (sucursal == command.sucursal || sucursal.nombre == 'OFICINAS') && folio == command.folio }
        
        Compra res = q.find()

        if (res == null) {
            notFound()
            return
        }
        // respond res, status: 200
        forward controller: 'compra', action: 'show', id: res.id
    }

    def recibir(Compra compra) {
        if (compra == null) {
            notFound()
            return
        }
        log.debug('Generando recepcion autormatica de compra: {}', compra.folio)
        RecepcionDeCompra com = recepcionDeCompraService.recibir(compra, getPrincipal().username)
        respond com
    }

    def print() {
        // log.debug('Imprimiendo movimiento: {}', params.ID)
        def pdf =  reportService.run('MovGenerico.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pedido.pdf')
    }
}

@ToString(includeNames=true,includePackage=false)
class RecepcionesFiltro {
    Date fechaInicial
    Date fechaFinal
    Sucursal sucursal
    Proveedor proveedor
    int registros = 20


    static constraints = {
        fechaInicial nullable:true
        fechaFinal nullable: true
        sucursal nullable:true
        proveedor nullable: true
        registros size:(1..1000)

    }

    /*String toString(){
        return "$fechaInicial al $fechaFinal ${proveedor?.nombre}"
    }*/
}

class CompraParaComSearchCommand {

    Sucursal sucursal
    Long folio

    String toString(){
        return "$sucursal $folio"
    }

    static constraints = {
        
    }
}
