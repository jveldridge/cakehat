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
 * A field that only accepts integer values. Depending on how this class is
 * constructed, a minimum and maximum value can be enforced.  Note that a leading
 * '+' symbol is permitted.
 *
 * @author jak2
 */
public class IntegerField extends JFormattedTextField
{
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
     * A field that only accepts integer values between the specified
     * minimum and maximum.
     * 
     * @param initValue
     * @param min
     * @param max
     */
    public IntegerField(int initValue, final int min, final int max)
    {
        this(initValue);

        this.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e){}

            public void insertUpdate(DocumentEvent e)
            {
                int value = 0;
                try {
                    String text = getText();
                    if (text.startsWith("+")) {
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
        });
    }

    /**
     * Get the value of this field as an integer.
     *
     * @return
     */
    public int getIntValue()
    {
        try {
            String text = getText();
            if (text.startsWith("+")) {
                text = text.substring(1);
            }
            return Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Set this field as the given value. If between 0 and 9 a leading 0 is
     * appended. So '3' will display as '03'.
     *
     * @param value
     */
    public void setIntValue(int value)
    {
        this.setText(Integer.toString(value));
    }

    /**
     * A formatter that allows only integers, but does allow a leading '+' sign,
     * unlike the formatter provided by {@link NumberFormat#getIntegerInstance()}.
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