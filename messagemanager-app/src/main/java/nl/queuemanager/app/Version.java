package nl.queuemanager.app;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.0";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Mon Jun 24 15:56:32 CEST 2024";
	public static final String BUILD_ID = "4.0";
}
