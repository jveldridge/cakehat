package gradesystem.components;

import java.util.Arrays;
import java.util.Collections;
import javax.swing.ComboBoxModel;

/**
 * A generic data storage used by {@link GenericJComboBox}. By having this class
 * be generic, it allows for accessing the data with type safety and no need to
 * cast.
 *
 * @author jak2
 */
class GenericComboBoxModel<T> extends GenericListModel<T> implements ComboBoxModel
{
    private T _selectedItem;

    public GenericComboBoxModel(Iterable<T> data)
    {
        super(data);

        _selectedItem = this.hasElements() ? this.getElementAt(0) : null;
    }

    public GenericComboBoxModel(T[] data)
    {
        this(Arrays.asList(data));
    }

    public GenericComboBoxModel()
    {
        this(Collections.EMPTY_LIST);
    }

    @Override
    public void setSelectedItem(Object item)
    {
        //If item is not the same as _selectedItem
        if((_selectedItem != null && !_selectedItem.equals(item)) ||
               (_selectedItem == null && item != null))
        {
            //If null (meaning no selection)
            if(item == null)
            {
                _selectedItem = null;

                //Matches behavior of javax.swing.DefaultComboBoxModel
                fireContentsChanged(this, -1, -1);
            }
            //If the selection is literally contained in the data
            else if(objectEquivalenceContained(item))
            {
                _selectedItem = (T) item;
                
                //Matches behavior of javax.swing.DefaultComboBoxModel
                fireContentsChanged(this, -1, -1);
            }
        }
    }

    public void setGenericSelectedItem(T item)
    {
        //If item is not the same as _selectedItem
        if((_selectedItem != null && !_selectedItem.equals(item)) ||
               (_selectedItem == null && item != null))
        {
            //If null (meaning no selection)
            if(item == null)
            {
                _selectedItem = null;

                //Matches behavior of javax.swing.DefaultComboBoxModel
                fireContentsChanged(this, -1, -1);
            }
            //If the selection is contained in the data
            else if(this.getElements().contains(item))
            {
                _selectedItem = item;

                //Matches behavior of javax.swing.DefaultComboBoxModel
                fireContentsChanged(this, -1, -1);
            }
        }
    }

    private boolean objectEquivalenceContained(Object obj)
    {
        boolean contained = false;

        for(T element : this.getElements())
        {
            if(obj == element)
            {
                contained = true;
            }
        }

        return contained;
    }

    @Override
    public T getSelectedItem()
    {
        return _selectedItem;
    }
}