package nl.queuemanager.app;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.2.0";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Tue Oct 28 15:40:24 CET 2025";
	public static final String BUILD_ID = "4.2.0";
}
