package nl.queuemanager.core.configuration;

import java.util.List;

public interface Configuration {

	/**
	 * Store a configuration value
	 * 
	 * @param key The key to store the value for
	 * @param value The value to store
	 */
	public void setValue(String key, String value);

	/**
	 * Retrieve a configured value or the default if none is saved.
	 * 
	 * @param key The key to retrieve the value for
	 * @param def The default value to return if no value was stored.
	 * @return 
	 */
	public String getValue(String key, String def);
	
	/**
	 * Set an attribute for this configuration section
	 * 
	 * @param name The name of the attribute
	 * @param value The value of the attribute
	 */
	public void setAttr(String name, String value);
	
	/**
	 * Retrieve a configured attribute or the default if none is saved.
	 * 
	 * @param key The attribute name to retrieve the value for
	 * @param def The default value to return if no value was stored.
	 * @return 
	 */
	public String getAttr(String name, String def);

	/**
	 * Get a list of the configuration keys in this section
	 */
	public List<String> listKeys();
	
	/**
	 * Retrieve a sub configuration object for the specified key. All methods
	 * called on that object will take effect only on the configuration subset
	 * inside that key.
	 * <p>
	 * The configuration section for this key will be created if it doesn't exist
	 * as soon as any values are saved inside it.
	 * 
	 * @param key
	 * @return
	 */
	public Configuration sub(String key);
	
	/**
	 * Remove a configuration sub object or key in a Configuration object
	 * 
	 * @param key
	 */
	public void del(String key);
	
}
