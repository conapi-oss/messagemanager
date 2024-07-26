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

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.update4j.*;
import org.update4j.inject.InjectSource;
import org.update4j.inject.Injectable;
import org.update4j.service.UpdateHandler;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;

public class StartupView extends FXMLView implements UpdateHandler, Injectable {

	private Configuration config;

	@FXML
	private Label status;

	@FXML
	private ImageView image;


	@FXML
	private Pane primary;

	@FXML
	private Pane secondary;

	@FXML
	private StackPane progressContainer;


	private DoubleProperty primaryPercent;
	private DoubleProperty secondaryPercent;

	private BooleanProperty running;

	private boolean workOffline;

	@InjectSource
	private Stage mainStage;

	public StartupView(Configuration config, Stage mainStage, boolean workOffline) {
		this.config = config;
		this.mainStage = mainStage;
		this.workOffline = workOffline;

		// no min/max/close buttons to look like a splash screen
		mainStage.initStyle(StageStyle.TRANSPARENT);//.UNDECORATED);
		image.setImage(App.splash);

		// for the update progress bar
		primaryPercent = new SimpleDoubleProperty(this, "primaryPercent");
		secondaryPercent = new SimpleDoubleProperty(this, "secondaryPercent");

		running = new SimpleBooleanProperty(this, "running");

		primary.maxWidthProperty().bind(progressContainer.widthProperty().multiply(primaryPercent));
		secondary.maxWidthProperty().bind(progressContainer.widthProperty().multiply(secondaryPercent));

		status.setOpacity(0);

		FadeTransition fade = new FadeTransition(Duration.seconds(1.5), status);
		fade.setToValue(0);

		running.addListener((obs, ov, nv) -> {
			if (nv) {
				fade.stop();
				status.setOpacity(1);
			} else {
				fade.playFromStart();
				primaryPercent.set(0);
				secondaryPercent.set(0);
			}
		});

		primary.visibleProperty().bind(running);
		secondary.visibleProperty().bind(primary.visibleProperty());

		// ensure splash screen is shown.
		mainStage.setAlwaysOnTop(true);
		mainStage.toFront();

		mainStage.setOnShown((WindowEvent event) -> runApplication());
	}

	private void launchConfig() {
			// we can now start
			fadeOut();

			final AppLauncher launcher = new AppLauncher();
			config.launch(launcher);

			if (launcher.isLaunchFailed()) {
				final Exception e = launcher.getLaunchError();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						showAlert("Launch Failed", "Unable to launch the application: " + e.getMessage());
						// exit the app
						Platform.exit();
					}
				});
			}
	}

    private void runApplication(){
		Task<Boolean> checkUpdates = checkUpdates();
		checkUpdates.setOnSucceeded(evt -> {
			if (checkUpdates.getValue()) {
				final ButtonType updateAndLaunch = new ButtonType("Yes", ButtonData.OK_DONE);
				final ButtonType skipAndLaunch = new ButtonType("Skip", ButtonData.CANCEL_CLOSE);
				final ButtonType alwaysAndLaunch = new ButtonType("ALWAYS", ButtonData.OK_DONE);
				Optional<ButtonType> result = Optional.of(skipAndLaunch);

				if(!AppProperties.isAutoUpdate()) {
					// otherwise dialog is in the background
					mainStage.setAlwaysOnTop(false);

					Alert alert = new Alert(AlertType.CONFIRMATION);
					// ensure it is shown on mac
					alert.initStyle(StageStyle.UTILITY);
					alert.initOwner(mainStage);

					alert.setHeaderText("Update required");
					alert.setContentText("Application is not up-to-date, update now?");
//TODO: would be nice to show a changelog here
					alert.getButtonTypes().setAll(updateAndLaunch, skipAndLaunch, alwaysAndLaunch);
					result = alert.showAndWait();
					if(result.get() == alwaysAndLaunch){
						AppProperties.setAutoUpdate(true);
					}
					mainStage.setAlwaysOnTop(true);
				}

				if(AppProperties.isAutoUpdate() || (result.isPresent() && result.get() != skipAndLaunch)){
					// update
					updateApplication();
				}
				else{
					// skipped update
					//launchConfig();
					justLaunch();
				}
			}
			else{
				// no update needed, simply launch
				//launchConfig();
				justLaunch();			}
		});

		run(checkUpdates);
	}

	private void fadeOut(){
		//to have a smooth transition
		mainStage.getScene().setFill(Color.TRANSPARENT);

        // show for at least some time, without the outer transition the behavior was not consistent
		FadeTransition fakeFadeShowSplash = new FadeTransition(Duration.seconds(2), mainStage.getScene().getRoot());
		fakeFadeShowSplash.setFromValue(1);
		fakeFadeShowSplash.setToValue(1);
		fakeFadeShowSplash.setCycleCount(1);
		fakeFadeShowSplash.setOnFinished((e) -> {
			FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), mainStage.getScene().getRoot());
			fadeOut.setFromValue(1);
			fadeOut.setToValue(0);
			fadeOut.setCycleCount(1);
			fadeOut.play();
		});
		fakeFadeShowSplash.play();
	}


	private void updateApplication(){
		if (running.get()) {
			return;
		}
		running.set(true);
		status.setText("Checking for updates...");

		Task<Void> doUpdate = new Task<>() {
			@Override
			protected Void call() throws Exception {
				Path zip = Paths.get("messagemanager-update.zip");
				final UpdateResult updateResult = config.update(UpdateOptions.archive(zip).updateHandler(StartupView.this));

				if(updateResult.getException() == null) {
					Archive.read(zip).install();
					// only now the content is downloaded and loaded
					//TODO: on linux this the file permissions are not properly set after extracting a shell script
					// +x is missing. Ideally the Archive.install would take care but we cannot override it here
					// below workaround can be removed if we fork our own update4j
					// ensure all scripts in bin are executeable
					try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(Path.of("", "bin"), "*.sh")) {
						stream.forEach(script -> {
							System.out.println("Setting execute permission for: "+ script );
                            try {
								script.toFile().setExecutable(true);
                                Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr-x"));
                            } catch (Exception ignore) {
                            }
                        });
					}
				}
				// always launch
				launchConfig();
				return null;
			}
		};
		run(doUpdate);
	}

	private void justLaunch(){
		if (running.get()) {
			return;
		}
		running.set(true);
		status.setText("Launching...");
		Task<Void> doLaunch = new Task<>() {
			@Override
			protected Void call() throws Exception {
				launchConfig();
				return null;
			}
		};
		run(doLaunch);
	}

	private void showAlert(final String headerText, final String contentText) {
		mainStage.setAlwaysOnTop(false);
		Alert alert = new Alert(AlertType.ERROR);
		// ensure it is shown on mac
		alert.initStyle(StageStyle.UTILITY);
		alert.initOwner(mainStage);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.setAlwaysOnTop(true);
		stage.toFront();

		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		alert.showAndWait();
		mainStage.setAlwaysOnTop(true);
	}

	private Task<Boolean> checkUpdates() {
		return new Task<>() {
			@Override
			protected Boolean call() throws Exception {
				if(workOffline) {
					// auto update is not possible
					Platform.runLater(() -> {
						showAlert("Automatic Update Check Failed", "Unable to connect to the automatic update site. Please check proxy settings (see setenv script in '" + Path.of("").toAbsolutePath().normalize() + "')");
					});
					return false;
				}
				else {
					//TODO: only check for updates once every x days, if online connection possible
					return config.requiresUpdate();
				}
			}
		};
	}

	private void run(Runnable runnable) {
		Thread runner = new Thread(runnable);
		runner.setDaemon(true);
		runner.start();
	}


	/*
	 * UpdateHandler methods
	 */
	@Override
	public void updateDownloadFileProgress(FileMetadata file, float frac) {
		Platform.runLater(() -> {
			status.setText("Downloading " + file.getPath().getFileName() + " (" + ((int) (100 * frac)) + "%)");
			secondaryPercent.set(frac);
		});
	}

	@Override
	public void updateDownloadProgress(float frac) throws InterruptedException {
		Platform.runLater(() -> primaryPercent.set(frac));
		// to make sure the user sees this
		Thread.sleep(5);
	}

	@Override
	public void failed(Throwable t) {
		Platform.runLater(() -> {
			System.out.println("Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
			t.printStackTrace();
			status.setText("Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
			showAlert("Update Failed", "Update failed: " + t.getMessage());
		});
	}

	@Override
	public void succeeded() {
		Platform.runLater(() -> status.setText("Download complete"));
	}

	@Override
	public void stop() {
		Platform.runLater(() -> running.set(false));
	}

	@Override
	public void doneDownloadFile(FileMetadata file, Path path) throws Throwable {
		System.out.println("DownloadFile: "+ path );
	}
}
