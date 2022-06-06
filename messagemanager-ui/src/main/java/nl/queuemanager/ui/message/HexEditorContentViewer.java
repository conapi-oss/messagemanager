package nl.queuemanager.ui.message;

import com.jhe.hexed.JHexEditor;

import javax.swing.*;

abstract class HexEditorContentViewer<T> implements ContentViewer<T> {
	
	protected abstract byte[] getContent(T object);
	
	public JComponent createUI(T object) {
		byte[] content = getContent(object);
		JHexEditor hexEditor = new JHexEditor(content != null ? content : new byte[] {});
		hexEditor.setReadOnly(true);
		return new JScrollPane(hexEditor);
	}
}
