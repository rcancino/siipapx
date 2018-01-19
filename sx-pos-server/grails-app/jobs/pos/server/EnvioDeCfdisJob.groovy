package pos.server

import grails.util.Environment
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.core.AppConfig
import sx.core.Venta

class EnvioDeCfdisJob {

    CfdiService cfdiService

    static triggers = {
        // simple repeatInterval: 60000l // execute job once in 5 seconds
        simple name:'envioDeCfdi', startDelay: 10000, repeatInterval: 120000
    }

    def execute() {
        log.debug('Buscando cfdis para enviar por email')
        if (Environment.current == Environment.PRODUCTION) {
            AppConfig config = AppConfig.first()
            if(config.envioDeCorreosActivo) {
                doEnviar()
            }
        }
    }

    private doEnviar(){
        Date dia = new Date()
        def cfdis = buscarCfdisPendientes(dia)
        if (cfdis) {
            log.debug('Enviando CFDIs {} pendientes ', cfdis.size());
            cfdis.each {
                Cfdi cfdi = Cfdi.get(it)
                Venta venta = Venta.where{cuentaPorCobrar.cfdi == cfdi}.find()
                if (venta) {
                    // log.debug('Enviando cfdi venta: {}', venta.statusInfo())
                    cfdiService.enviarFacturaEmail(cfdi, venta, venta.cliente.getCfdiMail())
                } else {
                    log.debug('No existe la venta origen del CFDI: {}', cfdi.id)
                    cfdi.comentario = "No existe la venta origen del CFDI"
                    cfdi.enviado = new Date();
                    cfdi.email = "NO ENVIADO"
                    cfdi.save flush: true
                }
            }
        }
    }

    def buscarCfdisPendientes(Date dia) {
        def cfdis = Cfdi.executeQuery("select c.id from Cfdi c where date(c.fecha) = ? " +
                "and c.cancelado = false and enviado = null", [dia])
        return cfdis
    }
}
