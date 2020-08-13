package sx.cfdi


import grails.web.context.ServletContextHolder

import sx.reports.ReportService
import com.luxsoft.cfdix.v33.V33PdfGeneratorPos
import com.luxsoft.cfdix.v33.ReciboDePagoPdfGenerator
import sx.cxc.Cobro

/**
 * Servicios de impresion de facturas centralizados
 *
 */
class CfdiPrintService {

    CfdiLocationService cfdiLocationService

    ReportService reportService

    Byte[] getPdf(Cfdi cfdi){
        String fileName = cfdi.url.getPath().substring(cfdi.url.getPath().lastIndexOf('/')+1)
        fileName = fileName.replaceAll('.xml', '.pdf')
        File file = new File(cfdiLocationService.getCfdiLocation(cfdi), fileName)
        if(file.exists()){
            return file.getBytes()
        } else {
            log.info('No hay impresion generada, generando archivo PDF');
            ByteArrayOutputStream out = generarPdf(cfdi)
            file.setBytes(out.toByteArray())
            return file.getBytes()
        }
    }

    public generarPdf( Cfdi cfdi) {
        if (cfdi.origen == 'VENTA'){
            return generarFactrura(cfdi)
        } else if(cfdi.origen == 'NOTA_CARGO'){
            // return generarPdfNotaDeCargo(cfdi)
        } else if(cfdi.tipoDeComprobante == 'P' ){
            return generarReciboDePago(cfdi)
        } else {
            // return generarPdfNota(cfdi)
            throw new RuntimeException('Tipo de factura no soportado')
        }
    }

    /** 
    * Genera el JasperPrint PDF (ByteArrayOutputStream) para Comprobantes tipo VENTA
    * 
    **/
    public generarFactrura(Cfdi cfdi){
        def realPath = ServletContextHolder.getServletContext().getRealPath("/reports") ?: 'reports'
        def data = V33PdfGeneratorPos.getReportData(cfdi)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }

    public generarReciboDePago( Cfdi cfdi) {
        def realPath = ServletContextHolder.getServletContext().getRealPath("/reports") ?: 'reports'
        Cobro cobro = Cobro.where{cfdi == cfdi}.find()
        def data = ReciboDePagoPdfGenerator.getReportData(cobro)
        Map parametros = data['PARAMETROS']
        parametros.LOGO = realPath + '/PAPEL_CFDI_LOGO.jpg'
       return reportService.run('ReciboDePagoCFDI33.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }
}
