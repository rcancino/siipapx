package sx.cfdi

import groovy.transform.CompileStatic

import groovy.transform.Canonical
import groovy.transform.ToString

// @CompileStatic
@Canonical
@ToString(includeNames = true)
class CfdiDto {
    String id
    String serie
    String folio
    String uuid
    Date fecha
    Date enviado
    String email
    Double total
    String tipoDeComprobante

    CfdiDto(){}

    CfdiDto(Cfdi cfdi ){
        this.id = cfdi.id
        this.serie = cfdi.serie
        this.folio = cfdi.folio
        this.fecha = cfdi.fecha
        this.uuid = cfdi.uuid
        this.enviado = cfdi.enviado
        this.email = cfdi.email
        this.total = cfdi.total.toDouble()
        this.tipoDeComprobante = cfdi.tipoDeComprobante
    }
}
