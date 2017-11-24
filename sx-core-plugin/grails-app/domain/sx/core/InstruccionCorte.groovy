package sx.core

import groovy.transform.ToString

@ToString(includes = 'cantidad,precio,instruccion', includeNames = true, includePackage = false)
class InstruccionCorte {

    String id

    Long cantidad = 0

    BigDecimal	precio = 0

    BigDecimal ancho = 0

    BigDecimal largo = 0

    // Integer tamaños = 0

    Boolean refinado = false

    String seleccionCalculo

    String tipo = 'CALCULADO'

    String instruccion

    String instruccionEmpacado

    VentaDet ventaDet

    static constraints = {
        seleccionCalculo nullable: true
        tipo nullable: true, maxSize: 50//inList:['CALCULADO','CRUZ','CARTA','MITAD','1/8','CROQUIS','DOBLE_CARTA','MEDIA_CARTA','OFICIO']
        instruccion nullable: true
        instruccionEmpacado nullable: true
    }

    static  mapping ={
        id generator:'uuid'
    }

    // static belongsTo = [ventaDet: VentaDet]
    static belongsTo = [ventaDet:Venta]

}
