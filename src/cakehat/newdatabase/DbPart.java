package cakehat.newdatabase;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a part of a gradable event as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbPart extends DbDataItem
{
    private final DbGradableEvent _gradableEvent;
    private volatile String _name;
    private volatile int _order;
    private volatile File _gmlTemplate;
    private volatile Double _outOf;
    private volatile String _quickName;
    private volatile File _gradingGuide;
    private volatile DbPartAction _demoAction;
    private volatile DbPartAction _openAction;
    private volatile DbPartAction _printAction;
    private volatile DbPartAction _runAction;
    private volatile DbPartAction _testAction;
    private final Set<DbInclusionFilter> _inclusionFilters;
    
    /**
     * Constructor to be used by the configuration manager to create a new part for a gradable event.
     * 
     * @param gradableEvent 
     * @param order 
     */
    public DbPart(DbGradableEvent gradableEvent, String name, int order)
    {
        super(null);
        
        _gradableEvent = gradableEvent;
        
        _name = name;
        _order = order;
        
        
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
     * @param demoAction
     * @param openAction
     * @param printAction
     * @param runAction
     * @param testAction
     * @param inclusionFilters 
     */
    DbPart(DbGradableEvent gradableEvent, int id, String name, int order, File gmlTemplate, Double outOf,
           String quickName, File gradingGuide, DbPartAction demoAction, DbPartAction openAction,
           DbPartAction printAction, DbPartAction runAction, DbPartAction testAction,
           Set<DbInclusionFilter> inclusionFilters)
    {
        super(id);
        
        _gradableEvent = gradableEvent;
        _name = name;
        _order = order;
        _gmlTemplate = gmlTemplate;
        _outOf = outOf;
        _quickName = quickName;
        _gradingGuide = gradingGuide;
        _demoAction = demoAction;
        _openAction = openAction;
        _printAction = printAction;
        _runAction = runAction;
        _testAction = testAction;
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
    
    public void setDemoAction(DbPartAction demoAction)
    {
        _demoAction = demoAction;
    }
    
    public DbPartAction getDemoAction()
    {
        return _demoAction;
    }
    
    public void setOpenAction(DbPartAction openAction)
    {
        _openAction = openAction;
    }
    
    public DbPartAction getOpenAction()
    {
        return _openAction;
    }
    
    public void setPrintAction(DbPartAction printAction)
    {
        _printAction = printAction;
    }
    
    public DbPartAction getPrintAction()
    {
        return _printAction;
    }
    
    public void setRunAction(DbPartAction runAction)
    {
        _runAction = runAction;
    }
    
    public DbPartAction getRunAction()
    {
        return _runAction;
    }
    
    public void setTestAction(DbPartAction testAction)
    {
        _testAction = testAction;
    }
    
    public DbPartAction getTestAction()
    {
        return _testAction;
    }
    
    public void addInclusionFilter(DbInclusionFilter filter)
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
}