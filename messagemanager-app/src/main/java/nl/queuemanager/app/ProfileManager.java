package nl.queuemanager.app;

import com.google.common.eventbus.Subscribe;
import nl.queuemanager.Profile;
import nl.queuemanager.ProfileActivatedEvent;
import nl.queuemanager.core.platform.PlatformHelper;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Singleton
public class ProfileManager {
	private Logger logger = Logger.getLogger(getClass().getName());
	
	private final File profilesFolder;
	private final Predicate<URL> jarExists;
	private Set<Profile> profiles = new HashSet<>();
	// what each plugin ships, by its module class, from its own profile
	private final Map<String, Shipped> shippedClients = new HashMap<>();
	
	@Inject
	public ProfileManager(PlatformHelper platform) {
		this(new File(platform.getDataFolder(), "profiles"), entry -> isOnDisk(entry, PluginModuleHelper.mmHome()));
	}

	/**
	 * @param jarExists whether a classpath entry names a jar that is there, where the app will load it from
	 */
	ProfileManager(File profilesFolder, Predicate<URL> jarExists) {
		this.profilesFolder = profilesFolder;
		this.jarExists = jarExists;
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
	
	@Subscribe
	public void profileActivated(ProfileActivatedEvent e) {
		tryToSaveProfile(e.getProfile());
	}

	public void putProfileIfNotExist(Profile profile) {
		putProfileIfNotExist(profile, false);
	}
	public void putProfileIfNotExist(Profile profile, boolean refreshMetadata) {
		if(refreshMetadata){
			List<String> shipped = generatedJars(profile.getClasspath());
			if(shipped != null && profile.getPlugins() != null && !profile.getPlugins().isEmpty()) {
				shippedClients.put(profile.getPlugins().get(0), new Shipped(folderOf(shipped.get(0)), profile.getClasspath()));
			}
			final Set<Profile> toBeRemoved = new HashSet<>();
			final Set<Profile> toBeAdded = new HashSet<>();

			profiles.forEach( p -> {
				if(p.getId().equals(profile.getId()) || p.getPlugins().get(0).equals(profile.getPlugins().get(0))){
					// same ID or same type of plugin
					// we want to update the metadata for all plugins that used the same plugin ID or class
					toBeRemoved.add(p);
					// always use latest commercial icons
					p.setIconData(profile.getIconData());
					//TODO: what if user changed the name and
					//existing.setDescription(profile.getDescription());
					//existing.setName(profile.getName());

					// important if we change plugins, this loop depends on the plugin classes therefore will only work to add additional classes
					p.setPlugins(profile.getPlugins());

					// The stored copy of the plugin's own profile follows a release that changed the plugin's client
					// libraries - another client version, a library raised under the same one, or a rollback - so the
					// change reaches installations that used the profile before (old jars stay on disk and would
					// otherwise go on being loaded). Jars the user added from elsewhere stay.
					List<URL> moved = p.getId().equals(profile.getId()) ? movedClasspath(p, profile) : null;
					if(moved != null) {
						p.setJars(profile.getJars());
						p.setClasspath(moved);
					}

					toBeRemoved.add(p);
					toBeAdded.add(p);
				}
			}
			);

			toBeAdded.add(profile);

			// refresh the set
			profiles.removeAll(toBeRemoved);
			profiles.addAll(toBeAdded);
		}
		else{
			profiles.add(profile);
		}
	}
	
	/**
	 * The classpath the stored copy of a plugin's own profile moves to when a release changed the plugin's client
	 * libraries, or null when it stays as it is. The plugin's classpath must be a generated one (every entry in one
	 * {@code @MM_HOME@/clients/<platform>/<version>/} folder); the stored entries in a version folder of that
	 * {@code clients/<platform>/} are the stored copy's generated part, the others are the user's own. It moves when the
	 * generated part, all in one folder, names a jar the plugin no longer names and every one of the plugin's jars is
	 * there: to the plugin's jars, then the user's own entries in their order - except a user's own copy of a jar the
	 * plugin ships (the same module name), which would replace the plugin's in the module layer. Jars the user removed
	 * (a subset of the plugin's), a generated part across two folders, and new jars that are missing - an update that
	 * did not finish - keep the stored classpath.
	 */
	private List<URL> movedClasspath(Profile stored, Profile fromPlugin) {
		List<String> pluginJars = generatedJars(fromPlugin.getClasspath());
		if(pluginJars == null || stored.getClasspath() == null) {
			return null;
		}
		String platform = platformOf(folderOf(pluginJars.get(0)));
		Map<Boolean, List<URL>> parts = stored.getClasspath().stream()
				.collect(Collectors.partitioningBy(entry -> inPlatform(entry, platform)));
		List<String> generated = generatedJars(parts.get(true));
		if(generated == null || pluginJars.containsAll(generated)
				|| !fromPlugin.getClasspath().stream().allMatch(jarExists)) {
			return null;
		}
		Set<String> shippedModules = moduleNames(fromPlugin.getClasspath());
		List<URL> moved = new ArrayList<>(fromPlugin.getClasspath());
		for(URL own: parts.get(false)) {
			if(!shippedModules.contains(moduleName(own))) {
				moved.add(own);
			}
		}
		return moved;
	}

	/**
	 * Whether a profile uses another version of a client its plugin ships: an entry in another version folder of the
	 * plugin's {@code clients/<platform>/}, or a jar of the user's own with the module name of a jar the plugin ships but
	 * another file name - a profile the user pinned, one an update could not move, or one made before the clients
	 * shipped with the plugins. False while the plugin's own profile has not been seen.
	 */
	public boolean namesAnotherClientVersion(Profile profile) {
		if(profile == null || profile.getPlugins() == null || profile.getPlugins().isEmpty() || profile.getClasspath() == null) {
			return false;
		}
		Shipped shipped = shippedClients.get(profile.getPlugins().get(0));
		if(shipped == null) {
			return false;
		}
		String platform = platformOf(shipped.folder);
		for(URL entry: profile.getClasspath()) {
			String folder = clientFolderOf(entry);
			if(folder != null ? folder.startsWith(platform) && !folder.equals(shipped.folder)
					: shipped.modules.contains(moduleName(entry)) && !shipped.fileNames.contains(fileName(entry))) {
				return true;
			}
		}
		return false;
	}

	/** What a plugin ships: its client folder, and the module and file names of its client jars. */
	private static final class Shipped {
		final String folder;
		final Set<String> modules;
		final Set<String> fileNames;

		Shipped(String folder, List<URL> classpath) {
			this.folder = folder;
			this.modules = moduleNames(classpath);
			this.fileNames = classpath.stream().map(ProfileManager::fileName).collect(Collectors.toSet());
		}
	}

	/** {@code clients/<platform>/<version>} of a generated entry, or null for any other entry. */
	private static String clientFolderOf(URL entry) {
		Matcher m = GENERATED_CLASSPATH_ENTRY.matcher(entry.toString());
		return m.matches() ? m.group(1) : null;
	}

	/** Whether an entry is a generated one in a version folder of {@code clients/<platform>/}. */
	private static boolean inPlatform(URL entry, String platform) {
		String folder = clientFolderOf(entry);
		return folder != null && folder.startsWith(platform);
	}

	private static String fileName(URL entry) {
		String path = entry.getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}

	/** The module name the plugin loader gives a jar ({@link PluginModuleHelper#deriveModuleName}). */
	private static String moduleName(URL entry) {
		return PluginModuleHelper.deriveModuleName(fileName(entry));
	}

	private static Set<String> moduleNames(List<URL> classpath) {
		return classpath.stream().map(ProfileManager::moduleName).collect(Collectors.toSet());
	}

	/** {@code clients/<platform>/<version>} of a {@code clients/<platform>/<version>/<jar>}. */
	private static String folderOf(String jar) {
		return jar.substring(0, jar.lastIndexOf('/'));
	}

	/** {@code clients/<platform>/} of a {@code clients/<platform>/<version>}. */
	private static String platformOf(String folder) {
		return folder.substring(0, folder.lastIndexOf('/') + 1);
	}

	/**
	 * The jars of a classpath a plugin generates - every entry in one {@code @MM_HOME@/clients/<platform>/<version>/}
	 * folder - as {@code clients/<platform>/<version>/<jar>}, or null for any other classpath, an empty one included.
	 */
	static List<String> generatedJars(List<URL> classpath) {
		if(classpath == null || classpath.isEmpty()) {
			return null;
		}
		List<String> jars = new ArrayList<>();
		String folder = null;
		for(URL entry: classpath) {
			String entryFolder = clientFolderOf(entry);
			if(entryFolder == null || (folder != null && !folder.equals(entryFolder))) {
				return null;
			}
			folder = entryFolder;
			jars.add(folder + "/" + fileName(entry));
		}
		return jars;
	}

	/**
	 * Whether a classpath entry names a jar that exists where the plugin loader will look for it.
	 */
	static boolean isOnDisk(URL entry, String mmHome) {
		if(mmHome == null) {
			return false;
		}
		try {
			return new File(URI.create(PluginModuleHelper.fixJarUrl(entry.toString(), mmHome))).isFile();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static final Pattern GENERATED_CLASSPATH_ENTRY =
			Pattern.compile("file:/+@MM_HOME@/(clients/[^/]+/[^/]+)/([^/]+\\.jar)");

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
