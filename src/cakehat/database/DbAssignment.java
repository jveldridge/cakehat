package cakehat.database;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a course assignment as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbAssignment extends DbDataItem implements Comparable<DbAssignment>
{
    private volatile String _name;
    private volatile int _order;
    private volatile boolean _hasGroups;
    
    private final Set<DbGradableEvent> _gradableEvents;
    
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
        _gradableEvents = new HashSet<DbGradableEvent>();
    }
    
    DbAssignment(int id, String name, int order, boolean hasGroups)
    {
        super(id);
        _name = name;
        _order = order;
        _hasGroups = hasGroups;
        _gradableEvents = new HashSet<DbGradableEvent>();
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
    
    public int getOrder()
    {
        return _order;
    }
    
    public void setHasGroups(boolean hasGroups)
    {
        _hasGroups = hasGroups;
    }
    
    public boolean hasGroups()
    {
        return _hasGroups;
    }
    
    void addGradableEvent(DbGradableEvent gradableEvent)
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
    
    public ImmutableSet<DbGradableEvent> getGradableEvents()
    {
        synchronized(_gradableEvents)
        {
            return ImmutableSet.copyOf(_gradableEvents);
        }
    }

    @Override
    public int compareTo(DbAssignment other)
    {
        return new Integer(_order).compareTo(other._order);
    }
}