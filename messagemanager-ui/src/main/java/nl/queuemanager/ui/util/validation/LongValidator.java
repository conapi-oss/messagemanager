package nl.queuemanager.ui.util.validation;

import javax.swing.*;

/**
 * A class for performing basic validation on text fields.
 * Verifies that the value is a valid long.
 * 
 * @author Stefan Fritz
 */
 
public class LongValidator extends AbstractValidator {
    private boolean allowEmpty = false;

    public LongValidator(ValidationStatusListener parent, String message) {
        super(parent, message);
    }

    public LongValidator(ValidationStatusListener parent, String message, boolean allowEmpty) {
        super(parent, message);
        this.allowEmpty = allowEmpty;
    }

    @Override
	protected boolean validationCriteria(JComponent c) {
        try {
            String text = ((JTextField)c).getText();

            // Allow empty text if allowed
            if (text.isEmpty() && allowEmpty) {
                return true;
            }

            Long.parseLong(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}