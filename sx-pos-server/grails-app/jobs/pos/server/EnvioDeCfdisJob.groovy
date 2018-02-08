package pos.server

import grails.util.Environment
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.core.AppConfig
import sx.core.Venta

class EnvioDeCfdisJob {

    CfdiService cfdiService

    static triggers = {
        // simple repeatInterval: 60000l // execute job once in 5 seconds
        simple name:'envioDeCfdi', startDelay: 10000, repeatInterval: 600000
    }

    def execute() {
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
                    try{
                        log.debug('Enviando cfdi venta: {}', venta.statusInfo())
                        cfdiService.enviarFacturaEmail(cfdi, venta, venta.cliente.getCfdiMail())
                    }catch (Exception ex) {
                        ex.printStackTrace()
                        String c = ExceptionUtils.getRootCauseMessage(ex)
                        log.debug('Error enviando correo Fac: {} Error: {}', venta.statusInfo(), c)
                        cfdi.enviado = new Date()
                        cfdi.email = venta.cliente.getCfdiMail()
                        cfdi.comentario = "Error en evio: ${c}"
                        cfdi.save flush:true
                    }
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
