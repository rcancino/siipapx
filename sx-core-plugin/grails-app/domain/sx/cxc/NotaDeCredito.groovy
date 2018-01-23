package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Autorizacion
import sx.core.Cliente
import sx.core.Sucursal
import sx.inventario.DevolucionDeVenta
import sx.utils.MonedaUtils


@ToString(includeNames=true,includePackage=false, includes = ['folio', 'serie','fecha','total','cfdi','nombre','partidas'])
@EqualsAndHashCode(includeFields = true,includes = ['id, serie','folio'])
class NotaDeCredito {

    String id

    Cliente cliente

    String nombre

    String serie

    Long folio = 0

    String tipo

    String tipoCartera

    Date fecha

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal tc = 1.0

    BigDecimal importe = 0.0

    BigDecimal impuesto = 0.0

    BigDecimal impuestoTasa = MonedaUtils.IVA

    BigDecimal total = 0.0

    String comentario

    List partidas = []

    Cfdi cfdi

    Sucursal sucursal

    BigDecimal descuento = 0.0

    Boolean financiero

    Cobro cobro

    String sw2

    Date dateCreated

    Date lastUpdated

    String usoDeCfdi

    String formaDePago

    Long rmd

    String rmdSucursal

    Boolean sinReferencia = false

    String createUser
    String updateUser

    static constraints = {
        serie maxSize: 20
        folio unique:'serie'
        tipoCartera minSize:3, maxSize: 3
        tipo(nullable:false,inList:['BONIFICACION', 'DEVOLUCION','DESCUENTOS_GLOBALES'])
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
        createUser nullable: true
        updateUser nullable: true
        usoDeCfdi nullable: true, maxSize:3
        formaDePago nullable: true, maxSize: 40
        rmd nullable: true
        rmdSucursal nullable: true, maxSize: 30
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

    def actualizarImportes() {
        if(!partidas) partidas = [];
        def imp = partidas.sum 0.0 , {det ->
            def sub = det.cantidad * det.valorUnitario  * tc
            sub = MonedaUtils.round(sub)
        }
        def iva = MonedaUtils.round( (imp * impuestoTasa) , 2)
        this.importe = imp
        this.impuesto = iva
        this.total = this.importe + this.impuesto
    }

    /*
    def beforeValidate(){
        if(!nombre) nombre = this.cliente.nombre
        if(!folio) folio = 0
    }


    def beforeUpdate() {
        actualizarImportes()
    }
    */

    def beforeValidate(){
        if(!nombre)
            nombre = this.cliente.nombre
    }


}
