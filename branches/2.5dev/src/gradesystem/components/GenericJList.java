package gradesystem.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import javax.swing.JList;

/**
 * A parameterized JList.
 *
 * @author jak2
 *
 * @param <E>
 */
public class GenericJList<E> extends JList
{

    public interface StringConverter<E> {
        public String convertToString(E item);
    }

    private static class DefaultStringConverter<E> implements StringConverter<E> {

        @Override
        public String convertToString(E item) {
            return item + "";
        }

    }

    private class ItemAndDisplayString<E> {

        private E _item;
        private String _displayString;

        public ItemAndDisplayString(E item, String displayString) {
            _item = item;
            _displayString = displayString;
        }

        public E getItem() {
            return _item;
        }

        @Override
        public String toString() {
            return _displayString;
        }

    }

    public GenericJList() { }

    public GenericJList(E[] items) {
        this(Arrays.asList(items), new DefaultStringConverter<E>());
    }

    public GenericJList(Collection<E> items) {
        this(items, new DefaultStringConverter<E>());
    }

    public GenericJList(Collection<E> items, StringConverter<E> converter) {
        Collection<ItemAndDisplayString> toStore = new ArrayList<ItemAndDisplayString>(items.size());
        for (E item : items) {
            toStore.add(new ItemAndDisplayString(item, converter.convertToString(item)));
        }

        super.setListData(toStore.toArray());
    }

    public void setListData(Collection<E> items) {
        this.setListData(items, new DefaultStringConverter<E>());
    }

    public void setListData(Collection<E> items, StringConverter<E> converter) {
        Collection<ItemAndDisplayString> toStore = new ArrayList<ItemAndDisplayString>(items.size());
        for (E item : items) {
            toStore.add(new ItemAndDisplayString(item, converter.convertToString(item)));
        }

        super.setListData(toStore.toArray());
    }

    public void deleteAllItems() {
        super.setListData(new Object[0]);
    }

    @Override
    public E getSelectedValue() {
        ItemAndDisplayString<E> selected = (ItemAndDisplayString<E>) super.getSelectedValue();
        return (selected == null ? null : selected.getItem());
    }

    /**
     * This method cannot be made type-safe, since, due to Java's implementation of
     * generics, setListData(Object[]) and setListData(E[]) result in a name clash without
     * one overriding the other. Thus, it is not supported.  Use setListData(Collection<E>)
     * instead.
     *
     * @param items
     */
    @Override
    public void setListData(Object[] items) {
        throw new UnsupportedOperationException("Not valid for GenericJList.  Please use setListData(Collection<E>)");
    }

    public Collection<E> getGenericSelectedValues() {
        Collection<E> values = new Vector<E>();
        for(Object obj : super.getSelectedValues())
        {
            values.add(((ItemAndDisplayString<E>) obj).getItem());
        }

        return values;
    }

    public Collection<E> getItems()
    {
        Vector<E> items = new Vector<E>();
        for (int i = 0; i < this.getModel().getSize(); i++)
        {
            items.add(((ItemAndDisplayString<E>) this.getModel().getElementAt(i)).getItem());
        }

        return items;
    }

    public int getItemCount()
    {
        return (super.getModel().getSize());
    }

    public boolean hasItems()
    {
        return (this.getItemCount() != 0);
    }

    /**
     * Selects the first entry if it exists.
     */
    public void selectFirst()
    {
        if(this.hasItems())
        {
            this.setSelectedIndex(0);
        }
    }

    public void selectAll()
    {
        int itemCount = this.getItemCount();
        if(itemCount != 0)
        {
            this.setSelectionInterval(0, itemCount - 1);
        }
    }

}