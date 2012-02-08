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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import nl.queuemanager.core.CoreModule;
import nl.queuemanager.smm.ui.SMMFrame;
import nl.queuemanager.ui.UIModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;

public class Main {

	// Do not allow instances of this class
	private Main() {}
	
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(
				Stage.PRODUCTION,
				new CoreModule(), 
				new UIModule(), 
				new SMMModule());
		
		// Create the main application frame
		final SMMFrame frame = injector.getInstance(SMMFrame.class);
		
		try {
			graph("/tmp/smm.dot", injector);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Set the frame visible and start the program
		frame.setVisible(true);
		frame.start();
	}
	
	private static void graph(String filename, Injector demoInjector) throws IOException {
		PrintWriter out = new PrintWriter(new File(filename), "UTF-8");

		Injector injector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
		GraphvizRenderer renderer = injector.getInstance(GraphvizRenderer.class);
		renderer.setOut(out).setRankdir("TB");

		injector.getInstance(InjectorGrapher.class).of(demoInjector).graph();
	}
}
