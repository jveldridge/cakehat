package gradesystem.components;

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
    public GenericJList(Collection<E> items)
    {
        super(items.toArray());
    }

    public GenericJList() { }

    public GenericJList(E[] items)
    {
        super(items);
    }

    public void setListData(Collection<E> items)
    {
        super.setListData(items.toArray());
    }

    @Override
    public E getSelectedValue()
    {
        return (E) super.getSelectedValue();
    }

    //@Override
    public Collection<E> getGenericSelectedValues()
    {
        Collection<E> values = new Vector<E>();
        for(Object obj : super.getSelectedValues())
        {
            values.add((E)obj);
        }

        return values;
    }

    public Collection<E> getItems()
    {
        Vector<E> items = new Vector<E>();
        for (int i = 0; i < this.getModel().getSize(); i++)
        {
            items.add((E)this.getModel().getElementAt(i));
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