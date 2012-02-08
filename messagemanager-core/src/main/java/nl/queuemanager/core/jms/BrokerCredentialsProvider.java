package nl.queuemanager.core.jms;

import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;

/**
 * Provides credentials for accessing a JMS Broker when they are required. This may happen by asking the
 * user, loading them from a configuration file or they may be hardcoded. Whichever is appropriate.
 * 
 * @author gerco
 *
 */
public interface BrokerCredentialsProvider {

	/**
	 * Get credentials for a broker. 
	 * 
	 * @param broker The broker for which credentials are required
	 * @param def The default credentials (if any)
	 * @param exception The exception that was encountered on the previous connection attempt (if any)
	 * @return Credentials object or null when no further connection attempts should be made
	 */
	public abstract Credentials getCredentials(JMSBroker broker, Credentials def, Exception exception);
	
}
