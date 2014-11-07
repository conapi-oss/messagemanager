package nl.queuemanager.core.util;

import java.util.EventObject;

public class ReleasePropertiesEvent extends EventObject {
	public static enum EVENT {
		RELEASE_NOTES_FOUND,
		MOTD_FOUND
	}
	
	private final EVENT id; 
	private final Object info; 
	
	public ReleasePropertiesEvent(EVENT id, Object source, Object info) {
		super(source);
		this.id = id;
		this.info = info;
	}

	public EVENT getId() {
		return id;
	}

	public Object getInfo() {
		return info;
	}
}

