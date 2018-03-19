package sx.tesoreria

import sx.core.Empresa
import sx.cxc.Cobro
import sx.cxc.CobroDeposito
import sx.cxc.CobroTransferencia
import sx.cxc.SolicitudDeDeposito

class SolicitudDeDepositoService {

    def autorizar(SolicitudDeDeposito solicitud) {
        assert solicitud.cobro == null, 'Solicitud ya autorizada'
        log.debug('Autorizando solicitud: {}',solicitud.folio)
        Cobro cobro = new Cobro()
        cobro.sucursal = solicitud.sucursal
        cobro.cliente = solicitud.cliente
        cobro.fecha = new Date()
        cobro.tipo = solicitud.tipo == 'CRE' ? 'CRE' : 'CON'
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
            if(cobro.tipo == 'CRE') {
                registrarIngreso(cobro)
            }

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
            if(cobro.tipo == 'CRE') {
                registrarIngreso(cobro)
            }
        }
        cobro.save failOnError: true, flush: true
        solicitud.cobro = cobro
        solicitud.save failOnError: true, flush: true

        return cobro
    }

    def registrarIngreso(SolicitudDeDeposito solicitud){
        Cobro cobro = solicitud.cobro
        assert !cobro.ingreso, "Ingreso de tesorería ya registrado para el cobro ${cobro.id}"
        Empresa empresa = Empresa.first()
        if(cobro.deposito) {
            CobroDeposito deposito = cobro.deposito
            MovimientoDeCuenta mov = new MovimientoDeCuenta()
            mov.referencia = "Deposito: ${deposito.folio} "
            mov.tipo = cobro.tipo;
            mov.fecha = cobro.fecha
            mov.formaDePago = cobro.formaDePago
            mov.comentario = "Deposito ${cobro.tipo} ${cobro.sucursal.nombre} "
            mov.cuenta = deposito.cuentaDestino
            mov.afavor = empresa.nombre
            mov.importe = cobro.importe
            mov.moneda = deposito.cuentaDestino.moneda
            mov.concepto = 'VENTAS'
            mov.save failOnError: true, flush: true
            deposito.ingreso = mov;
            cobro.save flush: true
            return mov

        } else if(cobro.transferencia) {
            CobroTransferencia transferencia = cobro.transferencia
            MovimientoDeCuenta mov = new MovimientoDeCuenta()
            mov.referencia = "Deposito: ${transferencia.folio} "
            mov.tipo = cobro.tipo;
            mov.fecha = cobro.fecha
            mov.formaDePago = cobro.formaDePago
            mov.comentario = "Transferencia ${cobro.tipo} ${cobro.sucursal.nombre} "
            mov.cuenta = transferencia.cuentaDestino
            mov.afavor = empresa.nombre
            mov.importe = cobro.importe
            mov.moneda = transferencia.cuentaDestino.moneda
            mov.concepto = 'VENTAS'
            mov.save failOnError: true, flush: true
            transferencia.ingreso = mov;
            cobro.save flush: true
            return mov

        }

    }
}
