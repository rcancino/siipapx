package sx.compras

import grails.rest.RestfulController
import groovy.transform.ToString
import sx.core.Existencia
import sx.core.Proveedor
import sx.core.Sucursal
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Folio
import sx.core.Inventario



@Secured("ROLE_INVENTARIO_USER")
class RecepcionDeCompraController extends  RestfulController{

    static responseFormats = ['json']

    RecepcionDeCompraService recepcionDeCompraService

    public RecepcionDeCompraController() {
        super(RecepcionDeCompra)
    }

   @Override
    protected List listAllResources(Map params) {
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = RecepcionDeCompra.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}   
        }
        if(params.documento) {
            def documento = params.int('documento')

            query = query.where {documento >=  documento}
        }
        if(params.remision) {
            def remision = params.remision
            query = query.where {remision >=  remision}
        }
        
        return query.list(params)
    }

    // @Override
    protected RecepcionDeCompra saveResource(RecepcionDeCompra resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.documento = Folio.nextFolio('COMS',serie)
            resource.createUser = username
        }
        resource.partidas.each {
            it.comentario = resource.comentario
        }
        resource.updateUser = username
        return super.saveResource(resource)
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
        def q = Compra.where{sucursal == command.sucursal && folio == command.folio }
        
        Compra res = q.find()

        if (res == null) {
            notFound()
            return
        }
        // respond res, status: 200
        forward controller: 'compra', action: 'show', id: res.id
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
