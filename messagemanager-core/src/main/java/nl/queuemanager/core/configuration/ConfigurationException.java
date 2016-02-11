package nl.queuemanager.core.configuration;

public class ConfigurationException extends Exception {

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
