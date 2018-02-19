package sx.core

import com.luxsoft.utils.MonedaUtils
import grails.events.EventPublisher
import grails.events.annotation.Publisher
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService
import sx.cxc.AplicacionDeCobro
import sx.cxc.Cobro
import sx.cxc.CuentaPorCobrar
import lx.cfdi.v33.CfdiUtils


import com.luxsoft.cfdix.v33.CfdiFacturaBuilder
import sx.inventario.InventarioService

@Transactional
class VentaService implements  EventPublisher{

    CfdiService cfdiService

    CfdiFacturaBuilder cfdiFacturaBuilder

    CfdiTimbradoService cfdiTimbradoService

    SpringSecurityService springSecurityService

    InventarioService inventarioService

    @Publisher
    def save(Venta venta) {
        fixCortes(venta)
        fixDescuentos(venta)
        fixNombre(venta)
        logEntity(venta)
        fixVendedor(venta)
        fixDescuentoOriginal(venta)
        fixSinExistencias(venta)
        if(venta.id == null){
            Folio folio=Folio.findOrCreateWhere(entidad: 'VENTAS', serie: 'PEDIDOS')
            def res = folio.folio + 1
            folio.folio = res
            venta.documento = res
            folio.save()
        }
        venta.save()
        return venta
    }

    /**
     * Arregla un aparente error en el mapeo de entidades VentaDet y Corte
     *
     * @TODO: Arreglar esto en el mapeo
     *
     * @param venta
     * @return
     */
    private fixCortes(Venta venta) {
        venta.partidas.each {
            if(it.corte)
                it.corte.ventaDet = it;
        }
    }

    private fixDescuentos(Venta venta) {
        venta.partidas.each {
            if(it.corte) {
                def descuentoGeneral = venta.descuento
                def descuento = it.descuento
                def factor = it.producto.unidad == 'MIL' ? 1000.00 : 1.00
                def importe = (it.cantidad * it.precio)/ factor
                def descuentoCalculado = (importe * descuento)/100.00
                descuentoCalculado = MonedaUtils.round (descuentoCalculado , 2)
                if(descuentoCalculado != it.descuentoImporte) {
                    log.debug('Error Descuento en partidas: {} calculado: {}', it.descuentoImporte, descuentoCalculado)
                    it.descuentoImporte = descuentoCalculado
                    it.total = it.subtotal + it.impuesto
                    it.descuentoImporte = it.importe - it.subtotal
                }
            }
            it.descuentoOriginal = it.descuentoOriginal > 0 ? venta.descuentoOriginal: it.descuentoOriginal
        }
    }

    private fixEnvio(Venta venta) {
        if(!venta.id && venta.envio) {
            if (!venta.envio.venta)
                venta.envio.venta = venta;
        }
    }

    /**
     * Actualiza el nombre en la venta cuando se trata de ventas de mostrador
     *
     * @param venta
     * @return
     */
    private fixNombre(Venta venta) {
        if(venta.cliente.rfc != 'XAXX010101000')
            venta.nombre = venta.cliente.nombre
    }

    private fixVendedor(Venta venta) {
        if(venta.vendedor == null) {
            venta.vendedor = Vendedor.findByNombres('CASA')
        }
        assert venta.vendedor, 'No fue posible asignar vendedor a la venta'
    }

    private fixDescuentoOriginal(Venta venta) {
        def desc = null;
        if(venta.tipo == 'CRE') {
            venta.partidas.each {it.descuentoOriginal = it.descuento}
            venta.descuentoOriginal = venta.descuento
        }
    }
    private fixSinExistencias(Venta venta) {
        def sinExistencias = venta.partidas.find { it.sinExistencia}
        venta.sinExistencia = sinExistencias != null
    }

    /**
     * Actualiza la columna de facturar para indicar que la venta esta lista para ser facurada en el area de caja
     * publicando un evento con el nombre del metodo. Es importante no cambiar el nombre de este metodo ya que es el id
     * del evento que otros servicios podrian estar escuchando (de manera asincrona)
     *
     * @param venta
     * @return
     */
    @Publisher
    def mandarFacturar(String ventaId) {
        Venta venta = Venta.get(ventaId)
        log.debug('Mandando facturar venta......'+ venta.getFolio())
        venta.facturar = new Date()
        venta.save()
        return venta
    }

    @Publisher
    def facturar(Venta pedido) {
        log.debug("Facturando  ${pedido.statusInfo()}")
        assert pedido.cuentaPorCobrar == null, "Pedido${pedido.getFolio()} ya facturado : ${pedido.statusInfo()}"
        pedido = generarCuentaPorCobrar(pedido)
        return pedido
    }

    @Publisher('facturar')
    def generarCuentaPorCobrar(Venta pedido) {
        CuentaPorCobrar cxc = new CuentaPorCobrar()
        cxc.sucursal = pedido.sucursal
        cxc.cliente = pedido.cliente
        cxc.tipoDocumento = 'VENTA'
        cxc.importe = pedido.importe
        cxc.descuentoImporte = pedido.descuentoImporte
        cxc.subtotal = pedido.subtotal
        cxc.impuesto = pedido.impuesto
        cxc.total  = pedido.total
        cxc.formaDePago = pedido.formaDePago
        cxc.moneda = pedido.moneda
        cxc.tipoDeCambio = pedido.tipoDeCambio
        cxc.comentario = pedido.comentario
        cxc.tipo = pedido.cod ? 'COD': pedido.tipo
        cxc.documento = Folio.nextFolio('FACTURAS',cxc.tipo)
        cxc.fecha = new Date()
        cxc.createUser = pedido.createUser
        cxc.updateUser = pedido.updateUser
        cxc.chequePostFechado = pedido.chequePostFechado
        cxc.comentario = pedido.comentario
        pedido.cuentaPorCobrar = cxc
        cxc.save failOnError: true
        log.debug('Cuenta por cobrar generada: {}', cxc)
        pedido.cuentaPorCobrar = cxc
        pedido.save flush: true
        inventarioService.afectarInventariosPorFacturar(pedido);
        return pedido
    }

    def generarCfdi(Venta venta){
        assert venta.cuentaPorCobrar, " La venta ${venta.documento} no se ha facturado"
        log.debug('Generando CFDI para  {}', venta.statusInfo())
        def comprobante = cfdiFacturaBuilder.build(venta)
        // log.debug('Comprobante: {}', CfdiUtils.serialize(comprobante))
        def cfdi = cfdiService.generarCfdi(comprobante, 'I')
        venta.cuentaPorCobrar.cfdi = cfdi
        venta.save flush: true
        return cfdi
    }

    def timbrar(Venta venta){
        log.debug("Timbrando  {}", venta.statusInfo());
        assert venta.cuentaPorCobrar, "La venta ${venta} no se ha facturado"
        assert !venta.cuentaPorCobrar?.cfdi?.uuid, "La venta ${venta} ya esta timbrada "
        def cfdi = venta.cuentaPorCobrar.cfdi
        if (cfdi == null) {
            cfdi = generarCfdi(venta)
        }
        cfdi = cfdiTimbradoService.timbrar(cfdi)
        venta.cuentaPorCobrar.uuid = cfdi.uuid
        venta.save flush:true
        return cfdi;
    }


    def logEntity(Venta venta) {
        /*
        def user = getUser()
        if(! venta.id)
            venta.createUser = user
        venta.updateUser = user
        */
    }

    def getUser() {
        def principal = springSecurityService.getPrincipal()
        return principal.username
    }

    def getFolio() {
        return Folio.nextFolio('VENTAS','PEDIDOS')
    }

    /**
     * Cancela una Factura (Venta facturada) cancelando su CFDI
     *
     * @param venta
     * @return
     */
    @Publisher
    def cancelarFactura(Venta factura, String username, String motivo) {
        assert factura.cuentaPorCobrar, "El pedido ${factura.statusInfo()} no esta facturado "
        assert username, 'Debe registrar usiuario para la cancelacion'
        assert motivo, 'Debe registrar motivo de cancelacion'

        log.debug('Cancelando factura {}', factura.statusInfo())
        CuentaPorCobrar cxc = factura.cuentaPorCobrar
        Cfdi cfdi = cxc.cfdi

        // 1o Desvincular la cuenta por cobrar y la venta
        factura.cuentaPorCobrar = null
        factura.facturar = null
        factura.impreso = null
        factura.save flush: true

        // 2o Eliminar la cuenta por cobrar sus aplicaciones y cancelar su CFDI
        eliminarAplicaciones(cxc)
        cancelarCuentaPorCobrar(cxc, username, motivo)
        inventarioService.afectarInventariosPorCancelacionDeFacturar(factura)

        // 3o Cancelar el CFDI
        if( cfdi && cfdi.uuid) {
            cfdi.status = 'CANCELACION_PENDIENTE'
            cfdi.save flush:true
        }
        return factura
    }

    /**
     * Elimina la CuentaPorCobrar
     *
     * @param cxc
     * @return
     */
    // @Publisher
    def cancelarCuentaPorCobrar(CuentaPorCobrar cxc, String usuario, String motivo) {
        cxc.importe = 0.0
        cxc.impuesto = 0.0
        cxc.total = 0.0
        cxc.comentario = 'CANCELADA'
        cxc.cancelada = new Date()
        cxc.cancelacionUsuario = usuario
        cxc.cancelacionMotivo = motivo
        cxc.save()
        // cxc.delete flush: true
    }

    /**
     * Elimina las aplicaciones de cobro de la cuenta por cobrar
     *
     * @param cxc
     * @return La cuenta por cobar sin aplicaciones sociadas
     */
    // @Publisher
    def eliminarAplicaciones(CuentaPorCobrar cxc) {
        def aplicaciones = AplicacionDeCobro.where{ cuentaPorCobrar == cxc}.list()
        log.debug('Eliminando {} aplicaciones a la factura {}', aplicaciones.size(), cxc.folio)
        aplicaciones.each { AplicacionDeCobro a ->
            Cobro cobro = a.cobro
            if(cobro.aplicaciones.size() == 1 ){
                if (cxc.formaDePago != 'TRANSFERENCIA' && !cxc.formaDePago.startsWith("DEPOSITO")) {
                    log.debug('Eliminando cobro {} {}', cobro.formaDePago, cobro)
                    cobro.delete flush:true;
                } else {
                    cobro.removeFromAplicaciones(a)
                    cobro.save()
                }
            } else {
                cobro.removeFromAplicaciones(a)
                cobro.save()
            }
        }
        return cxc
    }



}
