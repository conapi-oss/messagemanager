package nl.queuemanager.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Module;

public class PluginManager {
	private URLClassLoader pluginClassloader;
	
	public List<PluginDescriptor> getInstalledPlugins() {
		try {
			PluginDescriptor amq = new PluginDescriptor();
			amq.setName("ActiveMQ Plugin");
			amq.setDescription("Allows connections to ActiveMQ");
			amq.setModuleClass("nl.queuemanager.activemq.ActiveMQModule");
			amq.setClasspath(Arrays.asList(new URL[] {
					new URL("file:///Users/gerco/Projects/MessageManager/workspace/messagemanager/messagemanager-activemq/target/messagemanager-activemq-3.0-SNAPSHOT.jar"),
					new URL("file:///Users/gerco/Projects/Internal/apache-activemq-5.11.0/activemq-all-5.11.0.jar")
			}));
			
			PluginDescriptor smq = new PluginDescriptor();
			smq.setName("SonicMQ Plugin");
			smq.setDescription("Allows connections to Sonic MQ");
			smq.setModuleClass("nl.queuemanager.sonicmq.SMMModule");
			smq.setClasspath(Arrays.asList(new URL[] {
					new URL("file:///Users/gerco/Projects/MessageManager/workspace/messagemanager/messagemanager-sonicmq/target/messagemanager-sonicmq-3.0-SNAPSHOT.jar")
			}));
			
			PluginDescriptor solace = new PluginDescriptor();
			solace.setName("Solace Plugin");
			solace.setDescription("Allows connections to Solace");
			solace.setModuleClass("nl.queuemanager.solace.SolaceModule");
		
			return Arrays.asList(amq, smq, solace);
		} catch (MalformedURLException e) {
			return null;
		}
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
			System.out.println("Created classloader: " + pluginClassloader);

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
