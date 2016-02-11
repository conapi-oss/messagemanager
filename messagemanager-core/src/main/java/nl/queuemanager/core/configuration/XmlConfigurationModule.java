package nl.queuemanager.core.configuration;

import java.io.File;

import javax.inject.Singleton;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.core.platform.PlatformModule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class XmlConfigurationModule extends AbstractModule {

	private final String configFile;
	private final String namespaceUri;
	
	public XmlConfigurationModule(final String configFile, final String namespaceUri) {
		this.configFile = configFile;
		this.namespaceUri = namespaceUri;
	}
	
	public void configure() {
		install(new PlatformModule());
	}
	
	@Provides @Singleton
	public Configuration createConfiguration(PlatformHelper platform) {
		final File realConfigFile = configFile.contains("/") || configFile.contains("\\") ? 
				new File(configFile) : 
				new File(platform.getDataFolder(), configFile);
		return new XmlConfiguration(realConfigFile, namespaceUri);
	}

}
