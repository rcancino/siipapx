package sx.tesoreria

import grails.gorm.transactions.Transactional
import sx.core.AppConfig
import sx.cxc.AplicacionDeCobro
import sx.cxc.Cobro

@Transactional
class CorteCobranzaService {


    def prepararCorte( String formaDePago, String tipo, Date fecha) {
        log.debug('Calculando corte tipo {} ({}) para {}',  formaDePago, tipo, fecha.format('dd/MM/yyyy'))
        AppConfig config = AppConfig.first()
        CorteCobranza corte = new CorteCobranza()
        corte.formaDePago = formaDePago
        corte.sucursal = config.sucursal
        corte.tipoDeVenta = tipo
        corte.corte = new Date()
        corte.fecha = fecha
        corte.pagosRegistrados = getCobrosRegistrados(corte)
        corte.cortesAcumulado = getCortesAcumulados(corte)
        return corte
    }

    def getCobrosRegistrados(CorteCobranza corte) {
        if (corte.formaDePago.startsWith('TARJETA')) {
            return Cobro.executeQuery(
                    "select sum(c.importe) from Cobro c where c.sucursal=? and date(primeraAplicacion) = ? and c.formaDePago like ?",
                    [corte.sucursal,corte.fecha, 'TARJETA_%'])[0]?:00
        }
        if (corte.formaDePago == 'DEPOSITO') {
            log.debug(' Buscando aplicaciones para {} {} tipo  {} {}', corte.sucursal.nombre, corte.fecha.format('dd/MM/yyyy'), corte.tipoDeVenta, corte.formaDePago)
            Set<Cobro> cobros = Cobro.executeQuery(
                    "select a.cobro from AplicacionDeCobro a where date(a.cobro.primeraAplicacion) = ? and a.cobro.formaDePago like ? and a.cuentaPorCobrar.tipo = ?",
                [corte.fecha, 'DEPOSITO_%', corte.tipoDeVenta])
            def total = cobros.sum (0.0 , {it.importe})
            return total
        }
        if (corte.formaDePago == 'TRANSFERENCIA') {
            log.debug(' Buscando cobros para {} {} tipo  {} {}', corte.sucursal.nombre, corte.fecha.format('dd/MM/yyyy'), corte.tipoDeVenta, corte.formaDePago)
            Set<Cobro> cobros = Cobro.executeQuery(
                    "select a.cobro from AplicacionDeCobro a where date(a.cobro.primeraAplicacion) = ? and a.cobro.formaDePago = ? and a.cuentaPorCobrar.tipo = ?",
                    [corte.fecha, 'TRANSFERENCIA', corte.tipoDeVenta])
            def total = cobros.sum (0.0 , {it.importe})
            return total
        }
        def pagosRegistrados = Cobro.executeQuery(
                "select sum(c.importe) from Cobro c where c.sucursal=? and date(primeraAplicacion) = ? and c.formaDePago = ?",
                [corte.sucursal,corte.fecha, corte.formaDePago])[0]?:00

    }

    def getCortesAcumulados(CorteCobranza corte) {
        def cortes = CorteCobranza.where { tipoDeVenta == corte.tipoDeVenta && fecha == corte.fecha && formaDePago == corte.formaDePago}
        return cortes.sum (0.0, { it.deposito})
    }
}
