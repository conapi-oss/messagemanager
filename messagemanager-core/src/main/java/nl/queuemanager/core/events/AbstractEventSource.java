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
package nl.queuemanager.core.events;

import java.util.ArrayList;

import nl.queuemanager.core.util.CollectionFactory;

public abstract class AbstractEventSource<T> implements EventSource<T> {
	private final ArrayList<EventListener<T>> listeners = CollectionFactory.newArrayList();
	
	private static final boolean DEBUG = "TRUE".equalsIgnoreCase(System.getProperty("developer"));

	public void addListener(EventListener<T> listener) {
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(EventListener<T> listener) {
		if(listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}
	
	protected void dispatchEvent(T event) {
		for(EventListener<T> listener: CollectionFactory.newArrayList(listeners)) {
			if(DEBUG) System.out.println(Thread.currentThread() + " -> Dispatch event: " + event + " to listener " + listener);
			listener.processEvent(event);
		}
	}
}
