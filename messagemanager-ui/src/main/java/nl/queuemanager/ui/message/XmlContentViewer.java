package nl.queuemanager.ui.message;

import nl.queuemanager.core.Xerces210DocumentWrapper;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

abstract class XmlContentViewer<T> extends TextAreaContentViewer<T> {

	/**
	 * Ugly flag to make sure we set Xml syntax only if the content is actually Xml.
	 * formatXml() will set this flag to true if formatting succeeded.
	 */
	private boolean isXml = false;
	
	@Override
	public RSyntaxTextArea createTextArea(T object) {
		// Reset the flag since this ContentViewer object will be reused for every message
		isXml = false;

		// Create the textarea, formatting the Xml in the process
		RSyntaxTextArea textArea = super.createTextArea(object);
		
		// At this point, formatXml() will already have been called by super.createTextArea() so 
		// we can rely on the flag being set correctly. Set some flags to make Xml look nice.
		if(isXml) {
			//TODO: make this more flexible
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
			textArea.setCodeFoldingEnabled(true);
		}
		else {
			//assume json/plain text
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
			textArea.setCodeFoldingEnabled(true);
		}
		
		return textArea;
	}
	
	protected String formatXml(Document document) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			Result result = new StreamResult(sw);
			
			DOMSource source = new DOMSource(Xerces210DocumentWrapper.wrap(document));
			transformer.transform(source, result);

			isXml = true;
			
			return sw.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			return null;
		} catch (TransformerException e) {
			return null;
		}
	}
	
}
