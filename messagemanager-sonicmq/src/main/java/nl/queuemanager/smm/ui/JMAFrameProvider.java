package nl.queuemanager.smm.ui;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.sonicsw.ma.gui.util.JMAFrame;

/**
 * Provides a JMAFrame reference
 * 
 * @author gerco
 *
 */
public class JMAFrameProvider implements Provider<JMAFrame> {
	private final Injector injector;
	
	@Inject
	public JMAFrameProvider(Injector injector) {
		this.injector = injector;
	}

	/**
	 * Retrieve the SMMFrame reference from the Injector. We retrieve the SMMFrame class
	 * directly instead of JMAFrame because Guice will otherwise not be able to find an
	 * instance of JMAFrame.
	 */
	public JMAFrame get() {
		return injector.getInstance(SMMFrame.class);
	}
	
}
