package nl.queuemanager.app.tasks;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.task.BackgroundTask;

import javax.inject.Inject;

public class SetUserPrefTask extends BackgroundTask {

	private final CoreConfiguration config;
	private final String key;
	private final String value;
	
	@Inject
	public SetUserPrefTask(EventBus eventBus, CoreConfiguration config, @Assisted("key") String key, @Assisted("value") String value) {
		super(null, eventBus);
		this.config = config;
		this.key = key;
		this.value = value;
	}
	
	@Override
	public void execute() throws Exception {
		config.setUserPref(key, value);
	}

}
