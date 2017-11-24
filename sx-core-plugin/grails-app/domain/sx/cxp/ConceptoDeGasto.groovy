package sx.cxp

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Sucursal

/**
 * Created by rcancino on 18/04/17.
 */
@ToString(includes = ['productoServicio, cantidad, importe'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class ConceptoDeGasto {

    String id

    String cuentaContable

    Sucursal sucursal

    ProductoServicio productoServicio

    BigDecimal cantidad

    BigDecimal importe

    String concepto

    String comentario

    Date dateCreated

    Date lastUpdated

    Long sw2

    static belongsTo = [gasto: Gasto]

    static constraints = {
        sucursal nullable: true
        cuentaContable nullable: true
        comentario nullable: true
        concepto nullable: true
        productoServicio nullable: true
        sw2 nullable: true

    }

    static  mapping={
        id generator:'uuid'
    }


    

}
