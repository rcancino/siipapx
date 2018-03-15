package sx.tesoreria

import com.luxsoft.utils.Periodo
import grails.rest.*
import grails.converters.*
import sx.core.Sucursal
import sx.cxc.Cobro
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
            corte == null && cobro.fecha == periodo.fechaInicial
        }.list()
        Map<String, List<CobroTarjeta>> grupos = cobros.groupBy {it.cobro.sucursal}
        //def list = grupos.collect { [sucursal: it.key, cobros: it.value]}
        List<CobrosPorSucursal> list = grupos.collect { it ->
            new CobrosPorSucursal(sucursal: it.key, cobros: it.value, fecha: periodo.fechaInicial)
        }
        // respond list
        respond(cobros: list)
    }

    def generarCortes() {
        CobrosPorSucursal cobrosPorSucursal = new CobrosPorSucursal();
        bindData cobrosPorSucursal, getObjectToBind()
        corteDeTarjetaService.generar(cobrosPorSucursal.fecha, cobrosPorSucursal.sucursal, cobrosPorSucursal.cobros)
        log.debug('Cobros por sucursal: {}', cobrosPorSucursal)
        respond status: 200
    }

    def ajustarCobro() {
        CobroTarjeta cobroTarjeta = new CobroTarjeta()
        bindData(cobroTarjeta, getObjectToBind())
        Cobro cobro = cobroTarjeta.cobro
        cobro.tarjeta.visaMaster = cobroTarjeta.visaMaster
        cobro.tarjeta.debitoCredito = cobroTarjeta.debitoCredito
        cobro.save flush:true
        respond cobro
    }

    def aplicar(CorteDeTarjeta corte){
        corte = corteDeTarjetaService.aplicar(corte)
        respond corte
    }

    @Override
    protected void deleteResource(Object resource) {
        corteDeTarjetaService.eliminarCorte(resource);
    }
}

class CobrosPorSucursal {

    Date fecha
    Sucursal sucursal
    List<CobroTarjeta> cobros

    String toString() {
        "${sucursal} : Cobros: ${cobros.size()} Fecha: ${fecha?.format('dd/MM/yyyy')}"
    }
}
