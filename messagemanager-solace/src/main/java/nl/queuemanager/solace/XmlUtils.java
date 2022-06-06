package nl.queuemanager.solace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

class XmlUtils {
	
	public static Document parse(String xml)  throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(new InputSource(new StringReader(xml)));
	}
	
	public static byte[] serialize(Element ele) throws TransformerException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new DOMSource(ele), new StreamResult(result));
		
		return result.toByteArray();
	}
	
	public static XPathFactory xpathFactory(XPathVariableResolver variableResolver) {
		XPathFactory xpf = XPathFactory.newInstance();
		xpf.setXPathVariableResolver(variableResolver);
		return xpf;
	}
	
}