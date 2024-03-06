package nl.queuemanager.solace;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.core.task.Task;

import jakarta.inject.Inject;
import javax.jms.Session;

class ConnectToApplianceTask extends Task {

	private final SolaceDomain appliance;
	private final SempConnectionDescriptor descriptor;
	
	@Inject
	public ConnectToApplianceTask(SolaceDomain appliance, EventBus eventBus, @Assisted SempConnectionDescriptor descriptor) {
		super(appliance, eventBus);
		this.appliance = appliance;
		this.descriptor = descriptor;
	}
	
	@Override
	public void execute() throws Exception {
		SempConnection conn;
		switch(descriptor.getConnectionMethod()) {
		case SEMP_OVER_HTTP:
			conn = new HttpSEMPConnection(descriptor);
			// Make sure to perform a request to check the connection settings 
			// (mostly to make sure incorrectly configured SSL connections don't 
			// look like they connected but don't work anyways).
			conn.performShowRequest(SempRequests.showSession(), SempResponseCallback.NULL_HANDLER);
			break;
			
		case SEMP_OVER_MESSAGEBUS:
			// We have to create an SMF connection to be able to do any SEMP over Message Bus so we 
			// 'preconnect' by establishing a session that will later be reused if we happen to connect
			// to the same Message VPN. Creating that session has the side-effect of getting the router
			// name from the appliance - which we need to perform SEMP requests - so we have to call
			// appliance.getRouterName() *after* the session has been created.
			Session session = appliance.createNewSession(descriptor);
			descriptor.setApplianceName(appliance.getRouterName());
			conn = new SmfSempConnection(descriptor, session);
			break;
		default:
			throw new SempException("Unrecognized connection method: " + descriptor.getConnectionMethod());
		}
		
		appliance.connect(conn);
	}
	
	@Override
	public String toString() {
		return "Connecting to Solace at " + descriptor.toString();
	}

}
