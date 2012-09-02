package cakehat.database;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a part of a gradable event as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbPart extends DbDataItem implements Comparable<DbPart>
{
    private volatile DbGradableEvent _gradableEvent;
    private volatile String _name;
    private volatile int _order;
    private volatile String _quickName;
    private final Set<DbAction> _actions;
    private final Set<DbInclusionFilter> _inclusionFilters;
    
    public static DbPart build(DbGradableEvent gradableEvent, String name, int order) {
        DbPart part = new DbPart(gradableEvent, name, order);
        gradableEvent.addPart(part);
        
        return part;
    }
    
    /**
     * Constructor to be used by the configuration manager to create a new part for a gradable event.
     * 
     * @param gradableEvent 
     * @param order 
     */
    private DbPart(DbGradableEvent gradableEvent, String name, int order)
    {
        super(null);
        
        _gradableEvent = gradableEvent;
        
        _name = name;
        _order = order;
        
        _actions = new HashSet<DbAction>();
        _inclusionFilters = new HashSet<DbInclusionFilter>();
    }
    
    /**
     * Constructor to be used by the database to load part data into memory.
     * 
     * @param gradableEvent
     * @param id
     * @param name
     * @param order
     * @param gmlTemplate
     * @param outOf
     * @param quickName
     */
    DbPart(DbGradableEvent gradableEvent, int id, String name, int order, String quickName)
    {
        super(id);
        
        _gradableEvent = gradableEvent;
        _name = name;
        _order = order;
        _quickName = quickName;
        _actions = new HashSet<DbAction>();
        _inclusionFilters = new HashSet<DbInclusionFilter>();
    }
    
    public void setName(String name)
    {
        _name = name;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void setOrder(int order)
    {
        _order = order;
    }
    
    public int getOrder()
    {
        return _order;
    }

    public void setQuickName(String quickName)
    {
        _quickName = quickName;
    }
    
    public String getQuickName()
    {
        return _quickName;
    }
    
    public Set<DbAction> getActions()
    {
        synchronized (_actions)
        {
            return ImmutableSet.copyOf(_actions);
        }
    }
    
    void addAction(DbAction action)
    {
        synchronized (_actions)
        {
            _actions.add(action);
        }
    }
    
    public void removeAction(DbAction action)
    {
        synchronized (_actions)
        {
            _actions.remove(action);
        }
    }
    
    void addInclusionFilter(DbInclusionFilter filter)
    {
        synchronized(_inclusionFilters)
        {
            _inclusionFilters.add(filter);
        }
    }
    
    public void removeInclusionFilter(DbInclusionFilter filter)
    {
        synchronized(_inclusionFilters)
        {
            _inclusionFilters.remove(filter);
        }
    }
    
    public ImmutableSet<DbInclusionFilter> getInclusionFilters()
    {
        synchronized(_inclusionFilters)
        {
            return ImmutableSet.copyOf(_inclusionFilters);
        }
    }
    
    DbGradableEvent getGradableEvent()
    {
        return _gradableEvent;
    }
    
    @Override
    void setParentNull() {
        _gradableEvent = null;
    }
    
    @Override
    Iterable<DbDataItem> getChildren() {
        Collection<DbDataItem> children = new ArrayList<DbDataItem>();
        
        children.addAll(this.getActions());
        children.addAll(this.getInclusionFilters());
        
        return children;
    }
    
    @Override
    public int compareTo(DbPart other)
    {
        return new Integer(_order).compareTo(other._order);
    }
}