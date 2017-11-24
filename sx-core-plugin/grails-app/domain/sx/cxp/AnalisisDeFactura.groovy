package sx.cxp

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Proveedor
import sx.tesoreria.RequisicionDet

@ToString(excludes = ['id,version,sw2,dateCreated,lastUpdated'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class AnalisisDeFactura {

    String id

    Date fecha = new Date()

    CuentaPorPagar factura

    String comentario

    BigDecimal importe = 0.0

    List partidas = []

    Long sw2

    Date dateCreated
    Date lastUpdated

    static hasMany =[partidas: AnalisisDeFacturaDet]

    static constraints = {
        comentario nullable:true
        sw2 nullable:true
    }

    static mapping = {
        partidas cascade: "all-delete-orphan"
        id generator:'uuid'
        fecha type:'date' ,index: 'ANALISIS_IDX1'
    }
}
