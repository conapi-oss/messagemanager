package nl.queuemanager.core.configuration;

import java.io.File;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import nl.queuemanager.core.platform.PlatformHelper;

public class XmlConfigurationModule extends AbstractModule {

	private final String configFile;
	private final String namespaceUri;
	
	public XmlConfigurationModule(final String configFile, final String namespaceUri) {
		this.configFile = configFile;
		this.namespaceUri = namespaceUri;
	}
	
	@Provides @Singleton
	public Configuration createConfiguration(CoreXmlConfiguration config) {
		return config;
	}
	
	@Provides @Singleton
	public CoreConfiguration createCoreConfiguration(CoreXmlConfiguration config) {
		return config;
	}
	
	@Provides @Singleton
	public CoreXmlConfiguration createCoreXmlConfiguration(PlatformHelper platform) {
		final File realConfigFile = configFile.contains("/") || configFile.contains("\\") ? 
				new File(configFile) : 
				new File(platform.getDataFolder(), configFile);
		return new CoreXmlConfiguration(realConfigFile, namespaceUri, "Configuration");
	}

}
