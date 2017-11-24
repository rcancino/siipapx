package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Cliente
import sx.core.Sucursal

@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class ChequeDevuelto {


    String	id

    Cliente	cliente

    Sucursal	sucursal

    CuentaPorCobrar	cuentaPorCobrar

    Date	fecha

    Long	documento	 = 0

    String	tipoDocumento

    String	formaDePago

    BigDecimal	importe	 = 0

    BigDecimal	impuesto	 = 0

    BigDecimal	total	 = 0

    String	moneda = Currency.getInstance('MXN')

    BigDecimal	tipoDeCambio	 = 1

    String	comentario

    CobroCheque cheque

    String	sw2

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser



    static constraints = {
        tipoDeCambio(scale:6)
        tipoDocumento  nullable: true
        comentario nullable:true
        sw2 nullable:true
        cuentaPorCobrar nullable: true
    }


    static mapping = {
        id generator:'uuid'
        cliente index: 'CHEQUE_IDX3'
    }
}
