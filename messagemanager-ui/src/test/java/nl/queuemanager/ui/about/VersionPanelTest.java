package nl.queuemanager.ui.about;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The About view names the Java the application runs on: its version, vendor and folder, so a screenshot tells which
 * Java an installation uses. The view renders HTML, and a folder name can hold characters HTML reads as markup.
 */
public class VersionPanelTest {

	@Test
	public void theJavaLineNamesVersionVendorAndFolder() {
		assertEquals("Java: 25.0.5.1 (Amazon.com Inc.), /opt/mm/runtime/25.0.5.1.2-0123456789ab/linux-amd64",
				VersionPanel.javaLine("25.0.5.1", "Amazon.com Inc.", "/opt/mm/runtime/25.0.5.1.2-0123456789ab/linux-amd64"));
	}

	@Test
	public void aFolderNameReachesTheViewEscaped() {
		String line = VersionPanel.javaLine("17.0.13", "Eclipse Adoptium", "C:\\Tools & <Apps>\\jre");
		assertEquals("Java: 17.0.13 (Eclipse Adoptium), C:\\Tools &amp; &lt;Apps&gt;\\jre", line);
	}

	@Test
	public void theApplicationsOwnJavaHasALine() {
		String line = VersionPanel.javaLine();
		assertTrue(line, line.startsWith("Java: " + System.getProperty("java.version") + " ("));
	}
}
