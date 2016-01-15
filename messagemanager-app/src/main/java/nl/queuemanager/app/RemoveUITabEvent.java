package nl.queuemanager.app;


public class RemoveUITabEvent {
	private final int key;
	
	public RemoveUITabEvent(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}

}
