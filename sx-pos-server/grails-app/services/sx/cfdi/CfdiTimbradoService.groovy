package sx.cfdi


import grails.transaction.Transactional
import org.apache.commons.io.FileUtils
import org.springframework.context.ApplicationContext

import com.luxsoft.utils.ZipUtils

import sx.cfdi.providers.edicom.CFDi
import sx.cfdi.providers.edicom.CancelaResponse
import sx.core.Empresa

/*
import sx.cfdi.providers.cepdi.DatosExtra
import sx.cfdi.providers.cepdi.RespuestaTimbrado
import sx.cfdi.providers.cepdi.WS
*/

/**
 * Prueba de service para timbrado
 * 
 */
@Transactional
class CfdiTimbradoService {

  // WS cerpiService

  CFDi edicomService

  def timbrar(Cfdi cfdi) {
    timbrarEdicom(cfdi)
    return cfdi
  }

  /**
  * Web Service SAOP (wsdl) para los diversos servicios de timbado proporcionados por EDICOM. Actualmente 
  * se requiere habilitar un certificado inadecuado 
  * Utilizando (en Mac o unix): sudo keytool -importcert -alias edicom -file ~/dumps/certificados/caedicom01.cer -keystore cacerts;
  *
  */
  def timbrarEdicom(Cfdi cfdi) {
    File file = FileUtils.toFile(cfdi.url)
    // log.debug 'Timbrando archivo: ' + file.getPath()
    byte[] res = edicomService.getCfdiTest('PAP830101CR3','yqjvqfofb', file.bytes)

    Map map = ZipUtils.descomprimir(res)

    def entry = map.entrySet().iterator().next()
    File target = new File(file.getParent(), file.getName().replaceAll(".xml", "_SIGNED.xml"))
    FileUtils.writeByteArrayToFile(target, entry.getValue())
    CfdiTimbre timbre = new CfdiTimbre(entry.getValue())
    cfdi.uuid = timbre.uuid
    cfdi.url = target.toURI().toURL()
    cfdi.save flush: true
  }
  /*
  def timbrarCerpi() {
    File file = FileUtils.toFile(cfdi.url)
    DatosExtra extra = new DatosExtra()
    RespuestaTimbrado res = cerpiService.timbraXML('clientetimbrado001@mail.com','Demo123#',file.toString(), extra)
    if(res.exitoso) {
      cfdi.uuid = res.UUID
      println 'TFD: ' + res.TFD
      return res.XMLTimbrado
    } else {
      println( 'Error timbrando XML: ' + res.mensajeError + ' Código: ' + res.codigoError)
      return null

    }
  }
  */

  def cancelar(Cfdi cfdi){
    Empresa empresa = Empresa.first()

  }

  def cancelarEdicom(Cfdi cfdi, Empresa empresa) {
    CancelaResponse response = edicomService.cancelaCFDi(
            empresa.usuarioPac,
            empresa.passwordPac,
            [cfdi.uuid],
            empresa.certificadoDigitalPfx,
            empresa.passwordPfx)
  }

}
