package sx.cxc

import groovy.util.logging.Slf4j
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional
import grails.gorm.transactions.ReadOnly

import sx.core.Cliente
import sx.core.AppConfig

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
@Slf4j
class CuentaPorCobrarService {

    @Transactional
    CuentaPorCobrar saldar(CuentaPorCobrar cxc) {
        if (cxc.saldo > 0.0 && cxc.saldo <= 100.00) {
            Date fecha = new Date()
            Cobro cobro = new Cobro()
            cobro.sucursal = AppConfig.first().sucursal
            cobro.fecha = fecha
            cobro.tipo = cxc.tipo
            cobro.formaDePago = 'PAGO_DIF'
            cobro.importe = cxc.saldo
            cobro.fechaDeAplicacion = fecha
            cobro.referencia = cxc.folio
            cobro.cliente = cxc.cliente

            AplicacionDeCobro aplicacionDeCobro = new AplicacionDeCobro()
            aplicacionDeCobro.importe = cxc.saldo
            aplicacionDeCobro.formaDePago = 'PAGO_DIF'
            aplicacionDeCobro.fecha = fecha
            aplicacionDeCobro.cuentaPorCobrar = cxc
            cobro.addToAplicaciones(aplicacionDeCobro)
            cobro.save failOnError: true, flush: true

        }
        return cxc
    }

    @ReadOnly
    List<CuentaPorCobrarDTO> findAllPendientes() {
        List<CuentaPorCobrar> rows = CuentaPorCobrar
                .findAll(
                        """from CuentaPorCobrar c where c.tipo = :tipo and c.saldoReal > 0
                    order by c.fecha
                """
                , [tipo:'CRE'])
        List<CuentaPorCobrarDTO> res = rows.collect { cxc -> new CuentaPorCobrarDTO(cxc)}
        log.info('Registros de cartera: ', res.size())
        return res
    }

    @ReadOnly
    List<CuentaPorCobrarDTO> findPendientes(Cliente cliente) {
        List<CuentaPorCobrar> rows = CuentaPorCobrar
                .findAll(
                """from CuentaPorCobrar c 
                    where c.cliente.id = :clienteId 
                      and c.saldoReal > 0
                      and c.cfdi is not null
                    order by c.fecha
                """
                , [clienteId: cliente.id])
        List<CuentaPorCobrarDTO> res = rows.collect { cxc -> new CuentaPorCobrarDTO(cxc)}
        log.info(' {} Facturas pendientes para : {} ',res.size(), cliente.nombre)
        return res
    }
}

