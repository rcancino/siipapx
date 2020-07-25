package sx.cfdi

import groovy.transform.CompileStatic

import groovy.transform.Canonical
import groovy.transform.ToString

@CompileStatic
@Canonical
@ToString(includeNames = true)
class CfdiDto {
    String serie
    String folio
    String uuid

    CfdiDto(){}

    CfdiDto(Cfdi cfdi ){
        this.serie = cfdi.serie
        this.folio = cfdi.folio
        this.uuid = cfdi.uuid
    }
}
