package sx.cxc

import grails.rest.RestfulController
import groovy.transform.ToString
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio
import sx.core.Sucursal

@Secured("hasRole('ROLE_POS_USER')")
class SolicitudDeDepositoController extends RestfulController{

    static responseFormats = ['json']

    SolicitudDeDepositoController(){
        super(SolicitudDeDeposito)
    }

    @Override
    protected List listAllResources(Map params) {

        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 50
        def hoy = new Date()
        def query = SolicitudDeDeposito.where {}
        
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}   
        }

        if(params.pendientes) {
            query = query.where{ cobro == null || lastUpdated == hoy}
        }

        
        return query.list(params)
    }
    

    protected SolicitudDeDeposito saveResource(SolicitudDeDeposito resource) {
        resource.total = resource.cheque + resource.efectivo + resource.transferencia
        log.debug('Salvando solicitud: {}', resource)
        if(resource.id == null) {
            def serie = resource.sucursal.nombre
            resource.folio = Folio.nextFolio('SOLICITUDES_DEPOSITO',serie)
        }
        return super.saveResource(resource)
    }

    protected SolicitudDeDeposito updateResource(SolicitudDeDeposito resource) {
        resource.total = resource.cheque + resource.efectivo + resource.transferencia
        resource.comentario = null;
        return super.updateResource(resource)
    }

    def pendientes(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:10
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def sols = SolicitudDeDeposito.where{ sucursal == sucursal && cobro == null}.list(params)
        respond sols
    }
    
}

