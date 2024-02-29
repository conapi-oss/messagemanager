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
import java.util.stream.Stream;

/**
 * Creates the plugin Module Layer (Java 9+ modules) and filters out packages if needed
 *
 * @author Stefan Fritz (stefan@conapi.at)
 *
 */
public class PluginModuleHelper {

    // packages which are known to cause issues, we will filter them out
    // TODO: potentially make this configurable
    private static List<String> packagesToBeFiltered = Arrays.asList("javax.jms") ;
    public static ModuleLayer createPluginModuleLayer(final List<URL> urls, final ClassLoader parentClassloader) {
        final List<Path> jarPaths;
        try {
            final String tempFolder = Files.createTempDirectory("pluginJars").toString();
            // get a list of all the JARs and copy them to a temp location
            jarPaths = copyJarsToTemporaryLocation(urls, tempFolder);
            // now check all the found modules and see if any bad packages are in an unnamed module
            final List<Path> badJars = new ArrayList<>();
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
        final Configuration configuration = ModuleLayer.boot().configuration().resolveAndBind(moduleFinder, ModuleFinder.of(),List.of());
        // Create a ModuleLayer based on the Configuration
        return ModuleLayer.boot().defineModulesWithOneLoader(configuration, parentClassloader );//ClassLoader.getSystemClassLoader());
    }
    private static void repackageJars(final List<Path> badJars) throws IOException {
        for (Path jar : badJars) {
            repackageFilteredJar(jar);
        }
    }
    private static void repackageFilteredJar(final Path inputJar) throws IOException {
        final Path outputJar = Path.of(inputJar + ".filtered");
        try (JarOutputStream zs = new JarOutputStream(Files.newOutputStream(outputJar)); JarFile inputJarFile = new JarFile(inputJar.toFile())) {
            try (Stream<? extends JarEntry> entries = inputJarFile.stream()) {
                entries
                        .filter( entry -> !isInsideBadPackage(entry) || entry.isDirectory())
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

    private static boolean isInsideBadPackage(final JarEntry entry) {
        if(!entry.isDirectory()){
            // this is a file
            String name = entry.getName();
            int lastIndex = name.lastIndexOf("/");
            if(lastIndex>0) {
                // not a file in the root folder
                name = name.substring(0, lastIndex);
                final String packageName = name.replace("/", ".");
                return packagesToBeFiltered.contains(packageName);
            }
        }
        return false;
    }

    private static void separateBadJars(final List<Path> badJars, final List<Path> jarPaths) {
        // let the modulefinder process them
        final ModuleFinder tempFinder = ModuleFinder.of(jarPaths.toArray(Path[]::new));
        tempFinder.findAll().forEach(m -> {
            m.descriptor().packages().forEach( p -> {
                if(packagesToBeFiltered.contains(p)){
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

    private static List<Path> copyJarsToTemporaryLocation(final List<URL> urls, final String tempFolder) throws IOException {
        final List<Path> jarPaths = new ArrayList<>();
        for(URL url: urls) {
            final Path path = Paths.get(URI.create(url.toString()));
            final Path newJarPath = Files.copy(path, Paths.get(tempFolder,path.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
            jarPaths.add(newJarPath);
        }
        return jarPaths;
    }

    /**
     * This ensures that the jar name (= automatic module name) is valid
     * @param jarFileName
     * @return
     */
    /*private static String deriveModuleName(String jarFileName) {
        // Replace non-alphanumeric characters with underscores
        String sanitized = jarFileName.replaceAll(".jar$", "");
        sanitized = sanitized.replaceAll("[^A-Za-z0-9]", "");

        // Ensure the module name starts with a letter
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "x" + sanitized;
        }

        return sanitized + ".jar";
    } */
}
