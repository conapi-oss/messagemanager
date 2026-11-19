package nl.queuemanager.ui;

import org.junit.Test;

import java.lang.module.FindException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The error dialog renders its text as HTML: what an exception says - often text a broker or a server sent - must
 * reach it escaped, while the markup the dialog adds itself stays markup.
 */
public class ErrorListenerTest {

	@Test
	public void anExceptionsMessageReachesTheDialogEscaped() {
		String text = ErrorListener.translateExceptionMessage(
				new RuntimeException("<a href='file:///etc/hosts'>details</a> <img src='http://example.invalid/x'>"));

		assertFalse(text, text.contains("<"));
		assertTrue(text, text.contains("&lt;a href=&#39;file:///etc/hosts&#39;&gt;"));
	}

	@Test
	public void aCausesMessageReachesTheDialogEscaped() {
		String text = ErrorListener.translateExceptionMessage(
				new RuntimeException(new IllegalStateException("broker says <b>no</b>")));

		assertFalse(text, text.contains("<"));
		assertTrue(text, text.contains("&lt;b&gt;no&lt;/b&gt;"));
	}

	@Test
	public void theClassLoadingHintKeepsItsOwnLinkAndEscapesTheModuleError() {
		String text = ErrorListener.translateExceptionMessage(new FindException("Module <x> not found"));

		assertTrue(text, text.contains("<a href='https://conapi-oss.github.io/messagemanager/docs/troubleshooting/howto-classloadingissue'>"));
		assertTrue(text, text.contains("Module &lt;x&gt; not found"));
	}
}
