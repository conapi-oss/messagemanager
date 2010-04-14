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
package nl.queuemanager.smm;

import javax.jms.JMSException;

import nl.queuemanager.core.jms.JMSXMLMessage;

import org.w3c.dom.Document;

import progress.message.jclient.XMLMessage;

/**
 * Wraps a SonicMQ specfic XML message in a JMSXMLMessage interface to
 * insulate the SonicMQ specifics from the rest of the app.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
class SonicMQXMLMessage extends SonicMQMessage implements JMSXMLMessage {

	protected SonicMQXMLMessage(XMLMessage message) {
		super(message);
	}
	
	@Override
	XMLMessage getDelegate() {
		return (XMLMessage)delegate;
	}
	
	public Document getDocument() throws JMSException {
		return getDelegate().getDocument();
	}

	public boolean isNamespaceAware() {
		return getDelegate().isNamespaceAware();
	}

	public void setDocument(Document doc) throws JMSException {
		getDelegate().setDocument(doc);
	}

	public void setNamespaceAware(boolean namespaceAware) {
		getDelegate().setNamespaceAware(namespaceAware);
	}

	public String getText() throws JMSException {
		return getDelegate().getText();
	}

	public void setText(String text) throws JMSException {
		getDelegate().setText(text);
	}

}
