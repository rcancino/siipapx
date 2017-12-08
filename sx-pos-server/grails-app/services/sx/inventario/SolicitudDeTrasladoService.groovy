package sx.inventario

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import sx.core.Folio
import sx.core.Venta
import sx.core.VentaDet


@Transactional
class SolicitudDeTrasladoService {

    SpringSecurityService springSecurityService

    @Subscriber
    def onMandarFacturar(Venta venta) {
        if(venta.clasificacionVale != 'SIN_VALE' && venta.clasificacionVale != 'EXISTENCIA_VENTA') {
            log.debug (' Generando  vale automatico tipo: '+venta.clasificacionVale )
            generarValeAutomatico(venta.id);
        }
    }

    def generarValeAutomatico( String ventaId) {
        Venta.withNewTransaction {
            Venta venta = Venta.get(ventaId)
            List partidas = venta.partidas.findAll{ it.conVale }
            SolicitudDeTraslado sol = new SolicitudDeTraslado()
            sol.clasificacionVale = venta.clasificacionVale
            sol.documento = getFolio()
            sol.venta = venta
            sol.comentario =  "${venta.statusInfo()} ${venta.clasificacionVale}"
            sol.fecha = new Date()
            sol.sucursalSolicita = venta.sucursal
            sol.sucursalAtiende = venta.sucursalVale
            sol.referencia = "${venta.tipo} - ${venta.documento}"
            partidas.each { VentaDet det ->
                SolicitudDeTrasladoDet solDet = new SolicitudDeTrasladoDet()
                solDet.producto = det.producto
                solDet.comentario = "${venta.statusInfo()} ${venta.clasificacionVale}"
                solDet.solicitado = det.cantidad
                if(det.corte) {
                    solDet.cortesInstruccion = det.corte.instruccion
                    solDet.cortes = det.corte.cantidad
                }
                sol.addToPartidas(solDet)
            }
            sol.updateUser = venta.updateUser
            sol.createUser = venta.createUser
            sol.save()
        }
    }

    def logEntity(SolicitudDeTraslado sol) {
        def user = getUser()
        if(! sol.id)
            sol.createUser = user
        sol.updateUser = user
    }

    def getUser() {
        def principal = springSecurityService.getPrincipal()
        return principal.username
    }

    /**
     * Genera el folio para las solicitudes
     *
     * @return
     */
    def getFolio() {
        return Folio.nextFolio('SOLS','SOLS')
    }

}
