package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Venta

/**
 * Created by rcancino on 23/03/17.
 */

@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class NotaDeCargoDet {

    String id

    String concepto

    BigDecimal cargo = 0.0

    BigDecimal importe = 0.0

    CuentaPorCobrar	cuentaPorCobrar

    String sw2

    Date dateCreated

    Date lastUpdated


    static constraints = {
        cuentaPorCobrar nullable: true
        concepto nullable: true
    }

    static mapping={
        id generator:'uuid'
    }

    static belongsTo =[nota:NotaDeCargo]
}
