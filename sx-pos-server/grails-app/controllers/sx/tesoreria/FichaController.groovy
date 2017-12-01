package sx.tesoreria


import grails.rest.*
import grails.converters.*

class FichaController extends RestfulController {
    static responseFormats = ['json', 'xml']
    FichaController() {
        super(Ficha)
    }
}
