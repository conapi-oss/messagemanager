package nl.queuemanager.jms;
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


import javax.jms.Queue;

/**
 * Represents a JMS Queue that is associated with a certain broker.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public interface JMSQueue extends JMSDestination, Queue {
	/**
	 * The number of messages currently on the queue. -1 for unknown.
	 */
	public int getMessageCount();
}
