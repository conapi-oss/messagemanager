package nl.queuemanager.app;

import nl.queuemanager.ui.UITab;

public class AddUITabEvent {
	private final int key;
	private final UITab tab;
	
	public AddUITabEvent(int key, UITab tab) {
		this.key = key;
		this.tab = tab;
	}

	public int getKey() {
		return key;
	}

	public UITab getTab() {
		return tab;
	}
	
	public String toString() {
		return getClass().getName() + "(" + key + ", " + tab.getUITabName() + ")";
	}
}
