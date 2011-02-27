package utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GeneralUtilitiesImpl implements GeneralUtilities
{
    public String doubleToString(double value)
    {
        double roundedVal = round(value, 2);

        return Double.toString(roundedVal);
    }

    //TODO: Make this more efficient! Write the rounding code so that it
    //      doesn't need to create a BigDecimal. This code gets called a lot.
    public double round(double d, int decimalPlace)
    {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);

        return bd.doubleValue();
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

}