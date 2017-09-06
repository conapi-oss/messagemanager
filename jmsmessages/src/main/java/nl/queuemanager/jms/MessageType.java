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
package nl.queuemanager.jms;

public enum MessageType {
	TEXT_MESSAGE("Text-Message", javax.jms.TextMessage.class, ".txt"),
	XML_MESSAGE("XML-Message", nl.queuemanager.jms.JMSXMLMessage.class, ".xml"),
	BYTES_MESSAGE("Bytes-Message", javax.jms.BytesMessage.class, ".bin"),
	MAP_MESSAGE("Map-Message", javax.jms.MapMessage.class, ".map"),
	MULTIPART_MESSAGE("Multipart-Message", nl.queuemanager.jms.JMSMultipartMessage.class, ".multipart"),
	OBJECT_MESSAGE("Object-Message", javax.jms.ObjectMessage.class, ".bin"),
	STREAM_MESSAGE("Stream-Message", javax.jms.StreamMessage.class, ".bin"),
	MESSAGE("Message", javax.jms.Message.class, ".msg");

	private final String displayName;
	private final Class<?> clazz;
	private final String extension;
	
	private MessageType(String realName, Class<? extends javax.jms.Message> clazz, String extension) {
		this.displayName = realName;
		this.clazz = clazz;
		this.extension = extension;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public static MessageType fromString(String str) {
		for(MessageType t: values()) {
			if(t.displayName.equals(str))
				return t;
		}
		
		return null;
	}
	
	public static MessageType fromClass(Class<? extends javax.jms.Message> clazz) {
		for(MessageType t: values()) {
			if(t.clazz.isAssignableFrom(clazz))
				return t;
		}
		
		return null;
	}
}