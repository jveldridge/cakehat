package cakehat.newdatabase;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a course assignment as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbAssignment extends DbDataItem
{
    private volatile String _name;
    private volatile int _order;
    private volatile boolean _hasGroups;
    
    private final List<DbGradableEvent> _gradableEvents;
    
    /**
     * Constructor to be used by the configuration manager to create a new assignment for the course.
     * 
     * @param name
     * @param order 
     */
    public DbAssignment(String name, int order)
    {
        super(null);
        _name = name;
        _order = order;
        _hasGroups = false;
        _gradableEvents = new ArrayList<DbGradableEvent>();
    }
    
    /**
     * Constructor to be used by the database to load assignment data into memory.
     * 
     * @param id
     * @param name
     * @param order
     * @param hasGroups
     * @param gradableEvents 
     */
    DbAssignment(int id, String name, int order, boolean hasGroups, List<DbGradableEvent> gradableEvents)
    {
        super(id);
        _name = name;
        _order = order;
        _hasGroups = hasGroups;
        _gradableEvents = new ArrayList<DbGradableEvent>(gradableEvents);
    }
    
    public void setName(final String name)
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
    
    public Integer getOrder()
    {
        return _order;
    }
    
    public void setHasGroups(boolean hasGroups)
    {
        _hasGroups = hasGroups;
    }
    
    public boolean getHasGroups()
    {
        return _hasGroups;
    }
    
    public void addGradableEvent(DbGradableEvent gradableEvent)
    {
        synchronized(_gradableEvents)
        {
            _gradableEvents.add(gradableEvent);
        }
    }
    
    public void removeGradableEvent(DbGradableEvent gradableEvent)
    {
        synchronized(_gradableEvents)
        {
            _gradableEvents.remove(gradableEvent);
        }
    }
    
    public ImmutableList<DbGradableEvent> getGradableEvents()
    {
        synchronized(_gradableEvents)
        {
            return ImmutableList.copyOf(_gradableEvents);
        }
    }
}