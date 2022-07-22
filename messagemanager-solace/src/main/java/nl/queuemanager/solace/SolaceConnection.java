package nl.queuemanager.solace;

import com.solacesystems.jms.SolConnection;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSDestination;

import javax.jms.*;
import java.util.Map;

/**
 * This class keeps a Solace connection and a JMS session for a broker. It can be used
 * to store live connections conveniently in a Collection of some sort (only as a value,
 * usage as a key is not advisable due to equals() and hashCode() not being implemented
 * properly).
 */
class SolaceConnection {
	private final MessageVPN messageVpn;
	private final SolConnection connection;
	private final Session syncSession;
	private final Session asyncSession;
	
	/**
	 * Contains message producers for this broker, to prevent recreating them
	 * each time a message needs to be sent.
	 */
	private final Map<JMSDestination, MessageProducer> messageProducers;
	
	public SolaceConnection(MessageVPN messageVpn, SolConnection connection, Session syncSession, Session asyncSession) {
		this.messageVpn = messageVpn;
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
	public MessageVPN getBroker() {
		return messageVpn;
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
