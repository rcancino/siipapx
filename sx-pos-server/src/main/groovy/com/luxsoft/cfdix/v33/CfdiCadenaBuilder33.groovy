package com.luxsoft.cfdix.v33

import groovy.util.logging.Slf4j

import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import lx.cfdi.v33.Comprobante
import lx.cfdi.v33.CfdiUtils

@Slf4j
class CfdiCadenaBuilder33 {

    String xsltUrl = "http://www.sat.gob.mx/sitio_internet/cfd/3/cadenaoriginal_3_3/cadenaoriginal_3_3.xslt"

    private Transformer transformer;

    String build(Comprobante comprobante){
        log.debug('Generando cadena original para comprobante {}', comprobante.folio)
        // Build transformer
        Transformer transformer = getTransformer()

        // Source
        StreamSource xmlSource = buildSource(comprobante)

        // Target
        Writer writer = new StringWriter()
        StreamResult target = new StreamResult(writer)

        transformer.transform(xmlSource, target)
        String cadena = writer.toString()
        log.debug('Cadena generada: {}', cadena)
        return cadena
    }

    public Transformer getTransformer() {
        if (!this.transformer) {
            log.debug('Generando javax.xml.transform.Transformer para {}', this.xsltUrl)
            TransformerFactory factory=TransformerFactory.newInstance()
            StreamSource source	= new StreamSource(xsltUrl)
            this.transformer = factory.newTransformer(source)
        }
        return this.transformer;
    }

    StreamSource buildSource(Comprobante comprobante) {
        String xml = CfdiUtils.serialize(comprobante)
        Reader reader = new StringReader(xml)
        return  new StreamSource(reader)
    }


}