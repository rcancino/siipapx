package sx.core


import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional


@Transactional
class ExistenciaService {

    @Subscriber
    def onFacturar(Venta pedido) {
        log.debug('Detectando facturacion procesando existencias')
    }

    private actualizarExistencias(Venta venta) {
        log.debug("Actualizando existencias por ${venta.statusInfo()}")
        Date hoy = new Date()
        int month = hoy[Calendar.MONTH] + 1
        def year = hoy[Calendar.YEAR]
        venta.partidas.each { VentaDet det ->
            Existencia.withNewTransaction {
                Existencia exis = Existencia.where{ producto == det.producto && anio == year && mes== month}.find()
                assert exis, "No existe existencia ${year} - ${month} Para ${det.producto.clave}"
                actualizarVenta(det, venta.puesto)
                exis.save()
            }
        }
    }

    /**
     * Actualiza los datos relacionados con la venta
     *
     * @param existencia
     * @param det
     * @param puesto
     * @return
     */
    private actualizarVenta(Existencia exis, VentaDet det, boolean puesto = false) {
        if(!puesto) {
            // Facturas normales
            exis.venta += det.cantidad
        } else {
            // Puesto sin facturar: Incrementar los pedidos pendientes
            if(!venta.cuentaPorCobrar) {
                exis.pedidosPendiente -= det.cantidad
            } else {
                // Puesto facturado: Decrementar los pedidos pendientes y aumentar la venta
                exis.pedidosPendiente -= det.cantidad
                exis.pedidosPendiente += det.cantidad
            }
        }
    }
}
