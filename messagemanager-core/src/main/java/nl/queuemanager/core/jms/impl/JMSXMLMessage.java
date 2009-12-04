/**

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.queuemanager.core.jms.impl;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.JMSException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.queuemanager.core.Xerces210DocumentWrapper;
import nl.queuemanager.core.util.NullEntityResolver;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JMSXMLMessage extends JMSTextMessage implements nl.queuemanager.core.jms.JMSXMLMessage, Serializable {
	private static final long serialVersionUID = -1561318271681047429L;
	
	protected boolean namespaceAware = false;
	protected transient Document document;
	
	public Document getDocument() throws JMSException {
		if(document != null)
			return document;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(isNamespaceAware());
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new NullEntityResolver());
			document = db.parse(new InputSource(new StringReader(getText())));
			return document;
		} catch (ParserConfigurationException e) {
			throw new JMSException(e.toString());
		} catch (SAXException e) {
			throw new JMSException(e.toString());
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	@Override
	public void clearBody() throws JMSException {
		super.clearBody();
		document = null;
	}

	@Override
	public void setText(String text) throws JMSException {
		document = null;
		super.setText(text);
	}
	
	@Override
	public String getText() throws JMSException {
		if(super.getText() != null)
			return super.getText();
		
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			Result result = new StreamResult(sw);
			
			DOMSource source = new DOMSource(Xerces210DocumentWrapper.wrap(document));
			transformer.transform(source, result);
			
			super.setText(sw.toString());
			return super.getText();
		} catch (TransformerConfigurationException e) {
			throw new JMSException(e.toString());
		} catch (TransformerException e) {
			throw new JMSException(e.toString());
		}
	}

	public boolean isNamespaceAware() {
		return namespaceAware;
	}

	public void setDocument(Document doc) throws JMSException {
		document = doc;
		super.setText(null);
	}

	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}

}
