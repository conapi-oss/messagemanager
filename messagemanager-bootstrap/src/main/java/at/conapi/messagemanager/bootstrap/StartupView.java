package at.conapi.messagemanager.bootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.update4j.*;
import org.update4j.inject.InjectSource;
import org.update4j.inject.Injectable;
import org.update4j.service.UpdateHandler;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StartupView extends FXMLView implements UpdateHandler, Injectable {

	private Configuration config;

	@FXML
	private Label status;

	@FXML
	private ImageView image;

	@FXML
	private GridPane launchContainer;

	@FXML
	@InjectSource
	private CheckBox singleInstanceCheckbox;

	@FXML
	@InjectSource
	private TextField singleInstanceMessage;

	@FXML
	@InjectSource
	private CheckBox newWindowCheckbox;

	@FXML
	private GridPane updateContainer;

	@FXML
	private Pane primary;

	@FXML
	private Pane secondary;

	@FXML
	private StackPane progressContainer;
	
	@FXML
	private CheckBox slow;

	@FXML
	private Button update;

	@FXML
	private Button launch;

	@FXML
	private SVGPath updatePath;

	@FXML
	private SVGPath cancelPath;

	private DoubleProperty primaryPercent;
	private DoubleProperty secondaryPercent;

	private BooleanProperty running;
	private volatile boolean abort;

	@InjectSource
	private Stage primaryStage;

	@InjectSource
	private Image inverted = App.inverted;

	public StartupView(Configuration config, Stage primaryStage) {
		this.config = config;
		this.primaryStage = primaryStage;

		// no min/max/close buttons to look like a splash screen
		primaryStage.initStyle(StageStyle.TRANSPARENT);//.UNDECORATED);

		image.setImage(App.inverted);

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

		cancelPath.visibleProperty().bind(running);
		updatePath.visibleProperty().bind(cancelPath.visibleProperty().not());

		singleInstanceMessage.disableProperty().bind(singleInstanceCheckbox.selectedProperty().not());

		TextSeparator launchSeparator = new TextSeparator("Launch");
		launchContainer.add(launchSeparator, 0, 0, GridPane.REMAINING, 1);

		TextSeparator updateSeparator = new TextSeparator("Update");
		updateContainer.add(updateSeparator, 0, 0, GridPane.REMAINING, 1);

		// do auto update when shown to show progress bar
		primaryStage.setOnShown((WindowEvent event) -> {
			if(AppProperties.isAutoUpdate()){
				executeUpdateApplication(true);
			}
		});
	}

	private void updateComplete() {
		// we can now start
        config.launch(this);
		fadeOut();
	}

	@FXML
	void launchPressed(ActionEvent event) {
		runApplication();
	}

	@FXML
	void updatePressed(ActionEvent event) {
		executeUpdateApplication(false);
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
				ButtonType updateAndLaunch = new ButtonType("Yes", ButtonData.OK_DONE);
				ButtonType skipAndLaunch = new ButtonType("Skip", ButtonData.CANCEL_CLOSE);
				ButtonType alwaysAndLaunch = new ButtonType("ALWAYS", ButtonData.OK_DONE);

				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setHeaderText("Update required");
				alert.setContentText("Application is not up-to-date, update now?");
				alert.getButtonTypes().setAll(updateAndLaunch, skipAndLaunch, alwaysAndLaunch);

				Optional<ButtonType> result = alert.showAndWait();
				if(result.isPresent() && result.get() != skipAndLaunch){
					if(result.get() == alwaysAndLaunch){
						AppProperties.setAutoUpdate(true);
					}
					// update
					executeUpdateApplication(true);
					// launch done when update is complete
					//run.start();
				}
				else {
					// simply start even if X is pressed
					run.start();
				}
			} else {
				run.start();
			}
		});

		//runAndFade(checkUpdates);
		runSync(checkUpdates);
	}

	private void fadeOut(){
		//to have a smooth transition
		primaryStage.getScene().setFill(Color.TRANSPARENT);
		primaryStage.setAlwaysOnTop(true);

		FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5),primaryStage.getScene().getRoot());
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);
		fadeOut.setCycleCount(1);
		//After fade out, start Message Manager app
		fadeOut.setOnFinished((e) -> {
	//		Thread runner = new Thread(runnable);
	//		runner.setDaemon(true);
	//		runner.start();
			//primaryStage.hide();
			primaryStage.setAlwaysOnTop(false);
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

	private void runAndFade(Runnable runnable) {
		//to have a smooth transition
		primaryStage.getScene().setFill(Color.TRANSPARENT);
		primaryStage.setAlwaysOnTop(true);

		FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5),primaryStage.getScene().getRoot());
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);
		fadeOut.setCycleCount(1);
		//After fade out, start Message Manager app
		fadeOut.setOnFinished((e) -> {
			Thread runner = new Thread(runnable);
			runner.setDaemon(true);
			runner.start();
			//primaryStage.hide();
			primaryStage.setAlwaysOnTop(false);
		});
		// we want this to remain on top
		fadeOut.play();
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
		Thread.sleep(1);
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
