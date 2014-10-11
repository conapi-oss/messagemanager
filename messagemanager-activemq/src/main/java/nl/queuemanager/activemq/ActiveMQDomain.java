package nl.queuemanager.activemq;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.events.AbstractEventSource;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.DomainEvent.EVENT;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.jms.JMSTopic;
import nl.queuemanager.jms.impl.DestinationFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.google.common.eventbus.EventBus;

@Singleton
class ActiveMQDomain extends AbstractEventSource<DomainEvent> implements JMSDomain, NotificationListener {

	private final Logger log = Logger.getLogger(getClass().getName());

	private JMXConnector connector;

	private MBeanServerConnection mbeanServer;
	
	private Map<ActiveMQBroker, ActiveMQConnection> brokerConnections;

	private final Configuration config;

	@Inject
	public ActiveMQDomain(Configuration config, EventBus eventBus) {
		super(eventBus);
		this.config = config;
	}
	
	public void connect(String u) throws IOException {
		if(connector != null) {
			disconnect();
		}
		
		JMXServiceURL url = new JMXServiceURL(u);
		connector = JMXConnectorFactory.connect(url);
		mbeanServer = connector.getMBeanServerConnection();
		brokerConnections = CollectionFactory.newHashMap();
		
		dispatchEvent(new DomainEvent(EVENT.JMX_CONNECT, null, this));
	}
	
	public void disconnect() throws IOException {
		mbeanServer = null;
		connector.close();
		connector = null;
		brokerConnections = null;
	}

	@SuppressWarnings("unchecked")
	public List<? extends JMSBroker> enumerateBrokers() throws MalformedObjectNameException, JMException, IOException {
		List<JMSBroker> result = new ArrayList<JMSBroker>();
		
		// List all the activemq brokers in the VM
		Set<ObjectName> names = mbeanServer.queryNames(new ObjectName("org.apache.activemq:type=Broker,brokerName=*"),null);
		for(ObjectName name: names) {
			Map<String, String> transportConnectors = (Map<String, String>) mbeanServer.getAttribute(name, "TransportConnectors");
			for(String type: transportConnectors.keySet()) {
				if(isConnectorTypeSupported(type)) {
					String conn = transportConnectors.get(type);
					try {
						log.fine("Connector: " + conn.getClass().getName() + "; " + conn);
						result.add(new ActiveMQBroker(name, sanitizeConnectorURI(conn)));
					} catch (URISyntaxException e) {
						log.log(Level.FINE, "Could not parse connection URI: " + conn, e);
					}
				}
			}
		}
		
		dispatchEvent(new DomainEvent(EVENT.BROKERS_ENUMERATED, result, this));
		return result;
	}
	
	private URI sanitizeConnectorURI(String conn) throws URISyntaxException {
		URI uri = new URI(conn);
		return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
	}

	private boolean isConnectorTypeSupported(String type) {
		// FIXME Dynamically determine support for connector types (via classpath?)
		return type.equals("openwire");
	}

	public void enumerateQueues(JMSBroker broker, String filter) throws Exception {
		List<JMSQueue> queueList = getQueueList(broker, filter);
		dispatchEvent(new DomainEvent(EVENT.QUEUES_ENUMERATED, queueList, this));
	}

	public List<JMSQueue> getQueueList(JMSBroker broker, String filter) throws Exception {
		ActiveMQBroker b = (ActiveMQBroker)broker;
		List<JMSQueue> queues = new ArrayList<JMSQueue>();

		try {
			Set<ObjectName> names = mbeanServer.queryNames(new ObjectName(b.getObjectName() + ",destinationType=Queue,destinationName=*"), null);
			for(ObjectName name: names) {
				Long queueSize = (Long)mbeanServer.getAttribute(name, "QueueSize");
				queues.add(new ActiveMQQueue(b, name, queueSize));
			}
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
		
		return queues;
	}
	
	public JMSTopic createTopic(JMSBroker broker, String topicName) {
		return DestinationFactory.createTopic(broker, topicName);
	}

	public JMSQueue createQueue(JMSBroker broker, String queueName) {
		return DestinationFactory.createQueue(broker, queueName);
	}

	@SuppressWarnings("unchecked")
	public Enumeration<Message> enumerateMessages(JMSQueue queue) throws Exception {
		QueueBrowser browser = createQueueBrowser((ActiveMQQueue)queue);
		return browser.getEnumeration();
	}

	private QueueBrowser createQueueBrowser(JMSQueue queue) throws JMSException {
		ActiveMQConnection connection = brokerConnections.get(queue.getBroker());
		Session session = connection.getSyncSession();
		
		return session.createBrowser(session.createQueue(queue.getName()));
	}
	
	public MessageConsumer openConsumer(JMSDestination destination, MessageListener listener) throws JMSException {
		return new ActiveMQMessageConsumer();
	}

	public void sendMessage(JMSDestination destination, Message messageToSend) throws JMSException {
		// Let's not and say we did
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
		ActiveMQBroker broker = (ActiveMQBroker)aBroker;
		
		if(brokerConnections.get(broker) == null) {
			connectJMS(broker, credentials);
		}
		
		dispatchEvent(new DomainEvent(EVENT.BROKER_CONNECT, null, this));
	}

	private void connectJMS(ActiveMQBroker broker, Credentials cred) throws JMSException {
		if(brokerConnections.get(broker) != null)
			return;
		
		if(broker == null)
			throw new IllegalArgumentException("Broker must be supplied");
		
		// Try the configuration to get an alternate URL if one is configured.
		String brokerUrl = config.getBrokerPref(
				broker, Configuration.PREF_BROKER_ALTERNATE_URL, broker.getConnectionURI().toString());

		log.info("Connecting to " + brokerUrl);
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
		
		Connection connection;
		if(cred == null) {
			connection = factory.createConnection();
		} else {
			connection = factory.createConnection(cred.getUsername(), cred.getPassword());
		}

		connection.setExceptionListener(new ActiveMQExceptionListener());
		
		Session syncSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		Session asyncSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		
		brokerConnections.put(broker, new ActiveMQConnection(broker, connection, syncSession, asyncSession));
		connection.start();
	}
	
    public void handleNotification(Notification notification,
            Object handback) {
        echo("\nReceived notification:");
        echo("\tClassName: " + notification.getClass().getName());
        echo("\tSource: " + notification.getSource());
        echo("\tType: " + notification.getType());
        echo("\tMessage: " + notification.getMessage());
        if (notification instanceof AttributeChangeNotification) {
            AttributeChangeNotification acn =
                (AttributeChangeNotification) notification;
            echo("\tAttributeName: " + acn.getAttributeName());
            echo("\tAttributeType: " + acn.getAttributeType());
            echo("\tNewValue: " + acn.getNewValue());
            echo("\tOldValue: " + acn.getOldValue());
        }
    }
	
	public void echo(String str) {
		log.info(str);
	}
	
	private static class ActiveMQExceptionListener implements ExceptionListener {
		public void onException(JMSException ex) {
			System.out.println("EXCEPTION CAUGHT!");
			
			// TODO: Disconnect the broker and alert the user. DO NOT make the user lose any work,
			// only notify of the connection being broken.
			System.out.println(ex);
		}
	}
}
