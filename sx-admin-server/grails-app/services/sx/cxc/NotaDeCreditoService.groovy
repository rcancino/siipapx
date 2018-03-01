package sx.cxc

import grails.gorm.transactions.Transactional
import org.apache.commons.lang3.exception.ExceptionUtils

import lx.cfdi.v33.Comprobante
import com.luxsoft.cfdix.v33.NotaBuilder

import sx.core.Folio
import sx.cfdi.Cfdi
import sx.inventario.DevolucionDeVenta
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService
import com.luxsoft.utils.MonedaUtils

@Transactional
class NotaDeCreditoService {

    NotaBuilder notaBuilder

    CfdiService cfdiService

    CfdiTimbradoService cfdiTimbradoService

    def generarBonificacion(NotaDeCredito nota) {
        nota.tipo = 'BONIFICACION'
        nota.serie = 'BON'
        nota.folio = Folio.nextFolio('NOTA_DE_CREDITO', nota.serie)
        if(nota.tipoDeCalculo == 'PRORRATEO') {
            if (nota.total <= 0.0) {
                throw new NotaDeCreditoException('Nota de credito por bonificacion con prorrateo requiere de un total')
            }
            nota = calcularProrrateo(nota)
            Cobro cobro = generarCobro(nota)
            nota.save failOnError: true, flush: true
            cobro.save flush: true
            return nota

        } else {
            if (nota.descuento <= 0.0) {
                throw new NotaDeCreditoException('Nota de credito por bonificacion por porcentaje requiere de un descuento')
            }
            nota = calcularPorentaje(nota)
            Cobro cobro = generarCobro(nota)
            nota.save failOnError: true, flush: true
            cobro.save flush: true
            return nota
        }
    }

    def calcularProrrateo(NotaDeCredito nota) {
        BigDecimal importe = nota.total
        boolean sobreSaldo = nota.baseDelCalculo == 'Saldo' ? true : false
        List<CuentaPorCobrar> facturas = nota.partidas.collect{ it.cuentaPorCobrar}
        log.debug('Generando bonificaion por {} facturas', facturas.size())

        BigDecimal base = facturas.sum 0.0,{ item-> sobreSaldo ? item.getSaldo() : item.getTotal()}

        log.debug("Importe a prorratear: ${importe} Base del prorrateo ${base}")
        def acu = 0.0
        nota.partidas.each {  NotaDeCreditoDet det ->
            CuentaPorCobrar cxc = det.cuentaPorCobrar
            def monto = sobreSaldo ? cxc.getSaldo(): cxc.total
            def por = monto / base
            def asignado = MonedaUtils.round(importe * por)

            log.debug('Procesando factura {} Asignando {} a NotaDet', cxc.documento, asignado)

            acu = acu + asignado
            det.cuentaPorCobrar = cxc
            det.tipoDeDocumento = cxc.tipo
            det.fechaDocumento = cxc.fecha
            det.documento = cxc.documento
            det.sucursal = cxc.sucursal.nombre
            det.importe = asignado
            det.totalDocumento = cxc.total
            det.saldoDocumento = cxc.getSaldo()
            log.debug('Asignando partida {}', cxc.documento)
        }
        // println 'Acu: '+acu
        nota.total = importe
        nota.importe = MonedaUtils.calcularImporteDelTotal(nota.total)
        nota.impuesto = MonedaUtils.calcularImpuesto(nota.importe)
        log.debug('Partidas totales de la nota: {}', nota.partidas.size())
        return nota
    }

    def calcularPorentaje(NotaDeCredito nota) {
        log.debug('Generando bonificaion del {}%', nota.descuento)
        def acu = 0.0
        def descuento = nota.descuento / 100
        if ( nota.descuento2 > 0 ){
            def descuento2 = nota.descuento2;
            def r = 1.00 - descuento;
            def r2 = (descuento2 * r)/100
            def neto = descuento + r2;
            descuento = neto;
        }
        boolean sobreSaldo = nota.baseDelCalculo == 'Saldo' ? true : false
        nota.partidas.each {  NotaDeCreditoDet det ->
            CuentaPorCobrar cxc = det.cuentaPorCobrar
            def monto = sobreSaldo ? det.cuentaPorCobrar.getSaldo() : det.cuentaPorCobrar.getTotal()
            def asignado = MonedaUtils.round(monto * descuento)

            log.debug('Procesando factura {} Asignando {} a NotaDet', cxc.documento, asignado)
            acu = acu + asignado
            det.cuentaPorCobrar = cxc
            det.tipoDeDocumento = cxc.tipo
            det.fechaDocumento = cxc.fecha
            det.documento = cxc.documento
            det.sucursal = cxc.sucursal.nombre
            det.importe = asignado
            det.totalDocumento = cxc.total
            det.saldoDocumento = cxc.getSaldo()
            log.debug('Asignando partida {}', cxc.documento)
        }
        nota.total = acu
        nota.importe = MonedaUtils.calcularImporteDelTotal(nota.total)
        nota.impuesto = MonedaUtils.calcularImpuesto(nota.importe)
        log.debug('Partidas totales de la nota: {}', nota.partidas.size())
        return nota
    }

    def generarNotaDeDevolucion(NotaDeCredito nota, DevolucionDeVenta rmd) {
        if (rmd.cobro && nota.tipoCartera == 'CRE') {
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
        if (nota.tipoCartera == 'CRE'){
            Cobro cobro = generarCobro(nota)
            nota.save failOnError: true, flush: true
            aplicar(nota)
            rmd.cobro = cobro
            rmd.save flush: true
            return nota
        } else {
            nota.cobro = rmd.cobro
            nota.save failOnError: true, flush: true
            //aplicar(nota)
            return nota
        }

    }

    def generarCfdi(NotaDeCredito nota) {
        Comprobante comprobante = this.notaBuilder.build(nota);
        Cfdi cfdi = cfdiService.generarCfdi(comprobante, 'E')
        nota.cfdi = cfdi
        nota.save flush: true
        return nota
    }

    def timbrar(NotaDeCredito nota){
        try {
            if(!nota.cfdi) {
                nota = generarCfdi(nota)
            }
            def cfdi = nota.cfdi
            cfdi = cfdiTimbradoService.timbrar(cfdi)
            return nota
        } catch (Throwable ex){
            throw  new NotaDeCreditoException(ExceptionUtils.getRootCauseMessage(ex))
        }
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
        cobro.referencia = nota.folio.toString()
        cobro.formaDePago = nota.tipo == 'BON' ? 'BONIFICACION' : 'DEVOLUCION'
        nota.cobro = cobro
    }

    def aplicar(NotaDeCredito nota) {
        if (!nota.cobro) {
            generarCobro(nota)
        }
        Cobro cobro = nota.cobro
        log.debug('Aplicando cobro con disponible:{}', cobro.disponible)
        if(cobro.disponible <= 0.0) {
            throw new NotaDeCreditoException("Nota sin disponible no se puede aplicar")
        }
        if(nota.tipo.startsWith('BON')){
            this.aplicarCobroDeBonificacion(nota)
        } else {
            this.aplicarCobroDeDevolucion(nota)
        }
        cobro.save()
        return nota
    }

    protected aplicarCobroDeBonificacion(NotaDeCredito nota) {
        Cobro cobro = nota.cobro
        BigDecimal disponible = cobro.getDisponible()
        BigDecimal porAplicar = nota.partidas.sum 0.0, { it.importe}
        if(disponible < porAplicar) {
            throw new NotaDeCreditoException("Nota inconsistente para a plicar a sus relacionados " +
                    "Disponible:${disponible} Monto por aplicar: ${porAplicar}")
        }
        nota.partidas.each { NotaDeCreditoDet det ->
            CuentaPorCobrar cxc = det.cuentaPorCobrar
            if (cxc.saldo >= det.importe) {
                def aplicacion = new AplicacionDeCobro()
                aplicacion.cuentaPorCobrar = cxc
                aplicacion.fecha = new Date()
                aplicacion.importe = det.importe
                cobro.addToAplicaciones(aplicacion)
                if(!cobro.primeraAplicacion) {
                    cobro.primeraAplicacion = aplicacion.fecha
                }
            }
        }
    }

    protected aplicarCobroDeDevolucion(NotaDeCredito nota) {
        Cobro cobro = nota.cobro
        DevolucionDeVenta rmd = DevolucionDeVenta.where{ cobro == cobro}.find()
        if (rmd == null) {
            return
        }
        CuentaPorCobrar cxc = rmd.venta?.cuentaPorCobrar
        if (cxc) {
            BigDecimal saldo = cxc.saldo
            if(saldo > 0) {
                def importe = cobro.disponible <= saldo ? cobro.disponible : saldo
                def aplicacion = new AplicacionDeCobro()
                aplicacion.cuentaPorCobrar = cxc
                aplicacion.fecha = new Date()
                aplicacion.importe = importe
                cobro.addToAplicaciones(aplicacion)
                if(!cobro.primeraAplicacion) {
                    cobro.primeraAplicacion = aplicacion.fecha
                }
            }
        }
    }

    def eliminar(NotaDeCredito nota) {
        if(nota.cfdi ){
            Cfdi cfdi = nota.cfdi
            if (cfdi) {
                throw new NotaDeCreditoException('Nota de credito timbrada no se puede eliminar')
            }
        }
        // Eliminar el cobro
        Cobro cobro = nota.cobro
        nota.cobro = null
        cobro.delete flush: true
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