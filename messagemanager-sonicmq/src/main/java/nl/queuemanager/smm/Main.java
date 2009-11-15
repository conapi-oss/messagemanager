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

import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.smm.ui.SMMFrame;

public class Main {

	// Do not allow instances of this class
	private Main() {
	}
	
	public static void main(String[] args) {
		// Create the sonic domain proxy object
		final Domain sonic = new Domain();
		
		// Create the background worker 
		final TaskExecutor worker = new TaskExecutor();

		// Create the main application frame
		final SMMFrame frame = new SMMFrame(sonic, worker);
		
		// Set the frame visible and start the program
		frame.setVisible(true);
		frame.start();
	}
}
