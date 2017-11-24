package sx.logistica

import groovy.transform.ToString

import sx.core.Direccion
import sx.core.DireccionEntrega
import sx.core.Venta

@ToString( includes = "direccion venta",includeNames=true,includePackage=false)
class CondicionDeEnvio {

    String id

    Venta	venta

    String	condiciones

    Boolean	ocurre	 = false

    Boolean	recoleccion	 = false

    Boolean	asegurado	 = false

    Date	fechaDeEntrega = new Date()

    String	comentario

    BigDecimal	latitud	 = 0

    BigDecimal	longitud	 = 0

    Boolean parcial = false

    Date asignado

    Direccion direccion

    String zona
    String municipio
    String grupo
    

    static constraints = {
        condiciones nullable: true
        comentario nullable: true
        asignado nullable: true
        zona nullable: true, maxSize:20
        municipio nullable: true, maxSize: 100
        grupo nullable: true, maxSize:10

    }

    static embedded = ['direccion']

    static  mapping={
        id generator:'uuid'
    }

    // static belongsTo = [venta: Venta]

}
