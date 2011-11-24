package support.ui;

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
 public class GenericComboBoxModel<T> extends GenericImmutableListModel<T> implements ComboBoxModel
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
            if(item == null)
            {
                _selectedItem = null;

                //Matches behavior of javax.swing.DefaultComboBoxModel
                fireContentsChanged(this, -1, -1);
            }
            else
            {
                //If item is in the model, then it will be returned in a
                //typesafe manner, if it is not then null will be returned
                //and no selection should be made
                T itemInModel = findMatchingObject(item);
                if(itemInModel != null)
                {
                    _selectedItem = itemInModel;

                    //Matches behavior of javax.swing.DefaultComboBoxModel
                    fireContentsChanged(this, -1, -1);
                }
            }
        }
    }

    public void setGenericSelectedItem(T item)
    {
        //If item is not the same as _selectedItem
        if((_selectedItem != null && !_selectedItem.equals(item)) ||
               (_selectedItem == null && item != null))
        {
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

    /**
     * Finds the object stored in this model that is the <strong>exact</code>
     * same instance of <code>obj</code>. If none is found, null is returned.
     *
     * @param obj
     * @return
     */
    private T findMatchingObject(Object obj)
    {
        T match = null;

        for(T elem : this.getElements())
        {
            if(obj == elem)
            {
                match = elem;
                break;
            }
        }

        return match;
    }

    @Override
    public T getSelectedItem()
    {
        return _selectedItem;
    }
}