package nl.queuemanager.solace;

/**
 * This interface describes how to communicate to a Solace appliance using SEMP. Implementations
 * may use SEMP over HTTP, SEMP over SMF, etc
 */
interface SempConnection {

	void showMessageVPN(String vpnName, SempResponseCallback responseHandler) throws SempException;
	
	void showMessageSpoolByVPN(String vpnName, SempResponseCallback responseHandler) throws SempException;

	void performShowRequest(byte[] request, SempResponseCallback sempResponseHandler) throws SempException;
	
	void performAdminRequest(byte[] request, SempResponseCallback sempResponseHandler) throws SempException;

	SmfConnectionDescriptor getSmfConnectionDescriptor();
	
}
