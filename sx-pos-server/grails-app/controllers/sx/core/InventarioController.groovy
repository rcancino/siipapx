package sx.core

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*

@Secured("hasRole('ROLE_POS_USER')")
class InventarioController extends RestfulController {

    static responseFormats = ['json', 'xml']

    InventarioController() {
        super(Inventario)
    }

    protected List listAllResources(Map params) {
        log.debug('Localizando movimientos de inventario {}', params)
        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 200
        def query = Inventario.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { producto.clave =~ search || producto.descripcion =~ search}
        }
        def list = query.list(params)
        return list
    }

    def kardex(KardexCommand command){
        log.debug('Kardex: {}', command)
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        def inicio= new Date(params.fechaIni)
        def fin= new Date(params.fechaFin)
        def inventarios= Inventario.where{producto==producto && fecha>= inicio && fecha<= fin}.list()
        respond inventarios:inventarios, inventarioCount:100
    }



}

class KardexCommand {

    Producto producto
    Date fechaInicial
    Date fechaFinal

    String toString() {
        return "${producto.clave}  del ${fechaInicial.format('dd/MM/yyyy')} al ${fechaFinal.format('dd/MM/yyyy')}"
    }
}
