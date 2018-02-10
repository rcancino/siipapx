package sx.cxc

import grails.rest.RestfulController
import groovy.transform.ToString
import grails.plugin.springsecurity.annotation.Secured
import sx.core.AppConfig
import sx.core.Folio
import sx.core.Sucursal
import sx.tesoreria.SolicitudDeDepositoService


@Secured("hasRole('ROLE_POS_USER')")
class SolicitudDeDepositoController extends RestfulController{

    static responseFormats = ['json']

    SolicitudDeDepositoService solicitudDeDepositoService;

    SolicitudDeDepositoController(){
        super(SolicitudDeDeposito)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('List: {}', params)
        params.max = params.registros ?:1000
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = SolicitudDeDeposito.where {}
        if (params.cartera) {
            query = query.where { tipo == params.cartera}
        }
        if (params.pendientes) {
            if (params.boolean('pendientes'))
                query = query.where {cobro == null}
            else {
                query = query.where {cobro != null}
            }
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where { folio == params.term.toInteger() }
            } else {
                query = query.where { sucursal.nombre =~ search || banco.nombre =~ search  }
            }
        }
        return query.list(params)
    }

    def pendientes() {
        log.debug('Buscando solicitudes pendientes2 {}', params)
        params.max = params.registros ?:1000
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = SolicitudDeDeposito.where {
            cobro == null && comentario == null
        }
        def list = query.list(params)
        // log.debug('Solicitudes pendientes: {}', list.size())
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
        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                // log.debug('Buscando por folio: {} o total: {}', params.term.toInteger(), params.term.toBigDecimal())
                query = query.where { folio == params.term.toInteger() }
            } else {
                query = query.where { sucursal.nombre =~ search || banco.nombre =~ search  }
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



    @Override
    protected Object createResource() {
        SolicitudDeDeposito sol = new SolicitudDeDeposito()
        bindData sol, getObjectToBind()
        sol.sucursal = Sucursal.where { clave == 1}.find()
        sol.fecha = new Date()
        return sol
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
}

