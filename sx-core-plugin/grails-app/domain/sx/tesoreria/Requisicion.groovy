package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Autorizacion
import sx.core.Proveedor

@ToString(excludes ='id,version,dateCreated,lastUpdated,sw2,partidas',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id')
class Requisicion {

    String id

    String tipo

    Proveedor proveedor

    String afavor

    Currency moneda

    BigDecimal tipoDeCambio = 0.0

    Date fecha

    Date fechaDePago

    String formaDePago = 'CHEQUE'

    BigDecimal total = 0.0

    List partidas = []

    String comentario

    BigDecimal descuentoFinanciero = 0.0

    Long sw2

    Autorizacion autorizacion

    Date dateCreated

    Date lastUpdated

    static hasMany = [partidas: RequisicionDet]

    static constraints = {
        tipo inList: ['COMPRAS', 'GASTOS']
        total scale:4
        formaDePago inList:['TRANSFERENCIA','CHEQUE']
        comentario nullable:true
        autorizacion nullable:true
        sw2 nullable:true
    }

    static mapping = {
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        afavor index: 'REQ_IDX1'
        fechaDePago type:'date' , index: 'REQ_IDX2'
        fecha type:'date', index: 'REQ_IDX3'
        descuentoFinanciero scale: 4
    }

    def actualizar(){
        if(!afavor)
            afavor = proveedor.nombre
    }

    def beforeInsert(){
        actualizar()
    }

    /*
    enum Entidad {
        GASTOS, COMPRAS, REEMBOLSO, NOMINA, GENERICA, COMISION, COMPRA_DOLARES, TESORERIA
    }
    */
}


