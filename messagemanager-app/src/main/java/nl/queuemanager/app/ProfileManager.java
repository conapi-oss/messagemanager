package nl.queuemanager.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class ProfileManager {

	private List<Profile> profiles = new ArrayList<>();
	
	@Inject
	public ProfileManager(PluginManager pluginManager) {
		Profile smq = new Profile();
		smq.setName("Sonic MQ");
		smq.setDescription("Use this profile to connect to Sonic MQ. You will need to add the Sonic MQ jar files from your installation to the classpath list below before this profile will work.");
		smq.setPlugins(Collections.singletonList(pluginManager.getPluginByClassName("nl.queuemanager.smm.SMMModule")));
		profiles.add(smq);
		
		Profile amq = new Profile();
		amq.setName("ActiveMQ 5.11");
		amq.setDescription("Use this profile to connect to ActiveMQ 5.11.");
		amq.setPlugins(Collections.singletonList(pluginManager.getPluginByClassName("nl.queuemanager.activemq.ActiveMQModule")));
		profiles.add(amq);
	}
	
	public List<Profile> getAllProfiles() {
		return profiles;
	}
	
}
