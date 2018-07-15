package sx.crm


import grails.rest.*
import grails.converters.*

class BonificacionMCController extends RestfulController<BonificacionMC> {
    static responseFormats = ['json']
    BonificacionMCService bonificacionMCService
    BonificacionMCController() {
        super(BonificacionMC)
    }

    def list(Integer ejercicio, Integer mes){
        respond BonificacionMC.where{ejercicio == ejercicio && mes == mes}.list()
    }

    def generar(Integer ejercicio, Integer mes) {
        log.info('Generando {} {}', ejercicio, mes)
        respond bonificacionMCService.generar(ejercicio, mes)
    }
}
