package sx.cfdi

import com.luxsoft.cfdix.v33.NotaPdfGenerator
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

import com.luxsoft.cfdix.v33.V33PdfGenerator
import grails.rest.RestfulController
import groovy.transform.ToString
import sx.core.Cliente
import sx.cxc.NotaDeCredito
import sx.reports.ReportService


@Secured("hasRole('ROLE_POS_USER')")
class CfdiController extends RestfulController{

    CfdiTimbradoService cfdiTimbradoService

    CfdiService cfdiService;

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
        if (cfdi.tipoDeComprobante == 'I'){
            return generarPdfFactura()
        } else {
            return generarPdfNota(cfdi)
        }

    }

    private generarPdfFactura(Cfdi cfdi){
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def data = V33PdfGenerator.getReportData(cfdi)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }

    private generarPdfNota( Cfdi cfdi) {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        NotaDeCredito nota = NotaDeCredito.where{cfdi == cfdi}.find()
        def data = NotaPdfGenerator.getReportData(nota)
        Map parametros = data['PARAMETROS']
        parametros.LOGO = realPath + '/PAPEL_CFDI_LOGO.jpg'
        return reportService.run('PapelCFDI3Nota.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }

    def enviarEmail(Cfdi cfdi) {

        String targetEmail = params.target
        if(!targetEmail) {
            Cliente cliente = Cliente.where {rfc == cfdi.receptorRfc}.find()
            if (cliente) {
                targetEmail = cliente.getCfdiMail()
            }
        }

        if (targetEmail) {
            String message = """Apreciable cliente por este medio le hacemos llegar un comprobante fiscal digital (CFDI) . Este correo se envía de manera autmática favor de no responder a la dirección del mismo. Cualquier duda o aclaración
                la puede dirigir a: servicioaclientes@papelsa.com.mx
            """
            def xml = cfdi.getUrl().getBytes()
            def pdf = generarImpresionV33(cfdi).toByteArray()
            sendMail {
                multipart false
                to targetEmail
                from 'facturacion@papelsa.com.mx'
                subject "CFDI ${cfdi.serie}-${cfdi.folio}"
                text message
                attach("${cfdi.serie}-${cfdi.folio}.xml", 'text/xml', xml)
                attach("${cfdi.serie}-${cfdi.folio}.pdf", 'application/pdf', pdf)
            }
            cfdi.enviado = new Date()
            cfdi.email = targetEmail
            cfdi.save flush: true
            log.debug('CFDI: {} enviado a: {}', cfdi.uuid, targetEmail)

        }
        respond cfdi
    }

    def envioBatch(){
        EnvioBatchCommand command = new EnvioBatchCommand()
        command.properties = getObjectToBind()
        log.debug('Envio batch de facturas {}', command)
        respond 'OK', status:200
    }

}

@ToString(includeNames = true)
public class EnvioBatchCommand {
    Cliente cliente
    List facturas;
    String email


}
