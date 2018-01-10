package sx.cfdi

import com.luxsoft.cfdix.v33.V33PdfGenerator
import grails.gorm.transactions.Transactional
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
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

    def enviarFacturaEmail(Venta factura, String targetEmail) {

        assert factura.cuentaPorCobrar, "La venta ${factura.statusInfo()} no se ha facturado"
        assert factura.cuentaPorCobrar.cfdi.uuid, "La factura ${factura.statusInfo()} no se ha timbrado"

        Cfdi cfdi = factura.cuentaPorCobrar.cfdi

        if (targetEmail) {
            // def xml = cfdi.getUrl().getBytes()
            // def pdf = generarImpresionV33(cfdi)
            sendMail {
                multipart false
                from "facturacion.papelsa@papelsa.com.mx"
                to targetEmail
                subject "Correo de prueba"
                text "Correo de prueba"
                // attach("${cfdi.uuid}.xml", 'text/xml', xml)
                // attach("${cfdi.uuid}.pdf", 'text/xml', pdf)
            }
            cfdi.enviado = new Date()
            cfdi.email = targetEmail
        }
        log.debug('CFDI: {} enviado a: {}', cfdi.uuid, targetEmail)
    }

    private generarImpresionV33( Cfdi cfdi) {
        def realPath = resourceLocator.findResourceForURI('/reports')
        def data = V33PdfGenerator.getReportData(cfdi)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }



}
