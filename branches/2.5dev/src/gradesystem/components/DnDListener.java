package gradesystem.components;

import java.util.List;
import java.util.Map;

/**
 * A listener which is informed when values are added, removed, or
 * reordered from dragging.
 *
 * @param <T>
 *
 * @author jak2
 */
public interface DnDListener<T>
{
    /**
     * A map of the indices to values for the values added to the list.
     *
     * @param added
     */
    public void valuesAdded(Map<Integer, T> added);

    /**
     * The values removed from the list.
     *
     * @param removed
     */
    public void valuesRemoved(List<T> removed);

    /**
     * A map of the indices to values for the values reordered in this list.
     * The indices are the current indices these values are now located at.
     * <br/><br/>
     * This method is called when the drag and drop occurs entirely within the
     * same list, and so the values in the list do not change, but the order of
     * the values do.
     *
     * @param values the values that were reordered
     */
    public void valuesReordered(Map<Integer, T> values);
}