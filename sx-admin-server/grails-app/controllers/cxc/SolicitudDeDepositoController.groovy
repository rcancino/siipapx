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
        log.debug('Buscando solicitudes pendientes2 {}', params)
        params.max = params.registros ?:1000
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = SolicitudDeDeposito.where {
            cobro == null && comentario == null
        }
        Date inicio = Date.parse('dd/MM/yyyy', '29/12/2017')
        // query = query.where {sw2 ==  null}
        def list = query.list(params)
        log.debug('Solicitudes pendientes: {}', list.size())
        respond list
    }

    def autorizadas() {
        log.debug('Buscando solicitudes autorizadas {}', params)
        params.max = 20
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = SolicitudDeDeposito.where {
            cobro != null
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                log.debug('Buscando por folio: {}', params)
                query = query.where{folio == params.term.toInteger()}
            } else {
                query = query.where { sucursal.nombre =~ search || banco.nombre =~ search}
            }
        }
        def list = query.list(params)
        respond list
    }

    def autorizar(SolicitudDeDeposito sol) {
        log.debug('Autorizando solicitud de deposito {}', params.id)
        def res = solicitudDeDepositoService.autorizar(sol)
        respond res;

    }
    
}

