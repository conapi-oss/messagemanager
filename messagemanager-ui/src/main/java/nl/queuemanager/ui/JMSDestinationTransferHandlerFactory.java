package nl.queuemanager.ui;

import nl.queuemanager.ui.JMSDestinationTransferHandler.JMSDestinationHolder;

interface JMSDestinationTransferHandlerFactory {
	public abstract JMSDestinationTransferHandler create(JMSDestinationHolder destinationHolder);
}
