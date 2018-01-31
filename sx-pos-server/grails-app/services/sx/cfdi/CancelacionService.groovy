package sx.cfdi

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.slurpersupport.NodeChildren
import groovy.xml.XmlUtil
import org.bouncycastle.util.encoders.Base64
import sx.core.Empresa
import wslite.soap.SOAPClient
import wslite.soap.SOAPResponse

@CompileStatic
class CancelacionService {

    String url = 'http://ec.europa.eu/taxation_customs/vies/services/checkVatService'
    SOAPClient client = new SOAPClient("${url}.wsdl")

    @CompileDynamic
    Boolean validateVat(String memberStateCode, String vatNumberCode) {
        SOAPResponse response = client.send(SOAPAction: url) {
            body('xmlns': 'urn:ec.europa.eu:taxud:vies:services:checkVat:types') {
                checkVat {
                    countryCode(memberStateCode)
                    vatNumber(vatNumberCode)
                }
            }
        }
        response.checkVatResponse.valid.text() == 'true'
        // Base64.encode()
    }

    @CompileDynamic
    def cancelar(Cfdi cfdi){
        Empresa empresa = Empresa.first()
        String url = 'http://cfdi.service.ediwinws.edicom.com'
        String url2 = 'https://cfdiws.sedeb2b.com/EdiwinWS/services/CFDi?wsdl'
        SOAPClient client = new SOAPClient("https://cfdiws.sedeb2b.com/EdiwinWS/services/CFDi?wsdl")

        SOAPResponse response = client.send(SOAPAction: url){
            body('xmlns:cfdi': 'http://cfdi.service.ediwinws.edicom.com') {
                cancelaCFDi {
                    user(empresa.usuarioPac)
                    password(empresa.passwordPac)
                    rfc(cfdi.emisorRfc)
                    uuid(cfdi.uuid)
                    pfx(empresa.getCertificadoDigitalPfx().encodeBase64())
                    PfxPassword('pfxfilepapel')
                }
            }
        }


        def res = response.cancelaCFDiResponse
        def xml = new XmlSlurper().parseText(XmlUtil.serialize(res))
        String xmlString = new String(xml.cancelaCFDiReturn.ack.decodeBase64())
        xmlString = XmlUtil.serialize(xmlString)
        def xml2 = new XmlSlurper().parseText(xmlString)
        def folios = xml2.breadthFirst().find { it.name() == 'Folios'}
        if (folios ) {
            // println 'UUID: ' +folios.UUID
            // println 'Status: ' + folios.EstatusUUID
            log.debug('Cancelacion de cfdi: {} status: {}' , folios.UUID, folios.EstatusUUID)
            CfdiCancelado cancelacion = new CfdiCancelado()
            cancelacion.cfdi = cfdi
            cancelacion.uuid = cfdi.uuid
            cancelacion.serie = cfdi.serie
            cancelacion.folio = cfdi.folio
            cancelacion.message = 'CANCELACION '
            cancelacion.aka = xmlString.bytes
            cancelacion.statusSat = folios.EstatusUUID

            cancelacion.save failOnError: true, flush: true

            cfdi.cancelado = true
            cfdi.status = 'CANCELADO'
            cfdi.save flush: true
            cancelacion.save failOnError: true, flush: true
        }
    }


}
