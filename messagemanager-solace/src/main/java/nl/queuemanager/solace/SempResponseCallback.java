package nl.queuemanager.solace;

import org.w3c.dom.Document;

interface SempResponseCallback {
	/**
	 * Process a SEMP response fragment.
	 * 
	 * @param doc The SEMP response document from the appliance
	 * @return Whether processing was successful and the callback is ready for more responses (if there are any)
	 * @throws Any Exception
	 */
	public void handle(Document doc) throws Exception;
	
	public static SempResponseCallback NULL_HANDLER = new SempResponseCallback() {
		@Override
		public void handle(Document doc) throws Exception { }
	};
}
