package nl.queuemanager.core.tasks;

import com.google.inject.assistedinject.Assisted;

public interface PreconnectTaskFactory {
	// Release note & motd tasks
	public abstract CheckMotdTask checkMotdTask(@Assisted("uniqueId") String uniqueId, @Assisted("hostname") String hostname);
	public abstract CheckReleaseNoteTask checkReleaseNote(@Assisted("hostname") String hostname, @Assisted("buildId") String buildId);
}
