package sx.cxc

import grails.rest.RestfulController
import groovy.transform.ToString
import grails.plugin.springsecurity.annotation.Secured
import sx.tesoreria.SolicitudDeDepositoService


@Secured("hasRole('ROLE_POS_USER')")
class SolicitudDeDepositoController extends RestfulController{

    static responseFormats = ['json']

    SolicitudDeDepositoService solicitudDeDepositoService;

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

    def autorizar(SolicitudDeDeposito sol) {
        log.debug('Autorizando solicitud de deposito {}', params.id)
        def res = solicitudDeDepositoService.autorizar(sol)
        respond res;

    }
    
}

