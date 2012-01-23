package support.ui;

import java.util.Arrays;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * A parameterized {@link JComboBox}. Does <strong>not</strong> support adding, inserting, or removing specific items.
 * <br/><br/>
 * Unlike the standard {@code JComboBox}, this class supports having an item that is {@code null}. However, when
 * {@code null} is stored in this combo box, it is not possible to have no item in the combo box selected.
 * <br/><br/>
 * Unexpected behavior may arise if not all entries are unique.
 * 
 * @author jak2
 */
public class GenericJComboBox<E> extends JComboBox implements DescriptionProviderCellRenderer.ItemInfoProvider<E>
{
    private GenericComboBoxModel<E> _model;
    private DescriptionProviderCellRenderer _renderer;

    public GenericJComboBox(E... values)
    {
        this(Arrays.asList(values));
    }

    public GenericJComboBox(Iterable<E> values, final DescriptionProvider<E> descriptionProvider)
    {
        this(values);

        this.setDescriptionProvider(descriptionProvider);
    }
    
    public GenericJComboBox(Iterable<E> items)
    {
        this.setItems(items);
    }

    public void setDescriptionProvider(DescriptionProvider<E> descriptionProvider)
    {
        _renderer = new DescriptionProviderCellRenderer(new BasicComboBoxRenderer(), this, descriptionProvider);
        this.setRenderer(_renderer);
        _model.notifyRefresh();
    }

    /**
     * For internal use <strong>only</strong>.
     * <br/><br/>
     * This method must be public in order to interact properly with
     * {@link JComboBox}; however, it should not be called from other classes.
     *
     * @param obj
     *
     * @deprecated deprecated due to lack of type safety
     * @see #setGenericSelectedItem(java.lang.Object)
     */
    @Override
    public void setSelectedItem(Object obj)
    {
        _model.setSelectedItem(obj);
    }

    /**
     * Sets the selected item in a typesafe manner.
     *
     * @param item
     */
    public void setGenericSelectedItem(E item)
    {
        _model.setGenericSelectedItem(item);
    }

    @Override
    public E getSelectedItem()
    {
        return _model.getSelectedItem();
    }
    
    @Override
    public int getSelectedIndex()
    {
        //This allows for a null item in the list to be considered selected
        return _model.getElements().indexOf(_model.getSelectedItem());
    }

    @Override
    public E getItemAt(int index)
    {
        if (index < 0 || index >= _model.getSize()) {
            return null;
        }
        return _model.getElementAt(index);
    }

    /**
     * Replaces all existing data with <code>items</code>.
     *
     * @param items
     */
    public void setItems(Iterable<E> items)
    {
        this.setModel(new GenericComboBoxModel<E>(items));
    }

    /**
     * Returns an immutable list of all of items in this combo box.
     * 
     * @return
     */
    public List<E> getItems()
    {
        return _model.getElements();
    }

    /**
     * This method cannot ensure type safety.
     * <br/><br/>
     * The no argument super constructor will call this method with an empty
     * <code>model</code>, this behavior will be allowed, but the model provided
     * will not be used.
     *
     * @param model
     * @deprecated
     */
    @Override
    @Deprecated
    public void setModel(ComboBoxModel model)
    {
        //If an empty model, swap it out
        if(model.getSize() == 0)
        {
            this.setModel(new GenericComboBoxModel<E>());
        }
        //Due to type-erasure, it is not possible to verify that the elements
        //of the model are of type E, so do not allow this behavior
        else
        {
            throw new UnsupportedOperationException("Not valid for " +
                    "GenericJComboBox. Please use setItems(...)");
        }
    }

    /**
     * Sets the model in a typesafe manner.
     *
     * @param model
     */
    private void setModel(GenericComboBoxModel<E> model)
    {
        _model = model;
        super.setModel(model);
    }

    @Override
    public GenericComboBoxModel<E> getModel()
    {
        return _model;
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
        E elem;

        //For a JComboBox -1 indicates the popup is closed and it is rendering
        //the selected item
        if(i == -1)
        {
            elem = getSelectedItem();
        }
        else
        {
            elem = _model.getElementAt(i);
        }

        return elem;
    }

    @Override
    public void removeAllItems()
    {
        this.setModel(new GenericComboBoxModel<E>());
    }

    /**
     * <strong>Not supported</strong>
     *
     * @param anObject
     */
    @Override
    @Deprecated
    public void addItem(Object anObject)
    {
        throw new UnsupportedOperationException("Mutation not supported");
    }

    /**
     * <strong>Not supported</strong>
     *
     * @param anObject
     * @param index
     */
    @Override
    @Deprecated
    public void insertItemAt(Object anObject, int index)
    {
        throw new UnsupportedOperationException("Mutation not supported");
    }

    /**
     * <strong>Not supported</strong>
     *
     * @param anObject
     */
    @Override
    @Deprecated
    public void removeItem(Object anObject)
    {
        throw new UnsupportedOperationException("Mutation not supported");
    }

    /**
     * <strong>Not supported</strong>
     *
     * @param anIndex
     */
    @Override
    @Deprecated
    public void removeItemAt(int anIndex)
    {
        throw new UnsupportedOperationException("Mutation not supported");
    }
}