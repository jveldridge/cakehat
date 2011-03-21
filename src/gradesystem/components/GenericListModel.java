package gradesystem.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;

/**
 * A generic data storage used by {@link GenericJList}. By having this class be
 * generic, it allows for accessing the data with type safety and no need to
 * cast.
 *
 * @jak2
 */
class GenericListModel<T> extends AbstractListModel
{
    protected final List<T> _data;
    private final StringConverter<T> _converter;
    protected final List<ItemRepresentation<T>> _convertedData;
    protected final Map<T, ItemRepresentation<T>> _dataToConvertedDataMap;

    public GenericListModel(Iterable<T> data, StringConverter<T> converter)
    {
        _data = ImmutableList.copyOf(data);
        _converter = converter;

        ImmutableList.Builder<ItemRepresentation<T>> listBuilder = ImmutableList.builder();
        ImmutableMap.Builder<T, ItemRepresentation<T>> mapBuilder = ImmutableMap.builder();
        for (T item : data)
        {
            ItemRepresentation<T> representation = new ItemRepresentation(item, converter);
            mapBuilder.put(item, representation);
            listBuilder.add(representation);
        }
        _convertedData = listBuilder.build();
        _dataToConvertedDataMap = mapBuilder.build();
    }

    public GenericListModel(Iterable<T> data)
    {
        this(data, new DefaultStringConverter<T>());
    }

    public GenericListModel(T[] data, StringConverter<T> converter)
    {
        this(ImmutableList.of(data), converter);
    }

    public GenericListModel(T[] data)
    {
        this(data, new DefaultStringConverter<T>());
    }

    public GenericListModel()
    {
        this(Collections.EMPTY_LIST, new DefaultStringConverter<T>());
    }

    @Override
    public int getSize()
    {
        return _convertedData.size();
    }

    @Override
    public ItemRepresentation<T> getElementAt(int i)
    {
        return _convertedData.get(i);
    }

    public StringConverter<T> getConverter()
    {
        return _converter;
    }

    public T getDataAt(int i)
    {
        return _data.get(i);
    }

    public List<T> getData()
    {
        return _data;
    }

    public boolean hasData()
    {
        return !_data.isEmpty();
    }

    protected static class ItemRepresentation<E>
    {
        private final E _item;
        private final String _representation;

        public ItemRepresentation(E item, StringConverter<E> converter)
        {
            _item = item;
            _representation = converter.convertToString(_item);
        }

        public E getItem()
        {
            return _item;
        }

        @Override
        public String toString()
        {
            return _representation;
        }
    }

    protected static class DefaultStringConverter<E> implements StringConverter<E>
    {
        @Override
        public String convertToString(E item)
        {
            return item + "";
        }
    }
}
