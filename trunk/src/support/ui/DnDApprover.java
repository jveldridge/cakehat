package support.ui;

import java.util.Map;

/**
 * An approver determines whether or not values can be added, removed, or
 * reordered from a drop. If an approver returns <code>false</code> the
 * transfer will not occur. All of the methods are called after the drop,
 * not continously during the drag.
 *
 * @param <T>
 *
 * @author jak2
 */
public interface DnDApprover<T>
{
    /**
     * Whether these values can be added to the list at the specified indices.
     *
     * @param toAdd a map from index to be inserted at to the value that will
     * be inserted at the index
     * @return
     */
    public boolean canAddValues(Map<Integer, T> toAdd);

    /**
     * Whether these values, at the specified indices, can be removed from the
     * list.
     *
     * @param toRemove a map from index to be removed from to the value that
     * will be removed from that index
     * @return
     */
    public boolean canRemoveValues(Map<Integer, T> toRemove);

    /**
     * Whether these values, currently at the specified indices, can be
     * reordered within the list.
     *
     * @param toReorder a map from index to value for the values that will be
     * reordered
     * @return
     */
    public boolean canReorderValues(Map<Integer, T> toReorder);
}