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
package nl.queuemanager.smm;

import javax.jms.JMSException;

import nl.queuemanager.core.jms.JMSQueue;
import nl.queuemanager.core.jms.JMSDestination.TYPE;

public class SonicMQSimpleQueue extends SonicMQDestination implements JMSQueue {

	protected final String name;
	
	protected SonicMQSimpleQueue(SonicMQBroker broker, String name) {
		super(broker);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public TYPE getType() {
		return TYPE.QUEUE;
	}

	public int getMessageCount() {
		return -1;
	}

	public String getQueueName() throws JMSException {
		return getName();
	}

}
