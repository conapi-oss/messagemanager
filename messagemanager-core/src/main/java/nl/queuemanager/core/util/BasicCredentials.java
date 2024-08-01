package nl.queuemanager.core.util;

import lombok.Getter;
import lombok.Setter;
import nl.queuemanager.core.configuration.Configuration;

import javax.jms.ConnectionFactory;
import java.lang.reflect.Method;

public class BasicCredentials implements Credentials {

	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";

	@Getter @Setter private String username;
	@Getter @Setter private String password;
	
	public BasicCredentials() {
		
	}
	
	public BasicCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString() {
		return getUsername() + ":" + getPassword();
	}

	@Override
	/**
	 * Attemps to set username and password on the ConnectionFactory using reflection. If
	 * this doesn't work for some messaging providers - they will have to provide their own
	 * Credentials implementation and dialogs to create such objects.
	 */
	public void apply(ConnectionFactory cf) throws Exception {
		applyCredential(cf, "user", getUsername());
		applyCredential(cf, "password", getPassword());
	}

	private void applyCredential(Object target, String credentialType, String credentialValue) throws Exception {
		String[] methodNames = {
				"set" + capitalize(credentialType),
				"set" + capitalize(credentialType) + "Name",
				"set" + capitalize(credentialType.replace("name", ""))
		};

		for (String methodName : methodNames) {
			try {
				Method method = target.getClass().getMethod(methodName, String.class);
				method.invoke(target, credentialValue);
				return; // Successfully invoked the method, so exit the method
			} catch (NoSuchMethodException e) {
				// Method not found, continue to the next iteration
			}
		}

		// If we reach here, no matching method was found
		throw new NoSuchMethodException("No suitable method found to set " + credentialType);
	}

	private String capitalize(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	@Override
	public void saveTo(Configuration config) {
		config.setValue(USERNAME, getUsername());
		config.setValue(PASSWORD, getPassword());
	}

	@Override
	public BasicCredentials loadFrom(Configuration config) {
		setUsername(config.getValue(USERNAME, null));
		setPassword(config.getValue(PASSWORD, null));
		return this;
	}

	@Override
	public String getPrincipalName() {
		return getUsername();
	}
	
}
