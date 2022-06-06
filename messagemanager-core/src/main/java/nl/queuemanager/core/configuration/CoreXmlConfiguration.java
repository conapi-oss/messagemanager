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

import com.google.common.base.Strings;
import lombok.extern.java.Log;
import nl.queuemanager.core.MapNamespaceContext;
import nl.queuemanager.core.util.BasicCredentials;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSTopic;
import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Preferences utility class. Handles the format and location of the preferences,
 * no details about the preferences implementation should be outside of this class!
 *
 * FIXME The XML handling in here is terrible!
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@Log
class CoreXmlConfiguration extends XmlFileConfiguration implements CoreConfiguration {
	private final XPathFactory xpf;
	private final XPath xp;
	
	CoreXmlConfiguration(File configFile, String namespaceUri, String elementName) {
		super(configFile, namespaceUri, elementName);
		
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
		return getValue(key, def);
	}
		
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#setUserPref(java.lang.String, java.lang.String)
	 */
	public void setUserPref(final String key, final String value) {
		setValue(key, value);
	}

	public List<JMSBroker> listBrokers() {
		try {
			return readConfiguration(new Function<Element, List<JMSBroker>>() {
				@Override
				public List<JMSBroker> apply(Element prefs) throws Exception {
					List<JMSBroker> brokers = new ArrayList<JMSBroker>();
					
					NodeList brokerNodes = prefs.getElementsByTagNameNS(namespaceUri, "Broker");
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
							rootElementName, broker.toString(), key);
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
					setElementValue(getOrCreateBrokerElement(prefs, broker.toString()), namespaceUri, key, value);
					return true;
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void setBrokerCredentials(final JMSBroker broker, final Credentials credentials) {
		Configuration brokerSection = sub("Broker", "name", broker.toString());
		brokerSection.del("credentials"); // Delete the existing credentials to prevent mixing properties 
                                          // between different implementations of Credentials interface
		Configuration credentialsSection = brokerSection.sub("credentials", "class", credentials.getClass().getName());
		credentials.saveTo(credentialsSection);
	}

	public Credentials getBrokerCredentials(JMSBroker broker) {
		// Get the credentials object in the broker asked for
		Configuration brokerSection = sub("Broker", "name", broker.toString());
		Configuration credentialsSection = brokerSection.sub("credentials");
		String className = credentialsSection.getAttr("class", null);
		if(!Strings.isNullOrEmpty(className)) {
			// Create the class via reflection and load
			try {
				@SuppressWarnings("unchecked")
				Class<Credentials> clazz = (Class<Credentials>) Class.forName(className);
				Credentials cred = clazz.newInstance();
				return cred.loadFrom(credentialsSection);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				// Unable to instantiat the class. Log an error and confinue with alternate methods of loading
				log.log(Level.WARNING, String.format( "Unable to load credentials for %s", broker), e);
			}
		}
		
		// No class name stored or unable to load that class, try to load old style credentials if they exist
		// TODO This will cause the file to be opened, locked, read, unlocked and closed twice. Hardly efficient.
		String username = getBrokerPref(broker, "DefaultUsername", null);
		String password = getBrokerPref(broker, "DefaultPassword", null);
		if (username != null && password != null)
			return new BasicCredentials(username, password);

		// No credentials found
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
							rootElementName, broker.toString());
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
							rootElementName, broker.toString());
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
					Element brokerElement = getOrCreateBrokerElement(prefs, topic.getBroker().toString()); 
					Element subscribersElement = getOrCreateElement(brokerElement, namespaceUri, "Subscribers", null, null);
					
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
					Element brokerElement = getOrCreateBrokerElement(prefs, topic.getBroker().toString()); 
					Element publishersElement = getOrCreateElement(brokerElement, namespaceUri, "Publishers", null, null);
					
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
			rootElementName, topic.getBroker().toString(), topic.getName()));
	}
	
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.ConfigurationManager#removeTopicPublisher(nl.queuemanager.core.jms.JMSTopic)
	 */
	public void removeTopicPublisher(JMSTopic topic) {
		removePrefNode(String.format(
			"/c:%s/c:Broker[@name='%s']/c:Publishers/c:Publisher[text()='%s']", 
			rootElementName, topic.getBroker().toString(), topic.getName()));
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
				String.format("/c:%s/c:Broker[@name='%s']", rootElementName, brokerName),
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
	
}
