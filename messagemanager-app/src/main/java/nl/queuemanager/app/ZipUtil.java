package nl.queuemanager.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ZipUtil {

	private ZipUtil() {	}

	public static InputStream openStreamForZipEntry(ZipFile zipFile, ZipEntry entry) throws IOException {
		if(entry != null) {
			return zipFile.getInputStream(entry);
		}
		
		return null;
	}
}
