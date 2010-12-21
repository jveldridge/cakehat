package utils;

import java.util.Collection;

/**
 * Utility methods that do not fit well into any other utility class. If
 * enough methods in this class are sufficiently related they should be split
 * into a seperate utility class.
 */
public interface GeneralUtilities
{
    /**
     * Takes a double and returns it as a String rounded to 2
     * decimal places.
     *
     * @param value
     * @return the double as a String rounded to 2 decimal places
     */
    public String doubleToString(double value);

    /**
     * Rounds a double to the number of decimal places specified.
     *
     * @param d the double to round
     * @param decimalPlace the number of decimal places to round to
     * @return the rounded double
     */
    public double round(double d, int decimalPlace);

    /*
     * Checks to see if any of col2 is in col1. If one or more are in both then it will return true.
     *
     * @param col1 collection to test membership in
     * @param col2 collection to test who's elements we are testing
     * @return true if there is overlap
     */
    public <T> boolean containsAny(Collection<T> col1, Collection<T> col2);
}