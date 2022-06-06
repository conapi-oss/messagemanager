package nl.queuemanager.debug;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
 
public class DebugEventListener implements AWTEventListener {
    public void eventDispatched(AWTEvent event) {
        Object o = event.getSource();
        if (o instanceof JComponent) {
    	    JComponent source = (JComponent) o;
    	    Border border = source.getBorder();
 
	        switch (event.getID()) {
	        	case MouseEvent.MOUSE_ENTERED:
	        		if (border != null) {
	        		    source.setBorder(new DebugBorder(border));
	        		}
	        	    break;
	        	case MouseEvent.MOUSE_EXITED:
        			if (border != null && border instanceof DebugBorder) {
        			    source.setBorder(((DebugBorder) border).getDelegate());
        			}
	        	    break;
	        }
        }
    }
}