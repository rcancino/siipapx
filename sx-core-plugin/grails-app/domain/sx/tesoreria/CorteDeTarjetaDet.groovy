package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import sx.cxc.Cobro

@ToString(includes = ['comentario'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class CorteDeTarjetaDet {

	String id
	
	Cobro cobro

	String comentario

	String sw2 

	Date dateCreated

	Date lastUpdated

	static belongsTo = [corte: CorteDeTarjeta]

    static constraints = {
    	sw2 nullable:true
    	comentario nullable: true
    }

	static mapping = {
		id generator: 'uuid'
	}

}
