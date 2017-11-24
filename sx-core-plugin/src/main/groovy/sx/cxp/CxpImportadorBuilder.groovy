package sx.cxp

import groovy.util.slurpersupport.GPathResult
import sx.cfdi.ComprobanteFiscal
import sx.core.Direccion
import sx.core.Proveedor

/**
 * Created by rcancino on 17/09/16.
 */
class CxpImportadorBuilder {

    static String getUUID(GPathResult cfdi){
        def timbre = cfdi.'**'.find{ node -> node.name() == 'TimbreFiscalDigital' }
        if(!timbre) {
            throw new RuntimeException(message: 'Comprobante sin Timbre Fiscal Digital')
        }
        return timbre.@UUID
    }


    static ComprobanteFiscal  buscarComprobante(GPathResult cfdi){
        String UUID = getUUID(cfdi)
        return ComprobanteFiscal.where {uuid == UUID}.find()

    }

    static void resolverDatosGenerales(GPathResult cfdi, CuentaPorPagar cxp){
        cxp.documento = cxp.comprobante.folio ?: 'NA'
        cxp.fecha = cxp.comprobante.fecha
        cxp.vencimiento = cxp.fecha
        BigDecimal subTotal = cfdi['@subTotal'].toBigDecimal()
        BigDecimal total = cfdi.@total.toBigDecimal()
        cxp.subTotal = subTotal
        cxp.total = total

    }

    static void resolverProveedor(GPathResult cfdi, CuentaPorPagar cxp){
        def emisor = cfdi.'**'.find{ node -> node.name() == 'Emisor' }
        if(!emisor) {
            throw new RuntimeException(message: 'Comprobante sin Emisor')
        }
        Proveedor proveedor = Proveedor.where {rfc == emisor.@rfc.toString()}.find()
        if(!proveedor){
            proveedor = new Proveedor(
                    nombre: emisor.@nombre.toString(),
                    rfc: emisor.@rfc.toString(),
                    clave:emisor.@rfc.toString(),
                    direccion: new Direccion()
                    ).save failOnError:true, flush:true
        }
        cxp.proveedor = proveedor

    }

    static void resolverMoneda(GPathResult cfdi, CuentaPorPagar cxp){
        def moneda = cfdi.@Moneda.toString()
        if(moneda){
            cxp.moneda = Currency.getInstance(moneda)
            cxp.tipoDeCambio = cfdi.@TipoCambio.toBigDecimal()
        }

    }

    static void resolverImpuestos(GPathResult cfdi, CuentaPorPagar cxp){
        def impuestos = cfdi.'**'.find{ node -> node.name() == 'Impuestos' }
        if(impuestos && impuestos.@totalImpuestosTrasladados) {
            cxp.impuesto = impuestos.@totalImpuestosTrasladados.toBigDecimal()
        }
        def iva = cfdi.'**'.find{ node -> node.name() == 'Traslado' && node.@impuesto == 'IVA' }
        if(iva){
            cxp.impuestoTasa = iva.@tasa.toBigDecimal()
        }

    }

    static void resolverRetenciones(GPathResult cfdi, CuentaPorPagar cxp){
        def retIva = cfdi.'**'.find{ node -> node.name() == 'Retencion' && node.@impuesto == 'IVA' }
        if(retIva){
            cxp.retencionIva = retIva.@importe.toBigDecimal()
        }
        def retIsr = cfdi.'**'.find{ node -> node.name() == 'Retencion' && node.@impuesto == 'ISR' }
        if(retIsr){
            cxp.retencionIsr = retIsr.@importe.toBigDecimal()
        }

    }
}
