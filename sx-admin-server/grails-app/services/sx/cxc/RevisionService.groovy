package sx.cxc


import grails.gorm.transactions.Transactional
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.core.Cliente


class RevisionService {

    def actualizarPendientes() {
        def rows = CuentaPorCobrar.findAll(
                "from CuentaPorCobrar c  " +
                        " where  c.tipo = ? " +
                        " and c.total - c.pagos > 0 " +
                        " and c.uuid is not null " +
                        " and c.credito is null " +
                        " order by c.fecha desc",
                ['CRE'])
        rows.each { CuentaPorCobrar cxc ->
            registrarRevision(cxc)
        }

    }

    @Transactional
    def registrarRevision(CuentaPorCobrar cxc) {
        if(cxc.credito)
            return cxc
        VentaCredito credito = new VentaCredito()
        // Propiedades
        credito.diaPago = cxc.cliente.credito.diaCobro
        credito.diaRevision = cxc.cliente.credito.diaRevision
        credito.plazo = cxc.cliente.credito.plazo
        credito.revisada = false
        credito.revision = cxc.cliente.credito.revision
        credito.cobrador = cxc.cliente.credito.cobrador
        credito.socio = cxc.cliente.credito.socio
        // Fechas
        Date vto = cxc.fecha + cxc.cliente.credito.plazo.intValue()
        cxc.vencimiento = vto
        credito.vencimiento = vto

        // Se congela la fecha original de revision
        credito.fechaRevisionCxc = getProximaRevision(vto, cxc.cliente.credito.diaRevision.intValue())
        credito.fechaRevision = credito.fechaRevisionCxc

        // Se congela la fecha orignal de pago
        credito.fechaPago = getProximoPago(vto, cxc.cliente.credito.diaCobro.intValue())
        credito.reprogramarPago = credito.fechaPago

        cxc.credito = credito
        cxc.save failOnError: true, flush: true
    }

    def actualizarRevision(VentaCredito credito) {
        Date hoy = new Date()
        Integer diaRevision = credito.diaRevision
        Integer diaPago = credito.diaPago
        credito.fechaRevision = getProximaRevision(hoy, diaRevision)
        credito.reprogramarPago = getProximoPago(hoy, diaPago)
        if(credito.fechaRevision >= credito.reprogramarPago) {
            credito.reprogramarPago = getProximoPago(credito.fechaRevision, diaPago)
        }
        credito.save failOnError: true, flush: true
        return credito
    }

    def actualizar(CuentaPorCobrar cxc) {
        if(cxc.credito == null)
            return registrarRevision(cxc)
        Date vto = cxc.vencimiento
        Integer diaRevision = cxc.credito.diaRevision
        Integer diaPago = cxc.credito.diaPago
        cxc.credito.fechaRevision = getProximaRevision(vto, diaRevision)
        cxc.credito.reprogramarPago = getProximoPago(vto, diaPago)
        cxc.credito.save failOnError: true, flush: true
        return cxc
    }

    def getProximaRevision(def fecha, Integer diaRevision){
        Integer dia = (fecha[Calendar.DAY_OF_WEEK] - 1)
        Integer dif = dia - diaRevision
        if (dif >= 0){
            def faltantes = 7 - dif
            def proximo = fecha + faltantes
            return proximo
        } else {
            def proximo = fecha + dif.abs()
            return proximo
        }
    }

    def getProximoPago(def fecha, int diaCobro){
        def dia = (fecha[Calendar.DAY_OF_WEEK] - 1)
        def dif = dia - diaCobro
        if(dif > 0 ) {
            def faltantes = 7 - dif
            def proxima = fecha + faltantes
            return proxima
        } else {
            def proxima = fecha + dif.abs()
            return proxima
        }
    }

    def generar() {
        def rows = CuentaPorCobrar.findAll(
                "from CuentaPorCobrar c  " +
                        " where  c.tipo = ? " +
                        " and c.total - c.pagos > 0 " +
                        " and c.uuid is not null " +
                        " and c.credito is null " +
                        " and c.cancelada is null" +
                        " order by c.fecha desc",
                ['CRE'])
        List generated = []
        rows.each {
            try{
                registrarRevision(it)
                generated << it
            }catch (Exception ex) {
                String msg = ExceptionUtils.getRootCauseMessage(ex)
                log.debug('Error al generar registrar revision y cobro para : {}', it.id)
                log.debug('Error: {}', msg)
            }
        }
        return generated
    }

    def recalcularPendientes(Date fecha = new Date(), String comentarioRepPago = ''){
        def rows = CuentaPorCobrar.findAll(
                "from CuentaPorCobrar c  " +
                        " where  c.credito != null and " +
                        " c.tipo = ? " +
                        " and c.credito is not null " +
                        " and c.credito.revision = true " +
                        " and c.total - c.pagos > 0 " +
                        " and c.uuid is not null " +
                        " order by c.fecha desc",
                ['CRE'])
        rows.each { CuentaPorCobrar cxc ->
            VentaCredito credito = cxc.credito
            if(!credito.revisada){
                credito.fechaRevision = getProximaRevision(fecha, credito.diaRevision)
                credito.save flush: true
            } else {
                credito.reprogramarPago = getProximoPago(fecha, credito.diaPago)
                credito.comentarioReprogramarPago = comentarioRepPago
                credito.save flush: true

            }
        }
    }

}
