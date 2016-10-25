package nl.queuemanager.ui.util.validation;

import javax.swing.JComponent;
 
public interface ValidationStatusListener {
    void validateFailed(JComponent c, String message);  // Called when a component has failed validation.
    void validatePassed(JComponent c);  // Called when a component has passed validation.
}