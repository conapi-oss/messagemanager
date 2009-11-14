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

import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * JTextField that can only contain Integer or IPv4 values.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class JIntegerField extends JTextField {
	private long maxValue = Integer.MAX_VALUE;
	private long minValue = 0;
	private int maxLength = String.valueOf(maxValue).length();

	private boolean isIPField = false;

	/**
	 * Default constructor for IntegerTextField.
	 */
	public JIntegerField(int size) {
		super(size);
	}

	@Override
	protected Document createDefaultModel() {
		return new IntegerDocument();
	}

	/**
	 * Set the minimal value for the field.
	 * 
	 * @param value
	 */
	public void setMinValue(long value) {
		minValue = value;
	}

	/**
	 * Get the minimal value that can be entered.
	 * 
	 * @return
	 */
	public long getMinValue() {
		return minValue;
	}

	public void setIPField(boolean value) {
		isIPField = value;
	}

	public boolean getIPField() {
		return isIPField;
	}

	/**
	 * Set the maximum value that can be entered.
	 * 
	 * @param value
	 */
	public void setMaxValue(long value) {
		maxValue = value;
		setMaxLength(String.valueOf(maxValue).length());
	}

	/**
	 * Get the maximum value that can be entered.
	 * @return
	 */
	public long getMaxValue() {
		return maxValue;
	}

	/**
	 * Set the maximum length of the value.
	 * 
	 * @param value
	 */
	public void setMaxLength(int value) {
		maxLength = value;
	}

	/**
	 * Get the maximum length of the value.
	 * 
	 * @return
	 */
	public int getMaxLength() {
		return maxLength;
	}
	
	/**
	 * Get the integer value of the field.
	 * 
	 * @return
	 */
	public int getValue() {
		if("".equals(getText()) || getText() == null)
			return 0;
		return Integer.parseInt(getText());
	}
	
	/**
	 * Set the value.
	 * 
	 * @param value
	 */
	public void setValue(int value) {
		setText(Integer.toString(value));
	}

	private class IntegerDocument extends PlainDocument {
		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			long typedValue = -1;

			StringBuffer textBuffer = new StringBuffer(
					JIntegerField.this.getText().trim());
			// The offset argument must be greater than or equal to 0, and less
			// than or equal to the length of this string buffer
			if ((offs >= 0) && (offs <= textBuffer.length())) {
				textBuffer.insert(offs, str);
				String textValue = textBuffer.toString();
				if (textBuffer.length() > maxLength) {
//					JOptionPane.showMessageDialog(JIntegerField.this,
//							"The number of characters 	must be less than or equal to "
//									+ getMaxLength(), "Error Message",
//							JOptionPane.ERROR_MESSAGE);
					return;
				}

				if ((textValue == null) || (textValue.equals(""))) {
					remove(0, getLength());
					super.insertString(0, "", null);
					return;
				}

				if (textValue.equals("-") && minValue < 0) {
					super.insertString(offs, str, a);
					return;
				}

				// if(maxLength == 3 && str.equals(".") && isIPField)
				if (str.equals(".") && isIPField) {
					super.insertString(offs, str, a);
					return;
				} else {
					try {
						typedValue = Long.parseLong(textValue);
						if ((typedValue > maxValue) || (typedValue < minValue)) {
							JOptionPane.showMessageDialog(JIntegerField.this,
									"The value can only be from "
											+ getMinValue() + " to "
											+ getMaxValue(), "Error Message",
									JOptionPane.ERROR_MESSAGE);
						} else {
							super.insertString(offs, str, a);
						}
					} catch (NumberFormatException ex) {
						Toolkit.getDefaultToolkit().beep();
//						This message is annoying, don't show it.
//						JOptionPane.showMessageDialog(JIntegerField.this,
//								"Only numeric values allowed.",
//								"Error Message", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}
}
