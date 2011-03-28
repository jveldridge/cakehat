package gradesystem.components;

import javax.swing.AbstractListModel;

/**
 * The generic abstract definition for the data model that provides a list
 * with its contents.
 *
 * @author jak2
 */
public abstract class GenericAbstractListModel<T> extends AbstractListModel implements GenericListModel<T>
{
    @Override
    public boolean hasElements()
    {
        return !getElements().isEmpty();
    }

    @Override
    public T getElementAt(int i)
    {
        return getElements().get(i);
    }

    @Override
    public int getSize()
    {
        return getElements().size();
    }

    @Override
    public void notifyRefresh()
    {
        fireContentsChanged(this, 0, this.getSize() - 1);
    }
}