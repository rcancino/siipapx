package com.luxsoft.cfdix.v33

import groovy.util.logging.Slf4j
import org.bouncycastle.util.encoders.Base64

import lx.cfdi.utils.DateUtils
import lx.cfdi.v33.CMetodoPago
import lx.cfdi.v33.CTipoDeComprobante
import lx.cfdi.v33.CTipoFactor
import lx.cfdi.v33.CUsoCFDI
import lx.cfdi.v33.Comprobante
import lx.cfdi.v33.ObjectFactory

import sx.utils.MonedaUtils
import sx.core.Empresa
import sx.core.VentaDet
import sx.cxc.CuentaPorCobrar
import sx.cxc.NotaDeCredito
import sx.inventario.DevolucionDeVenta
import sx.inventario.DevolucionDeVentaDet


@Slf4j
class NotaBuilder {

    private factory = new ObjectFactory();
    private Comprobante comprobante;
    private Empresa empresa

    private NotaDeCredito nota
    private DevolucionDeVenta rmd

    private BigDecimal subTotal = 0.0

    private BigDecimal totalImpuestosTrasladados

    CfdiSellador33 sellador

    def build(NotaDeCredito nota){
        this.nota = nota
        this.empresa = Empresa.first()
        if (nota.tipo.startsWith('DEV')){
            rmd = DevolucionDeVenta.where{ cobro == this.nota.cobro}.find()
        }
        buildComprobante()
                .buildEmisor()
                .buildReceptor()
                .buildFormaDePago()
                .buildConceptos()
                .buildImpuestos()
                .buildTotales()
                .buildCertificado()
        comprobante = sellador.sellar(comprobante, empresa)
        return comprobante
    }
    def buildComprobante(){
        log.info("Generando CFDI 3.3 para Nota de credito {} {} - {} ", nota.tipo, nota.serie, nota.folio)
        this.comprobante = factory.createComprobante()
        comprobante.version = "3.3"
        comprobante.tipoDeComprobante = CTipoDeComprobante.E
        comprobante.serie = nota.serie
        comprobante.folio = nota.folio.toString()
        comprobante.setFecha(DateUtils.getCfdiDate(new Date()))
        comprobante.moneda =  V33CfdiUtils.getMonedaCode(nota.moneda)
        if(nota.moneda != MonedaUtils.PESOS){
            comprobante.tipoCambio = nota.tc
        }
        comprobante.lugarExpedicion = empresa.direccion.codigoPostal
        return this
    }

    def buildEmisor(){
        Comprobante.Emisor emisor = factory.createComprobanteEmisor()
        emisor.rfc = empresa.rfc
        emisor.nombre = empresa.nombre
        emisor.regimenFiscal = empresa.regimenClaveSat ?:'601'
        comprobante.emisor = emisor
        return this
    }

    def buildReceptor(){
        Comprobante.Receptor receptor = factory.createComprobanteReceptor()
        receptor.rfc = nota.cliente.rfc
        receptor.nombre = nota.cliente.nombre
        receptor.usoCFDI = CUsoCFDI.G_02
        comprobante.receptor = receptor
        return this
    }

    def buildFormaDePago(){
        comprobante.metodoPago = CMetodoPago.PUE
        if (nota.tipoCartera == 'CRE') {
            comprobante.formaPago = '99'
        } else {
            // Buscar el cfdi de la devolucion
        }
        //comprobante.condicionesDePago = this.venta.tipo == 'CON' ? 'Contado' : 'Credito'
        return this
    }

    def buildConceptos() {
        if (this.nota.tipo == 'DEV') {
            buildConceptosDevolucion()
        }
    }

    def buildConceptosDevolucion(){
        /** Conceptos ***/
        this.totalImpuestosTrasladados = 0.0
        Comprobante.Conceptos conceptos = factory.createComprobanteConceptos()
        this.rmd.partidas.each { DevolucionDeVentaDet item ->
            VentaDet det = item.ventaDet

            Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto()

            def factor = det.producto.unidad == 'MIL' ? 1000 : 1
            def importe = (item.cantidad/factor * det.precio)
            def descuento = (det.descuento / 100) * importe
            def subTot = importe - descto
            def impuesto = MonedaUtils.calcularImpuesto(subTot)
            this.subTotal = this.subTotal + subTot

            concepto.with {
                claveProdServ = '84111506'
                claveUnidad = 'ACT'
                String desc = det.producto.descripcion
                noIdentificacion = det.producto.clave
                cantidad = MonedaUtils.round(item.cantidad / factor,3)
                unidad = det.producto.unidad
                descripcion = desc
                valorUnitario = MonedaUtils.round(det.precio, 2)
                importe = MonedaUtils.round(importe, 2)
                descuento = MonedaUtils.round(descuento, 2)

                // Impuestos del concepto
                concepto.impuestos = factory.createComprobanteConceptosConceptoImpuestos()
                concepto.impuestos.traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados()
                Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado traslado1
                traslado1 = factory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado()
                traslado1.base =  det.subtotal
                traslado1.impuesto = '002'
                traslado1.tipoFactor = CTipoFactor.TASA
                traslado1.tasaOCuota = '0.160000'
                traslado1.importe = impuesto

                this.totalImpuestosTrasladados += traslado1.importe
                concepto.impuestos.traslados.traslado.add(traslado1)
                conceptos.concepto.add(concepto)

                comprobante.conceptos = conceptos
            }
        }
        return this
    }

    def buildImpuestos(){
        Comprobante.Impuestos impuestos = factory.createComprobanteImpuestos()
        impuestos.setTotalImpuestosTrasladados(this.totalImpuestosTrasladados)
        Comprobante.Impuestos.Traslados traslados = factory.createComprobanteImpuestosTraslados()
        Comprobante.Impuestos.Traslados.Traslado traslado = factory.createComprobanteImpuestosTrasladosTraslado()
        traslado.impuesto = '002'
        traslado.tipoFactor = CTipoFactor.TASA
        traslado.tasaOCuota = '0.160000'

        traslado.importe = this.totalImpuestosTrasladados
        traslados.traslado.add(traslado)
        impuestos.traslados = traslados
        comprobante.setImpuestos(impuestos)
        return this
    }

    def buildTotales(){
        def total = MonedaUtils.calcularTotal(this.subTotal)
        comprobante.subTotal = MonedaUtils.round(this.subTotal, 2) //.setScale(2, RoundingMode.CEILING)
        comprobante.total = MonedaUtils.round(total,2)
        comprobante.descuento = MonedaUtils.round(venta.descuentoImporte, 2)
        return this
    }

    def buildCertificado(){
        comprobante.setNoCertificado(empresa.numeroDeCertificado)
        byte[] encodedCert=Base64.encode(empresa.getCertificado().getEncoded())
        comprobante.setCertificado(new String(encodedCert))
        return this

    }
}
