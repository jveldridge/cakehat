package cakehat.database;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import support.resources.icons.IconLoader.IconImage;

/**
 * Represents an action as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbAction extends DbDataItem implements Comparable<DbAction>
{   
    private volatile DbPart _part;
    private volatile String _name;
    private volatile IconImage _icon;
    private volatile int _order;
    private volatile String _task;
    private final Set<DbActionProperty> _properties;
    
    public static DbAction build(DbPart part, String name, IconImage icon, int order) {
        DbAction action = new DbAction(part, name, icon, order);
        part.addAction(action);
        
        return action;
    }
    
    /**
     * Constructor to be used by the configuration manager to create a new action.
     * 
     * @param part
     * @param name
     * @param icon
     * @param order
     */
    private DbAction(DbPart part, String name, IconImage icon, int order)
    {
        super(null);
        
        _part = part;
        _name = name;
        _icon = icon;
        _order = order;
        _properties = new HashSet<DbActionProperty>();
    }

    /**
     * Constructor to be used by the database to load action data into memory.
     * 
     * @param part
     * @param id
     * @param name
     * @param icon
     * @param order
     * @param task
     */
    DbAction(DbPart part, int id, String name, String icon, int order, String task)
    {
        super(id);
        
        _part = part;
        
        _name = name;
        _icon = icon == null ? null : IconImage.valueOf(icon);
        _order = order;
        _task = task;
        _properties = new HashSet<DbActionProperty>();
    }
    
    public void setName(String name)
    {
        _name = name;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void setIcon(IconImage icon)
    {
        _icon = icon;
    }
    
    public IconImage getIcon()
    {
        return _icon;
    }
    
    public void setOrder(int order)
    {
        _order = order;
    }
    
    public int getOrder()
    {
        return _order;
    }
    
    public void setTask(String task)
    {
        _task = task;
    }
    
    public String getTask()
    {
        return _task;
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
    
    DbPart getPart()
    {
        return _part;
    }

    @Override
    void setParentNull()
    {
        _part = null;
    }
    
    @Override
    Iterable<DbActionProperty> getChildren()
    {
        return this.getActionProperties();
    }
    
    @Override
    public int compareTo(DbAction other)
    {
        return new Integer(_order).compareTo(other._order);
    }
}