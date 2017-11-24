package sx.compras

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import sx.core.Sucursal
import sx.core.Proveedor



@ToString(excludes = 'dateCreated,lastUpdated,version,partidas',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id,sucursal,folio')
class Compra {

    static auditable = false
    static stampable = true

	String id

	Proveedor proveedor

    Sucursal sucursal

	Long folio

    Date fecha

    Date entrega

	String comentario

	Date ultimaDepuracion

	BigDecimal importeBruto = 0.0

	BigDecimal importeNeto = 0.0

	BigDecimal importeDescuento = 0.0
    
	BigDecimal impuestos = 0.0

	BigDecimal total = 0.0

    Currency moneda = Currency.getInstance('MXN')

	BigDecimal tipoDeCambio = 1.0

    Boolean pendiente = true

	Boolean consolidada = false

    Boolean centralizada = false

    Boolean especial= false

    Boolean nacional = true

    List partidas  = []

    String sw2

	Date dateCreated

	Date lastUpdated

    String createdBy

    String lastUpdatedBy


    static embedded = ['log']

    static constraints = {
    	comentario nullable:true
    	ultimaDepuracion nullable:true
    	entrega nullable:true
        folio unique:'sucursal'
        sw2 nullable:true
        createdBy nullable: true
        lastUpdatedBy nullable: true

    }

    static hasMany =[partidas:CompraDet]

    static mapping = {
    	id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        fecha type:'date', index: 'COMPRA_IDX1'
        entrega type:'date'
        folio index: 'COMPRA_IDX2'
    }

}
