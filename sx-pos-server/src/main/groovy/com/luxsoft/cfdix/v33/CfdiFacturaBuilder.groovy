package com.luxsoft.cfdix.v33

import groovy.util.logging.Slf4j
import org.apache.commons.logging.LogFactory
import org.bouncycastle.util.encoders.Base64

import sx.core.Empresa
import sx.core.Venta
import sx.cxc.AplicacionDeCobro
import sx.cxc.Cobro
import sx.cxc.CuentaPorCobrar


import lx.cfdi.utils.DateUtils
import lx.cfdi.v33.ObjectFactory
import lx.cfdi.v33.Comprobante

// Catalogos
import lx.cfdi.v33.CUsoCFDI
import lx.cfdi.v33.CMetodoPago
import lx.cfdi.v33.CTipoDeComprobante
import lx.cfdi.v33.CMoneda
import lx.cfdi.v33.CTipoFactor

// Utilerias
import sx.utils.MonedaUtils

import java.math.RoundingMode

/**
 * TODO: Parametrizar el regimenFiscal de
 */
@Slf4j
class CfdiFacturaBuilder {

    private factory = new ObjectFactory();
    private Comprobante comprobante;
    private Empresa empresa

    private Venta venta
    private CuentaPorCobrar cxc

    private BigDecimal subTotal = 0.0
    private BigDecimal totalImpuestosTrasladados

    CfdiSellador33 sellador

    def build(Venta factura){

        this.venta = factura
        this.cxc = venta.cuentaPorCobrar
        this.empresa = Empresa.first()
        // assert empresa, 'La empresa no esta registrada...'
        buildComprobante()
                .buildFormaDePago()
            //.ajustarFormaDePago()
                .buildEmisor()
                .buildReceptor()
                .buildConceptos()
                .buildImpuestos()
                .buildTotales()
                .buildCertificado()

        // CfdiSellador33 sellador = new CfdiSellador33()
        comprobante = sellador.sellar(comprobante, empresa)
        return comprobante
    }


    def buildComprobante(){
        log.info("Generando CFDI 3.3 para factura: ${this.cxc.tipo} - ${this.cxc.documento}")
        this.comprobante = factory.createComprobante()
        comprobante.version = "3.3"
        comprobante.tipoDeComprobante = CTipoDeComprobante.I
        comprobante.serie = "${cxc.sucursal.nombre.substring(0,2)}FAC${cxc.tipo}"
        comprobante.folio = cxc.documento.toString()
        comprobante.setFecha(DateUtils.getCfdiDate(new Date()))
        comprobante.moneda =  V33CfdiUtils.getMonedaCode(cxc.moneda)

        if(cxc.moneda != MonedaUtils.PESOS){
            comprobante.tipoCambio = cxc.tipoDeCambio
        }

        comprobante.lugarExpedicion = cxc.sucursal.direccion.codigoPostal
        return this
    }

    def buildEmisor(){
        /**** Emisor ****/
        Comprobante.Emisor emisor = factory.createComprobanteEmisor()
        emisor.rfc = empresa.rfc
        emisor.nombre = empresa.nombre
        emisor.regimenFiscal = empresa.regimenClaveSat ?:'601'
        comprobante.emisor = emisor
        return this
    }

    def buildReceptor(){
        /** Receptor ***/
        Comprobante.Receptor receptor = factory.createComprobanteReceptor()
        receptor.rfc = venta.cliente.rfc
        receptor.nombre = venta.nombre
        switch(venta.usoDeCfdi) {
            case 'G01':
                receptor.usoCFDI = CUsoCFDI.G_01
                break
            case 'G02':
                receptor.usoCFDI = CUsoCFDI.G_02
                break
            case 'G03':
                receptor.usoCFDI = CUsoCFDI.G_03
                break
            case 'P01':
                receptor.usoCFDI = CUsoCFDI.P_01
                break
            default:
                receptor.usoCFDI = CUsoCFDI.G_01
                break
        }
        comprobante.receptor = receptor
        return this
    }

    /**
     *  FIX Para CRE, CON y COD
     */
    def buildFormaDePago(){

        if (this.venta.tipo == 'CRE' || this.venta.cod) {
            comprobante.formaPago = '99'
            comprobante.metodoPago = CMetodoPago.PPD
        } else {
            switch (this.venta.formaDePago) {
                case 'EFECTIVO':
                case 'DEPOSITO_EFECTIVO':
                    comprobante.formaPago = '01'
                    break
                case 'CHEQUE':
                case 'DEPOSITO_CHEQUE':
                    log.debug('Forma de pago 02: ', venta.formaDePago)
                    comprobante.formaPago = '02'
                    break
                case 'TRANSFERENCIA':
                    comprobante.formaPago = '03'
                    break
                case 'TARJETA_CREDITO':
                    comprobante.formaPago = '04'
                    break
                case 'TARJETA_DEBITO':
                    comprobante.formaPago = '28'
                    break
                case 'BONIFICACION':
                case 'DEVOLUCION':
                    comprobante.formaPago = '17'
                    break
                default:
                    comprobante.formaPago = '99'
            }
            comprobante.metodoPago = CMetodoPago.PUE
        }
        comprobante.condicionesDePago = this.venta.tipo == 'CON' ? 'Contado' : 'Credito'
        return this
    }

    def ajustarFormaDePago(){
        if (this.venta.tipo != 'CRE' ) {
            def max  = AplicacionDeCobro
                    .executeQuery("select a.cobro from AplicacionDeCobro a where a.cuentaPorCobrar.id = ? order by a.importe desc" ,
                    [venta.cuentaPorCobrar.id ])[0]
            if(max) {
                switch (max.cobro.formaDePago) {
                    case 'EFECTIVO':
                    case 'DEPOSITO_EFECTIVO':
                        comprobante.formaPago = '01'
                        break
                    case 'CHEQUE':
                    case 'DEPOSITO_CHEQUE':
                        comprobante.formaPago = '02'
                        break
                    case 'TRANSFERENCIA':
                        comprobante.formaPago = '03'
                        break
                    case 'TARJETA_CREDITO':
                        comprobante.formaPago = '04'
                        break
                    case 'TARJETA_DEBITO':
                        comprobante.formaPago = '28'
                        break
                    case 'BONIFICACION':
                    case 'DEVOLUCION':
                        comprobante.formaPago = '17'
                        break
                    default:
                        comprobante.formaPago = '99'
                }
            }

        }
    }

    def buildConceptos(){
        /** Conceptos ***/
        this.totalImpuestosTrasladados = 0.0
        Comprobante.Conceptos conceptos = factory.createComprobanteConceptos()
        this.venta.partidas.each { det ->

            Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto()
            concepto.with {
                assert det.producto.productoSat,
                        "No hay una claveProdServ definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
                assert det.producto.unidadSat.claveUnidadSat,
                        "No hay una claveUnidadSat definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
                def factor = det.producto.unidad == 'MIL' ? 1000 : 1
                String desc = det.producto.descripcion
                claveProdServ = det.producto.productoSat.claveProdServ
                noIdentificacion = det.producto.clave
                cantidad = MonedaUtils.round(det.cantidad / factor,3)
                claveUnidad = det.producto.unidadSat.claveUnidadSat
                unidad = det.producto.unidad
                descripcion = desc
                valorUnitario = MonedaUtils.round(det.precio, 2)
                importe = MonedaUtils.round(det.importe, 2)
                descuento = MonedaUtils.round(det.descuentoImporte, 2)

                // Impuestos del concepto
                concepto.impuestos = factory.createComprobanteConceptosConceptoImpuestos()
                concepto.impuestos.traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados()
                Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado traslado1
                traslado1 = factory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado()
                traslado1.base =  det.subtotal
                traslado1.impuesto = '002'
                traslado1.tipoFactor = CTipoFactor.TASA
                traslado1.tasaOCuota = '0.160000'
                traslado1.importe = det.impuesto
                this.totalImpuestosTrasladados += traslado1.importe
                concepto.impuestos.traslados.traslado.add(traslado1)
                conceptos.concepto.add(concepto)

                comprobante.conceptos = conceptos
            }
        }
        return this
    }

    def buildImpuestos(){
        /** Impuestos **/
        Comprobante.Impuestos impuestos = factory.createComprobanteImpuestos()
        impuestos.setTotalImpuestosTrasladados(this.totalImpuestosTrasladados)
        Comprobante.Impuestos.Traslados traslados = factory.createComprobanteImpuestosTraslados()
        Comprobante.Impuestos.Traslados.Traslado traslado = factory.createComprobanteImpuestosTrasladosTraslado()
        traslado.impuesto = '002'
        traslado.tipoFactor = CTipoFactor.TASA
        traslado.tasaOCuota = '0.160000'
        //traslado.importe = venta.impuestos
        traslado.importe = this.totalImpuestosTrasladados
        traslados.traslado.add(traslado)
        impuestos.traslados = traslados
        comprobante.setImpuestos(impuestos)
        return this
    }

    def buildTotales(){
        comprobante.subTotal = MonedaUtils.round(venta.importe, 2) //.setScale(2, RoundingMode.CEILING)
        comprobante.total = MonedaUtils.round(venta.total,2)
        comprobante.descuento = MonedaUtils.round(venta.descuentoImporte, 2)
        return this
    }

    def buildCertificado(){
        comprobante.setNoCertificado(empresa.numeroDeCertificado)
        byte[] encodedCert=Base64.encode(empresa.getCertificado().getEncoded())
        comprobante.setCertificado(new String(encodedCert))
        return this

    }



    Comprobante getComprobante(){
        return this.comprobante
    }

}
