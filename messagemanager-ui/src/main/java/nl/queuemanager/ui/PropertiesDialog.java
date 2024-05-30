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
package nl.queuemanager.ui;

import nl.queuemanager.core.util.CollectionFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class PropertiesDialog extends JDialog {
	private PropertiesPanel propPanel;
	private boolean okPressed = false;

	private PropertiesDialog(Frame owner, Map<String, Object> prop, List<String> predefinedPropertyNames) {
		super(owner, true);

		setTitle("Edit Message Properties");
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setResizable(false);
		
		propPanel = new PropertiesPanel(prop,predefinedPropertyNames);
		propPanel.refreshTable();
		getContentPane().add(propPanel);
		
		final JButton okButton = CommonUITasks.createButton("Ok", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				setVisible(false);
			}
		});
		
		getContentPane().add(okButton);
		getContentPane().add(Box.createVerticalStrut(5));

		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		pack();
	}
	
	public static Map<String, Object> editProperties(Map<String, ? extends Object> properties, List<String> predefinedPropertyNames) {
		Map<String, Object> tempProps = CollectionFactory.newHashMap(properties);
		
		PropertiesDialog dialog = new PropertiesDialog(null, tempProps, predefinedPropertyNames);
		CommonUITasks.centerWindow(dialog);
		dialog.setVisible(true);
		
		if(dialog.okPressed) {
			return tempProps;
		} else {
			return CollectionFactory.newHashMap(properties);
		}
	}
}
