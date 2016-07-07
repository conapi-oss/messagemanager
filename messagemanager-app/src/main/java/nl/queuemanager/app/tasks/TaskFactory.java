package nl.queuemanager.app.tasks;

import com.google.inject.assistedinject.Assisted;

import nl.queuemanager.Profile;
import nl.queuemanager.core.tasks.CheckMotdTask;
import nl.queuemanager.core.tasks.CheckReleaseNoteTask;

public interface TaskFactory {
	public abstract ActivateProfileTask activateProfile(Profile profile);
	public abstract SetUserPrefTask setUserPref(@Assisted("key") String key, @Assisted("value") String value);
	
	// Release note & motd tasks
	public abstract CheckMotdTask checkMotdTask(@Assisted("uniqueId") String uniqueId, @Assisted("hostname") String hostname);
	public abstract CheckReleaseNoteTask checkReleaseNote(@Assisted("hostname") String hostname, @Assisted("buildId") String buildId);
	
}
