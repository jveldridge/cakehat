package support.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 * A JTextField which shows shadow text when this field not in focus and has no input text in it. Shadow text is
 * displayed in light gray.
 *
 * @author jak2
 */
public class ShadowJTextField extends JTextField
{
    private static final Color SHADOW_COLOR = Color.LIGHT_GRAY;
    private final String _startText;
    private Color _prevForeground;
    private boolean _inShadow;

    public ShadowJTextField(String startText)
    {
        super(startText);

        _inShadow = true;
        _startText = startText;
        _prevForeground = this.getForeground();
        this.setForeground(SHADOW_COLOR);

        this.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent fe)
            {
                if(_inShadow)
                {
                    _inShadow = false;
                    setForeground(_prevForeground);
                    setText("");
                }
            }

            public void focusLost(FocusEvent fe)
            {
                if(getText() == null || getText().isEmpty())
                {
                    _inShadow = true;
                    _prevForeground = getForeground();
                    setForeground(SHADOW_COLOR);
                    setText(_startText);
                }
            }
        });
    }

    @Override
    public String getText()
    {
        String text;
        if(_inShadow)
        {
            text = null;
        }
        else
        {
            text = super.getText();
        }

        return text;
    }
}