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
package nl.queuemanager.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import javax.jms.Message;

import nl.queuemanager.core.MessageBuffer;
import nl.queuemanager.core.MessageEvent;
import nl.queuemanager.core.MessageEvent.EVENT;
import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.impl.MessageFactory;
import nl.queuemanager.core.util.CollectionFactory;

import org.junit.Before;
import org.junit.Test;

public class TestMessageBuffer implements EventListener<MessageEvent> {

	private MessageBuffer buffer;
	private List<MessageEvent.EVENT> expectedEvents;
	
	@Before
	public void setUp() throws Exception {
		buffer = new MessageBuffer();
		buffer.setMaximumNumberOfMessages(5);
		buffer.addListener(this);
		expectedEvents = CollectionFactory.newArrayList();
	}

	@Test
	public void testOnMessage() {
		Message message = MessageFactory.createMessage();
		expectedEvents.add(EVENT.MESSAGE_RECEIVED);		
		buffer.onMessage(message);
		
		assertEquals(1, buffer.getMessageCount());
		assertTrue(buffer.getMessages().contains(message));
	}

	@Test
	public void testGetMessageCount() {
		expectedEvents.add(EVENT.MESSAGE_RECEIVED);		
		buffer.onMessage(MessageFactory.createMessage());
		assertEquals(1, buffer.getMessageCount());
		
		expectedEvents.add(EVENT.MESSAGE_RECEIVED);		
		buffer.onMessage(MessageFactory.createMessage());
		assertEquals(2, buffer.getMessageCount());		
	}

	@Test
	public void testRemove() {
		Message message = MessageFactory.createMessage();
		expectedEvents.add(EVENT.MESSAGE_RECEIVED);		
		buffer.onMessage(message);
		
		assertEquals(1, buffer.getMessageCount());
		assertTrue(buffer.getMessages().contains(message));
		
		expectedEvents.add(EVENT.MESSAGE_DISCARDED);
		buffer.remove(Collections.singletonList(message));
		assertEquals(0, buffer.getMessageCount());
		assertFalse(buffer.getMessages().contains(message));
	}

	@Test
	public void testClear() {
		Message message = MessageFactory.createMessage();
		expectedEvents.add(EVENT.MESSAGE_RECEIVED);		
		buffer.onMessage(message);
		
		assertEquals(1, buffer.getMessageCount());
		
		buffer.clear();
		
		assertEquals(0, buffer.getMessageCount());		
	}

	@Test
	public void testSetMaximumNumberOfMessages() {
		buffer.setMaximumNumberOfMessages(3);
		
		for(int i=0; i<3; i++) {
			expectedEvents.add(EVENT.MESSAGE_RECEIVED);		
			buffer.onMessage(MessageFactory.createMessage());
		}
		
		assertEquals(3, buffer.getMessageCount());		
		
		Message message = MessageFactory.createMessage();
		expectedEvents.add(EVENT.MESSAGE_DISCARDED);		
		expectedEvents.add(EVENT.MESSAGE_RECEIVED);		
		buffer.onMessage(message);
		
		assertEquals(3, buffer.getMessageCount());
		assertTrue(buffer.getMessages().contains(message));
	}

	public void processEvent(MessageEvent event) {
		if(expectedEvents == null || expectedEvents.size() == 0)
			fail("Received event id " + event.getId() + " while not expecting any");
			
		if(!event.getId().equals(expectedEvents.get(0)))
			fail("Got event id " + event.getId() + " but was expecting event id " + expectedEvents.get(0));
		
		assertTrue("Event info was not a Message!", event.getInfo() instanceof Message);
		
		expectedEvents.remove(0);
	}

}
