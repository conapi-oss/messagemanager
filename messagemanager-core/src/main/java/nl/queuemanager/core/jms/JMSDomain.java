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
package nl.queuemanager.core.jms;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.jms.JMSTopic;

/**
 * Interface to a JMS Domain.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public interface JMSDomain {
	
	public boolean isFeatureSupported(JMSFeature feature);
	
	public abstract List<? extends JMSBroker> enumerateBrokers() throws Exception;

	/**
	 * Enumerate the queues on a particular JMS broker. Dispatches a QUEUES_ENUMERATED event
	 * when the queues have been retrieved.
	 * 
	 * @param broker
	 * @param filter
	 * @return
	 * @throws Exception 
	 */
	public abstract void enumerateQueues(JMSBroker broker, String filter) throws Exception;

	/**
	 * Enumerates the queues on a JMSBroker without raising a QUEUES_ENUMERATED event.
	 * 
	 * @param broker
	 * @param filter
	 * @return
	 * @throws IOException 
	 */
	public abstract List<JMSQueue> getQueueList(JMSBroker broker, String filter) throws Exception;

	/**
	 * Create a JMSTopic object for use with this Domain
	 * 
	 * @param broker Must be a broker created by this Domain.
	 * @param topicName
	 * @return
	 */
	public abstract JMSTopic createTopic(JMSBroker broker, String topicName);

	/**
	 * Create a JMSQueue object for use with this Domain
	 * 
	 * @param broker Must be a broker created by this Domain.
	 * @param topicName
	 * @return
	 */
	public abstract JMSQueue createQueue(JMSBroker broker, String queueName);

	/**
	 * Start browsing for messages on a JMSQueue created by this Domain.
	 * 
	 * @param queue
	 * @return Enumeration of Messages
	 * @throws Exception 
	 */
	public abstract Enumeration<Message> enumerateMessages(JMSQueue queue) throws Exception;

	/**
	 * Open a consumer for the specified destination and associate the specified MessageListener.
	 * The consumer must be created on a session that allows for asynchronous delivery.
	 * 
	 * @param destination
	 * @param listener
	 */
	public abstract MessageConsumer openConsumer(JMSDestination destination, MessageListener listener) throws JMSException;

	/**
	 * Send a message to the specified destination. The message will be converted to
	 * the Domains internal message format if required.
	 * 
	 * @param destination
	 * @param messageToSend
	 * @throws JMSException
	 */
	public abstract void sendMessage(JMSDestination destination, javax.jms.Message messageToSend) throws JMSException;

	/**
	 * Forward the message with the specified JMSMessageID from a JMSQueue to
	 * another JMSDestination. The JMSDestination objects are expected to have been
	 * created by this Domain.
	 * 
	 * @param from
	 * @param to
	 * @param messageID
	 * @throws Exception 
	 */
	public abstract void forwardMessage(JMSQueue from, JMSDestination to, String messageID) throws Exception;

	/**
	 * Delete all messages on the given list of queues. All queues must be created by this Domain instance.
	 * 
	 * @param queueList
	 * @throws Exception 
	 */
	public abstract void deleteMessages(List<JMSQueue> queueList) throws Exception;

	/**
	 * Delete the given set of messages from the queue.
	 * 
	 * @param queue
	 * @param messages
	 * @throws Exception 
	 */
	public abstract void deleteMessages(JMSQueue queue, List<Message> messages) throws Exception;
	
	/**
	 * Get credentials for a broker. 
	 * 
	 * @param broker The broker for which credentials are required
	 * @param def The default credentials (if any)
	 * @param exception The exception that was encountered on the previous connection attempt (if any)
	 * @return Credentials object or null when no further connection attempts should be made
	 */
	public abstract Credentials getCredentials(JMSBroker broker, Credentials def, Exception exception);
	
	/**
	 * Connects to a JMSBroker using the specified Credentials and creates a JMS 
	 * Session for that broker. If the credentials parameter is null, will attempt
	 * to use default credentials (if applicable for the implemenattion).
	 * 
	 * @throws JMSException 
	 */
	public abstract void connectToBroker(JMSBroker aBroker, Credentials credentials) throws JMSException;

}