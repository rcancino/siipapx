package sx.logistica

class SurtidoDet {

    String id

    String clave

    String descripcion

    Surtido surtido

    BigDecimal	cantidad	 = 0

    BigDecimal	valor	 = 0

    String	instruccionDeEntregaParcial

    EntregaParcialDet entregaParcialDet

    static belongsTo = [surtido: Surtido]

    static constraints = {
        clave nullable:true
        descripcion nullable: true
        instruccionDeEntregaParcial nullable: true
    }

     static mapping = {
        id generator: 'uuid'
    }
}
