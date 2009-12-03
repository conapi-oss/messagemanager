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
package nl.queuemanager.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import nl.queuemanager.core.Pair;
import nl.queuemanager.core.jms.JMSDestination;
import nl.queuemanager.core.jms.JMSPart;
import nl.queuemanager.core.jms.JMSQueue;
import nl.queuemanager.core.jms.impl.MessageFactory;
import nl.queuemanager.core.util.CollectionFactory;

/**
 * This class represents a list of messages in transit in a drag-and-drop
 * operation. It can represent a list of message ids, a list of messages
 * and the String content of every part of every message.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 */
public class MessageListTransferable implements Transferable {
	/**
	 * A list of Pair&lt;JMSQueue, String&gt; containing JMSQueues and JMS MessageIDs.
	 */
	public static DataFlavor messageIDListDataFlavor;
	
	/**
	 * A list of javax.jms.Message objects.
	 */
	public static final DataFlavor messageListDataFlavor;
	
	static {
		// Generate a unique MIME type for messageID list so that they will not
		// match when dragging across instances of SMM. A list of message IDs
		// is NOT portable across JVMs
		messageIDListDataFlavor = 
			new DataFlavor(
					"application/x-smm-messageidlist-" + System.currentTimeMillis() + 
					";class=" + ListOfMessageIDs.class.getName(), 
					"List of message IDs");
		
		messageListDataFlavor = 
			new DataFlavor(ListOfMessages.class, "List of Messages");
	}
	
	// Only transmit message IDs within the same JVM, they don't make sense when
	// connected to different environments.
	private transient final List<Pair<JMSQueue, String>> messageIDList;
	private final List<Message> messageList;
	
	private MessageListTransferable(
			List<Pair<JMSQueue, String>> listofIDs,
			List<Message> messageList) {
		this.messageIDList = listofIDs;
		this.messageList = messageList;
	}
	
	/**
	 * Create a list of messageIDs and Messages in the Transferable. This allows the messages
	 * to be moved as well as copied. If the queue parameter is null, the message ids will not
	 * be stored so the messages can only be copied.
	 * 
	 * @param messages
	 * @return
	 * @throws JMSException
	 */
	public static MessageListTransferable createFromJMSMessageList(
			final JMSQueue queue,
			final List<Message> messages) 
	throws JMSException {
		List<Pair<JMSQueue, String>> messageIDs = null;
		List<Message> internalMessages = CollectionFactory.newArrayList();
		
		if(queue != null)
			messageIDs = CollectionFactory.newArrayList();
		
		for(javax.jms.Message m: messages) {
			if(queue != null) {
				messageIDs.add(Pair.create(queue, m.getJMSMessageID()));
			}
			internalMessages.add(MessageFactory.copyMessage(m));
		}
		
		return new MessageListTransferable(messageIDs, internalMessages);
	}
	
	/**
	 * Create from a single {@link javax.jms.Message}.
	 * 
	 * @param jmsMessage
	 * @return
	 * @throws JMSException
	 */
	public static MessageListTransferable createFromJMSMessage(JMSQueue queue, javax.jms.Message jmsMessage) throws JMSException {
		return createFromJMSMessageList(queue, Collections.singletonList(jmsMessage));
	}
	
	/**
	 * Copy a {@link javax.jms.Message} into this transferable. This allows only the transfer of the
	 * message content and does not allow the moving of the message itself.
	 * 
	 * @param jmsMessage
	 * @return
	 * @throws JMSException
	 */
	public static MessageListTransferable copyFromJMSMessage(javax.jms.Message jmsMessage) throws JMSException {
		return createFromJMSMessage(null, jmsMessage);
	}
	
	/**
	 * Copy a List of {@link javax.jms.Message} into this transferable. This allows only the transfer of the
	 * message content and does not allow the moving of the message itself.
	 * 
	 * @param jmsMessage
	 * @return
	 * @throws JMSException
	 */
	public static MessageListTransferable copyFromJMSMessageList(List<javax.jms.Message> messages) throws JMSException {
		return createFromJMSMessageList(null, messages);
	}
	
	/**
	 * Create from a {@link progress.message.jclient.Part} Part of a multipart message. A new message will be created with the
	 * contents of this Part.
	 * 
	 * @param jmsPart
	 * @return
	 * @throws JMSException 
	 */
	public static MessageListTransferable createFromJMSPart(JMSPart jmsPart) throws JMSException {
		final Object content = jmsPart.getContent();
		if(content != null && content instanceof javax.jms.Message) {
			return copyFromJMSMessage((javax.jms.Message)content);
		} else {
			TextMessage message = MessageFactory.createTextMessage();
			message.setText(content != null ? content.toString() : "");
	        
	        return createFromJMSMessage(null, message);
		}
    }

	public Object getTransferData(DataFlavor dataflavor) throws UnsupportedFlavorException, IOException {
		if(!isDataFlavorSupported(dataflavor))
			throw new UnsupportedFlavorException(dataflavor);
		
		if(dataflavor.equals(messageIDListDataFlavor))
			return messageIDList;
		
		if(dataflavor.equals(messageListDataFlavor))
			return messageList;

		if(dataflavor.equals(DataFlavor.stringFlavor)) {
			StringBuffer result = new StringBuffer();
			for(Message m: messageList) {
				try {
					if(TextMessage.class.isAssignableFrom(m.getClass())) {
						result.append(((TextMessage)m).getText());
						result.append('\n');
					}
				} catch (JMSException e) {
					throw new IOException(e.toString());
				}
			}
			
			return result.toString();
		}
			
		return null;
	}

	public DataFlavor[] getTransferDataFlavors() {
		if(messageIDList == null && messageList != null) {
			return new DataFlavor[] {
					messageListDataFlavor,
					DataFlavor.stringFlavor
				};
		} else 
		if(messageIDList != null && messageList == null) {
			return new DataFlavor[] {
					messageIDListDataFlavor
				};
		} else
		if(messageIDList != null && messageList != null) {
			return new DataFlavor[] {
					messageIDListDataFlavor,
					messageListDataFlavor,
					DataFlavor.stringFlavor
				};
		} else {
			return new DataFlavor[]{};
		}
	}

	public boolean isDataFlavorSupported(DataFlavor dataflavor) {
		for(DataFlavor f: getTransferDataFlavors())
			if(f.equals(dataflavor))
				return true;
		
		return false;
	}
	
	private static interface ListOfMessages extends List<Message> {}
	
	private static interface ListOfMessageIDs extends List<Pair<JMSDestination, String>> {}
}
