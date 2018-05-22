package sx.cxc


import grails.gorm.transactions.Transactional
import org.apache.commons.lang3.exception.ExceptionUtils


class RevisionService {

    def buscarPendientes(){
        def rows = VentaCredito.findAll(
                "from VentaCredito v join v.cuentaPorCobrar   c  " +
                        " where  c.tipo = ? " +
                        " and c.total - c.pagos > 0 " +
                        " and c.cfdi.uuid is not null " +
                        " and c.credito is not null " +
                        " and c.cancelada is null" +
                        " order by c.fecha desc",
                ['CRE'])
        return rows
    }

    def generar() {
        def rows = CuentaPorCobrar.findAll(
                "from CuentaPorCobrar c  " +
                        " where  c.tipo = ? " +
                        " and c.total - c.pagos > 0 " +
                        " and c.cfdi.uuid is not null " +
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
        if(!credito.revisada) {
            credito.fechaRevision = getProximaRevision(hoy, diaRevision)
        }
        credito.reprogramarPago = getProximoPago(hoy, diaPago)
        if(credito.fechaRevision >= credito.reprogramarPago) {
            credito.reprogramarPago = getProximoPago(credito.fechaRevision, diaPago)
        }
        credito.save failOnError: true, flush: true
        return credito
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



    def recalcularPendientes(Date fecha = new Date(), String comentarioRepPago = ''){

        def rows = VentaCredito.findAll(
                "from VentaCredito c  " +
                        " c.tipo = ? " +
                        " and c.revision = true " +
                        " and c.cxc.total - c.cxc.pagos > 0 " +
                        " and c.cxc.uuid is not null " +
                        " order by c.fecha desc",
                ['CRE'])
        rows.each { VentaCredito credito ->
            actualizarRevision(credito)
        }
    }

}
