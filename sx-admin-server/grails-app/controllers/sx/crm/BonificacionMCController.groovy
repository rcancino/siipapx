package sx.crm


import grails.rest.*
import grails.converters.*

class BonificacionMCController extends RestfulController<BonificacionMC> {
    static responseFormats = ['json']
    BonificacionMCController() {
        super(BonificacionMC)
    }

    def list(Integer ejercicio, Integer mes){
        respond BonificacionMC.where{ejercicio == ejercicio && mes == mes}.list()
    }
}
