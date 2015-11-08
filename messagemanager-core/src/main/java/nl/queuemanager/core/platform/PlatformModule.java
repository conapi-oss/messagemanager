package nl.queuemanager.core.platform;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class PlatformModule extends AbstractModule {

	@Override
	protected void configure() {
		if(isOSX()) {
			bind(PlatformHelper.class).to(PlatformHelperOSX.class).in(Scopes.SINGLETON);
		} else if(isWindows()) {
			bind(PlatformHelper.class).to(PlatformHelperWindows.class).in(Scopes.SINGLETON);
		} else {
			// Use default PlatformHelper
		}
	}
	
	// From https://developer.apple.com/library/mac/technotes/tn2002/tn2110.html
	private static boolean isOSX() {
	    String osName = System.getProperty("os.name");
	    return osName.toUpperCase().contains("OS X");
	}
	
	private static boolean isWindows() {
	    String osName = System.getProperty("os.name");
	    return osName.toUpperCase().contains("WINDOWS");
	}

}
