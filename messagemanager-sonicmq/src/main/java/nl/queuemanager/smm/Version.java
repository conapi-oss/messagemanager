package nl.queuemanager.smm;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.1.1";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Fri Nov 22 08:43:37 CET 2024";
	public static final String BUILD_ID = "4.1.1";
}
