package sx.compras

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured("ROLE_COMPRAS_USER")
class AlcancesController extends RestfulController<Compra>{

    static responseFormats = ['json']

    AlcancesService alcancesService

    public AlcancesController(){
        super(Compra)
    }

    def generar() {
        def rows = alcancesService.generar()
        respond rows
    }
}
