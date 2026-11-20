package at.conapi.messagemanager.bootstrap;
/**
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.update4j.Archive;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.OS;
import org.update4j.UpdateOptions;
import org.update4j.UpdateResult;
import org.update4j.service.UpdateHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Message Manager's own Java runtime, in the background: once the application is up, this step reads the update
 * site's configuration for this platform ({@code runtime/<platform>.xml}), downloads and installs the runtime it
 * names into {@code runtime/<name>/<platform>/} under the install folder, and activates it only after it has proven
 * itself: every file matches its size and checksum, its executables are executable, the application is the one the
 * update site offers, and the runtime's own {@code java} has read the update site's setup.xml with this start's proxy
 * and TLS settings ({@link RuntimeCheck}).
 * <p>
 * Activating means writing {@code runtime/current}: one line, the runtime's home relative to the install folder. The
 * cleanup scripts read it at the next start and use that runtime when its {@code java -version} runs, marking the start
 * as a trial ({@code runtime/trying}); this step clears the mark once the application is up on it. A trial the
 * application never came up from sends the next start back to the Java it used before, and the runtime counts as
 * failed. In every other case the scripts leave the Java the installation runs on today. This step never asks, never
 * shows anything and never holds up a start; whatever happens is one line in {@code runtime/status.txt}.
 * <p>
 * The configuration is the truth, at every start that reaches it:
 * <ul>
 * <li>another name: installed beside the one in use, activated; old folders go at a later start;</li>
 * <li>no files (a withdrawn runtime): the pointer is removed;</li>
 * <li>a file of the active runtime missing or changed: the pointer is removed; a start that does not run on it
 * repairs the folder and activates it again. Files the user may adjust once installed (the trust store and
 * {@code conf/}) are left as they are.</li>
 * </ul>
 * A runtime that failed (its activation check, or a trial start) is remembered in {@code runtime/failed} and not tried
 * again until the configuration names another runtime.
 * <p>
 * It must run on every Update4J version installations hold (1.5.9 and later): it uses nothing newer.
 */
public final class RuntimeStep implements Runnable {

    static final String POINTER = "current";
    static final String STATUS = "status.txt";
    static final String TRYING = "trying";
    static final String FAILED = "failed";
    static final String DOWNLOAD = "download.zip";
    static final String HOME_PROPERTY = "runtime.home";
    static final long CHECK_SECONDS = 60;
    /** How long a start on the runtime may take to bring the application up before its trial counts as failed. */
    static final java.time.Duration TRIAL = java.time.Duration.ofMinutes(10);

    private final Path installDir;
    private final Path runtimeDir;
    private final String appConfigUrl;
    private final String platform;
    private final Path javaHome;

    RuntimeStep(Path installDir, String appConfigUrl, String platform, Path javaHome) {
        this.installDir = installDir;
        this.runtimeDir = installDir.resolve("runtime");
        this.appConfigUrl = appConfigUrl;
        this.platform = platform;
        this.javaHome = javaHome;
    }

    /** Starts the step for this installation on a daemon thread: quitting the application ends it, mid-download or not. */
    public static void startInBackground(String appConfigUrl) {
        RuntimeStep step = new RuntimeStep(Path.of("").toAbsolutePath(), appConfigUrl,
                platform(OS.CURRENT, System.getProperty("os.arch")), Path.of(System.getProperty("java.home")));
        Thread thread = new Thread(step, "java-runtime");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        String status;
        try {
            Optional<String> running = runningHome(installDir, javaHome);
            if (running.isPresent()) {
                // the application is up on a runtime: its trial start succeeded
                Files.deleteIfExists(runtimeDir.resolve(TRYING));
            } else {
                judgeTrial();
            }
            status = runOnce(running);
        } catch (Throwable t) {
            status = "failed: " + t;
        }
        writeStatus(status);
    }

    private String runOnce(Optional<String> running) throws IOException, InterruptedException {
        URI configUri = configUri(appConfigUrl, platform);
        Configuration config;
        try (Reader in = open(configUri)) {
            config = Configuration.read(in);
        } catch (FileNotFoundException | NoSuchFileException e) {
            return "no runtime for " + platform + " at " + configUri;
        } catch (IOException e) {
            return "update site not reached: " + configUri + ": " + e;
        }

        if (config.getFiles().isEmpty()) {
            // a withdrawn runtime: the next start uses the Java it used before
            removePointer();
            sweep(Optional.empty(), running);
            return "withdrawn: " + configUri + " lists no runtime";
        }
        String home = "runtime/" + config.getResolvedProperty(HOME_PROPERTY);
        if (!validHome(home)) {
            return "the configuration names no runtime home: " + configUri;
        }
        Path homeDir = installDir.resolve(home).toAbsolutePath().normalize();
        if (!config.getBasePath().toAbsolutePath().normalize().equals(runtimeDir.toAbsolutePath().normalize())) {
            return "the configuration installs into " + config.getBasePath() + ", not " + runtimeDir;
        }
        for (FileMetadata file : config.getFiles()) {
            if (!file.getPath().toAbsolutePath().normalize().startsWith(homeDir)) {
                return "the configuration lists " + file.getPath() + " outside " + home;
            }
        }
        String outcome = installAndActivate(config, home, homeDir, running);
        sweep(Optional.of(home), running);
        return outcome;
    }

    private String installAndActivate(Configuration config, String home, Path homeDir, Optional<String> running)
            throws IOException, InterruptedException {
        Optional<String> pointer = readPointer(installDir);
        if (differs(config, homeDir)) {
            if (running.isPresent() && running.get().equals(home)) {
                // a running Java's files are never replaced: the next start falls back and repairs them
                removePointer();
                return "the runtime this start runs on differs from its configuration: " + home;
            }
            if (pointer.isPresent() && pointer.get().equals(home)) {
                removePointer();
                pointer = Optional.empty();
            }
            Files.createDirectories(runtimeDir);
            long total = config.getFiles().stream().mapToLong(FileMetadata::getSize).sum();
            String noSpace = notEnoughSpace(Files.getFileStore(runtimeDir).getUsableSpace(), total);
            if (noSpace != null) {
                return noSpace;
            }
            String failure = download(config, homeDir);
            if (failure != null) {
                return "download failed: " + failure;
            }
            if (differs(config, homeDir)) {
                return "download failed: files of " + home + " still differ from the configuration";
            }
        }
        makeExecutable(homeDir);

        if (pointer.isPresent() && pointer.get().equals(home)) {
            return "active " + home;
        }
        String failed = readLine(runtimeDir.resolve(FAILED));
        if (failed != null && (failed + " ").startsWith(home + " ")) {
            return "not activated: " + failed;
        }
        if (!applicationCurrent()) {
            return "the runtime " + home + " waits for the application update the update site offers";
        }
        String failure = activationCheck(home);
        if (failure != null) {
            Files.writeString(runtimeDir.resolve(FAILED), home + " activation failed: " + oneLine(failure)
                    + System.lineSeparator(), StandardCharsets.UTF_8);
            return "activation failed " + home + ": " + failure;
        }
        writePointer(home);
        Files.deleteIfExists(runtimeDir.resolve(FAILED));
        return "active " + home;
    }

    /**
     * A trial mark older than {@link #TRIAL} is a start on the runtime that never brought the application up: that
     * runtime has failed, and the pointer to it goes. A younger mark may belong to a start still coming up, and stays.
     */
    private void judgeTrial() throws IOException {
        Path trying = runtimeDir.resolve(TRYING);
        if (!Files.exists(trying)
                || Files.getLastModifiedTime(trying).toInstant().isAfter(Instant.now().minus(TRIAL))) {
            return;
        }
        String tried = readLine(trying);
        tried = tried == null ? "" : tried.replace('\\', '/');
        Files.writeString(runtimeDir.resolve(FAILED), tried + " did not bring the application up"
                + System.lineSeparator(), StandardCharsets.UTF_8);
        Optional<String> pointer = readPointer(installDir);
        if (pointer.isPresent() && pointer.get().equals(tried)) {
            removePointer();
        }
        Files.deleteIfExists(trying);
    }

    /** Whether a file of the runtime is missing or differs; a file the user may adjust counts only when missing. */
    private static boolean differs(Configuration config, Path homeDir) throws IOException {
        for (FileMetadata file : config.getFiles()) {
            if (!keepAsIs(file, homeDir) && file.requiresUpdate()) {
                return true;
            }
        }
        return false;
    }

    private static boolean keepAsIs(FileMetadata file, Path homeDir) {
        Path path = file.getPath().toAbsolutePath().normalize();
        return userAdjustable(homeDir.relativize(path).toString().replace('\\', '/')) && Files.exists(path);
    }

    /** Whether the application on disk is the one the update site offers: a runtime is activated only under it. */
    private boolean applicationCurrent() {
        try (Reader in = open(URI.create(appConfigUrl))) {
            return !Configuration.read(in).requiresUpdate();
        } catch (IOException e) {
            return false;
        }
    }

    /** Downloads into a fresh archive and installs it; null when that worked, else why not. */
    private String download(Configuration config, Path homeDir) throws IOException {
        Path zip = runtimeDir.resolve(DOWNLOAD);
        removeDownload(); // what a killed download left
        UpdateResult result = config.update(UpdateOptions.archive(zip).updateHandler(new QuietDownload(homeDir)));
        if (result.getException() != null) {
            removeDownload();
            return String.valueOf(result.getException());
        }
        try {
            Archive.read(zip).install();
        } catch (IOException e) {
            removeDownload();
            return "could not install: " + e;
        }
        return null;
    }

    /**
     * Removes a download: the archive, and the temporary files Java's zip file system writes beside it, which stay
     * when its process dies or the disk fills up.
     */
    private void removeDownload() {
        try {
            Files.deleteIfExists(runtimeDir.resolve(DOWNLOAD));
            try (DirectoryStream<Path> temporary = Files.newDirectoryStream(runtimeDir, "zipfstmp*.tmp")) {
                for (Path file : temporary) {
                    Files.deleteIfExists(file);
                }
            }
        } catch (IOException e) {
            // another instance may hold them; a later start removes them
        }
    }

    /** Starts the runtime's own java with the activation check; null when it passed, else why not. */
    private String activationCheck(String home) throws IOException, InterruptedException {
        boolean windows = platform.startsWith("win-");
        Path java = installDir.resolve(home).resolve("bin").resolve(windows ? "java.exe" : "java");
        Path classPath;
        try {
            classPath = Path.of(RuntimeCheck.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            return "the bootstrap's own jar is unknown: " + e;
        }
        List<String> command = new ArrayList<>();
        command.add(java.toString());
        command.addAll(checkOptions(System.getProperties()));
        command.add("-cp");
        command.add(classPath.toString());
        command.add(RuntimeCheck.class.getName());
        command.add(setupUri(appConfigUrl).toString());

        Path output = Files.createTempFile("runtime-check", ".txt");
        try {
            Process process;
            try {
                process = new ProcessBuilder(command).directory(installDir.toFile()).redirectErrorStream(true)
                        .redirectOutput(output.toFile()).start();
            } catch (IOException e) {
                return "its java does not start: " + e.getMessage();
            }
            Thread end = new Thread(process::destroyForcibly);
            Runtime.getRuntime().addShutdownHook(end);
            try {
                if (!process.waitFor(CHECK_SECONDS, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    return "no answer within " + CHECK_SECONDS + " s";
                }
            } finally {
                try {
                    Runtime.getRuntime().removeShutdownHook(end);
                } catch (IllegalStateException shuttingDown) {
                    // the hook ends it
                }
            }
            if (process.exitValue() == 0) {
                return null;
            }
            List<String> lines = Files.readAllLines(output, StandardCharsets.UTF_8);
            String last = lines.stream().filter(l -> !l.isBlank()).reduce((a, b) -> b).orElse("");
            return last.isEmpty() ? "exit " + process.exitValue() : last.strip();
        } finally {
            Files.deleteIfExists(output);
        }
    }

    private void writePointer(String home) throws IOException {
        Path temp = runtimeDir.resolve(POINTER + ".new");
        // one line with this platform's line end, as the cleanup scripts read it (set /p, findstr, head)
        Files.writeString(temp, home + System.lineSeparator(), StandardCharsets.UTF_8);
        Files.move(temp, runtimeDir.resolve(POINTER), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void removePointer() throws IOException {
        Files.deleteIfExists(runtimeDir.resolve(POINTER));
    }

    private static String readLine(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            return lines.isEmpty() ? null : lines.get(0).strip();
        } catch (IOException e) {
            return null;
        }
    }

    private static String oneLine(String text) {
        return text.replaceAll("[\\r\\n]+", " ");
    }

    private void writeStatus(String status) {
        try {
            Files.createDirectories(runtimeDir);
            String line = Instant.now().truncatedTo(ChronoUnit.SECONDS) + " " + oneLine(status);
            Files.writeString(runtimeDir.resolve(STATUS), line + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Java runtime: " + status + " (status not written: " + e + ")");
        }
    }

    /**
     * Removes the old runtime folders: all but the one the configuration names, the one the pointer names and the one
     * this JVM runs from. One that cannot be removed (a Java running from it) stays for a later start.
     */
    private void sweep(Optional<String> configured, Optional<String> running) {
        Set<String> keep = new HashSet<>();
        for (Optional<String> home : List.of(configured, readPointer(installDir), running)) {
            home.ifPresent(h -> keep.add(h.split("/")[1]));
        }
        for (Path folder : foldersToRemove(runtimeDir, keep)) {
            try (Stream<Path> walk = Files.walk(folder)) {
                for (Path p : walk.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                    Files.deleteIfExists(p);
                }
            } catch (IOException e) {
                // a later start removes it
            }
        }
    }

    /** bin/* and the helpers the JDK starts as processes. Update4J before 1.5.11 installs no file executable. */
    private static void makeExecutable(Path home) throws IOException {
        if (!home.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            return;
        }
        List<Path> files = new ArrayList<>();
        if (Files.isDirectory(home.resolve("bin"))) {
            try (Stream<Path> bin = Files.list(home.resolve("bin"))) {
                bin.filter(Files::isRegularFile).forEach(files::add);
            }
        }
        for (String helper : List.of("lib/jspawnhelper", "lib/jexec")) {
            if (Files.isRegularFile(home.resolve(helper))) {
                files.add(home.resolve(helper));
            }
        }
        for (Path file : files) {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file);
            if (permissions.addAll(List.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_EXECUTE))) {
                Files.setPosixFilePermissions(file, permissions);
            }
        }
    }

    private static Reader open(URI uri) throws IOException {
        URLConnection connection = uri.toURL().openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(30 * 1000);
        connection.setReadTimeout(60 * 1000);
        return new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Opens a download with the patience corporate networks need: a 30 s connect and a 10 minute read timeout, as
     * antivirus and inspecting proxies can hold the end of a large file for minutes. The bootstrap's own updates and
     * the runtime's download use it alike.
     */
    static InputStream openPatiently(URI uri) throws IOException {
        URLConnection connection = uri.toURL().openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(30 * 1000);
        connection.setReadTimeout(10 * 60 * 1000);
        return connection.getInputStream();
    }

    /** Downloads without a word, and leaves the files the user may adjust as they are once they exist. */
    private static final class QuietDownload implements UpdateHandler {
        private final Path homeDir;

        QuietDownload(Path homeDir) {
            this.homeDir = homeDir;
        }

        @Override
        public boolean shouldCheckForUpdate(FileMetadata file) {
            return !keepAsIs(file, homeDir);
        }

        @Override
        public InputStream openDownloadStream(FileMetadata file) throws Throwable {
            return openPatiently(file.getUri());
        }
    }

    /** The platform's name in the update site: Update4J's name of the operating system and the JVM's own os.arch. */
    static String platform(OS os, String osArch) {
        return os.getShortName() + "-" + osArch;
    }

    /** Why a download of {@code total} bytes does not start with {@code usable} bytes free, or null when it can. */
    static String notEnoughSpace(long usable, long total) {
        // the download's temporary files, its archive and the installed files: about three times the runtime
        long needed = 3 * total;
        if (usable >= needed) {
            return null;
        }
        long mb = 1024 * 1024;
        return "not enough free disk space for the runtime: it needs " + (needed + mb - 1) / mb + " MB, "
                + usable / mb + " MB are free";
    }

    /** A file of a runtime the user may adjust (its trust store, its conf/), relative to the runtime's home. */
    static boolean userAdjustable(String relative) {
        return relative.equals("lib/security/cacerts") || relative.startsWith("conf/");
    }

    /** The platform's runtime configuration, beside the application's: {@code <site>/runtime/<platform>.xml}. */
    static URI configUri(String appConfigUrl, String platform) {
        return URI.create(appConfigUrl).resolve("../runtime/" + platform + ".xml");
    }

    /** The update site's setup.xml, which the activation check reads. */
    static URI setupUri(String appConfigUrl) {
        return URI.create(appConfigUrl).resolve("../setup.xml");
    }

    private static final Pattern HOME = Pattern.compile("runtime/[A-Za-z0-9_-][A-Za-z0-9._-]*/[A-Za-z0-9_-][A-Za-z0-9._-]*");

    /**
     * A runtime home as the pointer holds it: {@code runtime/<name>/<platform>}, each part letters, digits, {@code .},
     * {@code _} and {@code -}, not starting with a dot, and no {@code ..} anywhere. cleanup.sh and cleanup.cmd apply
     * the same rule (a test runs cleanup.sh against this one).
     */
    static boolean validHome(String home) {
        return home != null && HOME.matcher(home).matches() && !home.contains("..");
    }

    /** The home {@code runtime/current} names, when it holds a valid one on its first line. */
    static Optional<String> readPointer(Path installDir) {
        String first = readLine(installDir.resolve("runtime").resolve(POINTER));
        return validHome(first) ? Optional.of(first) : Optional.empty();
    }

    /** The home this JVM runs from, when it is a runtime under {@code runtime/}. */
    static Optional<String> runningHome(Path installDir, Path javaHome) {
        try {
            Path runtimeDir = installDir.resolve("runtime").toRealPath();
            Path home = javaHome.toRealPath();
            if (!home.startsWith(runtimeDir)) {
                return Optional.empty();
            }
            String candidate = "runtime/" + runtimeDir.relativize(home).toString().replace('\\', '/');
            return validHome(candidate) ? Optional.of(candidate) : Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /** The folders under {@code runtime/} to remove: every folder whose name is not kept. Files stay. */
    static List<Path> foldersToRemove(Path runtimeDir, Set<String> keepNames) {
        List<Path> remove = new ArrayList<>();
        if (!Files.isDirectory(runtimeDir)) {
            return remove;
        }
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(runtimeDir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry) && !keepNames.contains(entry.getFileName().toString())) {
                    remove.add(entry);
                }
            }
        } catch (IOException e) {
            return List.of();
        }
        remove.sort(Comparator.naturalOrder());
        return remove;
    }

    private static final List<String> CHECK_PREFIXES = List.of("http.", "https.", "socksProxy", "socksNonProxyHosts",
            "java.net.", "javax.net.", "jdk.http.", "jdk.tls.");

    /** The proxy and TLS system properties of this JVM, as {@code -D} options for the activation check. */
    static List<String> checkOptions(Properties system) {
        return system.stringPropertyNames().stream()
                .filter(key -> CHECK_PREFIXES.stream().anyMatch(key::startsWith))
                .sorted()
                .map(key -> "-D" + key + "=" + system.getProperty(key))
                .collect(Collectors.toList());
    }
}
