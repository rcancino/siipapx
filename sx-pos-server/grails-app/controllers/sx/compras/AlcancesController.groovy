package sx.compras

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import groovy.transform.ToString
import sx.core.Proveedor

@Secured("ROLE_COMPRAS_USER")
class AlcancesController extends RestfulController<Compra>{

    static responseFormats = ['json']

    AlcancesService alcancesService

    public AlcancesController(){
        super(Alcance)
    }

    def list() {
        log.debug('List: {}', params)
        def query = Alcance.where{fecha == new Date()&& comentario == null}
        if(params.proveedor) {
            def search = '%' + params.proveedor + '%'
            query = query.where { nombre =~ search }
        }
        if(params.producto) {
            def search = '%' + params.producto + '%'
            query = query.where { clave =~ search || descripcion =~ search}
        }
        if(params.linea) {
            def search = '%' + params.linea + '%'
            query = query.where { linea =~ search }
        }
        if(params.marca) {
            def search = '%' + params.marca + '%'
            query = query.where { marca =~ search }
        }
        if(params.clase) {
            def search = '%' + params.clase + '%'
            query = query.where { clase =~ search }
        }
        if(params.getFloat('alcanceMenor')) {
            float menor = params.getFloat('alcanceMenor')
            query = query.where{alcance <= menor.toBigDecimal()}
        }
        if(params.getFloat('alcanceMayor')) {
            float menor = params.getFloat('alcanceMayor')
            query = query.where{alcance > menor.toBigDecimal()}
        }
        respond query.list()
    }

    def generar(GenerarAlcanceCommand command) {
        log.debug('Generando alcance: {}', command)
        def rows = alcancesService.generar(command.fechaInicial, command.fechaFinal, command.meses)
        respond rows
    }

    def generarOrden(GenerarOrdenCommand command) {
        log.debug('Generando orden de compra {}', command)
        Compra compra = alcancesService.generarOrden(command.proveedor, command.partidas)
        respond compra
    }

    def actualizarMeses(){
        int meses = params.int('meses', 2)
        log.debug('Actualizando meses: {} ', meses)
        def res = alcancesService.actualizarMeses(meses)
        Map data = ['updated': res]
        log.debug('Act: ', data)
        respond data, status:200
    }
}

@ToString(includeNames=true,includePackage=false)
class GenerarAlcanceCommand {
    Date fechaInicial
    Date fechaFinal
    Integer meses
}

class GenerarOrdenCommand {

    Proveedor proveedor

    List<Alcance> partidas

    String toString() {
        return "Generando Orden de compra para ${proveedor} con ${partidas.size()} registros de alcance"
    }
}
