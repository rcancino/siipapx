package sx.cxp

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


import sx.core.Proveedor

@ToString(includeNames=true,includePackage=false, includes = 'nombre, serie, folio, fecha ,total, uuid')
@EqualsAndHashCode(includeFields = true,includes = 'id, uuid')
class CuentaPorPagar {

    String id

    Proveedor proveedor

    String nombre

    String tipo

    String folio

    String serie

    Date fecha

    Date vencimiento

    String moneda = Currency.getInstance('MXN').currencyCode
    BigDecimal tipoDeCambio=1.0

    //Importes
    BigDecimal subTotal = 0.0
    BigDecimal descuento = 0.0
    BigDecimal impuestoTrasladado = 0.0
    BigDecimal impuestoRetenido = 0.0
    BigDecimal total = 0.0

    BigDecimal descuentoFinanciero
    Date descuentoFinancieroVto

    String uuid

    String comentario

    Boolean analizada = false
    BigDecimal importaPorPagar = 0.0

    Date dateCreated
    Date lastUpdated

    String createUser
    String updateUser

    String sw2

    ComprobanteFiscal comprobanteFiscal

    static constraints = {
        tipo inList:['COMPRAS', 'GASTOS']
        folio maxSize: 10
        serie maxSize: 10
        moneda maxSize: 5
        tipoDeCambio(scale:6)
        subTotal(scale:4)
        descuento(scale: 4)
        impuestoTrasladado(scale:4)
        impuestoRetenido(sacle:4)
        total(scale:4)
        comentario(nullable:true)
        vencimiento (validator: { vencimiento, cxp ->
            if( (vencimiento <=> cxp.fecha) < 0 )
                return "vencimientoInvalido"
            else return true
        })
        descuentoFinanciero nullable:true
        descuentoFinancieroVto nullable:true
        uuid nullable:true, unique:true
        sw2 nullable:true
        comprobanteFiscal nullable: true
    }

    static mapping ={
        id generator:'uuid'
        // gastos cascade: "all-delete-orphan"
        //requisitado formula:'(select ifnull(sum(x.requisitado),0) from requisicion_det x where x.cuenta_por_pagar_id=id)'
        //pagosAplicados formula:'(select ifnull(sum(x.importe),0) from aplicacion_de_pago x where x.cuenta_por_pagar_id=id)'
        fecha type:'date' , index: 'CXP_IDX2'
        vencimiento type:'date', index: 'CXP_IDX2'
        descuentoFinancieroVto type:'date'
    }


    // static transients = ['pendienteRequisitar',]

    BigDecimal toPesos(String property){
        return "${property}" * tipoDeCambio

    }



}
