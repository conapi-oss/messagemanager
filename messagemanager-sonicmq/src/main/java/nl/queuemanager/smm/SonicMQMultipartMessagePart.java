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

import java.util.Enumeration;

import javax.jms.JMSException;

import nl.queuemanager.jms.JMSPart;
import progress.message.jclient.Part;

/**
 * Wraps a SonicMQ specfic multipart message in a JMSMultipartMessage interface to
 * insulate the SonicMQ specifics from the rest of the app.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
class SonicMQMultipartMessagePart implements JMSPart {
	protected final Part delegate;
	
	public SonicMQMultipartMessagePart(Part part) {
		this.delegate = part;
	}

	protected Part getDelegate() {
		return delegate;
	}
	
	public Object getContent() {
		return delegate.getContent();
	}
	
	public byte[] getContentBytes() {
		return delegate.getContentBytes();
	}	

	public String getContentType() {
		return delegate.getHeader().getContentType();
	}

	public String getHeaderField(String name) {
		return delegate.getHeader().getHeaderField(name);
	}

	@SuppressWarnings("unchecked")
	public Enumeration<String> getHeaderFieldNames() {
		return delegate.getHeader().getHeaderFieldNames();
	}

	public void setContent(Object content, String contentType) throws JMSException {
		delegate.setContent(content, contentType);
	}

	public void setHeaderField(String name, String value) throws JMSException {
		delegate.getHeader().setHeaderField(name, value);
	}

}
