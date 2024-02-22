package nl.queuemanager.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginModuleHelper {

    private static List<String> packagesToBeFiltered = Arrays.asList("javax.jms") ;
    public static ClassLoader createModuleClassLoader(final List<URL> urls, final ClassLoader parenClassloader) {
        List<Path> jarPaths;
        try {
            final String tempFolder = Files.createTempDirectory("pluginJars").toString();
            // get a list of all the JARs and copy them to a temp location
            jarPaths = copyJarsToTemporaryLocation(urls, tempFolder);
            // now check all the found modules and see if any bad packages are in an unnamed module
            List<Path> badJars = new ArrayList<>();
            separateBadJars(badJars, jarPaths);
            if (!badJars.isEmpty()){
                // Jars found with packages to be removed
                // as these are automatic modules we will merge them and remove the bad packages
                jarPaths.add(mergeJars(badJars, tempFolder));
            }

        } catch (IOException e) {
            throw new PluginManagerException("Unable to create temporary directory for plugin module JARs", e);
        }

        ModuleFinder moduleFinder = ModuleFinder.of(jarPaths.toArray(Path[]::new));

        // Create a list of ModuleReferences
        //List<ModuleReference> moduleReferences = new ArrayList<>();
        //moduleFinder.findAll().forEach(moduleReferences::add);

        // Create a Configuration based on the ModuleReferencess
        Configuration configuration = ModuleLayer.boot().configuration().resolveAndBind(moduleFinder, ModuleFinder.of(),List.of());

        // Create a ModuleLayer based on the Configuration
        ModuleLayer parentLayer = ModuleLayer.boot();
        ModuleLayer moduleLayer = parentLayer.defineModulesWithOneLoader(configuration, parenClassloader );//ClassLoader.getSystemClassLoader());

        // Derive the automatic module name from the JAR file name
        String automaticModuleName = jarPaths.get(0).getFileName().toString().replace(".jar", "");

        return moduleLayer.findLoader(automaticModuleName);
    }

    private static Path mergeJars(List<Path> badJars, String tempFolder) throws IOException {
        final Path mergedDir = Files.createDirectory(Paths.get(tempFolder, "merged"));
        // extract all bad Jars to the new directory
        for (Path jar : badJars) {
            extractJar(jar, mergedDir.toString());
            // now delete the bad Jars
            Files.deleteIfExists(jar);
        }

        // delete all the bad packages, directory has to be empty
        for (String p : packagesToBeFiltered) {
            // we need to delete all the empty folders
            final String packagePathString = p.replace(".", File.separator);
            /// delete up to the mergedDir level
            Path dirToDelete = Paths.get(mergedDir.toString(), packagePathString);
            do {
                final Path parent = dirToDelete.getParent();
                if(Files.list(dirToDelete).count()==0){
                    // only delete emtpy directories
                    Files.deleteIfExists(dirToDelete);
                }
                // remove last entry
                dirToDelete = parent;
            }
            while(!dirToDelete.equals(mergedDir));
        }

        return mergedDir;
    }


    private static void extractJar(Path jarPath, String destDir) throws IOException {
        JarFile jar = new JarFile(jarPath.toFile());
        Enumeration<JarEntry> enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry entry = enumEntries.nextElement();

            if(entry.getName().contains("META-INF") || isInsideBadpackage(entry)){
                // skip meta information
                continue;
            }

            java.io.File f = new java.io.File(destDir + java.io.File.separator + entry.getName());
            if (entry.isDirectory()) {
                // if its a directory, create it
                f.mkdir();
                continue;
            }

            try(    InputStream is = jar.getInputStream(entry); // get the input stream
                    FileOutputStream fos = new java.io.FileOutputStream(f)) {
                while (is.available() > 0) {  // write contents of 'is' to 'fos'
                    fos.write(is.read());
                }
            }
        }
        jar.close();
    }

    private static boolean isInsideBadpackage(JarEntry entry) {
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

    private static void separateBadJars(List<Path> badJars, List<Path> jarPaths) {
        // let the modulefinder process them
        ModuleFinder tempFinder = ModuleFinder.of(jarPaths.toArray(Path[]::new));

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

    private static List<Path> copyJarsToTemporaryLocation(List<URL> urls, String tempFolder) throws IOException {
        final List<Path> jarPaths = new ArrayList<>();
        for(URL url: urls) {
            final Path path = Paths.get(URI.create(url.toString()));
            final Path newJarPath = Files.copy(path, Paths.get(tempFolder,deriveModuleName(path.getFileName().toString())), StandardCopyOption.REPLACE_EXISTING);
            jarPaths.add(newJarPath);
        }
        return jarPaths;
    }

    /**
     * This ensures that the jar name (= automatic module name) is valid
     * @param jarFileName
     * @return
     */
    private static String deriveModuleName(String jarFileName) {
        // Replace non-alphanumeric characters with underscores
        String sanitized = jarFileName.replaceAll(".jar$", "");
        sanitized = sanitized.replaceAll("[^A-Za-z0-9]", "");

        // Ensure the module name starts with a letter
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "x" + sanitized;
        }

        return sanitized + ".jar";
    }
}
