package nl.queuemanager.app;

import nl.queuemanager.Profile;

public class ProfileActivatedEvent {

	private final Profile profile;
	
	public ProfileActivatedEvent(Profile profile) {
		this.profile = profile;
	}
	
	public Profile getProfile() {
		return profile;
	}

}
