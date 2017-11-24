package sx.inventario

import sx.core.Sucursal
import sx.core.Venta

class SolicitudDeTraslado {

    String	id

    Sucursal sucursalSolicita

    Sucursal	sucursalAtiende

    Long	documento	 = 0

    Date	fecha

    String	referencia

    Venta venta

    String	clasificacionVale

    Boolean	noAtender	 = false

    String	comentario

    List partidas =[]

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String sw2

    Date fechaInventario

    static hasMany = [partidas:SolicitudDeTrasladoDet]

    static constraints = {
        referencia nullable:true
        venta nullable: true
        clasificacionVale nullable: true
        comentario nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        sw2 nullable: true
        fechaInventario nullable: true
    }
    static mapping = {
        id generator:'uuid'
        fecha index:'FECHA_IDX'
    }

}
