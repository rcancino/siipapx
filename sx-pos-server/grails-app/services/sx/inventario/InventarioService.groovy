package sx.inventario

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.Inventario
import sx.core.Venta
import sx.core.VentaDet

@Transactional
class InventarioService {

    @Subscriber
    def onFacturar(Venta venta){
        log.debug("Afectando inventario para factura: ${venta.statusInfo()}")
        if(venta.cuentaPorCobrar){
            log.debug('Afectando inventario  {} partidas: {}', venta.statusInfo(), venta.partidas.size())
            log.debug('Cuenta por cobrar: {}', venta.cuentaPorCobrar)
            afectarInventariosPorFacturar(venta)
        }

    }

    private afectarInventariosPorFacturar(Venta factura){
        Venta.withNewTransaction {
            // Venta factura = Venta.get(venta2.id);
            def partidas = VentaDet.findAll { venta == factura}
            int renglon = 1
            partidas.each { VentaDet det ->
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
                renglon++
            }
        }
    }

    @Subscriber
    def onCancelarFactura(Map result){
        log.debug('Detectando factura cancelada {}', result.factura);
    }
}
