package sx.cfdi

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.util.Environment
import groovy.transform.CompileDynamic
import groovy.util.logging.Log4j
import groovy.xml.XmlUtil
import sx.core.Empresa
import wslite.soap.SOAPClient
import wslite.soap.SOAPResponse

@GrailsCompileStatic
@Transactional
@Log4j
class CfdiEdicomService {

    private Empresa empresa

    def timbrar(Cfdi cfdi) {

    }

    SOAPClient client = new SOAPClient("https://cfdiws.sedeb2b.com/EdiwinWS/services/CFDi?wsdl")

    @CompileDynamic
    def getCfdiTest(byte[] data){
        log.debug("Timbrando" )
        Empresa empresa = Empresa.first()
        String url = 'http://cfdi.service.ediwinws.edicom.com'
        SOAPResponse response = client.send(SOAPAction: url, sslTrustAllCerts:true){
            body('xmlns:cfdi': 'http://cfdi.service.ediwinws.edicom.com') {
                getCfdiTest{
                    user(empresa.usuarioPac)
                    password(empresa.passwordPac)
                    file(data.encodeBase64())
                }
            }
        }
        String res = response.getCfdiTestResponse
        return res.decodeBase64()

    }


    Boolean isTimbradoDePrueba() {
        return Environment.current != Environment.PRODUCTION
    }

    Empresa getEmpresa() {
        if(!empresa) {
            empresa= Empresa.first()
        }
        return empresa
    }
}
