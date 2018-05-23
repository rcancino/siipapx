package sx.cxc

import com.luxsoft.utils.MonedaUtils

class ComisionService {

    def generarComisionesCobrador(Date fechaIni, Date fechaFin){
        def origen = 'CRE'
        def rows = AplicacionDeCobro.findAll(
                "select a.cuentaPorCobrar.id, a.fecha, a.importe from AplicacionDeCobro a " +
                        "where a.fecha between ? and ? " +
                        "  and a.cuentaPorCobrar.tipo = ? " +
                        " and formaDePago not in('DEVOLUCION','BONIFICACION','PAGO_DIF') " +
                        " and a.cuentaPorCobrar.cliente.credito.cobrador.sw2 != 1 " +
                        " and a.cobro.anticipo = false and a.cobro.enviado = false " +
                        " and date(a.fecha) = date(a.cobro.primeraAplicacion)"
                ,[fechaIni, fechaFin, origen])

        Map<String, Object> cmap = rows.groupBy {it[0]}
        List<Comision> comisiones = []
        cmap.each { it ->
            String id = it.key
            def data = it.value
            BigDecimal pagoComisionable = data.sum { r -> r[2]}
            Date pagoComision = data.first()[1]
            println 'Cxc: '+ id + ' Fecha: ' +pagoComision + ' Total: '+ pagoComisionable + ' Raw: ' + data
            CuentaPorCobrar cxc = CuentaPorCobrar.get(id);
            Comision comision = new Comision()
            comision.cxc = cxc
            comision.cliente = cxc.cliente.nombre
            comision.comisionista = cxc.cliente.credito.cobrador.nombres
            comision.clave = cxc.cliente.credito.cobrador.sw2
            comision.sucursal = cxc.sucursal.nombre
            comision.documentoTipo = cxc.tipo
            comision.documento = cxc.documento
            comision.fechaDocto = cxc.fecha
            comision.total = cxc.total
            comision.atraso = cxc.atraso
            comision.comision = cxc.cliente.credito.cobrador.comision

            comision.tipo = 'COB'
            comision.fechaIni = fechaIni
            comision.fechaFin = fechaFin

            comision.fechaCobro = pagoComision
            comision.pagoComision = new Date()
            comision.pagoComisionable = pagoComisionable
            BigDecimal comisionImporte = comision.pagoComisionable * (comision.comision/100)
            comision.comisionImporte = MonedaUtils.round(comisionImporte)
            comision.save failOnError: true, flush: true
            comisiones << comision
        }
        return comisiones

    }
}


