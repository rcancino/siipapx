package sx.crm


import grails.rest.*
import grails.converters.*

class BonificacionMCController extends RestfulController<BonificacionMC> {
    static responseFormats = ['json']
    BonificacionMCController() {
        super(BonificacionMC)
    }
}
