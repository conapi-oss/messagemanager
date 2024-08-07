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

import jakarta.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

@Singleton
public class DesktopHelperJRE6 implements DesktopHelper {

	public void addLink(final JComponent component, final URI uri) {
		if(!Desktop.isDesktopSupported()) {
			System.err.println("Desktop is not supported on this platform");
			return;
		}
		
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(MouseEvent.BUTTON1 == e.getButton()) {
					try {
						Desktop.getDesktop().browse(uri);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		
		component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public void makeMacSheet(JDialog dialog) {
		dialog.setModal(true);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.getRootPane().putClientProperty("apple.awt.documentModalSheet", "true");
	}
}
