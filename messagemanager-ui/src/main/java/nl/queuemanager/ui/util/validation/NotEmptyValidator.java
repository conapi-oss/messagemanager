package nl.queuemanager.ui.util.validation;
 
import javax.swing.JComponent;
import javax.swing.JTextField;
 
/**
 * A class for performing basic validation on text fields. All it does is make 
 * sure that they are not null.
 * 
 * @author Michael Urban
 */
 
public class NotEmptyValidator extends AbstractValidator {
    public NotEmptyValidator(ValidationStatusListener parent, String message) {
        super(parent, message);
    }
	
    @Override
	protected boolean validationCriteria(JComponent c) {
        if (((JTextField)c).getText().equals(""))
            return false;
        return true;
    }
}