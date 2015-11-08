package nl.queuemanager.app.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.Profile;
import nl.queuemanager.app.PluginDescriptor;
import nl.queuemanager.app.PluginManager;
import nl.queuemanager.app.ProfileActivatedEvent;
import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.CoreModule;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.ui.UIModule;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.Assisted;

public class ActivateProfileTask extends Task {
	public static final String LAST_ACTIVE_PROFILE = "lastActiveProfile";

	private final Injector parentInjector;
	private final PluginManager pluginManager;
	private final Profile profile;
	private final Configuration config;
	
	@Inject
	protected ActivateProfileTask(Injector parentInjector, EventBus eventBus, PluginManager pluginManager, Configuration config, @Assisted Profile profile) {
		super(null, eventBus);
		this.parentInjector = parentInjector;
		this.pluginManager = pluginManager;
		this.profile = profile;
		this.config = config;
	}

	@Override
	public void execute() throws Exception {
		// Transform all plugin class names into plugindescriptors.
		Collection<PluginDescriptor> pluginDescriptors = Collections2.transform(profile.getPlugins(), new Function<String, PluginDescriptor>() {
			@Override
			public PluginDescriptor apply(String pluginClassName) {
				PluginDescriptor ret = pluginManager.getPluginByClassName(pluginClassName);
				if(ret == null) {
					ret = pluginManager.downloadPluginByClassname(pluginClassName);
				}
				return ret;
			}
		});
		
		// Load all the modules into the plugin classloader
		List<Module> pluginModules = pluginManager.loadPluginModules(pluginDescriptors, profile.getClasspath());
		
		// Load the configured plugin modules
		List<Module> modules = new ArrayList<Module>();
		modules.add(new CoreModule());
		modules.add(new UIModule());
		modules.addAll(pluginModules);
		try {
			final Injector injector = parentInjector.createChildInjector(modules);
			ConnectivityProviderPlugin provider = injector.getInstance(ConnectivityProviderPlugin.class);
			provider.initialize();
		} catch (NoClassDefFoundError e) {
			// Report the missing class to the user
			this.dispatchTaskError("Unable to activate profile. Class " + e.getMessage() + " could not be found");
			return;
		}

		// Save the last active profile
		config.setUserPref(LAST_ACTIVE_PROFILE, profile.getId());
		
		// Send the profile activated event
		eventBus.post(new ProfileActivatedEvent(profile));
	}
	
	@Override
	public String toString() {
		return "Activating profile " + profile.getName();
	}

}
