package sx.cfdi

import com.luxsoft.cfdix.v33.V33PdfGenerator
import grails.gorm.transactions.Transactional
import groovy.xml.XmlUtil
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.grails.core.io.ResourceLocator
import sx.core.AppConfig
import sx.core.Venta
import sx.reports.ReportService

@Transactional
class CfdiService {

    private AppConfig config

    ReportService reportService

    ResourceLocator resourceLocator

    Cfdi generarCfdi(Comprobante comprobante, String tipo) {
        Cfdi cfdi = new Cfdi()
        cfdi.tipoDeComprobante = tipo
        cfdi.fecha = Date.parse( "yyyy-MM-dd'T'HH:mm:ss", comprobante.fecha,)
        cfdi.serie = comprobante.serie
        cfdi.folio = comprobante.folio
        cfdi.emisor = comprobante.emisor.nombre
        cfdi.emisorRfc = comprobante.emisor.getRfc()
        cfdi.receptor = comprobante.receptor.nombre
        cfdi.receptorRfc = comprobante.receptor.rfc
        cfdi.total = comprobante.total
        cfdi.fileName = getFileName(cfdi)
        // cfdi.url = "${getDirPath(cfdi)}/${getFileName(cfdi)}"
        try {
            // save(comprobante, getDirPath(cfdi), cfdi.fileName)
            saveXml(cfdi, CfdiUtils.toXmlByteArray(comprobante))
            cfdi.save failOnError: true, flush:true
            return cfdi
        }catch (Exception ex) {
            ex.printStackTrace()
            return null
        }

    }

    void saveXml(Cfdi cfdi, byte[] data){
        def date = cfdi.fecha
        String year = date[Calendar.YEAR]
        String month = date[Calendar.MONTH]+1
        String day = date[Calendar.DATE]
        def cfdiRootDir = new File(getConfig().cfdiLocation?: "${System.properties['user.home']}/cfdis")
        final FileTreeBuilder treeBuilder = new FileTreeBuilder(cfdiRootDir)
        treeBuilder{
            dir(cfdi.emisor){
                dir(year){
                    dir(month){
                        dir(day){
                            File res = file(cfdi.fileName) {
                                setBytes(data)
                            }
                            cfdi.url = res.toURI().toURL()
                        }
                    }
                }
            }
        }
    }

    def save(Comprobante comprobante, String dirPath, String fileName){

        File dir = new File(dirPath)
        if(!dir.exists()) {
            dir.mkdirs()
        }
        byte[] data = CfdiUtils.toXmlByteArray(comprobante)
        File xmlFile = new File(dir, fileName);
        FileOutputStream os = new FileOutputStream(xmlFile, false)
        os.write(data)
        os.flush()
        os.close()
    }


    private getDirPath(Cfdi cfdi) {
        String dirPath = "${getConfig().cfdiLocation}/${cfdi.emisor}/${cfdi.fecha.format('YYYY')}/${cfdi.fecha.format('MM')}"
        return dirPath
    }

    private getFileName(Cfdi cfdi){
        String name = "${cfdi.receptorRfc}-${cfdi.serie}-${cfdi.folio}.xml"
        return name
    }


    AppConfig getConfig() {
        /*
        if(!this.config){
            this.config = AppConfig.first()
        }
        return this.config
        */
        return AppConfig.first()
    }

    def cancelar(Cfdi cfdi) {
        if (cfdi.uuid) {
            // Todo Cancelar en el SAT
        } else {
            cfdi.delete flush:true
        }
    }

    def enviar(Cfdi cfdi, String targetEmail) {
        assert !cfdi.cancelado, "CFDI ${cfdi.serie} ${cfdi.folio}  cancelado no se puede enviar por correo"
        assert cfdi.uuid, "El CFDI ${cfdi.serie} ${cfdi.folio} no se ha timbrado"
        if (cfdi.origen == 'VENTA') {
            Venta venta = Venta.where {cuentaPorCobrar.cfdi == cfdi}.find()
            assert venta, "No existe la venta origen del cfdi: ${cfdi.id}"
            String email = targetEmail ?: venta.cliente.getCfdiMail()
            if (!email) {
                cfdi.comentario = "CLIENTE ${venta.cliente.getCfdiMail()} SIN CORREO PARA ENVIO DE EMAIL "
                cfdi.save flush: true
                return cfdi
            }
            return enviarFacturaEmail(cfdi, venta, email)
        }
    }

    def enviarFacturaEmail(Cfdi cfdi, Venta factura, String targetEmail) {
        log.debug('Enviando cfdi {} {} al correo: {}', cfdi.serie,cfdi.folio, targetEmail)

        def xml = cfdi.getUrl().getBytes()
        def pdf = generarImpresionV33(cfdi, true).toByteArray()

        String message = """Apreciable cliente por este medio le hacemos llegar la factura electrónica de su compra. Este correo se envía de manera autmática favor de no responder a la dirección del mismo. Cualquier duda o aclaración 
            la puede dirigir a: servicioaclientes@papelsa.com.mx 
        """

        sendMail {
            multipart false
            from "noreplay@papelsa.com.mx"
            to targetEmail
            // to 'rubencancino6@gmail.com'
            subject "Envio de CFDI Serie: ${cfdi.serie} Folio: ${cfdi.folio}"
            html "<p>${message}</p> "
            attach("${cfdi.serie}-${cfdi.folio}.xml", 'text/xml', xml)
            attach("${cfdi.serie}-${cfdi.folio}.pdf", 'application/pdf', pdf)
        }
        cfdi.enviado = new Date()
        cfdi.email = targetEmail
        cfdi.save flush: true
    }

    private generarImpresionV33( Cfdi cfdi) {
        def realPath = resourceLocator.findResourceForURI('/reports')
        def data = V33PdfGenerator.getReportData(cfdi, true)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }



}
