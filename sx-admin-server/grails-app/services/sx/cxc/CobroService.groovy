package sx.cxc

import grails.gorm.transactions.Transactional

@Transactional
class CobroService {

    def save(Cobro cobro) {
        if(cobro.cheque) {
            cobro.referencia = cobro.cheque.numero
        }
        if(cobro.tarjeta) {
            cobro.referencia = cobro.tarjeta.validacion.toString()
        }
        setComisiones(cobro)
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

    def registrarAplicacion(Cobro cobro){

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


}
