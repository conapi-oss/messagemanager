package at.conapi.messagemanager.bootstrap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

/**
 * The runtime step: its decisions one by one, then whole runs against an update site on disk (file: links) with a
 * runtime whose {@code java} is a shell script. The script records how it was called and exits with the code in the
 * install folder's {@code java-exit.txt}, so the activation check can pass or fail without a real runtime.
 */
public class RuntimeStepTest {

    private static final String PLATFORM = "mac-aarch64";
    private static final String NAME = "25.0.5.1.2-0123456789ab";
    private static final String HOME = "runtime/" + NAME + "/" + PLATFORM;
    private static final String FAKE_JAVA = "#!/bin/sh\n"
            + "home=\"$(cd \"$(dirname \"$0\")/../../../..\" && pwd)\"\n"
            + "echo \"$@\" >> \"$home/java-calls.txt\"\n"
            + "code=\"$(cat \"$home/java-exit.txt\" 2>/dev/null || echo 0)\"\n"
            + "echo \"fake java exits $code\"\n"
            + "exit \"$code\"\n";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path site;
    private Path install;
    private String appConfigUrl;

    @Before
    public void folders() throws IOException {
        site = folder.newFolder("site").toPath();
        install = folder.newFolder("install with space").toPath();
        write(site.resolve("app/config.xml"), "<configuration/>");
        write(site.resolve("setup.xml"), "<configuration timestamp=\"x\"><files/></configuration>");
        appConfigUrl = site.resolve("app/config.xml").toUri().toString();
    }

    // -- the decisions ------------------------------------------------------------------------------------------

    @Test
    public void platformIsUpdate4jsOsNameAndTheJvmsArch() {
        assertEquals("mac-aarch64", RuntimeStep.platform("Mac OS X", "aarch64"));
        assertEquals("mac-x86_64", RuntimeStep.platform("Mac OS X", "x86_64"));
        assertEquals("win-amd64", RuntimeStep.platform("Windows 11", "amd64"));
        assertEquals("linux-amd64", RuntimeStep.platform("Linux", "amd64"));
        assertEquals("linux-aarch64", RuntimeStep.platform("Linux", "aarch64"));
    }

    @Test
    public void theRuntimeConfigurationSitsBesideTheApplications() {
        assertEquals("https://files.conapi.at/mm/stable/runtime/mac-aarch64.xml",
                RuntimeStep.configUri("https://files.conapi.at/mm/stable/app/config.xml", "mac-aarch64").toString());
        assertEquals("https://mirror.example.com/messagemanager/runtime/win-amd64.xml",
                RuntimeStep.configUri("https://mirror.example.com/messagemanager/app/config.xml", "win-amd64").toString());
        assertEquals("file:/C:/my%20mirror/runtime/linux-amd64.xml",
                RuntimeStep.configUri("file:/C:/my%20mirror/app/config.xml", "linux-amd64").toString());
        assertEquals("https://files.conapi.at/mm/dev/setup.xml",
                RuntimeStep.setupUri("https://files.conapi.at/mm/dev/app/config.xml").toString());
    }

    @Test
    public void aHomeIsAPathUnderRuntimeAndNothingElse() {
        assertTrue(RuntimeStep.validHome(HOME));
        assertTrue(RuntimeStep.validHome("runtime/25.0.5.1.2-abc/win-amd64"));
        for (String bad : new String[] { null, "", "runtime", "runtime/" + NAME, "/runtime/" + NAME + "/" + PLATFORM,
                "/usr/bin", "runtime/../" + PLATFORM, "runtime/" + NAME + "/..", "runtime/./" + PLATFORM,
                "runtime/a/b/c", "other/" + NAME + "/" + PLATFORM, "runtime\\" + NAME + "\\" + PLATFORM,
                "runtime/a b/" + PLATFORM, "runtime/a&calc/" + PLATFORM, " " + HOME }) {
            assertFalse(String.valueOf(bad), RuntimeStep.validHome(bad));
        }
    }

    @Test
    public void thePointerIsItsFirstLineWhenThatLineIsAHome() throws IOException {
        assertEquals(Optional.empty(), RuntimeStep.readPointer(install));
        write(install.resolve("runtime/current"), HOME);
        assertEquals(Optional.of(HOME), RuntimeStep.readPointer(install));
        write(install.resolve("runtime/current"), HOME + "\r\n");
        assertEquals(Optional.of(HOME), RuntimeStep.readPointer(install));
        write(install.resolve("runtime/current"), "");
        assertEquals(Optional.empty(), RuntimeStep.readPointer(install));
        write(install.resolve("runtime/current"), "/Library/Java/bin/java");
        assertEquals(Optional.empty(), RuntimeStep.readPointer(install));
        write(install.resolve("runtime/current"), "runtime/../../" + PLATFORM);
        assertEquals(Optional.empty(), RuntimeStep.readPointer(install));
    }

    @Test
    public void theRunningHomeIsKnownOnlyUnderRuntime() throws IOException {
        Path home = install.resolve(HOME);
        Files.createDirectories(home);
        assertEquals(Optional.of(HOME), RuntimeStep.runningHome(install, home));
        assertEquals(Optional.empty(), RuntimeStep.runningHome(install, Path.of("/Library/Java/JavaVirtualMachines/x/Contents/Home")));
        assertEquals(Optional.empty(), RuntimeStep.runningHome(install, install.resolve("jre")));
    }

    @Test
    public void aFailedActivationIsRememberedForItsHomeOnly() {
        String status = "2026-11-20T10:00:00Z activation failed " + HOME + ": fake java exits 1";
        assertTrue(RuntimeStep.activationFailedBefore(status, HOME));
        assertFalse(RuntimeStep.activationFailedBefore(status, "runtime/25.0.6-ffffffffffff/" + PLATFORM));
        assertFalse(RuntimeStep.activationFailedBefore("2026-11-20T10:00:00Z update site not reached", HOME));
        assertFalse(RuntimeStep.activationFailedBefore(null, HOME));
    }

    @Test
    public void theSweepRemovesEveryFolderThatIsNotKeptAndNoFile() throws IOException {
        Path runtime = install.resolve("runtime");
        for (String name : List.of("active", "running", "configured", "old-1", "old-2")) {
            Files.createDirectories(runtime.resolve(name).resolve(PLATFORM));
        }
        write(runtime.resolve("current"), HOME);
        write(runtime.resolve("status.txt"), "x");
        write(runtime.resolve("download.zip"), "x");
        List<Path> remove = RuntimeStep.foldersToRemove(runtime, Set.of("active", "running", "configured"));
        assertEquals(List.of(runtime.resolve("old-1"), runtime.resolve("old-2")), remove);
        assertEquals(List.of(), RuntimeStep.foldersToRemove(install.resolve("nothing-here"), Set.of()));
    }

    @Test
    public void theCheckGetsThisStartsProxyAndTlsSettingsOnly() {
        Properties system = new Properties();
        system.setProperty("https.proxyHost", "proxy.example.com");
        system.setProperty("https.proxyPort", "3128");
        system.setProperty("http.nonProxyHosts", "localhost|*.local");
        system.setProperty("java.net.useSystemProxies", "true");
        system.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");
        system.setProperty("jdk.tls.client.protocols", "TLSv1.3");
        system.setProperty("socksProxyHost", "socks.example.com");
        system.setProperty("java.home", "/old/java");
        system.setProperty("user.home", "/Users/someone");
        system.setProperty("javafx.embed.singleThread", "true");
        List<String> options = RuntimeStep.checkOptions(system);
        assertEquals(List.of("-Dhttp.nonProxyHosts=localhost|*.local", "-Dhttps.proxyHost=proxy.example.com",
                "-Dhttps.proxyPort=3128", "-Djava.net.useSystemProxies=true", "-Djavax.net.ssl.trustStoreType=Windows-ROOT",
                "-Djdk.tls.client.protocols=TLSv1.3", "-DsocksProxyHost=socks.example.com"), options);
    }

    // -- whole runs ---------------------------------------------------------------------------------------------

    @Test
    public void noConfigurationChangesNothingAndSaysWhy() throws IOException {
        run(null);
        assertFalse(Files.exists(install.resolve("runtime/current")));
        assertTrue(status(), status().contains("no runtime for " + PLATFORM));
        try (var list = Files.list(install.resolve("runtime"))) {
            assertEquals(1, list.count());
        }
    }

    @Test
    public void aRuntimeIsDownloadedCheckedAndActivated() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        run(null);
        assertEquals(HOME, pointer());
        assertTrue(status(), status().contains("active " + HOME));
        assertTrue(Files.isExecutable(install.resolve(HOME).resolve("bin/java")));
        assertTrue(Files.isExecutable(install.resolve(HOME).resolve("lib/jspawnhelper")));
        assertFalse(Files.isExecutable(install.resolve(HOME).resolve("lib/modules")));
        assertFalse(Files.exists(install.resolve("runtime/download.zip")));
        String call = calls().get(0);
        assertTrue(call, call.contains(RuntimeCheck.class.getName()));
        assertTrue(call, call.endsWith(RuntimeStep.setupUri(appConfigUrl).toString()));
    }

    @Test
    public void aFailedActivationWritesNoPointerAndIsNotTriedAgain() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        write(install.resolve("java-exit.txt"), "1");
        run(null);
        assertNull(pointer());
        assertTrue(status(), status().contains("activation failed " + HOME + ": fake java exits 1"));
        assertEquals(1, calls().size());

        run(null);
        assertNull(pointer());
        assertEquals("no second check of the same runtime", 1, calls().size());
        assertTrue(status(), status().contains("activation failed " + HOME));
        assertTrue("the runtime stays for when it can run", Files.exists(install.resolve(HOME).resolve("bin/java")));
    }

    @Test
    public void aFileWithTheWrongChecksumIsNeverActivated() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        write(site.resolve(HOME).resolve("lib/modules"), "other bytes, same length?");
        run(null);
        assertNull(pointer());
        assertTrue(status(), status().contains("download failed"));
        assertFalse(Files.exists(install.resolve("runtime/download.zip")));
        assertEquals(List.of(), calls());
    }

    @Test
    public void theActiveRuntimeThatDiffersLosesItsPointerAndIsRepairedByAStartNotRunningOnIt() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        run(null);
        assertEquals(HOME, pointer());

        write(install.resolve(HOME).resolve("lib/modules"), "damaged");
        run(install.resolve(HOME));
        assertNull("this start runs on it: remove the pointer, repair nothing", pointer());
        assertEquals("damaged", Files.readString(install.resolve(HOME).resolve("lib/modules")));
        assertTrue(status(), status().contains("differs"));

        run(null);
        assertEquals(HOME, pointer());
        assertEquals("modules", Files.readString(install.resolve(HOME).resolve("lib/modules")));
    }

    @Test
    public void aWithdrawnRuntimeLosesItsPointer() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        run(null);
        assertEquals(HOME, pointer());

        withdraw();
        run(install.resolve(HOME));
        assertNull(pointer());
        assertTrue("the running one stays", Files.exists(install.resolve(HOME)));
        assertTrue(status(), status().contains("withdrawn"));

        run(null);
        assertFalse("removed once nothing runs on it", Files.exists(install.resolve("runtime").resolve(NAME)));
    }

    @Test
    public void anotherNameIsInstalledBesideTheRunningOneWhichGoesLater() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        run(null);
        String next = "25.0.6.0.1-fedcba987654";
        publish(next);
        run(install.resolve(HOME));
        assertEquals("runtime/" + next + "/" + PLATFORM, pointer());
        assertTrue("still running on the old one", Files.exists(install.resolve(HOME)));

        run(install.resolve("runtime/" + next + "/" + PLATFORM));
        assertFalse(Files.exists(install.resolve("runtime").resolve(NAME)));
        assertTrue(Files.exists(install.resolve("runtime").resolve(next)));
    }

    @Test
    public void aBrokenPointerIsRepaired() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        run(null);
        write(install.resolve("runtime/current"), "../../somewhere/else");
        run(null);
        assertEquals(HOME, pointer());
    }

    @Test
    public void aStaleDownloadIsReplaced() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        write(install.resolve("runtime/download.zip"), "the rest of a killed download");
        // what Java's zip file system writes beside the archive while it downloads, and leaves when its process dies
        write(install.resolve("runtime/zipfstmp6175508679448022613.tmp"), "half a runtime");
        run(null);
        assertEquals(HOME, pointer());
        try (var list = Files.list(install.resolve("runtime"))) {
            assertEquals(List.of(), list.map(p -> p.getFileName().toString()).filter(n -> n.startsWith("zipfstmp")).collect(java.util.stream.Collectors.toList()));
        }
    }

    @Test
    public void aConfigurationThatInstallsElsewhereIsRefused() throws IOException {
        assumeFalse(windows());
        publish(NAME, folder.newFolder("elsewhere").toPath());
        run(null);
        assertNull(pointer());
        assertTrue(status(), status().contains("installs into"));
    }

    @Test
    public void aStepThatFailsNeverThrows() throws IOException {
        write(site.resolve("runtime/" + PLATFORM + ".xml"), "<not a configuration");
        run(null);
        assertNull(pointer());
        assertFalse(status().isBlank());
    }

    // -- helpers ------------------------------------------------------------------------------------------------

    private void run(Path javaHome) {
        new RuntimeStep(install, appConfigUrl, PLATFORM, javaHome == null ? Path.of("/the/java/of/today") : javaHome).run();
    }

    private void publish(String name) throws IOException {
        publish(name, install.resolve("runtime"));
    }

    /** The update site offers runtime {@code name}, as CreateConfig writes it, installing into {@code base}. */
    private void publish(String name, Path base) throws IOException {
        Path home = site.resolve("runtime").resolve(name).resolve(PLATFORM);
        write(home.resolve("bin/java"), FAKE_JAVA);
        write(home.resolve("lib/jspawnhelper"), "jspawnhelper");
        write(home.resolve("lib/modules"), "modules");
        write(home.resolve("release"), "JAVA_VERSION=\"25\"");
        String homeRef = name + "/" + PLATFORM;
        Configuration config = Configuration.builder()
                .basePath(base.toString())
                .files(FileMetadata.streamDirectory(home)
                        .peek(f -> f.classpath(false))
                        .peek(f -> f.modulepath(false))
                        .peek(f -> f.ignoreBootConflict())
                        .peek(f -> {
                            String file = home.relativize(f.getSource()).toString().replace('\\', '/');
                            f.path(homeRef + "/" + file);
                            f.uri(home.resolve(file).toUri().toString());
                        }))
                .property(RuntimeStep.HOME_PROPERTY, homeRef)
                .build();
        try (Writer out = Files.newBufferedWriter(site.resolve("runtime/" + PLATFORM + ".xml"))) {
            config.write(out);
        }
    }

    private void withdraw() throws IOException {
        Configuration empty = Configuration.builder().basePath(install.resolve("runtime").toString()).build();
        try (Writer out = Files.newBufferedWriter(site.resolve("runtime/" + PLATFORM + ".xml"))) {
            empty.write(out);
        }
    }

    private String pointer() throws IOException {
        Path p = install.resolve("runtime/current");
        return Files.exists(p) ? Files.readString(p) : null;
    }

    private String status() throws IOException {
        return Files.readString(install.resolve("runtime/status.txt"));
    }

    private List<String> calls() throws IOException {
        Path p = install.resolve("java-calls.txt");
        return Files.exists(p) ? Files.readAllLines(p) : List.of();
    }

    private static boolean windows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static void write(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
