package at.conapi.messagemanager.bootstrap;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.update4j.*;
import org.update4j.inject.InjectSource;
import org.update4j.inject.Injectable;
import org.update4j.service.UpdateHandler;

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
	private volatile boolean abort;

	@InjectSource
	private Stage primaryStage;

	public StartupView(Configuration config, Stage primaryStage) {
		this.config = config;
		this.primaryStage = primaryStage;

		// no min/max/close buttons to look like a splash screen
		primaryStage.initStyle(StageStyle.TRANSPARENT);//.UNDECORATED);
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

		// do auto update when shown to show progress bar
		primaryStage.setOnShown((WindowEvent event) -> {
			if(AppProperties.isAutoUpdate()){
				executeUpdateApplication(true);
			}
			else{
				//run it
				runApplication();
			}
		});

		// ensure splash screen is shown.
		primaryStage.setAlwaysOnTop(true);
		primaryStage.toFront();
	}

	private void updateComplete() {
		// we can now start
		fadeOut();
        config.launch(this);
	}
    private void runApplication(){
		Task<Boolean> checkUpdates = checkUpdates();
		checkUpdates.setOnSucceeded(evt -> {
			Thread run = new Thread(() -> {
				config.launch(this);
				//config.launch();
			});
			run.setName("messagemanager-app-thread");

			//FIXME: add opt-out checkbox
			if (checkUpdates.getValue()) {

				// otherwise dialog is in the background
				primaryStage.setAlwaysOnTop(false);

				ButtonType updateAndLaunch = new ButtonType("Yes", ButtonData.OK_DONE);
				ButtonType skipAndLaunch = new ButtonType("Skip", ButtonData.CANCEL_CLOSE);
				ButtonType alwaysAndLaunch = new ButtonType("ALWAYS", ButtonData.OK_DONE);

				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setHeaderText("Update required");
				alert.setContentText("Application is not up-to-date, update now?");
				alert.getButtonTypes().setAll(updateAndLaunch, skipAndLaunch, alwaysAndLaunch);

				Optional<ButtonType> result = alert.showAndWait();
				primaryStage.setAlwaysOnTop(true);

				if(result.isPresent() && result.get() != skipAndLaunch){
					if(result.get() == alwaysAndLaunch){
						AppProperties.setAutoUpdate(true);
					}
					// update
					executeUpdateApplication(true);
				}
				else {
					// simply start
					updateComplete();
				}
			} else {
				updateComplete();
			}
		});

		runSync(checkUpdates);
	}

	private void fadeOut(){
		//to have a smooth transition
		primaryStage.getScene().setFill(Color.TRANSPARENT);
		//primaryStage.setAlwaysOnTop(true);
		//primaryStage.show();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
		}
        // show for at least some time
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2),primaryStage.getScene().getRoot());
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);
		fadeOut.setCycleCount(1);
		//After fade out, start Message Manager app
		fadeOut.setOnFinished((e) -> {
	//		Thread runner = new Thread(runnable);
	//		runner.setDaemon(true);
	//		runner.start();
			//primaryStage.hide();
		//	primaryStage.setAlwaysOnTop(false);
			//primaryStage.hide();
		});
		// we want this to remain on top
		fadeOut.play();
	}


	private void executeUpdateApplication(boolean sync){
		if (running.get()) {
			abort = true;
			return;
		}
		running.set(true);

		status.setText("Checking for updates...");

		Task<Boolean> checkUpdates = checkUpdates();
		checkUpdates.setOnSucceeded(evt -> {
			if (!checkUpdates.getValue()) {
				status.setText("No updates found");
				running.set(false);
				updateComplete();
			} else {
				Task<Void> doUpdate = new Task<>() {

					@Override
					protected Void call() throws Exception {
						Path zip = Paths.get("messagemanager-update.zip");
						final UpdateResult updateResult = config.update(UpdateOptions.archive(zip).updateHandler(StartupView.this));
						if(updateResult.getException() == null) {
							Archive.read(zip).install();
							// only now the content is downloaded and loaded
							updateComplete();
						}

						// we also launch if update failed
						//updateComplete();

						return null;
					}

				};

				run(doUpdate);
			}
		});


		if(sync) {
            runSync(checkUpdates);
        }
		else {
			run(checkUpdates);
		}
	}

	private void showAlert(final String headerText, final String contentText) {
		primaryStage.setAlwaysOnTop(false);

		Alert alert = new Alert(AlertType.ERROR);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		alert.showAndWait();

		primaryStage.setAlwaysOnTop(true);
	}

	private Task<Boolean> checkUpdates() {
		return new Task<>() {

			@Override
			protected Boolean call() throws Exception {
				return config.requiresUpdate();
			}

		};
	}

	private void run(Runnable runnable) {
		Thread runner = new Thread(runnable);
		runner.setDaemon(true);
		runner.start();
	}

	private void runSync(Runnable runnable) {
		Thread runner = new Thread(runnable);
		runner.setDaemon(true);
		runner.run();
	}

	/*
	 * UpdateHandler methods
	 */
	@Override
	public void updateDownloadFileProgress(FileMetadata file, float frac) throws AbortException {
		Platform.runLater(() -> {
			status.setText("Downloading " + file.getPath().getFileName() + " (" + ((int) (100 * frac)) + "%)");
			secondaryPercent.set(frac);
		});

		if (abort) {
			throw new AbortException();
		}
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
			if (t instanceof AbortException) {
				System.err.println("Update aborted");
				status.setText("Update aborted");
			}
			else {
				System.out.println("Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
				t.printStackTrace();
				status.setText("Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
			}
			showAlert("Update Failed", "Update failed: " + t.getMessage());
			// launch here as only then the alert box remains
			updateComplete();
		});
	}

	@Override
	public void succeeded() {
		Platform.runLater(() -> status.setText("Download complete"));
	}

	@Override
	public void stop() {
		Platform.runLater(() -> running.set(false));
		abort = false;
	}

}
