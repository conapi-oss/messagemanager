package nl.queuemanager.core;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.1.0";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Wed Oct 16 16:16:18 CEST 2024";
	public static final String BUILD_ID = "4.1.0";
}
