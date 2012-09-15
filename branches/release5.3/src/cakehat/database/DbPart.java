package cakehat.database;

import cakehat.database.assignment.PartActionDescription;
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
    private volatile Integer _gradableEventId;
    private volatile String _name;
    private volatile int _order;
    private volatile File _gmlTemplate;
    private volatile Double _outOf;
    private volatile String _quickName;
    private volatile File _gradingGuide;
    private final Set<DbPartAction> _actions;
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
        
        _gradableEventId = gradableEvent.getId();
        
        _name = name;
        _order = order;
        
        _actions = new HashSet<DbPartAction>();
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
     * @param gradingGuide
     * @param actions
     * @param inclusionFilters 
     */
    DbPart(int gradableEventId, int id, String name, int order, String gmlTemplate, Double outOf, String quickName,
           String gradingGuide, Set<DbPartAction> actions, Set<DbInclusionFilter> inclusionFilters)
    {
        super(id);
        
        _gradableEventId = gradableEventId;
        _name = name;
        _order = order;
        _gmlTemplate = gmlTemplate == null ? null : new File(gmlTemplate);
        _outOf = outOf;
        _quickName = quickName;
        _gradingGuide = gradingGuide == null ? null : new File(gradingGuide);
        _actions = new HashSet<DbPartAction>(actions);
        _inclusionFilters = new HashSet<DbInclusionFilter>(inclusionFilters);
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

    public void setGmlTemplate(File gmlTemplate)
    {
        _gmlTemplate = gmlTemplate;
    }
    
    public File getGmlTemplate()
    {
        return _gmlTemplate;
    }

    public void setOutOf(Double outOf)
    {
        _outOf = outOf;
    }
    
    public Double getOutOf()
    {
        return _outOf;
    }

    public void setQuickName(String quickName)
    {
        _quickName = quickName;
    }
    
    public String getQuickName()
    {
        return _quickName;
    }

    public void setGradingGuide(File gradingGuide)
    {
        _gradingGuide = gradingGuide;
    }
    
    public File getGradingGuide()
    {
        return _gradingGuide;
    }
    
    public Set<DbPartAction> getActions()
    {
        synchronized (_actions)
        {
            return ImmutableSet.copyOf(_actions);
        }
    }
    
    public DbPartAction getAction(PartActionDescription.ActionType type)
    {
        synchronized (_actions) {
            for (DbPartAction action : _actions) {
                if (action.getType() == type) {
                    return action;
                }
            }
            
            return null;
        }
    }
    
    void addAction(DbPartAction action)
    {
        synchronized (_actions)
        {
            _actions.add(action);
        }
    }
    
    public void removeAction(DbPartAction action)
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
    
    Integer getGradableEventId()
    {
        return _gradableEventId;
    }

    @Override
    void setParentId(Integer id) {
        _gradableEventId = id;
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