package sx.logistica

class EntregaParcialDet {

    String	id

    String ventaDet

    String producto

    String clave

    String descripcion

    BigDecimal	cantidad	 = 0

    BigDecimal	valor	 = 0

    BigDecimal entregado = 0

    String	instruccionDeEntregaParcial

    EntregaParcial entregaParcial

    static belongsTo = [entregaParcial: EntregaParcial]

    static constraints = {
        ventaDet nullable: true
        producto nullable: true
        clave nullable:true
        descripcion nullable: true
        instruccionDeEntregaParcial nullable: true
    }

    static mapping = {
        id generator:'uuid'
       
    }


}
