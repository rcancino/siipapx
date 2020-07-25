package sx.cxc

import groovy.util.logging.Slf4j

import grails.gorm.transactions.Transactional

import org.apache.commons.lang3.exception.ExceptionUtils

import lx.cfdi.v33.Comprobante
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService

import com.luxsoft.cfdix.v33.NotaDeCargoBuilder
import com.luxsoft.utils.MonedaUtils

import sx.core.LogUser
import sx.core.AppConfig
import sx.core.Folio
import sx.core.Sucursal

@Transactional
@Slf4j
class NotaDeCargoService implements LogUser{

    CfdiTimbradoService cfdiTimbradoService

    CfdiService cfdiService

    NotaDeCargoBuilder notaDeCargoBuilder

    Sucursal sucursal = null

    NotaDeCargo save(NotaDeCargo nota){
        if(nota.id) {
            throw new NotaDeCargoException('Nota ya ha sido salvada')
        }
        nota.folio = Folio.nextFolio('NOTA_DE_CARGO', nota.serie)
        nota.sucursal = getSucursal()
        logEntity(nota)
        nota.save failOnError: true, flush:true
    }

    NotaDeCargo update(NotaDeCargo nota){
        log.info('Actualizando nota: {}', nota.folio)
        if(nota.cfdi && nota.cfdi.uuid) {
            throw new CargoTimbradoException(nota)
        }
        actualizarCuentaPorCobrar(nota)
        logEntity(nota)
        nota.save failOnError: true, flush:true
    }

    def actualizarCuentaPorCobrar(NotaDeCargo nota) {
        if(nota.cuentaPorCobrar == null) {
            nota.cuentaPorCobrar = generarCuentaPorCobrar(nota)
            return
        } else {
            CuentaPorCobrar cxc = nota.cuentaPorCobrar
            cxc.importe = nota.importe
            cxc.impuesto = nota.impuesto
            cxc.subtotal = nota.importe
            cxc.total = nota.total
            cxc.comentario = nota.comentario
            logEntity(cxc)
            // cxc.save()
        }
        
    }


    def generarCuentaPorCobrar(NotaDeCargo nota) {
        CuentaPorCobrar cxc = new CuentaPorCobrar()
        cxc.sucursal = nota.sucursal
        cxc.cliente = nota.cliente
        cxc.tipoDocumento = 'NOTA_DE_CARGO'
        cxc.importe = nota.importe
        cxc.descuentoImporte = 0.0
        cxc.subtotal = nota.importe
        cxc.impuesto = nota.impuesto
        cxc.total  = nota.total
        cxc.formaDePago = nota.formaDePago
        cxc.moneda = nota.moneda
        cxc.tipoDeCambio = nota.tipoDeCambio
        cxc.comentario = nota.comentario
        cxc.tipo = nota.tipo
        cxc.documento = nota.folio
        cxc.fecha = new Date()
        cxc.comentario = nota.comentario
        logEntity(cxc)
        // nota.cuentaPorCobrar = cxc
        // cxc.save()
        return cxc
    }

    

    def delete(NotaDeCargo nota){
        if(nota.cfdi) {
          throw new NotaDeCargoException("Nota de cargo ya timbrada no se puedd eliminar")
        }
        def cxc = nota.cuentaPorCobrar
        nota.cuentaPorCobrar = null
        nota.delete flush: true
        cxc.delete flush: true
    }

    def generarCfdi(NotaDeCargo nota) {
        Comprobante comprobante = this.notaDeCargoBuilder.build(nota);
        Cfdi cfdi = cfdiService.generarCfdi(comprobante, 'I', 'NOTA_CARGO')
        nota.cuentaPorCobrar.cfdi = cfdi
        nota.cuentaPorCobrar.uuid = cfdi.uuid
        nota.cfdi = cfdi
        nota.save flush: true
        return nota
    }

    def timbrar(NotaDeCargo nota){
        if(nota.cfdi && nota.cfdi.uuid) {
            throw new CargoTimbradoException(nota)
        }
        if(!nota.cfdi) {
            nota = generarCfdi(nota)
        }
        def cfdi = nota.cfdi
        cfdi = cfdiTimbradoService.timbrar(cfdi)
        return nota
    }

    def cancelar(NotaDeCargo cargo, String motivo) {

        def cxc = cargo.cuentaPorCobrar
        def cfdi = cxc.cfdi

        // 1o Marcar la cancelacion
        cargo.cancelacion = new Date()
        cargo.cancelacionMotivo = motivo
        cargo.cancelacionUsuario = getCurrentUserName()
        cargo.save()

        // 2o Cancelar la cuenta por cobrar 
        cancelarCuentaPorCobrar(cxc, motivo, cargo.cancelacionUsuario)

        // 3o Cancelar el CFDI
        if( cfdi && cfdi.uuid) {
            cfdi.status = 'CANCELACION_PENDIENTE'
            cfdi.save flush:true
        }
        return cargo
    }

    /**
     * Cancela la cuenta por cobrar
     *
     * @param cxc
     * @return
     */
    def cancelarCuentaPorCobrar(CuentaPorCobrar cxc, String motivo, String usuario) {
        cxc.importe = 0.0
        cxc.impuesto = 0.0
        cxc.subtotal = 0.0
        cxc.descuentoImporte = 0.0
        cxc.total = 0.0
        cxc.comentario = 'CANCELADA'
        cxc.cancelada = new Date()
        cxc.cancelacionUsuario = usuario
        cxc.cancelacionMotivo = motivo
        cxc.cfdi = null
        logEntity(cxc)
        cxc.save()
        // cxc.delete flush: true
    }

    def generarConceptoUnico(NotaDeCargo nota) {
        if(!nota.partidas) {
            log.info('Generando concepto unico para nora tipo {} N.Cargo ', nota.tipo)
            NotaDeCargoDet det = new NotaDeCargoDet()
            det.comentario = nota.comentario

            BigDecimal total = nota.total
            det.total = total
            det.importe = MonedaUtils.calcularImporteDelTotal(total)
            det.impuesto = MonedaUtils.calcularImpuesto(det.importe)
            det.total = det.importe + det.impuesto

            det.documento = nota.folio
            det.documentoTipo = nota.tipo
            det.documentoSaldo = 0.0
            det.documentoTotal = 0.0
            det.documentoFecha = nota.fecha
            det.sucursal = nota.sucursal.nombre
            nota.addToPartidas(det)
        }
    }

    def actualizarConceptoUnico(NotaDeCargo nota) {
        if(nota.partidas && nota.tipo == 'CHE') {
            NotaDeCargoDet det = nota.partidas[0]
            det.comentario = nota.comentario

            BigDecimal total = nota.total
            det.total = total
            det.importe = MonedaUtils.calcularImporteDelTotal(total)
            det.impuesto = MonedaUtils.calcularImpuesto(det.importe)
            det.total = det.importe + det.impuesto

            det.documento = nota.folio
            det.documentoTipo = nota.tipo
            det.documentoSaldo = nota.total
            det.documentoTotal = nota.total
            det.documentoFecha = nota.fecha
            det.sucursal = nota.sucursal.nombre
        }
    }

    Sucursal getSucursal() {
        if(!this.sucursal) {
            this.sucursal = AppConfig.first().sucursal
        }
        return this.sucursal
    }

    /**
    * Depreciado, se actualiza en la UI
    *   
    */
    private actualizarPartidas(NotaDeCargo nota){
        nota.partidas.each { NotaDeCargoDet det ->
            if (det.cuentaPorCobrar) {
                CuentaPorCobrar cxc = det.cuentaPorCobrar
                det.documento = cxc.documento
                det.documentoFecha = cxc.fecha
                det.documentoTipo= cxc.tipo
                det.documentoTotal = cxc.total
                det.documentoSaldo = cxc.saldo
                det.sucursal = cxc.sucursal.nombre
            }
            det.comentario = nota.comentario
        }
    }

    /**
    * Depreciado, se actualiza en la UI
    *   
    */
    def calcularProrrateo(NotaDeCargo nota) {
        assert nota.total >0 , 'Nota de cargo requiere total para proceder Total registrado: ' + nota.total
        nota.cargo = 0.0;
        BigDecimal importe = nota.total

        List<CuentaPorCobrar> facturas = nota.partidas.collect{ it.cuentaPorCobrar}
        NotaDeCargoDet sinSaldo = nota.partidas.find{it.cuentaPorCobrar.getSaldo() == 0.0}
        log.debug('Se encontro una factura con saldo {}', sinSaldo)
        boolean sobreSaldo = sinSaldo == null

        BigDecimal base = facturas.sum 0.0,{ item -> sobreSaldo ? item.getSaldo() : item.getTotal()}

        log.debug("Importe a prorratear: ${importe} Base del prorrateo ${base} Tipo ${sobreSaldo ? 'SOBR SALDO': 'SOBRE TOTAL'}")

        nota.partidas.each {  NotaDeCargoDet det ->
            CuentaPorCobrar cxc = det.cuentaPorCobrar
            def monto = sobreSaldo ? cxc.getSaldo(): cxc.total
            def por = monto / base
            def asignado = MonedaUtils.round(importe * por)
            det.importe = MonedaUtils.calcularImporteDelTotal(asignado)
            det.impuesto = MonedaUtils.calcularImpuesto(det.importe)
            det.total = det.importe + det.impuesto
        }
        return nota
    }

    /**
    * Depreciado, se actualiza en la UI
    *   
    */
    def calcularPorentaje(NotaDeCargo nota) {
        log.debug('Generando Nota de cargo por el {}%', nota.cargo)
        def cargo = nota.cargo / 100
        boolean sobreSaldo = true
        nota.partidas.each {  NotaDeCargoDet det ->
            CuentaPorCobrar cxc = det.cuentaPorCobrar
            def monto = sobreSaldo ? det.cuentaPorCobrar.getSaldo() : det.cuentaPorCobrar.getTotal()
            def total = MonedaUtils.round(monto * cargo)
            det.total = total
            det.importe = MonedaUtils.calcularImporteDelTotal(total)
            det.impuesto = MonedaUtils.calcularImpuesto(det.importe)
        }
        return nota
    }



}

class NotaDeCargoException  extends RuntimeException {

    NotaDeCargo cargo

    NotaDeCargoException(String message) {
        super(message)
    }
    NotaDeCargoException(String message, NotaDeCargo nc) {
        super(message)
        this.cargo = nc
    }
}

class CargoTimbradoException extends NotaDeCargoException {
    CargoTimbradoException(NotaDeCargo c) {
        super("Nota de cargo ${c.folio} ya ha sido Timbrada con el folio: ${c.cfdi.uuid}", c)
    }
}

