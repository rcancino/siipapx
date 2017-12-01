package sx.cxc


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_CXC_USER')")
class NotaDeCargoController extends RestfulController {
    
    static responseFormats = ['json']

    NotaDeCargoController() {
        super(NotaDeCargo)
    }
}
