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
package nl.queuemanager.test.support;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import nl.queuemanager.core.util.CollectionFactory;

/**
 * ExecutorService that performs all work on the calling thread.
 * 
 * @author gerco
 *
 */
public class SynchronousExecutorService extends AbstractExecutorService {

	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return true;
	}

	public boolean isShutdown() {
		return false;
	}

	public boolean isTerminated() {
		return false;
	}

	public void shutdown() {
	}

	public List<Runnable> shutdownNow() {
		return CollectionFactory.newArrayList();
	}

	/**
	 * Execute the task on the current thread.
	 */
	public void execute(Runnable command) {
		command.run();
	}
}
