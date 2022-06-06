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
package nl.queuemanager.jms.impl;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.Serializable;

class JMSTextMessage extends JMSMessage implements TextMessage, Serializable {

	private static final long serialVersionUID = -7611977799066950298L;
	
	private String text;
	
	@Override
	public void clearBody() throws JMSException {
		setText(null);
	}

	@Override
	public boolean isBodyAssignableTo(Class c) throws JMSException {
		return String.class.isAssignableFrom(c);
	}

	public String getText() throws JMSException {
		return text;
	}

	public void setText(String text) throws JMSException {
		this.text = text;
	}

}
