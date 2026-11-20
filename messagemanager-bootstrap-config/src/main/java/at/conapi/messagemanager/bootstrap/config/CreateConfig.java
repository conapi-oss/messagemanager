package at.conapi.messagemanager.bootstrap.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.OS;



public class CreateConfig {

    public static void main(String[] args) throws IOException {

/*
        bootstrapRoot = "${buildDir}/root"
        bootstrapTarget = "${bootstrapRoot}/target"
        bootstrapConfigLocation="${bootstrapTarget}/config"
                */
        final String jarsLocation = args[0]; //maven

        // run .\gradlew :messagemanager-bootstrap:jlink
        final String bootstrapJarsLocation = args[1]; // target
        String configLoc = args[2];
        String build = args[3]; //default: stable
        // The JavaFX version the bootstrap is built with: its one home is javafx { version } in the bootstrap's
        // build.gradle, which passes it here.
        if (args.length < 5 || args[4].isBlank()) {
            throw new IllegalArgumentException("no JavaFX version: createUpdate4JConfig passes javafx.version as the fifth argument");
        }
        javafxVersion = args[4];

        //TODO:  Need to automate config creation per platform + hosting per platform then

        // where to create and create the config


        String baseUrl = "https://files.conapi.at/mm/"+build; //http://localhost/messagemanager";//TODO: replace with proper hosting URL

        String dir = configLoc + "/app";
        Files.createDirectories(Paths.get(dir));

        String clientsDir = configLoc + "/clients";
        Files.createDirectories(Paths.get(clientsDir));

        String readonlyDir = configLoc + "/readonly";
        Files.createDirectories(Paths.get(readonlyDir));

        String pluginExtraLibsDir = configLoc + "/plugins/libs";
        Files.createDirectories(Paths.get(pluginExtraLibsDir));

        Configuration config = Configuration.builder()
                .baseUri(baseUrl + "/app")
                .basePath("${user.dir}/app")
                // app jars
                .files(FileMetadata.streamDirectory(dir)
                        .filter( r -> !r.getSource().toString().endsWith("config.xml"))
                        .peek(r -> r.modulepath(r.getSource().toString().endsWith(".jar"))))

                // plugins
                .files(FileMetadata.streamDirectory(configLoc + "/plugins")
                        .filter( r -> !r.getSource().toString().endsWith("-common.jar"))
                        .filter( r -> r.getSource().getFileName().toString().startsWith("messagemanager-")) // this allows us to exclude the extra plugin jars
                        .peek(f -> f.uri(baseUrl +"/plugins/" + f.getSource().toFile().getName()))
                        .peek( f -> f.path("../plugins/" + f.getSource().toFile().getName())))

                // clients, not put on modulepath nor classpath, including all subfolders. They load in a plugin's own
                // layer, never at boot: Update4J's boot conflict check would only derive a module name from each file
                // name, and one it cannot derive (netty-codec-native-quic: 'native' is a keyword) fails the update.
                .files(FileMetadata.streamDirectory(configLoc + "/clients")
                        .peek(r -> r.modulepath(false))
                        .peek(r -> r.classpath(false))
                        .peek(r -> r.ignoreBootConflict())
                        .peek( f -> {
                            String pathToJar = f.getSource().toFile().getPath();
                            pathToJar = pathToJar.substring(pathToJar.indexOf("clients") + 8);
                            pathToJar = pathToJar.replace("\\", "/");
                            f.path("../clients/" + pathToJar);
                            f.uri(baseUrl +"/clients/" + pathToJar);
                        })
                )
                // the extra plugin jars, , not put on modulepath nor classpath
                .files(FileMetadata.streamDirectory(configLoc + "/plugins/libs")
                        .peek(r -> r.modulepath(false))
                        .peek(r -> r.classpath(false))
                        .peek(r -> r.ignoreBootConflict())
                        .peek( f -> {
                            String pathToJar = f.getSource().toFile().getPath();
                            pathToJar = pathToJar.substring(pathToJar.indexOf("plugins") + 8);
                            pathToJar = pathToJar.replace("\\", "/");
                            f.path("../plugins/" + pathToJar);
                            f.uri(baseUrl +"/plugins/" + pathToJar);
                        })
                )
                // samples etc. , not put on modulepath nor classpath
                .files(FileMetadata.streamDirectory(configLoc + "/readonly")
                        .peek(r -> r.modulepath(false))
                        .peek(r -> r.classpath(false))
                        .peek(r -> r.ignoreBootConflict())
                        .peek( f -> {
                            String pathToJar = f.getSource().toFile().getPath();
                            pathToJar = pathToJar.substring(pathToJar.indexOf("readonly") + 9);
                            pathToJar = pathToJar.replace("\\", "/");
                            f.path("../readonly/" + pathToJar);
                            f.uri(baseUrl +"/readonly/" + pathToJar);
                        })
                )
                // put common plugin jars on modulepath
                .files(FileMetadata.streamDirectory(configLoc + "/plugins")
                        .filter( r -> r.getSource().toString().endsWith("-common.jar"))
                        .peek(f -> f.uri(baseUrl +"/plugins/" + f.getSource().toFile().getName()))
                        //.peek(r -> r.modulepath(r.getSource().toString().endsWith(".jar"))))
                        .peek(r -> r.modulepath().path("../plugins/" + r.getSource().toFile().getName())))
                .property("default.launcher.main.class", "nl.queuemanager.app.Main")
                //default.launcher.argument.<num>
                //default.launcher.system.<key>
                .property("maven.central", MAVEN_BASE)
                .build();


        // ensure directory exits
        // delete the previous config
        Path configLocationPath = Paths.get(dir + "/config.xml");
        Files.deleteIfExists(configLocationPath);
        Files.createDirectories(Paths.get(dir));
        try (Writer out = Files.newBufferedWriter(configLocationPath, StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW)) {
            config.write(out);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        config = null;

        // bootstrap config

        String cacheLoc = jarsLocation+ "/fxcache";
        dir = configLoc + "/bootstrap";

        cacheJavafx(jarsLocation, bootstrapJarsLocation);

        Configuration setup = Configuration.builder()
                .basePath("${user.dir}/bootstrap")

                // once removed this as it caused file size warnings, BUT it is needed to allow the app to work offline!
                .file(FileMetadata.readFrom(configLocationPath)//dir + "/../app/config.xml") // fall back if no internet
                        .uri(baseUrl + "/app/config.xml")
                        .path("../app/config.xml"))

                //.file(FileMetadata.readFrom(dir + "/messagemanager-bootstrap-1.0.jar")
                //        .modulepath()
                 //       .uri(baseUrl +"/bootstrap/messagemanager-bootstrap-1.0.jar"))

                /*.files(FileMetadata.streamDirectory(dir)
                        .peek(r -> r.modulepath(r.getSource().toString().endsWith(".jar"))
                                .uri(baseUrl +"/bootstrap/" + r.getSource().toFile().getName()).ignoreBootConflict()
                        ))

                 */

                // special handling for update4j
                .files(FileMetadata.streamDirectory(dir)
                        .filter( fm -> fm.getSource().getFileName().toFile().toString().startsWith("update4j"))
                      //  .peek(f -> f.modulepath()) // anyway only update4j-<version>.jar
                        .peek(f -> f.uri(baseUrl +"/bootstrap/" + f.getSource().toFile().getName()))
                        .peek(f -> f.ignoreBootConflict())
                        .peek( f -> f.path("update4j.jar.new"))
                )

                // any other jar (for now just the bootstrap jar)
                .files(FileMetadata.streamDirectory(dir)
                        .filter( fm -> !fm.getSource().getFileName().toFile().toString().startsWith("update4j"))
                        .peek(f -> f.modulepath(f.getSource().toString().endsWith(".jar")))
                        .peek(f -> f.uri(baseUrl +"/bootstrap/" + f.getSource().toFile().getName()))
                        .peek(f -> f.ignoreBootConflict()))

                // launch scripts, still keep these here as duplicate as a last resort option if app update fails
                .files(FileMetadata.streamDirectory(configLoc + "/bin")
                        .filter( fm -> !fm.getSource().getFileName().toFile().toString().startsWith("update4j"))
                        .peek(f -> f.uri(baseUrl +"/bin/" + f.getSource().toFile().getName()))
                        .peek( f -> f.path("../bin/" + f.getSource().toFile().getName()))
                        .peek( f -> {
                            // Mark shell scripts as executable
                            if (f.getSource().toString().endsWith(".sh")) {
                                f.executable(true);
                            }
                        }))

                // javafx files
                .files(FileMetadata.streamDirectory(cacheLoc)
                        .filter(fm -> fm.getSource().getFileName().toString().startsWith("javafx"))
                        .peek(f -> f.modulepath())
                        .peek(f -> f.ignoreBootConflict()) // if run with JDK 9/10
                        .peek(f -> f.osFromFilename())
                        .peek(f ->f.uri(extractJavafxURL(f.getSource(), f.getOs())))
                        .peek( f ->
                                        {      // for Mac we need to support two architectures
                                                String pathToJar = f.getSource().toString();
                                                if(pathToJar.contains("-mac")) {
                                                    if (pathToJar.contains("aarch64")) {
                                                        //String arch = System.getProperty("os.arch");
                                                        // add ARM Mac Jar entries properly, for all other we use default arch
                                                        f.arch("aarch64");
                                                    }
                                                    else{
                                                        System.out.println("Setting MAC Intel Architecture");
                                                        f.arch("x86_64");
                                                    }
                                                }
                                        }
                                )
                       // .peek( f -> f.path(getNonOsSpecificJavafxName(f.getSource())))
                )

                .property("default.launcher.main.class", "at.conapi.messagemanager.bootstrap.Delegate")
                .property("maven.central", MAVEN_BASE)
                .property("maven.central.javafx", "${maven.central}/org/openjfx/")
                .build();

        try (Writer out = Files.newBufferedWriter(Paths.get(configLoc + "/setup.xml"))) {
            setup.write(out);
        }

        writeRuntimeConfigs(Paths.get(configLoc), baseUrl);
    }

    /**
     * Writes one Update4J configuration per platform folder of the Java runtime the tree holds,
     * {@code runtime/<name>/<platform>/}: {@code runtime/<platform>.xml}, which the bootstrap reads in the background
     * once the app is up. Its files install under {@code ${user.dir}/runtime}; the property {@code runtime.home} names
     * {@code <name>/<platform>}. An empty platform folder (a platform the build does not ship) gets a configuration
     * without files, which withdraws a runtime installations may still hold. A tree without {@code runtime/} gets none.
     * A tree with two names fails: an installation must never see two runtimes offered at once. setup.xml and
     * app/config.xml never list the runtime, so a runtime that cannot be downloaded holds up nothing else.
     */
    public static void writeRuntimeConfigs(Path configLoc, String baseUrl) throws IOException {
        Path runtimeDir = configLoc.resolve("runtime");
        if (!Files.isDirectory(runtimeDir)) {
            return;
        }
        List<Path> names;
        try (Stream<Path> list = Files.list(runtimeDir)) {
            names = list.filter(Files::isDirectory).sorted().collect(Collectors.toList());
        }
        try (Stream<Path> list = Files.list(runtimeDir)) {
            for (Path old : (Iterable<Path>) list.filter(p -> p.getFileName().toString().endsWith(".xml"))::iterator) {
                Files.delete(old);
            }
        }
        if (names.size() > 1) {
            throw new IllegalStateException("two or more Java runtimes in " + runtimeDir + ": " + names
                    + " - the tree holds exactly one runtime name");
        }
        if (names.isEmpty()) {
            return;
        }
        Path name = names.get(0);
        List<Path> platforms;
        try (Stream<Path> list = Files.list(name)) {
            platforms = list.filter(Files::isDirectory).sorted().collect(Collectors.toList());
        }
        for (Path platform : platforms) {
            String home = name.getFileName() + "/" + platform.getFileName();
            Configuration runtime = Configuration.builder()
                    .basePath("${user.dir}/runtime")
                    .files(FileMetadata.streamDirectory(platform)
                            .peek(f -> f.classpath(false))
                            .peek(f -> f.modulepath(false))
                            // lib/jrt-fs.jar is part of the JDK: its packages are java.base's
                            .peek(f -> f.ignoreBootConflict())
                            .peek(f -> {
                                String file = platform.relativize(f.getSource()).toString().replace("\\", "/");
                                f.path(home + "/" + file);
                                f.uri(baseUrl + "/runtime/" + home + "/" + file);
                            }))
                    .property("runtime.home", home)
                    .build();
            Path xml = runtimeDir.resolve(platform.getFileName() + ".xml");
            try (Writer out = Files.newBufferedWriter(xml, StandardCharsets.UTF_8)) {
                runtime.write(out);
            }
        }
    }

    private static String javafxVersion;

    private static String getNonOsSpecificJavafxName(Path source) {
        /**
         * Translates
         * javafx-base-17-linux.jar
         * javafx-base-17-mac.jar
         * javafx-base-17-mac-aarch64.jar
         * javafx-base-17-win.jar
         *
         * to:
         * javafx-base-17.jar
         */
        String fileName = source.getFileName().toString();
        String[] parts = fileName.split("-");

        if (parts.length >= 3) {
            StringBuilder newFileName = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                newFileName.append(parts[i]);
                if (i < 2) {
                    newFileName.append("-");
                }
            }
            newFileName.append(".jar");
            return newFileName.toString();
        }

        return source.getFileName().toString();
    }

    private static final String MAVEN_BASE = "https://repo1.maven.org/maven2";

    private static String mavenUrl(String groupId, String artifactId, String version, OS os) {
        StringBuilder builder = new StringBuilder();
        builder.append(MAVEN_BASE + '/');
        builder.append(groupId.replace('.', '/') + "/");
        builder.append(artifactId.replace('.', '-') + "/");
        builder.append(version + "/");
        builder.append(artifactId.replace('.', '-') + "-" + version);

        if (os != null) {
            //TODO: not sure how to handel intel/arm for Mac, just support arm for now
            String osShortName = OS.OTHER.equals(os)?"mac-aarch64":os.getShortName();
            //String osShortName = OS.OTHER.equals(os)||OS.MAC.equals(os)?"mac-aarch64":os.getShortName();
            builder.append('-' + osShortName);
        }

        builder.append(".jar");

        return builder.toString();
    }

/*    private static String mavenUrl(String groupId, String artifactId, String version) {
        return mavenUrl(groupId, artifactId, version, null);
    }
*/
    private static String extractJavafxURL(Path path, OS os) {
        Pattern regex = Pattern.compile("javafx-([a-z]+)-([0-9.]+)(?:-(win|mac|linux|mac-aarch64))?\\.jar");
        Matcher match = regex.matcher(path.getFileName().toString());

        if (!match.find())
            return null;

        String module = match.group(1);
        String version = javafxVersion;
        String osArch = match.group(3);
        if ((os == null || os.equals(OS.MAC)) && osArch != null) {
            if(osArch.equals("mac-aarch64")){
                os = OS.OTHER;
            }
            else {
                os = OS.fromShortName(osArch);
            }
        }

        final String mavenUrl = mavenUrl("org.openjfx", "javafx." + module, version, os);
        return mavenUrl;
    }

    private static String injectOs(String file, OS os) {
        String osName = OS.OTHER.equals(os)?"mac-aarch64":os.getShortName();
        return file.replaceAll("(.+)\\.jar", "$1-" + osName + ".jar");
    }

    private static void cacheJavafx(String baseDir, String target) throws IOException {
        String names = target + "/javafx";
        Path cacheDir = Paths.get(baseDir, "fxcache");

        try (Stream<Path> files = Files.list(Paths.get(names))) {
            files.forEach(f -> {
                try {

                    if (!Files.isDirectory(cacheDir))
                        Files.createDirectory(cacheDir);

                    // use OTHER for MAC AARM
                    for (OS os : EnumSet.of(OS.WINDOWS, OS.MAC, OS.LINUX, OS.OTHER)) {
                        Path file = cacheDir.resolve(injectOs(f.getFileName().toString(), os));

                        if (Files.notExists(file)) {
                            String download = extractJavafxURL(f, os);
                            URI uri = URI.create(download);
                            try (InputStream in = uri.toURL().openStream()) {
                                Files.copy(in, file);
                            }
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}

