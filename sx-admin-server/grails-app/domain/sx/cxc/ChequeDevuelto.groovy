package sx.cxc

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
// @GrailsCompileStatic
class ChequeDevuelto {

    String	id

    Long folio

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
        cxc unique: true
        cheque unique: true
    }

    static mapping = {
        id generator:'uuid'
    }
}
