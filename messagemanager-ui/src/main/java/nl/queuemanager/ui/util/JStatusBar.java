/**

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.queuemanager.ui.util;

import nl.queuemanager.ui.progress.Throbber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Implements a status bar for a JFrame (anything with a <code>BorderLayout</code>, really). 
 * Recommended placement is <code>BorderLayout.SOUTH</code>.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
public class JStatusBar extends JPanel {
	private final JLabel statusLabel;
	private final JButton cancelButton;
	private final JProgressBar progressBar;
	private final Throbber throbber;
	
	private volatile boolean cancelPressed;
	
	/**
	 * Create a new JStatusBar object.
	 */
	public JStatusBar() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		setPreferredSize(new Dimension(10, 23));

		statusLabel = new JLabel();
		throbber = new Throbber();
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCancelEnabled(false);
				cancelPressed = true;
			}
		});
		
		progressBar = new JProgressBar();
		progressBar.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
		
		add(statusLabel);
		add(Box.createHorizontalGlue());
		add(cancelButton);
		add(Box.createHorizontalStrut(5));
		add(progressBar);
		add(Box.createHorizontalStrut(5));
		add(throbber);
		
		setText("");
		setBusy(false);
		disableProgressBar();
		setCancelEnabled(false);
	}
	
	/**
	 * Set the text in the status bar and clear the error icon.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		statusLabel.setText(text);
		statusLabel.setIcon(null);
	}
	
	/**
	 * Set the busy flag. When the busy flag is on, the throbber is visible.
	 * 
	 * @param busy
	 */
	public void setBusy(boolean busy) {
		throbber.setVisible(busy);
	}
	
	/**
	 * Enable the progress bar.
	 * 
	 * @param min
	 * @param max
	 */
	public void enableProgressBar(int min, int max) {
		progressBar.setMinimum(min);
		progressBar.setMaximum(max);			
		progressBar.setVisible(true);
	}
	
	public void setProgressAmount(int amount) {
		progressBar.setValue(amount);
	}

	public void disableProgressBar() {
		progressBar.setVisible(false);
	}
	
	public void setCancelEnabled(boolean enabled) {
		cancelPressed = false;
		cancelButton.setVisible(enabled);
	}
	
	public boolean getCancelPressed() {
		return cancelPressed;
	}
}
