package nl.queuemanager.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nl.queuemanager.core.platform.PlatformHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Singleton
public class ProfileManager {
	private Logger logger = Logger.getLogger(getClass().getName());
	
	private final PlatformHelper platform;
	
	private Set<Profile> profiles = new HashSet<>();
	
	@Inject
	public ProfileManager(PlatformHelper platform) {
		this.platform = platform;
	}
	
	public Set<Profile> getAllProfiles() {
		return profiles;
	}
	
	public void putProfile(Profile profile) {
		profiles.add(profile);
	}

	public Profile readDescriptor(InputStream stream) {
		if(stream == null) {
			return null;
		}
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(stream));
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			Profile profile = new Profile();
			profile.setName(xpath.evaluate("/profile/name", doc));
			profile.setDescription(xpath.evaluate("/profile/description", doc));
			profile.setPlugins(readPluginList((NodeList)xpath.evaluate("/profile/plugins/plugin", doc, XPathConstants.NODESET)));
			return profile;
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			logger.log(Level.WARNING, "Unable to read profile", e);
		}
		
		return null;
	}
	
	private List<String> readPluginList(NodeList nodes) {
		List<String> ret = new ArrayList<String>();
		
		for(int i=0; i<nodes.getLength(); i++) {
			Node n = nodes.item(i);
			ret.add(n.getTextContent());
		}
		
		return ret;
	}
	
}
