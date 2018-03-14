package sx.tesoreria

import com.luxsoft.utils.MonedaUtils
import grails.gorm.transactions.Transactional
import sx.core.Folio
import sx.core.Sucursal
import sx.cxc.Cobro
import sx.cxc.CobroTarjeta


class CorteDeTarjetaService {

    def generar(Date fecha, Sucursal sucursal, List<CobroTarjeta> cobros) {
        generarCorteVisaMastercard(fecha, sucursal, cobros)
        generarCorteAmex(fecha, sucursal, cobros)
    }

    @Transactional
    def generarCorteVisaMastercard(Date fecha, Sucursal sucursal, List<CobroTarjeta> cobros) {
        List visaMastercard = cobros.findAll { it.visaMaster}
        if (!visaMastercard)
            return
        BigDecimal total = visaMastercard.sum(0.0, { it.cobro.importe})
        BigDecimal debito = visaMastercard.sum(0.0, {
            it.debitoCredito ? it.cobro.importe : 0.0
        })
        BigDecimal debitoComision = MonedaUtils.round(debito * 2.36)
        BigDecimal debitoComisionIva = MonedaUtils.round(debitoComision * MonedaUtils.IVA)

        BigDecimal credito = visaMastercard.sum 0.0 ,{ !it.debitoCredito ? it.cobro.importe : 0.0}
        BigDecimal creditoComision = MonedaUtils.round(credito * 1.46)
        BigDecimal creditoComisionIva = MonedaUtils.round(debitoComision * MonedaUtils.IVA)


        CorteDeTarjeta corte = new CorteDeTarjeta()
        corte.sucursal = sucursal
        corte.visaMaster = true
        corte.folio = Folio.nextFolio('CORTE_TARJETA', sucursal.nombre)
        corte.corte = fecha
        corte.total = total
        corte.cuentaDeBanco = CuentaDeBanco.first()
        corte.comentario = 'CORTE AUTOMATICO'

        // INGRESO
        CorteDeTarjetaAplicacion ingreso = new CorteDeTarjetaAplicacion()
        ingreso.importe = total
        ingreso.visaMaster = true
        ingreso.debitoCredito = false
        ingreso.tipo = TipoDeAplicacion.VISAMASTER_INGRESO
        corte.addToAplicaciones(ingreso)

        // DEBITO
        CorteDeTarjetaAplicacion comisionDebito = new CorteDeTarjetaAplicacion()
        comisionDebito.importe = debitoComision
        comisionDebito.visaMaster = true
        comisionDebito.debitoCredito = true
        comisionDebito.tipo = TipoDeAplicacion.DEBITO_COMISON
        corte.addToAplicaciones(comisionDebito)

        CorteDeTarjetaAplicacion comisionDebitoIva = new CorteDeTarjetaAplicacion()
        comisionDebitoIva.importe = debitoComisionIva
        comisionDebitoIva.visaMaster = true
        comisionDebitoIva.debitoCredito = true
        comisionDebitoIva.tipo = TipoDeAplicacion.DEBITO_COMISON_IVA
        corte.addToAplicaciones(comisionDebitoIva)

        // CREDITO
        CorteDeTarjetaAplicacion comisionCredito = new CorteDeTarjetaAplicacion()
        comisionCredito.importe = creditoComision
        comisionCredito.tipo = TipoDeAplicacion.CREDITO_COMISION
        comisionCredito.visaMaster = true
        comisionCredito.debitoCredito = false
        corte.addToAplicaciones(comisionCredito)

        CorteDeTarjetaAplicacion comisionCreditoIva = new CorteDeTarjetaAplicacion()
        comisionCreditoIva.tipo = TipoDeAplicacion.CREDITO_COMISION_IVA
        comisionCreditoIva.importe = creditoComisionIva
        comisionCreditoIva.visaMaster = true
        comisionCreditoIva.debitoCredito = false
        corte.addToAplicaciones(comisionCreditoIva)

        corte.save()
        visaMastercard.each { CobroTarjeta it ->
            CobroTarjeta cobroTarjeta = CobroTarjeta.get(it.id)
            cobroTarjeta.corteDeTarjeta = corte
            cobroTarjeta.corte = corte.id
            cobroTarjeta.save()
        }

    }

    @Transactional
    def generarCorteAmex(Date fecha, Sucursal sucursal, List<CobroTarjeta> cobros) {

        List<CobroTarjeta> amex = cobros.findAll{!it.visaMaster}

        if(amex) {
            BigDecimal amexIngreso = amex.sum 0.0, {it.cobro.importe}
            BigDecimal amexComision = MonedaUtils.round(amexIngreso * 3.8, 2)
            BigDecimal amexComisionIva = MonedaUtils.round(amexComision * MonedaUtils.IVA, 2)

            CorteDeTarjeta corte = new CorteDeTarjeta()
            corte.sucursal = sucursal
            corte.visaMaster = false
            corte.folio = Folio.nextFolio('CORTE_TARJETA', sucursal.nombre)
            corte.corte = fecha
            corte.total = amexIngreso
            corte.cuentaDeBanco = CuentaDeBanco.first()
            corte.comentario = 'CORTE AUTOMATICO AMEX'

            // INGRESO
            CorteDeTarjetaAplicacion ingreso = new CorteDeTarjetaAplicacion()
            ingreso.importe = amexIngreso
            ingreso.visaMaster = false
            ingreso.debitoCredito = false
            ingreso.tipo = TipoDeAplicacion.AMEX_INGRESO
            corte.addToAplicaciones(ingreso)

            // COMISION
            CorteDeTarjetaAplicacion comision = new CorteDeTarjetaAplicacion()
            comision.importe = amexComision
            comision.visaMaster = false
            comision.debitoCredito = false
            comision.tipo = TipoDeAplicacion.AMEX_COMISION
            corte.addToAplicaciones(comision)

            CorteDeTarjetaAplicacion comisionIva = new CorteDeTarjetaAplicacion()
            comisionIva.importe = amexComisionIva
            comisionIva.visaMaster = true
            comisionIva.debitoCredito = true
            comisionIva.tipo = TipoDeAplicacion.DEBITO_COMISON_IVA
            corte.addToAplicaciones(comisionIva)

            corte.save()

            amex.each {
                log.debug('Actualizando cobro: {}', it.id)
                CobroTarjeta cobroTarjeta = CobroTarjeta.get(it.id)
                cobroTarjeta.corte = corte.id
                cobroTarjeta.save()
            }

        }
    }

    @Transactional
    def ajustarCobro(Cobro cobro, CobroTarjeta tarjet){

    }

}
