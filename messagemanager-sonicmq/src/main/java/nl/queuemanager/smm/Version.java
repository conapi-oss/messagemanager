package nl.queuemanager.smm;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "4.0";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "Mon Jul 22 17:23:22 CEST 2024";
	public static final String BUILD_ID = "4.0";
}
