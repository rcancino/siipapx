package sx.cfdi

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

import com.luxsoft.cfdix.v33.V33PdfGenerator
import grails.rest.RestfulController
import sx.core.Venta
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
        cfdi.getUrl().getBytes()
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
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def data = V33PdfGenerator.getReportData(cfdi)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }

    private generarImpresionV32( Cfdi cfdi) {
    }


    def enviarFacturaEmail(Venta factura) {
        Cfdi cfdi = factura.cuentaPorCobrar.uuid
        def email = params[email] ?: factura.cliente.getCfdiMail()
        if (email) {
            def xml = cfdi.getUrl().getBytes()
            def pdf = generarImpresionV33(cfdi)
            sendMail {
                multipart true
                from "me@org.com"
                to "rubencancino6@gmail.com"
                subject "Correo de prueba"
                text "Correo de prueba"
                attach("${cfdi.uuid}.xml", 'text/xml', xml)
            }
        }
        log.debug('Correo enviado para CFDI: {}', cfdi.uuid)
        respond factura
    }

}

class EnvioDeFacturaCfdiCommand {
    String target
    Venta factura

    static constraints = {
        target email: true
    }

    String toString() {
        return "${factura?.statusInfo()} Email:${target}"
    }
}
