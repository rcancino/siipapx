package sx.logistica

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import sx.security.User

@EqualsAndHashCode(includes='nombre,rfc')
@ToString( includes = "origen,entidad,nombre,documento",includeNames=true,includePackage=false)
class Surtido {

    String	id

    String	origen

    String	entidad

    String	nombre

    String	comentario

    Boolean	entregaLocal	 = true

    Boolean	parcial	 = false

    Long	documento	 = 0

    String	tipoDeVenta

    Date	fecha

    Long	folioFac	 = 0

    User	userLastUpdate

    String	clasificacionVale

    User	asignado

    Date	iniciado

    Date	corteFin

    Date	corteInicio

    Date	asignacionCorte

    User	cerro

    Date	cierreSurtido

    User	revisionUsuario

    Date	revision

    User	entrego

    Date	entregado

    User	depuradoUsuario

    Date	depurado

    Boolean	reimportado	 = false

    BigDecimal	kilos	 = 0

    Integer	prods	 = 0

    BigDecimal	tiempoSurtido	 = 0

    String	comportamiento

    BigDecimal	kilosCorte	 = 0

    Integer	prodsCorte	 = 0

    User	cortador

    BigDecimal	tiempoCorte	 = 0


    Boolean	valido	 = false

    List<Corte> cortes = []


    static constraints = {
        comentario nullable: true
        asignado nullable: true
        iniciado nullable: true
        corteFin nullable: true
        corteInicio nullable: true
        asignacionCorte nullable: true
        cerro   nullable: true
        cierreSurtido nullable: true
        revisionUsuario nullable: true
        revision nullable: true
        entrego nullable: true
        entregado nullable: true
        depuradoUsuario nullable: true
        depurado nullable: true
        tiempoSurtido nullable: true
        comportamiento nullable: true
        kilosCorte nullable: true
        prodsCorte nullable: true
        cortador nullable: true
        tiempoCorte nullable: true
        comportamiento nullable: true
        entidad inList:['PST','FAC','SOL','TRS']
    }

    static hasMany =[cortes:Corte]

    static mapping = {
        id generator: 'uuid'
        cortes cascade: "all-delete-orphan"
    }
}
