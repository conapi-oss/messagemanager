package nl.queuemanager.core.configuration;

import nl.queuemanager.core.configuration.CoreXmlConfiguration.JMSBrokerName;
import nl.queuemanager.core.util.BasicCredentials;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.impl.DestinationFactory;
import org.junit.Before;
import org.junit.Test;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class XmlConfigurationTest {
	
	private static final String NAMESPACE_URI = "urn:test-config"; 
	private CoreXmlConfiguration config;
	private File configFile;
	
	@Before
	public void before() throws IOException {
		configFile = File.createTempFile("mmtest", "xml");
		configFile.deleteOnExit();
		config = new CoreXmlConfiguration(configFile, NAMESPACE_URI, "TestConfiguration");
	}
	
	@Test
	public void testGetUniqueId() {
		String uuid = config.getUniqueId();
		assertNotNull(uuid);
		assertEquals(36, uuid.length());
	}

	@Test
	public void testGetSetUserPref() {
		final String KEY = "someKey";
		final String VALUE = "some value";
		final String DEF = "default value";

		// Check that the default works
		assertEquals(DEF, config.getUserPref(KEY, DEF));
		
		// Now set a value and check the new value
		config.setUserPref(KEY, VALUE);
		assertEquals(VALUE, config.getUserPref(KEY, "default value"));
	}

	@Test
	public void testListBrokers() {
		// Initially, empty list
		assertTrue(config.listBrokers().isEmpty());
		
		// Add a broker and retest
		config.setBrokerPref(new JMSBrokerName("broker1"), "key", "value");
		assertEquals(1, config.listBrokers().size());
		
		// Add another broker and retest
		config.setBrokerPref(new JMSBrokerName("broker2"), "key", "value");
		assertEquals(2, config.listBrokers().size());
		
		// Set another property on an existing broker and retest
		config.setBrokerPref(new JMSBrokerName("broker2"), "key2", "value");
		assertEquals(2, config.listBrokers().size());
	}

	@Test
	public void testGetSetBrokerPref() {
		final String KEY = "someKey";
		final String VALUE = "some value";
		final String DEF = "default value";
		final JMSBroker broker = new JMSBrokerName("some broker");

		// Test default value
		assertEquals(DEF, config.getBrokerPref(broker, KEY, DEF));
		
		// Now set a value and retrieve it
		config.setBrokerPref(broker, KEY, VALUE);
		assertEquals(VALUE, config.getBrokerPref(broker, KEY, DEF));
	}

	@Test
	public void testGetSetBrokerCredentials() {
		final JMSBroker broker = new JMSBrokerName("some broker");
		final BasicCredentials creds = new BasicCredentials("username", "password");
		config.setBrokerCredentials(broker, creds);
		
		final BasicCredentials creds2 = (BasicCredentials)config.getBrokerCredentials(broker);
		assertEquals(creds.getUsername(), creds2.getUsername());
		assertEquals(creds.getPassword(), creds2.getPassword());
		
		final BasicCredentials creds3 = new BasicCredentials("user2", "pass2");
		config.setBrokerCredentials(broker, creds3);
		
		final BasicCredentials creds4 = (BasicCredentials)config.getBrokerCredentials(broker);
		assertEquals(creds3.getUsername(), creds4.getUsername());
		assertEquals(creds3.getPassword(), creds4.getPassword());
	}
	
	@Test
	public void testGetSetCustomBrokerCredentials() {
		final JMSBroker broker = new JMSBrokerName("some broker");
		final SomeCredentials creds = new SomeCredentials();
		config.setBrokerCredentials(broker, creds);
		Credentials creds2 = config.getBrokerCredentials(broker); // Loading this will do assertEquals() and such
		assertEquals(creds, creds2);
	}
	
	@Test
	public void testGetOldStyleBrokerCredentials() {
		final JMSBroker broker = new JMSBrokerName("some broker");
		
		Configuration brokerSection = config.sub("Broker", "name", "some broker");
		brokerSection.setValue("DefaultUsername", "someuser");
		brokerSection.setValue("DefaultPassword", "somepass");
		
		BasicCredentials cred = (BasicCredentials)config.getBrokerCredentials(broker);
		assertEquals(cred.getUsername(), "someuser");
		assertEquals(cred.getPassword(), "somepass");
	}

	@Test
	public void testGetTopicSubscriberNames() {
		final JMSBroker broker1 = new JMSBrokerName("broker1");
		config.addTopicSubscriber(DestinationFactory.createTopic(broker1, "topic1"));
		config.addTopicSubscriber(DestinationFactory.createTopic(broker1, "topic2"));
		config.addTopicSubscriber(DestinationFactory.createTopic(broker1, "topic3"));
		
		final JMSBroker broker2 = new JMSBrokerName("broker2");
		config.addTopicSubscriber(DestinationFactory.createTopic(broker2, "topic4"));
		config.addTopicSubscriber(DestinationFactory.createTopic(broker2, "topic5"));
		config.addTopicSubscriber(DestinationFactory.createTopic(broker2, "topic6"));
		
		List<String> names1 = config.getTopicSubscriberNames(broker1);
		assertEquals(3, names1.size());
		assertTrue(names1.contains("topic1"));
		assertTrue(names1.contains("topic2"));
		assertTrue(names1.contains("topic3"));
		
		List<String> names2 = config.getTopicSubscriberNames(broker2);
		assertEquals(3, names2.size());
		assertTrue(names2.contains("topic4"));
		assertTrue(names2.contains("topic5"));
		assertTrue(names2.contains("topic6"));
	}

	@Test
	public void testGetTopicPublisherNames() {
		final JMSBroker broker1 = new JMSBrokerName("broker1");
		config.addTopicPublisher(DestinationFactory.createTopic(broker1, "topic1"));
		config.addTopicPublisher(DestinationFactory.createTopic(broker1, "topic2"));
		config.addTopicPublisher(DestinationFactory.createTopic(broker1, "topic3"));
		
		final JMSBroker broker2 = new JMSBrokerName("broker2");
		config.addTopicPublisher(DestinationFactory.createTopic(broker2, "topic4"));
		config.addTopicPublisher(DestinationFactory.createTopic(broker2, "topic5"));
		config.addTopicPublisher(DestinationFactory.createTopic(broker2, "topic6"));

		assertContainsAll(config.getTopicPublisherNames(broker1), "topic1", "topic2", "topic3");
		config.removeTopicPublisher(DestinationFactory.createTopic(broker1, "topic2"));
		assertContainsAll(config.getTopicPublisherNames(broker1), "topic1", "topic3");

		assertContainsAll(config.getTopicPublisherNames(broker2), "topic4", "topic5", "topic6");
		config.removeTopicPublisher(DestinationFactory.createTopic(broker2, "topic6"));
		assertContainsAll(config.getTopicPublisherNames(broker2), "topic4", "topic5");
	}
	
	@Test
	public void testSubConfiguration() {
		final Configuration plugin1 = config.sub("plugin1");
		final Configuration plugin2 = config.sub("plugin2");
		
		plugin1.setValue("key1", "value1");
		plugin2.setValue("key2", "value2");
		
		assertEquals("value1", plugin1.getValue("key1", "default1"));
		assertEquals("value2", plugin2.getValue("key2", "default2"));
		assertEquals("default1", plugin1.getValue("doesnotexist", "default1"));
		assertEquals("default2", plugin2.getValue("doesnotexist", "default2"));
		assertEquals("default1", plugin1.getValue("key2", "default1"));
		assertEquals("default2", plugin2.getValue("key1", "default2"));
	}
	
	@Test
	public void testSubSubConfiguration() {
		final Configuration plugin1 = config.sub("plugin1");
		final Configuration plugin2 = config.sub("plugin2");

		final Configuration sub11 = plugin1.sub("sub1");
		final Configuration sub12 = plugin1.sub("sub2");
		final Configuration sub21 = plugin2.sub("sub1");
		final Configuration sub22 = plugin2.sub("sub2");

		sub11.setValue("key1", "value11");
		sub12.setValue("key2", "value12");
		sub21.setValue("key1", "value21");
		sub22.setValue("key2", "value22");
		
		assertEquals("value11", sub11.getValue("key1", "default1"));
		assertEquals("value12", sub12.getValue("key2", "default2"));
		assertEquals("value21", sub21.getValue("key1", "default1"));
		assertEquals("value22", sub22.getValue("key2", "default2"));
	}
	
	private static void assertContainsAll(List<String> list, String... values) {
		assertEquals(values.length, list.size());
		for(String value: values) {
			assertTrue(list.contains(value));
		}
	}
	
	public static class SomeCredentials implements Credentials {
		private final String PRINCIPAL = "principal";
		private final String CREDENTIALS = "credentials";

		@Override
		public void saveTo(Configuration config) {
			config.setValue(PRINCIPAL, PRINCIPAL);
			config.setValue(CREDENTIALS, CREDENTIALS);
		}

		@Override
		public Credentials loadFrom(Configuration config) {
			assertEquals(PRINCIPAL, config.getValue(PRINCIPAL, null));
			assertEquals(CREDENTIALS, config.getValue(CREDENTIALS, null));
			return this;
		}

		@Override
		public void apply(ConnectionFactory cf) throws Exception {
			// No-op
		}

		@Override
		public String getPrincipalName() {
			return PRINCIPAL;
		}
		
		@Override
		public boolean equals(Object other) {
			return other.getClass().equals(getClass());
		}
		
	}

}
