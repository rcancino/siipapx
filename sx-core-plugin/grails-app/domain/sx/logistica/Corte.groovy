package sx.logistica

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.InstruccionCorte
import sx.core.Producto
import sx.security.User

@GrailsCompileStatic
@ToString( includes = "producto, asignado",includeNames=true,includePackage=false)
@EqualsAndHashCode(includes = 'id, producto, asignado')
class Corte {

    String	id

    InstruccionCorte instruccionCorte

    Surtido surtido

    Producto producto

    User	asignado

    User	empacador

    User	canceladoUsuario

    Date	inicio

    Date	fin

    Date	empacadoInicio

    Date	empacadoFin

    Date	cancelado

    Date	asignacion

    static constraints = {
        instruccionCorte nullable: true
        asignado nullable: true
        empacador nullable: true
        canceladoUsuario nullable: true
        inicio nullable: true
        fin nullable: true
        empacadoFin nullable: true
        empacadoInicio nullable: true
        cancelado nullable: true
        asignacion nullable: true
    }

    static mapping = {
        id generator: 'uuid'
    }

    static belongsTo = [surtido:Surtido]
}
