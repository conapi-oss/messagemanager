package nl.queuemanager.app;

import com.google.common.eventbus.Subscribe;
import nl.queuemanager.Profile;
import nl.queuemanager.ProfileActivatedEvent;
import nl.queuemanager.core.platform.PlatformHelper;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Singleton
public class ProfileManager {
	private Logger logger = Logger.getLogger(getClass().getName());
	
	private final File profilesFolder;
	private Set<Profile> profiles = new HashSet<>();
	
	@Inject
	public ProfileManager(PlatformHelper platform) {
		this.profilesFolder = new File(platform.getDataFolder(), "profiles");
		load(profilesFolder);
	}
	
	public Profile getProfileById(String id) {
		for(Profile p: profiles) {
			if(id.equals(p.getId())) {
				return p;
			}
		}
		return null;
	}
	
	public Set<Profile> getAllProfiles() {
		return profiles;
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
		
		if(files == null) {
			logger.warning("files[] was null?");
			return;
		}
		
		for(File file: files) {
			try {
				logger.fine(String.format("Loading profile from file %s", file.getAbsolutePath()));
				putProfileIfNotExist(readDescriptor(new FileInputStream(file), null));
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
			appendTextElement(profileElement, "icon", "base64:" + Base64.getEncoder().encodeToString(profile.getIconData()));
			appendTextElement(profileElement, "description", profile.getDescription());
			
			profileElement.appendChild(createListElement("jars", "jar", profile.getJars(), doc));
			profileElement.appendChild(createListElement("plugins", "plugin", profile.getPlugins(), doc));
			profileElement.appendChild(createListElement("classpath", "entry", profile.getClasspath(), doc));
			
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
	
	private Element createListElement(String name, String entryName, List<?> objects, Document doc) {
		Element listElement = doc.createElement(name);
		for(Object obj: objects) {
			appendTextElement(listElement, entryName, obj.toString());
		}
		return listElement;
	}

	private void appendTextElement(Element parent, String name, String value) {
		Element e = parent.getOwnerDocument().createElement(name);
		e.setTextContent(value);
		parent.appendChild(e);
	}

	public Profile readDescriptor(InputStream stream, ZipFile pluginZip) {
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
			profile.setIconData(loadIcon(xpath.evaluate("/profile/icon", doc), pluginZip));
			profile.setDescription(xpath.evaluate("/profile/description", doc));
			profile.setJars(readStringList((NodeList)xpath.evaluate("/profile/jars/jar", doc, XPathConstants.NODESET)));
			profile.setPlugins(readStringList((NodeList)xpath.evaluate("/profile/plugins/plugin", doc, XPathConstants.NODESET)));
			profile.setClasspath(readClasspath((NodeList)xpath.evaluate("/profile/classpath/entry", doc, XPathConstants.NODESET)));
			return profile;
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			logger.log(Level.WARNING, "Unable to read profile", e);
		}
		
		return null;
	}
	
	private byte[] loadIcon(final String iconString, final ZipFile pluginZip) throws IOException {
		if(iconString == null || "".equals(iconString.trim())) {
			return null;
		}

		// If the data is base64 encoded, return the decoded version
		if(iconString.startsWith("base64:")) {
			return Base64.getDecoder().decode(iconString.substring(7));
		}
		
		if(pluginZip == null) {
			logger.warning("No zipfile provided!");
			return null;
		}
		
		final ZipEntry iconEntry = pluginZip.getEntry(iconString);
		if(iconEntry == null) {
			logger.warning("Unable to find icon at path " + iconString);
			return null;
		}
		
		// Grab the resource and load the image (if any)
		try(InputStream iconStream = ZipUtil.openStreamForZipEntry(pluginZip, iconEntry)) {
			byte[] data = new byte[(int) iconEntry.getSize()];
			int offset = 0, n=0;
			while((n = iconStream.read(data, offset, data.length-offset)) > 0) {
				offset += n;
				logger.finest("Reading icon: " + offset + "/" + data.length);
			}
			return data;
		}
	}

	private List<String> readStringList(NodeList nodes) {
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
