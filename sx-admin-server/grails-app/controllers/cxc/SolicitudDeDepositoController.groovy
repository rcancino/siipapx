package sx.cxc

import grails.rest.RestfulController
import groovy.transform.ToString
import grails.plugin.springsecurity.annotation.Secured


@Secured("hasRole('ROLE_POS_USER')")
class SolicitudDeDepositoController extends RestfulController{

    static responseFormats = ['json']

    SolicitudDeDepositoController(){
        super(SolicitudDeDeposito)
    }

    def pendientes() {
        log.debug('Buscando solicitudes pendientes {}', params)
        params.max = params.registros ?:50
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = SolicitudDeDeposito.where { cobro == null && autorizacion == null}
        def list = query.list(params)
        log.debug('Solicitudes pendientes: {}', list.size())
        respond list
    }
    
}

