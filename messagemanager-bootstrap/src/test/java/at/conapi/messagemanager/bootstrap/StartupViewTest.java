package at.conapi.messagemanager.bootstrap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    private Configuration configNaming(String file, Path install) throws IOException {
        Path site = folder.newFolder("site").toPath();
        Path source = site.resolve(file);
        Files.write(source, "the new release".getBytes(StandardCharsets.UTF_8));
        return Configuration.builder()
                .baseUri(site.toUri())
                .basePath(install)
                .file(FileMetadata.readFrom(source).path(file))
                .build();
    }
}
