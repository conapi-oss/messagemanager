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

}
