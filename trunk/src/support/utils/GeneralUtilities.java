package support.utils;

import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Utility methods that do not fit well into any other utility class. If enough methods in this class are sufficiently
 * related they should be split into a separate utility class.
 */
public interface GeneralUtilities
{
    /**
     * Checks to see if any of col2 is in col1. If one or more are in both then it will return true.
     *
     * @param col1 collection to test membership in
     * @param col2 collection to test who's elements we are testing
     * @return {@code true} if there is overlap
     */
    public <T> boolean containsAny(Collection<T> col1, Collection<T> col2);

    /**
     * Creates a button with an icon that has centered text.
     * <br/><br/>
     * This is done by calculating the necessary gap space between the icon and the text. There is no built-in way to
     * center the text in a button (including using HTML) when an icon is also present. When an icon is present, all
     * text placement is relative to the icon.
     *
     * @param text
     * @param icon
     * @param buttonWidth the width the button will be when displayed
     * @param iconOnLeft if {@code true} the icon will be displayed on the left, if {@code false} it will be displayed
     * on the right
     * @return
     */
    public JButton createTextCenteredButton(String text, Icon icon, int buttonWidth, boolean iconOnLeft);
}