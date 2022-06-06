package nl.queuemanager.ui.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMiscUtils {

	@Test
	public void testHumanReadableSize0() {
		assertEquals("", MiscUtils.humanReadableSize(0));
	}
	
	@Test
	public void humanReadableSizeTinyBytes() {
		assertEquals("12B", MiscUtils.humanReadableSize(12));
	}
	
	@Test
	public void humanReadableSizeBytes() {
		assertEquals("123B", MiscUtils.humanReadableSize(123));
	}
	
	@Test
	public void humanReadableSizeKiloBytes() {
		assertEquals("123kB", MiscUtils.humanReadableSize(123*1024));
	}
	
	@Test
	public void humanReadableSizeMegaBytes() {
		assertEquals("123MB", MiscUtils.humanReadableSize(123*1024*1024));
	}
	
	@Test
	public void humanReadableSizeRoundTo1GB() {
		assertEquals("1GB", MiscUtils.humanReadableSize(1000*1024*1024));
	}
	
	@Test
	public void humanReadableRoundToDecimalGB() {
		assertEquals("1.3GB", MiscUtils.humanReadableSize(1343*1024*1024));
	}
	
	@Test
	public void humanReadableSizeGigaBytes() {
		assertEquals("123GB", MiscUtils.humanReadableSize(123L*1024L*1024L*1024L));
	}
	
	@Test
	public void humanReadableSizeTeraBytes() {
		assertEquals("123TB", MiscUtils.humanReadableSize(123L*1024L*1024L*1024L*1024L));
	}
	
}
