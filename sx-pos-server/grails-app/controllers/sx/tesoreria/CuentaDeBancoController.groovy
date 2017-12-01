package sx.tesoreria


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("ROLE_CXC_USER")
class CuentaDeBancoController extends RestfulController {

    static responseFormats = ['json']

    CuentaDeBancoController() {
        super(CuentaDeBanco)
    }
}
