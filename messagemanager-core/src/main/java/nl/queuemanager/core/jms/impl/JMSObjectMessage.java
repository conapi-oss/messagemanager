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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * Object message implementation that stores the Serializable object in a byte
 * array. When the object is set, it it serialized and when getObject() is called,
 * the object is revived.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
class JMSObjectMessage extends JMSMessage implements ObjectMessage, Serializable {

	protected byte[] content;
	
	@Override
	public void clearBody() {
		content = null;
	}
	
	/**
	 * Get the object from the message. The object will be unserialized from the message.
	 */
	public Serializable getObject() throws JMSException {
		if(content == null)
			return null;
		
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(content);
			ObjectInputStream oin = new ObjectInputStream(bin);
			return (Serializable)oin.readObject();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		} catch (ClassNotFoundException e) {
			throw new JMSException(e.toString());
		}
	}

	/**
	 * Store the object in the message, the object will be serialized immediately. 
	 */
	public void setObject(Serializable object) throws JMSException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(object);
			oos.flush();
			this.content = bos.toByteArray();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

}
