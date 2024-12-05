package nl.queuemanager.app;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.1.1";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Thu Dec 05 11:39:39 CET 2024";
	public static final String BUILD_ID = "4.1.1";
}
