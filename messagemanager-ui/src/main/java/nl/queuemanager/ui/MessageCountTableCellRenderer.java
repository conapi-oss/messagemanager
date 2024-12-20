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

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;

/**
 * <p>
 * This class is used to change the cell colour to red if the number of
 * messages in a queue is bigger than zero
 * </p>
 */
@SuppressWarnings("serial")
public class MessageCountTableCellRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		
		Component cell = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);

		if (!isSelected) {
			int amount = (value instanceof Integer) ? (Integer) value : 0;
			if (amount > 0) {
				cell.setForeground(Color.BLACK);
				cell.setBackground(Color.decode("#e0de2a"));
			} else {
				cell.setForeground((Color) UIManager.get("Table.foreground"));
				cell.setBackground((Color) UIManager.get("Table.background"));
			}
		}
		
		return cell;
	}
}