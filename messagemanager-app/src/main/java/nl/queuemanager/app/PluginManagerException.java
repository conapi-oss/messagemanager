package nl.queuemanager.app;

@SuppressWarnings("serial")
public class PluginManagerException extends RuntimeException {

	public PluginManagerException() {
	}

	public PluginManagerException(String message) {
		super(message);
	}

	public PluginManagerException(Throwable cause) {
		super(cause);
	}

	public PluginManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public PluginManagerException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
