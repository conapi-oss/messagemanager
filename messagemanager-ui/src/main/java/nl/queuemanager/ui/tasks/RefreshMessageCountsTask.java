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
package nl.queuemanager.ui.tasks;

import java.util.List;

import javax.swing.SwingUtilities;

import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.BackgroundTask;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.ui.util.ListTableModel;

public class RefreshMessageCountsTask extends BackgroundTask {

	private final JMSDomain sonic;
	private final ListTableModel<JMSQueue> tableModel;
	private JMSBroker broker;
	
	public RefreshMessageCountsTask(
			JMSDomain sonic,
			JMSBroker broker,
			ListTableModel<JMSQueue> tableModel) {
		super(broker);
		
		this.sonic = sonic;
		this.broker = broker;
		this.tableModel = tableModel;
	}
	
	@Override
	public void execute() throws Exception {
		if(broker == null) 
			return;
		
		final List<JMSQueue> queues = sonic.getQueueList(broker, null);
		
		// Update the tableModel with the new counts
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for(JMSQueue queue: queues) {
					int row = tableModel.getItemRow(queue);
					JMSQueue existingQueue = tableModel.getRowItem(row);
					if(row >= 0	&& (existingQueue.getMessageCount() != queue.getMessageCount())) {
						tableModel.setRowItem(row, queue);
					}
				}
			}
		});
	}

	@Override
	public String toString() {
//		return "Refreshing message counts";
		return "";
	}
}
