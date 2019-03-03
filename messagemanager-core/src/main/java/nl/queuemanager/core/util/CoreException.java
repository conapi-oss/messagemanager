package nl.queuemanager.core.util;

@SuppressWarnings("serial")
public class CoreException extends Exception {

	public CoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public CoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
