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

import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;

import nl.queuemanager.core.jms.JMSDestination;
import nl.queuemanager.core.util.CollectionFactory;
import progress.message.jclient.Session;

/**
 * This class keeps a SonicMQ connection and a JMS session for a broker. It can be used
 * to store live connections convieniently in a Collection of some sort (only as a value,
 * usage as a key is not advisable due to equals() and hashCode() not being implemented
 * properly).
 * 
 * @author gerco
 *
 */
class SonicMQConnection {
	private final SonicMQBroker broker;
	private final Connection connection;
	private final Session syncSession;
	private final Session asyncSession;
	
	/**
	 * Contains message producers for this broker, to prevent recreating them
	 * each time a message needs to be sent.
	 */
	private final Map<JMSDestination, MessageProducer> messageProducers;
	
	public SonicMQConnection(SonicMQBroker broker, Connection connection, Session syncSession, Session asyncSession) {
		this.broker = broker;
		this.connection = connection;
		this.syncSession = syncSession;
		this.asyncSession = asyncSession;
		
		this.messageProducers = CollectionFactory.newHashMap();
	}

	/**
	 * Create a synchronous messageconsumer for the specified {@link JMSDestination}. The 
	 * destination can be either a queue or a topic.
	 * 
	 * @param destination
	 * @return
	 * @throws JMSException
	 */
	public MessageConsumer getSyncConsumer(JMSDestination destination) throws JMSException {
		return getConsumer(syncSession, destination);
	}
	
	/**
	 * Create an asynchronous messageconsumer for the specified {@link JMSDestination}. The 
	 * destination can be either a queue or a topic.
	 * 
	 * @param destination
	 * @return
	 * @throws JMSException
	 */
	public MessageConsumer getASyncConsumer(JMSDestination destination) throws JMSException {
		return getConsumer(asyncSession, destination);
	}
	
	private MessageConsumer getConsumer(Session session, JMSDestination destination) throws JMSException {
		switch(destination.getType()) {
		case QUEUE:
			return session.createConsumer(session.createQueue(destination.getName()));
			
		case TOPIC:
			return session.createConsumer(session.createTopic(destination.getName()));
				
		default:
			throw new UnsupportedOperationException("Destination type not supported!");			
		}
	}		
		
	/**
	 * Create a MessageProducer for the given {@link JMSDestination} and store it in the cache.
	 * The destination may be a queue or a topic.
	 * 
	 * @param destination
	 * @return
	 * @throws JMSException
	 */
	public MessageProducer getMessageProducer(JMSDestination destination) throws JMSException {
		if(messageProducers.containsKey(destination))
			return messageProducers.get(destination);
		
		switch(destination.getType()) {
		case QUEUE:
			MessageProducer qmp = syncSession.createProducer(syncSession.createQueue(destination.getName()));
			messageProducers.put(destination, qmp);
			return qmp;
			
		case TOPIC:
			MessageProducer tmp = syncSession.createProducer(syncSession.createTopic(destination.getName()));
			messageProducers.put(destination, tmp);
			return tmp;
			
		default:
			throw new UnsupportedOperationException("Destination type not supported!");
		}		
	}
	
	/**
	 * @return the broker
	 */
	public SonicMQBroker getBroker() {
		return broker;
	}
	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * @return the synchronous session
	 */
	public Session getSyncSession() {
		return syncSession;
	}	
	
	/**
	 * @return the synchronous session
	 */
	public Session getASyncSession() {
		return asyncSession;
	}	
}
