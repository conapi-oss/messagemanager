/**
 * 
 */
package nl.queuemanager.core.util;

import javax.jms.ConnectionFactory;

import nl.queuemanager.core.configuration.Configuration;

public interface Credentials {
	
	public void saveTo(Configuration config);
	
	/**
	 * Load a credentials object from the specified configuration. This should really be a static
	 * method but since we can't specify those in interfaces, we'll do it this way. The returned
	 * Credentials may can be different from the invokee and should be used instead of the invokee
	 * in all cases. The state of the invokee is undefined after this method was invoked and it 
	 * should not be used for another invocation of loadFrom().
	 * 
	 * @param config
	 * @return
	 */
	public Credentials loadFrom(Configuration config);
	
	public void apply(ConnectionFactory cf) throws Exception;
	
	public String getPrincipalName();
	
}