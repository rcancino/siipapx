package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class ChequeDevuelto {

    String	id

    String nombre

    CobroCheque cheque

    CuentaPorCobrar	cxc

    String	comentario

    String	sw2

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    static constraints = {
        comentario nullable:true
        createUser nullable: true
        updateUser nullable: true
        sw2 nullable:true
    }

    static mapping = {
        id generator:'uuid'
    }
}
