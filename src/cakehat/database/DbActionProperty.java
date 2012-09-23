package cakehat.database;

/**
 * Represents an action property of an action as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbActionProperty extends DbDataItem
{   
    private volatile DbAction _action;
    private final String _key;
    private volatile String _value;
    
    public static DbActionProperty build(DbAction action, String key) {
        DbActionProperty property = new DbActionProperty(action, key);
        action.addActionProperty(property);
        
        return property;
    }
    
    /**
     * Constructor to be used by the configuration manager to create a new action property for an action.
     * 
     * @param action 
     */
    private DbActionProperty(DbAction action, String key)
    {
        super(null);
        
        _action = action;
        _key = key;
        _value = "";
    }

    /**
     * Constructor to be used by the database to load action property data into memory.
     * 
     * @param action
     * @param id
     * @param key
     * @param value 
     */
    DbActionProperty(DbAction action, int id, String key, String value)
    {
        super(id);

        _action = action;
        _key = key;
        _value = value;
    }
    
    public String getKey()
    {
        return _key;
    }
    
    public void setValue(String value)
    {
        _value = value;
    }
    
    public String getValue()
    {
        return _value;
    }
    
    DbAction getAction() {
        return _action;
    }
    
}