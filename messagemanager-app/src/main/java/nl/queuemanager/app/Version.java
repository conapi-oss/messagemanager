package nl.queuemanager.app;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.0-SNAPSHOT";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Fri May 17 16:18:30 CEST 2024";
	public static final String BUILD_ID = "4.0-SNAPSHOT";
}
