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
package nl.queuemanager.jms.impl;

import javax.jms.JMSException;

class JMSQueueImpl extends JMSDestinationImpl implements nl.queuemanager.jms.JMSQueue {
	
	private String name;
	private int messageCount;

	JMSQueueImpl(String name) {
		this(name, 0);
	}
	
	JMSQueueImpl(String name, int messageCount) {
		this.name = name;
		this.messageCount = messageCount;
	}
	
	public int getMessageCount() {
		return messageCount;
	}

	public String getName() {
		return name;
	}

	public TYPE getType() {
		return TYPE.QUEUE;
	}

	public String getQueueName() throws JMSException {
		return getName();
	}

}
