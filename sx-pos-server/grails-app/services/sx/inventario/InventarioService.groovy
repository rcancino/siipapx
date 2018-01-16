package sx.inventario

import grails.events.EventPublisher
import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.Inventario
import sx.core.Venta
import sx.core.VentaDet

@Transactional
class InventarioService implements EventPublisher{

    // @Subscriber
    def onFacturar(Venta venta){
        // log.debug("Afectando inventario para factura: ${venta.statusInfo()}")
        if(venta.cuentaPorCobrar){
            // log.debug('Afectando inventario por {}', venta.statusInfo())
            // log.debug('Cuenta por cobrar: {}', venta.cuentaPorCobrar)
            //afectarInventariosPorFacturar(venta)
        }

    }

    def afectarInventariosPorFacturar(Venta factura){
        log.debug('AFECTANDO inventario por: {}', factura.statusInfo())
        int renglon = 1
        factura.partidas.each { VentaDet det ->
            Inventario inventario = new Inventario()
            inventario.sucursal = factura.sucursal
            inventario.documento = factura.cuentaPorCobrar.documento
            inventario.cantidad = det.cantidad.abs() * -1
            inventario.kilos = det.kilos;
            inventario.comentario = det.comentario
            inventario.fecha = factura.cuentaPorCobrar.fecha
            inventario.producto = det.producto
            inventario.tipo = 'FAC'
            inventario.tipoVenta = factura.cuentaPorCobrar.tipo
            inventario.renglon = renglon
            det.inventario = inventario
            inventario.save(failOnError: true, flush:true)
            log.debug('Inventario generado: {}', inventario)
            renglon++
        }
    }

    def afectarInventariosPorCancelacionDeFacturar(Venta factura){
        log.debug('AFECTANDO inventario por factura cancelada: {} ', factura.statusInfo())
        factura.partidas.each { VentaDet det ->
            Inventario inventario = det.inventario
            if (inventario) {
                det.inventario = null
                inventario.delete flush: true
                notify('inventarioEliminado', inventario)
                log.debug('Inventario generado: {}', inventario)
            }
        }
    }


}
