package nl.queuemanager.app;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.0.3";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Thu Aug 01 11:37:24 CEST 2024";
	public static final String BUILD_ID = "4.0.3";
}
