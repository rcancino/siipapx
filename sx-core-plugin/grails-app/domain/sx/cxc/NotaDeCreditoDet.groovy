package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true,includePackage=false, excludes = ['cargo','id','version'])
@EqualsAndHashCode(includeFields = true)
class NotaDeCreditoDet {

    String id

    BigDecimal cantidad

    String unidad

    String numeroDeIdentificacion

    String descripcion

    BigDecimal valorUnitario

    BigDecimal importe

    String comentario

    CuentaPorCobrar	cuentaPorCobrar

    String	concepto

    BigDecimal	descuento = 0.0

    String claveProdServ = '84111506'

    String claveProdDesc = 'servicios de facturación'

    String claveUnidadSat = 'ACT'

    Date dateCreated

    Date lastUpdated






    static constraints = {
        unidad maxSize:100
        numeroDeIdentificacion maxSize:50
        comentario nullable:true
        cuentaPorCobrar nullable:  true
        claveProdDesc nullable: true, maxSize: 250
        claveProdServ nullable: true, maxSize: 100
        claveUnidadSat nullable: true, maxSize: 50
    }

    static mapping={
        id generator:'uuid'
    }

    static belongsTo =[nota:NotaDeCredito]
}
