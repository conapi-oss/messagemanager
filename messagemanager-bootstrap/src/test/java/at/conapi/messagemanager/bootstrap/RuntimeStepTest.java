package at.conapi.messagemanager.bootstrap;

import at.conapi.messagemanager.bootstrap.config.CreateConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.OS;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

/**
 * The runtime step: its decisions one by one, then whole runs against an update site on disk (file: links) that
 * {@link CreateConfig} writes, as the build does, with a runtime whose {@code java} is a shell script. The script
 * records how it was called and exits with the code in the install folder's {@code java-exit.txt}, so the activation
 * check can pass or fail without a real runtime. The last tests run {@link RuntimeCheck} itself, and source the
 * cleanup script, so the three readers of a pointer agree.
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
        assertEquals("mac-aarch64", RuntimeStep.platform(OS.MAC, "aarch64"));
        assertEquals("mac-x86_64", RuntimeStep.platform(OS.MAC, "x86_64"));
        assertEquals("win-amd64", RuntimeStep.platform(OS.WINDOWS, "amd64"));
        assertEquals("linux-amd64", RuntimeStep.platform(OS.LINUX, "amd64"));
        assertEquals("linux-aarch64", RuntimeStep.platform(OS.LINUX, "aarch64"));
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

    private static final String[] GOOD_HOMES = { HOME, "runtime/25.0.5.1.2-abc/win-amd64", "runtime/a_b/x-y" };
    private static final String[] BAD_HOMES = { "", "runtime", "runtime/" + NAME, "/runtime/" + NAME + "/" + PLATFORM,
            "/usr/bin", "runtime/../" + PLATFORM, "runtime/" + NAME + "/..", "runtime/./" + PLATFORM,
            "runtime/a/b/c", "other/" + NAME + "/" + PLATFORM, "runtime\\" + NAME + "\\" + PLATFORM,
            "runtime/a b/" + PLATFORM, "runtime/a&calc/" + PLATFORM, " " + HOME, "runtime/25..1-x/" + PLATFORM,
            "runtime//" + PLATFORM, "runtime/.hidden/" + PLATFORM };

    @Test
    public void aHomeIsAPathUnderRuntimeAndNothingElse() {
        for (String good : GOOD_HOMES) {
            assertTrue(good, RuntimeStep.validHome(good));
        }
        assertFalse(RuntimeStep.validHome(null));
        for (String bad : BAD_HOMES) {
            assertFalse(bad, RuntimeStep.validHome(bad));
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

    @Test
    public void theDownloadNeedsThreeTimesTheRuntimeFree() {
        long mb = 1024 * 1024;
        assertNull(RuntimeStep.notEnoughSpace(400 * mb, 100 * mb));
        assertNull(RuntimeStep.notEnoughSpace(300 * mb, 100 * mb));
        String why = RuntimeStep.notEnoughSpace(299 * mb, 100 * mb);
        assertNotNull(why);
        assertTrue(why, why.contains("300 MB") && why.contains("299 MB"));
    }

    @Test
    public void filesTheUserMayAdjustAreConfAndTheTrustStore() {
        assertTrue(RuntimeStep.userAdjustable("conf/security/java.security"));
        assertTrue(RuntimeStep.userAdjustable("conf/net.properties"));
        assertTrue(RuntimeStep.userAdjustable("lib/security/cacerts"));
        assertFalse(RuntimeStep.userAdjustable("lib/security/default.policy"));
        assertFalse(RuntimeStep.userAdjustable("lib/modules"));
        assertFalse(RuntimeStep.userAdjustable("bin/java"));
        assertFalse(RuntimeStep.userAdjustable("legal/java.base/LICENSE"));
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
        assertEquals("one line, with this platform's line end", HOME + System.lineSeparator(),
                Files.readString(install.resolve("runtime/current")));
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
        assertTrue("the runtime stays for when it can run", Files.exists(install.resolve(HOME).resolve("bin/java")));
    }

    @Test
    public void aFailedActivationStaysRememberedThroughAStartThatReachedNothing() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        write(install.resolve("java-exit.txt"), "1");
        run(null);
        Path xml = site.resolve("runtime/" + PLATFORM + ".xml");
        Path away = site.resolve("away.xml");
        Files.move(xml, away);
        run(null);
        assertTrue(status(), status().contains("no runtime for"));
        Files.move(away, xml);
        run(null);
        assertEquals("not tried again after a start that read no configuration", 1, calls().size());
        assertNull(pointer());
    }

    @Test
    public void anotherRuntimeIsTriedEvenThoughThePreviousOneFailed() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        write(install.resolve("java-exit.txt"), "1");
        run(null);
        Files.delete(install.resolve("java-exit.txt"));
        String next = "25.0.6.0.1-fedcba987654";
        publish(next);
        run(null);
        assertEquals("runtime/" + next + "/" + PLATFORM, pointer());
        assertFalse("the one that failed is swept", Files.exists(install.resolve("runtime").resolve(NAME)));
    }

    @Test
    public void aFileWithTheWrongChecksumIsNeverActivated() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        // the same length as "modules", other bytes: only the checksum tells them apart
        write(site.resolve(HOME).resolve("lib/modules"), "MODULES");
        run(null);
        assertNull(pointer());
        assertTrue(status(), status().contains("download failed") && status().contains("Checksum of file 'modules'"));
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
    public void filesTheUserAdjustedInTheRuntimeStayAndAreNoDamage() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        run(null);
        assertEquals(HOME, pointer());
        write(install.resolve(HOME).resolve("lib/security/cacerts"), "the company's root added");
        write(install.resolve(HOME).resolve("conf/security/java.security"), "jdk.tls.disabledAlgorithms=");

        run(install.resolve(HOME));
        assertEquals(HOME, pointer());
        assertTrue(status(), status().contains("active " + HOME));
        run(null);
        assertEquals("the company's root added", Files.readString(install.resolve(HOME).resolve("lib/security/cacerts")));
        assertEquals("jdk.tls.disabledAlgorithms=", Files.readString(install.resolve(HOME).resolve("conf/security/java.security")));

        Files.delete(install.resolve(HOME).resolve("lib/security/cacerts"));
        run(null);
        assertEquals("a missing one is still fetched", "cacerts", Files.readString(install.resolve(HOME).resolve("lib/security/cacerts")));
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
            assertEquals(List.of(), list.map(p -> p.getFileName().toString()).filter(n -> n.startsWith("zipfstmp")).collect(Collectors.toList()));
        }
    }

    @Test
    public void aRuntimeWaitsForTheApplicationUpdate() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        Path jar = folder.newFile("messagemanager-app.jar").toPath();
        Files.writeString(jar, "the new app");
        Configuration app = Configuration.builder()
                .basePath(install.resolve("app").toString())
                .file(FileMetadata.readFrom(jar).path("messagemanager-app.jar").uri(jar.toUri().toString()))
                .build();
        try (Writer out = Files.newBufferedWriter(site.resolve("app/config.xml"))) {
            app.write(out);
        }
        run(null);
        assertNull("not activated under an application the update site has replaced", pointer());
        assertTrue(status(), status().contains("waits for the application update"));
        assertTrue("downloaded already", Files.exists(install.resolve(HOME).resolve("bin/java")));

        Files.createDirectories(install.resolve("app"));
        Files.copy(jar, install.resolve("app/messagemanager-app.jar"));
        run(null);
        assertEquals(HOME, pointer());
    }

    @Test
    public void aStartOnTheRuntimeThatCameUpClearsItsTrial() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        run(null);
        write(install.resolve("runtime/trying"), HOME);
        run(install.resolve(HOME));
        assertFalse(Files.exists(install.resolve("runtime/trying")));
        assertEquals(HOME, pointer());
    }

    @Test
    public void aTrialTheAppNeverCameUpFromFailsTheRuntime() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        run(null);
        assertEquals(HOME, pointer());
        Path trying = install.resolve("runtime/trying");
        write(trying, HOME + System.lineSeparator());
        Files.setLastModifiedTime(trying, java.nio.file.attribute.FileTime.from(java.time.Instant.now().minus(java.time.Duration.ofMinutes(11))));
        run(null);
        assertNull(pointer());
        assertFalse(Files.exists(trying));
        assertTrue(Files.readString(install.resolve("runtime/failed")).startsWith(HOME + " "));
        assertTrue(status(), status().contains("did not bring the application up"));

        run(null);
        assertNull("not activated again", pointer());
        assertEquals("checked only at the first activation", 1, calls().size());
    }

    @Test
    public void aFreshTrialIsLeftToTheStartThatMayStillBeComingUp() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        run(null);
        write(install.resolve("runtime/trying"), HOME + System.lineSeparator());
        run(null);
        assertEquals(HOME, pointer());
        assertTrue(Files.exists(install.resolve("runtime/trying")));
        assertFalse(Files.exists(install.resolve("runtime/failed")));
    }

    @Test
    public void aRuntimeThatFailedIsNotActivatedAgain() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        write(install.resolve("runtime/failed"), HOME + " the app did not start on it");
        run(null);
        assertNull(pointer());
        assertEquals(List.of(), calls());
        assertTrue(status(), status().contains("not activated") && status().contains("the app did not start on it"));
    }

    @Test
    public void filesOutsideTheHomeAreRefused() throws IOException {
        assumeFalse(windows());
        Path home = site.resolve(HOME);
        write(home.resolve("bin/java"), FAKE_JAVA);
        Path evil = folder.newFile("evil").toPath();
        Configuration config = Configuration.builder()
                .basePath(install.resolve("runtime").toString())
                .file(FileMetadata.readFrom(home.resolve("bin/java")).path(NAME + "/" + PLATFORM + "/bin/java").uri(home.resolve("bin/java").toUri().toString()))
                .file(FileMetadata.readFrom(evil).path(NAME + "/" + PLATFORM + "-x/evil").uri(evil.toUri().toString()))
                .file(FileMetadata.readFrom(evil).path("../../bin/evil").uri(evil.toUri().toString()))
                .property(RuntimeStep.HOME_PROPERTY, NAME + "/" + PLATFORM)
                .build();
        try (Writer out = Files.newBufferedWriter(site.resolve("runtime/" + PLATFORM + ".xml"))) {
            config.write(out);
        }
        run(null);
        assertNull(pointer());
        assertTrue(status(), status().contains("outside"));
        assertFalse(Files.exists(install.resolve("runtime").resolve(NAME + "/" + PLATFORM + "-x/evil")));
        assertFalse(Files.exists(install.resolve("bin/evil")));
        assertEquals(List.of(), calls());
    }

    @Test
    public void aConfigurationThatInstallsElsewhereIsRefused() throws IOException {
        assumeFalse(windows());
        publish(NAME);
        String xml = Files.readString(site.resolve("runtime/" + PLATFORM + ".xml"));
        Files.writeString(site.resolve("runtime/" + PLATFORM + ".xml"),
                xml.replace(install.resolve("runtime").toString(), folder.newFolder("elsewhere").toString()));
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

    // -- the activation check itself, and the cleanup script ----------------------------------------------------

    @Test
    public void theCheckReadsAnUpdateConfigurationAndNothingElse() throws Exception {
        Path html = site.resolve("blocked.html");
        Files.writeString(html, "<html>blocked by the proxy</html>");
        assertEquals(0, check(site.resolve("setup.xml").toUri().toString()).exit);
        Result notConfig = check(html.toUri().toString());
        assertEquals(1, notConfig.exit);
        assertTrue(notConfig.output, notConfig.output.contains("not an update configuration"));
        assertEquals(1, check(site.resolve("missing.xml").toUri().toString()).exit);
    }

    @Test
    public void theCleanupScriptAgreesWithTheStepOnWhatAPointerIs() throws Exception {
        assumeFalse(windows());
        for (String home : GOOD_HOMES) {
            assertEquals(home, "./" + home + "/bin/java", cleanup(home, true));
        }
        for (String home : BAD_HOMES) {
            assertEquals(home, "today's java", cleanup(home, true));
        }
    }

    @Test
    public void theCleanupScriptMarksATrialAndUsesNoRuntimeWhileOneIsUnfinished() throws Exception {
        assumeFalse(windows());
        assertEquals("./" + HOME + "/bin/java", cleanup(HOME, true));
        assertEquals("it marks the trial", HOME, Files.readString(install.resolve("runtime/trying")).strip());

        // the app has not come up from that start (yet): the next start keeps today's Java and leaves the rest
        assertEquals("today's java", cleanup(HOME, false));
        assertTrue(Files.exists(install.resolve("runtime/current")));
        assertTrue(Files.exists(install.resolve("runtime/trying")));
    }

    // -- helpers ------------------------------------------------------------------------------------------------

    private void run(Path javaHome) {
        new RuntimeStep(install, appConfigUrl, PLATFORM, javaHome == null ? Path.of("/the/java/of/today") : javaHome).run();
    }

    /**
     * The update site offers runtime {@code name}: its files, and the configuration {@link CreateConfig} writes for
     * them, with {@code ${user.dir}} standing for this test's install folder. A site holds one runtime name.
     */
    private void publish(String name) throws IOException {
        clearRuntimes();
        Path home = site.resolve("runtime").resolve(name).resolve(PLATFORM);
        write(home.resolve("bin/java"), FAKE_JAVA);
        write(home.resolve("lib/jspawnhelper"), "jspawnhelper");
        write(home.resolve("lib/modules"), "modules");
        write(home.resolve("lib/security/cacerts"), "cacerts");
        write(home.resolve("conf/security/java.security"), "java.security");
        write(home.resolve("release"), "JAVA_VERSION=\"25\"");
        writeConfigs();
    }

    /** What the build makes of a platform it no longer ships: an empty folder, so a configuration without files. */
    private void withdraw() throws IOException {
        clearRuntimes();
        Files.createDirectories(site.resolve("runtime").resolve(NAME).resolve(PLATFORM));
        writeConfigs();
    }

    private void clearRuntimes() throws IOException {
        Path runtime = site.resolve("runtime");
        if (Files.isDirectory(runtime)) {
            try (Stream<Path> walk = Files.walk(runtime)) {
                for (Path p : walk.sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList())) {
                    Files.delete(p);
                }
            }
        }
    }

    private void writeConfigs() throws IOException {
        String base = site.toUri().toString();
        CreateConfig.writeRuntimeConfigs(site, base.substring(0, base.length() - 1));
        Path xml = site.resolve("runtime/" + PLATFORM + ".xml");
        Files.writeString(xml, Files.readString(xml).replace("${user.dir}", install.toString()));
    }

    private static final class Result {
        final int exit;
        final String output;

        Result(int exit, String output) {
            this.exit = exit;
            this.output = output;
        }
    }

    /** {@link RuntimeCheck} run as the step runs it: this JVM's java, the class path it was loaded from. */
    private static Result check(String url) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", windows() ? "java.exe" : "java").toString();
        String cp = Path.of(RuntimeCheck.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        Process p = new ProcessBuilder(java, "-cp", cp, RuntimeCheck.class.getName(), url).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(p.waitFor(60, TimeUnit.SECONDS));
        return new Result(p.exitValue(), out);
    }

    /**
     * Sources scripts/cleanup.sh in the install folder with {@code runtime/current} holding {@code pointer} (and a
     * runtime there whose java runs); {@code fresh} clears an earlier trial first. Returns the LAUNCHER_JAVA it leaves.
     */
    private String cleanup(String pointer, boolean fresh) throws Exception {
        Path runtime = install.resolve("runtime");
        if (fresh) {
            Files.deleteIfExists(runtime.resolve("trying"));
            Files.deleteIfExists(runtime.resolve("failed"));
        }
        write(runtime.resolve("current"), pointer + "\n");
        // a java that runs wherever the pointer leads inside the install folder: only the script's validation decides
        if (!Path.of(pointer).isAbsolute() && install.resolve(pointer).normalize().startsWith(install)) {
            Path java = install.resolve(pointer).normalize().resolve("bin/java");
            write(java, "#!/bin/sh\nexit 0\n");
            java.toFile().setExecutable(true);
        }
        Path script = Path.of("scripts/cleanup.sh").toAbsolutePath();
        List<String> command = new ArrayList<>(List.of("bash", "-c",
                "LAUNCHER_JAVA=\"today's java\"; . \"$1\" >/dev/null; printf '%s' \"$LAUNCHER_JAVA\"", "cleanup", script.toString()));
        Process p = new ProcessBuilder(command).directory(install.toFile()).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(p.waitFor(30, TimeUnit.SECONDS));
        return out;
    }

    private String pointer() throws IOException {
        Path p = install.resolve("runtime/current");
        return Files.exists(p) ? Files.readString(p).strip() : null;
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
