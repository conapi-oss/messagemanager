package nl.queuemanager.solace;

import java.io.File;

import javax.jms.ConnectionFactory;

import com.google.common.base.Strings;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SupportedProperty;

import lombok.Getter;
import lombok.Setter;
import nl.queuemanager.core.configuration.Configuration;
import nl.queuemanager.core.util.Credentials;

class SolaceClientCertificateCredentials implements Credentials {

	@Getter @Setter private File keyStoreFile;
	@Getter @Setter private String keyStorePassword;
	@Getter @Setter private String privateKeyAlias;
	@Getter @Setter private String privateKeyPassword;

	@Override
	public void apply(ConnectionFactory cf) throws Exception {
		if(!(cf instanceof SolConnectionFactory)) {
			throw new IllegalArgumentException("cf");
		}
		
		SolConnectionFactory scf = (SolConnectionFactory)cf;
		scf.setSSLKeyStore(getKeyStoreFile().getAbsolutePath());
		scf.setSSLKeyStoreFormat("JKS"); // TODO Auto detect keystore format
		scf.setSSLKeyStorePassword(getKeyStorePassword());
		scf.setSSLPrivateKeyAlias(getPrivateKeyAlias());
		scf.setSSLPrivateKeyPassword(getPrivateKeyPassword());
	}

	@Override
	public void saveTo(Configuration config) {
		if(getKeyStoreFile() != null) {
			config.setValue(SupportedProperty.SOLACE_JMS_SSL_KEY_STORE, getKeyStoreFile().getAbsolutePath());
		} else {
			config.del(SupportedProperty.SOLACE_JMS_SSL_KEY_STORE);
		}
		config.setValue(SupportedProperty.SOLACE_JMS_SSL_KEY_STORE_PASSWORD, getKeyStorePassword());
		config.setValue(SupportedProperty.SOLACE_JMS_SSL_PRIVATE_KEY_ALIAS, getPrivateKeyAlias());
		config.setValue(SupportedProperty.SOLACE_JMS_SSL_PRIVATE_KEY_PASSWORD, getPrivateKeyPassword());
	}

	@Override
	public SolaceClientCertificateCredentials loadFrom(Configuration config) {
		String tmpKeyStoreFile = config.getValue(SupportedProperty.SOLACE_JMS_SSL_KEY_STORE, null);
		if(!Strings.isNullOrEmpty(tmpKeyStoreFile)) {
			setKeyStoreFile(new File(tmpKeyStoreFile));
		}
		setKeyStorePassword(config.getValue(SupportedProperty.SOLACE_JMS_SSL_KEY_STORE_PASSWORD, null));
		setPrivateKeyAlias(config.getValue(SupportedProperty.SOLACE_JMS_SSL_PRIVATE_KEY_ALIAS, null));
		setPrivateKeyPassword(config.getValue(SupportedProperty.SOLACE_JMS_SSL_PRIVATE_KEY_PASSWORD, null));
		return this;
	}

	@Override
	public String getPrincipalName() {
		return getPrivateKeyAlias();
	}

}
