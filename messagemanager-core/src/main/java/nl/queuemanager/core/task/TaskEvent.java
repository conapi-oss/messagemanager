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
package nl.queuemanager.core.task;

import java.util.EventObject;

import nl.queuemanager.core.events.EventSource;


public class TaskEvent extends EventObject {
	public static enum EVENT {
		/**
		 * Dispatched before execution, if the task needs to wait for its resource to 
		 * become available or for its dependencies. Not guaranteed to dispatch for 
		 * every task because not every task needs to wait before execution.
		 * <p>
		 * <strong>This event may be dispatched multiple times before the task is started.</strong>
		 */
		TASK_WAITING,
		
		/**
		 * dispatched when a task starts executing. The source is the Task itself, the 
		 * info object is null by default.
		 */
		TASK_STARTED,
		
		/**
		 * dispatched when a task has progress to report. The info object is an Integer 
		 * object, the source is the Task itself. Some Tasks do not dispatch this event, 
		 * some do. It is optional.
		 */
		TASK_PROGRESS,
		
		/**
		 * dispatched when a task has stopped executing. This event always occurs, whether 
		 * there has been an error or not. The Task itself is the source and the info object 
		 * is null by default.
		 */
		TASK_FINISHED,
		
		/**
		 * dispatched when a task is being discarded by the executor. The task has never 
		 * had a chance to execute.
		 */
		TASK_DISCARDED,
		
		/**
		 * reports an error in the Task. The info object is the Exception that occurred, 
		 * the Task is the source.
		 */
		TASK_ERROR
	}
	
	private final EVENT id;
	private final Object info;
	
	public TaskEvent(EVENT id, Object info, EventSource<TaskEvent> source) {
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
	public EventSource<TaskEvent> getSource() {
		return (EventSource<TaskEvent>)super.getSource();
	}

	@Override
	public String toString() {
		return getSource() + ": " + getId() + " (" + getInfo() + ")";
	}
}
