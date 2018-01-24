package sx.cxc

import grails.rest.RestfulController

import grails.plugin.springsecurity.annotation.Secured
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

    NotaDeCreditoController(){
        super(NotaDeCredito)
    }

    @Override
    protected Object createResource() {
        log.debug('Preprando persistencia de nota de crediot params: {}', params)
        NotaDeCredito nota = new NotaDeCredito()
        bindData nota, getObjectToBind()
        Sucursal sucursal = Sucursal.where { nombre == 'OFICINAS'}.find()
        nota.sucursal = sucursal
        nota.serie = nota.tipo
        return nota
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 15
        params.sort = 'lastUpdated'
        params.order = 'desc'
        return super.listAllResources(params)
    }

    @Override
    protected Object saveResource(Object resource) {
        log.debug('Salvando nota de credito: {}', resource)
        if (params.devolucion) {
            DevolucionDeVenta devolucion = DevolucionDeVenta.get(params.devolucion)
            return notaDeCreditoService.generarNotaDeDevolucion(resource, devolucion)
        } else {
            throw new IllegalArgumentException('Pendiente de implementar')
        }
    }

    def buscarRmd() {
       // log.debug('Localizando rmd {}', params)
        params.max = 10
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def cliente = params.cliente
        def query = DevolucionDeVenta.where{ venta.cliente.id == cliente}
        if(params.term) {
            if(params.term.isInteger()) {
                query = query.where{documento == params.term.toInteger()}
            }
        }
        respond query.list(params)
    }

    def handleNotaDeCreditoException(NotaDeCreditoException sx) {
        respond ([message: sx.message], status: 422)
    }

}
