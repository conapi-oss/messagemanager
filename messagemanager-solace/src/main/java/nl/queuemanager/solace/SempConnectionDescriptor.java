package nl.queuemanager.solace;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import nl.queuemanager.core.configuration.Configuration;
import nl.queuemanager.core.util.BasicCredentials;

import java.net.URI;
import java.net.URISyntaxException;

import static nl.queuemanager.solace.SempConnectionMethod.SEMP_OVER_HTTP;

/**
 * SempConnectionDescriptor adds semp connection properties to SmfConnectionDescriptor
 * allowing for the storage of connection information for semp connections (over http 
 * or smf).
 */
class SempConnectionDescriptor extends SmfConnectionDescriptor {
	public static final String DESCRIPTION = "description";
	public static final String CONNECTION_METHOD = "connectionMethod";
	public static final String HTTP_HOST = "httpHost";
	public static final String HTTP_PORT = "httpPort";
	public static final String APPLIANCE_NAME = "applianceName";

	/**
	 * User visible description to identify the connection
	 */
	@Getter @Setter private String description;
	
	/**
	 * How to make the management connection to the appliance, over Http or Smf.
	 */
	@Getter @Setter private SempConnectionMethod connectionMethod = SempConnectionMethod.SEMP_OVER_HTTP;
	
	/**
	 * Http(s) hostname for the appliance, only used for Semp over Http connections.
	 */
	@Getter @Setter private String httpHost;
	
	/**
	 * Http(s) port for the appliance, only used for Semp over Http connections.
	 */
	@Getter @Setter private int httpPort;

	/**
	 * The name of the appliance to be used for Semp over Smf queries. Not used for 
	 * Semp over Http. This is different from the DNS name for the appliance although
	 * the two may be identical.
	 */
	@Getter @Setter private String applianceName;

	public URI createHttpUri() throws SempException {
		if(getHttpHost() == null || getHttpHost().trim().length() == 0) {
			throw new IllegalArgumentException("Hostname is required");
		}
		if(getHttpPort() == 0) {
			throw new IllegalArgumentException("Port number is required");
		}
		if(getCredentials() == null) {
			throw new IllegalArgumentException("Credentials are required");
		}
		if(getCredentials() != null) {
			if(!(getCredentials() instanceof BasicCredentials)) {
				throw new IllegalStateException("Only basic credentials are supported over http, we got " + getCredentials().getClass().getSimpleName());
			}
			
			if(getCredentials().getPrincipalName() == null || getCredentials().getPrincipalName().trim().length() == 0) {
				throw new IllegalArgumentException("User name is required");
			}
		}
		
		try {
			URI uri = URI.create(String.format("http%s://%s:%d/SEMP",
					isSecure()?"s":"", getHttpHost(), getHttpPort()));
			BasicCredentials cred = (BasicCredentials)getCredentials();
			if(cred != null && cred.getUsername() != null) {
				uri = addUserInfoToURI(uri, cred.getUsername(), cred.getPassword());
			}
			return uri;
		} catch (URISyntaxException e) {
			throw new SempException("Unable to construct HTTP URI", e);
		}
	}

	private static URI addUserInfoToURI(URI uri, String username, String password) throws URISyntaxException {
		URI uriWithUserInfo = new URI(
				uri.getScheme(), 
				username + ":" + password,
				uri.getHost(),
				uri.getPort(),
				uri.getPath(),
				uri.getQuery(),
				uri.getFragment());
		return uriWithUserInfo;
	}
	
	/**
	 * Save this connection descriptor to the provided Configuration object.
	 * @param c
	 */
	void saveTo(Configuration c) {
		c.setValue(CONNECTION_METHOD, getConnectionMethod().name());
		c.setValue(DESCRIPTION, getDescription());
		c.setValue(HTTP_HOST, getHttpHost());
		c.setValue(HTTP_PORT, Integer.toString(getHttpPort()));
		c.setValue(APPLIANCE_NAME, getApplianceName());
		super.saveTo(c);
	}

	/**
	 * Load this connection descriptor from the provided Configuration object.
	 * @param c
	 */
	SempConnectionDescriptor loadFrom(Configuration c) {
		super.loadFrom(c);
		setConnectionMethod(SempConnectionMethod.valueOf(c.getValue(CONNECTION_METHOD, SempConnectionMethod.SEMP_OVER_HTTP.name())));
		setDescription(c.getValue(DESCRIPTION, null));
		setHttpHost(c.getValue(HTTP_HOST, null));
		setHttpPort(Integer.parseInt(c.getValue(HTTP_PORT, "-1")));
		setApplianceName(c.getValue(APPLIANCE_NAME, null));
		return this;
	}

	String getDisplayName() {
		String hostname = getConnectionMethod() == SEMP_OVER_HTTP ? getHttpHost() : getSmfHost();
		int port = getConnectionMethod() == SEMP_OVER_HTTP ? getHttpPort() : getSmfPort();
		String principal = getCredentials() != null ? getCredentials().getPrincipalName() : "";
		
		return String.format("[%s:%d] - %s", hostname, port, principal);
	}
	
	@Override
	public String toString() {
		if(Strings.isNullOrEmpty(getDescription())) {
			return getDisplayName();
		} else {
			return getDescription() + " " + getDisplayName();
		}
	}

}
