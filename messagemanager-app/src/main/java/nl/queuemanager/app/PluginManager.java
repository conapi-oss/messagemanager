package nl.queuemanager.app;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nl.queuemanager.Version;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.core.util.EnumerationIterator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.inject.Module;

@Singleton
public class PluginManager {
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final PlatformHelper platform;
	private final ProfileManager profileManager;
	
	private Map<String, PluginDescriptor> plugins = new HashMap<>();
	private URLClassLoader pluginClassloader;
	
	@Inject
	public PluginManager(PlatformHelper platform, ProfileManager profileManager) {
		this.platform = platform;
		this.profileManager = profileManager;
		plugins.putAll(findInstalledPlugins());
	}
	
	public List<PluginDescriptor> getInstalledPlugins() {
		return new ArrayList<>(plugins.values());
	}
	
	public Map<String, PluginDescriptor> findInstalledPlugins() {
		Map<String, PluginDescriptor> ret = new HashMap<String, PluginDescriptor>();
		
		File pluginsFolder = new File(new File(platform.getDataFolder(), "plugins"), Version.VERSION);
		pluginsFolder.mkdirs(); // Ensure the directory exists
		logger.info("Looking for plugins in " + pluginsFolder.getAbsolutePath());
		
		// Find all plugin files
		final File[] pluginFiles = pluginsFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});

		// Now read their plugin descriptors to make sure we only consider actual plugin jars
		for(final File pluginFile: pluginFiles) {
			logger.fine(String.format("Reading potential plugin file %s", pluginFile.getAbsolutePath()));
			try(ZipFile pluginZip = new ZipFile(pluginFile)) {
				
				for(ZipEntry entry: EnumerationIterator.of(pluginZip.entries())) {
					// Normalize directory separators in entry name
					String name = entry.getName().replace('\\', '/');
					
					// If entry is a plugin descriptor, read it
					if(name.startsWith("MessageManager/plugins/") && name.endsWith(".xml")) try (
						InputStream descriptorStream = openStreamForZipEntry(pluginZip, entry))
					{
						PluginDescriptor descriptor = readDescriptor(pluginFile, descriptorStream);
						if(descriptor != null) {
							logger.info(String.format("Found plugin: %s (%s) in file %s", descriptor.getName(), descriptor.getModuleClassName(), pluginFile.getName()));
							ret.put(descriptor.getModuleClassName(), descriptor);
						}
					}
					
					// If entry is a profile descriptor, read that
					if(name.startsWith("MessageManager/profiles/") && name.endsWith(".xml")) try (
						InputStream stream = openStreamForZipEntry(pluginZip, entry))
					{
						Profile profile = profileManager.readDescriptor(stream);
						if(profile != null) {
							logger.info(String.format("Found profile: %s in file %s", profile.getName(), pluginFile.getName()));
							profileManager.putProfile(profile);
						}
					}
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Unable to read plugin descriptor for " + pluginFile.getAbsolutePath(), e);
			}
		}
		
		return ret;
	}
	
	private InputStream openStreamForZipEntry(ZipFile zipFile, ZipEntry entry) throws IOException {
		if(entry != null) {
			return zipFile.getInputStream(entry);
		}
		return null;
	}
	
	private PluginDescriptor readDescriptor(File pluginFile, InputStream stream) {
		if(stream == null) {
			return null;
		}
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(stream));
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			PluginDescriptor descriptor = new PluginDescriptor();
			descriptor.setName(xpath.evaluate("/plugin/name", doc));
			descriptor.setDescription(xpath.evaluate("/plugin/description", doc));
			descriptor.setModuleClass(xpath.evaluate("/plugin/moduleClass", doc));
			descriptor.setFile(pluginFile);
			return descriptor;
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			logger.log(Level.WARNING, "Unable to read plugin descriptor from " + pluginFile.getAbsolutePath(), e);
		}
		
		return null;
	}
	
	public PluginDescriptor getPluginByClassName(String classname) {
		if(plugins.size() == 0) {
			getInstalledPlugins();
		}

		return plugins.get(classname);
	}
	
	public PluginDescriptor downloadPluginByClassname(String classname) throws PluginManagerException {
		throw new PluginManagerException("Plugin " + classname + " does not exist!");
	}

	public List<Module> loadPluginModules(Collection<? extends PluginDescriptor> plugins, List<URL> classpath) throws PluginManagerException {
		if(pluginClassloader != null) {
			throw new IllegalStateException("PluginManager can only load plugins once!");
		}
		
		try {
			List<URL> urls = new ArrayList<URL>();
			for(PluginDescriptor plugin: plugins) {
				try {
					urls.add(plugin.getFile().toURI().toURL());
				} catch (MalformedURLException e) {
				}
				urls.addAll(plugin.getClasspath());
			}
			urls.addAll(classpath);
			
			URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
			System.out.println("Created classloader: " + Arrays.toString(classLoader.getURLs()));

			List<Module> result = new ArrayList<Module>();
			for(PluginDescriptor plugin: plugins) {
				Class<Module> moduleClass = (Class<Module>) classLoader.loadClass(plugin.getModuleClassName());
				Module module = moduleClass.newInstance();
				result.add(module);
			}
			
			pluginClassloader = classLoader;
			return result;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new PluginManagerException("Unable to load plugin modules", e);
		}
	}
}
