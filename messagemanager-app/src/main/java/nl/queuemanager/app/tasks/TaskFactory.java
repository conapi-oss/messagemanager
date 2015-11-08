package nl.queuemanager.app.tasks;

import nl.queuemanager.Profile;

public interface TaskFactory {
	public abstract ActivateProfileTask activateProfile(Profile profile);
	
}
