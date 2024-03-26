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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        //TODO:  Need to automate config creation per platform + hosting per platform then

        // where to create and create the config


        String baseUrl = "https://product.conapi.at/messagemanager"; //http://localhost/messagemanager";//TODO: replace with proper hosting URL

        String dir = configLoc + "/app";
        Files.createDirectories(Paths.get(dir));

        Configuration config = Configuration.builder()
                .baseUri(baseUrl + "/app")
                .basePath("${user.dir}/app")
                .files(FileMetadata.streamDirectory(dir).filter( r -> !r.getSource().endsWith("config.xml"))
                        .peek(r -> r.modulepath(r.getSource().toString().endsWith(".jar"))))
                // plugins
                .files(FileMetadata.streamDirectory(configLoc + "/plugins")
                        .peek(f -> f.uri(baseUrl +"/plugins/" + f.getSource().toFile().getName()))
                        .peek( f -> f.path("../plugins/" + f.getSource().toFile().getName())))
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

                // special handling for updat4j
                .files(FileMetadata.streamDirectory(dir)
                        .filter( fm -> fm.getSource().getFileName().toFile().toString().startsWith("update4j"))
                        //.peek(f -> f.modulepath(f.getSource().toString().endsWith(".jar")))
                        .peek(f -> f.modulepath()) // anyway only update4j-<version>.jar
                        .peek(f -> f.uri(baseUrl +"/bootstrap/" + f.getSource().toFile().getName()))
                        .peek(f -> f.ignoreBootConflict())
                        .peek( f -> f.path("update4j.jar"))
                )

                // any other jar (for now just the bootsrap jar)
                .files(FileMetadata.streamDirectory(dir)
                        .filter( fm -> !fm.getSource().getFileName().toFile().toString().startsWith("update4j"))
                        .peek(f -> f.modulepath(f.getSource().toString().endsWith(".jar")))
                        .peek(f -> f.uri(baseUrl +"/bootstrap/" + f.getSource().toFile().getName()))
                        .peek(f -> f.ignoreBootConflict()))

                // launch scripts, still keep these here as duplicate as a last resort option if app update fails
                .files(FileMetadata.streamDirectory(configLoc + "/bin")
                        .filter( fm -> !fm.getSource().getFileName().toFile().toString().startsWith("update4j"))
                        .peek(f -> f.uri(baseUrl +"/bin/" + f.getSource().toFile().getName()))
                        .peek( f -> f.path("../bin/" + f.getSource().toFile().getName())))

                .files(FileMetadata.streamDirectory(cacheLoc)
                        .filter(fm -> fm.getSource().getFileName().toString().startsWith("javafx"))
                        .peek(f -> f.modulepath())
                        .peek(f -> f.ignoreBootConflict()) // if run with JDK 9/10
                        .peek(f -> f.osFromFilename())
                        .peek(f -> f.uri(extractJavafxURL(f.getSource(), f.getOs()))))
                .property("default.launcher.main.class", "at.conapi.messagemanager.bootstrap.Delegate")
                .property("maven.central", MAVEN_BASE)
                .property("maven.central.javafx", "${maven.central}/org/openjfx/")
                .build();

        try (Writer out = Files.newBufferedWriter(Paths.get(configLoc + "/setup.xml"))) {
            setup.write(out);
        }

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
            builder.append('-' + os.getShortName());
        }

        builder.append(".jar");

        return builder.toString();
    }

    private static String mavenUrl(String groupId, String artifactId, String version) {
        return mavenUrl(groupId, artifactId, version, null);
    }

    private static String extractJavafxURL(Path path, OS os) {
        Pattern regex = Pattern.compile("javafx-([a-z]+)-([0-9.]+)(?:-(win|mac|linux))?\\.jar");
        Matcher match = regex.matcher(path.getFileName().toString());

        if (!match.find())
            return null;

        String module = match.group(1);
        String version = match.group(2);
        if (os == null && match.groupCount() > 2) {
            os = OS.fromShortName(match.group(3));
        }

        return mavenUrl("org.openjfx", "javafx." + module, version, os);
    }

    private static String injectOs(String file, OS os) {
        return file.replaceAll("(.+)\\.jar", "$1-" + os.getShortName() + ".jar");
    }

    private static void cacheJavafx(String baseDir, String target) throws IOException {
        String names = target + "/javafx";
        Path cacheDir = Paths.get(baseDir, "fxcache");

        try (Stream<Path> files = Files.list(Paths.get(names))) {
            files.forEach(f -> {
                try {

                    if (!Files.isDirectory(cacheDir))
                        Files.createDirectory(cacheDir);

                    for (OS os : EnumSet.of(OS.WINDOWS, OS.MAC, OS.LINUX)) {
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

