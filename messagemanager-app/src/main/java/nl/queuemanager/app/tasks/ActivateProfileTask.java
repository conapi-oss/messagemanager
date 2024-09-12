package nl.queuemanager.app.tasks;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.EventBus;
import com.google.inject.*;
import com.google.inject.Module;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.Profile;
import nl.queuemanager.ProfileActivatedEvent;
import nl.queuemanager.UITabProviderPlugin;
import nl.queuemanager.app.PluginDescriptor;
import nl.queuemanager.app.PluginManager;
import nl.queuemanager.core.CoreModule;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.util.CoreException;
import nl.queuemanager.ui.UIModule;

import jakarta.inject.Inject;

import java.net.URL;
import java.util.*;

public class ActivateProfileTask extends Task {
	public static final String LAST_ACTIVE_PROFILE = "lastActiveProfile";

	private final Injector parentInjector;
	private final PluginManager pluginManager;
	private final Profile profile;
	private final CoreConfiguration config;
	
	@Inject
	protected ActivateProfileTask(Injector parentInjector, EventBus eventBus, PluginManager pluginManager, CoreConfiguration config, @Assisted Profile profile) {
		super(null, eventBus);
		this.parentInjector = parentInjector;
		this.pluginManager = pluginManager;
		this.profile = profile;
		this.config = config;
	}

	@Override
	public void execute() throws Exception {
		// Transform all plugin class names into plugindescriptors.
		// first get the profile plugins, use LinkedHashSet to preserve the order
		final Set<String> plugins = new LinkedHashSet<String>();

		// add the profile plugins
		plugins.addAll(profile.getPlugins());

		//TODO: need to add a way to specify global/generic plugins
		// for now we just have this one:
		plugins.add("at.conapi.scripting.ScriptingModule");

		// then add the UI tab providers
		Collection<PluginDescriptor> pluginDescriptors = Collections2.transform(plugins, new Function<String, PluginDescriptor>() {
			@Override
			public PluginDescriptor apply(String pluginClassName) {
				PluginDescriptor ret = pluginManager.getPluginByClassName(pluginClassName);

				if(ret == null) {
					ret = pluginManager.downloadPluginByClassname(pluginClassName);
				}
				return ret;
			}
		});

		// Load the configured plugin modules
		final List<Module> modules = new ArrayList<Module>();
		modules.add(new CoreModule());
		modules.add(new UIModule());

		// Load all the modules into the plugin classloader
		for(PluginDescriptor pluginDescriptor: pluginDescriptors) {
			// pass null for the non profile plugins to avoid setting the context/worker classloader
			List<URL> classpath = pluginDescriptor.getModuleClassName().contains("Scripting")?null:profile.getClasspath();
			final List<Module> pluginModules = pluginManager.loadPluginModules(Collections.singletonList(pluginDescriptor),classpath );
			modules.addAll(pluginModules);
		}

		// Add the UI tab provider plugins
		try {
			final Injector injector = parentInjector.createChildInjector(modules);
			Map<Key<?>, Binding<?>> bindings = injector.getBindings();
			for (Binding<?> binding : bindings.values()) {
				if (binding.getKey().getTypeLiteral().getRawType().equals(UITabProviderPlugin.class)) {
					UITabProviderPlugin provider = (UITabProviderPlugin) binding.getProvider().get();
					provider.initialize();
				}
			}
		} catch (NoClassDefFoundError e) {
			// Report the missing class to the user
			this.dispatchTaskError(new CoreException("Unable to initialize UI tab. Class " + e.getMessage() + " could not be found", e));
			return;
		}

		// Add the connectivity provider plugin
		try {
			final Injector injector = parentInjector.createChildInjector(modules);
			final ConnectivityProviderPlugin provider = injector.getInstance(ConnectivityProviderPlugin.class);
			provider.initialize();
		} catch (NoClassDefFoundError e) {
			// Report the missing class to the user
			this.dispatchTaskError(new CoreException("Unable to activate profile. Class " + e.getMessage() + " could not be found", e));
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
