package sx.cloud

import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper

import org.springframework.beans.factory.annotation.Value

import grails.compiler.GrailsCompileStatic
import grails.util.Environment

import com.mailjet.client.errors.MailjetException
import com.mailjet.client.errors.MailjetSocketTimeoutException
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.MailjetResponse
import com.mailjet.client.ClientOptions
import com.mailjet.client.resource.Emailv31

import org.json.JSONArray
import org.json.JSONObject

import sx.cfdi.Cfdi
import sx.cfdi.MailjetLog
import sx.cfdi.EnvioDeComprobantes
import sx.cfdi.CfdiLocationService
import sx.cfdi.CfdiPrintService
  
@Slf4j
// @GrailsCompileStatic
class MailJetService {

  @Value('${MJ_APIKEY_PUBLIC}')
  String mailJetPublicKey;
 
  @Value('${MJ_APIKEY_PRIVATE}')
  String mailJetPrivateKey;

  @Value('${MJ_DEFAUL_SENDER}')
  String mailJetDefaultSender;

  CfdiLocationService cfdiLocationService
  CfdiPrintService cfdiPrintService

  private MailjetClient client 



  def enviarComprobantes(EnvioDeComprobantes command) {
    if(!command.source) command.source = mailJetDefaultSender 
    String message = buildDefaultMessage(command)
    log.debug('Enviando {} comprobantes a: {} email:{}', command.cfdis.size(), command.nombre, command.target)
    
    JSONArray attachments = buildAttachments(command)
    
    MailjetRequest request = new MailjetRequest(Emailv31.resource)
      .property(Emailv31.MESSAGES, new JSONArray()
                .put(new JSONObject()
                    .put(Emailv31.Message.FROM, new JSONObject()
                        .put("Email", command.source)
                        .put("Name", "Papel S.A. de C.V. (Ventas)"))
                    .put(Emailv31.Message.TO, new JSONArray()
                        .put(new JSONObject()
                            .put("Email", command.target)
                            .put("Name", command.nombre)))
                    .put(Emailv31.Message.SUBJECT, "Comprobantes electrónicos")
                    .put(Emailv31.Message.TEXTPART, message)
                    // .put(Emailv31.Message.HTMLPART, "<h3>Dear passenger 1, welcome to <a href=\"https://www.mailjet.com/\">Mailjet</a>!</h3><br />May the delivery force be with you!")
                    .put(Emailv31.Message.ATTACHMENTS, attachments)));
    
    request.property(Emailv31.SANDBOX_MODE, isSandboxMode())  
    MailjetResponse response = getClient().post(request);
    def status = response.getStatus()
    def data = response.getData()
    log.info('Response Status: {}', status)
    // log.info('Response Data: {}', response.getData())
    def mLog = buildLog(data, command)
    mLog.statusCode = status


    if(status == 200 ){
      log.info('Envio exitoso actualizando cfdis')
      command.cfdis.each {
        
        def cfdi = Cfdi.get(it.toString())
        cfdi.enviado = new Date()
        cfdi.email = command.target
        cfdi.save flush: true
        // emaiLog.save failOnError: true, flush: true
      }
    }
    mLog.save failOnError: true, flush: true
    return mLog
  }

  protected JSONArray buildAttachments(EnvioDeComprobantes command) {
    JSONArray attachments = new JSONArray()
    command.cfdis.each {
      // def pdfEncoded = new URL(pdfPath).getBytes().encodeBase64()
      Cfdi cfdi = Cfdi.get(it)
      Byte[] xml = cfdiLocationService.getXml(cfdi)
      Byte[] pdf = cfdiPrintService.getPdf(cfdi)
      String factura = cfdi.fileName
      def xmlEncoded = xml.encodeBase64()
      def pdfEncoded = pdf.encodeBase64()
        attachments.put(new JSONObject()
          .put("ContentType", "application/xml")
          .put("Filename", "${factura}")
          .put("Base64Content", xmlEncoded)
        )
        .put(new JSONObject()
          .put("ContentType", "application/pdf")
          .put("Filename", "${factura}.pdf")
          .put("Base64Content", pdfEncoded)
        )
    }
    return attachments
  }

  protected buildLog(JSONArray data, EnvioDeComprobantes command) {

    def mailjetLog = new MailjetLog()
    mailjetLog.target = command.target
    mailjetLog.nombre = command.nombre
    mailjetLog.cfdis = command.cfdis

    JsonSlurper slurper = new JsonSlurper()
    String jsonString = data.toString()
    List parsedJson = slurper.parseText(jsonString) 
    Map xmap = parsedJson.get(0)

    log.info('Parsed Json: {}', parsedJson)

    String status = xmap['Status']
    mailjetLog.status = status

    log.info('Estatus {}', status)
    if (status == 'error') {
      def errors = xmap['Errors']
      if(errors) {
        log.info('Errors: {}', errors)
        StringBuffer sb = new StringBuffer()
        errors.each {
          def errorMessage = it['ErrorMessage']
          sb << errorMessage
        }
        mailjetLog.messageErrors = sb.toString().take(255)
      }
    } else if(status =='success') {
      def to = xmap['To']
      if(to) {
        // log.info('MessageHref: {}', to.MessageHref.class, to.MessageUUID, to.MessageID)
        mailjetLog.messageHref = to.MessageHref[0]
        mailjetLog.messageUUID = to.MessageUUID[0]
        mailjetLog.messageID = to.MessageID[0]
      }
    }
    
    log.info('MailLog: {}', mailjetLog)
    return mailjetLog
  }

  


  /* *
  * Datos de prueba:
  * def pdfPath = "https://firebasestorage.googleapis.com/v0/b/siipapx-436ce.appspot.com/o/cfdis%2FTAFACCON-83735.pdf?alt=media&token=9c36de27-25af-45df-8fbb-6ac04f7e0175"
  * def xmlPath = "https://firebasestorage.googleapis.com/v0/b/siipapx-436ce.appspot.com/o/cfdis%2FTAFACCON-83735.xml?alt=media&token=b6023eb6-8176-424e-aee7-4a9f097e100e"
  * URL url = new URL(path);
  * url.getBytes().encodeBase64()
  *
  *
  */
  def enviarCfdi(
      String targetEmail, 
      String factura,
      String pdfPath, 
      String xmlPath,
      String targetName = 'NOMBRE'
      ) throws MailjetException, MailjetSocketTimeoutException {

    String source = this.mailJetDefaultSender
    log.info("Enviando: source: {} targetEmail: {}",source, targetEmail)
    
    String message = """Apreciable cliente por este medio le hacemos llegar la factura electrónica de su compra. 
      Este correo se envía de manera autmática favor de no responder a la dirección del mismo. 
      Cualquier duda o aclaración la puede dirigir a: servicioaclientes@papelsa.com.mx 
            """
    def pdfEncoded = new URL(pdfPath).getBytes().encodeBase64()
    def xmlEncoded = new URL(xmlPath).getBytes().encodeBase64()
    

    MailjetRequest request = new MailjetRequest(Emailv31.resource)
      .property(Emailv31.MESSAGES, new JSONArray()
                .put(new JSONObject()
                    .put(Emailv31.Message.FROM, new JSONObject()
                        .put("Email", source)
                        .put("Name", "Papel S.A. de C.V. (Ventas)"))
                    .put(Emailv31.Message.TO, new JSONArray()
                        .put(new JSONObject()
                            .put("Email", targetEmail)
                            .put("Name", targetName)))
                    .put(Emailv31.Message.SUBJECT, "Envío de comprobantes fiscales")
                    .put(Emailv31.Message.TEXTPART, message)
                    // .put(Emailv31.Message.HTMLPART, "<h3>Dear passenger 1, welcome to <a href=\"https://www.mailjet.com/\">Mailjet</a>!</h3><br />May the delivery force be with you!")
                    .put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                        .put(new JSONObject()
                            .put("ContentType", "application/pdf")
                            .put("Filename", "${factura}.pdf")
                            .put("Base64Content", pdfEncoded))
                        .put(new JSONObject()
                            .put("ContentType", "application/xml")
                            .put("Filename", "${factura}.xml")
                            .put("Base64Content", xmlEncoded))
                        )
                    ));

      MailjetResponse response = getClient().post(request);
      def status = response.getStatus()
      def data = response.getData()
      log.info('Response Status: {}', status)
      log.info('Response Data: {}', response.getData())
      return [status: status, data: data]
  }

  String buildDefaultMessage(EnvioDeComprobantes command) {
    """Apreciable cliente por este medio le hacemos llegar ${command.cfdis.size()} comprobantes fiscales. 
      Este correo se envía de manera autmática favor de no responder a la dirección del mismo. 
      Cualquier duda o aclaración la puede dirigir a: servicioaclientes@papelsa.com.mx 
            """
  }

  MailjetClient getClient() {
    if(client == null) {
      this.client = new MailjetClient(mailJetPublicKey,mailJetPrivateKey,new ClientOptions("v3.1"))
    } 
    return this.client
  }
  
  Boolean isSandboxMode() {
      return Environment.isDevelopmentMode()
  }
  

}


