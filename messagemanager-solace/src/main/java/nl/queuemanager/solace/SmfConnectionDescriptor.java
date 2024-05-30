package nl.queuemanager.solace;

import com.google.common.base.Strings;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SupportedProperty;
import lombok.Data;
import nl.queuemanager.core.configuration.Configuration;
import nl.queuemanager.core.util.BasicCredentials;
import nl.queuemanager.core.util.Credentials;

import java.io.File;
import java.net.URI;

/**
 * SolaceConnectionDescriptor holds all properties required for creating
 * a messaging connection to Solace. This connection may be over SSL, and 
 * it may use basic or client certificate credentials.
 */
@Data
class SmfConnectionDescriptor {
	public static final String SMF_HOSTNAME = "smfHostname";
	public static final String SMF_PORT = "smfPort";
	public static final String CREDENTIALS = "credentials";
	public static final String SECURE = "secure";

	private String key;
	private String smfHost;
	private int smfPort;
	private String messageVpn;
	private String applianceName;
	private AuthenticationScheme authenticationScheme = AuthenticationScheme.BASIC;
	private Credentials credentials;
	private boolean secure;
	private File trustStoreFile;
	private String trustStorePassword;

	public URI createSmfUri() throws SempException {
		final String format = "smf%s://%s:%d";
		return URI.create(String.format(format,
				(isSecure()?"s":""),
				getSmfHost(), getSmfPort()));
	}
	
	void saveTo(Configuration c) {
		c.setValue(SMF_HOSTNAME, getSmfHost());
		c.setValue(SMF_PORT, Integer.toString(getSmfPort()));
		c.setValue(SupportedProperty.SOLACE_JMS_VPN, getMessageVpn());
		c.setValue(SupportedProperty.SOLACE_JMS_AUTHENTICATION_SCHEME, getAuthenticationScheme().name());
		if(getCredentials() != null) {
			getCredentials().saveTo(c.sub(CREDENTIALS));
		}
		c.setValue(SECURE, Boolean.toString(isSecure()));
		c.setValue(SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE, getTrustStoreFile() != null ? getTrustStoreFile().getAbsolutePath() : null);
		c.setValue(SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE_PASSWORD, getTrustStorePassword());
	}
	
	SmfConnectionDescriptor loadFrom(Configuration config) {
		setKey(config.getKey());
		setSmfHost(config.getValue(SMF_HOSTNAME, null));
		setSmfPort(Integer.parseInt(config.getValue(SMF_PORT, "55555")));
		
		setAuthenticationScheme(AuthenticationScheme.valueOf(config.getValue(SupportedProperty.SOLACE_JMS_AUTHENTICATION_SCHEME, AuthenticationScheme.BASIC.name())));
		switch(getAuthenticationScheme()) {
		case BASIC:
			setCredentials(new BasicCredentials().loadFrom(config));
			break;
		case CLIENT_CERTIFICATE:
			setCredentials(new SolaceClientCertificateCredentials().loadFrom(config));
			break;
		}
		
		setSecure(Boolean.parseBoolean(config.getValue(SECURE, "false")));
		String tmpTrustStoreFile = config.getValue(SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE, null);
		if(!Strings.isNullOrEmpty(tmpTrustStoreFile)) {
			setTrustStoreFile(new File(tmpTrustStoreFile));
		}
		setTrustStorePassword(config.getValue(SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE_PASSWORD, null));
		setMessageVpn(config.getValue(SupportedProperty.SOLACE_JMS_VPN, null));
		return this;
	}
	
	void apply(SolConnectionFactory cf) throws Exception {
		URI uri = createSmfUri();
		
		cf.setHost(uri.getScheme() + "://" + uri.getHost());
		cf.setPort(uri.getPort());
		cf.setAuthenticationScheme(getAuthenticationScheme().getSolacePropertyValue());
		if(isSecure()) {
			cf.setSSLTrustStore(getTrustStoreFile().getAbsolutePath());
			cf.setSSLTrustStorePassword(getTrustStorePassword());
		}
		cf.setVPN(getMessageVpn());
		if(getCredentials() != null) {
			getCredentials().apply(cf);
		}
	}

}