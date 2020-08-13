package sx.cxc

import groovy.transform.CompileStatic
import groovy.transform.Canonical
import groovy.transform.ToString
import sx.cfdi.CfdiDto
import sx.core.Cliente

// @CompileStatic
@Canonical
@ToString(includeNames = true)
class CuentaPorCobrarDTO {
    String id
    String clienteId
    String nombre
    String cfdiMail
    Map cliente
    String sucursal
    String tipo
    String tipoDocumento
    Date fecha
    Date vencimiento
    String formaDePago
    Long documento


    String moneda
    Double tipoDeCambio
    Double importe
    Double descuentoImporte
    Double subtotal
    Double impuesto
    Double total

    Double pagos
    Double saldo
    Integer atraso
    Date cancelada
    String cancelacionUsuario
    String cancelacionMotivo
    Date juridico = null
    String comentario
    CfdiDto cfdi


    CuentaPorCobrarDTO() {}

    CuentaPorCobrarDTO(CuentaPorCobrar cxc) {
        id = cxc.id
        clienteId = cxc.cliente.id
        cfdiMail = cxc.cliente.cfdiMail
        nombre = cxc.cliente.nombre
        cliente = [id: cxc.cliente.id, nombre: cxc.cliente.nombre, cfdiMail: cxc.cliente.cfdiMail]
        sucursal = cxc.sucursal.nombre
        tipo = cxc.tipo
        tipoDocumento = cxc.tipoDocumento
        formaDePago = cxc.formaDePago
        fecha = cxc.fecha
        vencimiento = cxc.vencimiento
        documento = cxc.documento

        if(cxc.cfdi) {
            cfdi = new CfdiDto(cxc.cfdi)
        }
        moneda = cxc.moneda
        tipoDeCambio = cxc.tipoDeCambio
        importe = cxc.importe
        descuentoImporte = cxc.descuentoImporte
        subtotal = cxc.subtotal
        impuesto = cxc.impuesto
        total = cxc.total
        pagos = cxc.pagos
        saldo = cxc.saldoReal
        atraso = cxc.atrasoCalculado
        cancelada = cxc.cancelada
        cancelacionUsuario = cxc.cancelacionUsuario
        cancelacionMotivo = cxc.cancelacionMotivo
        juridico = cxc.juridico
        comentario = cxc.comentario
    }
}

