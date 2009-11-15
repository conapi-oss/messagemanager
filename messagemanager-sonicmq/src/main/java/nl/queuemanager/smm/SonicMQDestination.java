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

import nl.queuemanager.core.jms.JMSBroker;
import nl.queuemanager.core.jms.JMSDestination;

/**
 * Abstract base class for SonicMQ based JMSDestinations
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public abstract class SonicMQDestination implements JMSDestination {

	protected SonicMQBroker broker;
	
	public SonicMQDestination(SonicMQBroker broker) {
		this.broker = broker;
	}
	
	public JMSBroker getBroker() {
		return broker;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof JMSDestination) 
			&& ((JMSDestination)o).getType().equals(getType())
			&& ((JMSDestination)o).getName().equals(getName());
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
	}
	
	public int compareTo(JMSDestination o) {
		return toString().toLowerCase().compareTo(o.toString().toLowerCase());
	}
}
