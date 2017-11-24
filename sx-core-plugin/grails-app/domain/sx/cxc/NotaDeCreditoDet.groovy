package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true,includePackage=false, excludes = ['cargo','id','version'])
@EqualsAndHashCode(includeFields = true)
class NotaDeCreditoDet {

    String id

    BigDecimal cantidad

    String unidad

    String numeroDeIdentificacion

    String descripcion

    BigDecimal valorUnitario

    BigDecimal importe

    String comentario

    CuentaPorCobrar	cuentaPorCobrar

    String	concepto

    BigDecimal	descuento	 = 0

    Date dateCreated

    Date lastUpdated






    static constraints = {
        unidad maxSize:100
        numeroDeIdentificacion maxSize:50
        comentario nullable:true
        cuentaPorCobrar nullable:  true
    }

    static mapping={
        id generator:'uuid'
    }

    static belongsTo =[nota:NotaDeCredito]
}
