package nl.queuemanager.core;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.0.3";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Tue Jul 30 15:43:30 CEST 2024";
	public static final String BUILD_ID = "4.0.3";
}
