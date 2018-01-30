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

    private generarImpresionV33( Cfdi cfdi, boolean envio = false) {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def data = V33PdfGenerator.getReportData(cfdi, envio)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }

    private generarImpresionV32( Cfdi cfdi) {
    }


    def enviarFacturaEmail(EnvioDeFacturaCfdiCommand command) {
        if (command == null) {
            notFound()
            return
        }
        Venta factura = command.factura
        assert command.factura.cuentaPorCobrar, "La venta ${factura.statusInfo()} no se ha facturado"
        assert command.factura.cuentaPorCobrar.cfdi.uuid, "La factura ${factura.statusInfo()} no se ha timbrado"

        Cfdi cfdi = factura.cuentaPorCobrar.cfdi
        String targetEmail = command.target

        if (targetEmail) {
            def xml = cfdi.getUrl().getBytes()
            def pdf = generarImpresionV33(cfdi, true).toByteArray()
            sendMail {

                multipart false
                to targetEmail
                subject "Envio de CFDI ${cfdi.uuid}"
                text "Apreciable cliente por este medio le hacemos llegar la factura electrónica de su compra"
                attach("${cfdi.serie}-${cfdi.folio}.xml", 'text/xml', xml)
                attach("${cfdi.serie}-${cfdi.folio}.pdf", 'application/pdf', pdf)
            }
            cfdi.enviado = new Date()
            cfdi.email = targetEmail
            cfdi.save flush: true
            if(!factura.cfdiMail) {
                factura.cfdiMail = targetEmail
                factura.save flush: true
            }
        }
        log.debug('CFDI: {} enviado a: {}', cfdi.uuid, targetEmail)
        respond command
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
