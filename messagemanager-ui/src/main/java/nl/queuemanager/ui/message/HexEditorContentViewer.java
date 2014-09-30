package nl.queuemanager.ui.message;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import com.jhe.hexed.JHexEditor;

abstract class HexEditorContentViewer<T> implements ContentViewer<T> {
	
	protected abstract byte[] getContent(T object);
	
	public JComponent createUI(T object) {
		JHexEditor hexEditor = new JHexEditor(getContent(object));
		hexEditor.setReadOnly(true);
		return new JScrollPane(hexEditor);
	}
}
