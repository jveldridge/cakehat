package gradesystem.components;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import javax.swing.ComboBoxModel;

/**
 * A generic data storage used by {@link GenericJComboBox}. By having this class be
 * generic, it allows for accessing the data with type safety and no need to
 * cast.
 *
 * @author jak2
 */
class GenericComboBoxModel<T> extends GenericListModel<T> implements ComboBoxModel
{
//_selectedItem = _convertedData.isEmpty() ? null : _convertedData.get(0);

    /**
     * Only used when this class is used by {@link GenericJComboBox}.
     */
    private ItemRepresentation<T> _selectedItem;



    public GenericComboBoxModel(Iterable<T> data, StringConverter<T> converter)
    {
        super(data, converter);

        _selectedItem = _convertedData.isEmpty() ? null : _convertedData.get(0);
    }

    public GenericComboBoxModel(Iterable<T> data)
    {
        this(data, new DefaultStringConverter<T>());
    }

    public GenericComboBoxModel(T[] data, StringConverter<T> converter)
    {
        this(ImmutableList.of(data), converter);
    }

    public GenericComboBoxModel(T[] data)
    {
        this(data, new DefaultStringConverter<T>());
    }

    public GenericComboBoxModel()
    {
        this(Collections.EMPTY_LIST, new DefaultStringConverter<T>());
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
            //If the selection is contained in the underlying data
            else if(_data.contains(item))
            {
                //Select the corresponding wrapper around this item
                _selectedItem = _dataToConvertedDataMap.get(item);
                
                //Matches behavior of javax.swing.DefaultComboBoxModel
                fireContentsChanged(this, -1, -1);
            }
        }
    }

    @Override
    public void setSelectedItem(Object obj)
    {
        //If obj is not the same as _selectedItem
        if((_selectedItem != null && !_selectedItem.equals(obj)) ||
               (_selectedItem == null && obj != null))
        {
            //If null (meaning no selection)
            if(obj == null)
            {
                _selectedItem = null;

                //Matches behavior of javax.swing.DefaultComboBoxModel
                fireContentsChanged(this, -1, -1);
            }
            //If the selection is of the representation
            else if(obj instanceof ItemRepresentation && _convertedData.contains(obj))
            {
                _selectedItem = (ItemRepresentation<T>) obj;

                //Matches behavior of javax.swing.DefaultComboBoxModel
                fireContentsChanged(this, -1, -1);
            }
            else if(!(obj instanceof ItemRepresentation))
            {
                throw new RuntimeException("Selection should never be set " +
                        "to an object that is not an ItemRepresentation.");
            }
        }
    }

    @Override
    public ItemRepresentation<T> getSelectedItem()
    {
        return _selectedItem;
    }

    public T getSelectedData()
    {
        return _selectedItem == null ? null : _selectedItem.getItem();
    }
}