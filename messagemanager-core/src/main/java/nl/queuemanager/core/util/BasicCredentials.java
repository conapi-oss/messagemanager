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
		Method setUsername = cf.getClass().getMethod("setUsername", new Class[] { String.class} );
		setUsername.invoke(cf, new Object[] { getUsername() });
		
		Method setPassword = cf.getClass().getMethod("setPassword", new Class[] { String.class} );
		setPassword.invoke(cf, new Object[] { getPassword() });
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
