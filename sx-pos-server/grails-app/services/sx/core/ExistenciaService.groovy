package sx.core


import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional


@Transactional
class ExistenciaService {

    @Subscriber
    def onFacturar(Venta pedido) {
        log.debug('Detectando facturacion procesando existencias')
    }

    private actualizarExistenciasPorVenta(Venta venta) {
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

    def actualizarExistencias(Long ejercicio, Long mes, Producto producto) {

        def mesAnterior = mes == 1 ? 12 : mes - 1
        def ejercicioAnterior = mes == 1 ? ejercicio -1 : ejercicio
        def anterior = findExitencia(producto, ejercicioAnterior, mesAnterior)
        def actual = findOrCreateNext(anterior)
        actual.with {

        }
    }

    Existencia findOrCreateNext(Existencia existencia) {
        def year = existencia.mes == 12 ? existencia.anio + 1 : existencia.anio
        def mes = existencia.mes == 12 ? 1 : existencia.mes + 1
        return Existencia.findOrSaveWhere(
                sucursal: getSucursal(),
                producto: existencia.producto,
                ejercicio: year,
                mes: mes);
    }

    Existencia findExitencia(Producto producto, Long ejercicio, Long mes) {
        return Existencia.where {
            sucursal == getSucursal() &&
            producto == producto &&
            anio == ejercicio &&
            mes == mes }.find()
    }



    private Sucursal sucursal

    Sucursal getSucursal() {
        if(sucursal == null){
            sucursal = AppConfig.first().sucursal
        }
        return sucursal
    }
}
