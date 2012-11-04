/*
 * Copyright [2008-2009] [Kiev Gama - kiev.gama@gmail.com]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package nl.queuemanager.scripting;

import java.awt.Component;

import javax.script.ScriptEngineFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
/**
 * The renderer that correctly displays the name of available scripting engines.
 * 
 * @author Kiev Gama (kiev.gama@gmail.com)
 *
 */
class ScriptLanguageCellRenderer implements ListCellRenderer {
	private JLabel label = new JLabel();
	
	public ScriptLanguageCellRenderer() {
		label.setOpaque(true);
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		ScriptEngineFactory factory = (ScriptEngineFactory)value;
		
		label.setText(factory.getEngineName());
		
		if (isSelected) {
			label.setForeground(UIManager.getColor("label.selectedForeground"));
			label.setBackground(UIManager.getColor("label.selectedBackground"));
		} else {
			label.setForeground(UIManager.getColor("label.unselectedForeground"));
			label.setBackground(UIManager.getColor("label.unselectedBackground"));
		}
		return label;
	}

}
