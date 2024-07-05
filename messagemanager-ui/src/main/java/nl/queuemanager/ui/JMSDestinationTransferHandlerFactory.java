package nl.queuemanager.ui;

import nl.queuemanager.core.tasks.FireRefreshRequiredTask.JMSDestinationHolder;

interface JMSDestinationTransferHandlerFactory {
	public abstract JMSDestinationTransferHandler create(JMSDestinationHolder destinationHolder);
}
