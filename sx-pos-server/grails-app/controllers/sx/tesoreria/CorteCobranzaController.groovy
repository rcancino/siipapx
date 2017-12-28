package sx.tesoreria

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_POS_USER')")
class CorteCobranzaController extends RestfulController {

    static responseFormats = ['json']

    CorteCobranzaService corteCobranzaService

    CorteCobranzaController() {
      super(CorteCobranza)
    }

    @Override
    protected List listAllResources(Map params) {

        log.debug('Buscando cortes con {}', params)
        params.sort = 'corte'
        params.order = 'asc'
        params.max = 100
        def query = CorteCobranza.where {}
        if (params.fecha) {
            CobranzaPorFecha cc = new CobranzaPorFecha()
            def result = bindData(params, cc)
            log.debug('Binding: {}', result)
            query = query.where {fecha == cc.fecha}
        }
        return query.list(params)
    }

    protected CorteCobranza saveResource(CorteCobranza resource) {
        def username = getPrincipal().username
        resource.createUser = username
        resource.updateUser = username
        resource.corte = new Date()
        if(resource.anticipoCorte) {
            resource.fechaDeposito = resource.fecha - 1
        } else {
            resource.fechaDeposito = resource.fecha
        }
        return super.saveResource(resource)
    }

    protected CorteCobranza updateResource(CorteCobranza resource) {
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }

    def cortes(CortesPorFecha command) {
        log.debug('Buscando cortes del {}', command.fecha)
        params.sort = 'corte'
        params.order = 'asc'
        def query = CorteCobranza.where {fecha == command.fecha}
        respond query.list(params)
    }

    def preparar(CortesPorFecha command){
        log.debug('Preparando corte: {}', command)
        CorteCobranza corte = corteCobranzaService.prepararCorte(command.formaDePago, command.tipo, command.fecha);
        respond corte
    }

}

class CortesPorFecha {
    Date fecha
    String formaDePago
    String tipo

    String toString() {
        return "${formaDePago} ${fecha.format('dd/MM/yyyy')}"
    }
}

class CobranzaPorFecha {
    Date fecha

    String toString() {
        return "${fecha.format('dd/MM/yyyy')}"
    }
}
