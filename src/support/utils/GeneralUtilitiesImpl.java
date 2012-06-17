package support.utils;

import java.awt.FontMetrics;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class GeneralUtilitiesImpl implements GeneralUtilities
{
    @Override
    public <T> boolean containsAny(Collection<T> col1, Collection<T> col2)
    {
        for (T item : col2)
        {
            if (col1.contains(item))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public <E extends Throwable> E findInStack(Throwable throwable, Class<E> throwableClass)
    {
        if(throwable == null)
        {
            return null;
        }
        else if(throwableClass.isInstance(throwable))
        {
            return throwableClass.cast(throwable);
        }
        else
        {
            return findInStack(throwable.getCause(), throwableClass);
        }
    }

    @Override
    public JButton createTextCenteredButton(String text, Icon icon, int buttonWidth, boolean iconOnLeft)
    {
        JButton button = new JButton(text, icon);

        if(iconOnLeft)
        {
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
        }
        else
        {
            button.setHorizontalAlignment(SwingConstants.RIGHT);
            button.setHorizontalTextPosition(SwingConstants.LEFT);
        }

        FontMetrics metrics = button.getFontMetrics(button.getFont());
        int textWidth = metrics.charsWidth(text.toCharArray(), 0, text.length());
        int textStartX = (buttonWidth - textWidth) / 2;
        int insetWidth = iconOnLeft ? button.getInsets().left : button.getInsets().right;
        int textGap = textStartX - insetWidth - icon.getIconWidth();
        button.setIconTextGap(textGap);

        return button;
    }
}