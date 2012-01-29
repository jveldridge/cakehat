package cakehat.newdatabase;

/**
 * Represents an action property of a part action as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbActionProperty extends DbDataItem
{   
    private volatile Integer _partActionId;
    private final String _key;
    private volatile String _value;
    
    public static DbActionProperty build(DbPartAction partAction, String key) {
        DbActionProperty property = new DbActionProperty(partAction, key);
        partAction.addActionProperty(property);
        
        return property;
    }
    
    /**
     * Constructor to be used by the configuration manager to create a new part action property for a part action.
     * 
     * @param partAction 
     */
    private DbActionProperty(DbPartAction partAction, String key)
    {
        super(null);
        
        _partActionId = partAction.getId();
        _key = key;
        _value = "";
    }

    /**
     * Constructor to be used by the database to load action property data into memory.
     * 
     * @param partAction 
     * @param id
     * @param key
     * @param value 
     */
    DbActionProperty(int partActionId, int id, String key, String value)
    {
        super(id);
        
        _partActionId = partActionId;
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
    
    Integer getPartActionId() {
        return _partActionId;
    }
    
    @Override
    void setParentId(Integer id) {
        _partActionId = id;
    }
}