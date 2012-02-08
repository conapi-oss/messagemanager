package nl.queuemanager.core.util;

public class UserCanceledException extends RuntimeException {

	private static final long serialVersionUID = -4896017355321152383L;

	public UserCanceledException() {
	}

	public UserCanceledException(String message) {
		super(message);
	}

	public UserCanceledException(Throwable cause) {
		super(cause);
	}

	public UserCanceledException(String message, Throwable cause) {
		super(message, cause);
	}

}
