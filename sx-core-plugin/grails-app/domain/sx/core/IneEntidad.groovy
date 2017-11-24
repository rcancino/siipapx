package sx.core

class IneEntidad {

    String	id

    String	ambito

    String	clave

    VentaIne	complementoINE

    String	contabilidades

   // Long	entidades_idx	 = 0

    static constraints = {


    }

    static  mapping ={
        id generator:'uuid'
    }

}
