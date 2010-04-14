package nl.queuemanager.ui.message;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.queuemanager.core.Xerces210DocumentWrapper;

import org.w3c.dom.Document;

abstract class XmlContentViewer<T> extends TextAreaContentViewer<T> {

	protected String formatXml(Document document) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			Result result = new StreamResult(sw);
			
			DOMSource source = new DOMSource(Xerces210DocumentWrapper.wrap(document));
			transformer.transform(source, result);
			
			return sw.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			return null;
		} catch (TransformerException e) {
			return null;
		}
	}
	
}
