package sx.logistica

class InstruccionDeCorte {

    String	id

    String	origenDet

    String	entidad

    VentaParcialDet	parcialDet

    Surtido	surtido

    BigDecimal	cantidad	 = 0

    Long	cortes	 = 0

    BigDecimal	precioCortes	 = 0

    BigDecimal	cortesAncho	 = 0

    BigDecimal	cortesLargo	 = 0

    Integer	tamaños	 = 0

    Boolean	refinado	 = false

    String	seleccionCalculo

    String	cortesTipo	 = 'CALCULADO'

    String	cortesInstruccion

    String	instruccionEmpacado

    BigDecimal	importeEmpacado	 = 0


    static constraints = {

        entidad inList:['PST','FAC','SOL','TRS']
        seleccionCalculo inList:['HORIZONTAL','VERTICAL','OPTIMO']
        cortesTipo inList:['CALCULADO','CRUZ','CARTA','MITAD','1/8','CROQUIS','DOBLE_CARTA','MEDIA_CARTA','OFICIO']
        parcialDet nullable: true

    }
    static  mapping = {
        id generator: 'uuid'
    }

}
