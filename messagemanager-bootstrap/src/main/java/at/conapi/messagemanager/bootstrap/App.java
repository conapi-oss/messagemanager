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
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.stage.StageStyle;
import org.update4j.Configuration;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class App extends Application {

	public static final boolean MAC;
	//TODO: move to implementation 'com.github.Dansoftowner:jSystemThemeDetector:3.6' when changing the UI
	static {
		boolean mac = false;
		try {
			final String osName = System.getProperty("os.name");
			mac = osName != null && osName.toLowerCase().contains("mac");
		} catch (Exception e) {
			e.printStackTrace();
		}
		MAC = mac;
	}


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

		if(MAC){
			// show a proper menu to at least exit
			showMacQuitMenu(primaryStage);

			// without this the quit is freezing the VM on Mac
			primaryStage.setOnCloseRequest(event -> shutdown());
		}
		primaryStage.show();

		Stage mainStage = new Stage();
		mainStage.initOwner(primaryStage);
		mainStage.initStyle(StageStyle.TRANSPARENT);
		mainStage.setMinWidth(600);
		mainStage.setMinHeight(300);

		boolean workOffline = false;
		Configuration config = null;

		if(shouldCheckForUpdates()) {
			// check for updates
			final URL configUrl = new URL(AppProperties.getUpdateUrl());
			System.out.println("Loading: " + configUrl);
			final URLConnection con = configUrl.openConnection();
			System.out.println("Connection opened: " + configUrl);
			con.setConnectTimeout(AppProperties.getConnectTimeout());
			con.setReadTimeout(AppProperties.getReadTimeout());
			// Some downloads may fail with HTTP/403, this may solve it
			con.addRequestProperty("User-Agent", "Mozilla/5.0");
			try (Reader in = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
				config = Configuration.read(in);
				// keep track of successful update checks
				AppProperties.setLastSuccessfulUpdateCheck(System.currentTimeMillis());
				AppProperties.resetFailedUpdates();
			} catch (IOException e) {
				// keep track of failed updates
				AppProperties.incrementFailedUpdates();

				e.printStackTrace();
				System.err.println("Could not load remote update config, falling back to local config.");
				workOffline = true;
			}
		}
		else{
			workOffline = true;
		}

		if(workOffline) {
			// fall back to local config
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

	private boolean shouldCheckForUpdates() {
		if(!AppProperties.isStableRelease()){
			// always check for updates for a non-stable release
			return true;
		}

		// only check for updates once every x days until the check was successful
		long lastSuccessfulUpdateCheck = AppProperties.getLastSuccessfulUpdateCheck();
		int updateFrequency = AppProperties.getUpdateFrequency();
        return lastSuccessfulUpdateCheck == 0 || System.currentTimeMillis() - lastSuccessfulUpdateCheck > Duration.ofDays(updateFrequency).toMillis();
	}

	private void showMacQuitMenu(final Stage primaryStage) {
		/*final MenuBar menuBar = new MenuBar();
		menuBar.setUseSystemMenuBar(true);
		final Menu menu = new Menu("Message Manager");
	    menuBar.getMenus().add(menu);
		Platform.runLater(() -> menuBar.setUseSystemMenuBar(true));

		BorderPane borderPane = new BorderPane();
		borderPane.setTop (menuBar);
		primaryStage.setScene (new Scene (borderPane));
		*/
		// just add the shutdown handler for now
	}

	private void shutdown(){
		Platform.exit();
		System.exit(0);
	}
}
