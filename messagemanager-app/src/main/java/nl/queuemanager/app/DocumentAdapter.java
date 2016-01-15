package nl.queuemanager.app;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class DocumentAdapter implements DocumentListener {

	@Override
	public final void insertUpdate(DocumentEvent e) {
		updated(e);
	}

	@Override
	public final void removeUpdate(DocumentEvent e) {
		updated(e);
	}

	@Override
	public final void changedUpdate(DocumentEvent e) {
		updated(e);
	}
	
	public abstract void updated(DocumentEvent e);

}
