package sx.tesoreria


import sx.cxp.CuentaPorPagar

class ComisionBancaria {

	String id
	
	Date fecha

	CuentaDeBanco cuenta

	BigDecimal comision = 0.0

	BigDecimal impuestoTasa = 0.0

	BigDecimal impuesto = 0.0

	String comentario

	String referenciaBancaria

	CuentaPorPagar cxp
	
	static hasMany =[movimientos:MovimientoDeCuenta]

    static constraints = {
		comentario(nullable:true,maxSize:200)
		referenciaBancaria(nullable:true,maxSize:100)
		cxp nullable:true
    }
	
	static mapping ={
		id generator: 'uuid'
		fecha type:'date'
		movimientos cascade:"all-delete-orphan"
	}
	
}

