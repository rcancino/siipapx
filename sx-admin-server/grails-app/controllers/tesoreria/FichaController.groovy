package sx.tesoreria

import grails.gorm.transactions.Transactional
import grails.rest.*
import grails.converters.*
import org.springframework.http.HttpStatus
import sx.cxc.CuentaPorCobrar

class FichaController extends RestfulController {
    
    static responseFormats = ['json']

    FichaService fichaService
    
    FichaController() {
        super(Ficha)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('List: {}', params)
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        PorFechaCommand command = new PorFechaCommand()
        bindData(command, params)
        def query = Ficha.where {fecha == command.fecha}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        log.debug('Cargando fichas {}', command)
        return query.list(params)
    }

    @Transactional
    def generar(FichasBuildCommand command) {
        log.debug('Generando fichas: {}', command)
        def res = fichaService.generar(command)
        // log.debug('Res: {}', res);
        respond status: HttpStatus.CREATED

    }
}

class PorFechaCommand {

    Date fecha

    String toString() {
        return fecha.format('dd/MM/yyyy')
    }
}

class FichasBuildCommand {
    Date fecha
    String formaDePago
    String tipo
    CuentaDeBanco cuenta

    String toString() {
        return "${formaDePago} ${tipo} ${fecha.format('dd/MM/yyyy')} ${cuenta.descripcion}"
    }
}

