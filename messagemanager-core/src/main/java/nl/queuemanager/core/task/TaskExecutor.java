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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.queuemanager.core.events.AbstractEventSource;
import nl.queuemanager.core.events.EventListener;

public class TaskExecutor extends AbstractEventSource<TaskEvent> implements EventListener<TaskEvent>
{
	// Single-use override executor. For testing purposes only!
	private final ExecutorService overrideExecutor;

	// List of Tasks that are waiting to run
	private final List<Task> waitingTasks;
	
	// A Map of Executors. One for each Resource.
	private final Map<Object, ExecutorService> executors;

	// Protects access to both the executors Map and the waitingTasks List.
	private final Object executorLock;
		
	public TaskExecutor() {
		this.overrideExecutor = null;
		this.waitingTasks = new LinkedList<Task>();
		this.executors = new HashMap<Object, ExecutorService>();
		this.executorLock = new Object();
	}
	
	public TaskExecutor(ExecutorService executor) {
		this.overrideExecutor = executor;
		this.waitingTasks = new LinkedList<Task>();
		this.executors = null;
		this.executorLock = new Object();
	}
		
	/**
	 * Clear the queue of this TaskExecutor. Any running tasks will be signalled to
	 * stop and any queued or waiting tasks will be removed from the queue.
	 */
	public synchronized void clearQueue() {
		List<Runnable> discardedTasks = new LinkedList<Runnable>();
		
		synchronized(executorLock) {
			discardedTasks.addAll(waitingTasks);
			waitingTasks.clear();
			
			if(overrideExecutor != null) {
				overrideExecutor.shutdownNow();
			} else {
				// This will attempt to stop the currently running tasks and not attempt to 
				// execute any already submitted tasks.
				for(ExecutorService e: executors.values()) {
					discardedTasks.addAll(e.shutdownNow());
				}
				
				/*
				 * Tasks may still be running at this point, throw away the executors we have
				 * so new ones will be created to run new tasks on. We create new ones because
				 * an Executor that has been shutdown will never accept new tasks.
				 */
				executors.clear();
			}
		}
		
		for(Runnable r: discardedTasks) {
			((Task)r).dispatchTaskDiscarded();
		}
	}
	
	/**
	 * Called by Tasks to signal that they have finished executing. Called after all
	 * listeners have been notified of this event.
	 */
	void afterExecute(Task task) {
		processWaitingTasks();
	}
	
	/**
	 * Execute the given task at some point in the future. Possibly on a background thread.
	 * 
	 * @param task
	 */
	public void execute(final Task task) {
		task.setExecutor(this);
		task.addListener(this);
				
		task.dispatchTaskWaiting();
		
		if(task.getDependencyCount() == 0) {
			executeNow(task);
		} else synchronized(executorLock) {
			waitingTasks.add(task);
		}
	}

	/**
	 * Get an executor to schedule tasks on for the specified resource object (or null).
	 * 
	 * @param resource
	 * @return
	 */
	protected Executor getExecutorForResource(final Object resource) {
		if(overrideExecutor != null)
			return overrideExecutor;
		
		synchronized(executorLock) {
			ExecutorService e = executors.get(resource);
			
			if(e == null) {
				// If the resource is null, the Map must accept the null key!
				executors.put(resource, e = Executors.newSingleThreadExecutor());
			}
			
			return e;
		}
	}
	
	/**
	 * Execute the given Tasks in the order they are given. Each Task
	 * will be given a dependency to the previous Task in the array. The Tasks 
	 * are guaranteed to run in-order, but not guaranteed to run on the same Thread.
	 * 
	 * @param tasks
	 */
	public synchronized void executeInOrder(Task... tasks) {
		if(tasks.length == 1) {
			execute(tasks[0]);
		} else {
			// Prepare the Tasks by adding the previous Task to their dependencies
			for(int i = tasks.length-1; i>0; i--) {
				tasks[i].addDependency(tasks[i-1]);
			}
			
			// Now submit all the tasks in first-to-last order
			for(Task t: tasks) {
				execute(t);
			}
		}
	}

	/**
	 * Submit the Task to an executor now, do not check for dependencies.
	 * 
	 * @param task
	 */
	protected void executeNow(Task task) {
		// Find the executor for this task by using it's resource
		final Object resource = task.getResource();
		Executor e = getExecutorForResource(resource);
		e.execute(task);
	}

	/**
	 * Walk the list of waiting tasks and submit any that have
	 * no dependencies to their respective Executors.
	 */
	protected void processWaitingTasks() {
		synchronized(executorLock) {
			for(Iterator<Task> it = waitingTasks.iterator(); it.hasNext();) {
				final Task task = it.next(); 
				if(task.getDependencyCount() == 0) {
					executeNow(task);
					it.remove();
				}
			}
		}
	}
	
	public void processEvent(TaskEvent event) {
		// Forward the task event from the task to the application
		dispatchEvent(event);
	}
}
