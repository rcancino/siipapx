package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Cliente

/**
 * Created by rcancino on 23/03/17.
 */
@ToString( includes = "cliente, fecha, total", includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id','documento', 'fecha'])
class NotaDeCargo {

    String	id

    Cliente	cliente

    Date fecha = new Date()

    Long documento

    BigDecimal importe

    BigDecimal impuesto

    BigDecimal total

    String formaDePago

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal tipoDeCambio = 1.0

    String comentario

    CuentaPorCobrar cuentaPorCobrar

    List partidas = []

    String tipo

    BigDecimal cargo = 0.0

    String	sw2 = ""

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser


    static constraints = {
        tipoDeCambio(scale:6)
        comentario nullable:true
        sw2 nullable:true

    }

    static hasMany =[partidas:NotaDeCargoDet]

    static mapping = {
        id generator:'uuid'
        cliente index: 'NCARGO_IDX3'
    }
}
