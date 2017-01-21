package nl.queuemanager.debug;

import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;

import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.jms.JMSFeature;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.jms.JMSTopic;

public class NullDomain implements JMSDomain {

	public boolean isFeatureSupported(JMSFeature feature) {
		return false;
	}
	
	public void addListener(EventListener<DomainEvent> listener) {
	}

	public void removeListener(EventListener<DomainEvent> listener) {
	}

	public List<? extends JMSBroker> enumerateBrokers() throws MalformedObjectNameException, JMException {
		return CollectionFactory.newArrayList();
	}

	public void enumerateQueues(JMSBroker broker, String filter) {
	}

	public List<JMSQueue> getQueueList(JMSBroker broker, String filter) {
		return CollectionFactory.newArrayList();
	}

	public JMSTopic createTopic(JMSBroker broker, String topicName) {
		return null;
	}

	public JMSQueue createQueue(JMSBroker broker, String queueName) {
		return null;
	}

	public Enumeration<Message> enumerateMessages(JMSQueue queue) throws JMSException {
		return null;
	}

	public MessageConsumer openConsumer(JMSDestination destination, MessageListener listener) throws JMSException {
		return null;
	}

	public void sendMessage(JMSDestination destination, Message messageToSend) throws JMSException {
	}

	public void forwardMessage(JMSQueue from, JMSDestination to, String messageID) throws JMSException {
	}

	public void deleteMessages(List<JMSQueue> queueList) throws JMSException, JMException {
	}

	public void deleteMessages(JMSQueue queue, List<Message> messages) throws JMSException {
	}

	public void connectToBroker(JMSBroker aBroker, Credentials credentials) throws JMSException {
	}

	@Override
	public Credentials getCredentials(JMSBroker broker, Credentials def, Exception exception) {
		return def;
	}

}
