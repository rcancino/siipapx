package sx.cxp

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


/**
 * Created by rcancino on 06/04/17.
 */
@ToString(includes = ['fecha','importe'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
abstract class  AplicacionCxP {

    String id

    Date fecha

    AbonoCxP abono

    CuentaPorPagar cxp

    BigDecimal importe

    String comentario

    String sw2

    Date dateCreated

    Date lastUpdated

    static belongsTo = [abono: AbonoCxP]

    static constraints = {
        comentario nullable:true
        sw2 nullable: true
    }

    static mapping = {
        id generator:'uuid'
        fecha type:'date' ,index: 'CXP_APLICACION_IDX1'
    }
}
