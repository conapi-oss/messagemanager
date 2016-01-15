package nl.queuemanager.solace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;

import nl.queuemanager.core.Configuration;
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

import com.google.common.eventbus.EventBus;

@Singleton
public class SolaceDomain extends AbstractEventSource<DomainEvent> implements JMSDomain {

	private final Logger log = Logger.getLogger(getClass().getName());
	
	private final Configuration config;

	@Inject
	public SolaceDomain(Configuration config, EventBus eventBus) {
		super(eventBus);
		this.config = config;
	}
	
	public void connect(String urlspec, String username, String password) throws IOException {
//		if(connector != null) {
//			disconnect();
//		}

		URL url = new URL(urlspec);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
		con.setDoOutput(true);
		Writer out = new OutputStreamWriter(con.getOutputStream());
		out.write("<rpc semp-version=\"soltr/7_1_1\"><show><message-vpn><vpn-name>*</vpn-name></message-vpn></show></rpc>");
		out.flush();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
		
		dispatchEvent(new DomainEvent(EVENT.JMX_CONNECT, null, this));
	}
	
	public List<? extends JMSBroker> enumerateBrokers() throws MalformedObjectNameException, JMException {
		List<JMSBroker> brokers = new ArrayList<JMSBroker>();
//		brokers.add(broker);
		dispatchEvent(new DomainEvent(EVENT.BROKERS_ENUMERATED, brokers, this));
		return brokers;
	}

	public void enumerateQueues(JMSBroker broker, String filter) {
		List<JMSQueue> queues = getQueueList(broker, filter);
		dispatchEvent(new DomainEvent(EVENT.QUEUES_ENUMERATED, queues, this));
	}

	public List<JMSQueue> getQueueList(JMSBroker broker, String filter) {
		List<JMSQueue> queues = new ArrayList<JMSQueue>();
		queues.add(createQueue(broker, "Queue 1"));
		queues.add(createQueue(broker, "Queue 2"));
		queues.add(createQueue(broker, "Queue 3"));
		queues.add(createQueue(broker, "Queue 4"));
		queues.add(createQueue(broker, "Queue 5"));
		return queues;
	}

	public JMSTopic createTopic(JMSBroker broker, String topicName) {
		return DestinationFactory.createTopic(broker, topicName);
	}

	public JMSQueue createQueue(JMSBroker broker, String queueName) {
		return DestinationFactory.createQueue(broker, queueName);
	}

	public Enumeration<Message> enumerateMessages(JMSQueue queue) throws JMSException {
		return Collections.enumeration(new ArrayList<Message>());
	}

	public MessageConsumer openConsumer(JMSDestination destination, MessageListener listener) throws JMSException {
		return new FakeMQMessageConsumer();
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
		dispatchEvent(new DomainEvent(EVENT.BROKER_CONNECT, null, this));
	}

	public boolean isFeatureSupported(JMSFeature feature) {
		return false;
	}
}
