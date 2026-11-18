package nl.queuemanager.app;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

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
