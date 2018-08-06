package sx.cfdi

import com.luxsoft.cfdix.v33.NotaDeCargoPdfGenerator
import com.luxsoft.cfdix.v33.NotaPdfGenerator
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

import com.luxsoft.cfdix.v33.V33PdfGenerator
import grails.rest.RestfulController
import groovy.transform.ToString
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.core.Cliente
import sx.core.Venta
import sx.cxc.CuentaPorCobrar
import sx.cxc.NotaDeCargo
import sx.cxc.NotaDeCredito
import sx.reports.ReportService


@Secured("hasRole('ROLE_POS_USER')")
class CfdiController extends RestfulController{

    CfdiTimbradoService cfdiTimbradoService

    CfdiService cfdiService;

    CfdiLocationService cfdiLocationService

    ReportService reportService

    CfdiPrintService cfdiPrintService

    CfdiMailService cfdiMailService

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
        log.info('Imprimiendo CFDI: ', params)
        def pdf = cfdiPrintService.getPdf(cfdi)
        render (file: pdf, contentType: 'application/pdf', filename: cfdi.fileName)
    }

    def descargarXml(Cfdi cfdi) {
        def xml = cfdiLocationService.getXml(cfdi)
        File file = File.createTempFile('temp_', 'xml')
        file.setBytes(xml)

        response.setHeader "Content-disposition", "attachment; filename=${cfdi.fileName}"
        response.setHeader("Content-Length", "${file.length()}")
        response.setContentType("text/xml")
        InputStream contentStream = file.newInputStream()
        response.outputStream << contentStream
        webRequest.renderView = false
        // render (file: file, contentType: 'text/xml', filename: cfdi.fileName)
    }

    private generarImpresionV33( Cfdi cfdi) {
        if (cfdi.origen == 'VENTA'){
            return generarPdfFactura()
        } else if(cfdi.origen == 'NOTA_CARGO'){
            return generarPdfNotaDeCargo(cfdi)
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

    private generarPdfNotaDeCargo( Cfdi cfdi) {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        NotaDeCargo cargo = NotaDeCargo.where{cfdi == cfdi}.find()
        def data = NotaDeCargoPdfGenerator.getReportData(cargo)
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
                from 'facturacion@papelsa.mobi'
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

    def envioBatch( EnvioBatchCommand command){
        log.info('Envio batch: {}', params)
        log.info('Command {}', command)
        if (command == null) {
            notFound()
            return
        }
        if (command.hasErrors()) {
            respond command.errors
            return
        }
        List<Cfdi> cfdis = []
        command.facturas.each {
            Cfdi c = Cfdi.get(it)
            if (c.receptorRfc == command.cliente.rfc) {
                cfdis<< c
            }
        }
        cfdiMailService.envioBatch(cfdis, command.target, 'Envio automático');
        // log.debug('Envio batch de facturas {}', command)
        respond 'OK', status:200
    }

}

@ToString(includeNames = true)
public class EnvioBatchCommand {
    Cliente cliente
    List facturas;
    String target

    static constraints = {
        target email: true
    }

}