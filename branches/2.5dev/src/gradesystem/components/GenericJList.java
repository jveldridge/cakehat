package gradesystem.components;

import com.google.common.collect.ImmutableList;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * A parameterized {@link JList}. By using a {@link StringConverter} the
 * value that is displayed in the list may be something other than the value
 * returned by <code>toString()</code>.
 *
 * @author jak2
 *
 * @param <E>
 */
public class GenericJList<E> extends JList
{
    private GenericListModel<E> _model;

    public GenericJList()
    {
        this.clearList();
    }

    public GenericJList(E... values)
    {
        this.setListData(ImmutableList.of(values));
    }

    public GenericJList(Iterable<E> values)
    {
        this.setListData(values);
    }

    public GenericJList(Iterable<E> values, StringConverter<E> converter)
    {
        this.setListData(values, converter);
    }

    /**
     * This method cannot be made type-safe due to Java's implementation of
     * generics: setListData(Object[]) and setListData(E[]) would result in a
     * signature clash due to type erasure.
     * <br/><br/>
     * Thus, this method cannot be supported.
     *
     * @see #setListData(java.lang.Iterable)
     * @see #setListData(java.lang.Iterable, gradesystem.components.GenericJList.StringConverter)
     *
     * @param values
     *
     * @deprecated deprecated due to lack of type-safety
     */
    @Override
    public void setListData(Object[] values)
    {
        throw new UnsupportedOperationException("Not valid for GenericJList. " +
                "Please use setListData(...)");
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
        
        this.setModel(new GenericListModel<E>(values));

        if(maintainSelected)
        {
            this.setSelectedValues(selected);
        }
    }

    /**
     * Sets the values displayed in the list. They will be displayed as defined
     * by the <code>converter</code>. Replaces all existing values. This will
     * result no values being selected.
     *
     * @param values
     * @param converter
     */
    public void setListData(Iterable<E> values, StringConverter<E> converter)
    {
        this.setListData(values, converter, false);
    }

    /**
     * Sets the values displayed in the list. They will be displayed as defined
     * by the <code>converter</code>. Replaces all existing values.
     * <br/><br/>
     * If <code>maintainSelected</code> is <code>true</code> then all currently
     * selected values will be selected for the <code>values</code> passed in.
     * This may result in no selections being made. If
     * <code>maintainSelected</code> is <code>false</code> then no selections
     * will be made.
     *
     * @param values
     * @param converter
     * @param maintainSelected
     */
    public void setListData(Iterable<E> values, StringConverter<E> converter, boolean maintainSelected)
    {
        List<E> selected = null;
        if(maintainSelected)
        {
            selected = this.getGenericSelectedValues();
        }

        this.setModel(new GenericListModel<E>(values, converter));

        if(maintainSelected)
        {
            this.setSelectedValues(selected);
        }
    }

    /**
     * Removes all items from the list.
     */
    public void clearList()
    {
        this.setModel(new GenericListModel<E>());
    }

    /**
     * This method should never be called as doing so interferes with the
     * type-safety this class provides.
     *
     * @param model
     * @deprecated deprecated due to lack of type-safety
     */
    @Override
    public void setModel(ListModel model)
    {
        throw new UnsupportedOperationException("Not valid for GenericJList. " +
                "Please use setListData(...)");
    }

    /**
     * Stores the model and passes it to the superclass.
     * 
     * @param model
     */
    private void setModel(GenericListModel<E> model)
    {
        _model = model;
        super.setModel(model);
    }

    /**
     * Returns the first selected values in the list or <code>null</code> if no
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
            value = _model.getDataAt(index);
        }

        return value;
    }

    /**
     * Returns an immutable list of all the selected values in this JList.
     *
     * @return
     */
    public List<E> getGenericSelectedValues()
    {
        int[] indices = super.getSelectedIndices();

        ImmutableList.Builder<E> builder = ImmutableList.builder();
        for(int index : indices)
        {
            builder.add(_model.getDataAt(index));
        }

        return builder.build();
    }

    /**
     * Returns an immutable list of all values in this JList.
     * 
     * @return
     */
    public List<E> getValues()
    {
        return _model.getData();
    }

    /**
     * The number of values in the list.
     *
     * @return
     */
    public int getValuesCount()
    {
        return _model.getSize();
    }

    /**
     * If there are any items in the list.
     *
     * @return
     */
    public boolean hasValues()
    {
        return _model.hasData();
    }

    /**
     * Visually updates the list. If a {@link StringConverter} is used it will
     * recalculate the Strings to display.
     */
    public void refreshList()
    {
        List<E> selectedValues = this.getGenericSelectedValues();

        //Resets the model
        //This causes the converter to recalculate the displayed strings and
        //for the list to recalculate the visualization
        GenericListModel<E> model =
                new GenericListModel<E>(_model.getData(), _model.getConverter());
        this.setModel(model);

        this.setSelectedValues(selectedValues);
    }

    /**
     * Selects the first value if the list is not empty.
     */
    public void selectFirst()
    {
        if(this.hasValues())
        {
            this.setSelectedIndex(0);
        }
    }

    /**
     * Selects all values if the list is not empty.
     */
    public void selectAll()
    {
        int count = this.getValuesCount();
        if(count != 0)
        {
            this.setSelectionInterval(0, count - 1);
        }
    }

    /**
     * Selects the specified value in the list if the value is in the list.
     *
     * @param value
     */
    public void setSelectedValue(E value)
    {
        int index = _model.getData().indexOf(value);

        if(index != -1)
        {
            this.setSelectedIndex(index);
        }
    }

    /**
     * Selects all specified values. If a value does not exist in the list then
     * it will not be selected, but no problems will arise.
     *
     * @param values
     */
    public void setSelectedValues(List<E> values)
    {
        List<Integer> indices = new ArrayList<Integer>();
        for(E value : values)
        {
            int index = this.getValues().indexOf(value);
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
     * Causes the font used by the this list to be plain. By default the font
     * is bold.
     *
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