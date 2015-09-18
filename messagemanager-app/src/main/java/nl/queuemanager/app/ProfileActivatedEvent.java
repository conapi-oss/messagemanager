package nl.queuemanager.app;

public class ProfileActivatedEvent {

	private final Profile profile;
	
	public ProfileActivatedEvent(Profile profile) {
		this.profile = profile;
	}
	
	public Profile getProfile() {
		return profile;
	}

}
