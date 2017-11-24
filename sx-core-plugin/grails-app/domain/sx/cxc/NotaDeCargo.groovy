package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Cliente

/**
 * Created by rcancino on 23/03/17.
 */
@ToString( includes = "cliente,total",includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class NotaDeCargo {

    String	id

    Cliente	cliente

    Date	fecha

    Long	documento	 = 0

    BigDecimal	importe	 = 0

    BigDecimal	impuesto	 = 0

    BigDecimal	total	 = 0

    String	formaDePago

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal	tipoDeCambio	 = 1

    String	comentario

    CuentaPorCobrar cuentaPorCobrar

    List partidas = []

    String	tipoDeDocumento

    BigDecimal	cargo	 = 0

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

    static hasMany =[partidas:NotaDeCargoDet]

    static mapping = {
        id generator:'uuid'
        cliente index: 'NCARGO_IDX3'
    }
}
