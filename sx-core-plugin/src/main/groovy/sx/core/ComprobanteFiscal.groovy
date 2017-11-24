package sx.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.grails.datastore.gorm.GormValidateable


/**
 * Created by rcancino on 09/09/16.
 */
@ToString(includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['uuid','url'])
class ComprobanteFiscal implements GormValidateable{

    //Datos de CFDI...
    URL url
    String fileName
    String uuid
    String rfc
    String serie
    String folio
    String metodoDePago
    String tipoDeComprobante
    BigDecimal total

    Boolean provisionado = false

    static constraints = {
        url url:true, nullable:true
        fileName nullable:true
        uuid nullable:true,maxSize:50,unique:true
        rfc nullable:true
        serie nullable:true,maxSize:30
        folio nullable:true,maxSize:30
        metodoDePago nullable:true
        tipoDeComprobante maxSize:20, nullable:true
    }
}
