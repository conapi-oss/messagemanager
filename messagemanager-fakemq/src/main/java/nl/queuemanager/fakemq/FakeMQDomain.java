package nl.queuemanager.fakemq;

import com.google.common.eventbus.EventBus;
import nl.queuemanager.core.events.AbstractEventSource;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.DomainEvent.EVENT;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.jms.JMSFeature;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.jms.JMSTopic;
import nl.queuemanager.jms.impl.DestinationFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Singleton
public class FakeMQDomain extends AbstractEventSource<DomainEvent> implements JMSDomain {

	private final JMSBroker broker = new FakeMQBroker("FakeMQ Broker 1");

	@Inject
	public FakeMQDomain(EventBus eventBus) {
		super(eventBus);
	}
	
	public void connect() {
		dispatchEvent(new DomainEvent(EVENT.JMX_CONNECT, null, this));
	}
	
	public List<? extends JMSBroker> enumerateBrokers() throws MalformedObjectNameException, JMException {
		List<JMSBroker> brokers = new ArrayList<JMSBroker>();
		brokers.add(broker);
		dispatchEvent(new DomainEvent(EVENT.BROKERS_ENUMERATED, brokers, this));
		return brokers;
	}

	public void enumerateQueues(JMSBroker broker, String filter) {
		List<JMSQueue> queues = getQueueList(broker, filter);
		dispatchEvent(new DomainEvent(EVENT.QUEUES_ENUMERATED, queues, this));
	}

	public List<JMSQueue> getQueueList(JMSBroker broker, String filter) {
		List<JMSQueue> queues = new ArrayList<JMSQueue>();
		queues.add(createQueue(broker, "Queue 1", 1));
		queues.add(createQueue(broker, "Queue 2", 2));
		queues.add(createQueue(broker, "Queue 3", 3));
		queues.add(createQueue(broker, "Queue 4", 4));
		queues.add(createQueue(broker, "Queue 5", 5));
		return queues;
	}

	public JMSTopic createTopic(JMSBroker broker, String topicName) {
		return DestinationFactory.createTopic(broker, topicName);
	}

	public JMSQueue createQueue(JMSBroker broker, String queueName) {
		return createQueue(broker, queueName, 0);
	}

	public JMSQueue createQueue(JMSBroker broker, String queueName, int messageCount) {
		return new FakeMQQueue((FakeMQBroker)broker, queueName, messageCount);
	}

	public Enumeration<Message> enumerateMessages(JMSQueue queue) throws JMSException {
		return Collections.enumeration(FakeMQMessageCreator.createRandomMessages(5));
	}

	
	public MessageConsumer openConsumer(JMSDestination destination, MessageListener listener) throws JMSException {
		return new FakeMQMessageConsumer(listener);
	}

	public void sendMessage(JMSDestination destination, Message messageToSend) throws JMSException {
		// Let's not and say we did
		System.out.println("Pretending to send " + messageToSend);
	}

	public void forwardMessage(JMSQueue from, JMSDestination to, String messageID) throws JMSException {
		// Let's not and say we did
	}

	public void deleteMessages(List<JMSQueue> queueList) throws JMSException, JMException {
		// Let's not and say we did
	}

	public void deleteMessages(JMSQueue queue, List<Message> messages) throws JMSException {
		// Let's not and say we did
	}

	public void connectToBroker(JMSBroker aBroker, Credentials credentials) throws JMSException {
		dispatchEvent(new DomainEvent(EVENT.BROKER_CONNECT, null, this));
	}

	public boolean isFeatureSupported(JMSFeature feature) {
		return true;
	}

	@Override
	public Credentials getCredentials(JMSBroker broker, Credentials def, Exception exception) {
		return null;
	}

}
