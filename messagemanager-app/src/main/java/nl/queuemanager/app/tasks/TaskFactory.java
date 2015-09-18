package nl.queuemanager.app.tasks;

import nl.queuemanager.app.Profile;

public interface TaskFactory {
	public abstract ActivateProfileTask activateProfile(Profile profile);
	
}
