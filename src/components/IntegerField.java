package components;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author jak2
 */
public class IntegerField extends JFormattedTextField
{
    public IntegerField() { }

    public IntegerField(int initValue, final int min, final int max)
    {
        super(NumberFormat.getIntegerInstance());

        this.setIntValue(initValue);

        this.setColumns(2);

        this.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e){}

            public void insertUpdate(DocumentEvent e)
            {
                int value = 0;
                try
                {
                    value = Integer.parseInt(getText());
                }
                catch (Exception exc) {}

                int newValue = value;
                if(value < min)
                {
                    newValue = min;
                }
                if(value > max)
                {
                    newValue = max;
                }

                final boolean changeValue = (newValue != value);
                final int finalValue = newValue;

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        if(changeValue)
                        {
                            setIntValue(finalValue);
                        }
                    }
                });
            }

            public void removeUpdate(DocumentEvent e)
            {
                insertUpdate(e);
            }
        });

        this.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent fe) { }


            public void focusLost(FocusEvent fe)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        setIntValue(getIntValue());
                    }
                });
            }
        });
    }

    public int getIntValue()
    {
        return Integer.parseInt(getText());
    }

    public void setIntValue(int value)
    {
        String valueText = value + "";
        if(value > -1 && value < 10)
        {
            valueText = "0" + valueText;
        }
        this.setText(valueText);
    }
}