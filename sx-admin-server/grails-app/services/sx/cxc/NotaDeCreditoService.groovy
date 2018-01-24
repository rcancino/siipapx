package sx.cxc

import com.luxsoft.cfdix.CFDIXUtils
import com.luxsoft.cfdix.v33.NotaBuilder
import grails.gorm.transactions.Transactional
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import sx.core.Folio
import sx.core.Sucursal
import sx.inventario.DevolucionDeVenta

@Transactional
class NotaDeCreditoService {

    NotaBuilder notaBuilder

    def generarNotaDeDevolucion(NotaDeCredito nota, DevolucionDeVenta rmd) {
        if (rmd.cobro) {
            throw new NotaDeCreditoException("RMD ${rmd.documento} ${rmd.sucursal} Ya tiene nota de credito generada")
        }
        assert rmd.cobro == null, "RMD ${rmd.documento} ${rmd.sucursal} Ya tiene nota de credito generada"
        log.debug('Generando nota de credito de devolucion para el rmd {}', rmd)
        nota.cliente = rmd.venta.cliente
        nota.sucursal = rmd.sucursal
        nota.tipo = 'DEVOLUCION'
        nota.serie = 'DEV'
        nota.importe = rmd.importe
        nota.impuesto = rmd.impuesto
        nota.total = rmd.total
        nota.folio = Folio.nextFolio('NOTA_DE_CREDITO', nota.serie)

        Cobro cobro = generarCobro(nota)
        nota.save failOnError: true, flush: true
        rmd.cobro = cobro
        rmd.save flush: true
        return nota
    }

    def generarCfdi(NotaBuilder nota) {
        Comprobante comprobante = notaBuilder.build(nota);
        CfdiUtils.serialize(comprobante)
        CFDIXUtils.parse()
    }


    private generarCobro(NotaDeCredito nota) {
        Cobro cobro = new Cobro()
        cobro.setCliente(nota.cliente)
        cobro.setFecha(new Date())
        cobro.importe = nota.total
        cobro.moneda = nota.moneda
        cobro.tipoDeCambio = nota.tc
        cobro.tipo = nota.tipoCartera
        cobro.comentario = nota.comentario
        cobro.createUser = nota.createUser
        cobro.updateUser = nota.updateUser
        cobro.sucursal = nota.sucursal
        cobro.formaDePago = nota.tipo == 'BON' ? 'BONIFICACION' : 'DEVOLUCION'
        nota.cobro = cobro
    }


}

class NotaDeCreditoException  extends RuntimeException {

    NotaDeCreditoException(String message){
        super(message)
    }

}