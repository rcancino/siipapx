package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Autorizacion
import sx.core.Cliente
import sx.core.Sucursal
import sx.utils.MonedaUtils


@ToString(includeNames=true,includePackage=false, includes = ['folio', 'serie','fecha','total','cfdi','nombre','partidas'])
@EqualsAndHashCode(includeFields = true,includes = ['serie','folio'])
class NotaDeCredito {

    String id

    Cliente cliente

    String nombre

    String serie

    Long folio

    String tipo

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

    Date impreso

    Cobro cobro

    Date dateCreated

    Date lastUpdated

    static constraints = {
        serie maxSize: 20
        folio unique:'serie'
        tipo(nullable:false,inList:['BONIFICACION','DEVOLUCION'])
        tc(scale:4,validator:{ val,obj ->
            if(obj.moneda!=MonedaUtils.PESOS && val <= 1.0)
                return "tipoDeCambioError"
            else
                return true
        })
        comentario nullable:true
        cfdi nullable:true
        cobro nullable: true
    }

    static hasMany =[partidas:NotaDeCreditoDet]

    static mapping ={
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        fecha type: 'date'
        impreso type: 'date'
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


}
