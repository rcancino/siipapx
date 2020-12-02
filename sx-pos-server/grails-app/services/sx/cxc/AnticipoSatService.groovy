package sx.cxc

import groovy.util.logging.Slf4j
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import grails.gorm.transactions.Transactional

@Slf4j
@Transactional
class AnticipoSatService {

    def generarAnticipo(CuentaPorCobrar cuentaPorCobrar) {
        if(cuentaPorCobrar.tipo != 'ANT') 
            throw new RuntimeException("La factura ${cuentaPorCobrar.folio} no es de anticipo")
        log.info('Generando anticipo para CXC: {}, UUID: {} ', cuentaPorCobrar.getFolio(), cuentaPorCobrar.cfdi.uuid)
        def comprobante = cuentaPorCobrar.cfdi
        def anticipo = new AnticipoSat()
        anticipo.sucursal = cuentaPorCobrar.sucursal.nombre
        anticipo.cliente = cuentaPorCobrar.cliente.id
        anticipo.nombre = cuentaPorCobrar.cliente.nombre
        anticipo.rfc = cuentaPorCobrar.cliente.rfc
        anticipo.fecha = cuentaPorCobrar.fecha
        anticipo.cxc = cuentaPorCobrar.id
        anticipo.cxcDocumento = cuentaPorCobrar.documento
        anticipo.moneda = cuentaPorCobrar.moneda.currencyCode
        anticipo.tipoDeCambio = cuentaPorCobrar.tipoDeCambio
        anticipo.cfdi = comprobante.id
        anticipo.cfdiSerie = comprobante.serie
        anticipo.cfdiFolio = comprobante.folio
        anticipo.uuid = comprobante.uuid
        anticipo.importe = cuentaPorCobrar.importe
        anticipo.impuesto = cuentaPorCobrar.impuesto
        anticipo.total = cuentaPorCobrar.total
        anticipo.comentario = cuentaPorCobrar.comentario
        anticipo.createUser = cuentaPorCobrar.createUser
        anticipo.updateUser = cuentaPorCobrar.updateUser
        anticipo.save failOnError: true, flush: true
        return anticipo
    }


    Cobro generarCobroConAnticipo(CuentaPorCobrar cxc, AnticipoSat anticipo) {
        if(cxc.saldo <= 0.0) 
            throw new RuntimeException("La factura ${cxc.folio} ya esta pagada")
        if(anticipo.disponible <= 0.0) 
            throw new RuntimeException("Anticipo: ${anticipo.folio} ya no tiene disponible")
        Cobro cobro = new Cobro([anticipo: true, anticipoSat: anticipo.id]);
        cobro.with {
            id = "TST${new Date().time}"
            sucursal = cxc.sucursal
            cliente = cxc.cliente
            tipo = cxc.tipo
            fecha = new Date()
            formaDePago = 'COMPENSACION'
            moneda = cxc.moneda
            tipoDeCambio = cxc.tipoDeCambio
            importe = cxc.total
            referencia = anticipo.folio
            createUser = cxc.createUser
            updateUser = cxc.updateUser
            comentario = "Pago con anticipo ${anticipo.folio}"
        }
        cxc.relacionados = buildRelacionados(cxc)
        cxc.save()
        cobro.save failOnError: true, flush: true
        return cobro
    }


    String buildRelacionados(CuentaPorCobrar... facturas) {
        return new JsonBuilder(facturas.collect{[tipo: '07', uuid:it.cfdi.uuid]}).toString()
    }

    List parseRelacionados(String json) {
        return new JsonSlurper().parseText(json)
    }
    

}
