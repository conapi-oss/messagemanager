package nl.queuemanager.solace;

enum SempConnectionMethod { 
	SEMP_OVER_HTTP("SEMP over Http(s)"),
	SEMP_OVER_MESSAGEBUS("SEMP over Message Bus");
	
	private final String displayName;
	
	private SempConnectionMethod(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
}