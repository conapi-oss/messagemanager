package nl.queuemanager.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Module;

public class PluginManager {
	private Map<String, PluginDescriptor> plugins = new HashMap<>();
	private URLClassLoader pluginClassloader;
	
	public PluginManager() {
		try {
			PluginDescriptor amq = new PluginDescriptor();
			amq.setName("ActiveMQ Plugin");
			amq.setDescription("Allows connections to ActiveMQ");
			amq.setModuleClass("nl.queuemanager.activemq.ActiveMQModule");
			amq.setClasspath(Arrays.asList(new URL[] {
					new URL("file:///Users/gerco/Projects/MessageManager/workspace/messagemanager/messagemanager-activemq/target/messagemanager-activemq-3.0-SNAPSHOT.jar"),
					new URL("file:///Users/gerco/Projects/Internal/apache-activemq-5.11.0/activemq-all-5.11.0.jar")
			}));
			plugins.put(amq.getModuleClassName(), amq);
			
			PluginDescriptor smq = new PluginDescriptor();
			smq.setName("SonicMQ Plugin");
			smq.setDescription("Allows connections to Sonic MQ");
			smq.setModuleClass("nl.queuemanager.smm.SMMModule");
			smq.setClasspath(Arrays.asList(new URL[] {
					new URL("file:///Users/gerco/Projects/MessageManager/workspace/messagemanager/messagemanager-sonicmq/target/messagemanager-sonicmq-3.0-SNAPSHOT.jar")
			}));
			plugins.put(smq.getModuleClassName(), smq);
			
			PluginDescriptor solace = new PluginDescriptor();
			solace.setName("Solace Plugin");
			solace.setDescription("Allows connections to Solace");
			solace.setModuleClass("nl.queuemanager.solace.SolaceModule");
			plugins.put(solace.getModuleClassName(), solace);
		} catch (MalformedURLException e) {
		}
	}
	
	public List<PluginDescriptor> getInstalledPlugins() {
		return new ArrayList<>(plugins.values());
	}
	
	public PluginDescriptor getPluginByClassName(String classname) {
		return plugins.get(classname);
	}
	
	public List<Module> loadPluginModules(List<PluginDescriptor> plugins, List<URL> classpath) {
		if(pluginClassloader != null) {
			throw new IllegalStateException("PluginManager can only load plugins once!");
		}
		
		try {
			List<URL> urls = new ArrayList<URL>();
			for(PluginDescriptor plugin: plugins) {
				urls.addAll(plugin.getClasspath());
			}
			urls.addAll(classpath);
			
			pluginClassloader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
			System.out.println("Created classloader: " + Arrays.toString(pluginClassloader.getURLs()));

			List<Module> result = new ArrayList<Module>();
			for(PluginDescriptor plugin: plugins) {
				Class<Module> moduleClass = (Class<Module>) pluginClassloader.loadClass(plugin.getModuleClassName());
				Module module = moduleClass.newInstance();
				result.add(module);
			}
			return result;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
