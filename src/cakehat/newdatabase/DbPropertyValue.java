package cakehat.newdatabase;

/**
 * A single value stored in the database's property table.
 *
 * @author jak2
 */
public class DbPropertyValue<T> extends DbDataItem
{
    private T _value;
    
    public DbPropertyValue()
    {
        super(false, null);
    }
    
    DbPropertyValue(int id, T value)
    {
        super(true, id);
    }
    
    public void setValue(final T value)
    {
        updateUnderLock(new Runnable()
        {
            @Override
            public void run()
            {
                _value = value;
            }
        });
    }
    
    public T getValue()
    {
        return _value;
    }
}