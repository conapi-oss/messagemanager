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
package nl.queuemanager.smm;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.CoreModule;
import nl.queuemanager.smm.ui.ConnectionTabPanel;
import nl.queuemanager.smm.ui.SMMFrame;
import nl.queuemanager.smm.ui.SMMUIModule;
import nl.queuemanager.ui.UIModule;

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
		
		// Create the default modules
		List<Module> modules = new ArrayList<Module>();
		modules.add(new SMMConfigurationModule());
		modules.add(new CoreModule());
		modules.add(new UIModule());
		modules.add(new SMMModule());
		modules.add(new SMMUIModule());
		
		// Load plugin modules
		modules.addAll(createPluginModules());
		
		// Now that the module list is complete, create the injector
		Injector injector = Guice.createInjector(Stage.PRODUCTION, modules);
		
		// Create the main application frame
		final SMMFrame frame = injector.getInstance(SMMFrame.class);
		
		// Make the frame visible
		frame.setVisible(true);
		
		// Pop up the connection dialog
		injector.getInstance(ConnectionTabPanel.class).showDefaultConnectionDialog();
	}
	
	/**
	 * Load initial Injector to be able to read configuration for plugin loading.
	 * For some reason, Guice complains about things already being injected when
	 * child injectors are used. Until I find a solution for that, we dicard this
	 * injector and configuration object after use and use it only to retrieve the
	 * list of plugin modules to load.
	 */
	private static List<Module> createPluginModules() {
		Injector configInjector = Guice.createInjector(Stage.PRODUCTION, new SMMConfigurationModule());
		Configuration config = configInjector.getInstance(Configuration.class);
		
		String[] moduleNameList = config.getUserPref(Configuration.PREF_PLUGIN_MODULES, "").split(",");
		List<Module> modules = new ArrayList<Module>(moduleNameList.length);
		
		for(String moduleName: moduleNameList) {
			if(moduleName.length() == 0)
				continue;
			
			Module module = loadModule(moduleName);
			if(module != null && module instanceof Module) {
				modules.add(module);
			}
		}
		
//		modules.add(loadModule("nl.queuemanager.scripting.ScriptingModule"));
		
		return modules;
	}

	private static Module loadModule(String moduleName) {
		try {
			return (Module) Class.forName(moduleName).newInstance();
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

	
}
