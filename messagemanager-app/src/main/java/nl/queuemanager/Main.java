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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import nl.queuemanager.app.AppModule;
import nl.queuemanager.app.EventBusDebugger;
import nl.queuemanager.app.MMFrame;
import nl.queuemanager.app.PluginManager;
import nl.queuemanager.core.DebugProperty;
import nl.queuemanager.core.PreconnectCoreModule;
import nl.queuemanager.core.configuration.XmlConfigurationModule;
import nl.queuemanager.core.events.ApplicationInitializedEvent;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.core.platform.QuitEvent;
import nl.queuemanager.debug.DebugEventListener;
import nl.queuemanager.debug.TracingEventQueue;
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
		fixPolicyForWebStart();
		
		// Set look & feel to native
		setNativeLAF();
		
		if(DebugProperty.enableSwingDebug.isEnabled()) {
			enableSwingDebug();
		}
		
		// Create the default modules
		List<Module> modules = new ArrayList<Module>();
		modules.add(new XmlConfigurationModule("config.xml", "urn:queuemanager-config"));
		modules.add(new PreconnectCoreModule());
		modules.add(new PreconnectUIModule());
		modules.add(new AppModule());
		
		// Now that the module list is complete, create the injector
		final Injector injector = Guice.createInjector(Stage.PRODUCTION, modules);
		
		// Enable the event debugger
		injector.getInstance(EventBusDebugger.class);
		
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
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TracingEventQueue());
	}
	
	/**
	 * When running in Web Start, the ClassLoaders created by the application do not inherit the permissions
	 * of the web start Class Loader. This basically grants all permissions to anything. Not very secure, but
	 * it works for the moment. As long as we don't load any untrusted plugins it should be fine.
	 */
	private static void fixPolicyForWebStart() {
		Policy.setPolicy(new Policy() {
			public PermissionCollection getPermissions(CodeSource codesource) {
				Permissions perms = new Permissions();
				perms.add(new AllPermission());
				return(perms);
			}
			
			public void refresh() {}
		});
	}
	
}
