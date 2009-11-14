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
package nl.queuemanager.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

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

import nl.queuemanager.core.jms.JMSBroker;
import nl.queuemanager.core.jms.JMSTopic;
import nl.queuemanager.core.util.CollectionFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Preferences utility class. Handles the format and location of the preferences,
 * no details about the preferences implementation should be outside of this class!
 *
 * FIXME The XML handling in here is terrible!
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class PrefUtils {
	private static final String ROOT_ELEMENT = "Configuration";

	private static final String NAMESPACE_URI = "urn:SonicMessageManagerConfig";
	
	public static final String PREF_BROWSE_DIRECTORY = "browseDirectory"; 
	public static final String PREF_SAVE_DIRECTORY = "saveDirectory";
	public static final String PREF_MAILINGLIST_STATUS = "mailingListStatus";
	public static final String PREF_MAX_BUFFERED_MSG = "maxBufferedMessages";
	public static final String PREF_AUTOREFRESH_INTERVAL = "autoRefreshInterval";
	
	public static final String PREF_BROKER_ALTERNATE_URL = "alternateUrl";
	
	private final DocumentBuilderFactory dbf;
	private final DocumentBuilder db;
	
	private final TransformerFactory tff;
	private final Transformer tf;
	
	private final XPathFactory xpf;
	private final XPath xp;
	
	private static final File prefsFile = new File(System.getProperty("user.home"), ".SonicMessageManager.xml");
	private static final ThreadLocal<PrefUtils> instanceHolder = new ThreadLocal<PrefUtils>();
	
	public static PrefUtils getInstance() {
		PrefUtils instance = instanceHolder.get();
		
		if(instance == null) {
			instance = new PrefUtils();
			instanceHolder.set(instance);
		}
		
		return instance;
	}
	
	private PrefUtils() {
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
		nsContext.add("c", NAMESPACE_URI);
		xp.setNamespaceContext(nsContext);
	}
	
	public String getUserPref(String key, String def) {
		Document prefs = readPrefs();
		String result = "";
		
		try {
			result = xp.evaluate(String.format("/c:%s/c:%s", ROOT_ELEMENT, key), prefs.getDocumentElement());
		} catch (XPathExpressionException e) {
			System.err.println("Unable to get preference key " + key + ":");
			e.printStackTrace();
			return def;
		}
		
		if(result != null && result.length()>0)
			return result;

		// No value for this preference, save the default value
		if(def != null && !"".equals(def))
			setUserPref(key, def);
		return def;
	}
		
	public void setUserPref(String key, String value) {
		Document prefs = readPrefs();
		
		try {
			setElementValue(prefs.getDocumentElement(), new String[]{key}, value);
			savePrefs(prefs);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	public String getBrokerPref(JMSBroker broker, String key, String def) {
		Document prefs = readPrefs();
		
		String expr = String.format("/c:%s/c:Broker[@name='%s']/c:%s", 
				ROOT_ELEMENT, broker.toString(), key);
		try {
			String res = (String)xp.evaluate(expr, prefs.getDocumentElement(), XPathConstants.STRING);
			if(res != null && !"".equals(res))
				return res;
		} catch (XPathExpressionException e) {
			System.out.println(String.format(
					"Unable to retrieve key %s for broker %s", key, broker.toString()));
			e.printStackTrace();
		}

		// No value foud for this pref, save the default value
		if(def != null && !"".equals(def))
			setBrokerPref(broker, key, def);
		return def;
	}
	
	public void setBrokerPref(JMSBroker broker, String key, String value) {
		Document prefs = readPrefs();
		
		try {
			setElementValue(getBrokerElement(prefs, broker.toString()), new String[]{key}, value);
			savePrefs(prefs);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieve the stored list of topic subscribers for a broker
	 * 
	 * @param broker
	 * @return
	 */
	public List<String> getTopicSubscriberNames(JMSBroker broker) {
		Document prefs = readPrefs();
		
		String expr = String.format("/c:%s/c:Broker[@name='%s']/c:Subscribers/c:Subscriber", 
				ROOT_ELEMENT, broker.toString());
		try {
			return getNodeValues(prefs.getDocumentElement(), expr);
		} catch (XPathExpressionException e) {
			System.out.println("Unable to retrieve topic subscriber names for broker " + broker);
			e.printStackTrace();
		}
		
		return CollectionFactory.newArrayList();
	}

	/**
	 * Retrieve the stored list of topic publishers for a broker
	 * 
	 * @param broker
	 * @return
	 */
	public List<String> getTopicPublisherNames(JMSBroker broker) {
		Document prefs = readPrefs();
		
		String expr = String.format("/c:%s/c:Broker[@name='%s']/c:Publishers/c:Publisher", 
				ROOT_ELEMENT, broker.toString());
		try {
			return getNodeValues(prefs.getDocumentElement(), expr);
		} catch (XPathExpressionException e) {
			System.out.println("Unable to retrieve topic publisher names for broker " + broker);
			e.printStackTrace();
		}
		
		return CollectionFactory.newArrayList();
	}
	
	/**
	 * Add a topic subscriber to the list for its broker
	 * 
	 * @param topic
	 */
	public void addTopicSubscriber(JMSTopic topic) {
		if(getTopicSubscriberNames(topic.getBroker()).contains(topic.getName()))
			return;
		
		Document prefs = readPrefs();
		
		try {
			Node brokerElement = getBrokerElement(prefs, topic.getBroker().toString()); 
			Node subscribersElement = getOrCreateElementAtPath(brokerElement, new String[] {"Subscribers"});
			addElement(subscribersElement, "Subscriber", topic.getName());
			
			savePrefs(prefs);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a topic publisher to the list for its broker
	 * 
	 * @param topic
	 */
	public void addTopicPublisher(JMSTopic topic) {
		if(getTopicPublisherNames(topic.getBroker()).contains(topic.getName()))
			return;
		
		Document prefs = readPrefs();
		
		try {
			Node brokerElement = getBrokerElement(prefs, topic.getBroker().toString()); 
			Node subscribersElement = getOrCreateElementAtPath(brokerElement, new String[] {"Publishers"});
			addElement(subscribersElement, "Publisher", topic.getName());
			
			savePrefs(prefs);
		} catch (XPathExpressionException e) {
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
	
	/**
	 * Remove a topic publisher from the saved list for its associated broker.
	 * 
	 * @param topic
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
				potential = curNode.getOwnerDocument().createElementNS(NAMESPACE_URI, cur);
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
		Node newNode = context.getOwnerDocument().createElementNS(NAMESPACE_URI, nodeName);
		newNode.setTextContent(nodeValue);
		context.appendChild(newNode);
	}
	
	/**
	 * Remove a single node from the preferences document if it exists.
	 * 
	 * @param xpathExpression
	 */
	private void removePrefNode(String xpathExpression) {
		Document prefs = readPrefs();

		try {
			Node nodeToRemove = (Node)xp.evaluate(
					xpathExpression, prefs.getDocumentElement(), XPathConstants.NODE);
			if(nodeToRemove != null) {
				nodeToRemove.getParentNode().removeChild(nodeToRemove);
				savePrefs(prefs);
			}
		} catch (XPathExpressionException e) {
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
	private Element getBrokerElement(Document prefs, String brokerName)
			throws XPathExpressionException {
		Element brokerElement = (Element)xp.evaluate(
				String.format("/c:%s/c:Broker[@name='%s']", ROOT_ELEMENT, brokerName),
				prefs.getDocumentElement(), XPathConstants.NODE);
		if(brokerElement == null) {
			brokerElement = prefs.createElementNS(NAMESPACE_URI, "Broker");
			Attr nameAttribute = prefs.createAttribute("name");
			nameAttribute.setTextContent(brokerName);
			brokerElement.getAttributes().setNamedItem(nameAttribute);
			prefs.getDocumentElement().appendChild(brokerElement);
		}
		
		return brokerElement;
	}
	
	/**
	 * Read the existing preference document or create a new one if it doesn't exist yet.
	 * 
	 * @return
	 */
	private Document readPrefs() {		
		if(!prefsFile.exists()) {
			// The file does not exist. Create a new Document.
			return newConfig();
		}
		
		try {
			return db.parse(prefsFile);
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

	/**
	 * Save the Document object to the preferences file.
	 * 
	 * @param doc
	 */
	private void savePrefs(Document doc) {
		try {
			StreamResult r = new StreamResult(prefsFile);
			Source s = new DOMSource(doc);
			
			tf.transform(s, r);
		} catch (TransformerException e) {
			throw new RuntimeException("Error while saving prefs");
		}
	}
	
	/**
	 * Create a new, empty, configuration document.
	 * 
	 * @return
	 */
	private Document newConfig() {
		Document d = db.newDocument();
		Element configElement = d.createElementNS(NAMESPACE_URI, ROOT_ELEMENT);
		d.appendChild(configElement);
		
		// Read possibly existing settings from the java.util.Preferences store and copy them to
		// the configuration document. Then remove them.
		convertOldUserPref(configElement, PREF_BROWSE_DIRECTORY);
		convertOldUserPref(configElement, PREF_SAVE_DIRECTORY);
		convertOldUserPref(configElement, PREF_MAILINGLIST_STATUS);

		savePrefs(d);
		return d;
	}

	/**
	 * Convert an old preference to the new preferences format and remove the old preference.
	 * 
	 * @param configElement
	 * @param key
	 */
	private void convertOldUserPref(final Element configElement, final String key) {
		final String browseDir = getOldUserPref(key);
		if(browseDir != null) {
			Element e = configElement.getOwnerDocument().createElementNS(NAMESPACE_URI, key);
			e.setTextContent(browseDir);
			configElement.appendChild(e);
			removeOldUserPref(key);
		}
	}

	/**
	 * Remove a preference from the java.util.Preferences store.
	 * 
	 * @param key
	 */
	private void removeOldUserPref(final String key) {
		Preferences userNode = Preferences.userNodeForPackage(PrefUtils.class);
		userNode.remove(key);
	}

	/**
	 * Get a preference value from the java.util.Preferences store.
	 * 
	 * @param key
	 * @return
	 */
	private String getOldUserPref(final String key) {
		Preferences userNode = Preferences.userNodeForPackage(PrefUtils.class);
		return userNode.get(key, null);
	}
}
