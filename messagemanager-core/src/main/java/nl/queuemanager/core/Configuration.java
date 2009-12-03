/**
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
package nl.queuemanager.core;

import java.util.List;

import nl.queuemanager.core.jms.JMSBroker;
import nl.queuemanager.core.jms.JMSTopic;

public interface Configuration {
	public static final String PREF_BROWSE_DIRECTORY = "browseDirectory";
	public static final String PREF_SAVE_DIRECTORY = "saveDirectory";
	public static final String PREF_MAX_BUFFERED_MSG = "maxBufferedMessages";
	public static final String PREF_AUTOREFRESH_INTERVAL = "autoRefreshInterval";
	public static final String PREF_BROKER_ALTERNATE_URL = "alternateUrl";

	/**
	 * Get a per-user preference value.
	 * 
	 * @param key
	 * @param def
	 * @return
	 */
	public abstract String getUserPref(String key, String def);

	/**
	 * Set a preference value for the user.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setUserPref(String key, String value);

	/**
	 * Get the value of a single per-broker preference.
	 * 
	 * @param broker
	 * @param key
	 * @param def
	 * @return
	 */
	public abstract String getBrokerPref(JMSBroker broker, String key, String def);

	/**
	 * Set a single preference value for the specified broker.
	 * 
	 * @param broker
	 * @param key
	 * @param value
	 */
	public abstract void setBrokerPref(JMSBroker broker, String key, String value);

	/**
	 * Retrieve the stored list of topic subscribers for a broker
	 * 
	 * @param broker
	 * @return
	 */
	public abstract List<String> getTopicSubscriberNames(JMSBroker broker);

	/**
	 * Retrieve the stored list of topic publishers for a broker
	 * 
	 * @param broker
	 * @return
	 */
	public abstract List<String> getTopicPublisherNames(JMSBroker broker);

	/**
	 * Add a topic subscriber to the list for its broker
	 * 
	 * @param topic
	 */
	public abstract void addTopicSubscriber(JMSTopic topic);

	/**
	 * Add a topic publisher to the list for its broker
	 * 
	 * @param topic
	 */
	public abstract void addTopicPublisher(JMSTopic topic);

	/**
	 * Remove a topic publisher from the saved list for its associated broker.
	 * 
	 * @param topic
	 */
	public abstract void removeTopicPublisher(JMSTopic topic);
	
	/**
	 * Remove a topic subscriber from the saved list for its associated broker.
	 * 
	 * @param topic
	 */
	public void removeTopicSubscriber(JMSTopic topic);
}