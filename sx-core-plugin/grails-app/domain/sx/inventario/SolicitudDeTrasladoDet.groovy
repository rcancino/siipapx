package sx.inventario

import sx.core.Producto

class SolicitudDeTrasladoDet {

    String	id

    SolicitudDeTraslado	solicitudDeTraslado

    Producto producto

    BigDecimal	solicitado	 = 0

    BigDecimal	recibido	 = 0

    Long	cortes	 = 0

    String	cortesInstruccion

    String	comentario

    String sw2

    Date dateCreated

    Date lastUpdated



    static belongsTo = [solicitudDeTraslado:SolicitudDeTraslado]

    static constraints = {
        cortesInstruccion nullable:true
        comentario nullable: true
        sw2 nullable: true

    }

    static mapping ={
        id generator:'uuid'
        producto index:'PRODUCTO_IDX'
    }

}
