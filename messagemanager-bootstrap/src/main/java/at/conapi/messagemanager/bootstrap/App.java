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


import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import org.update4j.Configuration;
import org.update4j.service.Delegate;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application implements Delegate {

	@Override
	public long version() {
		return 0;
	}

	@Override
	public void main(List<String> args) throws Throwable {
		launch();
	}

	// for testing purposes only
	public static void main(String[] args) {
		launch();
	}

	public static List<Image> images;
	public static Image splash;


	@Override
	public void init() {
		System.setProperty("update4j.suppress.warning", "true");

		List<String> sizes = List.of("tiny", "small", "medium", "large");
		images = sizes.stream()
						.map(s -> ("/icons/messagemanager-icon-" + s + ".png"))
						.map(s -> getClass().getResource(s).toExternalForm())
						.map(Image::new)
						.collect(Collectors.toList());
		splash = createImage(this,"/icons/splash.png");
	}

	public static Image createImage(Object context, String resourceName) {
		URL _url = context.getClass().getResource(resourceName);
		return new Image(_url.toExternalForm());
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.initStyle(StageStyle.UTILITY); // to avoid the taskbar icon
		primaryStage.setOpacity(0);
		primaryStage.setHeight(0);
		primaryStage.setWidth(0);
		primaryStage.show();

		Stage mainStage = new Stage();
		mainStage.initOwner(primaryStage);
		mainStage.initStyle(StageStyle.TRANSPARENT);
		mainStage.setMinWidth(650);
		mainStage.setMinHeight(500);


		URL configUrl = new URL(AppProperties.getUpdateUrl());
		Configuration config = null;
		System.out.println("Loading: " + configUrl);
		boolean workOffline = false;
		try (Reader in = new InputStreamReader(configUrl.openStream(), StandardCharsets.UTF_8)) {
			config = Configuration.read(in);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not load remote config, falling back to local.");
			workOffline = true;
			try (Reader in = Files.newBufferedReader(Paths.get("app/config.xml"))) {
				config = Configuration.read(in);
			}
		}

		// ensure the base dir exists
		Files.createDirectories(config.getBasePath());

		StartupView startup = new StartupView(config, mainStage, workOffline);

		Scene scene = new Scene(startup);
		scene.getStylesheets().add(getClass().getResource("root.css").toExternalForm());

		// not needed as we now use UTILITY to hide from task bar
		//primaryStage.getIcons().addAll(images);
		mainStage.setScene(scene);

		mainStage.setTitle("Message Manager");
		mainStage.show();
	}
}
