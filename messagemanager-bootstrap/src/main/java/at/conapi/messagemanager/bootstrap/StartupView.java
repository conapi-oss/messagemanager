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
import javafx.util.Duration;
import org.update4j.*;
import org.update4j.inject.InjectSource;
import org.update4j.inject.Injectable;
import org.update4j.service.UpdateHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	}

	private void launchConfig() {
		// we can now start
		fadeOut();

		final AppLauncher launcher = new AppLauncher();
        config.launch(launcher);

		if(launcher.isLaunchFailed()){
			final Exception e = launcher.getLaunchError();
			showAlert("Launch Failed", "Unable to launch the application: " + e.getMessage());
			// exit the app
			Platform.exit();
		}
	}
    public void runApplication() throws IOException {

		if (config.requiresUpdate()) {
			final ButtonType updateAndLaunch = new ButtonType("Yes", ButtonData.OK_DONE);
			final ButtonType skipAndLaunch = new ButtonType("Skip", ButtonData.CANCEL_CLOSE);
			final ButtonType alwaysAndLaunch = new ButtonType("ALWAYS", ButtonData.OK_DONE);
			Optional<ButtonType> result = Optional.of(skipAndLaunch);

			if(!AppProperties.isAutoUpdate()) {
				// otherwise dialog is in the background
				mainStage.setAlwaysOnTop(false);

				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setHeaderText("Update required");
				alert.setContentText("Application is not up-to-date, update now?");
				alert.getButtonTypes().setAll(updateAndLaunch, skipAndLaunch, alwaysAndLaunch);

				result = alert.showAndWait();
				if(result.get() == alwaysAndLaunch){
					AppProperties.setAutoUpdate(true);
				}
				mainStage.setAlwaysOnTop(true);
			}

			if(AppProperties.isAutoUpdate() || (result.isPresent() && result.get() != skipAndLaunch)){
				// autoupdate is on or any button except skip was pressed
				try {
					// update
					updateApplication();
				}
				catch (Throwable t){
					// show the error but then still try to launch
					System.out.println("Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
					t.printStackTrace();
					status.setText("Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());

					showAlert("Update Failed", "Update failed: " + t.getMessage());
				}
			}
		}
		// simply start
		launchConfig();
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


	private void updateApplication() throws Throwable {
		status.setText("Checking for updates...");
		if (config.requiresUpdate()) {
			Path zip = Paths.get("messagemanager-update.zip");
			final UpdateResult updateResult = config.update(UpdateOptions.archive(zip).updateHandler(StartupView.this));
			if (updateResult.getException() == null) {
				status.setText("Download complete");
				Archive.read(zip).install();
				// only now the content is downloaded and loaded
			}
			else{
				throw updateResult.getException();
			}
		}
	}

	private void showAlert(final String headerText, final String contentText) {
		mainStage.setAlwaysOnTop(false);

		Alert alert = new Alert(AlertType.ERROR);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		alert.showAndWait();

		mainStage.setAlwaysOnTop(true);
	}

	/*
	 * UpdateHandler methods
	 */
	@Override
	public void updateDownloadFileProgress(FileMetadata file, float frac) {
		Platform.runLater(() -> { //TODO: no longer showing
			status.setText("Downloading " + file.getPath().getFileName() + " (" + ((int) (100 * frac)) + "%)");
			secondaryPercent.set(frac);
		});
	}

	@Override
	public void updateDownloadProgress(float frac) throws InterruptedException {
		Platform.runLater(() -> primaryPercent.set(frac));
		// to make sure the user sees this
		Thread.sleep(10);
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

	}

}
