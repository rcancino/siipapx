package sx.contabilidad

import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode(includes='clave')
class SubTipoDePoliza {

    String clave

    String descripcion

    String procesador

    String tipo

    Integer orden = 0

    static constraints = {
        clave maxSize:20, unique:true
        procesador nullable:true
        tipo(inList:['INGRESO','EGRESO','DIARIO'])
    }

    static  mapping={
        id generator:'uuid'
    }

    String toString() {
        return "$clave ($descripcion"
    }
}
