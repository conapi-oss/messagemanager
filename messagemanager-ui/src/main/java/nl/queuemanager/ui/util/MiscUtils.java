package nl.queuemanager.ui.util;

import java.text.DecimalFormat;

public abstract class MiscUtils {
	
	public static String humanReadableSize(double bytes) {
		if(bytes < 1)
			return "";
		
	    int i = 0;
	    String byteUnits[] = new String[] {"B", "kB", "MB", "GB", "TB"};
	    while (bytes > 999 && i<byteUnits.length) {
	        bytes = bytes / 1024;
	        i++;
	    }

	    DecimalFormat df = new DecimalFormat(bytes > 100 ? "#" : "#.#");
	    return df.format(bytes) + byteUnits[i];
	}	

	/**
	 * Text for a component that renders HTML (a dialog's message, an editor pane): what it holds - a broker's or a
	 * server's words, a folder name - shows as text and is never read as markup. Null stays null.
	 */
	public static String html(String text) {
		if(text == null)
			return null;
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}

}
