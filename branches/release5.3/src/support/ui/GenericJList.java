package support.ui;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * A parameterized {@link JList}. By using a {@link StringConverter} the value that is displayed in the list may be
 * something other than the value returned by {@link Object#toString()}.
 *
 * @author jak2
 *
 * @param <E>
 */
public class GenericJList<E> extends JList implements DescriptionProviderCellRenderer.ItemInfoProvider<E>
{
    private GenericListModel<E> _model;
    private DescriptionProviderCellRenderer _renderer;

    public GenericJList(E... values)
    {
        this(Arrays.asList(values));
    }

    public GenericJList(Iterable<E> values, DescriptionProvider<E> converter)
    {
        this(values);

        this.setDescriptionProvider(converter);
    }

    public GenericJList(Iterable<E> values)
    {
        this.setListData(values);
    }
    
    public GenericJList(DescriptionProvider<E> converter) 
    {
        this(Collections.<E>emptyList(), converter);
    }

    /**
     * Sets the DescriptionProvider used to render all values in the list. This will cause the list to be refreshed,
     * re-rendering all of its cells.
     *
     * @param converter
     */
    public void setDescriptionProvider(DescriptionProvider<E> descriptionProvider)
    {
        _renderer = new DescriptionProviderCellRenderer<E>(new DefaultListCellRenderer(), this, descriptionProvider);
        this.setCellRenderer(_renderer);
        _model.notifyRefresh();
    }

    /**
     * This method cannot be made type-safe due to Java's implementation of
     * generics: setListData(Object[]) and setListData(E[]) would result in a
     * signature clash due to type erasure.
     *
     * @see #setListData(java.lang.Iterable)
     * @see #setListData(java.lang.Iterable, boolean)
     *
     * @param values
     *
     * @deprecated deprecated due to lack of type-safety
     */
    @Override
    public void setListData(Object[] values)
    {
        throw new UnsupportedOperationException("Not valid for GenericJList. " +
                "Please use setListData(Iterable<E>)");
    }

    /**
     * This method cannot be made type-safe due to Java's implementation of
     * generics: setListData(Vector<?>) and setListData(Vector<E>) would result
     * in a signature clash due to type erasure.
     *
     * @see #setListData(java.lang.Iterable)
     * @see #setListData(java.lang.Iterable, boolean)
     *
     * @param values
     *
     * @deprecated deprecated due to lack of type-safety
     */
    @Override
    public void setListData(Vector<?> values)
    {
        throw new UnsupportedOperationException("Not valid for GenericJList. " +
                "Please use setListData(Iterable<E>)");
    }

    /**
     * Sets the values displayed in the list. Replaces all existing values.
     * This will result in no values being selected.
     *
     * @param values
     */
    public void setListData(Iterable<E> values)
    {
        this.setListData(values, false);
    }

    /**
     * Sets the values displayed in the list. Replaces all existing values.
     * <br/><br/>
     * If <code>maintainSelected</code> is <code>true</code> then all currently
     * selected values will be selected for the <code>values</code> passed in.
     * This may result in no selections being made. If
     * <code>maintainSelected</code> is <code>false</code> then no selections
     * will be made.
     * <br/><br/>
     * Unexpected behavior may arise in maintaining the selection if not all
     * values are unique.
     *
     * @param values
     * @param maintainSelected
     */
    public void setListData(Iterable<E> values, boolean maintainSelected)
    {
        List<E> selected = null;
        if(maintainSelected)
        {
            selected = this.getGenericSelectedValues();
        }

        if (_renderer != null) {
            _renderer.clearCache();
        }
        this.setModel(buildModel(values));

        if(maintainSelected)
        {
            this.setSelectedValues(selected);
        }
    }

    /**
     * Removes all items from the list.
     */
    public void clearListData()
    {
        this.setListData(Collections.<E>emptyList());
    }

    /**
     * This method should never be called as doing so interferes with the
     * type-safety this class provides.
     *
     * @param model
     * @deprecated due to lack of type-safety
     */
    @Override
    public void setModel(ListModel model)
    {
        throw new UnsupportedOperationException("Not valid for GenericJList. " +
                "Please use setListData(...)");
    }

    /**
     * Sets the model that represents the values of the list and notifies
     * property change listeners. Situations should not arise in which direct
     * users of this class will require a different model. Subclasses may wish
     * to use a different model, in that case they should override
     * {@link #buildModel(Iterable)}.
     *
     * @param model
     */
    private void setModel(GenericListModel<E> model)
    {
        _model = model;
        super.setModel(model);
    }
    
    @Override
    public GenericListModel<E> getModel()
    {
        return _model;
    }

    /**
     * Override this method in subclasses to use a different list model. By
     * default an immutable list model will be used.
     * 
     * @param values
     * @return
     */
    protected GenericListModel<E> buildModel(Iterable<E> values)
    {
        return new GenericImmutableListModel<E>(values);
    }
    
    /**
     * This method must be public to match a required interface; however, it is
     * not intended for external use.
     *
     * @param i
     * @return
     */
    public E getElementDisplayedAt(int i)
    {
        return _model.getElementAt(i);
    }

    /**
     * Returns an immutable list of all values in this list.
     * 
     * @return
     */
    public List<E> getListData()
    {
        return _model.getElements();
    }

    /**
     * If there are any values in the list.
     *
     * @return
     */
    public boolean hasListData()
    {
        return _model.hasElements();
    }

    /**
     * Visually updates the list. If a {@link DescriptionProvider} is used it will recalculate the {@code String}s
     * displayed.
     */
    public void refreshList()
    {
        if(_renderer != null)
        {
            _renderer.clearCache();
        }

        _model.notifyRefresh();
    }

    /**
     * Returns whether the list has a selected value.
     * 
     * @return
     */
    public boolean hasSelectedValue()
    {
        return (super.getSelectedIndex() != -1);
    }
    
    /**
     * Returns the first selected value in the list or <code>null</code> if no
     * items are selected.
     *
     * @return
     */
    @Override
    public E getSelectedValue()
    {
        E value = null;

        int index = super.getSelectedIndex();
        if(index != -1)
        {
            value = _model.getElementAt(index);
        }

        return value;
    }

    /**
     * This method cannot properly be made typesafe because Java does not
     * support generic array creation. (Casting an Object array to a generic
     * array is possible, but problematic because the array then cannot ensure
     * that all values of the array are of the generic type.)
     *
     * @see #getGenericSelectedValues()
     *
     * @return
     * @deprecated
     */
    @Deprecated
    @Override
    public Object[] getSelectedValues()
    {
        throw new UnsupportedOperationException("Not valid for GenericJList. " +
                "Please use getGenericSelectedValues()");
    }

    /**
     * Returns an immutable list of all the selected values in this JList.
     *
     * @return
     */
    public List<E> getGenericSelectedValues()
    {
        int[] indices = super.getSelectedIndices();

        ArrayList<E> list = new ArrayList<E>();
        for(int index : indices)
        {
            list.add(_model.getElementAt(index));
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * Returns an immutable map of all the selected indices mapped to the value
     * at that index.
     *
     * @return
     */
    public Map<Integer, E> getGenericSelectedValuesMap()
    {
        Map<Integer, E> map = new HashMap<Integer, E>();

        int[] indices = super.getSelectedIndices();
        for(int index : indices)
        {
            map.put(index, _model.getElementAt(index));
        }

        return Collections.unmodifiableMap(map);
    }
    
    /**
     * Returns the number of values selected in the list.
     * 
     * @return 
     */
    public int getSelectionSize()
    {
        return super.getSelectedIndices().length;
    }

    /**
     * This method cannot be made typesafe.
     *
     * @param anObject
     * @param shouldScroll
     *
     * @deprecated due to lack of type safety
     */
    @Override
    public void setSelectedValue(Object anObject, boolean shouldScroll)
    {
        throw new UnsupportedOperationException("Not valid for GenericJList. " +
                "Please use setSelectedValue(...)");
    }

    /**
     * Selects the specified value in the list if the value is in the list.
     *
     * @param value
     */
    public void setSelectedValue(E value)
    {
        int index = _model.getElements().indexOf(value);

        if(index != -1)
        {
            this.setSelectedIndex(index);
        }
    }

    /**
     * Selects the first occurence of each specified value. If a value does not
     * exist in the list then it will not be selected, but no problems will
     * arise.
     *
     * @param values
     */
    public void setSelectedValues(List<E> values)
    {
        List<Integer> indices = new ArrayList<Integer>();
        for(E value : values)
        {
            int index = this.getListData().indexOf(value);
            if(index != -1)
            {
                indices.add(index);
            }
        }
        int[] convertedIndices = new int[indices.size()];
        for(int i = 0; i < indices.size(); i++)
        {
            convertedIndices[i] = indices.get(i);
        }

        if(convertedIndices.length != 0)
        {
            this.setSelectedIndices(convertedIndices);
        }
    }
    
    /**
     * Selects the first value if the list has values.
     */
    public void selectFirst()
    {
        if(this.hasListData())
        {
            this.setSelectedIndex(0);
        }
    }

    /**
     * Selects all values if the list has values.
     */
    public void selectAll()
    {
        if(this.hasListData())
        {
            this.setSelectionInterval(0, this.getListData().size() - 1);
        }
    }

    /**
     * Causes the font used by the this list to be plain. By default the font
     * is bold.
     */
    public void usePlainFont()
    {
        this.setFont(this.getFont().deriveFont(Font.PLAIN));
    }

    /**
     * Causes the font used by this list to be bold. This is the default.
     */
    public void useBoldFont()
    {
        this.setFont(this.getFont().deriveFont(Font.BOLD));
    }
}