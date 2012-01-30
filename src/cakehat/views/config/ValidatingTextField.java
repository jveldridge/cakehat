package cakehat.views.config;

import support.ui.DocumentAdapter;
import cakehat.database.DbDataItem;
import cakehat.views.config.ValidationResult.ValidationState;
import com.google.common.collect.ImmutableMap;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Map;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

/**
 *
 * @author jak2
 */
abstract class ValidatingTextField extends JTextField
{
    private static final Map<ValidationState, Color> VALIDATION_STATE_TO_COLOR =
            ImmutableMap.<ValidationState, Color>builder()
            .put(ValidationState.NOT_VALIDATED, Color.WHITE)
            .put(ValidationState.NO_KNOWN_ISSUE, Color.WHITE)
            .put(ValidationState.WARNING, new Color(255, 255, 204)) //Yellow
            .put(ValidationState.ERROR, new Color(255, 204, 204))   //Red
            .build();
    
    public ValidatingTextField()
    {   
        this.getDocument().addDocumentListener(new DocumentAdapter()
        {
            @Override
            public void modificationOccurred(DocumentEvent de)
            {
                textModified();
            }
        });
        
        this.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent fe)
            {
                fieldFocusLost();
            }
        });
        
        //Set the initial text from the database value and perform a validation pass on it - but do this after the
        //object has finished constructin so that the subclass is in a proper state
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                setTextToDbValue();
            }
        });
    }
    
    public final void setTextToDbValue()
    {
        String initialValue = getDbValue();
        setText(initialValue);

        //The document listener will not be modified if the initial value is an empty string as there is no
        //change, so force validation to be run
        if(initialValue.isEmpty())
        {
            textModified();
        }
    }
    
    private void textModified()
    {
        ValidationResult result = validate(this.getText());
        
        this.setBackground(VALIDATION_STATE_TO_COLOR.get(result.getValidationState()));
        this.setToolTipText(result.getMessage());
    }
    
    private void fieldFocusLost()
    {
        String textInField = this.getText();
        String textInDbItem = this.getDbValue();
        
        //If the value has changed
        if(!textInField.equals(textInDbItem))
        {
            //If the value is not acceptable, revert it to the value currently in the database item
            if(this.validate(textInField).getValidationState() == ValidationState.ERROR)
            {
                this.setText(textInDbItem);
            }
            //Otherwise ask the subclass to apply the change
            else
            {
                this.applyChange(textInField);
            }
        }
    }
    
    /**
     * Returns a value stored in the {@link DbDataItem} for the value this text field is visualizing. If the field
     * loses focus and is in error the value of the text field will be reverted to display this value.
     * 
     * @return 
     */
    protected abstract String getDbValue();
    
    /**
     * Returns a determination on the validity of {@code value}.
     * 
     * @param value
     * @return 
     */
    protected abstract ValidationResult validate(String value);
    
    /**
     * Called to respond to the user being done with the text field (loss of focus).
     * 
     * @param newValue 
     */
    protected abstract void applyChange(String newValue);
}