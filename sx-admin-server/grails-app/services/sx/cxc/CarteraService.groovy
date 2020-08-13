package sx.cxc

import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j

// @GrailsCompileStatic
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
@Slf4j
class CarteraService {

    List<CuentaPorCobrarDTO> generarCartera(String cartera) {
        List<CuentaPorCobrar> rows = CuentaPorCobrar
                .findAll(
                        """from CuentaPorCobrar c where c.tipo = :tipo and c.saldoReal > 0
                    order by c.fecha
                """
                , [tipo: cartera])
        List<CuentaPorCobrarDTO> res = rows.collect { cxc -> new CuentaPorCobrarDTO(cxc)}
        println "Registros: ${res.size()}"
        return res
    }
}
