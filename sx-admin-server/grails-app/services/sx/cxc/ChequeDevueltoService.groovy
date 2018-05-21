package sx.cxc

import com.luxsoft.utils.MonedaUtils
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import sx.core.Folio


// @GrailsCompileStatic
@Transactional
class ChequeDevueltoService {

    ChequeDevuelto registrarChequeDevuelto(CobroCheque cobroCheque) {
        println 'Registrando CHE'
        CuentaPorCobrar cxc = new CuentaPorCobrar()
        cxc.cliente = cobroCheque.cobro.cliente
        cxc.sucursal = cobroCheque.cobro.sucursal
        cxc.formaDePago = cobroCheque.cobro.formaDePago
        cxc.documento = Folio.nextFolio('CHEQUE_DEVUELTO', cxc.tipo)
        cxc.comentario = 'Cargo por cheque devuelto'
        cxc.fecha = new Date()
        cxc.tipo = 'CHE'
        cxc.tipoDocumento = 'CHEQUE_DEVUELTO'
        cxc.importe = MonedaUtils.calcularImporteDelTotal(cobroCheque.cobro.importe)
        cxc.subtotal = cxc.importe
        cxc.impuesto = MonedaUtils.calcularImpuesto(cxc.importe)
        cxc.total = MonedaUtils.calcularTotal(cxc.importe + cxc.impuesto)
        println 'Total CHE: ' + cxc.total;
        cxc.createUser = 'PENDIENTE'
        cxc.updateUser = 'PENDIENTE'
        cxc.save failOnError: true

        ChequeDevuelto che = new ChequeDevuelto()
        che.comentario = ''
        che.folio = cxc.documento
        che.nombre = cxc.cliente.nombre
        che.cxc = cxc
        che.cheque = cobroCheque
        che.createUser = cxc.createUser
        che.updateUser = cxc.updateUser
        che.save failOnError: true, flush: true
        return che

    }

    ChequeDevuelto save(ChequeDevuelto chequeDevuelto) {
        chequeDevuelto.folio = Folio.nextFolio('CHEQUE_DEVUELTO','CHE')
        return chequeDevuelto.save(failOnError: true, flush: true)
    }

    ChequeDevuelto update(ChequeDevuelto chequeDevuelto) {
        return chequeDevuelto.save(failOnError: true, flush: true)
    }
}
