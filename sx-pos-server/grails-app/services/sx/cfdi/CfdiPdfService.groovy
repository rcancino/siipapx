package sx.cfdi

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import groovy.xml.XmlUtil
import groovy.util.logging.Slf4j

import grails.util.Environment
import grails.gorm.transactions.Transactional
import grails.web.context.ServletContextHolder



import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.grails.core.io.ResourceLocator

import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import lx.cfdi.v33.ine.IneUtils
import com.luxsoft.cfdix.CFDIXUtils
import com.luxsoft.cfdix.v33.V33PdfGenerator
import sx.core.AppConfig
import sx.core.Venta
import sx.reports.ReportService
import sx.cloud.FirebaseService

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;

import java.nio.file.Paths;

@Slf4j
class CfdiPdfService {

    private AppConfig config

    ReportService reportService

    ResourceLocator grailsResourceLocator

    FirebaseService firebaseService

    ByteArrayOutputStream generarPdf( Cfdi cfdi, boolean envio = true) {
        def realPath = grailsResourceLocator.findResourceForURI("/reports").getURI().getPath() ?: 'reports'
        def data = V33PdfGenerator.getReportData(cfdi, envio)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }

    def pushToFireStorage(Cfdi cfdi) {

        String objectName = "cfdis/${cfdi.serie}-${cfdi.folio}.pdf"
        def rawData = this.generarPdf(cfdi)
        def data = rawData.toByteArray()
        
        String projectId = firebaseService.projectId //'siipapx-436ce'
        String bucketName = firebaseService.firebaseBucket // 'siipapx-436ce.appspot.com'
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService()

        BlobId blobId = BlobId.of(bucketName, objectName)
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("application/pdf")
            .setMetadata([size: data.length, uuid: cfdi.uuid, receptorRfc: cfdi.receptorRfc])
            .build()

        storage.create(blobInfo,data)
        log.info('Factura {} publicada en firebase exitosamente', objectName)

    }

    /*
    def generarImpresionV33( Cfdi cfdi) {
        def logoPath = ServletContextHolder.getServletContext().getRealPath("reports/PAPEL_CFDI_LOGO.jpg")
        def data = V33PdfGenerator.getReportData(cfdi, true)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = logoPath
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }
    */
    



}
