package cakehat.newdatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a course assignment as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbAssignment extends DbDataItem
{
    private String _name;
    private int _order;
    private boolean _hasGroups = false;
    
    private final List<DbGradableEvent> _gradableEvents;
    
    /**
     * Constructor to be used by the configuration manager to create a new assignment for the course.
     * 
     * @param order 
     */
    public DbAssignment(int order)
    {
        super(false, null);
        _order = order;
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
        super(true, id);
        _name = name;
        _order = order;
        _hasGroups = hasGroups;
        _gradableEvents = gradableEvents;
    }
    
    public void setName(final String name)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _name = name;
            }
        });
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void setOrder(final int order)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _order = order;
            }
        });
    }
    
    public Integer getOrder()
    {
        return _order;
    }
    
    public void setHasGroups(final boolean hasGroups)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _hasGroups = hasGroups;
            }
        });
    }
    
    public boolean getHasGroups()
    {
        return _hasGroups;
    }
    
    public void addGradableEvent(final DbGradableEvent gradableEvent)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _gradableEvents.add(gradableEvent);
            }
        });
    }
    
    public void removeGradableEvent(final DbGradableEvent gradableEvent)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _gradableEvents.remove(gradableEvent);
            }
        });
    }
    
    public List<DbGradableEvent> getGradableEvents()
    {
        return Collections.unmodifiableList(_gradableEvents);
    }
}