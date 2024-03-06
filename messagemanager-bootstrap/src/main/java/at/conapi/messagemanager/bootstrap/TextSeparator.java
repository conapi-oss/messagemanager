package at.conapi.messagemanager.bootstrap;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class TextSeparator extends FXMLView {

	@FXML
	private Label text;
	@FXML
	private HBox box;

	public TextSeparator(String text) {
		this.text.setText(text);
	}

	public TextSeparator() {
		this("");
	}

	@FXML
	private void initialize() {
		text.visibleProperty().bind(text.textProperty().isNotEmpty());
		box.spacingProperty().bind(Bindings.when(text.visibleProperty()).then(15).otherwise(0));
	}
}
