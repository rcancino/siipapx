package sx.cxp

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Proveedor

@ToString(excludes = ['id,version,sw2,dateCreated,lastUpdated'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class ContraRecibo {

    String id

    Long folio

    Date fecha = new Date()

    Proveedor proveedor

    BigDecimal total = 0.0

    String comentario

    Currency moneda = Currency.getInstance('MXN')

    Date dateCreated

    Date lastUpdated

    Long sw2

    List cuentasPorPagar = []

    static  hasMany =[cuentasPorPagar: CuentaPorPagar]


    static constraints = {
        comentario nullable:true
        sw2 nullable:true
    }

    static mapping = {
        id generator:'uuid'
        fecha type:'date' ,index: 'CRIBO_IDX1'
        proveedor index: 'CRIBO_IDX1'

    }


}
