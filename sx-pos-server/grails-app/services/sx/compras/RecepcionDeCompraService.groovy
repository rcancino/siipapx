package sx.compras

import grails.events.annotation.Publisher
import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.Existencia
import sx.core.Inventario

@Transactional

class RecepcionDeCompraService {

    @Publisher
    def afectarInventario( RecepcionDeCompra com) {
        def renglon = 1;
        com.partidas.each { det ->
            Inventario inventario = new Inventario()
            inventario.sucursal = com.sucursal
            inventario.documento = com.documento
            inventario.cantidad = det.cantidad
            inventario.comentario = det.comentario
            inventario.fecha = com.fecha
            inventario.producto = det.producto
            inventario.tipo = 'COM'
            inventario.renglon = renglon
            det.inventario = inventario
            renglon++
        }
        com.fechaInventario = new Date()
        com.save flush: true
        actualizarExistencias(com)
    }

    def actualizarExistencias(RecepcionDeCompra com) {
        Date hoy = new Date()
        Integer ejercicio = hoy[Calendar.YEAR]
        Integer mes = hoy[Calendar.MONTH] + 1
        com.partidas.each { RecepcionDeCompraDet det ->
            Existencia existencia = Existencia.where { anio == ejercicio && mes == mes && producto == det.producto && sucursal == com.sucursal }.find()
            assert existencia, "No existe la existencia del producto ${det.producto.clave} para ${ejercicio} - ${mes}"
            existencia.cantidad = existencia.cantidad + det.cantidad.abs()
            existencia.save failOnError: true, flush: true
            log.debug('Existencia actualizada: {}', existencia)
        }
    }
}
