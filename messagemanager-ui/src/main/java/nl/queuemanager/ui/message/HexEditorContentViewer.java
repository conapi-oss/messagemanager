package nl.queuemanager.ui.message;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import com.jhe.hexed.JHexEditor;

abstract class HexEditorContentViewer<T> implements ContentViewer<T> {
	
	protected abstract byte[] getContent(T object);
	
	public JComponent createUI(T object) {
		byte[] content = getContent(object);
		JHexEditor hexEditor = new JHexEditor(content != null ? content : new byte[] {});
		hexEditor.setReadOnly(true);
		return new JScrollPane(hexEditor);
	}
}
