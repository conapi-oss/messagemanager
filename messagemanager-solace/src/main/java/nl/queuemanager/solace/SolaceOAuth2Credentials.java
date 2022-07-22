package nl.queuemanager.solace;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SupportedProperty;
import lombok.Getter;
import lombok.Setter;
import nl.queuemanager.core.configuration.Configuration;
import nl.queuemanager.core.util.Credentials;

import javax.jms.ConnectionFactory;
import java.util.Hashtable;

class SolaceOAuth2Credentials implements Credentials {

	@Getter @Setter private String accessToken;
	@Getter @Setter private String idToken;

	public void apply(Hashtable<String, Object> env) {
		env.put(SupportedProperty.SOLACE_JMS_OAUTH2_ACCESS_TOKEN, accessToken);
		env.put(SupportedProperty.SOLACE_JMS_OIDC_ID_TOKEN, idToken);
//		env.put(SupportedProperty.SOLACE_JMS_OAUTH2_ISSUER_IDENTIFIER, "https://auth.pingone.com/9119e5c0-5fa8-4f68-82a9-4384649c3d7b/as");
	}

	@Override
	public void apply(ConnectionFactory cf) throws Exception {
	}

	@Override
	public void saveTo(Configuration config) {
		config.setValue(SupportedProperty.SOLACE_JMS_OAUTH2_ACCESS_TOKEN, getAccessToken());
		config.setValue(SupportedProperty.SOLACE_JMS_OIDC_ID_TOKEN, getIdToken());
	}

	@Override
	public SolaceOAuth2Credentials loadFrom(Configuration config) {
		setAccessToken(config.getValue(SupportedProperty.SOLACE_JMS_OAUTH2_ACCESS_TOKEN, null));
		setIdToken(config.getValue(SupportedProperty.SOLACE_JMS_OIDC_ID_TOKEN, null));
		return this;
	}

	@Override
	public String getPrincipalName() {
		return "";
	}

}
