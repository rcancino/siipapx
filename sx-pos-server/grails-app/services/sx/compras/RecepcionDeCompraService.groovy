package sx.compras

import grails.events.annotation.Publisher
import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.Existencia
import sx.core.Folio
import sx.core.Inventario
import sx.core.Producto

@Transactional
class RecepcionDeCompraService {

    def recibir(Compra compra, String username){
        log.debug('Generando COM a de comra {} con {} partidas', compra.folio, compra.partidas.size())

        RecepcionDeCompra com = new RecepcionDeCompra()
        com.sucursal = compra.sucursal
        com.fecha = new Date()
        com.compra = compra
        com.proveedor = compra.proveedor
        compra.partidas.each { CompraDet item ->
            log.debug('Procesando partida: {}', item)
            if (item.getPorRecibir() > 0) {
                RecepcionDeCompraDet det = new RecepcionDeCompraDet()
                det.producto = item.producto
                det.cantidad = item.getPorRecibir()
                det.compraDet = item
                com.addToPartidas(det)
            }
        }
        return this.save(com, username)
    }

    @Publisher('saveCom')
    def save(RecepcionDeCompra resource, String username) {
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.documento = Folio.nextFolio('COMS',serie)
            resource.createUser = username
        }
        resource.partidas.each { RecepcionDeCompraDet it ->
            it.comentario = resource.comentario
            it.kilos = getKilos(it.producto, it.cantidad)
        }
        resource.updateUser = username
        resource = resource.save failOnError: true, flush: true
        return resource
    }

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

    private getKilos(Producto producto, BigDecimal cantidad) {
        def factor = producto.unidad == 'MIL' ? 1000 : 1;
        return producto.kilos * (cantidad/factor)
    }
}
