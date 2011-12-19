package cakehat.newdatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Represents a gradable event, such as a digital or paper handin, lab, or design check, as it is represented in the
 * database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbGradableEvent extends DbDataItem
{
    private final DbAssignment _asgn;
    private String _name;
    private int _order;
    private File _directory;
    private String _deadlineType; //TODO: Expose type as enum
    private DateTime _earlyDate;
    private Double _earlyPoints;
    private DateTime _onTimeDate;
    private DateTime _lateDate;
    private Double _latePoints;
    private Period _latePeriod;
    private final List<DbPart> _parts;
    
    /**
     * Constructor to be used by the configuration manager to create a new gradable event for an assignment.
     * 
     * @param asgn
     * @param order 
     */
    public DbGradableEvent(DbAssignment asgn, int order)
    {
        super(false, null);
        _asgn = asgn;
        _order = order;
        _parts = new ArrayList<DbPart>();
    }
    
    /**
     * Constructor to be used by the database to load gradable event data into memory.
     * 
     * @param asgn
     * @param id
     * @param name
     * @param order
     * @param directory
     * @param deadlineType
     * @param earlyDate
     * @param earlyPoints
     * @param onTimeDate
     * @param lateDate
     * @param latePoints
     * @param latePeriod
     * @param parts 
     */
    DbGradableEvent(DbAssignment asgn, int id, String name, int order, File directory, String deadlineType,
                    DateTime earlyDate, Double earlyPoints, DateTime onTimeDate, DateTime lateDate, Double latePoints,
                    Period latePeriod, List<DbPart> parts)
    {
        super(true, id);
        _asgn = asgn;
        _name = name;
        _order = order;
        _directory = directory;
        _deadlineType = deadlineType;
        _earlyDate = earlyDate;
        _earlyPoints = earlyPoints;
        _onTimeDate = onTimeDate;
        _lateDate = lateDate;
        _latePoints = latePoints;
        _latePeriod = latePeriod;
        _parts = parts;
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

    public void setDirectory(final File directory)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _directory = directory;
            }
        });
    }
    
    public void setDeadlineType(final String deadlineType)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _deadlineType = deadlineType;
            }
        });
    }

    public void setEarlyDate(final DateTime earlyDate)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _earlyDate = earlyDate;
            }
        });
    }

    public void setEarlyPoints(final Double earlyPoints)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _earlyPoints = earlyPoints;
            }
        });
    }

    public void setOnTimeDate(final DateTime onTimeDate)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _onTimeDate = onTimeDate;
            }
        });
    }
    
    public void setLateDate(final DateTime lateDate)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _lateDate = lateDate;
            }
        });
    }

    public void setLatePoints(final Double latePoints)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _latePoints = latePoints;
            }
        });
    }

    public void setLatePeriod(final Period latePeriod)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _latePeriod = latePeriod;
            }
        });
    }
    
    public void addPart(final DbPart part)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _parts.add(part);
            }
        });
    }
    
    public void removePart(final DbPart part)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _parts.remove(part);
            }
        });
    }
    
    public List<DbPart> getParts()
    {
        return Collections.unmodifiableList(_parts);
    }
    
    public File getDirectory()
    {
        return _directory;
    }

    public String getDeadlineType()
    {
        return _deadlineType;
    }

    public DateTime getEarlyDate()
    {
        return _earlyDate;
    }
    
    public Double getEarlyPoints()
    {
        return _earlyPoints;
    }
    
    public DateTime getOnTimeDate()
    {
        return _onTimeDate;
    }

    public DateTime getLateDate()
    {
        return _lateDate;
    }

    public Double getLatePoints()
    {
        return _latePoints;
    }

    public Period getLatePeriod()
    {
        return _latePeriod;
    }
    
    DbAssignment getAssignment()
    {
        return _asgn;
    }
}