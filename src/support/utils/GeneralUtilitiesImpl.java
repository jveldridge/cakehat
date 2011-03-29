package support.utils;

import java.awt.FontMetrics;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class GeneralUtilitiesImpl implements GeneralUtilities
{
    public String doubleToString(double value)
    {
        double roundedVal = round(value, 2);

        return Double.toString(roundedVal);
    }

    public double round(double d, int decimalPlace)
    {
        double roundedValue;

        if(Double.isNaN(d))
        {
            roundedValue = Double.NaN;
        }
        else
        {
            BigDecimal bd = new BigDecimal(Double.toString(d));
            bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
            roundedValue = bd.doubleValue();
        }

        return roundedValue;
    }

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

    public <E extends Throwable> E findInStack(Throwable throwable, Class<E> throwableClass)
    {
        if(throwable == null)
        {
            return null;
        }
        else if(throwableClass.isInstance(throwable))
        {
            return (E) throwable;
        }
        else
        {
            return findInStack(throwable.getCause(), throwableClass);
        }
    }

    public <E> boolean containSameElements(Collection<E> c1, Collection<E> c2) {
        Collection<E> diff1 = new ArrayList<E>(c1);
        diff1.removeAll(c2);

        Collection<E> diff2 = new ArrayList<E>(c2);
        diff2.removeAll(c1);

        return diff1.isEmpty() && diff2.isEmpty();
    }

    /**
     * The code inside of the runnable is run with the error stream redirected
     * such that all calls on {@link System#err} are silenced. The error stream
     * is restored after <code>toRun</code> is run.
     *
     * @param toRun
     */
    public void runWithSilencedError(Runnable toRun)
    {
        PrintStream originalErr = System.err;

        //In case a runtime exception is thrown, restore the error stream in finally
        try
        {
            PrintStream silentErr = new PrintStream(new OutputStream()
            {
                @Override
                public void write(int i) throws IOException { }
            });
            System.setErr(silentErr);

            toRun.run();
        }
        finally
        {
            System.setErr(originalErr);
        }
    }

    public JButton createTextCenteredButton(String text, Icon icon,
            int buttonWidth, boolean iconOnLeft)
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