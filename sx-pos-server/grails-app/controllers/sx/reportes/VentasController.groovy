package sx.reportes


import grails.plugins.jasper.JasperExportFormat
import grails.plugins.jasper.JasperReportDef
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Sucursal

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class VentasController {

    // static responseFormats = ['json', 'xml']


    def reporteService

    def ventasDiarias() {
        
        println 'Fecha: ' + params.fecha
        params.FECHA = params.fecha
        params.ORIGEN = 'CON'
        println "Generando  Reporte... con params: " + params
        def repParams = [:]

        fileFormat: JasperExportFormat.PDF_FORMAT
        repParams['ORIGEN'] = params.ORIGEN
        repParams['SUCURSAL'] = params.SUCURSAL
        repParams['FECHA'] = params.FECHA
        println 'Ejecutando reporte de Ventas Diarias con parametros: ' + repParams +"---"+params.name+"---"+params.fileName
        def pdf = this.reporteService.run('ventas_diarias', repParams)
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
        def pdf = this.reporteService.run('CobranzaCamioneta', repParams)
        def fileName = "CobranzaCOD.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)

    }

    def cobranzaEfectivo() {

       def pdf= this.reporteService.reporteFechaSucursal(params.SUCURSAL,params.FECHA, 'CobranzaEfectivo')
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "CobranzaEfectivo.pdf")
    }

    def cobranzaContado(){
        def pdf= this.reporteService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,'FacturasCobrada')
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "CobranzaContado.pdf")
    }

    def facturasCanceladas(){
        def pdf= this.reporteService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,'')
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "FacturasCanceladas.pdf")
    }

    def aplicacionDeSaldos(){
        def pdf= this.reporteService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,params.name)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "AplicacionDeSaldos.pdf")
    }

    def disponiblesSucursal(){
        def pdf= this.reporteService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,params.name)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "DisponiblesSucursal.pdf")
    }

    def facturasPendientesCod(){
        def pdf= this.reporteService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,'fac_pen_camioneta')
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "FacturasPendientesCOD.pdf")
    }

    def facturasPendientesCodEmbarques(){
        def pdf= this.reporteService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,'fac_pen_camionetaNew')
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "FacturasPendientesCODEmbarques.pdf")
    }

    def ventasDiariasCheques(){
        def pdf= this.reporteService.reporteFechaSucursal(params.SUCURSAL,params.FECHA,params.name)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "VentasDiariasCheques.pdf")
    }


}





