package sx.reportes


import grails.plugins.jasper.JasperExportFormat
import grails.plugins.jasper.JasperReportDef
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Sucursal
import sx.reports.ReportService

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class VentasController {

    // static responseFormats = ['json', 'xml']


    ReportService reportService

    def ventasDiarias(PorFechaCommand command) {
        def repParams = [:]
        repParams['ORIGEN'] = 'CON'
        repParams['SUCURSAL'] = params.SUCURSAL
        repParams['FECHA'] = command.fecha.format('dd/MM/yyyy')
        def pdf = this.reportService.run('ventas_diarias', repParams)
        def fileName = "VentasDiarias.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def cobranzaCod() {
        // println "Generando  Reporte... con params: " + param
        def repParams = [:]

        fileFormat: JasperExportFormat.PDF_FORMAT

        repParams['SUCURSAL'] = params.SUCURSAL
        repParams['FECHA'] = params.FECHA
        repParams['SALDOAFAVOR']=0.0
        println 'Ejecutando reporte de Cobranza COD con parametros: ' + repParams +"---"+params.name+"---"+params.fileName
        def pdf = this.reportService.run('CobranzaCamioneta', repParams)
        def fileName = "CobranzaCOD.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)

    }

    def cobranzaEfectivo() {
        def pdf = reportService.run('CobranzaEfectivo', params)
        // def pdf= this.reportService.reporteFechaSucursal(params.SUCURSAL,params.FECHA, 'CobranzaEfectivo')
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "CobranzaEfectivo.pdf")
    }

    def cobranzaContado(){
        def pdf = reportService.run('FacturasCobrada', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "CobranzaContado.pdf")
    }

    def facturasCanceladas(){
        params.FECHA = params.fecha
        def pdf = reportService.run('FacturasCanceladas', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "FacturasCanceladas.pdf")
    }

    def aplicacionDeSaldos(){
        // params.FECHA = params.fecha
        def pdf = reportService.run('AplicacionDeSaldos', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "AplicacionDeSaldos.pdf")
    }

    def disponiblesSucursal(){
        params.FECHA = params.fecha
        def pdf = reportService.run('DisponiblesSucursal', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "DisponiblesSucursal.pdf")
    }

    def facturasPendientesCod(){
        def pdf = reportService.run('fac_pen_camioneta', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "fac_pen_camioneta.pdf")
    }

    def facturasPendientesCodEmbarques(){
        params.FECHA = params.fecha
        // def pdf= this.reportService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,'fac_pen_camionetaNew')
        def pdf = reportService.run('fac_pen_camionetaNew', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "fac_pen_camionetaNew'.pdf")
    }

    def ventasDiariasCheques(){
        params.FECHA = params.fecha
        // def pdf= this.reportService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,'fac_pen_camionetaNew')
        def pdf = reportService.run('ventas_diariasCHE', params)
        // def pdf= this.reportService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,params.name)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "VentasDiariasCheques.pdf")
    }


}


class PorFechaCommand {
    Date fecha

    String toString() {
        return fecha.format('dd/MM/yyyy')
    }
}




