/**

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.queuemanager.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.jms.Message;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import nl.queuemanager.core.Pair;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.BackgroundTask;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskEvent;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.EnumerateQueuesTask;
import nl.queuemanager.core.tasks.SendFileListTask;
import nl.queuemanager.core.tasks.SendMessageListTask;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
class JMSDestinationTransferHandler extends TransferHandler {
	private JMSDomain domain;
	private TaskExecutor worker;

	// The object to read JMSDestinations from
	private final JMSDestinationHolder destinationHolder;
	
	private int sourceActions;

	@Inject
	public JMSDestinationTransferHandler(
			JMSDomain sonic, 
			TaskExecutor worker, 
			@Assisted JMSDestinationHolder destinationHolder) 
	{
		setSourceActions(COPY);
		setDomain(sonic);
		setWorker(worker);
		this.destinationHolder = destinationHolder;
	}

	/**
	 * Create a Transferable for when a JMSDestination is dragged or copied.
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
		return new JMSDestinationInfoTransferable(destinationHolder.getJMSDestinationList());
	}
	
	/**
	 * Determines if this TransferHandler is able to import one of the supplied 
	 * dataflavors. By default accepts messageIdList, messageList and javaFileList.
	 */
	@Override
	public boolean canImport(JComponent jcomponent, DataFlavor[] dataflavors) {
		for(DataFlavor flavor: dataflavors) {
			if(flavor.equals(MessageListTransferable.messageIDListDataFlavor))
				return true;
			
			if(flavor.equals(MessageListTransferable.messageListDataFlavor))
				return true;
			
			if(flavor.equals(DataFlavor.javaFileListFlavor))
				return true;
		}
		
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(JComponent component, Transferable transferable) {
		try {
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for(DataFlavor f: flavors) {
				// TODO: Implement preference
				if(f.equals(MessageListTransferable.messageIDListDataFlavor)) {
					final List<Pair<JMSQueue, String>> messageIDList = 
						(List<Pair<JMSQueue, String>>) transferable.getTransferData(
								MessageListTransferable.messageIDListDataFlavor);
					return importMessageIDList(destinationHolder, messageIDList);
				}
				
				if(f.equals(MessageListTransferable.messageListDataFlavor)) {
					final List<Message> messageList = (List<Message>) 
						transferable.getTransferData(MessageListTransferable.messageListDataFlavor);

					return importMessageList(destinationHolder, messageList);
				}
				
				if(f.equals(DataFlavor.javaFileListFlavor)) {
					final List<File> fileList = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
					return importFileList(destinationHolder, fileList);
				}
			}
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}	
	
	/**
	 * Import a list of message ids into the component
	 * 
	 * @param component
	 * @param messageList
	 * @return
	 */
	protected boolean importMessageIDList(JMSDestinationHolder destinationHolder, final List<Pair<JMSQueue, String>> messageList) {
		final JMSQueue toQueue = (JMSQueue)destinationHolder.getJMSDestination();
		
		getWorker().executeInOrder(new Task(toQueue.getBroker()) {
			@Override
			public void execute() throws Exception {
				int i = 0;
				for(Pair<JMSQueue, String> messageInfo: messageList) {
					final JMSQueue fromDst = messageInfo.first();
					final String messageID = messageInfo.second();
					getDomain().forwardMessage(fromDst, toQueue, messageID);
					dispatchEvent(new TaskEvent(TaskEvent.EVENT.TASK_PROGRESS, i++, this));
				}
			}
			@Override
			public int getProgressMaximum() {
				return messageList.size();
			}
			@Override
			public String toString() {
				return "Moving " + messageList.size() + " message to " + toQueue;
			}
		},
		new EnumerateQueuesTask(getDomain(), toQueue.getBroker(), null),
		new FireRefreshRequiredTask(null, destinationHolder, toQueue));
		
		return true;
	}

	/**
	 * Import a list of {@link Message} objects into the component.
	 * 
	 * @param component
	 * @param messageList
	 * @return
	 */
	protected boolean importMessageList(JMSDestinationHolder destinationHolder, List<Message> messageList) {
		final JMSDestination destination = destinationHolder.getJMSDestination();
		
		getWorker().executeInOrder(
			new SendMessageListTask(destination, messageList, getDomain()),
			new EnumerateQueuesTask(getDomain(), destination.getBroker(), null),
			new FireRefreshRequiredTask(null, destinationHolder, destination));
		
		return true;
	}

	/**
	 * Import a list of files into the component.
	 * 
	 * @param component
	 * @param fileList
	 * @return
	 */
	protected boolean importFileList(JMSDestinationHolder destinationHolder, List<File> fileList) {
		final JMSDestination destination = destinationHolder.getJMSDestination();
		
		getWorker().executeInOrder(
			new SendFileListTask(destination, fileList, getDomain()),
			new EnumerateQueuesTask(getDomain(), destination.getBroker(), null),
			new FireRefreshRequiredTask(null, destinationHolder, destination));
		
		return true;
	}


	/**
	 * Set the source actions supported by this TransferHandler.
	 * 
	 * @param sourceActions
	 */
	protected void setSourceActions(int sourceActions) {
		this.sourceActions = sourceActions;
	}
	
	@Override
	public int getSourceActions(JComponent c) {
		return sourceActions;
	}
	
	protected JMSDomain getDomain() {
		return domain;
	}
	
	protected void setDomain(JMSDomain domain) {
		this.domain = domain;
	}
	
	protected TaskExecutor getWorker() {
		return worker;
	}
	
	protected void setWorker(TaskExecutor worker) {
		this.worker = worker;
	}
	
	public interface JMSDestinationHolder {
		public JMSDestination getJMSDestination();
		public List<JMSDestination> getJMSDestinationList();
		public void refreshRequired(JMSDestination destination);
	}
	
	static class FireRefreshRequiredTask extends BackgroundTask {
		private final JMSDestinationHolder target;
		private final JMSDestination destination;
		
		protected FireRefreshRequiredTask(Object resource, JMSDestinationHolder target, JMSDestination destination) {
			super(resource);
			this.target = target;
			this.destination = destination;
		}

		@Override
		public void execute() throws Exception {
			target.refreshRequired(destination);
		}
	}
}
