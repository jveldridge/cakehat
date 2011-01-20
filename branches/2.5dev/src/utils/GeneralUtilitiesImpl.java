package utils;

import java.math.BigDecimal;
import java.util.Collection;

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
}