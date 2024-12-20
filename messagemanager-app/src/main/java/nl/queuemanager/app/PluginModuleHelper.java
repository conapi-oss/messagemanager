package nl.queuemanager.app;

import java.io.*;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates the plugin Module Layer (Java 9+ modules) and filters out packages if needed
 *
 * @author Stefan Fritz (stefan@conapi.at)
 *
 */
public class PluginModuleHelper {

    public static ClassLoader findFirstModuleClassLoader(final ModuleLayer moduleLayer) {
        final String firstLoadedModule = moduleLayer.modules().stream().findFirst().get().getName();
        final ClassLoader classLoader = moduleLayer.findLoader(firstLoadedModule);
        return classLoader;
    }

    // packages which are known to cause issues, we will filter them out
    // TODO: potentially make this configurable
    // IMPORTANT: due to jar name adjustments (deriveModuleName) the jar name must be regex to match the name before and after the adjustment
    // sonic_Client --> sonicclient --> sonic.*lient
    private static List<String> packagesToBeFiltered = Arrays.asList("javax.jms","sonic.*lient:com.sonicsw.security.ssl") ;
    public static ModuleLayer createPluginModuleLayer(final List<URL> urls, final ModuleLayer parentLayer, final ClassLoader parentClassLoader) {//){
        final Set<Path> jarPaths;
        try {
            final String tempFolder = Files.createTempDirectory("pluginJars").toString();
            // get a list of all the JARs and copy them to a temp location
            jarPaths = copyJarsToTemporaryLocation(urls, tempFolder);
            // now check all the found modules and see if any bad packages are in an unnamed module
            final Set<Path> badJars = new HashSet<>();
            separateBadJars(badJars, jarPaths);
            if (!badJars.isEmpty()){
                // Jars found with packages to be removed
                // as these are automatic modules we will merge/repackage them and remove the bad packages
                //jarPaths.add(mergeJars(badJars, tempFolder));
                repackageJars(badJars);
                jarPaths.addAll(badJars);
            }

        } catch (IOException e) {
            throw new PluginManagerException("Unable to create temporary directory for plugin module JARs", e);
        }

        final ModuleFinder moduleFinder = ModuleFinder.of(jarPaths.toArray(Path[]::new));
        // Create a Configuration based on the ModuleReferencess

        // this does not always work, especially if more than a boot layer exists
        //ModuleLayer parentLayer = ModuleLayer.boot();
        //final ModuleLayer parentLayer = parentClass.getModule().getLayer();
        //ClassLoader parentClassLoader = parentClass.getClassLoader();

        //ModuleNames to be loaded
        final Set<String>  moduleNames       = moduleFinder.findAll().stream().map(moduleRef -> moduleRef.descriptor().name()).collect(Collectors.toSet());
        final Configuration configuration = parentLayer.configuration().resolveAndBind(moduleFinder, ModuleFinder.of(),moduleNames);//List.of());
        // Create a ModuleLayer based on the Configuration
        final ModuleLayer pluginLayer = parentLayer.defineModulesWithOneLoader(configuration, parentClassLoader );//ClassLoader.getSystemClassLoader());
        return pluginLayer;
    }
    private static void repackageJars(final Set<Path> badJars) throws IOException {
        for (Path jar : badJars) {
            repackageFilteredJar(jar);
        }
    }
    private static void repackageFilteredJar(final Path inputJar) throws IOException {
        final Path outputJar = Path.of(inputJar + ".filtered");
        try (JarOutputStream zs = new JarOutputStream(Files.newOutputStream(outputJar)); JarFile inputJarFile = new JarFile(inputJar.toFile())) {
            try (Stream<? extends JarEntry> entries = inputJarFile.stream()) {
                entries
                        .filter( entry -> !isInsideBadPackage(entry, inputJar.getFileName().toString().replaceAll(".jar$","") ) || entry.isDirectory())
                        .forEach(entry -> {
                            try {
                                zs.putNextEntry(entry);
                                inputJarFile.getInputStream(entry).transferTo(zs);
                                zs.closeEntry();
                            } catch (IOException e) {
                                throw  new RuntimeException(e);
                            }
                });
            }
        }
        Files.delete(inputJar);
        Files.move(outputJar,inputJar);
    }

    private static boolean isInsideBadPackage(final JarEntry entry, final String jarName) {
        if(!entry.isDirectory()){
            // this is a file
            String name = entry.getName();
            int lastIndex = name.lastIndexOf("/");
            if(lastIndex>0) {
                // not a file in the root folder
                name = name.substring(0, lastIndex);
                final String packageName = name.replace("/", ".");
                return isPackageToBeFiltered(jarName,packageName);
            }
        }
        return false;
    }

    private static boolean isPackageToBeFiltered(final String jarName, final String packageName) {
        for(String badPackage :packagesToBeFiltered) {
            if(badPackage.contains(":")){
                // check also jar name
                final String[] badPackageInfo = badPackage.split(":");
                if(jarName.matches(badPackageInfo[0]) && badPackageInfo[1].equals(packageName))
                    return true;
            }
            else{
                if(badPackage.equals(packageName))
                    return true;
            }
        }
        return false;
    }

    private static void separateBadJars(final Set<Path> badJars, final Set<Path> jarPaths) {
        // let the modulefinder process them
        final ModuleFinder tempFinder = ModuleFinder.of(jarPaths.toArray(Path[]::new));
        tempFinder.findAll().forEach(m -> {
            m.descriptor().packages().forEach( p -> {
                if(isPackageToBeFiltered(m.descriptor().name(),p)){ // for automatic modules the jar and module name are identical
                    if(m.descriptor().isAutomatic()){
                        // automatic module name --> legacy
                        final Path badJar = Paths.get(m.location().get());
                        badJars.add(badJar);
                        jarPaths.remove(badJar);
                    }
                    else {
                        // this should not happen!
                        throw new PluginManagerException("Module '" + m.descriptor().name()+ "' contains filtered package: " + p );
                    }
                }
            });
        });
    }

    private static Set<Path> copyJarsToTemporaryLocation(final List<URL> urls, final String tempFolder) throws IOException {
        final Set<Path> jarPaths = new HashSet<>();
        for(URL url: urls) {
            //replace any @MM_HOME@ placeholder with the actual value of the MM_HOME environment variable
            String jarUrl = url.toString();
            jarUrl = fixJarUrl(jarUrl);

            final Path path = Paths.get(URI.create(jarUrl));
            final Path tempFolderPath = Paths.get(tempFolder);
            final String derivedModuleName = deriveModuleName(path.getFileName().toString());
            final Path newJarPath = Files.copy(path, tempFolderPath.resolve(derivedModuleName), StandardCopyOption.REPLACE_EXISTING);

            //final Path newJarPath = Files.copy(path, Paths.get(tempFolder,path.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
            jarPaths.add(newJarPath);
        }
        return jarPaths;
    }

    /**
     * Ensures that the Jar file URLs are in proper format.
      * @param jarUrl
     * @return
     */
    public static String fixJarUrl(String jarUrl) {
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
            jarUrl = jarUrl.replace("file:/@MM_HOME@", "file:///@MM_HOME@");
            jarUrl = jarUrl.replace("@MM_HOME@", System.getenv("MM_HOME"));
            jarUrl = jarUrl.replace("\\", "/");
        }
        else{
            // MacOs and Linux only // as the MM_HOME will have / from root
            jarUrl = jarUrl.replace("file:/@MM_HOME@", "file://@MM_HOME@");
            jarUrl = jarUrl.replace("@MM_HOME@", System.getenv("MM_HOME"));
            // remove escape characters
            jarUrl = jarUrl.replace("\\", "");
        }
        // whitespace in path
        jarUrl = jarUrl.replace(" ", "%20");

        return jarUrl;
    }

    /**
     * This ensures that the jar name (= automatic module name) is valid
     * @param jarFileName
     * @return
     */
    static String deriveModuleName(String jarFileName) {

        // Remove .jar extension
        String name = jarFileName.replaceAll("\\.jar$", "");

        // Remove version-like information
        name = name.replaceAll("-\\d+([._-]\\d+)*([._-][a-zA-Z0-9]+)?", "");

        // Replace remaining non-alphanumeric characters with dots
        name = name.replaceAll("[^A-Za-z0-9]", ".");

        // Remove leading/trailing dots and collapse multiple dots
        name = name.replaceAll("^\\.|\\.$", "").replaceAll("\\.{2,}", ".");

        // Ensure the name starts with a letter
        if (!Character.isLetter(name.charAt(0))) {
            name = "m" + name;
        }

        return name.toLowerCase() + ".jar";
    }
}
