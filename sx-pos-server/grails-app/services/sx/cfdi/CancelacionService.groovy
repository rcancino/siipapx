package sx.cfdi

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.bouncycastle.util.encoders.Base64
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
    def cancelar(){
        String url = 'http://cfdi.service.ediwinws.edicom.com'
        String url2 = 'https://cfdiws.sedeb2b.com/EdiwinWS/services/CFDi?wsdl'
        SOAPClient client = new SOAPClient(url2)
    }
}
