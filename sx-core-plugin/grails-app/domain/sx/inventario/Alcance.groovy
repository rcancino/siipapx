package sx.inventario

import sx.core.Producto
import sx.core.Sucursal

class Alcance {

    String	id

    Sucursal sucursal

    Producto producto

    Date inicio

    Date fin

    Boolean	nacional = true

    BigDecimal kilos = 0

    BigDecimal pedidosPendiente = 0

    BigDecimal venta = 0

    BigDecimal devolucionVenta	= 0

    BigDecimal existencia	= 0


    static constraints = {       
        
    }

    static mapping={
        id generator:'uuid'
        sucursal index: 'ALC_SUCURSAL_IDX'
 
    }
}
