package sx.cxp

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Proveedor
import sx.utils.MonedaUtils

/**
 * Created by rcancino on 25/04/17.
 */
@ToString(includes = ['fecha','documento','proveedor','total'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class AbonoCxP {

    String id

    Date fecha = new Date()

    Proveedor proveedor

    String documento

    Currency moneda = MonedaUtils.PESOS

    BigDecimal tipoDeCambio=1.0

    BigDecimal total=0.0

    String comentario

    Long sw2

    List aplicaciones = []

    Date dateCreated

    Date lastUpdated

    static hasMany =[aplicaciones: AplicacionCxP]

    static constraints = {
        tipoDeCambio(scale:6)
        total(scale:4)
        comentario nullable:true
        sw2 nullable:true
    }

    static mapping ={
        id generator:'uuid'
        fecha type:'date' , index: 'ABONO_CXP_IDX1'
        aplicaciones cascade: "all-delete-orphan"
    }
}
