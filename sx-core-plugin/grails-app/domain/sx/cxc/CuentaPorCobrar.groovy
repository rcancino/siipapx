package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Cliente
import sx.core.Sucursal

@ToString(excludes = ['id,version,sw2,dateCreated,lastUpdated'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class CuentaPorCobrar {

    String	id

    Cliente	cliente

    Sucursal sucursal

    String tipoDocumento

    Long	documento	 = 0

    BigDecimal	importe	 = 0

    BigDecimal descuentoImporte = 0

    BigDecimal subtotal = 0

    BigDecimal	impuesto	 = 0

    BigDecimal	total	 = 0

    String	formaDePago

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal	tipoDeCambio	 = 1

    BigDecimal	cargo	 = 0

    String	comentario

    String	sw2

    String	uuid

    String tipo

    Cfdi cfdi

    Date fecha

    Date vencimiento

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    BigDecimal pagos = 0.0

    BigDecimal saldo = 0.0

    Integer atraso = 0

    Boolean chequePostFechado = false

    Date cancelada

    String cancelacionUsuario

    String cancelacionMotivo

    VentaCredito credito

    Date juridico = null

    Integer atrasoCalculado
    
    BigDecimal saldoReal

    static constraints = {
        tipoDocumento inList:['VENTA','CHEQUE_DEVUELTO','DEVOLUCION_CLIENTE','NOTA_DE_CARGO']
        tipo nullable:true, inList:['CON','COD','CRE','CHE','JUR','PSF','INE','OTR','ACF','ANT','AND']
        documento maxSize: 20
        uuid nullable:true, unique:true
        tipoDeCambio(scale:6)
        uuid nullable: true
        comentario nullable:true
        sw2 nullable:true
        cfdi nullable: true
        chequePostFechado nullable: true
        cancelada nullable: true
        cancelacionUsuario nullable: true
        cancelacionMotivo nullable: true
        credito nullable: true
        vencimiento nullable: true
        juridico nullable: true
    }


    static mapping = {
        id generator:'uuid'
        fecha type:'date' ,index: 'CXC_IDX1'
        vencimiento type: 'date'
        cancelada type: 'date'
        juridico type: 'date'

        // Formulas
        pagos formula:'(select COALESCE(sum(x.importe),0) from aplicacion_de_cobro x where x.cuenta_por_cobrar_id=id)'
        saldoReal formula:'total - (select COALESCE(sum(x.importe),0) from aplicacion_de_cobro x where x.cuenta_por_cobrar_id=id) '
        atrasoCalculado formula: 'IF( TO_DAYS(CURRENT_DATE()) - TO_DAYS(IFNULL(vencimiento, fecha))  < 0, 0, TO_DAYS(CURRENT_DATE()) - TO_DAYS( IFNULL(vencimiento, fecha)) ) '
        
        // Indexs
        cliente index: 'CXC_IDX3'
        formaDePago index: 'CXC_IDX9'
        vencimiento index: 'CXC_IDX10'
    }

    static transients = ['saldo','folio','atraso']

    BigDecimal getSaldo() {
        return total - pagos
    }

    String getFolio() {
        return "${tipo}-${documento}"
    }

    Integer getAtraso() {
        if (getSaldo() && vencimiento) {
            def hoy  = new Date()
            def res =  hoy - vencimiento
            return  res <= 0 ? 0 : res;
        }
        return 0
    }




}
