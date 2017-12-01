package sx.inventario

import grails.rest.RestfulController
import groovy.transform.ToString
import sx.core.Proveedor
import sx.core.Sucursal
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Folio
import sx.core.Inventario



@Secured("ROLE_INVENTARIO_USER")
class SolicitudDeTrasladoController extends  RestfulController{

  static responseFormats = ['json']

  public SolicitudDeTrasladoController() {
    super(SolicitudDeTraslado)
  }

  @Override
  protected List listAllResources(Map params) {
    println ' Buscando sols: '+params
    params.sort = 'lastUpdated'
    params.order = 'desc'
    def query = SolicitudDeTraslado.where {}
    if(params.sucursal){
      query = query.where {sucursalSolicita.id ==  params.sucursal}   
    }
    if( params.sucursalAtiende) {
      query = query.where {sucursalAtiende.id ==  params.sucursalAtiende}    
    }
    if(params.documento) {
      def documento = params.int('documento')
      query = query.where {documento ==  documento}
    }
    return query.list(params)
  }
    
  protected SolicitudDeTraslado saveResource(SolicitudDeTraslado resource) {
    def username = getPrincipal().username
    if(resource.id == null) {
      def serie = resource.sucursalSolicita.clave
      resource.documento = Folio.nextFolio('SOLS',serie)
      resource.createUser = username
    }
    resource.partidas.each {
      it.comentario = resource.comentario
    }
    resource.updateUser = username
    return super.saveResource(resource)
  }

  protected SolicitudDeTraslado updateResource(SolicitudDeTraslado resource) {
    if(params.inventariar){
      def renglon = 1;
      resource.partidas.each { det ->
        // Inventario inventario = new Inventario()
        // inventario.sucursal = resource.sucursal
        // inventario.documento = resource.documento
        // inventario.cantidad = det.cantidad
        // inventario.comentario = det.comentario
        // inventario.fecha = resource.fecha
        // inventario.producto = det.producto
        // inventario.tipo = resource.tipo
        // det.inventario = inventario
        // det.renglon = renglon
        renglon++
      }
      resource.fechaInventario = new Date()
    }

    return super.updateResource(resource)
  }

  public buscarSolicitudPendiente(SolSearchCommand command){

    command.validate()
    if (command.hasErrors()) {
      respond command.errors // STATUS CODE 422
      return
    }
    def q = SolicitudDeTraslado.where{sucursalAtiende == command.sucursal && documento == command.documento }

    SolicitudDeTraslado res = q.find()
    if (res == null) {
      notFound()
      return
    }
    // respond res, status: 200
    forward action: 'show', id: res.id
  }

}


class SolSearchCommand {
  Sucursal sucursal
  Long documento

  String toString() {
    return "Criteria: ${sucursal.nombre} Folio: ${documento}";
  }

}

    
    