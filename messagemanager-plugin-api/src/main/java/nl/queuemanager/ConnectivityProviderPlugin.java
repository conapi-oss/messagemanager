package nl.queuemanager;

/**
 * Interface to implement in modules that provide connectivity to a message broker implementation. This
 * is different from a JMSDomain in the sense that a JMSDomain is used to create the actual connections
 * and this class is mainly for the user interface and module loading to present the user with a choice
 * of installed message broker plugins.
 * 
 * @author gerco
 */
public interface ConnectivityProviderPlugin {

	void initialize();

}
