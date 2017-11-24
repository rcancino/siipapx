package sx.core

import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode(includes='id')
class Socio {

	String id

    String clave="NX"

    String nombre

    BigDecimal comisionCobrador = 0.0

    BigDecimal comisionVendedor = 0.0

    Vendedor vendedor

    Cliente cliente

    String direccion

    Long sw2

    static constraints = {

        vendedor nullable:true
        sw2 nullable:true
    }

    static mapping = {
        id generator:'uuid'
    }

    String toString() {
    	return nombre
    }
}