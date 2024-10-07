package at.conapi.messagemanager.bootstrap;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

public class AppProperties {

    private final static String PROP_AUTO_UPDATE = "autoupdate";
    private final static String PROP_UPDATE_URL = "updateurl";
    private final static String PROP_CONNECT_TIMEOUT = "connecttimeout";
    private final static String PROP_READ_TIMEOUT = "readtimeout";
    private final static String PROP_UPDATE_FREQUENCY = "updatefrequency"; //in in days
    private final static String PROP_FAILED_UPDATES = "failedupdates";
    private final static String PROP_LAST_SUCCESSFUL_UPDATE_CHECK = "lastsuccessfulupdatecheck";

    private static Path propertiesLocation;
    private static Properties appProperties = new Properties();
    private static String updateUrl;
    private static Boolean autoUpdate;
    private static Integer connectTimeout;
    private static Integer readTimeout;
    private static Integer updateFrequency;
    private static Integer failedUpdates;
    private static Long lastSuccessfulUpdateCheck;

    static{
        propertiesLocation = Path.of("", "launcher.properties");

        loadProperties();
        autoUpdate = Boolean.valueOf(appProperties.getProperty(PROP_AUTO_UPDATE, "false"));

        // check if there is an override configured, otherwise take env variable and defaults
        updateUrl = appProperties.getProperty(PROP_UPDATE_URL);
        if(updateUrl==null) {
            // seems there is no override, let's figure the app update url out
            updateUrl = System.getenv("UPDATE_URL");
            if (updateUrl == null) {
                // use default
                updateUrl = appProperties.getProperty(PROP_UPDATE_URL, "https://files.conapi.at/mm/stable/app/config.xml");//http://localhost/messagemanager/app/config.xml");
            } else {
                //strip any quotes
                updateUrl = updateUrl.replaceAll("\"", "");
                updateUrl = updateUrl.replaceAll("'", "");
                // https://files.conapi.at/mm/stable/setup.xml
                updateUrl = updateUrl.replaceAll("setup.xml", "app/config.xml");
            }
        }
        connectTimeout = Integer.valueOf(appProperties.getProperty(PROP_CONNECT_TIMEOUT, "5000"));
        readTimeout = Integer.valueOf(appProperties.getProperty(PROP_READ_TIMEOUT, "5000"));
        updateFrequency = Integer.valueOf(appProperties.getProperty(PROP_UPDATE_FREQUENCY, "7"));
        failedUpdates = Integer.valueOf(appProperties.getProperty(PROP_FAILED_UPDATES, "0"));
        lastSuccessfulUpdateCheck = Long.valueOf(appProperties.getProperty(PROP_LAST_SUCCESSFUL_UPDATE_CHECK, "0"));

        saveProperties(); // to ensure we have a file next time, even if it has just defaults
    }

    public static boolean isStableRelease(){
        return updateUrl.contains("/stable/");
    }

    private static void loadProperties(){
        try {
            appProperties.load(new FileInputStream(propertiesLocation.toFile()));
        } catch (IOException e) {
            if(e instanceof FileNotFoundException){
                System.out.println("Using defaults, could not load : " + propertiesLocation.toFile());
            }
            else {
                e.printStackTrace();
            }
            // we use defaults
        }
    }

    private static void saveProperties(){
        try {
            //appProperties.setProperty(PROP_UPDATE_URL, updateUrl); better not to save this to allow new app new version
            appProperties.setProperty(PROP_AUTO_UPDATE, autoUpdate.toString());
            appProperties.setProperty(PROP_UPDATE_FREQUENCY, updateFrequency.toString());
            appProperties.setProperty(PROP_FAILED_UPDATES, failedUpdates.toString());
            appProperties.setProperty(PROP_LAST_SUCCESSFUL_UPDATE_CHECK, lastSuccessfulUpdateCheck.toString());

            appProperties.store(new FileWriter(propertiesLocation.toFile()), new Date().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUpdateUrl(){
        return updateUrl;
    }

    public static Boolean isAutoUpdate() {
        return autoUpdate;
    }

    public static void setAutoUpdate(boolean newAutoUpdate){
        autoUpdate = newAutoUpdate;
        saveProperties();
    }
    public static int getReadTimeout(){
        return readTimeout;
    }

    public static int getConnectTimeout(){
        return connectTimeout;
    }

    public static int getUpdateFrequency(){
        return updateFrequency;
    }

    public static int getFailedUpdates(){
        return failedUpdates;
    }

    public static long getLastSuccessfulUpdateCheck(){
        return lastSuccessfulUpdateCheck;
    }

    public static void setLastSuccessfulUpdateCheck(long lastSuccessfulUpdateCheck){
        AppProperties.lastSuccessfulUpdateCheck = lastSuccessfulUpdateCheck;
        saveProperties();
    }

    public static void incrementFailedUpdates(){
        failedUpdates++;
        saveProperties();
    }

    public static void resetFailedUpdates(){
        failedUpdates = 0;
        saveProperties();
    }
}

