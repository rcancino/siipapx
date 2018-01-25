package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true,includePackage=false, excludes = ['id','version'])
@EqualsAndHashCode(includeFields = true,includes = ['id','cuentaPorCobrar'])
class NotaDeCreditoDet {

    String id

    CuentaPorCobrar	cuentaPorCobrar

    BigDecimal importe

    String comentario

    static constraints = {
        comentario nullable:true
    }

    static mapping={
        id generator:'uuid'
    }

    static belongsTo =[nota:NotaDeCredito]
}
