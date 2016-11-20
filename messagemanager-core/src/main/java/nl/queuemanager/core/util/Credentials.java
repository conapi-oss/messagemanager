/**
 * 
 */
package nl.queuemanager.core.util;

import javax.jms.ConnectionFactory;

import nl.queuemanager.core.configuration.Configuration;

public interface Credentials {
	
	public void saveTo(Configuration config);
	
	public Credentials loadFrom(Configuration config);
	
	public void apply(ConnectionFactory cf) throws Exception;
	
	public String getPrincipalName();
	
}