package sx.core

import grails.compiler.GrailsCompileStatic
import grails.plugins.jasper.JasperExportFormat
import grails.plugins.jasper.JasperReportDef
import grails.plugins.jasper.JasperService

@GrailsCompileStatic
class ReportService {

  JasperService jasperService

    ByteArrayOutputStream run(String reportName, Map params ) {
    def reportDef= new JasperReportDef(
            name: reportName,
            fileFormat: JasperExportFormat.PDF_FORMAT,
            parameters: params
    )
    ByteArrayOutputStream pdfStream = jasperService.generateReport(reportDef)
    return pdfStream
  }

  ByteArrayOutputStream run(String reportName, Map params, Collection data){
    log.debug('Ejecutando reporte: ' + reportName)
    def reportDef=new JasperReportDef(
            name:reportName,
            fileFormat: JasperExportFormat.PDF_FORMAT,
            parameters:params,
            reportData:data
    )
    ByteArrayOutputStream  stream=jasperService.generateReport(reportDef)
    return stream
  }
}




