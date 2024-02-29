package __PACKAGE;

public class Version {

	public static synchronized String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "__VERSION";
	//public static final String SUFFIX = "${suffix}";
	public static final String BUILD_TIMESTAMP = "__BUILD_TIMESTAMP";
	public static final String BUILD_ID = "__VERSION";
}
