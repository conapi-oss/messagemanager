package nl.queuemanager.app;

import java.io.*;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Stream;

public class PluginModuleHelper {

    private static List<String> packagesToBeFiltered = Arrays.asList("javax.jms") ;
    public static ModuleLayer createModuleClassLoader(final List<URL> urls, final ClassLoader parentClassloader) {
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
                // as these are automatic modules we will merge/repackage them and remove the bad packages
                //jarPaths.add(mergeJars(badJars, tempFolder));
                repackageJars(badJars, tempFolder);
                jarPaths.addAll(badJars);
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
        ModuleLayer moduleLayer = parentLayer.defineModulesWithOneLoader(configuration, parentClassloader );//ClassLoader.getSystemClassLoader());

        // Derive the automatic module name from the JAR file name
        //String automaticModuleName = jarPaths.get(0).getFileName().toString().replace(".jar", "");
        //return moduleLayer.findLoader(automaticModuleName);
        return moduleLayer;
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

    private static void repackageJars(List<Path> badJars, String tempFolder) throws IOException {
        final Path extractedDir = Files.createDirectory(Paths.get(tempFolder, "extracted"));
        // extract all bad Jars to the new directory
        for (Path jar : badJars) {
            long start = System.currentTimeMillis();
            /*extractJar(jar, extractedDir.toString());
            long extract = System.currentTimeMillis();
            // now delete the bad Jars
            Files.deleteIfExists(jar);
            long delete = System.currentTimeMillis();

            removeFilteredPackages(extractedDir);
            long remove = System.currentTimeMillis();

            // repackage it
            repackageJar(extractedDir, jar);*/
            repackageFilteredJar(jar);
            long repackage = System.currentTimeMillis();
System.out.println(repackage-start);
/*            long timeToExtract = extract-start;
            long timeToDelete = delete-extract;
            long timeToRemove = remove-delete;
            long timeToRepackage = repackage-remove;
            System.out.printf("Extract: %s, Delete: %s, Remove: %s, Repackage: %s", timeToExtract,timeToDelete,timeToRemove,timeToRepackage);*/

        }
    }

    private static void repackageJar(Path extractedDir, Path jar) throws IOException {
        try (JarOutputStream zs = new JarOutputStream(Files.newOutputStream(jar));
             Stream<Path> paths = Files.walk(extractedDir)) {
            paths
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        JarEntry zipEntry = new JarEntry(extractedDir.relativize(path).toString().replace("\\", "/"));
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }
    }

    private static void repackageFilteredJar(final Path inputJar) throws IOException {
        final Path outputJar = Path.of(inputJar + ".filtered");
        try (JarOutputStream zs = new JarOutputStream(Files.newOutputStream(outputJar)); JarFile inputJarFile = new JarFile(inputJar.toFile())) {
            // Get a stream of Entries
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

    private static void removeFilteredPackages(Path extractedDir) throws IOException {
        // delete all the bad packages, directory has to be empty
        for (String p : packagesToBeFiltered) {
            // we need to delete all the empty folders
            final String packagePathString = p.replace(".", File.separator);
            /// delete up to the mergedDir level
            Path dirToDelete = Paths.get(extractedDir.toString(), packagePathString);
            do {
                final Path parent = dirToDelete.getParent();
                if(Files.list(dirToDelete).count()==0){
                    // only delete emtpy directories
                    Files.deleteIfExists(dirToDelete);
                }
                // remove last entry
                dirToDelete = parent;
            }
            while(!dirToDelete.equals(extractedDir));
        }
    }


    private static void extractJar(Path jarPath, String destDir) throws IOException {
        JarFile jar = new JarFile(jarPath.toFile());
        Enumeration<JarEntry> enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry entry = enumEntries.nextElement();

            if(/*entry.getName().contains("META-INF") ||*/ isInsideBadPackage(entry)){
                // skip meta information
                continue;
            }

            java.io.File f = new java.io.File(destDir + java.io.File.separator + entry.getName());
            if (entry.isDirectory()) {
                // if its a directory, create it
                f.mkdir();
                continue;
            }

            try(InputStream is = jar.getInputStream(entry)){
                Files.copy(is, f.toPath());
            }
        }
        jar.close();
    }

    private static boolean isInsideBadPackage(JarEntry entry) {
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
            //final Path newJarPath = Files.copy(path, Paths.get(tempFolder,deriveModuleName(path.getFileName().toString())), StandardCopyOption.REPLACE_EXISTING);
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
    }

     */
}
