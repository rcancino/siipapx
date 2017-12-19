package sx.inventario

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import grails.converters.*
import sx.reports.ReportService

@Secured("ROLE_INVENTARIO_USER")
class TrasladoController extends RestfulController {

    static responseFormats = ['json']

    SolicitudDeTrasladoService solicitudDeTrasladoService

    TrasladoService trasladoService

    ReportService reportService

    TrasladoController() {
        super(Traslado)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 100;
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = Traslado.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        if(params.tipo) {
            query = query.where {tipo == params.tipo}
        }
        return query.list(params)
    }

    def salida(Traslado tps){
        if(tps == null) {
            notFound()
            return
        }
        assert tps.tipo == 'TPS', 'El registro de salida es para trasplados tipo TPS'
        def res = trasladoService.registrarSalida(tps)
        respond res

    }

    def entrada(Traslado tpe){
        if(tpe == null) {
            notFound()
            return
        }
        assert tpe.tipo == 'TPE', 'El registro de entrada es para trasplados tipo TPE'
        def res = trasladoService.registrarEntrada(tpe)
        respond res

    }

    def print() {
        params.TRALADO_ID = params.ID
        Traslado traslado = Traslado.get(params.ID)
        params.SOL_ID = traslado.solicitudDeTraslado.id
        def reportName = traslado.tipo == 'TPS' ? 'SalidaDeTraslado.jrxml' : 'EntradaTraslado.jrxml'
        log.debug('Imprimiendo Traslado: {}-{} Usando reporte: {} params: {}', traslado.tipo, traslado.documento, reportName, params)
        def pdf =  reportService.run(reportName, params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Traslado.pdf')
    }
}
