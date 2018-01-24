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

    private BigDecimal subTotalAcumulado = 0.0
    private BigDecimal descuentoAcumulado = 0.0
    private BigDecimal totalImpuestosTrasladados = 0.0


    CfdiSellador33 sellador

    def build(NotaDeCredito nota){
        this.nota = nota
        this.empresa = Empresa.first()
        subTotalAcumulado = 0.0
        descuentoAcumulado = 0.0
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
        if (this.nota.tipo.startsWith('DEV')) {
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
            importe = MonedaUtils.round(importe)

            def descuento = (det.descuento / 100) * importe
            descuento = MonedaUtils.round(descuento)

            def subTot =  importe - descuento

            def impuesto = subTot * MonedaUtils.IVA
            impuesto = MonedaUtils.round(impuesto)

            // this.descuentoAcumulado = this.descuentoAcumulado + descuento
            concepto.descuento = descuento
            concepto.claveProdServ = '84111506'
            concepto.claveUnidad = 'ACT'
            concepto.noIdentificacion = det.producto.clave
            concepto.cantidad = MonedaUtils.round(item.cantidad / factor,3)
            concepto.unidad = det.producto.unidad
            concepto.descripcion = det.producto.descripcion
            concepto.valorUnitario = MonedaUtils.round(det.precio, 2)
            concepto.importe = importe

            concepto.impuestos = factory.createComprobanteConceptosConceptoImpuestos()
            concepto.impuestos.traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados()

            Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado traslado1
            traslado1 = factory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado()
            traslado1.base =  subTot
            traslado1.impuesto = '002'
            traslado1.tipoFactor = CTipoFactor.TASA
            traslado1.tasaOCuota = '0.160000'
            traslado1.importe = impuesto


            concepto.impuestos.traslados.traslado.add(traslado1)
            conceptos.concepto.add(concepto)

            // Acumulados
            this.totalImpuestosTrasladados += traslado1.importe
            this.subTotalAcumulado = this.subTotalAcumulado + subTot
            this.descuentoAcumulado = this.descuentoAcumulado + descuento

        }
        comprobante.conceptos = conceptos
        return this
    }

    def buildImpuestos(){
        Comprobante.Impuestos impuestos = factory.createComprobanteImpuestos()
        impuestos.setTotalImpuestosTrasladados(MonedaUtils.round(this.totalImpuestosTrasladados))
        Comprobante.Impuestos.Traslados traslados = factory.createComprobanteImpuestosTraslados()
        Comprobante.Impuestos.Traslados.Traslado traslado = factory.createComprobanteImpuestosTrasladosTraslado()
        traslado.impuesto = '002'
        traslado.tipoFactor = CTipoFactor.TASA
        traslado.tasaOCuota = '0.160000'

        traslado.importe = MonedaUtils.round(this.totalImpuestosTrasladados)
        traslados.traslado.add(traslado)
        impuestos.traslados = traslados
        comprobante.setImpuestos(impuestos)
        return this
    }

    def buildTotales(){

        comprobante.descuento = this.descuentoAcumulado
        comprobante.subTotal = this.subTotalAcumulado
        comprobante.total = this.subTotalAcumulado + this.totalImpuestosTrasladados

        return this
    }

    def buildCertificado(){
        comprobante.setNoCertificado(empresa.numeroDeCertificado)
        byte[] encodedCert=Base64.encode(empresa.getCertificado().getEncoded())
        comprobante.setCertificado(new String(encodedCert))
        return this

    }
}
