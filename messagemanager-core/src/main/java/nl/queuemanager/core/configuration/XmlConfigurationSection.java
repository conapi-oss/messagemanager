package nl.queuemanager.core.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;

public class XmlConfigurationSection implements Configuration {

	/**
	 * The parent XmlConfigurationSection for this object
	 */
	private final XmlConfigurationSection parent;

	/**
	 * The namespace of the element this section represents
	 */
	protected final String namespaceUri;
	
	/**
	 * The root element of this section, will be created in the parent if needed.
	 */
	protected final String rootElementName;
	
	public XmlConfigurationSection(String namespaceUri, String elementName) {
		this(null, namespaceUri, elementName);
	}
	
	public XmlConfigurationSection(XmlConfigurationSection parent, String namespaceUri, String elementName) {
		this.parent = parent;
		this.namespaceUri = namespaceUri;
		this.rootElementName = elementName;
	}

	@Override
	public void setValue(final String key, final String value) {
		try {
			mutateConfiguration(new Function<Element, Boolean>() {
				@Override
				public Boolean apply(Element prefs) throws Exception {
					setElementValue(prefs, namespaceUri, key, value);
					return true;
				}
			});
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getValue(final String key, final String def) {
		try {
			final String res = readConfiguration(new Function<Element, String>() {
				@Override
				public String apply(Element prefs) throws Exception {
					Element element = getFirstChildElementNamed(prefs, namespaceUri, key);
					if(element != null) {
						return element.getTextContent();
					}
					return def;
				}
			});
			
			if(!Strings.isNullOrEmpty(res)) {
				return res;
			}
			
			// No value found for this pref, save the default value
			if(!Strings.isNullOrEmpty(def)) {
				setValue(key, def);
			}
			
			return def;
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return def;
		}
	}

	@Override
	public Configuration sub(String key) {
		return new XmlConfigurationSection(this, namespaceUri, key);
	}
	
	/**
	 * Set the value of the Element, creating it if required.
	 * 
	 * @param path
	 * @param value
	 */
	protected static void setElementValue(final Element context, final String namespaceUri, final String name, final String value) {
		Node node = getOrCreateElement(context, namespaceUri, name);
		node.setTextContent(value);
	}

	/**
	 * Get or create the Elements
	 * 
	 * @param context
	 * @param path
	 * @return The last element of the path
	 */
	protected static Element getOrCreateElement(final Element context, final String namespaceUri, final String name) {
		// Check if the element already exists
		Element existingElement = getFirstChildElementNamed(context, namespaceUri, name);
		if(existingElement != null) {
			return existingElement;
		}
		
		// No existing element was found, so create one
		Element newElement = context.getOwnerDocument().createElementNS(namespaceUri, name);
		context.appendChild(newElement);
		return newElement;
	}
	
	private static Element getFirstChildElementNamed(Element context, String namespaceUri, String name) {
		NodeList children = context.getElementsByTagNameNS(namespaceUri, name);
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i); 
			if(child.getNodeType() == Node.ELEMENT_NODE) {
				return (Element)child;
			}
		}
		
		return null;
	}
	
	@Override
	public List<String> listKeys() {
		List<String> keys = new ArrayList<String>();
		try {
			final NodeList res = readConfiguration(new Function<Element, NodeList>() {
				@Override
				public NodeList apply(Element prefs) throws Exception {
					return prefs.getChildNodes();
				}
			});
			for(int i = 1; i<res.getLength(); i++) {
				Node item = res.item(i);
				if(item.getNodeType() == Node.ELEMENT_NODE) {
					keys.add(res.item(i).getLocalName());
				}
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		
		return keys;
	}
	
	protected interface Function<T,R> {
		R apply(T t) throws Exception;
	}

	void mutateConfiguration(Function<? super Element, Boolean> mutateFunc) throws ConfigurationException {
		parent.mutateConfiguration(wrap(mutateFunc));
	}
	
	<R> R readConfiguration(Function<Element, R> readFunc) throws ConfigurationException {
		return parent.readConfiguration(wrap(readFunc));
	}

	/**
	 * Wrap a Function (mutating or not) in a Function that will "dig down" in the parent
	 * configuration before executing the given Function on the correct configuration section.
	 * 
	 * @param func The function to be wrapped
	 * @return
	 */
	private <R> Function<Element, R> wrap(final Function<? super Element,R> func) {
		return new Function<Element, R>() {
			@Override
			public R apply(Element parentElement) throws Exception {
				// Now apply the original function with the newly created element for it
				return func.apply(getOrCreateElement(parentElement, namespaceUri, rootElementName));
			}
		};
	}

}
