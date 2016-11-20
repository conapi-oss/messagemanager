package nl.queuemanager.ui.util.validation;
 
import java.awt.Color;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
 
/**
 * This class handles most of the details of validating a component, including
 * all display elements such as popup help boxes and color changes.
 * 
 * @author Michael Urban
 * @version Beta 1
 * @see ValidationStatusListener
 */
 
public abstract class AbstractValidator extends InputVerifier {
	private String message;
    private ValidationStatusListener parent;
    private boolean blockField = true;
	
    private AbstractValidator(String message) {
        this.message = message;
    }
	
    /**
     * @param parent An object that implements the ValidationCapable interface.
     * @param c The JComponent to be validated.
     * @param message A message to be displayed in the popup help tip if validation fails.
     */	
    public AbstractValidator (ValidationStatusListener parent, String message) {		
        this(message);
        this.parent = parent;
    }
	
    /**
     * Implement the actual validation logic in this method. The method should
     * return false if data is invalid and true if it is valid. It is also possible
     * to set the popup message text with setMessage() before returning, and thus
     * customize the message text for different types of validation problems.
     * 
     * @param c The JComponent to be validated.
     * @return false if data is invalid. true if it is valid.
     */
	
    protected abstract boolean validationCriteria(JComponent c);
	
    /**
     * This method is called by Java when a component needs to be validated.
     * It should not be called directly. Do not override this method unless
     * you really want to change validation behavior. Implement
     * validationCriteria() instead.
     */
	
    @Override
	public boolean verify(JComponent c) {		
        if (!validationCriteria(c)) {

        	if(parent != null) {
        		parent.validateFailed(c, message);
        	}
			
            c.setBackground(Color.PINK);
        	return false || !isBlockField();
        }
        
        c.setBackground(Color.WHITE);		
        if(parent != null) {
        	parent.validatePassed(c);
        }
		
        return true;
    }
	
    /**
     * Changes the message that appears in the popup help tip when a component's
     * data is invalid. Subclasses can use this to provide context sensitive help
     * depending on what the user did wrong.
     * 
     * @param message
     */	
    protected void setMessage(String message) {
    	this.message = message;
    }

	public boolean isBlockField() {
		return blockField;
	}

	public void setBlockField(boolean blockField) {
		this.blockField = blockField;
	}	
}