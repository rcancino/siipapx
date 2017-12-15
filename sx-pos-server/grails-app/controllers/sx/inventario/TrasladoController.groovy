package sx.inventario

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import grails.converters.*

@Secured("ROLE_INVENTARIO_USER")
class TrasladoController extends RestfulController {

    static responseFormats = ['json']

    SolicitudDeTrasladoService solicitudDeTrasladoService

    TrasladoController() {
        super(Traslado)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 100;
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = Traslado.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        if(params.tipo) {
            query = query.where {tipo == params.tipo}
        }
        return query.list(params)
    }

    def print() {
        // log.debug('Imprimiendo TPS.jrxml: ID:{}', params.ID)
        params.ID = params.ID
        def pdf =  reportService.run('SolicitudDeTraslado.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Traslado.pdf')
    }
}
