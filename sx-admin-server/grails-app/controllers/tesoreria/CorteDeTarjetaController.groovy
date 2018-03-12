package sx.tesoreria

import com.luxsoft.utils.Periodo
import grails.rest.*
import grails.converters.*
import sx.core.Sucursal
import sx.cxc.CobroTarjeta

class CorteDeTarjetaController extends RestfulController {

    static responseFormats = ['json']

    CorteDeTarjetaService corteDeTarjetaService

    CorteDeTarjetaController() {
        super(CorteDeTarjeta)
    }

    def pendientes() {
        log.debug('Pendientes: {}', params)
        Periodo periodo = new Periodo()
        bindData(periodo, params)
        log.debug(' Periodo: {}', periodo)
        List<CobroTarjeta> cobros = CobroTarjeta.where{
            corteDeTarjeta == null && cobro.fecha == periodo.fechaInicial
        }.list()
        Map<String, List<CobroTarjeta>> grupos = cobros.groupBy {it.cobro.sucursal}
        def list = grupos.collect { [sucursal: it.key, cobros: it.value]}
        // respond list
        respond(cobros: list)
    }

}

class CobrosPorSucursal {
    String sucursal
    List<CobroTarjeta> cobros

    String toString() {
        "${sucursal} : Cobros: ${cobros.size()}"
    }
}
