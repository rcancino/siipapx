package com.luxsoft.cfdix.v33

import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import lx.cfdi.v33.Comprobante
import lx.cfdi.v33.CfdiUtils


class CfdiCadenaBuilder33 {

	String xsltUrl = "http://www.sat.gob.mx/sitio_internet/cfd/3/cadenaoriginal_3_3/cadenaoriginal_3_3.xslt"

	String build(Comprobante comprobante){

		// Build transformer
		TransformerFactory factory=TransformerFactory.newInstance()
		StreamSource source	= new StreamSource(xsltUrl)
		Transformer transformer = factory.newTransformer(source)

		// Source
		String xml = CfdiUtils.serialize(comprobante)
		Reader reader = new StringReader(xml)
		StreamSource xmlSource = new StreamSource(reader)
		// Target
		Writer writer = new StringWriter()
		StreamResult target = new StreamResult(writer)

		transformer.transform(xmlSource, target)
		return writer.toString()

	}

}