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
package nl.queuemanager.core.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.MapNamespaceContext;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSTopic;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

/**
 * Preferences utility class. Handles the format and location of the preferences,
 * no details about the preferences implementation should be outside of this class!
 *
 * FIXME The XML handling in here is terrible!
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
class XmlConfiguration implements Configuration {
	private static final String ROOT_ELEMENT = "Configuration";

	private final DocumentBuilderFactory dbf;
	private final DocumentBuilder db;
	
	private final TransformerFactory tff;
	private final Transformer tf;
	
	private final XPathFactory xpf;
	private final XPath xp;
	
	private final File configFile;
	private final String namespaceUri;
	
	@Inject
	XmlConfiguration(File configFile, String namespaceUri) {
		if(configFile == null)
			throw new IllegalArgumentException("configFile");
		
		if(namespaceUri == null)
			throw new IllegalArgumentException("namespaceUri");

		this.configFile = configFile;
		this.namespaceUri = namespaceUri;
		
		// Initialize the XML Parser
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Unable to configure XML parser!");
		}	
		
		// Initialize the XML generator
		tff = TransformerFactory.newInstance();
		try {
			tf = tff.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException("Unable to configure XML generator!");
		}
		
		// Initialize the XPath processor
		xpf = XPathFactory.newInstance();
		xp = xpf.newXPath();
		MapNamespaceContext nsContext = new MapNamespaceContext();
		nsContext.add("c", namespaceUri);
		xp.setNamespaceContext(nsContext);
	}
	
	public synchronized String getUniqueId() {
		String uniqueId = getUserPref(PREF_UNIQUE_ID, null);
		if(uniqueId == null) {
			uniqueId = UUID.randomUUID().toString();
			setUserPref(PREF_UNIQUE_ID, uniqueId);
		}
		return uniqueId;
	}
	
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#getUserPref(java.lang.String, java.lang.String)
	 */
	public String getUserPref(final String key, final String def) {
		try {
			final String res = readConfiguration(new Function<Element, String>() {
				@Override
				public String apply(Element prefs) throws Exception {
					return xp.evaluate(String.format("/c:%s/c:%s", ROOT_ELEMENT, key), prefs);
				}
			});
			
			if(!Strings.isNullOrEmpty(res)) {
				return res;
			}
			
			// No value found for this pref, save the default value
			if(!Strings.isNullOrEmpty(def)) {
				setUserPref(key, def);
			}
			
			return def;
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return def;
		}
	}
		
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#setUserPref(java.lang.String, java.lang.String)
	 */
	public void setUserPref(final String key, final String value) {
		try {
			mutateConfiguration(new Function<Element, Boolean>() {
				@Override
				public Boolean apply(Element prefs) throws Exception {
					setElementValue(prefs, new String[]{key}, value);
					return true;
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public List<JMSBroker> listBrokers() {
		try {
			return readConfiguration(new Function<Element, List<JMSBroker>>() {
				@Override
				public List<JMSBroker> apply(Element prefs) throws Exception {
					List<JMSBroker> brokers = new ArrayList<JMSBroker>();
					
					String expr = String.format("/c:%s/c:Broker", ROOT_ELEMENT);
					NodeList brokerNodes = (NodeList)xp.evaluate(expr, prefs, XPathConstants.NODESET);
					for(int i=0; i< brokerNodes.getLength(); i++) {
						Element brokerElement = (Element) brokerNodes.item(i);
						brokers.add(new JMSBrokerName(brokerElement.getAttribute("name")));
					}
					return brokers;
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#getBrokerPref(nl.queuemanager.core.jms.JMSBroker, java.lang.String, java.lang.String)
	 */
	public String getBrokerPref(final JMSBroker broker, final String key, String def) {
		try {
			final String res = readConfiguration(new Function<Element, String>() {
				@Override
				public String apply(Element prefs) throws Exception {
					final String expr = String.format("/c:%s/c:Broker[@name='%s']/c:%s", 
							ROOT_ELEMENT, broker.toString(), key);
					return (String)xp.evaluate(expr, prefs, XPathConstants.STRING);
				}
			});
			
			if(!Strings.isNullOrEmpty(res)) {
				return res;
			}
			
			// No value found for this pref, save the default value
			if(!Strings.isNullOrEmpty(def)) {
				setBrokerPref(broker, key, def);
			}
			return def;
		} catch(ConfigurationException e) {
			e.printStackTrace();
			return def;
		}
	}
	
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#setBrokerPref(nl.queuemanager.core.jms.JMSBroker, java.lang.String, java.lang.String)
	 */
	public void setBrokerPref(final JMSBroker broker, final String key, final String value) {
		try {
			mutateConfiguration(new Function<Element, Boolean>() {
				@Override
				public Boolean apply(Element prefs) throws Exception {
					setElementValue(getOrCreateBrokerElement(prefs, broker.toString()), new String[]{key}, value);
					return true;
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void setBrokerCredentials(final JMSBroker broker, final Credentials credentials) {
		try {
			mutateConfiguration(new Function<Element, Boolean>() {
				@Override
				public Boolean apply(Element prefs) throws Exception {
					Element brokerElement = getOrCreateBrokerElement(prefs, broker.toString());
					setElementValue(brokerElement, new String[] { "DefaultUsername" }, credentials.getUsername());
					setElementValue(brokerElement, new String[] { "DefaultPassword" }, credentials.getPassword());
					return true;
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public Credentials getBrokerCredentials(JMSBroker broker) {
		// TODO This will cause the file to be opened, locked, read, unlocked and closed twice. Hardly efficient.
		String username = getBrokerPref(broker, "DefaultUsername", null);
		String password = getBrokerPref(broker, "DefaultPassword", null);
		if (username != null && password != null)
			return new Credentials(username, password);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#getTopicSubscriberNames(nl.queuemanager.core.jms.JMSBroker)
	 */
	public List<String> getTopicSubscriberNames(final JMSBroker broker) {
		try {
			return readConfiguration(new Function<Element, List<String>>() {
				@Override
				public List<String> apply(Element prefs) throws Exception {
					String expr = String.format("/c:%s/c:Broker[@name='%s']/c:Subscribers/c:Subscriber", 
							ROOT_ELEMENT, broker.toString());
					return getNodeValues(prefs, expr);
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#getTopicPublisherNames(nl.queuemanager.core.jms.JMSBroker)
	 */
	public List<String> getTopicPublisherNames(final JMSBroker broker) {
		try {
			return readConfiguration(new Function<Element, List<String>>() {
				@Override
				public List<String> apply(Element prefs) throws Exception {
					String expr = String.format("/c:%s/c:Broker[@name='%s']/c:Publishers/c:Publisher", 
							ROOT_ELEMENT, broker.toString());
					return getNodeValues(prefs, expr);
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#addTopicSubscriber(nl.queuemanager.core.jms.JMSTopic)
	 */
	public void addTopicSubscriber(final JMSTopic topic) {
		try {
			mutateConfiguration(new Function<Element, Boolean>() {
				@Override
				public Boolean apply(Element prefs) throws Exception {
					Node brokerElement = getOrCreateBrokerElement(prefs, topic.getBroker().toString()); 
					Node subscribersElement = getOrCreateElementAtPath(brokerElement, new String[] {"Subscribers"});
					
					// Check to see if this topic is already saved. Ignore if it already exists.
					for(Node child = subscribersElement.getFirstChild(); child != null; child = child.getNextSibling()) {
						if(child.getNodeType() == Node.ELEMENT_NODE && topic.getName().equals(child.getTextContent())) {
							return false;
						}
					}
					
					addElement(subscribersElement, "Subscriber", topic.getName());
					return true;
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#addTopicPublisher(nl.queuemanager.core.jms.JMSTopic)
	 */
	public void addTopicPublisher(final JMSTopic topic) {
		try {
			mutateConfiguration(new Function<Element, Boolean>() {
				@Override
				public Boolean apply(Element prefs) throws Exception {
					Node brokerElement = getOrCreateBrokerElement(prefs, topic.getBroker().toString()); 
					Node publishersElement = getOrCreateElementAtPath(brokerElement, new String[] {"Publishers"});
					
					// Check to see if this topic is already saved. Ignore if it already exists.
					for(Node child = publishersElement.getFirstChild(); child != null; child = child.getNextSibling()) {
						if(child.getNodeType() == Node.ELEMENT_NODE && topic.getName().equals(child.getTextContent())) {
							return false;
						}
					}
					
					addElement(publishersElement, "Publisher", topic.getName());
					return true;
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove a topic subscriber from the saved list for its associated broker.
	 * 
	 * @param topic
	 */
	public void removeTopicSubscriber(JMSTopic topic) {
		removePrefNode(String.format(
			"/c:%s/c:Broker[@name='%s']/c:Subscribers/c:Subscriber[text()='%s']", 
			ROOT_ELEMENT, topic.getBroker().toString(), topic.getName()));
	}
	
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#removeTopicPublisher(nl.queuemanager.core.jms.JMSTopic)
	 */
	public void removeTopicPublisher(JMSTopic topic) {
		removePrefNode(String.format(
			"/c:%s/c:Broker[@name='%s']/c:Publishers/c:Publisher[text()='%s']", 
			ROOT_ELEMENT, topic.getBroker().toString(), topic.getName()));
	}

	/**
	 * Return the values of the matched nodes as a list of strings.
	 * 
	 * @param context
	 * @param xpathExpression
	 * @return
	 * @throws XPathExpressionException 
	 */
	private List<String> getNodeValues(Node context, String xpathExpression) throws XPathExpressionException {
		List<String> result = CollectionFactory.newArrayList();
		NodeList resultNodes = (NodeList)xp.evaluate(xpathExpression, context, XPathConstants.NODESET);
		if(resultNodes != null) {
			for(int i = 0; i<resultNodes.getLength(); i++)
				result.add(resultNodes.item(i).getTextContent());
		}
		return result;
	}

	/**
	 * Set the value of the Element indicated by the path, creating Elements if required.
	 * 
	 * @param path
	 * @param value
	 * @throws XPathExpressionException 
	 */
	private void setElementValue(final Node context, final String[] path, final String value) throws XPathExpressionException {
		Node node = getOrCreateElementAtPath(context, path);
		node.setTextContent(value);
	}

	/**
	 * Get or create the Elements at the specified path.
	 * 
	 * @param context
	 * @param path
	 * @return The last element of the path
	 * @throws XPathExpressionException 
	 */
	private Node getOrCreateElementAtPath(final Node context, final String[] path) throws XPathExpressionException {
		Node curNode = context;
		
		for(String cur: path) {
			Node potential = (Node)xp.evaluate("c:" + cur, curNode, XPathConstants.NODE);
			if(potential == null) {
				// Create the node and add to the document. Then use as the current node.
				potential = curNode.getOwnerDocument().createElementNS(namespaceUri, cur);
				curNode.appendChild(potential);
			}
			curNode = potential;
		}
		
		return curNode;
	}
	
	/**
	 * Add an element to the context node with the specified value.
	 * 
	 * @param context
	 * @param nodeName
	 * @param nodeValue
	 */
	private void addElement(final Node context, final String nodeName, final String nodeValue) {
		Node newNode = context.getOwnerDocument().createElementNS(namespaceUri, nodeName);
		newNode.setTextContent(nodeValue);
		context.appendChild(newNode);
	}
	
	/**
	 * Remove a single node from the preferences document if it exists.
	 * 
	 * @param xpathExpression
	 */
	private void removePrefNode(final String xpathExpression) {
		try {
			mutateConfiguration(new Function<Element, Boolean>() {
				@Override
				public Boolean apply(Element prefs) throws Exception {
					Node nodeToRemove = (Node)xp.evaluate(
							xpathExpression, prefs, XPathConstants.NODE);
					if(nodeToRemove != null) {
						nodeToRemove.getParentNode().removeChild(nodeToRemove);
						return true;
					}
					return false;
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the Element that refers to a certain broker. Creating it if it doesn't
	 * exist yet.
	 *  
	 * @param prefs
	 * @param brokerName
	 * @return
	 * @throws XPathExpressionException
	 */
	private Element getOrCreateBrokerElement(Element prefs, String brokerName)
			throws XPathExpressionException {
		Element brokerElement = (Element)xp.evaluate(
				String.format("/c:%s/c:Broker[@name='%s']", ROOT_ELEMENT, brokerName),
				prefs, XPathConstants.NODE);
		if(brokerElement == null) {
			final Document doc = prefs.getOwnerDocument();
			brokerElement = doc.createElementNS(namespaceUri, "Broker");
			Attr nameAttribute = doc.createAttribute("name");
			nameAttribute.setTextContent(brokerName);
			brokerElement.getAttributes().setNamedItem(nameAttribute);
			prefs.appendChild(brokerElement);
		}
		
		return brokerElement;
	}
		
	/**
	 * Create a new, empty, configuration document.
	 * 
	 * @return
	 */
	private Document newConfig() {
		Document d = db.newDocument();
		Element configElement = d.createElementNS(namespaceUri, ROOT_ELEMENT);
		d.appendChild(configElement);
		return d;
	}

	static class JMSBrokerName implements JMSBroker {
		private final String name;

		public JMSBrokerName(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
		
		public int compareTo(JMSBroker other) {
			return name.compareTo(other.toString());
		}
	}

	private final Object lock = new Object();
	protected interface Function<T,R> {
		R apply(T t) throws Exception;
	}
	protected void mutateConfiguration(Function<? super Element, Boolean> mutateFunc) throws ConfigurationException {
		// This lock is to make sure only one thread in this process will access the
		// file at any time.
		synchronized(lock) {
			// Obtain file lock. This is to make sure multiple processes synchronize properly
			try(final FileChannel channel = FileChannel.open(configFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
				final FileLock lock = channel.lock()) {

				Document configuration = readConfiguration(channel);
				Boolean changed = mutateFunc.apply(configuration.getDocumentElement());
				if(changed) {
					writeConfiguration(configuration, channel);
				}
			} catch (IOException e) {
				throw new ConfigurationException(e);
			} catch (Exception e) {
				throw new ConfigurationException(e);
			}
		}
	}
	
	
	protected <R> R readConfiguration(Function<Element, R> readFunc) throws ConfigurationException {
		// This lock is to make sure only one thread in this process will access the
		// file at any time.
		synchronized(lock) {
			try(final FileChannel channel = FileChannel.open(configFile.toPath(), StandardOpenOption.READ)) {

				Document configuration = readConfiguration(channel);
				return readFunc.apply(configuration.getDocumentElement());
			} catch (IOException e) {
				throw new ConfigurationException(e);
			} catch (Exception e) {
				throw new ConfigurationException(e);
			}
		}
	}
	
	private Document readConfiguration(FileChannel channel) {
		try {
			final int fileSize = (int)configFile.length();
			if(fileSize == 0) {
				return newConfig();
			}
			
			final ByteBuffer buffer = ByteBuffer.allocate(fileSize);
			while(channel.read(buffer) > 0);
			
			return db.parse(new InputSource(new ByteArrayInputStream(buffer.array())));
		} catch (IOException e) {
			System.out.println("IOException getting configuration, creating new document." + e);
			e.printStackTrace();
			return newConfig();
		} catch (SAXException e) {
			System.out.println("Unable to parse configuration, creating new document." + e);
			e.printStackTrace();
			return newConfig();
		}
	}
	
	private void writeConfiguration(Document configuration, FileChannel channel) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		StreamResult r = new StreamResult(buffer);
		Source s = new DOMSource(configuration);
		
		try {
			tf.transform(s, r);
			channel.truncate(0);
			channel.write(ByteBuffer.wrap(buffer.toByteArray()));
		} catch (TransformerException e) {
			System.err.println("Error while saving prefs!");
			e.printStackTrace(System.err);
		}
	}
	
}
