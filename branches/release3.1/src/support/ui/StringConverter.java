package support.ui;

/**
 * Implement this interface to allow for displaying a String other than
 * a value's <code>toString()</code> return.
 *
 * @param <E>
 *
 * @see GenericJComboBox
 * @see GenericJList
 */
public interface StringConverter<E>
{
    public String convertToString(E item);
}