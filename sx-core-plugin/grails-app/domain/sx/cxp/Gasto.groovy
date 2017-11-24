package sx.cxp

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Created by rcancino on 18/04/17.
 */
@ToString(includes = ['cantidad','descripcion','valorUnitario','total'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class Gasto {

    String id

    CuentaPorPagar cxp

    String descripcion

    String unidad

    BigDecimal cantidad

    BigDecimal valorUnitario

    BigDecimal importe

    String comentario

    /*Refactorizar las retenciones **/

    BigDecimal tasaIva

    BigDecimal retencionIsrTasa=0

    BigDecimal retencionIsr=0

    BigDecimal retencionIvaTasa=0

    BigDecimal retencionIva=0


    List partidas = []

    Date dateCreated
    
    Date lastUpdated

    Long sw2

    static belongsTo = [cxp: CuentaPorPagar]

    static hasMany =[partidas: ConceptoDeGasto]

    static constraints = {
        importe(scale:4)
        descripcion nullable:true
        comentario nullable: true
        sw2 nullable: true
    }

    static mapping ={
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
    }
}
