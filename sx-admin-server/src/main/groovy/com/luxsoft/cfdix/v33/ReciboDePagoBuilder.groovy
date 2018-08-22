package com.luxsoft.cfdix.v33

import groovy.util.logging.Slf4j
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Pagos
import org.bouncycastle.util.encoders.Base64

import lx.cfdi.utils.DateUtils
import lx.cfdi.v33.CMetodoPago
import lx.cfdi.v33.CTipoDeComprobante
import lx.cfdi.v33.CTipoFactor
import lx.cfdi.v33.CUsoCFDI
import lx.cfdi.v33.Comprobante
import lx.cfdi.v33.ObjectFactory
import sx.cxc.AplicacionDeCobro
import sx.cxc.Cobro
import sx.core.Empresa
import sx.cxc.CobroCheque
import sx.cxc.CobroTransferencia
import sx.cxc.CuentaPorCobrar


@Slf4j
class ReciboDePagoBuilder {

    private factory = new ObjectFactory();
    private Comprobante comprobante;
    private Empresa empresa

    private Cobro cobro

    CfdiSellador33 sellador

    def build(Cobro cobro){
        this.cobro = cobro
        this.empresa = Empresa.first()
        buildComprobante()
        .buildEmisor()
        .buildReceptor()
        .buildConceptos()
        .buildCertificado()
        .buildRelacionados()
        .buildComplementoPago()
        comprobante = sellador.sellar(comprobante, empresa)
        log.debug('CFDI generado {}', CfdiUtils.serialize(comprobante))
        return comprobante
    }
    def buildComprobante(){
        log.info("Generando Recibo de pago CFDI 3.3 para Cobro {} {} - {} ", cobro.tipo)
        this.comprobante = factory.createComprobante()
        comprobante.version = "3.3"
        comprobante.tipoDeComprobante = CTipoDeComprobante.P
        comprobante.serie = "PAG-${cobro.sucursal.nombre.substring(0,2)}"
        comprobante.folio = 1
        comprobante.setFecha(DateUtils.getCfdiDate(new Date()))
        comprobante.moneda =  'XXX'
        comprobante.subTotal = 0
        comprobante.total = 0
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
        receptor.rfc = cobro.cliente.rfc
        receptor.nombre = cobro.cliente.nombre
        receptor.usoCFDI = CUsoCFDI.P_01
        comprobante.receptor = receptor
        return this
    }



    def buildConceptos(){
        Comprobante.Conceptos conceptos = factory.createComprobanteConceptos()
        Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto()
        concepto.claveProdServ = '84111506'
        concepto.cantidad = 1
        concepto.claveUnidad = 'ACT'
        concepto.descripcion = 'Pago'
        concepto.valorUnitario = 0
        concepto.importe = 0
        conceptos.concepto.add(concepto)
        comprobante.conceptos = conceptos
        return this
    }

    def buildRelacionados() {
        Comprobante.CfdiRelacionados relacionados = factory.createComprobanteCfdiRelacionados()
        relacionados.tipoRelacion = '04'
        this.cobro.aplicaciones.each { AplicacionDeCobro det ->
            Comprobante.CfdiRelacionados.CfdiRelacionado relacionado = factory.createComprobanteCfdiRelacionadosCfdiRelacionado()
            def cxc = det.cuentaPorCobrar
            def uuid = cxc.uuid
            if(!uuid)
                throw new RuntimeException("La cuenta por cobrar ${cxc.id} no tiene uuid ")
            relacionado.UUID = uuid
            relacionados.cfdiRelacionado.add(relacionado)
        }
        comprobante.cfdiRelacionados = relacionados
        return this
    }

    def buildCertificado(){
        comprobante.setNoCertificado(empresa.numeroDeCertificado)
        byte[] encodedCert=Base64.encode(empresa.getCertificado().getEncoded())
        comprobante.setCertificado(new String(encodedCert))
        return this
    }

    def buildComplementoPago() {
        Comprobante.Complemento complemento = factory.createComprobanteComplemento()

        Pagos pagos = factory.createPagos()
        pagos.version = '1.0'

        Pagos.Pago pago = factory.createPagosPago()
        pago.fechaPago = DateUtils.getCfdiDate(cobro.fecha) // Preguntar
        pago.formaDePagoP = getFormaDePago()
        pago.monedaP = cobro.moneda.currencyCode
        if(this.cobro.moneda.currencyCode != 'MXN') {
            pago.tipoCambioP = cobro.tipoDeCambio
        }
        List<AplicacionDeCobro> aplicaciones = this.cobro.aplicaciones.findAll{it.recibo == null}
        pago.monto = aplicaciones.sum 0.0, {it.importe}
        log.debug('Monto del pago: {}', pago.monto)
        pago.numOperacion = this.cobro.referencia
        if(this.cobro.cheque) {
            CobroCheque cheque = this.cobro.cheque
            pago.numOperacion = cheque.numero.toString()
        } else if(this.cobro.transferencia) {
            CobroTransferencia transferencia = this.cobro.transferencia
            // pago.rfcEmisorCtaOrd = transferencia.bancoOrigen. PENDIENTE
            // pago.ctaOrdenante = transferencia. // PENDIENTE
            // pago.ctaBeneficiario = transferencia.cuentaDestino.numero
        }
        aplicaciones.each { AplicacionDeCobro aplicacion ->
            Pagos.Pago.DoctoRelacionado relacionado = factory.createPagosPagoDoctoRelacionado()

            CuentaPorCobrar cxc = aplicacion.cuentaPorCobrar
            assert cxc.uuid, "Cuenta por cobrar ${cxc.id} no timbrada"
            relacionado.idDocumento = cxc.uuid
            relacionado.folio = cxc.folio
            relacionado.serie = cxc.cfdi.serie
            relacionado.monedaDR = cxc.moneda.currencyCode
            if(this.cobro.moneda.currencyCode != 'MXN') {
                relacionado.tipoCambioDR = cxc.tipoDeCambio
            }
            relacionado.metodoDePagoDR = 'PPD'
            relacionado.numParcialidad = 1

            BigDecimal saldoAnterior = (cxc.total - aplicacion.importe)?: cxc.total
            if(saldoAnterior <= 1) {
                saldoAnterior = cxc.total
            }
            BigDecimal saldoInsoluto = saldoAnterior - aplicacion.importe
            println "Importe saldo anterior: ${saldoAnterior} Pago: ${aplicacion.importe} Sdo Insoluto: ${saldoInsoluto}"
            relacionado.impSaldoAnt = saldoAnterior
            relacionado.impPagado = aplicacion.importe
            // relacionado.impSaldoInsoluto = saldoInsoluto
            relacionado.impSaldoInsoluto = relacionado.impSaldoAnt - relacionado.impPagado

            pago.doctoRelacionado.add(relacionado)

        }
        pagos.pago.add(pago)
        complemento.any.add(pagos)
        comprobante.complemento = complemento

    }

    def getFormaDePago(){
        switch (this.cobro.formaDePago) {
            case 'EFECTIVO':
            case 'DEPOSITO_EFECTIVO':
                return '01'
            case 'CHEQUE':
            case 'DEPOSITO_CHEQUE':
                return '02'
            case 'TRANSFERENCIA':
                return '03'
            case 'TARJETA_CREDITO':
                return '04'
            case 'TARJETA_DEBITO':
                return '28'
            case 'BONIFICACION':
            case 'DEVOLUCION':
                return '17'
            default:
                return '99'
        }

    }
}

