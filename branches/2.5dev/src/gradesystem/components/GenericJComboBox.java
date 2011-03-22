package gradesystem.components;

import java.util.Arrays;
import java.util.Collections;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * A parameterized {@link JComboBox}. Does <strong>not</strong> support adding,
 * inserting, or removing specific items.
 * <br/><br/>
 * Unexpected behavior may arise if not all entries are unique.
 * 
 * @author jak2
 */
public class GenericJComboBox<E> extends JComboBox
{
    private GenericComboBoxModel<E> _model;
    private StringConverterCellRenderer _renderer;

    public GenericJComboBox()
    {
        this.setItems(Collections.EMPTY_LIST);
    }

    public GenericJComboBox(Iterable<E> items)
    {
        this.setItems(items);
    }

    public GenericJComboBox(Iterable<E> values, final StringConverter<E> converter)
    {
        this.setItems(values);

        this.setStringConverter(converter);
    }

    public void setStringConverter(StringConverter<E> converter)
    {
        _renderer = new StringConverterCellRenderer(new BasicComboBoxRenderer(), converter);
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
    public E getItemAt(int index)
    {
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

    public static void main(String[] args) throws Throwable
    {
        UIManager.setLookAndFeel(new MetalLookAndFeel());

        JFrame frame = new JFrame();

        GenericJComboBox box = new GenericJComboBox(Arrays.asList("Hello", "World"),
                new StringConverter<String>() {

            public String convertToString(String item) {
                return item + " !";
            }

        });

        frame.add(box);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}