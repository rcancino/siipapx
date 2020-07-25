package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Cliente
import sx.core.Sucursal

/**
 * Todo: Ajustes pendientes (manuales) a la base de datos de produccion para liberación 
 * - Quitar el not-null a cuentaPorCobrar
 * - Agregar la columna de uuid a NotaDeCargoDet
 * - Agregar columna de cancelacion y cancelacion_motivo (Para cuando ya tiene asociado un CFDI)
 *
 *
 *
 */
@ToString( includes = "serie, folio, fecha, cliente", includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id','serie', 'folio'])
class NotaDeCargo {

    String	id

    Sucursal sucursal

    Cliente	cliente

    Date fecha = new Date()

    String tipo

    String serie = 'CAR'

    Long folio = -1

    String formaDePago = "POR DEFINIR"

    BigDecimal cargo = 0.0

    BigDecimal importe = 0.0

    BigDecimal impuesto = 0.0

    BigDecimal total = 0.0

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal tipoDeCambio = 1.0

    String comentario

    CuentaPorCobrar cuentaPorCobrar

    Cfdi cfdi

    String usoDeCfdi

    List<NotaDeCargoDet> partidas = []

    String	sw2

    String tipoDeCalculo = 'PORCENTAJE'

    Date cancelacion
    String cancelacionMotivo
    String cancelacionUsuario

    Date dateCreated
    Date lastUpdated
    String createUser
    String updateUser

    static constraints = {
        serie maxSize: 20
        tipoDeCambio(scale:6)
        comentario nullable:true
        updateUser nullable: true
        createUser nullable: true
        usoDeCfdi maxSize:3
        sw2 nullable:true
        cfdi nullable: true
        tipoDeCalculo inList: ['PORCENTAJE','PRORRATEO']
        cuentaPorCobrar nullable: true
        cancelacion nullable: true
        cancelacionMotivo nullable: true
        cancelacionUsuario nullable: true
    }

    static hasMany =[partidas:NotaDeCargoDet]

    static mapping = {
        id generator:'uuid'
        cliente index: 'NCARGO_IDX3'
        fecha type: 'date'
    }
}
