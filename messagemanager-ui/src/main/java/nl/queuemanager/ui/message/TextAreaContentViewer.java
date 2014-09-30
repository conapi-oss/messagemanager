package nl.queuemanager.ui.message;

import nl.queuemanager.ui.util.JSearchableTextArea;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

abstract class TextAreaContentViewer<T> implements ContentViewer<T> {
	
	protected abstract String getContent(T object);
	
	public RTextScrollPane createUI(T object) {
		final RSyntaxTextArea textArea = createTextArea(object);
		return new RTextScrollPane(textArea);
	}

	protected RSyntaxTextArea createTextArea(T object) {
		final RSyntaxTextArea textArea = new JSearchableTextArea();
		textArea.setText(getContent(object));
		textArea.setEditable(false);
		textArea.setCaretPosition(0);
		return textArea;
	}
}
