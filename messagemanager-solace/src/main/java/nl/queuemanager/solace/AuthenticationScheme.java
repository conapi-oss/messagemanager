package nl.queuemanager.solace;

import com.solacesystems.jms.SupportedProperty;

enum AuthenticationScheme {
	BASIC("Basic authentication", SupportedProperty.AUTHENTICATION_SCHEME_BASIC), 
	CLIENT_CERTIFICATE("Client certificate", SupportedProperty.AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE);
	
	private final String displayName;
	private final String solacePropertyValue;
	
	private AuthenticationScheme(String displayName, String solacePropertyValue) {
		this.displayName = displayName;
		this.solacePropertyValue = solacePropertyValue;
	}
	
	String getSolacePropertyValue() {
		return solacePropertyValue;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
}