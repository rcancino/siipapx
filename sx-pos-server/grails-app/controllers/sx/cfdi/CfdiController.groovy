package sx.cfdi

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

import com.luxsoft.cfdix.v33.V33PdfGenerator
import grails.rest.RestfulController
import sx.reports.ReportService


@Secured("hasRole('ROLE_POS_USER')")
class CfdiController extends RestfulController{

    CfdiTimbradoService cfdiTimbradoService

    ReportService reportService

    static responseFormats = ['json']

    CfdiController(){
        super(Cfdi)
    }

    def mostrarXml(Cfdi cfdi){
        if(cfdi == null ){
            notFound()
            return
        }
        render (file: cfdi.getUrl().newInputStream(), contentType: 'text/xml', filename: cfdi.fileName, encoding: "UTF-8")
    }

    @Transactional
    def print( Cfdi cfdi) {
        def pdf = null
        if(cfdi.versionCfdi == '3.3') {
            pdf = generarImpresionV33(cfdi)
        } else {
            pdf = generarImpresionV32(cfdi)
        }
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: cfdi.fileName)
        //render [:]
    }

    private generarImpresionV33( Cfdi cfdi) {
        def realPath = servletContext.getRealPath("/reports")
        def data = V33PdfGenerator.getReportData(cfdi)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }

    private generarImpresionV32( Cfdi cfdi) {
    }

}
