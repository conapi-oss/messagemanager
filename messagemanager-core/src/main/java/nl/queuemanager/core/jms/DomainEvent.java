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
package nl.queuemanager.core.jms;

import java.util.EventObject;

import nl.queuemanager.core.events.EventSource;

public class DomainEvent extends EventObject {

	public static enum EVENT {
		JMX_CONNECT,
		JMX_DISCONNECT,
		
		BROKERS_ENUMERATED,
		BROKER_CONNECT,
		BROKER_DISCONNECT,
		
		QUEUES_ENUMERATED,
//		TOPICS_ENUMERATED,
		
		MESSAGES_SENT
	}
	
	private final EVENT id;
	private final Object info;
	
	public DomainEvent(EVENT id, Object info, EventSource<DomainEvent> source) {
		super(source);
		this.id = id;
		this.info = info;
	}

	public Object getInfo() {
		return info;
	}

	public EVENT getId() {
		return id;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EventSource<DomainEvent> getSource() {
		return (EventSource<DomainEvent>)super.getSource();
	}

	@Override
	public String toString() {
		return getSource() + ": " + getId() + " (" + getInfo() + ")";
	}
}
