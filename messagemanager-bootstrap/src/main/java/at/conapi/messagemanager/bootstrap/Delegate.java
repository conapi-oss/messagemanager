package at.conapi.messagemanager.bootstrap;
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


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.update4j.Configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Delegate implements org.update4j.service.Delegate {
	@Override
	public long version() {
		return 0;
	}

	@Override
	public void main(List<String> args) throws Throwable {
		checkJavaVersion();
		System.setProperty("javafx.embed.singleThread", "true");
		Application.launch(App.class);
	}

	public static void main(String[] args) {
		checkJavaVersion();
		System.setProperty("javafx.embed.singleThread", "true");
		Application.launch(App.class, args);
	}

	private static void checkJavaVersion(){
		try{
			if(Runtime.version().feature() >=17){
				return;
			}
		}
		catch (Throwable e){
		}
		System.err.println("Java Runtime 17 or higher is required.");
		System.exit(1);
	}
}
