package nl.queuemanager.core.jms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;

import com.google.inject.Singleton;

import nl.queuemanager.core.events.AbstractEventSource;
import nl.queuemanager.core.jms.DomainEvent.EVENT;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.jms.JMSTopic;

/**
 * This class is a JMS Domain Facade meant to be passed in to various parts of the application to allow them
 * to be constructed by Guice. The delegate can be set at runtime and this class makes sure to gracefully 
 * handle calls before the delegate is known.
 * 
 * After the delegate is known, this class is responsible for calling the real JMSDomain class and firing events
 * to the rest of the application for various things.
 * 
 * @author gerco
 *
 */
@Singleton
public class JMSDomainFacade extends AbstractEventSource<DomainEvent> implements JMSDomain {

	private JMSDomain delegate;
	
	public void setDelegate(JMSDomain delegate) {
		this.delegate = delegate;
	}
	
	public List<? extends JMSBroker> enumerateBrokers() throws MalformedObjectNameException, JMException {
		List<? extends JMSBroker> brokers;
		
		if(delegate != null) {
			 brokers = delegate.enumerateBrokers();
		} else {
			brokers = new ArrayList<JMSBroker>();
		}
		
		dispatchEvent(new DomainEvent(EVENT.BROKERS_ENUMERATED, brokers, this));
		return brokers;
	}

	public void enumerateQueues(JMSBroker broker, String filter) {
		List<JMSQueue> queues = delegate.getQueueList(broker, filter);
		dispatchEvent(new DomainEvent(EVENT.QUEUES_ENUMERATED, queues, this));
	}

	public List<JMSQueue> getQueueList(JMSBroker broker, String filter) {
		// TODO Auto-generated method stub
		return null;
	}

	public JMSTopic createTopic(JMSBroker broker, String topicName) {
		// TODO Auto-generated method stub
		return null;
	}

	public JMSQueue createQueue(JMSBroker broker, String queueName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<Message> enumerateMessages(JMSQueue queue)
			throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public MessageConsumer openConsumer(JMSDestination destination,
			MessageListener listener) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendMessage(JMSDestination destination, Message messageToSend)
			throws JMSException {
		// TODO Auto-generated method stub

	}

	public void forwardMessage(JMSQueue from, JMSDestination to,
			String messageID) throws JMSException {
		// TODO Auto-generated method stub

	}

	public void deleteMessages(List<JMSQueue> queueList) throws JMSException,
			JMException {
		// TODO Auto-generated method stub

	}

	public void deleteMessages(JMSQueue queue, List<Message> messages)
			throws JMSException {
		// TODO Auto-generated method stub

	}

	public void connectToBroker(JMSBroker aBroker, Credentials credentials)
			throws JMSException {
		// TODO Auto-generated method stub

	}

}
