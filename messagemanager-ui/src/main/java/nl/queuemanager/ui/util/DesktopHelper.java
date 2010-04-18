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

import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JDialog;

public interface DesktopHelper {

	/**
	 * Makes the component a link to the specified URI that opens in the default browser.
	 * 
	 * @param component
	 * @param uri
	 */
	public abstract void addLink(JComponent component, URI uri);
	
	/**
	 * When running on Mac OS X JRE1.6, make the dialog a pop-out sheet.
	 * 
	 * @param dialog
	 */
	public abstract void makeMacSheet(JDialog dialog);
	
}
