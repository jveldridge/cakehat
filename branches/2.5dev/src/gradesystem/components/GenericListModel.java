package gradesystem.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * A generic data storage used by {@link GenericJList}. By having this class be
 * generic, it allows for accessing the data with type safety and no need to
 * cast.
 *
 * @author jak2
 */
class GenericListModel<T> extends AbstractListModel
{
    private final List<T> _elements;

    public GenericListModel(Iterable<T> data)
    {
        ArrayList<T> dataBuilder = new ArrayList<T>();
        for(T item : data)
        {
            dataBuilder.add(item);
        }
        _elements = Collections.unmodifiableList(dataBuilder);
    }

    public GenericListModel(T[] data)
    {
        this(Arrays.asList(data));
    }

    public GenericListModel()
    {
        this(Collections.EMPTY_LIST);
    }

    @Override
    public int getSize()
    {
        return _elements.size();
    }

    @Override
    public T getElementAt(int i)
    {
        return _elements.get(i);
    }

    public List<T> getElements()
    {
        return _elements;
    }

    public boolean hasElements()
    {
        return !_elements.isEmpty();
    }

    /**
     * Fires off an event that all of the elements of this model have changed.
     * This will result in the UI using this model to repaint the elements.
     */
    public void notifyRefresh()
    {
        fireContentsChanged(this, 0, this.getSize() - 1);
    }
}