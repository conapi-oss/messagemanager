package nl.queuemanager.solace;

interface TaskFactory {
	public abstract ConnectToApplianceTask connectToAppliance(SempConnectionDescriptor descriptor);
}
