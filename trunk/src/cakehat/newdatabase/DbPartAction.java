package cakehat.newdatabase;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a part action--to demo, open, print, run, or test a part--as it is represented in the database and
 * configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbPartAction extends DbDataItem
{    
    private volatile DbPart _part;
    private volatile String _name;
    private final Set<DbActionProperty> _properties;
    
    /**
     * Constructor to be used by the configuration manager to create a new part action for a part.
     * 
     * @param part
     */
    public DbPartAction(DbPart part, String name)
    {
        super(null);
        
        _part = part;
        _name = name;
        _properties = new HashSet<DbActionProperty>();
    }

    /**
     * Constructor to be used by the database to load part action data into memory.
     * 
     * @param part
     * @param id
     * @param name
     * @param properties 
     */
    DbPartAction(DbPart part, int id, String name, Set<DbActionProperty> properties)
    {
        super(id);
        
        _part = part;
        _name = name;
        _properties = properties;
    }
    
    public void setName(String name)
    {
        _name = name;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void addActionProperty(DbActionProperty property)
    {
        synchronized(_properties)
        {
            _properties.add(property);
        }
    }
    
    public void removeActionProperty(DbActionProperty property)
    {
        synchronized(_properties)
        {
            _properties.remove(property);
        }
    }
    
    public ImmutableSet<DbActionProperty> getActionProperties()
    {
        synchronized(_properties)
        {
            return ImmutableSet.copyOf(_properties);
        }
    }
    
    DbPart getPart()
    {
        return _part;
    }
}