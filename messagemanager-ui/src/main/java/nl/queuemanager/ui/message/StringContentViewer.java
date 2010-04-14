package nl.queuemanager.ui.message;

import javax.swing.JComponent;
import javax.swing.JLabel;

class StringContentViewer implements ContentViewer<String> {

	public JComponent createUI(String str) {
		return new JLabel(str);
	}

	public boolean supports(String str) {
		return str != null;
	}		
}
