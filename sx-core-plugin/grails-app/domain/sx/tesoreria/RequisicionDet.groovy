package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cxp.CuentaPorPagar

@ToString(excludes ='id,version,dateCreated,lastUpdated,sw2, requisicion',includeNames=true,includePackage=false)
@EqualsAndHashCode
class RequisicionDet {

    String id

    CuentaPorPagar cxp

    BigDecimal descuentoFinanciero = 0.0

    BigDecimal analizado = 0.0

    BigDecimal aPagar = 0.0

    String comentario

    Date dateCreated

    Date lastUpdated

    static belongsTo = [requisicion:Requisicion]

    static constraints = {
        comentario nullable:true
        cxp nullable: true
    }

    static mapping ={
        id generator:'uuid'

    }

}
