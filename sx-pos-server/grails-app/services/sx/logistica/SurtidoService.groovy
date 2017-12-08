package sx.logistica

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.Venta
import sx.core.VentaDet
import sx.security.User

@Transactional
class SurtidoService {

    @Subscriber
    def onFacturar(Venta venta){
        log.debug("Generando Surtido para factura: ${venta.statusInfo()}")
        // Venta pedido = Venta.findById(source.id, [fetch:[partidas:"join"]])
        if(venta.cuentaPorCobrar)
            generarSurtido(venta)

    }

    @Subscriber
    def onSave(Venta venta) {
        if(venta.puesto){
            log.debug("Generando Surtido para pedido tipo  PUESTO: {}", venta.getFolio())
            generarSurtido(venta)
        }
    }

    private generarSurtido(Venta venta) {
        Surtido.withNewTransaction {
            Venta pedido = Venta.get(venta.id)
            Surtido found = Surtido.where{ origen == pedido.id}.find()
            if(found) {
                assert found.iniciado == null, 'Pedido con surtido ya iniciado'
                found.delete()
            }
            Surtido surtido = new Surtido()
            surtido.documento = pedido.documento
            surtido.comentario =  pedido.comentario
            surtido.fecha = new Date()
            surtido.clasificacionVale = pedido.clasificacionVale
            surtido.folioFac = pedido.documento
            surtido.nombre = pedido.nombre
            surtido.kilos = pedido.kilos
            surtido.kilosCorte = pedido.partidas.sum 0, { VentaDet det -> det.corte ? det.kilos : 0}
            surtido.folioFac = pedido.cuentaPorCobrar ? pedido.cuentaPorCobrar.documento : 0
            surtido.entidad = pedido.puesto ? 'PST' : 'FAC'
            surtido.origen = pedido.id
            surtido.entregaLocal = pedido.envio ? false : true
            surtido.prods = pedido.partidas.count{ VentaDet det -> det.producto.inventariable && !det.conVale}
            surtido.prodsCorte = pedido.partidas.count { VentaDet det -> det.corte != null}
            surtido.userLastUpdate = User.findByUsername(pedido.updateUser)
            surtido.tipoDeVenta = pedido.tipo
            pedido.partidas.findAll{it.corte}.each { VentaDet det ->
                Corte corte = new Corte()
                corte.producto = det.producto
                corte.instruccionCorte = det.corte
                surtido.addToCortes(corte)
            }
            surtido.save failOnError: true, flush: true
            log.debug('Surtido generado {}', surtido)
            return surtido
        }
    }
}
