package nl.queuemanager.app;


import org.junit.Test;

import static nl.queuemanager.app.PluginModuleHelper.deriveModuleName;
import static org.junit.Assert.assertEquals;


public class PluginModuleHelperTest {

    @Test
    public void testDeriveModuleName() {
        // Test cases
        assertEquals("org.apache.servicemix.bundles.jzlib.jar", deriveModuleName("org.apache.servicemix.bundles.jzlib-1.0.7_2.jar"));
        assertEquals("zstd.jni.jar", deriveModuleName("zstd-jni-1.5.5-6.jar"));
        assertEquals("test.file.jar", deriveModuleName("test_file-1.2.3-SNAPSHOT.jar"));
        assertEquals("my.library.jar", deriveModuleName("my-library-2.0-RC1.jar"));
        assertEquals("commons.lang3.jar", deriveModuleName("commons-lang3-3.12.0.jar"));
        assertEquals("guava.jar", deriveModuleName("guava-33.5.0-jre.jar"));
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

}
