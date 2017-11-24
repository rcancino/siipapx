package sx.tesoreria

import sx.core.Sucursal
import sx.tesoreria.CuentaDeBanco
import sx.tesoreria.MovimientoDeCuenta

class CorteDeTarjeta {

	String id

	Long folio = 0

	Sucursal sucursal

	Date corte = new Date()

	CuentaDeBanco cuentaDeBanco

	BigDecimal total = 0.0

	String comentario

	Boolean	visaMaster	 = true

	String sw2

	List partidas = []

	List aplicaciones = []

	Date dateCreated

	Date lastUpdated



	static hasMany =[partidas: CorteDeTarjetaDet, aplicaciones: CorteDeTarjetaAplicacion]

    static constraints = {
    	sw2 nullable: true
    	comentario nullable: true
    	corte nullable: true
    }

    static mapping ={
		id generator: 'uuid'
        fecha type:'date' , index: 'CORTE_TAR_IDX1'
        corte type: 'date'
        partidas cascade: "all-delete-orphan"
        aplicaciones cascade: "all-delete-orphan"
    }
}





