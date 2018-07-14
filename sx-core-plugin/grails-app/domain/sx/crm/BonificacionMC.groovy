package sx.crm

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Cliente

@ToString(excludes = 'dateCreated,lastUpdated,version',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id, nombre')
class BonificacionMC {

    String id

    Cliente cliente

    String nombre

    Integer ejercicio

    Integer mes

    Date fecha

    Integer vigenciaDias = 90

    Date vencimiento

    Date suspendido

    String suspendidoComentario

    BigDecimal importe

    BigDecimal aplicado

    BigDecimal disponible

    Date ultimoMovimiento

    Date dateCreated
    Date lastUpdated

    static constraints = {
        vencimiento nullable: true
        suspendidoComentario nullbale: true
    }

    static mapping = {
        id generator:'uuid'
        fecha type:'date'
        vencimiento type:'date'
        suspendido type:'date'
        ultimoMovimiento type:'date'
    }

    static transients = ['disponible']
}
