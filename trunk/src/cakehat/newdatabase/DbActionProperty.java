package cakehat.newdatabase;

/**
 * Represents an action property of a part action as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbActionProperty extends DbDataItem
{   
    private final DbPartAction _partAction;
    private String _key;
    private String _value;
    
    /**
     * Constructor to be used by the configuration manager to create a new part action property for a part action.
     * 
     * @param partAction 
     */
    public DbActionProperty(DbPartAction partAction)
    {
        super(false, null);
        
        _partAction = partAction;
    }

    /**
     * Constructor to be used by the database to load action property data into memory.
     * 
     * @param partAction 
     * @param id
     * @param key
     * @param value 
     */
    DbActionProperty(DbPartAction partAction, int id, String key, String value)
    {
        super(true, null);
        _partAction = partAction;
        _key = key;
        _value = value;
    }
    
    public void setKey(final String key)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _key = key;
            }
        });
    }
    
    public String getKey()
    {
        return _key;
    }
    
    public void setValue(final String value)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _value = value;
            }
        });
    }
    
    public String getValue()
    {
        return _value;
    }
    
    DbPartAction getPartAction()
    {
        return _partAction;
    }
}