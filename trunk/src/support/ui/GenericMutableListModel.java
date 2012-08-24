package support.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * A generic list model that allows for its contents to be mutated.
 *
 * @author jak2
 */
public class GenericMutableListModel<T> extends GenericAbstractListModel<T>
{
    private final List<T> _elements;

    public GenericMutableListModel(Iterable<T> data)
    {
        _elements = new ArrayList<T>();
        for(T item : data)
        {
            _elements.add(item);
        }
    }

    @Override
    public List<T> getElements()
    {
        //Return the elements as immutable, because changes that occur to the data need to have events fired off
        return Collections.unmodifiableList(_elements);
    }

    /**
     * Inserts the {@code element} at {@code index}.
     *
     * @param element
     * @param index
     *
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     */
    public void insertElementAt(T element, int index)
    {
        _elements.add(index, element);
        fireIntervalAdded(this, index, index);
    }

    /**
     * Inserts the {@code elements} starting at {@code startIndex}.
     *
     * @param elements
     * @param startIndex
     *
     * @throws ArrayIndexOutOfBoundsException if the startIndex is out of range
     */
    public void insertElementsAt(Iterable<T> elements, int startIndex)
    {
        int currIndex = startIndex;
        for(T element : elements)
        {
            _elements.add(currIndex, element);
            currIndex++;
        }

        fireIntervalAdded(this, currIndex, currIndex - 1);
    }

    /**
     * Inserts the {@code element} at the end of the list model.
     *
     * @param element
     */
    public void addElement(T element)
    {
        this.insertElementAt(element, this.getSize());
    }

    /**
     * Removes the element at the specified {@code index}.
     *
     * @param index
     *
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     */
    public void removeElementAt(int index)
    {
        _elements.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    /**
     * Removes all of the elements specified by {@code indices}. Properly handles elements shifting position as elements
     * at the specified indices are removed.
     *
     * @param indices <strong>must</strong> be unique
     *
     * @throws ArrayIndexOutOfBoundsException if any of the indices are out of range
     */
    public void removeElementsAt(int[] indices)
    {
        //Since arrays are passed by reference, copy the array to avoid sorting caller's array
        indices = Arrays.copyOf(indices, indices.length);

        //Sort the array from lowest to highest
        Arrays.sort(indices);
        
        //Iterate over the indices from highest to lowest so that removing them in that order does not cause the indices
        //to remove to shift
        for(int i = indices.length - 1; i >= 0; i--)
        {
            this.removeElementAt(indices[i]);
        }
    }

    /**
     * Removes the first occurrence of {@code element} if it exists in the model.
     *
     * @param element
     */
    public void removeElement(T element)
    {
        int index = _elements.indexOf(element);
        if(index != -1)
        {
            this.removeElementAt(index);
        }
    }

    /**
     * Removes all elements from this model.
     */
    public void removeAll()
    {
        int size = this.getSize();
        _elements.clear();
        fireIntervalRemoved(this, 0, size - 1);
    }

    /**
     * Sorts all elements in the list according to the {@code comparator}.
     *
     * @param comparator
     */
    public void sortElements(Comparator<? super T> comparator)
    {
        Collections.sort(_elements, comparator);
        this.notifyRefresh();
    }
}