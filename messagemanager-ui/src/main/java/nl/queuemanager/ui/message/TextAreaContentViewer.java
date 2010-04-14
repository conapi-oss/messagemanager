package nl.queuemanager.ui.message;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import nl.queuemanager.ui.util.JSearchableTextArea;

abstract class TextAreaContentViewer<T> implements ContentViewer<T> {
	
	protected abstract String getContent(T object);
	
	public JComponent createUI(T object) {
		final JTextArea textArea = new JSearchableTextArea();
		textArea.setEditable(false);
		textArea.setText(getContent(object));
		textArea.setCaretPosition(0);

		textArea.setToolTipText("Type to search");
		
		return textArea;
	}
}
