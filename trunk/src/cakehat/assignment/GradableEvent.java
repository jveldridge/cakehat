package cakehat.assignment;

import cakehat.Allocator;
import cakehat.database.Group;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * A {@code GradableEvent} belongs to an {@code Assignment} and is a collection of {@link Part}s. Conceptually a
 * {@code GradableEvent} represents a gradable product of work done by a group of one or more students. This could be,
 * but is not limited, to paper handins, digital handins, labs, design checks, and exams. In the case where a
 * {@code GradableEvent} represents a digital handin, the course may specify a directory where those digital handins
 * exist. If a directory is specified then the {@link Part}s that belong to that {@code GradableEvent} may operate
 * on the contents of that digital handin.
 * <br/><br/>
 * An assignment may specify any number of gradable events. Each gradable events will be represented as a single
 * instance of this class in memory. Each gradable event has a unique identifier that will remain stable regardless of
 * changes to other properties.
 * 
 * @author jak2
 */
public class GradableEvent implements Comparable<GradableEvent>, Iterable<Part>
{
    private final int _id;
    private final String _name;
    private final int _order;
    
    private final File _handinDirectory;
    private final DeadlineInfo _deadlineInfo;
    
    /**
     * This value will be set after construction because the assignment object will not be constructed until after the
     * construction of this object, and both objects need to know about each other. Because other threads will be
     * accessing this field, to ensure visibility this value must be volatile.
     */
    private volatile Assignment _assignment;
    
    private final List<Part> _parts;
    
    /**
     * Populated with the files representing the digital handins. It will be {@code null} until it is first requested
     * with a call to {@link #getHandins()} and it may become {@code null} again when the cache is cleared by
     * {@link #clearHandinCache()}.
     */
    private List<File> _handins = null;
    
    /**
     * Constructs a GradableEvent.
     * 
     * @param id unique identifier for this GradableEvent relative to all other GradableEvents, stable regardless of
     * changes
     * @param name human readable name of this Part, may not be {@code null}
     * @param order relative order of this GradableEvent to other GradableEvent in the same Assignment, must be unique
     * for that Assignment
     * @param handinDirectory directory containing digital handins for the parts that belong to this GradableEvent, does
     * not have to be a valid directory, may be {@code null}
     * @param deadlineInfo the deadline information, may be {@code null}
     * @param parts may not be {@code null}
     */
    GradableEvent(int id, String name, int order, File handinDirectory, DeadlineInfo deadlineInfo, List<Part> parts)
    {
        //Validation
        if(name == null)
        {
            throw new NullPointerException("name may not be null");
        }
        if(parts == null)
        {
            throw new NullPointerException("parts may not be null");
        }
        
        _id = id;
        _name = name;
        _order = order;
        
        _deadlineInfo = (deadlineInfo == null ? DeadlineInfo.newNoDeadlineInfo() : deadlineInfo);
        _handinDirectory = handinDirectory;
        
        _parts = ImmutableList.copyOf(parts);
    }
    
    /**
     * Sets the assignment this GradableEvent belongs to.
     * 
     * @param asgn
     * @throws NullPointerException if {@code asgn} is null
     * @throws IllegalStateException if this method has been called before for this instance
     */
    void setAssignment(Assignment asgn)
    {
        if(asgn == null)
        {
            throw new NullPointerException("GradableEvent cannot belong to a null Assignment");
        }
        
        if(_assignment != null)
        {
            throw new IllegalStateException("Assignment may only be set once");
        }
        
        _assignment = asgn;
    }
    
    /**
     * A unique identifier for this GradableEvent.
     * 
     * @return
     */
    public int getId()
    {
        return _id;
    }
    
    /**
     * The assignment this GradableEvent belongs to.
     * 
     * @return 
     * @throws IllegalStateException if the assignment this GradableEvent belongs to has not yet been set
     */
    public Assignment getAssignment()
    {
        if(_assignment == null)
        {
            throw new IllegalStateException("Assignment has not yet been set");
        }
        
        return _assignment;
    }
    
    /**
     * The name of this GradableEvent.
     * 
     * @return 
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * The deadline information for this GradableEvent.
     * 
     * @return 
     */
    public DeadlineInfo getDeadlineInfo()
    {
        return _deadlineInfo;
    }
    
    /**
     * Returns an immutable list of all of the parts that belong to this GradableEvent. 
     * 
     * @return 
     */
    public List<Part> getParts()
    {
        return _parts;
    }
    
    /**
     * Whether this GradableEvent has digital handins associated with it.
     * 
     * @return 
     */
    public boolean hasDigitalHandins()
    {
        return _handinDirectory != null;
    }

    /**
     * Clears the cached list of the files that are the digital handins.
     */
    public void clearDigitalHandinCache()
    {
        _handins = null;
    }

    /**
     * Returns an immutable list of the {@link File}s for each digital handin for this GradableEvent. If this method has
     * not been called before it will load all of the digital handins. Subsequent calls of this method will return the
     * same list unless the cache has been cleared with {@link #clearHandinCache()}.
     *
     * @return list of digital handins
     */
    private List<File> getDigitalHandins() throws IOException
    {
        //If digital handins have not been requested yet, load them
        if(_handins == null)
        {
            if(_handinDirectory != null && _handinDirectory.exists() && _handinDirectory.isDirectory())
            {
                FileFilter handinFilter = Allocator.getArchiveUtilities().getSupportedFormatsFilter();

                try
                {
                    _handins = ImmutableList.copyOf(Allocator.getFileSystemUtilities()
                            .getFiles(_handinDirectory, handinFilter));
                }
                catch(IOException e)
                {
                    throw new IOException("Unable to retrieve handins for gradable event [" + 
                            _name + "] with specified digital handin directory [" +
                            _handinDirectory.getAbsolutePath() + "]", e);
                }
            }
            else
            {
                _handins = Collections.emptyList();
            }
        }

        return _handins;
    }

    /**
     * Finds the most recent digital handin for the group. The most recent is selected because multiple members of the
     * group may have turned in the assignment. For a given group valid handin names are the logins of each group member
     * and the name of the group. If no digital handin exists {@code null} will be returned.
     *
     * @param group
     * @return File representing the group's digital handin or {@code null}
     */
    public File getDigitalHandin(Group group) throws IOException
    {
        //Valid names are the login of any group member or the group name
        HashSet<String> validHandinNames = new HashSet<String>(group.getMemberLogins());
        validHandinNames.add(group.getName());

        //Get all handins for the group
        ArrayList<File> matchingHandins = new ArrayList<File>();
        List<File> allHandins;
        try
        {
            allHandins = this.getDigitalHandins();
        }
        catch(IOException e)
        {
            throw new IOException("Unable to retrieve handin for group [" + group.getName() + "] for gradable event [" +
                    _name + "] with specified digital handin directory [" + _handinDirectory.getAbsolutePath() + "]", e);
        }

        for(File handin : allHandins)
        {
            for(String name : validHandinNames)
            {
                if(handin.getName().startsWith(name + "."))
                {
                    matchingHandins.add(handin);
                }
            }
        }

        //Find the most recent
        File mostRecentHandin = null;
        for(File handin : matchingHandins)
        {
            if(mostRecentHandin == null)
            {
                mostRecentHandin = handin;
            }
            else if(Allocator.getFileSystemUtilities().getModifiedDate(handin)
                    .after(Allocator.getFileSystemUtilities().getModifiedDate(mostRecentHandin)))
            {
                mostRecentHandin = handin;
            }
        }

        return mostRecentHandin;
    }

    /**
     * Whether the group has a digital handin.
     *
     * @param group
     * @return
     * @throws IOException
     */
    public boolean hasDigitalHandin(Group group) throws IOException
    {
        return (this.getDigitalHandin(group) != null);
    }

    /**
     * Returns the names of the files, without extensions, for each digital handin. This will likely be either be a
     * student login or the name of a group, but this is not guaranteed - it is entirely dependent on the archive files
     * in the handin directory.
     *
     * @return
     * @throws IOException
     */
    public List<String> getDigitalHandinNames() throws IOException
    {
        ArrayList<String> logins = new ArrayList<String>();
        for(File handin : this.getDigitalHandins())
        {
            //Split at the first . in the filename
            //If handin is "jak2.tar", will add the "jak2" part
            //If handin is "jak2.tar.gz", will add the "jak2" part
            logins.add(handin.getName().split("\\.")[0]);
        }

        return logins;
    }

    /**
     * Compares this GradableEvent to another based on its ordering.
     * 
     * @param ge
     * @return
     */
    public int compareTo(GradableEvent ge)
    {
        return ((Integer)this._order).compareTo(ge._order);
    }

    public Iterator<Part> iterator()
    {
        return getParts().iterator();
    }
}