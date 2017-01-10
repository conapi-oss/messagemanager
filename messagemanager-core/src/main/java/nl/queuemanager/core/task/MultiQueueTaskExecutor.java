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

import javax.inject.Inject;

class MultiQueueTaskExecutor implements TaskExecutor 
{
	// List of Tasks that are waiting to run
	private final List<Task> waitingTasks;
	
	// A Map of Executors. One for each Resource.
	private final Map<Object, ExecutorService> executors;

	// Protects access to both the executors Map and the waitingTasks List.
	private final Object executorLock;
	
	// Default ClassLoader to set as the thread context ClassLoader before executing a task
	private ClassLoader contextClassLoader;
		
	@Inject
	public MultiQueueTaskExecutor() {
		this.waitingTasks = new LinkedList<Task>();
		this.executors = new HashMap<Object, ExecutorService>();
		this.executorLock = new Object();
	}
			
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.task.TaskExecutor#clearQueue()
	 */
	public synchronized void clearQueue() {
		List<Runnable> discardedTasks = new LinkedList<Runnable>();
		
		synchronized(executorLock) {
			// Since the waiting tasks list is protected by the Executors lock, it is safe
			// to work with the list while background threads are still completing tasks.
			discardedTasks.addAll(waitingTasks);
			waitingTasks.clear();
			
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
		
		for(Runnable r: discardedTasks) {
			((Task)r).dispatchTaskDiscarded();
		}
	}
	
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.task.TaskExecutor#execute(nl.queuemanager.core.task.Task)
	 */
	public void execute(final Task task) {
		task.setExecutor(this);
		if(task.getContextClassLoader() == null && getContextClassLoader() != null) {
			task.setContextClassLoader(getContextClassLoader());
		}
		
		if(task.getDependencyCount() == 0) {
			executeNow(task);
		} else synchronized(executorLock) {
			task.dispatchTaskWaiting();
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
		synchronized(executorLock) {
			ExecutorService e = executors.get(resource);
			
			if(e == null) {
				// If the resource is null, we have no dependencies. Spawn an temporary executor for just this task.
				return Executors.newSingleThreadExecutor();
			}
			
			return e;
		}
	}
	
	/* (non-Javadoc)
	 * @see nl.queuemanager.core.task.TaskExecutor#executeInOrder(nl.queuemanager.core.task.Task)
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

	public ClassLoader getContextClassLoader() {
		return contextClassLoader;
	}

	public void setContextClassLoader(ClassLoader contextClassLoader) {
		this.contextClassLoader = contextClassLoader;
	}
	
}
