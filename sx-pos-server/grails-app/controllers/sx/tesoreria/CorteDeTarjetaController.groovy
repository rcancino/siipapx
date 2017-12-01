package sx.tesoreria


import grails.rest.*
import grails.converters.*

class CorteDeTarjetaController extends RestfulController {
    static responseFormats = ['json', 'xml']
    CorteDeTarjetaController() {
        super(CorteDeTarjeta)
    }
}
