package sx.core

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

@ToString(excludes ='id,version,dateCreated,lastUpdated,sw2,direccion',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id')
class Proveedor {

    String id

    String nombre

    String clave

    String rfc = 'XAXX010101000'

    Boolean activo = true

    String tipo = 'COMPRAS'

    String telefono1

    String telefono2

    String telefono3

    boolean nacional = true

    String	cuentaBancaria

    Integer plazo = 0

    Direccion direccion

    Long sw2

    Date dateCreated

    Date lastUpdated

    static constraints = {
        rfc size:12..13
        nombre unique: true
        clave unique: true
        tipo inList:['COMPRAS','GASTOS','COMPRA_NACIONAL','COMPRA_IMPORTACION','GASTOS_FLETES','GASTOS_PROFESIONALES','GASTOS_SERVICIOS',
                     'GASTOS_BIENES','GASTOS_ BIENES_Y_SERVICIOS']
        sw2 nullable: true
        telefono1 nullable:true ,maxSize:30
        telefono2 nullable:true ,maxSize:30
        telefono3 nullable:true ,maxSize:30
        cuentaBancaria nullable: true
        direccion nullable: true
    }

    static embedded = ['direccion']

    static mapping={
        id generator:'uuid'
    }


}
