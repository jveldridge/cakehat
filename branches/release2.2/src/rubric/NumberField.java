package rubric;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import rubric.Rubric.Subsection;

import utils.Allocator;

/**
 * A text field specifically meant for numbers. This class cannot be directly
 * constructed, instead use one of its public static methods to get an instance.
 *
 * Instances can be retrieved as either managed or non-managed. If managed, when
 * its value changes it directly updates the rubric and informs the StateManager
 * which keeps track of the save state.
 *
 * @author spoletto
 * @author jak2
 */
class NumberField extends JFormattedTextField
{    
    private Subsection _subsection = null;
    private RubricPanel _panel = null;
    private StateManager _stateManager;
    private double _oldValue;

    private NumberField(double value, boolean editable)
    {
        super(NumberFormat.getNumberInstance());

        this.setValue(value);
        if (value == 0) {
            this.setBackground(java.awt.Color.YELLOW);
        }
        _oldValue = value;

        super.setEditable(editable);
        this.setColumns(5);
        this.setHorizontalAlignment(CENTER);
    }

    /**
     * Sets the value of this NumberField while making sure it looks nice by
     * displaying as an integer value if it has no decimal part, and otherwise
     * rounding as specified by the GeneralUtilities's doubleToString(...)
     * method.
     *
     * @param value value to display
     */
    public void setValue(double value)
    {
        String text;
        if(value == (int) value)
        {
            text = (int) value + "";
        }
        else
        {
            text = Allocator.getGeneralUtilities().doubleToString(value);
        }
        this.setText(text);
    }

    /**
     * Returns an editable NumberField with the specified value.
     *
     * @param value value to display
     * @param managed   whether it should be listen for changes and report them
     *                  to a StateManager
     * @return editable NumberField with the specified value
     */
    private static NumberField getAsEditable(double value, boolean managed)
    {
        final NumberField field = new NumberField(value, true);

        if(!managed)
        {
            return field;
        }

        field.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e){}

            public void insertUpdate(DocumentEvent e)
            {
                if(field.isEditable())
                {
                    try
                    {
                        double newValue = Double.parseDouble(field.getText());
                        if (newValue != field._oldValue)
                        {
                            field._oldValue = newValue;
                            field._stateManager.rubricChanged();
                        }
                        if (field._subsection != null)
                        {
                            if (newValue > field._subsection.getOutOf())
                            {
                                field.setBackground(java.awt.Color.RED);
                            }
                            else if (newValue == 0)
                            {
                                field.setBackground(java.awt.Color.YELLOW);
                            }
                            else
                            {
                                field.setBackground(java.awt.Color.WHITE);
                            }
                            field._subsection.setScore(newValue);
                            field._panel.updateTotals();
                        }
                    }
                    catch (Exception exc) {}
                }
            }

            public void removeUpdate(DocumentEvent e)
            {
                insertUpdate(e);
            }
        });

        field.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        field.selectAll();
                    }
                });
            }

            public void focusLost(FocusEvent e)
            {
                if (field.getText().isEmpty())
                {
                    field.setText("0");
                    try
                    {
                        double newValue = 0.0;
                        if (newValue != field._oldValue)
                        {
                            field._oldValue = newValue;
                            field._stateManager.rubricChanged();
                        }
                        if (field._subsection != null)
                        {
                            field._subsection.setScore(newValue);
                            field._panel.updateTotals();
                        }
                    }
                    catch (Exception exc) {}
                }
            }
        });

        return field;
    }

    /**
     * Returns a NumberField that displays a Subsection's score.
     *
     * @param subsection Subsection whose score will be displayed
     * @param panel Containing panel, if not managed pass in null
     * @param manager StateManager that keeps track of this field's state, if this
     *                NumberField is not meant to be managed, pass in null
     * @return
     */
    public static NumberField getAsScore(Subsection subsection, RubricPanel panel, StateManager manager)
    {
        //If manager was passed in then this field is managed
        NumberField field = getAsEditable(subsection.getScore(), manager != null);

        //If managed, add data
        if(manager != null)
        {
            field._subsection = subsection;
            field._panel = panel;
            field._stateManager = manager;
        }

        return field;
    }

    /**
     * Sets whether this field is editable or not. Only call this on a field
     * that was initialized as editable. Calling this on a field that was
     * initialized as uneditable will cause issues.
     *
     * @param editable
     */
    @Override
    public void setEditable(boolean editable)
    {
        super.setEditable(editable);

        if(editable)
        {
            this.setBackground(java.awt.Color.WHITE);
        }
        else
        {
            this.setBackground(java.awt.Color.LIGHT_GRAY);
        }
    }

    /**
     * Returns an uneditable NumberField. Given a light gray background to
     * visually represent it's uneditable nature.
     * 
     * @param value
     * @return
     */
    public static NumberField getAsUneditable(double value)
    {
        NumberField field = new NumberField(value, false);
        field.setBackground(java.awt.Color.LIGHT_GRAY);

        return field;
    }
}