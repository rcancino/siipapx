package sx.tesoreria

import sx.cxc.Cobro
import sx.cxc.CobroDeposito
import sx.cxc.CobroTransferencia
import sx.cxc.SolicitudDeDeposito

class SolicitudDeDepositoService {

    def autorizar(SolicitudDeDeposito solicitud) {
        log.debug('Autorizando solicitud: {}',solicitud.folio)
        Cobro cobro = new Cobro()
        cobro.sucursal = solicitud.sucursal
        cobro.cliente = solicitud.cliente
        cobro.fecha = new Date()
        cobro.tipo = ''
        if (solicitud.transferencia > 0) {
            cobro.formaDePago = 'TRANSFERENCIA'
        } else {
            if (solicitud.cheque > solicitud.efectivo) {
                cobro.formaDePago = 'DEPOSITO_CHEQUE'
            } else {
                cobro.formaDePago = 'DEPOSITO_EFECTIVO'
            }
        }
        cobro.referencia = solicitud.referencia
        // cobro.sw2 = 'SOLICITUD AUTORIZADA: ' + solicitud.folio
        cobro.importe = solicitud.total
        if(solicitud.transferencia > 0.0 ){
            CobroTransferencia transferencia = new CobroTransferencia()
            transferencia.cuentaDestino = solicitud.cuenta
            transferencia.bancoOrigen = solicitud.banco
            transferencia.fechaDeposito = solicitud.fechaDeposito
            transferencia.folio = solicitud.folio
            transferencia.cobro = cobro
            transferencia.save failOnError: true, flush: true

        } else {
            CobroDeposito deposito = new CobroDeposito()
            deposito.fechaDeposito = solicitud.fechaDeposito
            deposito.bancoOrigen = solicitud.banco
            deposito.cuentaDestino = solicitud.cuenta
            deposito.totalCheque = solicitud.cheque
            deposito.totalEfectivo = solicitud.efectivo
            deposito.folio = solicitud.folio
            deposito.cobro = cobro
            deposito.save failOnError: true, flush: true
        }
        cobro.save failOnError: true, flush: true
        solicitud.cobro = cobro
        solicitud.save failOnError: true, flush: true
        return cobro
    }
}
