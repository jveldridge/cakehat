package gradesystem.components;

import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 * A parameterized {@link JComboBox}. Does <strong>not</strong> support adding,
 * inserting, or removing specific items.
 * <br/><br/>
 * All entries <strong>must</strong> be unique.
 * 
 * @author jeldridg
 * @author jak2
 */
public class GenericJComboBox<E> extends JComboBox
{
    private GenericComboBoxModel<E> _model;

    public GenericJComboBox()
    {
        this.setItems(new ArrayList<E>());
    }

    public GenericJComboBox(Iterable<E> items)
    {
        this.setItems(items);
    }

    public GenericJComboBox(Iterable<E> values, StringConverter<E> converter)
    {
        this.setItems(values, converter);
    }

    /**
     * Do not use the method, it cannot ensure type safety.
     *
     * @param obj
     * @see #setGenericSelectedItem(java.lang.Object)
     */
    @Override
    @Deprecated
    public void setSelectedItem(Object obj)
    {
        //This method will still internally be called by JComboBox which
        //will either be passing in null or an instance of the internal
        //wrapper object that the GenericComboBoxModel uses
        _model.setSelectedItem(obj);
    }

    /**
     * Sets the selected item in a type safe manner.
     *
     * @param item may be <code>null</code>
     */
    public void setGenericSelectedItem(E item)
    {
        _model.setGenericSelectedItem(item);
    }

    @Override
    public E getSelectedItem()
    {
        return _model.getSelectedData();
    }

    @Override
    public E getItemAt(int index)
    {
        return _model.getDataAt(index);
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
     * Replaces all existing data with <code>items</code>. They will be
     * displayed as defined by the <code>converter</code>.
     *
     * @param items
     * @param converter
     */
    public void setItems(Iterable<E> items, StringConverter<E> converter)
    {
        this.setModel(new GenericComboBoxModel<E>(items, converter));
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
     * @deprecated
     */
    @Override
    @Deprecated
    public void insertItemAt(Object anObject,
                         int index)
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