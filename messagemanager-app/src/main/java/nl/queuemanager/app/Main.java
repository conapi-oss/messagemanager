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
package nl.queuemanager.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import nl.queuemanager.core.DebugProperty;
import nl.queuemanager.core.PreconnectCoreModule;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.events.ApplicationInitializedEvent;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.core.platform.QuitEvent;
import nl.queuemanager.core.task.MultiQueueTaskExecutorModule;
import nl.queuemanager.debug.DebugEventListener;
import nl.queuemanager.debug.TracingEventQueue;
import nl.queuemanager.ui.PreconnectUIModule;
import nl.queuemanager.ui.settings.SettingsModule;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

	// Do not allow instances of this class
	private Main() {}
	
	public static void main(String[] args) {
		enableFlatLAF();
		enableDebugLogging(DebugProperty.developer.isEnabled());

		// Create boot injector so we can set LAF before creating any UI
		final Injector bootInjector = Guice.createInjector(Stage.PRODUCTION, new BootModule());
		
		// Set the LAF
		// setConfiguredLAF(bootInjector.getInstance(CoreConfiguration.class));
		
		if(DebugProperty.enableSwingDebug.isEnabled()) {
			enableSwingDebug();
		}
		
		// Now create the real injector as a child with the default modules
		List<com.google.inject.Module> modules = new ArrayList<>();
		modules.add(new SettingsModule());
        modules.add(new MultiQueueTaskExecutorModule());
		modules.add(new PreconnectCoreModule());
		modules.add(new PreconnectUIModule());
		modules.add(new AppModule());
		
		// Now that the module list is complete, create the injector
		final Injector injector = bootInjector.createChildInjector(modules);
				
		// Enable the event debugger
		if(DebugProperty.developer.isEnabled()) {
			injector.getInstance(EventBusDebugger.class);
		}
		
		// FIXME Find all installed plugins and load their default profiles
		injector.getInstance(PluginManager.class);
		
		final EventBus eventBus = injector.getInstance(EventBus.class);
		
		// Invoke initializing the GUI on the EDT
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Set some platform properties before the UI really loads
				PlatformHelper helper = injector.getInstance(PlatformHelper.class);
				helper.setApplicationName("Message Manager");
				
				// Create the main application frame
				final JFrame frame = injector.getInstance(MMFrame.class);

				// When this frame closes, quit the application by posting a QuitEvent
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						eventBus.post(new QuitEvent());
					}
				});
				
				// Make the frame visible
				frame.setVisible(true);
				
				// Send the ApplicationInitializedEvent
				eventBus.post(new ApplicationInitializedEvent());
			}
		});
	}
	
	private static void enableDebugLogging(boolean enabled) {
		Logger ourLogger = Logger.getLogger(Main.class.getPackage().getName());
		ourLogger.setLevel(Level.ALL);
	}

	private static void enableFlatLAF() {
		FlatLightLaf.setup();
		try {
			UIManager.setLookAndFeel( new FlatLightLaf() );
		} catch (UnsupportedLookAndFeelException e) {
			throw new RuntimeException(e);
		}
	}

	private static void setConfiguredLAF(CoreConfiguration config) { 
		String configuredClassName = config.getUserPref(
				CoreConfiguration.PREF_LOOK_AND_FEEL,
				UIManager.getSystemLookAndFeelClassName());
				
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if (info.getClassName().equals(configuredClassName)) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
			if(Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("awt.dynamicLayoutSupported")))
				Toolkit.getDefaultToolkit().setDynamicLayout(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void enableSwingDebug() {
		Toolkit.getDefaultToolkit().addAWTEventListener(new DebugEventListener(), AWTEvent.MOUSE_EVENT_MASK);
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TracingEventQueue());
	}

}
