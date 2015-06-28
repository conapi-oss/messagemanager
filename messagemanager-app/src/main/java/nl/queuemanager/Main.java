/**

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.queuemanager;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import nl.queuemanager.app.AppModule;
import nl.queuemanager.app.MMFrame;
import nl.queuemanager.app.PluginDescriptor;
import nl.queuemanager.core.PreconnectCoreModule;
import nl.queuemanager.core.configuration.XmlConfigurationModule;
import nl.queuemanager.core.events.ApplicationInitializedEvent;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.ui.PreconnectUIModule;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

public class Main {

	// Do not allow instances of this class
	private Main() {}
	
	public static void main(String[] args) {
		// Set look & feel to native
		setNativeLAF();
		enableSwingDebug();
		
		// Create the configuration module
		// FIXME set a correct filename, etc
		XmlConfigurationModule configurationModule = new XmlConfigurationModule("config.xml", "urn:blah");
		
		// Create the default modules
		List<Module> modules = new ArrayList<Module>();
		modules.add(configurationModule);
		modules.add(new AppModule());
		modules.add(new PreconnectCoreModule());
		modules.add(new PreconnectUIModule());
		
		// Now that the module list is complete, create the injector
		final Injector injector = Guice.createInjector(Stage.PRODUCTION, modules);
		
		// Invoke initializing the GUI on the EDT
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Set some platform properties before the UI really loads
				PlatformHelper helper = injector.getInstance(PlatformHelper.class);
				helper.setApplicationName("Message Manager");
				
				// Create the main application frame
				final JFrame frame = injector.getInstance(MMFrame.class);
				
				// Make the frame visible
				frame.setVisible(true);
				
				// Send the ApplicationInitializedEvent
				injector.getInstance(EventBus.class).post(new ApplicationInitializedEvent());
			}
		});
	}
	
	public static List<Module> loadPluginModules(List<PluginDescriptor> plugins, List<URL> classpath) {
		try {
			URLClassLoader classLoader = new URLClassLoader(classpath.toArray(new URL[classpath.size()]));
			System.out.println("Created classloader: " + classLoader);

			List<Module> result = new ArrayList<Module>();
			for(PluginDescriptor plugin: plugins) {
				Class<Module> moduleClass = (Class<Module>) classLoader.loadClass(plugin.getModuleClassName());
				Module module = moduleClass.newInstance();
				result.add(module);
			}
			return result;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void setNativeLAF() { 
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if(Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("awt.dynamicLayoutSupported")))
				Toolkit.getDefaultToolkit().setDynamicLayout(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void enableSwingDebug() {
		Toolkit.getDefaultToolkit().addAWTEventListener(new DebugEventListener(), AWTEvent.MOUSE_EVENT_MASK);
//		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TracingEventQueue());
	}
	
}
