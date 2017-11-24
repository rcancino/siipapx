package sx.cfdi

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Created by rcancino on 11/09/16.
 */
@ToString(includeNames=true,includePackage=false, excludes = ['dateCreated','lastUpdated','comprobanteFiscal'])
@EqualsAndHashCode(includeFields = true,includes = ['uuid'])
class Acuse {

    URL url

    String uuid

    String estado

    String codigoEstatus

    ComprobanteFiscal comprobanteFiscal

    Date dateCreated

    Date lastUpdated

    static constraints = {
        url url:true
        estado maxSize:100
        codigoEstatus maxSize:100
        uuid unique:true
    }

    static  mapping={
        id generator:'uuid'
    }

}
