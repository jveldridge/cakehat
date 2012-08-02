package cakehat.database;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Represents a gradable event, such as a digital or paper handin, lab, or design check, as it is represented in the
 * database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbGradableEvent extends DbDataItem implements Comparable<DbGradableEvent>
{
    private volatile DbAssignment _asgn;
    private volatile String _name;
    private volatile int _order;
    private volatile File _directory;
    private volatile DeadlineInfo.Type _deadlineType;
    private volatile DateTime _earlyDate;
    private volatile Double _earlyPoints;
    private volatile DateTime _onTimeDate;
    private volatile DateTime _lateDate;
    private volatile Double _latePoints;
    private volatile Period _latePeriod;
    private final Set<DbPart> _parts;
    
    public static DbGradableEvent build(DbAssignment asgn, String name, int order) {
        DbGradableEvent event = new DbGradableEvent(asgn, name, order);
        asgn.addGradableEvent(event);
        
        return event;
    }
    
    /**
     * Constructor to be used by the configuration manager to create a new gradable event for an assignment.
     * 
     * @param asgn
     * @param order 
     */
    private DbGradableEvent(DbAssignment asgn, String name, int order)
    {
        super(null);
        
        _asgn = asgn;
        _name = name;
        _order = order;
        _parts = new HashSet<DbPart>();
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
     */    
    DbGradableEvent(DbAssignment asgn, int id, String name, int order, String directory, String deadlineType,
                    String earlyDate, Double earlyPoints, String onTimeDate, String lateDate, Double latePoints,
                    String latePeriod)
    {
        super(id);
        
        _asgn = asgn;
        _name = name;
        _order = order;
        _directory = directory == null ? null : new File(directory);
        _deadlineType = deadlineType == null ? null : DeadlineInfo.Type.valueOf(deadlineType);
        _earlyDate = earlyDate == null ? null : new DateTime(earlyDate);
        _earlyPoints = earlyPoints;
        _onTimeDate = onTimeDate == null ? null : new DateTime(onTimeDate);
        _lateDate = lateDate == null ? null : new DateTime(lateDate);
        _latePoints = latePoints;
        _latePeriod = latePeriod == null ? null : new Period(latePeriod);
        _parts = new HashSet<DbPart>();
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

    public void setDirectory(File directory)
    {
        _directory = directory;
    }
    
    public void setDeadlineType(DeadlineInfo.Type deadlineType)
    {
        _deadlineType = deadlineType;
    }

    public void setEarlyDate(DateTime earlyDate)
    {
        _earlyDate = earlyDate;
    }

    public void setEarlyPoints(Double earlyPoints)
    {
        _earlyPoints = earlyPoints;
    }

    public void setOnTimeDate(DateTime onTimeDate)
    {
        _onTimeDate = onTimeDate;
    }
    
    public void setLateDate(DateTime lateDate)
    {
        _lateDate = lateDate;
    }

    public void setLatePoints(Double latePoints)
    {
        _latePoints = latePoints;
    }

    public void setLatePeriod(Period latePeriod)
    {
        _latePeriod = latePeriod;
    }
    
    void addPart(DbPart part)
    {
        synchronized(_parts)
        {
            _parts.add(part);
        }
    }
    
    public void removePart(DbPart part)
    {
        synchronized(_parts)
        {
            _parts.remove(part);
        }
    }
    
    public ImmutableSet<DbPart> getParts()
    {
        synchronized(_parts)
        {
            return ImmutableSet.copyOf(_parts);
        }
    }
    
    public File getDirectory()
    {
        return _directory;
    }

    public DeadlineInfo.Type getDeadlineType()
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
    
    @Override
    void setParentNull() {
        _asgn = null;
    }
    
    @Override
    Iterable<DbPart> getChildren() {
        return this.getParts();
    }

    @Override
    public int compareTo(DbGradableEvent other)
    {
        return new Integer(_order).compareTo(other._order);
    }
}