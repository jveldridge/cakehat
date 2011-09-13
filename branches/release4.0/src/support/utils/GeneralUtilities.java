package support.utils;

import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JButton;

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

    /**
     * Returns the first instance of a class by the type of
     * <code>throwableClass</code> that exists in the causal heirarchy. This
     * heirarchy starts at <code>throwable</code> and continues until
     * {@link Throwable#getCause() } returns <code>null</code>. If no
     * {@link Throwable} is found of type <code>throwableClass</code> then
     * <code>null</code> is returned.
     *
     * @param <E>
     * @param throwable
     * @param throwableClass
     * @return the first matching {@link Throwable} or <code>null</code> if no
     * match is found
     */
    public <E extends Throwable> E findInStack(Throwable throwable, Class<E> throwableClass);

    /**
     * Returns whether or not the two given Collections contain the same elements.
     * The Collections are considered to contain the same elements if removing all
     * elements of one Collection from the other results in an empty Collection.
     *
     * @param <E>
     * @param c1
     * @param c2
     * @return true if the Collections contain the same elements, false if not
     */
    public <E> boolean  containSameElements(Collection<E> c1, Collection<E> c2);

    /**
     * The code inside of the runnable is run with the error stream redirected
     * such that all calls on {@link System#err} are silenced. The error stream
     * is restored after <code>toRun</code> is run.
     *
     * @param toRun
     */
    public void runWithSilencedError(Runnable toRun);

    /**
     * Creates a button with an icon that has centered text.
     * <br/><br/>
     * This is done by calculating the necessary gap space between the icon and
     * the text. There is no built-in way to center the text in a button
     * (including using HTML) when an icon is also present. When an icon is
     * present, all text placement is relative to the icon.
     *
     * @param text
     * @param icon
     * @param buttonWidth the width the button will be when displayed
     * @param iconOnLeft if <code>true</code> the icon will be displayed on the
     * left, if <code>false</code> it will be displayed on the right
     * @return
     */
    public JButton createTextCenteredButton(String text, Icon icon,
            int buttonWidth, boolean iconOnLeft);
}