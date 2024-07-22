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
package nl.queuemanager.core.tasks;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.BackgroundTask;
import nl.queuemanager.jms.JMSBroker;

import javax.annotation.Nullable;

public class EnumerateTopicsTask extends BackgroundTask {

	private final JMSDomain domain;
	private final JMSBroker broker;
	private final String filter;

	@Inject
    EnumerateTopicsTask(
			JMSDomain domain,
			EventBus eventBus,
			@Assisted JMSBroker broker, 
			@Nullable @Assisted String filter) 
	{
		super(domain, eventBus);
		
		this.domain = domain;
		this.broker = broker;
		this.filter = filter;
	}

	@Override
	public void execute() throws Exception {
		domain.enumerateTopics(broker, filter);
	}
	
	@Override
	public String toString() {
		return "Enumerating topics for " + broker;
	}

}
