package at.conapi.messagemanager.bootstrap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.OS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * When the start updates the application. The local app/config.xml is replaced with the update site's in the
 * background of an earlier start, so it can name files that are not installed yet - also on a start that does not
 * ask the update site.
 */
public class StartupViewTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void aStartThatDoesNotAskTheSiteUpdatesWhenTheLocalConfigNamesAFileThatIsNotInstalled() throws IOException {
        Path install = folder.newFolder("install").toPath();
        Configuration config = configNaming("app-2.jar", install);

        assertTrue(StartupView.updateRequired(config));
    }

    @Test
    public void aStartThatDoesNotAskTheSiteLaunchesWhenEveryFileIsInstalled() throws IOException {
        Path install = folder.newFolder("install").toPath();
        Configuration config = configNaming("app-2.jar", install);
        Files.copy(folder.getRoot().toPath().resolve("site/app-2.jar"), install.resolve("app-2.jar"));

        assertFalse(StartupView.updateRequired(config));
    }

    @Test
    public void anUpdateThatAddsAJarTheLaunchNeedsIsInstalledWithoutOfferingSkip() throws IOException {
        Path install = folder.newFolder("install").toPath();
        Configuration config = configNaming("app-2.jar", install, null, true);

        assertTrue(StartupView.launchNeedsAFileThatIsNotInstalled(config));
        assertFalse(StartupView.offersSkip(false, config));
    }

    @Test
    public void anUpdateThatAddsOnlyAFileTheLaunchDoesNotNeedIsOfferedWithSkip() throws IOException {
        Path install = folder.newFolder("install").toPath();
        Configuration config = configNaming("clients/kafka/4.1/kafka-clients.jar", install, null, false);

        assertTrue(StartupView.updateRequired(config));
        assertTrue(StartupView.offersSkip(false, config));
    }

    @Test
    public void anUpdateThatOnlyChangesInstalledFilesIsOfferedWithSkip() throws IOException {
        Path install = folder.newFolder("install").toPath();
        Configuration config = configNaming("app-2.jar", install, null, true);
        Files.write(install.resolve("app-2.jar"), "the release before".getBytes(StandardCharsets.UTF_8));

        assertTrue(StartupView.updateRequired(config));
        assertTrue(StartupView.offersSkip(false, config));
    }

    @Test
    public void aJarForAnotherPlatformIsNotExpectedToBeInstalled() throws IOException {
        Path install = folder.newFolder("install").toPath();
        OS another = OS.CURRENT == OS.WINDOWS ? OS.MAC : OS.WINDOWS;
        Configuration config = configNaming("app-2.jar", install, another, true);

        assertFalse(StartupView.launchNeedsAFileThatIsNotInstalled(config));
    }

    @Test
    public void autoUpdateNeverOffersSkip() throws IOException {
        Path install = folder.newFolder("install").toPath();
        Configuration config = configNaming("app-2.jar", install, null, true);
        Files.write(install.resolve("app-2.jar"), "the release before".getBytes(StandardCharsets.UTF_8));

        assertFalse(StartupView.offersSkip(true, config));
    }

    private Configuration configNaming(String file, Path install) throws IOException {
        return configNaming(file, install, null, true);
    }

    private Configuration configNaming(String file, Path install, OS os, boolean modulepath) throws IOException {
        Path site = folder.newFolder("site").toPath();
        Path source = site.resolve(file);
        Files.createDirectories(source.getParent());
        Files.write(source, jar("the new release"));
        return Configuration.builder()
                .baseUri(site.toUri())
                .basePath(install)
                .file(FileMetadata.readFrom(source).path(file).os(os).modulepath(modulepath))
                .build();
    }

    /** A jar: Update4J keeps a file on the module path only when it is a zip. */
    private static byte[] jar(String content) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("content.txt"));
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return bytes.toByteArray();
    }
}
