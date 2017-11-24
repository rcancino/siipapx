package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.ComprobanteFiscal
import sx.core.Autorizacion
import sx.core.Cliente
import sx.core.Sucursal
import sx.utils.MonedaUtils


@ToString(includeNames=true,includePackage=false, includes = ['folio', 'serie','fecha','total','cfdi','nombre','partidas'])
@EqualsAndHashCode(includeFields = true,includes = ['serie','folio'])
class NotaDeCredito {

    static auditable = true

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

    BigDecimal disponible = 0.0

    String comentario

    BigDecimal aplicado  = 0.0

    List partidas

    ComprobanteFiscal cfdi

    Sucursal sucursal

    Autorizacion autorizacion

    String	tipoDeOperacion

    String	modoDeCalculo

    BigDecimal	descuento	 = 0

    Date	impreso

    Date	primeraAplicacion

    BigDecimal	diferencia	 = 0

    Date	diferenciaFecha

    Date dateCreated

    Date lastUpdated


    static constraints = {
        serie maxSize: 20
        folio unique:'serie'
        tipo(nullable:false,inList:['DESCUENTO','BONIFICACION','DEVOLUCION'])
        tc(scale:4,validator:{ val,obj ->
            if(obj.moneda!=MonedaUtils.PESOS && val <= 1.0)
                return "tipoDeCambioError"
            else
                return true
        })
        comentario nullable:true
        cfdi nullable:true
    }

    static hasMany =[partidas:NotaDeCreditoDet]

    static mapping ={
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        //requisitado formula:'select sum(x.total) from requisicion'
        //aplicado formula:'(select COALESCE(sum(x.total),0) from aplicacion x where x.abono_id=id)'
        //disponible formula:'(select total-COALESCE(SUM(x.total),0) from aplicacion x where x.abono_id=id)'
        //aplicaciones cascade: "all-delete-orphan"
    }

    //static transients = ['disponible']


    BigDecimal getTotalMN(String property){
        return "${property}"*tc

    }

    def actualizarImportes() {
        if(!partidas) partidas = [];
        def imp = partidas.sum 0.0 , {det ->
            def sub = det.cantidad * det.valorUnitario  * tc
            sub = MonedaUtils.round(sub)
        }
        this.with{
            importe = imp
            impuesto = imp * impuestoTasa
            total = importe + impuesto
        }
    }

    def beforeValidate(){
        if(!nombre) nombre = this.cliente.nombre
        if(!folio) folio = 0
    }


    def beforeUpdate() {
        actualizarImportes()
    }


}
