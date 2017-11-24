package sx.cxp

import sx.cfdi.ComprobanteFiscal

/**
 * Created by rcancino on 19/04/17.
 */
class NotaCxP extends AbonoCxP{

    ConceptoDeNotaCxP concepto

    ComprobanteFiscal comprobante

    BigDecimal tipoDeCambio=1.0

    BigDecimal importe = 0.0

    BigDecimal impuesto=0.0

    static constraints = {
        tipoDeCambio(scale:6)
        importe(scale:4)
        impuesto(scale:4)
        comprobante nullable:true, unique:true
    }

}

enum ConceptoDeNotaCxP{
    DEVLUCION
    ,DESCUENTO_FINANCIERO
    ,DESCUENTO
    ,DESCUENTO_ANTICIPO
    ,BONIFICACION
}
