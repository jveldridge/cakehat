package cakehat.database;

import cakehat.assignment.PartActionDescription.ActionType;
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
    private volatile Integer _partId;
    private final ActionType _type;
    private volatile String _name;
    private final Set<DbActionProperty> _properties;
    
    public static DbPartAction build(DbPart part, ActionType type) {
        DbPartAction action = new DbPartAction(part, type);
        part.addAction(action);
        
        return action;
    }
    
    /**
     * Constructor to be used by the configuration manager to create a new part action for a part.
     * 
     * @param part
     */
    private DbPartAction(DbPart part, ActionType type)
    {
        super(null);
        
        _partId = part.getId();
        _type = type;
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
    DbPartAction(int partId, int id, String type, String name, Set<DbActionProperty> properties)
    {
        super(id);
        
        _partId = partId;
        _type = ActionType.valueOf(type);
        _name = name;
        _properties = new HashSet<DbActionProperty>(properties);
    }

    public ActionType getType()
    {
        return _type;
    }
    
    public void setName(String name)
    {
        _name = name;
    }
    
    public String getName()
    {
        return _name;
    }
    
    void addActionProperty(DbActionProperty property)
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
    
    public void removeAllActionProperties()
    {
        synchronized(_properties)
        {
            _properties.clear();
        }
    }
    
    public ImmutableSet<DbActionProperty> getActionProperties()
    {
        synchronized(_properties)
        {
            return ImmutableSet.copyOf(_properties);
        }
    }
    
    Integer getPartId() {
        return _partId;
    }

    @Override
    void setParentId(Integer id) {
        _partId = id;
    }
    
    @Override
    Iterable<DbActionProperty> getChildren() {
        return this.getActionProperties();
    }
}