package at.conapi.messagemanager.bootstrap.config;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * CreateConfig on scratch trees: the Java runtime's configurations (one per platform folder), nothing of the runtime
 * in setup.xml or app/config.xml, and the JavaFX version taken from its argument. The JavaFX cache is filled with
 * stand-in jars, so nothing is downloaded.
 */
public class CreateConfigTest {

    private static final String JAVAFX = "21.0.13";
    private static final String NAME = "25.0.5.1.2-0123456789ab";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path root;
    private Path target;
    private Path config;

    @Before
    public void tree() throws IOException {
        root = folder.newFolder("root").toPath();
        target = root.resolve("target");
        config = target.resolve("config");
        write(config.resolve("app/messagemanager-app.jar"), "app");
        write(config.resolve("bootstrap/messagemanager-bootstrap.jar"), "bootstrap");
        write(config.resolve("bootstrap/update4j-1.5.12.jar"), "update4j");
        write(config.resolve("bin/launch.sh"), "#!/bin/bash");
        write(config.resolve("bin/cleanup.sh"), "#!/bin/bash");
        Files.createDirectories(config.resolve("plugins"));
        for (String module : List.of("base", "graphics")) {
            write(target.resolve("javafx/javafx-" + module + "-21.jar"), module);
            for (String os : List.of("win", "mac", "linux", "mac-aarch64")) {
                write(root.resolve("fxcache/javafx-" + module + "-21-" + os + ".jar"), module + os);
            }
        }
    }

    private void runtime(String name, String platform) throws IOException {
        Path home = config.resolve("runtime").resolve(name).resolve(platform);
        write(home.resolve("release"), "JAVA_VERSION=\"25.0.5.1\"");
        write(home.resolve("bin/java"), "java " + platform);
        write(home.resolve("lib/jrt-fs.jar"), "jrt-fs");
        write(home.resolve("legal/java.base/LICENSE"), "license");
    }

    private void createConfig() throws IOException {
        CreateConfig.main(new String[] { root.toString(), target.toString(), config.toString(), "dev", JAVAFX });
    }

    @Test
    public void aTreeWithoutRuntimeGetsNoRuntimeConfiguration() throws IOException {
        createConfig();
        assertFalse(Files.exists(config.resolve("runtime")));
    }

    @Test
    public void eachPlatformFolderGetsItsOwnConfiguration() throws IOException {
        runtime(NAME, "mac-aarch64");
        runtime(NAME, "win-amd64");
        createConfig();

        try (var files = Files.list(config.resolve("runtime"))) {
            assertEquals(List.of(NAME, "mac-aarch64.xml", "win-amd64.xml"),
                    files.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList()));
        }
        Configuration mac = read(config.resolve("runtime/mac-aarch64.xml"));
        assertEquals(NAME + "/mac-aarch64", mac.getResolvedProperty("runtime.home"));
        assertEquals("${user.dir}/runtime", rawBasePath(config.resolve("runtime/mac-aarch64.xml")));
        assertEquals(4, mac.getFiles().size());
        for (FileMetadata f : mac.getFiles()) {
            String path = f.getPath().toString().replace('\\', '/');
            assertTrue(path, path.contains("/runtime/" + NAME + "/mac-aarch64/"));
            String relative = path.substring(path.indexOf("/runtime/") + "/runtime/".length());
            assertEquals("https://files.conapi.at/mm/dev/runtime/" + relative, f.getUri().toString());
            assertFalse(path, f.isClasspath());
            assertFalse(path, f.isModulepath());
            assertTrue(path, f.isIgnoreBootConflict());
        }
        Configuration win = read(config.resolve("runtime/win-amd64.xml"));
        assertEquals(NAME + "/win-amd64", win.getResolvedProperty("runtime.home"));
        assertTrue(win.getFiles().stream().allMatch(f -> f.getUri().toString().contains("/runtime/" + NAME + "/win-amd64/")));
    }

    @Test
    public void aTreeWithTwoRuntimeNamesFails() throws IOException {
        runtime(NAME, "mac-aarch64");
        runtime("25.0.5.1.2-ba9876543210", "mac-aarch64");
        try {
            createConfig();
            fail("two runtime names must fail the configuration");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("two"));
        }
    }

    @Test
    public void setupAndAppConfigurationsListNothingOfTheRuntime() throws IOException {
        runtime(NAME, "mac-aarch64");
        createConfig();
        for (String manifest : List.of("setup.xml", "app/config.xml")) {
            String xml = Files.readString(config.resolve(manifest), StandardCharsets.UTF_8);
            assertFalse(manifest, xml.contains("runtime"));
        }
    }

    @Test
    public void javafxComesInTheVersionItIsGiven() throws IOException {
        createConfig();
        List<String> javafx = read(config.resolve("setup.xml")).getFiles().stream()
                .map(f -> f.getUri().toString())
                .filter(u -> u.contains("/org/openjfx/"))
                .collect(Collectors.toList());
        assertEquals(8, javafx.size());
        for (String uri : javafx) {
            assertTrue(uri, uri.contains("/" + JAVAFX + "/javafx-") && uri.contains("-" + JAVAFX + "-"));
        }
    }

    @Test
    public void withoutAJavafxVersionItStops() {
        try {
            CreateConfig.main(new String[] { root.toString(), target.toString(), config.toString(), "dev" });
            fail("the JavaFX version has one home, the build, and must be given");
        } catch (Exception expected) {
            // the message names what is missing
            assertTrue(String.valueOf(expected.getMessage()), String.valueOf(expected.getMessage()).contains("JavaFX"));
        }
    }

    private static String rawBasePath(Path xml) throws IOException {
        String s = Files.readString(xml, StandardCharsets.UTF_8);
        int i = s.indexOf("path=\"", s.indexOf("<base"));
        return s.substring(i + 6, s.indexOf('"', i + 6));
    }

    private static Configuration read(Path xml) throws IOException {
        try (Reader in = Files.newBufferedReader(xml, StandardCharsets.UTF_8)) {
            return Configuration.read(in);
        }
    }

    private static void write(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
