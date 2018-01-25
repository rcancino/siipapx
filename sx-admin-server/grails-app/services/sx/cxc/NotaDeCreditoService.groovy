package sx.cxc

import com.luxsoft.cfdix.CFDIXUtils
import com.luxsoft.cfdix.v33.NotaBuilder
import grails.gorm.transactions.Transactional
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import sx.core.Folio

import sx.cfdi.Cfdi
import sx.inventario.DevolucionDeVenta
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService

@Transactional
class NotaDeCreditoService {

    NotaBuilder notaBuilder

    CfdiService cfdiService

    CfdiTimbradoService cfdiTimbradoService

    def generarNotaBonificacion(NotaDeCredito nota) {
        log.debug('Presistiendo nota de devolucion {}', nota)
        nota.tipo = 'BONIFICACION'
        nota.serie = 'BON'
        nota.folio = Folio.nextFolio('NOTA_DE_CREDITO', nota.serie)
        Cobro cobro = generarCobro(nota)
        nota.save failOnError: true, flush: true
        return nota
    }

    def generarNotaDeDevolucion(NotaDeCredito nota, DevolucionDeVenta rmd) {
        if (rmd.cobro) {
            throw new NotaDeCreditoException("RMD ${rmd.documento} ${rmd.sucursal} Ya tiene nota de credito generada")
        }
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

    def generarCfdi(NotaDeCredito nota) {
        Comprobante comprobante = this.notaBuilder.build(nota);
        Cfdi cfdi = cfdiService.generarCfdi(comprobante, 'E')
        nota.cfdi = cfdi
        nota.save flush: true
        return nota
    }

    def timbrar(NotaDeCredito nota){
        if(!nota.cfdi) {
            nota = generarCfdi(nota)
        }
        def cfdi = nota.cfdi
        cfdi = cfdiTimbradoService.timbrar(cfdi)
        return nota
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

    def eliminar(NotaDeCredito nota) {
        if(nota.cfdi ){
            Cfdi cfdi = nota.cfdi
            if (cfdi.uuid) {
                throw new NotaDeCreditoException('Nota de credito timbrada no se puede eliminar')
            }
            nota.cfdi = null
            cfdi.delete flush: true
        }
        nota.delete flush:true
    }

    def cancelar(NotaDeCredito nota) {
        assert nota.cfdi, 'Nota sin XML generado no se puede cancelar'
        assert nota.cfdi.uuid, 'Nota sin timbrar no se puede cancelar'
        Cfdi cfdi = nota.cfdi
        cfdiTimbradorService.cancelar(cfdi)
        nota.comentario = 'CANCELADA'
        nota.save flush: true
    }


}

class NotaDeCreditoException  extends RuntimeException {

    NotaDeCreditoException(String message){
        super(message)
    }

}