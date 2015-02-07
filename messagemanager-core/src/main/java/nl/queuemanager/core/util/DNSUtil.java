package nl.queuemanager.core.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;

public class DNSUtil {
	
	public static final Logger log = Logger.getLogger(DNSUtil.class.getName());
	
	/**
	 * Returns first DNS TXT record of the provided hostname. In the case of any errors, returns the empty String.
	 * @param hostName
	 */
	public static String getFirstTxtRecord(String hostname) {
	    java.util.Hashtable<String, String> env = new java.util.Hashtable<String, String>();	
	    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

	    log.fine(String.format("Trying to resolve %s", hostname));
	    
	    try {
	        javax.naming.directory.DirContext dirContext = new javax.naming.directory.InitialDirContext(env);
	        
	        if(dirContext != null) {
		        javax.naming.directory.Attributes attrs = dirContext.getAttributes(hostname, new String[] { "TXT" });
		        
		        if(attrs != null) {
			        javax.naming.directory.Attribute attr = attrs.get("TXT");
		
			        if(attr != null) {
			            return unquote(attr.get().toString());
			        }
		        }
	        }
	    } catch (java.lang.NumberFormatException e) {
	    	log.log(Level.WARNING, String.format("JVM BUG! Unable to handle IPv6 nameservers: See https://bugs.openjdk.java.net/browse/JDK-6991580"), e);
	    } catch (javax.naming.NamingException e) {
	    	log.log(Level.WARNING, String.format("Unable to retrieve first TXT record for %s", hostname), e);
	    }
	    
        return "";
	}
	
	private static String unquote(String str) {
		if(str.startsWith("\"") && str.endsWith("\"") && str.length()>=2) {
			return str.substring(1, str.length()-1);
		}
		return str;
	}
}