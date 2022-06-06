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

import nl.queuemanager.jms.JMSBroker;

import javax.management.ObjectName;

class SonicMQBroker implements Comparable<JMSBroker>, JMSBroker {
	public static enum ROLE {
		PRIMARY,
		BACKUP
	}
	
	private final ObjectName objectName;
	private final String brokerName;
	private final String brokerURL;	
	private final ROLE role;

	public SonicMQBroker(ObjectName objectName, String brokerName, String connectionUrl, ROLE role) {
		this.objectName = objectName;
		this.brokerName = brokerName;
		this.brokerURL  = sanitizeBrokerUrl(connectionUrl);
		this.role       = role;
	}

	private String sanitizeBrokerUrl(String connectionUrl) {
		// Removes #ONLY from the URL if present
		return connectionUrl.replace("#ONLY", "");
	}

	public String getBrokerName() {
		return brokerName;
	}

	public String getBrokerURL() {
		return brokerURL;
	}

	public ObjectName getObjectName() {
		return objectName;
	}

	public ROLE getRole() {
		return role;
	}

	@Override
	public String toString() {
		return getBrokerName() + " (" + getBrokerURL() + ")";
	}

	public int compareTo(JMSBroker other) {
		return toString().compareTo(other.toString());
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((brokerName == null) ? 0 : brokerName.hashCode());
		result = PRIME * result + ((brokerURL == null) ? 0 : brokerURL.hashCode());
		result = PRIME * result + ((objectName == null) ? 0 : objectName.hashCode());
		result = PRIME * result + ((role == null) ? 0 : role.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SonicMQBroker other = (SonicMQBroker) obj;
		if (brokerName == null) {
			if (other.brokerName != null)
				return false;
		} else if (!brokerName.equals(other.brokerName))
			return false;
		if (brokerURL == null) {
			if (other.brokerURL != null)
				return false;
		} else if (!brokerURL.equals(other.brokerURL))
			return false;
		if (objectName == null) {
			if (other.objectName != null)
				return false;
		} else if (!objectName.equals(other.objectName))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		return true;
	}
}