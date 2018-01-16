package sx.compras

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional

@Transactional
class CompraService {

    @Subscriber
    def onSaveCom(RecepcionDeCompra com) {
        log.debug('Detectando modificacion de com {}', com.documento)
        Compra.withNewSession {
            Compra compra = Compra.get(com.compra.id)
            compra.refresh()
            compra.actualizarStatus()
            compra.save flush: true
            log.debug('Status de compra {} actualizado', compra.folio)
        }
    }
}
