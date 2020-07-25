package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Cliente
import sx.core.Sucursal
import sx.utils.MonedaUtils

/*
 * Todo: Ajustes pendientes (manuales) a la base de datos de produccion para liberación 
 * - Permitir nulos en la columna de cobro
 * - Agrgar las columna de concepto para aumentar la clasificación de Notas de crédito
 * - Agregar columna de cancelacion y cancelacion_motivo (Para cuando ya tiene asociado un CFDI)
 * 
 *
 */
@ToString(includeNames=true,includePackage=false, excludes = ['id, version, partidas'])
@EqualsAndHashCode(includeFields = true,includes = ['id, serie','folio'])
class NotaDeCredito {

    String id

    Cliente cliente

    String nombre

    String concepto

    String serie

    Long folio = -1

    String tipo

    String tipoCartera

    String tipoDeCalculo = 'PORCENTAJE'

    String baseDelCalculo = 'SALDO'

    Date fecha = new Date()

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal tc = 1.0

    BigDecimal importe = 0.0

    BigDecimal impuesto = 0.0

    BigDecimal impuestoTasa = MonedaUtils.IVA

    BigDecimal total = 0.0

    String comentario

    List<NotaDeCreditoDet> partidas = []

    Sucursal sucursal

    BigDecimal descuento = 0.0

    BigDecimal descuento2 = 0.0

    Boolean financiero = false

    Cobro cobro

    String sw2

    String usoDeCfdi

    String formaDePago

    Long rmd

    String rmdSucursal

    Boolean sinReferencia = false

    Cfdi cfdi

    Date cancelacion
    String cancelacionMotivo
    String cancelacionUsuario

    String createUser
    String updateUser
    Date dateCreated
    Date lastUpdated


    static constraints = {
        serie maxSize: 20
        folio unique:'serie'
        tipoCartera inList: ['CRE','CON', 'CHE', 'JUR', 'COD']
        tipo(nullable:false,inList:['BONIFICACION', 'DEVOLUCION'])
        tc(scale:6,validator:{ val,obj ->
            if(obj.moneda!=MonedaUtils.PESOS && val <= 1.0)
                return "tipoDeCambioError"
            else
                return true
        })
        comentario nullable:true
        cfdi nullable:true
        cobro nullable: true
        sw2 nullable: true
        usoDeCfdi nullable: true, maxSize:3
        formaDePago nullable: true, maxSize: 40
        rmd nullable: true
        rmdSucursal nullable: true, maxSize: 30
        nombre nullable: true
        tipoDeCalculo nullable: true, maxSize: 20
        baseDelCalculo nullable: true, maxSize: 20
        concepto nullable: true, maxSize: 20
        createUser nullable: true
        updateUser nullable: true
        cancelacion nullable: true
        cancelacionMotivo nullable: true
        cancelacionUsuario nullable: true
    }

    static hasMany =[partidas:NotaDeCreditoDet]

    static mapping ={
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        fecha type: 'date'
    }

    BigDecimal getTotalMN(String property){
        return "${property}"*tc
    }

    def beforeInsert() {
        updateNombre();
    }

    def updateNombre() {
        if(!this.nombre && this.cliente) {
            this.nombre = this.cliente.nombre;
        }
    }


}
