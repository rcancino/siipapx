package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields = true,includes = ['id','cuentaPorCobrar'])
class NotaDeCreditoDet {

    String id

    CuentaPorCobrar	cuentaPorCobrar

    BigDecimal importe

    Long documento = 0

    String tipoDeDocumento

    Date fechaDocumento

    BigDecimal totalDocumento = 0.0

    BigDecimal saldoDocumento = 0.0

    String sucursal

    String comentario

    static constraints = {
        comentario nullable:true
    }

    static mapping={
        id generator:'uuid'
        fechaDocumento type: 'date'
        tipoDeDocumento maxSize:10
        sucursal maxSize: 20
    }

    static belongsTo =[nota:NotaDeCredito]
}
