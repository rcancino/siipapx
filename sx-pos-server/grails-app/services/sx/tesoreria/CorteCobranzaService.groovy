package sx.tesoreria

import grails.gorm.transactions.Transactional
import sx.core.AppConfig

@Transactional
class CorteCobranzaService {


    def prepararCorte( String formaDePago, String tipo) {
        AppConfig config = AppConfig.first()
        CorteCobranza corte = new CorteCobranza()
        corte.formaDePago = formaDePago
        corte.sucursal = config.sucursal
        corte.tipoDeVenta = tipo
        corte.corte = new Date()
        corte.fecha = new Date()
        return corte
    }
}
