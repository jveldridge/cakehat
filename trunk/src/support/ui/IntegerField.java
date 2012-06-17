package support.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A field that only accepts integer values. Depending on how this class is constructed, a minimum and maximum value can
 * be enforced.  Note that a leading '+' symbol is permitted.
 *
 * @author jak2
 */
public class IntegerField extends JFormattedTextField
{
    private boolean _showLeadingZero = false;
    private DocumentListener _rangeRestricter;

    /**
     * A field that only accepts integer values.
     */
    public IntegerField()
    {
        this(0);
    }

    /**
     * A field that only accepts integer values.
     *
     * @param initValue
     */
    public IntegerField(int initValue)
    {
        super(new IntegerFormat());

        this.setIntValue(initValue);

        this.setColumns(2);

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

    /**
     * Restricts the allowable range between {@code min} and {@code max} inclusive.
     *
     * @param min
     * @param max
     */
    public void setRangeRestriction(final int min, final int max)
    {
        //If there was previous range restricter, remove it
        if(_rangeRestricter != null)
        {
            this.getDocument().removeDocumentListener(_rangeRestricter);
        }

        _rangeRestricter = new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e){}

            public void insertUpdate(DocumentEvent e)
            {
                int value = 0;
                try
                {
                    String text = getText();
                    if (text.startsWith("+"))
                    {
                        text = text.substring(1);
                    }
                    value = Integer.parseInt(text);
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
        };

        this.getDocument().addDocumentListener(_rangeRestricter);

        //Apply it immediately
        this.setIntValue(this.getIntValue());
    }

    /**
     * Get the value of this field as an integer.
     *
     * @return
     */
    public int getIntValue()
    {
        int value = 0;

        try
        {
            String text = getText();
            if (text.startsWith("+"))
            {
                text = text.substring(1);
            }
            value = Integer.parseInt(text);
        }
        catch (NumberFormatException e) { }

        return value;
    }

    /**
     * Set this field as the given value.
     *
     * @param value
     */
    public void setIntValue(int value)
    {
        String valueText = Integer.toString(value);
        if(_showLeadingZero && value > -1 && value < 10)
        {
            valueText = "0" + valueText;
        }
        this.setText(valueText);
    }

    /**
     * If {@code show} is {@code true} then when the integer is between {@code 0} and {@code 9} inclusive it will be
     * displayed with a leading {@code 0}. For example the value {@code 3} will be displayed as {@code 03}.
     *
     * @param show
     */
    public void setShowLeadingZero(boolean show)
    {
        _showLeadingZero = show;

        //Apply immediately
        this.setIntValue(this.getIntValue());
    }

    /**
     * A formatter that allows only integers, but does allow a leading '+' sign, unlike the formatter provided by
     * {@link NumberFormat#getIntegerInstance()}.
     */
    private static class IntegerFormat extends Format
    {
        //Delegate everything to the integer formatter
        private static NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance();

        @Override
        public StringBuffer format(Object o, StringBuffer sb, FieldPosition fp)
        {
            return INTEGER_FORMAT.format(o, sb, fp);
        }

        @Override
        public Object parseObject(String string, ParsePosition pp)
        {
            if(string.startsWith("+"))
            {
                string = string.substring(1);
            }
            return INTEGER_FORMAT.parseObject(string, pp);
        }

        @Override
        public AttributedCharacterIterator formatToCharacterIterator(Object obj)
        {
            return INTEGER_FORMAT.formatToCharacterIterator(obj);
        }
    }
}