package sx.logistica

import sx.core.Producto
import sx.security.User

class Corte {

    String	id

    InstruccionDeCorte	instruccionDeCorte

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

        instruccionDeCorte nullable: true
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
}
