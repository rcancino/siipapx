package sx.cxc

import com.luxsoft.cfdix.v33.ReciboDePagoBuilder
import grails.gorm.transactions.Transactional
import lx.cfdi.v33.Comprobante
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService
import sx.core.Folio

@Transactional
class CobroService {

    ReciboDePagoBuilder reciboDePagoBuilder

    CfdiService cfdiService

    CfdiTimbradoService cfdiTimbradoService

    def save(Cobro cobro) {
        if(cobro.cheque) {
            cobro.referencia = cobro.cheque.numero
        }
        if(cobro.tarjeta) {
            cobro.referencia = cobro.tarjeta.validacion.toString()
        }
        setComisiones(cobro)
        if(cobro.id == null) {
            cobro.fecha = new Date()
        }
        cobro.save flush:true

    }


    def generarCobroDeContado(CuentaPorCobrar cxc, List<Cobro> cobros) {
        def saldo = cxc.saldo
        cobros.each { cobro ->
            def disponible = cobro.disponible
            def importe = disponible < saldo ? disponible : saldo
            def aplicacion = new AplicacionDeCobro()
            aplicacion.cuentaPorCobrar = cxc
            aplicacion.fecha = new Date()
            aplicacion.importe = importe
            cobro.addToAplicaciones(aplicacion)
            disponible = disponible - aplicacion.importe

            if(disponible < 10 && disponible > 0.01) {
                cobro.diferencia = disponible
                cobro.diferenciaFecha = new Date()
            }
            setComisiones(cobro)
            cobro.save failOnError: true, flush: true
            saldo = saldo - importe
        }
        return cxc
    }

    def registrarAplicacion(Cobro cobro, List<CuentaPorCobrar> pendientes){
        def fecha = new Date()
        def disponible = cobro.disponible
        if (disponible <= 0)
            return cobro
        pendientes.each { cxc ->
            def saldo = cxc.saldo
            if (disponible > 0) {
                def importe = saldo <= disponible ? saldo : disponible
                AplicacionDeCobro aplicacion = new AplicacionDeCobro()
                aplicacion.importe = importe
                aplicacion.formaDePago = cobro.formaDePago
                aplicacion.cuentaPorCobrar = cxc
                aplicacion.fecha = fecha

                cobro.addToAplicaciones(aplicacion)
                if(cobro.primeraAplicacion == null)
                    cobro.primeraAplicacion = fecha

                disponible = disponible - importe
            }
        }
        cobro.save flush: true
        return cobro
    }

    def registrarAplicacion2(Cobro cobro){

        def disponible = cobro.disponible
        if (disponible <= 0)
            return cobro
        def pendientes = cobro.pendientesDeAplicar
        def fecha = cobro.fechaDeAplicacion ?: new Date()
        pendientes.each { cxc ->
            def saldo = cxc.saldo
            if (disponible > 0) {
                def importe = saldo <= disponible ? saldo : disponible
                AplicacionDeCobro aplicacion = new AplicacionDeCobro()
                aplicacion.importe = importe
                aplicacion.formaDePago = cobro.formaDePago
                aplicacion.cuentaPorCobrar = cxc
                aplicacion.fecha = fecha
                cobro.addToAplicaciones(aplicacion)
                if(cobro.primeraAplicacion == null)
                    cobro.primeraAplicacion = fecha
                disponible = disponible - importe
            }
        }
        cobro.save()
        return cobro
    }


    def saldar(Cobro cobro){
        if(cobro.disponible <= 100.00 && cobro.disponible > 0.00) {
            cobro.diferencia = cobro.disponible;
            cobro.diferenciaFecha = new Date()
            cobro.save flush: true
        }
        return cobro
    }

    private setComisiones(Cobro cobro) {
        if (cobro.tarjeta) {
            if(cobro.tarjeta.debitoCredito) {
                cobro.tarjeta.comision = 1.46
            } else if (cobro.tarjeta.visaMaster) {
                cobro.tarjeta.comision = 2.36
            } else {
                cobro.tarjeta.comision = 3.80
            }
        }
    }


    def generarCfdi(Cobro cobro) {
        validarParaTimbrado(cobro)
        Comprobante comprobante = this.reciboDePagoBuilder.build(cobro)
        Cfdi cfdi = cfdiService.generarCfdi(comprobante, 'P', 'COBROS')
        cobro.cfdi = cfdi

        cobro.save flush: true
        return cobro
    }

    def timbrar(Cobro cobro){
        if(!cobro.cfdi) {
            log.info('Generando Recibo electronico de pago cobro: {}', cobro.id)
            cobro = generarCfdi(cobro)
        }
        cfdiTimbradoService.timbrar(cobro.cfdi)
        cobro.refresh()
        return cobro
    }

    def validarParaTimbrado(Cobro cobro) {
        assert cobro.disponible == 0.0, 'El cobro debe estar totalmente aplicado'
        def sinCfdi = cobro.aplicaciones.find {AplicacionDeCobro det -> det.cuentaPorCobrar.cfdi == null}
        assert sinCfdi == null , 'El cobro no debe tener aplicaciones a facturas sin timbrar'
        List<AplicacionDeCobro> contado = cobro.aplicaciones.find{AplicacionDeCobro det -> det.cuentaPorCobrar.tipo == 'CON' }
        assert contado == null, 'El cobro no debe tener aplicaciones de contado'
    }


}
