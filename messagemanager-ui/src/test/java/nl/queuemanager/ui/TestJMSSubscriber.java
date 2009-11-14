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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.jms.Message;

import nl.queuemanager.core.MessageBuffer;
import nl.queuemanager.core.MessageEvent;
import nl.queuemanager.core.jms.JMSDestination;
import nl.queuemanager.core.jms.impl.MessageFactory;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.test.support.SynchronousExecutorService;
import nl.queuemanager.ui.JMSSubscriber;

import org.junit.Before;
import org.junit.Test;

public class TestJMSSubscriber implements Observer {

	private TaskExecutor worker;
	private JMSSubscriber subscriber;
	private JMSDestination destination;
	private MessageBuffer buffer;
	
	private volatile int expectedUpdates;
	private List<MessageEvent.EVENT> expectedEvents;
	
	@Before
	public void before() {
		worker = new TaskExecutor(new SynchronousExecutorService());
		buffer = new MessageBuffer();
		subscriber = new JMSSubscriber(null, worker, destination, buffer);
		subscriber.addObserver(this);
	}
	
	@Test
	public void testReceiveMessage() {
		expectedUpdates++;
		buffer.onMessage(MessageFactory.createMessage());
		
		assertEquals(1, subscriber.getMessageCount());
		
		checkUpdatesReceived();
	}
	
	@Test
	public void testProcessEvent() {
		expectedUpdates++;
		subscriber.processEvent(null);
		checkUpdatesReceived();
	}

	@Test
	public void testRemoveMessages() {
		Message message = MessageFactory.createMessage();
		
		expectedUpdates++;
		buffer.onMessage(message);
		
		assertEquals(1, subscriber.getMessageCount());
		
		buffer.remove(Collections.singletonList(message));
		
		assertEquals(0, subscriber.getMessageCount());
		checkUpdatesReceived();
	}

	@Test
	public void testClear() {
		expectedUpdates += 2;
		buffer.onMessage(MessageFactory.createMessage());
		buffer.onMessage(MessageFactory.createMessage());
		
		assertEquals(2, subscriber.getMessageCount());
		checkUpdatesReceived();
	}
	
	@Test
	public void testLockMessage() {
		Message message = MessageFactory.createMessage();

		buffer.setMaximumNumberOfMessages(3);
		
		expectedUpdates = 9; // 6x MESSAGE_RECEIVED + 3x MESSAGE_DISCARDED
		buffer.onMessage(message);
		buffer.onMessage(MessageFactory.createMessage());
		buffer.onMessage(MessageFactory.createMessage());
		
		assertEquals(3, buffer.getMessageCount());
		
		buffer.lockMessage(message);
		buffer.onMessage(MessageFactory.createMessage());
		buffer.onMessage(MessageFactory.createMessage());
		buffer.onMessage(MessageFactory.createMessage());
		
		assertEquals(3, buffer.getMessageCount());
		assertTrue(buffer.getMessages().contains(message));
		
		checkUpdatesReceived();
	}
	
	@Test
	public void testUnlockMessage() {
		buffer.setMaximumNumberOfMessages(3);
		Message message = MessageFactory.createMessage();
		
		// Send the message to be locked
		expectedUpdates++;
		buffer.onMessage(message);
		buffer.lockMessage(message);
		
		// Fill up the buffer
		expectedUpdates += 4;
		buffer.onMessage(MessageFactory.createMessage());
		buffer.onMessage(MessageFactory.createMessage());
		buffer.onMessage(MessageFactory.createMessage());
		
		assertEquals(3, buffer.getMessageCount());
		
		// Unlock the message and send another to make it disappear;
		expectedUpdates += 2;
		buffer.unlockMessage(message);
		buffer.onMessage(MessageFactory.createMessage());

		assertFalse(buffer.getMessages().contains(message));
		
		checkUpdatesReceived();
	}
	
	@Test
	public void testLockAll() {
		buffer.setMaximumNumberOfMessages(3);
		Message m1 = MessageFactory.createMessage();
		Message m2 = MessageFactory.createMessage();
		Message m3 = MessageFactory.createMessage();
		Message m4 = MessageFactory.createMessage();
		
		expectedUpdates += 3;
		buffer.onMessage(m1);
		buffer.lockMessage(m1);
		buffer.onMessage(m2);
		buffer.lockMessage(m2);
		buffer.onMessage(m3);
		buffer.lockMessage(m3);
		
		assertEquals(3, buffer.getMessageCount());
		
		expectedUpdates++;
		buffer.onMessage(m4);
		
		assertEquals(4, buffer.getMessageCount());		
		
		checkUpdatesReceived();
	}
	
	@Test
	public void testUnlockAll() {
		buffer.setMaximumNumberOfMessages(3);
		Message m1 = MessageFactory.createMessage();
		Message m2 = MessageFactory.createMessage();
		Message m3 = MessageFactory.createMessage();
		Message m4 = MessageFactory.createMessage();
		
		expectedUpdates += 3;
		buffer.onMessage(m1);
		buffer.lockMessage(m1);
		buffer.onMessage(m2);
		buffer.lockMessage(m2);
		buffer.onMessage(m3);
		buffer.lockMessage(m3);
		
		assertEquals(3, buffer.getMessageCount());
		
		expectedUpdates++;
		buffer.onMessage(m4);
		
		assertEquals(4, buffer.getMessageCount());		

		buffer.unlockAll();
		
		expectedUpdates += 3; // One message added, two messages removed
		buffer.onMessage(MessageFactory.createMessage());

		assertEquals(3, buffer.getMessageCount());
		
		checkUpdatesReceived();
	}

	@Test
	public void testGetDestination() {
		assertEquals(destination, subscriber.getDestination());
	}

	public void processEvent(MessageEvent event) {
		if(expectedEvents == null || expectedEvents.size() == 0)
			fail("Received event id " + event.getId() + " while not expecting any");
			
		if(!event.getId().equals(expectedEvents.get(0)))
			fail("Got event id " + event.getId() + " but was expecting event id " + expectedEvents.get(0));
		
		assertTrue("Event info was not a Message!", event.getInfo() instanceof Message);
		
		expectedEvents.remove(0);
	}

	public void checkUpdatesReceived() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue("Did not receive an update, but " + expectedUpdates + " more were expected", expectedUpdates == 0);
	}
	
	public void update(Observable o, Object arg) {
		assertTrue("Was not expecting an update, but " + o + " called notifyObservers()", expectedUpdates > 0);
		expectedUpdates--;
//		System.out.println("Update received, " + expectedUpdates + " more to go");
	}

}
