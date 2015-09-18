package nl.queuemanager.app.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.Assisted;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.app.PluginDescriptor;
import nl.queuemanager.app.PluginManager;
import nl.queuemanager.app.Profile;
import nl.queuemanager.app.ProfileActivatedEvent;
import nl.queuemanager.core.CoreModule;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.ui.UIModule;

public class ActivateProfileTask extends Task {

	private final Injector parentInjector;
	private final PluginManager pluginManager;
	private final Profile profile;
	
	@Inject
	protected ActivateProfileTask(Injector parentInjector, EventBus eventBus, PluginManager pluginManager, @Assisted Profile profile) {
		super(null, eventBus);
		this.parentInjector = parentInjector;
		this.pluginManager = pluginManager;
		this.profile = profile;
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
		final Injector injector = parentInjector.createChildInjector(modules);
		ConnectivityProviderPlugin provider = injector.getInstance(ConnectivityProviderPlugin.class);
		provider.initialize();
		
		eventBus.post(new ProfileActivatedEvent(profile));
	}
	
	@Override
	public String toString() {
		return "Activating profile " + profile.getName();
	}

}
