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

import java.util.Set;

import nl.queuemanager.core.task.TaskEvent.EVENT;
import nl.queuemanager.core.util.WeakHashSet;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * This class is the base class for all tasks to be executed on the TaskExecutor. 
 * When the execute() method throws an exception and the Task is used as a Runnable, 
 * the run() method will catch it and dispatch TASK_ERROR and clear the task queue.
 * 
 * @author gerco
 *
 */
public abstract class Task implements Runnable {

	private MultiQueueTaskExecutor executor;
	
	/**
	 * The resource object that this tasks wants to be available. A Task may
	 * only have a single resource object or no resource object. Only a single
	 * Task is allowed to be active per resource object at any one time.
	 */
	private final Object resource;
	
	/**
	 * The set of other tasks (if any) this Task depends upon. This task will not
	 * start to run before all of it's dependencies have finished running.
	 */
	private final Set<Task> dependencies;
	
	/**
	 * This object will be notify()'d whenever a dependency has finished running.
	 */
	private final Object dependenciesLock = new Object();
	
	/**
	 * EventBus to send all task events on. This is most-likely the application-wide bus.
	 */
	protected final EventBus eventBus;
	
	/**
	 * Thread context classloader to use for the task
	 */
	protected ClassLoader contextClassLoader;
	
	protected long startTime;
	
	/**
	 * The task's current status. 
	 */
	protected TaskStatus status = TaskStatus.NEW;
	private final Object statusLock = new Object();

	/**
	 * Construct a Task with the specified object as it's resource, may be null.
	 * 
	 * @param resource
	 */
	@SuppressWarnings("unchecked")
	protected Task(Object resource, EventBus eventBus) {
		this.resource = resource;
		this.eventBus = eventBus;
		
		/*
		 * This must be a Weak Set because when the queue is cleared by the Executors,
		 * any dependencies we were waiting on must be able to be garbage collected or
		 * the waiting will never stop!
		 */
		this.dependencies = new WeakHashSet();
	}
	
	MultiQueueTaskExecutor getExecutor() {
		return executor;
	}

	void setExecutor(MultiQueueTaskExecutor executor) {
		this.executor = executor;
	}
	
	/**
	 * Add a dependency to this Task. This task will not run until all of it's 
	 * dependencies have finished running.
	 * 
	 * @param task
	 */
	public void addDependency(Task task) {
		synchronized(dependenciesLock) {
			dependencies.add(task);
		}
	}

	/**
	 * Remove the specified task as a dependency of this task
	 * 
	 * @param task
	 */
	public boolean removeDependency(Task task) {
		synchronized(dependenciesLock) {
			boolean res = dependencies.remove(task);
			dependenciesLock.notify();
			return res;
		}
	}
	
	/**
	 * Return the number of dependencies for this Task.
	 * 
	 * @return
	 */
	public int getDependencyCount() {
		synchronized(dependenciesLock) {
			return dependencies.size();
		}
	}
		
	public final void run() {
		if(getDependencyCount() != 0)
			throw new IllegalStateException("Task started with non-zero dependency count!");

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		if(getContextClassLoader() != null) {
			Thread.currentThread().setContextClassLoader(getContextClassLoader());
		}
		
		startTime = System.currentTimeMillis();
		dispatchTaskStarted();
		
		try {
			execute();
		} catch (Throwable t) {
			// Tell the executor to clear the task queue
			getExecutor().clearQueue();
			
			// Tell the application that there was an error
			dispatchTaskError(new Exception("An unexpected error occurred in task " + toString(), t));
		} finally {
			if(previousClassLoader != null) {
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}
		}
		
		dispatchTaskFinished();
	}

	protected void dispatchTaskWaiting() {
		if(transitionTo(TaskStatus.WAITING)) {
			eventBus.post(new TaskEvent(EVENT.TASK_WAITING, getInfo(), this));
		}
	}
	
	protected void dispatchTaskStarted() {
		if(transitionTo(TaskStatus.STARTED)) {
			eventBus.post(new TaskEvent(EVENT.TASK_STARTED, getInfo(), this));
		}
	}
	
	protected void dispatchTaskError(Throwable t) {
		if(transitionTo(TaskStatus.ERROR)) {
			eventBus.post(new TaskEvent(EVENT.TASK_ERROR, t, this));
		}
	}
	
	protected void dispatchTaskFinished() {
		if(transitionTo(TaskStatus.FINISHED)) {
			eventBus.post(new TaskEvent(EVENT.TASK_FINISHED, getInfo(), this));
			safeUnregister();
		}
	}
	
	void dispatchTaskDiscarded() {
		if(transitionTo(TaskStatus.DISCARDED)) {
			eventBus.post(new TaskEvent(EVENT.TASK_DISCARDED, null, this));
			safeUnregister();
		}
	}
	
	private void safeUnregister() {
		try {
			eventBus.unregister(this);
		} catch (IllegalArgumentException e) {
			// Not all tasks will unregister properly, those that do not subscribe to any events will cause an Exception.
			// Ignore that exception here.
		}
	}
	
	/**
	 * This method should perform the actual work. Any exceptions thrown will be converted
	 * into a TASK_ERROR event on the EventListener<TaskEvent>.
	 * 
	 * @throws Exception
	 */
	public abstract void execute() throws Exception;
	
	/**
	 * This method should return status information for the running task. It may be called
	 * one or more times when a task dispatches TASK_PROGRESS. It will not be called when
	 * TASK_PROGRESS is never dispatched.
	 * 
	 * @return The status of the running task
	 */
	public String getStatus() {
		return this.toString();
	}
	
	/**
	 * Return an Object describing the current status of the Task. This object is included in
	 * events published from the task (except TASK_PROGRESS and TASK_ERROR). The default 
	 * implementation returns null
	 * 
	 * @return
	 */
	protected Object getInfo() {
		return null;
	}
	
	/**
	 * The maximum progress value reported through TASK_PROGRESS events. When this value is
	 * reached, the task is considered complete. By default, this method returns 1.
	 * 
	 * @return The highest value to ever be reported through TASK_PROGRESS.
	 */
	public int getProgressMaximum() {
		return 1;
	}
		
	/**
	 * Raise the TASK_PROGRESS event with this task as the source and 'current' as the value
	 * 
	 * @param current The amount of progress that has been made (in total)
	 */
	protected void reportProgress(int current) {
		eventBus.post(new TaskEvent(EVENT.TASK_PROGRESS, current, this));
	}
	
	/**
	 * When this Task is a true background task, the progress dialog will not
	 * pop up and allow the user to continue working while this task executes.
	 */
	public boolean isBackground() {
		return false;
	}

	/**
	 * Get this Tasks resource object, if any.
	 * 
	 * @return
	 */
	public Object getResource() {
		return resource;
	}
	
	public ClassLoader getContextClassLoader() {
		return contextClassLoader;
	}

	public void setContextClassLoader(ClassLoader contextClassLoader) {
		this.contextClassLoader = contextClassLoader;
	}

	/**
	 * When a Task that this Task depends on has sent it's TASK_FINISHED event. Remove
	 * that task as a dependency.
	 */
	@Subscribe
	public void processEvent(TaskEvent event) {
		if(event.getId() == EVENT.TASK_FINISHED) {
			removeDependency((Task)event.getSource());
		}
	}
	
	private boolean transitionTo(TaskStatus newStatus) {
		synchronized(statusLock) {
			if(status.transitionAllowed(newStatus)) {
				status = newStatus;
				return true;
			}
			
			return false;
		}
	}
	
	public enum TaskStatus {
		NEW, WAITING, DISCARDED, STARTED, ERROR, FINISHED;

		public boolean transitionAllowed(TaskStatus newStatus) {
			return newStatus.ordinal() > this.ordinal();
		}
	}
	
}
