package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Cliente
import sx.core.Sucursal

@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class DevolucionCliente {

    String	id

    Cliente cliente

    Sucursal sucursal

    Long	documento	 = 0

    BigDecimal	importe	 = 0

    BigDecimal	impuesto	 = 0

    BigDecimal	total	 = 0

    String	formaDePago

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal	tipoDeCambio	 = 1

    String	comentario

    CuentaPorCobrar cuentaPorCobrar

    Date	fecha

    String	tipoDeDocumento

    String	sw2

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser


    static constraints = {
        tipoDeCambio(scale:6)
        comentario nullable:true
        sw2 nullable:true
        cuentaPorCobrar nullable: true
        tipoDeDocumento nullable: true
    }


    static mapping = {
        id generator:'uuid'
        cliente index: 'DEVOLUCION_IDX3'
    }
}
