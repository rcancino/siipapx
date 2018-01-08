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
        cobro.formaDePago = solicitud.transferencia > 0.0 ? 'TRANSFERENCIA' : 'DEPOSITO'
        cobro.referencia = solicitud.referencia
        cobro.sw2 = 'SOLICITUD AUTORIZADA,' + solicitud.id
        cobro.importe = solicitud.total
        if(solicitud.transferencia > 0.0 ){
            CobroTransferencia transferencia = new CobroTransferencia()
            transferencia.cuentaDestino = solicitud.cuenta
            transferencia.bancoOrigen = solicitud.banco
            transferencia.fechaDeposito = solicitud.fechaDeposito
            transferencia.folio = solicitud.folio
            transferencia.cobro = cobro
            transferencia.save failOnError: true, flush: true
            //log.debug('Cobro transferencia generado: {}', transferencia.id)

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
        // log.debug('Entidad valida: {}', cobro.validate())
        // log.debug('Errores: {}', cobro.errors)
        cobro.save failOnError: true, flush: true
        solicitud.cobro = cobro
        solicitud.save failOnError: true, flush: true
        return cobro
    }
}
