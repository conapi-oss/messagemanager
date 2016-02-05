package nl.queuemanager.core;

public enum DebugProperty {
	forceInstallPlugins,
	forceMotdCheck,
	forceReleaseNoteCheck,
	enableSwingDebug;
	
	public boolean isEnabled() {
		return Boolean.getBoolean("mm." + name()) 
			|| Boolean.getBoolean("jnlp.mm." + name());
	}
	
}
