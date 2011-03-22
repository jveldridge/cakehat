package gradesystem.components;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
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
    private StringConverterCellRenderer _renderer;

    public GenericJList()
    {
        this.clearList();
    }

    public GenericJList(E... values)
    {
        this.setListData(Arrays.asList(values));
    }

    public GenericJList(Iterable<E> values)
    {
        this.setListData(values);
    }

    public GenericJList(Iterable<E> values, StringConverter<E> converter)
    {
        this.setListData(values);

        this.setStringConverter(converter);
    }

    /**
     * Sets the StringConverter used to render all values in the list. This
     * will cause the list to be refreshed, re-rendering all of its cells.
     *
     * @param converter
     */
    public void setStringConverter(StringConverter<E> converter)
    {
        _renderer = new StringConverterCellRenderer(new DefaultListCellRenderer(), converter);
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
     * @see #setListData(java.lang.Iterable)
     * @see #setListData(java.lang.Iterable, boolean) 
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
            value = _model.getElementAt(index);
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

        ArrayList<E> list = new ArrayList<E>();
        for(int index : indices)
        {
            list.add(_model.getElementAt(index));
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * Returns an immutable list of all values in this JList.
     * 
     * @return
     */
    public List<E> getValues()
    {
        return _model.getElements();
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
        return _model.hasElements();
    }

    /**
     * Visually updates the list. If a {@link StringConverter} is used it will
     * recalculate the Strings to display.
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
        int index = _model.getElements().indexOf(value);

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