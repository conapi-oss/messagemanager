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

import nl.queuemanager.jms.JMSTopic;

import com.sonicsw.mq.common.runtime.IDurableSubscriptionData;

/**
 * SonicMQ based implementation of {@link JMSTopic}.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
class SonicMQTopic extends SonicMQDestination implements JMSTopic {
	private String topicName;
	
	// Unused for now, until I think of a use for it.
//	private IDurableSubscriptionData subscriptionData;
	
	protected SonicMQTopic(SonicMQBroker broker, String topicName) {
		super(broker);
		this.topicName = topicName;
	}
	
	protected SonicMQTopic(SonicMQBroker broker, IDurableSubscriptionData subscriptionData) {
		super(broker);
		this.topicName = subscriptionData.getTopicName();
//		this.subscriptionData = subscriptionData;
	}
	
	public TYPE getType() {
		return TYPE.TOPIC;
	}
	
	public String getName() {
		return topicName;
	}

	public String getTopicName() throws JMSException {
		return getName();
	}	
}
