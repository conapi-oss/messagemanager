package nl.queuemanager.solace;

import com.google.common.eventbus.EventBus;
import com.solacesystems.jcsmp.CapabilityType;
import com.solacesystems.jcsmp.InvalidOperationException;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jms.SolConnection;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.SupportedProperty;
import com.solacesystems.jms.impl.ConnectionProperties;
import lombok.Getter;
import lombok.extern.java.Log;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import javax.jms.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.*;
import java.util.logging.Level;

@Singleton @Log
class SolaceDomain extends AbstractEventSource<DomainEvent> implements JMSDomain {

	private final Map<MessageVPN, SolaceConnection> brokerConnections = new HashMap<>();
	private final Object brokerConnections_lock = new Object();

	@Getter private String routerName;
	
	private SempConnection semp;

	private final Provider<SolaceBrokerCredentialsDialog> credentialsDialogProvider;
	
	@Inject
	public SolaceDomain(EventBus eventBus, Provider<SolaceBrokerCredentialsDialog> credentialsDialogProvider) {
		super(eventBus);
		this.credentialsDialogProvider = credentialsDialogProvider;
	}
	
	public void connect(SempConnection sempConnection) throws SempException {
		this.semp = sempConnection;
		dispatchEvent(new DomainEvent(EVENT.JMX_CONNECT, null, this));
	}
		
	public void disconnect() {
		for(SolaceConnection conn: brokerConnections.values()) {
			try {
				conn.getConnection().close();
			} catch (JMSException e) {
				log.log(Level.WARNING, "Exception while closing JMS connection", e);
			}
		}
		this.brokerConnections.clear();;
	}
	
	@Override
	public boolean isFeatureSupported(JMSFeature feature) {
		switch(feature) {
		case FORWARD_MESSAGE:
			return false;
			
//		case QUEUE_CAPACITY:
		case QUEUE_MESSAGES_SIZE:
			return true;
		
		case QUEUE_CLEAR_MESSAGES:
			// We can only clear messages over HTTP for some reason
			return semp != null && semp instanceof HttpSEMPConnection;
			
		default:
			return false;
		}
	}

	@Override
	/**
	 * Enumerate all Message VPNs
	 */
	public List<? extends JMSBroker> enumerateBrokers() throws Exception {
		assert semp != null;
		
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final List<MessageVPN> result = new ArrayList<MessageVPN>();
		final SempResponseCallback callback = new SempResponseCallback() {
			@Override
			public void handle(Document doc) throws Exception {
				NodeList vpns = (NodeList)xpath.evaluate("/rpc-reply/rpc/show/message-vpn/vpn[enabled='true']", doc, XPathConstants.NODESET);
				for(int i=0; i<vpns.getLength(); i++) {
					// TODO Find out if this user can access the VPN and hide it if not
					Element vpn = (Element)vpns.item(i);
					String name = xpath.evaluate("name", vpn);
					result.add(new MessageVPN(name, semp.getSmfConnectionDescriptor().createSmfUri()));
				}
			}
		};
		
		// Get all enabled message VPNs
		try {
			semp.showMessageVPN(null, callback);
		} catch (SempException e) {
			if(e.getResponseCode() == 403) {
				// If this is a permission error, retry the request on only our own message vpn
				semp.showMessageVPN(semp.getSmfConnectionDescriptor().getMessageVpn(), callback);
			} else {
				// Otherwise just rethrow the error
				throw e;
			}
		}
		
		dispatchEvent(new DomainEvent(EVENT.BROKERS_ENUMERATED, result, this));
		return result;
	}
	
	@Override
	/**
	 * List all queues in the Message VPN
	 */
	public void enumerateQueues(JMSBroker broker, String filter) throws Exception {
		List<JMSQueue> queueList = getQueueList(broker, filter);
		dispatchEvent(new DomainEvent(EVENT.QUEUES_ENUMERATED, queueList, this));
	}

	@Override
	/**
	 * List all queues in the Message VPN
	 */
	public List<JMSQueue> getQueueList(JMSBroker broker, String filter) throws Exception {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final MessageVPN v = (MessageVPN)broker;
		final List<JMSQueue> result = new ArrayList<JMSQueue>();

		// TODO Recognize and somehow indicate to the user when a queue is (partially) shutdown. Clear messages is still allowed when a queue is shut down!
		
		log.fine(String.format("Enumerating queues on %s with filter %s", broker, filter));
		try {
			semp.performShowRequest(SempRequests.showQueues(v.getName()), new SempResponseCallback() {
				@Override
				public void handle(Document doc) throws Exception {
					NodeList queues = (NodeList)xpath.evaluate("/rpc-reply/rpc/show/queue/queues/queue", doc, XPathConstants.NODESET);
					for(int i=0; i<queues.getLength(); i++) {
						Element queue = (Element)queues.item(i);
						String name = xpath.evaluate("name", queue);
						long numMessagesSpooled = ((Number)xpath.evaluate("info/num-messages-spooled", queue, XPathConstants.NUMBER)).longValue();
						long spoolUsageBytes = Math.round(((Number)xpath.evaluate("info/current-spool-usage-in-mb", queue, XPathConstants.NUMBER)).doubleValue() * 1024 * 1024);
						boolean ingressShutdown = !parseSolaceStatus(xpath.evaluate("info/ingress-config-status", queue));
						boolean egressShutdown = !parseSolaceStatus(xpath.evaluate("info/egress-config-status", queue));
						result.add(new SolaceQueue(v, name, numMessagesSpooled, spoolUsageBytes,
								ingressShutdown, egressShutdown));
					}
				}
			});
		} catch (SempException e) {
			log.warning(String.format("Unable to enumerate queues for %s: %s", broker, e.getMessage()));
		}
		
		return result;
	}
	
	private boolean parseSolaceStatus(String status) {
		status = status.toLowerCase();
		
		if(status.equals("up")
		|| status.equals("active")
		|| status.equals("primary")
		|| status.equals("1")) {
			return true;
		}
		
		return false;
	}

	@Override
	public JMSTopic createTopic(JMSBroker broker, String topicName) {
		return DestinationFactory.createTopic(broker, topicName);
	}

	@Override
	public JMSQueue createQueue(JMSBroker broker, String queueName) {
		return DestinationFactory.createQueue(broker, queueName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<Message> enumerateMessages(JMSQueue queue) throws Exception {
		// TODO What is I have no permission to read it?
		// TODO What if I have no access to the Message VPN as JMS, get using SEMP?
		// TODO This will throw "503 Service Unavailable" when guaranteed message receive is not enabled
		// TODO "503 Service Unavailable" can also happen if you don't specify a VPN name and try to connect JNDI
		// TODO Requires at least "read-only" permissions on the queue or to be the queue's owner
		// TODO Incorporate -DSolace_JMS_Browser_Timeout_In_MS=1000
		SolaceQueue solqueue = (SolaceQueue)queue;

		// We can only browse is egress is not shut down
		if(!solqueue.isEgressShutdown()) {
			QueueBrowser browser = createQueueBrowser((SolaceQueue)queue);
			return browser.getEnumeration();
		} else {
			// If we cannot browse, return an empty enumeration to prevent the UI from showing the user an error
			return Collections.emptyEnumeration();
		}
		
	}
	
	private QueueBrowser createQueueBrowser(JMSQueue queue) throws JMSException {
		SolaceConnection connection = brokerConnections.get(queue.getBroker());
		Session session = connection.getSyncSession();
		
		return session.createBrowser(session.createQueue(queue.getName()));
	}

	/**
	 * Open an asynchronous consumer for the specified destination.
	 * 
	 * @param destination
	 * @param listener
	 */
	private MessageConsumer openASyncConsumer(JMSDestination destination) throws JMSException {
		SolaceConnection connection = brokerConnections.get(destination.getBroker());
		return connection.getASyncConsumer(destination);
	}

	public MessageConsumer openConsumer(JMSDestination destination, MessageListener listener) throws JMSException {
		MessageConsumer consumer = openASyncConsumer(destination);
		consumer.setMessageListener(listener);
		return consumer;
	}

	@Override
	public void sendMessage(JMSDestination destination, Message messageToSend) throws JMSException {
		SolaceConnection connection = brokerConnections.get(destination.getBroker());
		Session session = connection.getSyncSession();
		
		Message jmsMessage = 
			SolaceMessageConverter.convertMessage(session, messageToSend);
		
		MessageProducer producer = connection.getMessageProducer(destination);
		long timetolive = producer.getTimeToLive();
		if(messageToSend.getJMSExpiration() != 0) {
			timetolive = messageToSend.getJMSExpiration() - messageToSend.getJMSTimestamp();
		}
		
		log.info("Sending message with deliverymode " + jmsMessage.getJMSDeliveryMode());
		
		producer.send(jmsMessage, jmsMessage.getJMSDeliveryMode(), jmsMessage.getJMSPriority(), timetolive);
	}

	@Override
	public void forwardMessage(JMSQueue from, JMSDestination to, String messageID) throws Exception {
		throw new InvalidOperationException("Not implemented");
	}

	@Override
	public void deleteMessages(List<JMSQueue> queueList) throws Exception {
		// TODO What if I have no permission to do this?
		for(JMSQueue queue: queueList) {
			MessageVPN vpn = (MessageVPN)queue.getBroker();
			semp.performAdminRequest(SempRequests.deleteMessages(vpn.getName(), queue.getName()), SempResponseCallback.NULL_HANDLER);
		}
	}

	@Override
	public void deleteMessages(JMSQueue queue, List<Message> messages) throws Exception {
		// TODO What if I only have read-only permission to this queue?
		SolaceConnection connection = brokerConnections.get(queue.getBroker());
		Session session = connection.getSyncSession();
		
		for(final Message m: messages) {
			String selector = "JMSMessageID = '" + m.getJMSMessageID() + "'";
			
			MessageConsumer consumer = session.createConsumer(
					session.createQueue(queue.getName()), 
					selector);

			boolean deleted = false;
			for(int tries=0; tries<3; tries++) {
				Message msg = consumer.receive(500);
				if(msg != null) {
					msg.acknowledge();
					deleted = true;
					break;
				}
			}
			consumer.close();
			if(!deleted) {
				throw new JMSException(String.format(
						"Unable to find message with id %s, it may have been consumed by a different client.",
						m.getJMSMessageID()));
			}
		}
	}

	@Override
	public Credentials getCredentials(JMSBroker broker, Credentials def, Exception exception) {
		log.info(String.format("Requesting credentials to connect to %s", broker));
		SolaceBrokerCredentialsDialog dialog = credentialsDialogProvider.get();
		try {
			return dialog.getCredentials(broker, def, exception);
		} finally {
			dialog.dispose();
		}
	}

	@Override
	public void connectToBroker(JMSBroker aBroker, Credentials credentials) throws JMSException {
		MessageVPN broker = (MessageVPN)aBroker;
		
		if(brokerConnections.get(broker) == null) {
			connectJMS(broker, credentials, semp.getSmfConnectionDescriptor());
		}
		
		dispatchEvent(new DomainEvent(EVENT.BROKER_CONNECT, null, this));
	}

	private void connectJMS(MessageVPN vpn, Credentials cred, SmfConnectionDescriptor descriptor) throws JMSException {
		// FIXME Also need to be able to set other connection properties like SSL Trust Store, etc
		if(vpn == null) {
			throw new IllegalArgumentException("Message VPN must be supplied");
		}
		
		synchronized (brokerConnections_lock) {
			if(brokerConnections.get(vpn) != null)
				return;
						
			// Try the configuration to get an alternate URL if one is configured.
//			String brokerUrl = config.getBrokerPref(
//					broker, CoreConfiguration.PREF_BROKER_ALTERNATE_URL, broker.getConnectionURI().toString());
			
			if(cred == null) {
				cred = descriptor.getCredentials();
			}
			
			log.info("Connecting to " + vpn);
			SolConnectionFactory factory;
			try {
				Hashtable<String, Object> env = new Hashtable<>();
				// Make sure browsers time out quickly to keep the application responsive
				env.put(SupportedProperty.SOLACE_JMS_BROWSER_TIMEOUT_IN_MS, 1000);
				// Allow message manager connections to be easily identified by administrators
				env.put(SupportedProperty.SOLACE_JMS_CLIENT_DESCRIPTION, "Message Manager");
				factory = SolJmsUtility.createConnectionFactory(env);
				
				// We are a "monitoring application" so we want to receive all messages even if
				// they are send with DeliverToOne. If we don't do this, a client with MM is able
				// to disrupt message delivery to a working system (by becoming one of the 
				// subscribers messages are round-robin'ed between)
				factory.setDeliverToOneOverride(true);
				
				// Make sure to use DIRECT transport so messages can be sent on
				// appliances that do not support persistence
				factory.setDirectTransport(true);
				
				descriptor.apply(factory);
				factory.setVPN(vpn.getName());
				cred.apply(factory); // cred may be different from the credentials in the descriptor so allow for overriding
			} catch (Exception e) {
				throw new JMSException(e.toString());
			}
			
			Connection connection = factory.createConnection();
			connection.setExceptionListener(new ExceptionListener());
			
			Session syncSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			Session asyncSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			
			brokerConnections.put(vpn, new SolaceConnection(vpn, connection, syncSession, asyncSession));
			connection.start();
			
			routerName = getRouterNameFromSolConnection(connection);
		}
	}
	
	private String getRouterNameFromSolConnection(Connection connection) {
		if(connection instanceof SolConnection) {
			SolConnection solCon = ((SolConnection)connection);
			ConnectionProperties props = solCon.getProperties();
			JCSMPSession jcsmp = props.getJCSMPSession();
			try {
				return (String)jcsmp.getCapability(CapabilityType.PEER_ROUTER_NAME);
			} catch (JCSMPException e) {
				e.printStackTrace();
			}
		}
		
		return "";
	}
	
	/**
	 * Create a new Session for a particular Message VPN, connecting to it if required. This Session
	 * is not retained by the SolaceDomain class and must be managed separately.
	 * @param vpn
	 * @throws JMSException 
	 * @throws SempException 
	 */
	protected Session createNewSession(SmfConnectionDescriptor descriptor) throws JMSException, SempException {
		// Make sure we are connected to this Message VPN
		MessageVPN vpn = new MessageVPN(descriptor.getMessageVpn(), descriptor.createSmfUri());
		connectJMS(vpn, descriptor.getCredentials(), descriptor);
		
		// Create and return the session
		SolaceConnection conn = brokerConnections.get(vpn);
		return conn.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	private static class ExceptionListener implements javax.jms.ExceptionListener {
		public void onException(JMSException ex) {
			log.log(Level.WARNING, "EXCEPTION CAUGHT!", ex);
			
			// TODO: Disconnect the broker and alert the user. DO NOT make the user lose any work,
			// only notify of the connection being broken.
		}
	}
	
	@Override
	public String toString() {
		if(semp != null) {
			return semp.toString();
		}
		return super.toString();
	}
}
