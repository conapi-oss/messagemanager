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

import nl.queuemanager.core.Pair;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSMultipartMessage;
import nl.queuemanager.jms.JMSPart;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.jms.impl.DestinationFactory;
import nl.queuemanager.jms.impl.MessageFactory;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class TestMessageListTransferable {

	List<Message> messages;
	
	JMSQueue sampleQ1 = DestinationFactory.createQueue(null, "SampleQ1");
	
	@Before
	public void before() throws JMSException {
		TextMessage m1 = MessageFactory.createTextMessage();
		m1.setText("text");
		
		TextMessage m2 = MessageFactory.createTextMessage();
		m2.setText("text2");
		
		messages = CollectionFactory.newArrayList();
		messages.add(m1);
		messages.add(m2);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFromJMSMessageWithText() throws JMSException, UnsupportedFlavorException, IOException {
		TextMessage msg = MessageFactory.createTextMessage();
		msg.setJMSDestination(sampleQ1);
		msg.setText("some text");
		
		MessageListTransferable t = MessageListTransferable.createFromJMSMessage(sampleQ1, msg);		
		List<Pair<JMSDestination,String>> messageIdList = (List<Pair<JMSDestination,String>>) 
			t.getTransferData(MessageListTransferable.messageIDListDataFlavor);
		assertEquals(sampleQ1, messageIdList.get(0).first());
		assertEquals(msg.getJMSMessageID(), messageIdList.get(0).second());
		
		List<Message> messageList = (List<Message>) t.getTransferData(MessageListTransferable.messageListDataFlavor);
		assertEquals(msg.getJMSMessageID(), messageList.get(0).getJMSMessageID());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCopyFromJMSMessageWithText() throws JMSException, UnsupportedFlavorException, IOException {
		TextMessage message = MessageFactory.createTextMessage();
		message.setJMSDestination(sampleQ1);
		message.setText("some text");
		String messageID = message.getJMSMessageID();
		
		MessageListTransferable t = MessageListTransferable.copyFromJMSMessage(message);

		List<Message> data = (List<Message>) t.getTransferData(MessageListTransferable.messageListDataFlavor);
		assertEquals(messageID, data.get(0).getJMSMessageID());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFromJMSPart() throws Exception {
		JMSMultipartMessage mm = MessageFactory.createMultipartMessage();
		JMSPart srcPart = mm.createPart();
		srcPart.setContent("some text", JMSPart.CONTENT_TEXT);
		
		MessageListTransferable tr = MessageListTransferable.createFromJMSPart(srcPart);
		
		List<Message> data = (List<Message>) tr.getTransferData(MessageListTransferable.messageListDataFlavor);
		assertEquals(1, data.size());
		
		assertEquals("some text", ((TextMessage)data.get(0)).getText());
	}

	@Test
	public void testGetTransferDataInFlavorString() throws UnsupportedFlavorException, IOException, JMSException {
		MessageListTransferable tr = MessageListTransferable.createFromJMSMessageList(null, messages);
		
		Object data = tr.getTransferData(DataFlavor.stringFlavor);
		assertTrue("TransferData is of the wrong type", data instanceof String);

		assertEquals("text\ntext2\n", data);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetTransferDataInFlavorMessageList() throws UnsupportedFlavorException, IOException, JMSException {
		MessageListTransferable tr = MessageListTransferable.createFromJMSMessageList(null, messages);
		
		List<Message> data = (List<Message>)tr.getTransferData(MessageListTransferable.messageListDataFlavor);
		
		for(Message m: data) {
			assertTrue(TextMessage.class.isAssignableFrom(m.getClass()));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTransferDataInFlavorMessageIDList() throws JMSException, UnsupportedFlavorException, IOException {
		List<javax.jms.Message> messages = CollectionFactory.newArrayList();
		
		TextMessage m1 = MessageFactory.createTextMessage();
		m1.setText("message 1");
		messages.add(m1);
		
		TextMessage m2 = MessageFactory.createTextMessage();
		m2.setText("message 2");
		messages.add(m2);
		
		MessageListTransferable t = MessageListTransferable.createFromJMSMessageList(sampleQ1, messages);
		
		List<Pair<JMSQueue,String>> messageIdList = (List<Pair<JMSQueue,String>>) 
			t.getTransferData(MessageListTransferable.messageIDListDataFlavor);
		assertEquals(sampleQ1, messageIdList.get(0).first());
		assertEquals(m1.getJMSMessageID(), messageIdList.get(0).second());
		
		assertEquals(sampleQ1, messageIdList.get(1).first());
		assertEquals(m2.getJMSMessageID(), messageIdList.get(1).second());
	}
	
	@Test
	public void testGetTransferDataFlavors() throws JMSException {
		MessageListTransferable tr = MessageListTransferable.createFromJMSMessageList(null, messages);
		
		assertArrayEquals(
			new DataFlavor[] {
				MessageListTransferable.messageListDataFlavor,
				DataFlavor.stringFlavor,
			}, 
			tr.getTransferDataFlavors());
	}
}
