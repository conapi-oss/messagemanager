package at.conapi.messagemanager.bootstrap;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

public class AppProperties {

    private final static String PROP_AUTO_UPDATE = "autoupdate";
    private final static String PROP_UPDATE_URL = "updateurl";

    private static Path propertiesLocation;
    private static Properties appProperties = new Properties();
    private static String updateUrl;
    private static Boolean autoUpdate;

    static{

        propertiesLocation = Path.of("", "launcher.properties");

        loadProperties();
        autoUpdate = Boolean.valueOf(appProperties.getProperty(PROP_AUTO_UPDATE, "false"));
        updateUrl = appProperties.getProperty(PROP_UPDATE_URL,"https://product.conapi.at/messagemanager/app/config.xml");//http://localhost/messagemanager/app/config.xml");
        saveProperties(); // to ensure we have a file next time, even if it has just defaults
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

}
