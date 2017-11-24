package sx.compras

import sx.core.Inventario
import sx.core.Producto
import sx.cxp.AnalisisDevolucionCompra

class DevolucionDeCompraDet {


    String	id

    DevolucionDeCompra devolucionDeCompra

    AnalisisDevolucionCompra analisisDevolucion

    Inventario inventario

    Producto producto

    BigDecimal	cantidad	 = 0

    BigDecimal	costoDec	 = 0

    BigDecimal	importeCosto	 = 0

    String	comentario

    String sw2

    RecepcionDeCompraDet recepcionDeCompraDet

    Date dateCreated

    Date lastUpdated

    static belongsTo = [ devolucionDeCompra:DevolucionDeCompra ]

    static constraints = {
        analisisDevolucion nullable: true
        comentario nullable: true
        inventario nullable: true
        sw2 nullable: true
        recepcionDeCompraDet nullable: true

        analisisDevolucion nullable: true
    }

    static mapping = {
        id generator:'uuid'

    }

}
