package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/*
 * Todo: Ajustes pendientes (manuales) a la base de datos de produccion para liberación 
 * 
 * A NotaDeCreditoDet:
 * - Agregar la columna de total
 * - Agregar la columna de uuid
 *
 */
@EqualsAndHashCode(includeFields = true,includes = ['id','cuentaPorCobrar'])
class NotaDeCreditoDet {

    String id

    CuentaPorCobrar	cuentaPorCobrar

    BigDecimal base = 0.0

    BigDecimal impuesto = 0.0

    BigDecimal importe = 0.0

    BigDecimal total = 0.0

    Long documento = 0

    String tipoDeDocumento

    Date fechaDocumento

    BigDecimal totalDocumento = 0.0

    BigDecimal saldoDocumento = 0.0

    String sucursal

    String comentario

    String uuid

    static constraints = {
        comentario nullable:true
        uuid nullable: true
    }

    static mapping={
        id generator:'uuid'
        fechaDocumento type: 'date'
        tipoDeDocumento maxSize:10
        sucursal maxSize: 20

    }

    static belongsTo =[nota:NotaDeCredito]
}
