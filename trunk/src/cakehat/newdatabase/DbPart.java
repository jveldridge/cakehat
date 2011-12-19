package cakehat.newdatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a part of a gradable event as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbPart extends DbDataItem
{
    private final DbGradableEvent _gradableEvent;
    private String _name;
    private int _order;
    private File _gmlTemplate;
    private Double _outOf;
    private String _quickName;
    private File _gradingGuide;
    private DbPartAction _demoAction;
    private DbPartAction _openAction;
    private DbPartAction _printAction;
    private DbPartAction _runAction;
    private DbPartAction _testAction;
    private final List<DbInclusionFilter> _inclusionFilters;
    
    /**
     * Constructor to be used by the configuration manager to create a new part for a gradable event.
     * 
     * @param gradableEvent 
     * @param order 
     */
    public DbPart(DbGradableEvent gradableEvent, int order)
    {
        super(false, null);
        _gradableEvent = gradableEvent;
        _order = order;
        _inclusionFilters = new ArrayList<DbInclusionFilter>();
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
           List<DbInclusionFilter> inclusionFilters)
    {
        super(true, id);
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
        _inclusionFilters = inclusionFilters;
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

    public void setGmlTemplate(final File gmlTemplate)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _gmlTemplate = gmlTemplate;
            }
        });
    }
    
    public File getGmlTemplate()
    {
        return _gmlTemplate;
    }

    public void setOutOf(final Double outOf)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _outOf = outOf;
            }
        });
    }
    
    public Double getOutOf()
    {
        return _outOf;
    }

    public void setQuickName(final String quickName)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _quickName = quickName;
            }
        });
    }
    
    public String getQuickName()
    {
        return _quickName;
    }

    public void setGradingGuide(final File gradingGuide)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _gradingGuide = gradingGuide;
            }
        });
    }
    
    public File getGradingGuide()
    {
        return _gradingGuide;
    }
    
    public void setDemoAction(final DbPartAction demoAction)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _demoAction = demoAction;
            }
        });
    }
    
    public DbPartAction getDemoAction()
    {
        return _demoAction;
    }
    
    public void setOpenAction(final DbPartAction openAction)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _openAction = openAction;
            }
        });
    }
    
    public DbPartAction getOpenAction()
    {
        return _openAction;
    }
    
    public void setPrintAction(final DbPartAction printAction)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _printAction = printAction;
            }
        });
    }
    
    public DbPartAction getPrintAction()
    {
        return _printAction;
    }
    
    public void setRunAction(final DbPartAction runAction)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _runAction = runAction;
            }
        });
    }
    
    public DbPartAction getRunAction()
    {
        return _runAction;
    }
    
    public void setTestAction(final DbPartAction testAction)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _testAction = testAction;
            }
        });
    }
    
    public DbPartAction getTestAction()
    {
        return _testAction;
    }
    
    public void addInclusionFilter(final DbInclusionFilter filter)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _inclusionFilters.add(filter);
            }
        });
    }
    
    public void removeInclusionFilter(final DbInclusionFilter filter)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _inclusionFilters.remove(filter);
            }
        });
    }
    
    public List<DbInclusionFilter> getInclusionFilters()
    {
        return Collections.unmodifiableList(_inclusionFilters);
    }
    
    DbGradableEvent getGradableEvent()
    {
        return _gradableEvent;
    }
}