package nl.queuemanager.app;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static nl.queuemanager.app.PluginModuleHelper.deriveModuleName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@code @MM_HOME@} in a profile's classpath must name the folder the client libraries are installed into, which is
 * where updates write them: the folder the app runs from.
 */
public class PluginModuleHelperTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testDeriveModuleName() {
        // Test cases
        assertEquals("org.apache.servicemix.bundles.jzlib.jar", deriveModuleName("org.apache.servicemix.bundles.jzlib-1.0.7_2.jar"));
        assertEquals("zstd.jni.jar", deriveModuleName("zstd-jni-1.5.5-6.jar"));
        assertEquals("test.file.jar", deriveModuleName("test_file-1.2.3-SNAPSHOT.jar"));
        assertEquals("my.library.jar", deriveModuleName("my-library-2.0-RC1.jar"));
        assertEquals("commons.lang3.jar", deriveModuleName("commons-lang3-3.12.0.jar"));
        assertEquals("guava.jar", deriveModuleName("guava-33.6.0-jre.jar"));
        assertEquals("lz4.java.jar", deriveModuleName("lz4-java-1.8.0.jar"));
        
        // Test case for name starting with a number
        assertEquals("m4j.time.jar", deriveModuleName("4j-time-1.0.0.jar"));
        
        // Test case for name with multiple consecutive non-alphanumeric characters
        assertEquals("weird.name.jar", deriveModuleName("weird__--..name-1.0.0.jar"));
        
        // Test case for name with only version-like information
        // assertEquals("m.jar", deriveModuleName("1.0.0.jar"));
        
        // Test case for empty name after processing
        //assertEquals("m.jar", deriveModuleName("-1.0.0.jar"));
    }

	@Test
	public void theFolderTheAppRunsFromIsMmHomeWhenItHoldsTheClients() throws Exception {
		File runFolder = folder.newFolder("Application Support");
		assertTrue(new File(runFolder, "clients").mkdir());

		assertEquals(runFolder.getAbsolutePath(), PluginModuleHelper.mmHome(runFolder.getAbsolutePath(), "/extracted/archive"));
	}

	@Test
	public void theVariableIsMmHomeWhenTheFolderTheAppRunsFromHoldsNoClients() throws Exception {
		File elsewhere = folder.newFolder("elsewhere");

		assertEquals("/extracted/archive", PluginModuleHelper.mmHome(elsewhere.getAbsolutePath(), "/extracted/archive"));
	}

	@Test
	public void thereIsNoMmHomeWithoutClientsAndWithoutTheVariable() throws Exception {
		File elsewhere = folder.newFolder("elsewhere");

		assertNull(PluginModuleHelper.mmHome(elsewhere.getAbsolutePath(), null));
	}
}
