package nl.queuemanager.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nl.queuemanager.core.platform.PlatformHelper;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.eventbus.Subscribe;

@Singleton
public class ProfileManager {
	private Logger logger = Logger.getLogger(getClass().getName());
	
	private final PlatformHelper platform;
	
	private final File profilesFolder;
	private Set<Profile> profiles = new HashSet<>();
	
	@Inject
	public ProfileManager(PlatformHelper platform) {
		this.platform = platform;
		this.profilesFolder = new File(platform.getDataFolder(), "profiles");
		load(profilesFolder);
	}
	
	public Set<Profile> getAllProfiles() {
		return profiles;
	}
	
	@Subscribe
	public void profileActivated(ProfileActivatedEvent e) {
		tryToSaveProfile(e.getProfile());
	}
	
	public void putProfileIfNotExist(Profile profile) {
		profiles.add(profile);
	}
	
	public void removeProfile(Profile profile) {
		profiles.remove(profile);
		File file = fileForProfile(profile);
		if(file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * Load existing profiles from disk
	 */
	private void load(File profilesFolder) {
		File[] files = profilesFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
			
		});
		
		for(File file: files) {
			try {
				logger.fine(String.format("Loading profile from file %s", file.getAbsolutePath()));
				putProfileIfNotExist(readDescriptor(new FileInputStream(file)));
			} catch (FileNotFoundException e) {
				logger.log(Level.WARNING, "Unable to load profile from file " + file.getAbsolutePath(), e);
			}
		}
	}
	
	public boolean tryToSaveProfile(Profile profile) {
		profilesFolder.mkdirs();
		File file = fileForProfile(profile);

		logger.info(String.format("Saving profile %s to file %s", profile.getName(), file.getAbsolutePath()));
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			
			Element profileElement = doc.createElement("profile");
			appendTextElement(profileElement, "id", profile.getId());
			appendTextElement(profileElement, "name", profile.getName());
			appendTextElement(profileElement, "description", profile.getDescription());
			
			Element pluginListElement = doc.createElement("plugins");
			for(String pluginClass: profile.getPlugins()) {
				appendTextElement(pluginListElement, "plugin", pluginClass);
			}
			profileElement.appendChild(pluginListElement);
			
			Element classpathElement = doc.createElement("classpath");
			for(URL url: profile.getClasspath()) {
				appendTextElement(classpathElement, "entry", url.toString());
			}
			profileElement.appendChild(classpathElement);
			doc.appendChild(profileElement);
			
			Result result = new StreamResult(file);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(doc), result);
			
			return true;
		} catch (ParserConfigurationException | TransformerException e) {
			logger.log(Level.WARNING, "Unable to save profile " + profile.getName() + " to file " + file.getAbsolutePath(), e);
		}
		
		return false;
	}

	private File fileForProfile(Profile profile) {
		File file = new File(profilesFolder, profile.getId() + ".xml");
		return file;
	}
	
	private void appendTextElement(Element parent, String name, String value) {
		Element e = parent.getOwnerDocument().createElement(name);
		e.setTextContent(value);
		parent.appendChild(e);
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
			profile.setId(xpath.evaluate("/profile/id", doc));
			profile.setName(xpath.evaluate("/profile/name", doc));
			profile.setDescription(xpath.evaluate("/profile/description", doc));
			profile.setPlugins(readPluginList((NodeList)xpath.evaluate("/profile/plugins/plugin", doc, XPathConstants.NODESET)));
			profile.setClasspath(readClasspath((NodeList)xpath.evaluate("/profile/classpath/entry", doc, XPathConstants.NODESET)));
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
	
	private List<URL> readClasspath(NodeList nodes) {
		List<URL> ret = new ArrayList<URL>();
		
		for(int i=0; i<nodes.getLength(); i++) {
			Node n = nodes.item(i);
			try {
				ret.add(new URL(n.getTextContent()));
			} catch (MalformedURLException | DOMException e) {
				// Unable to parse URL. Ignore this one.
				logger.log(Level.WARNING, String.format("Unable to parse %s",  n.getTextContent(), e));
			}
		}
		
		return ret;
	}
	
}
