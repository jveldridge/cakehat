package cakehat.newdatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a part action--to demo, open, print, run, or test a part--as it is represented in the database and
 * configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbPartAction extends DbDataItem
{    
    private DbPart _part;
    private String _mode;
    private final List<DbActionProperty> _properties;
    
    /**
     * Constructor to be used by the configuration manager to create a new part action for a part.
     * 
     * @param part
     */
    public DbPartAction(DbPart part)
    {
        super(true, null);
        _part = part;
        _properties = new ArrayList<DbActionProperty>();
    }

    /**
     * Constructor to be used by the database to load part action data into memory.
     * 
     * @param part
     * @param id
     * @param mode
     * @param properties 
     */
    DbPartAction(DbPart part, int id, String mode, List<DbActionProperty> properties)
    {
        super(true, id);
        _part = part;
        _mode = mode;
        _properties = properties;
    }
    
    public void setMode(final String mode)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _mode = mode;
            }
        });
    }
    
    public String getMode()
    {
        return _mode;
    }
    
    public void addActionProperty(final DbActionProperty property)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _properties.add(property);
            }
        });
    }
    
    public void removeActionProperty(final DbActionProperty property)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _properties.remove(property);
            }
        });
    }
    
    public List<DbActionProperty> getActionProperties()
    {
        return Collections.unmodifiableList(_properties);
    }
    
    DbPart getPart()
    {
        return _part;
    }
}