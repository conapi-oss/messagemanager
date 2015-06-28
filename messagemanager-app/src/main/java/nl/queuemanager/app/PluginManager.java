package nl.queuemanager.app;

import java.util.Arrays;
import java.util.List;

public class PluginManager {
	public static List<PluginDescriptor> getInstalledPlugins() {
		PluginDescriptor amq = new PluginDescriptor();
		amq.setName("ActiveMQ Plugin");
		amq.setDescription("Allows connections to ActiveMQ");
		amq.setModuleClass("nl.queuemanager.activemq.ActiveMQModule");
		
		PluginDescriptor smq = new PluginDescriptor();
		smq.setName("SonicMQ Plugin");
		smq.setDescription("Allows connections to Sonic MQ");
		smq.setModuleClass("nl.queuemanager.sonicmq.SMMModule");
		
		PluginDescriptor solace = new PluginDescriptor();
		solace.setName("Solace Plugin");
		solace.setDescription("Allows connections to Solace");
		solace.setModuleClass("nl.queuemanager.solace.SolaceModule");
		
		return Arrays.asList(amq, smq, solace);
	}
}
