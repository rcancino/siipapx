package sx.inventario

import sx.core.Producto

class SectorDet {

    String id

    Producto producto

    Sector	sector

    BigDecimal	cantidad	 = 0

    String	comentario

    BigDecimal	indice	 = 0.0


    static  belongsTo = [sector:Sector]


    static constraints = {
        comentario nullable: true
        indice(scale:2)
    }


    static mapping = {
        id generator:'uuid'
    }
}
