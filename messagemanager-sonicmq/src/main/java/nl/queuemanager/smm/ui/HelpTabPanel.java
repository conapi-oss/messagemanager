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
package nl.queuemanager.smm.ui;

import java.net.URL;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelpContentViewer;
import javax.help.JHelpNavigator;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

@SuppressWarnings("serial")
class HelpTabPanel extends JSplitPane {

	public HelpTabPanel() {
		super(JSplitPane.HORIZONTAL_SPLIT);
		
	    ClassLoader cl = 
	        getClass().getClassLoader();
	    
	    URL hsURL = HelpSet.findHelpSet(cl, "javahelp/jhelpset.hs");
		try {
			HelpSet hs = new HelpSet(null, hsURL);
			
			JHelpContentViewer viewer = new JHelpContentViewer(hs);
			JHelpNavigator xnav = (JHelpNavigator) hs.getNavigatorView("TOC").createNavigator(viewer.getModel());
			
			setLeftComponent(xnav);
			setRightComponent(viewer);
		} catch (HelpSetException e) {
			// Inform the user
			setLeftComponent(new JLabel("Unable to load the manual: " + e.getMessage()));
		}
		    
	}
}
