package sx.core

import sx.cfdi.Cfdi

class VentaIne {

    String	id

    Cfdi cfdi

    Venta	venta

    Long	contabilidad	 = 0

    String	tipo_de_comite

    String	tipo_de_proceso

    static constraints = {
        tipo_de_comite nullable: true
        tipo_de_proceso nullable: true
    }

    static  mapping ={
        id generator:'uuid'
    }
}
