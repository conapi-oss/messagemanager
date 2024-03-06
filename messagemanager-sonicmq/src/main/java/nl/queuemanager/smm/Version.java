package nl.queuemanager.smm;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.0-SNAPSHOT";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Wed Mar 06 13:03:42 CET 2024";
	public static final String BUILD_ID = "4.0-SNAPSHOT";
}
