package nl.queuemanager.smm;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.0-SNAPSHOT";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Thu Mar 14 13:26:25 CET 2024";
	public static final String BUILD_ID = "4.0-SNAPSHOT";
}
