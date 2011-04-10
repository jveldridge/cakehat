package cakehat.config.handin;

import com.google.common.collect.ImmutableList;
import cakehat.Allocator;
import cakehat.config.Assignment;
import cakehat.config.TimeInformation;
import cakehat.database.Group;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents all of the handins from all of the groups for a given Assignment.
 * <br/><br/>
 * Each {@link Assignment} has at most one <code>Handin</code>.
 *
 * @author jak2
 */
public class Handin
{
    private final Assignment _assignment;
    private final TimeInformation _timeInfo;

    /**
     * Populated with the files representing the handins. It will be
     * <code>null</code> until it is first requested with a call to
     * {@link #getHandins()} and it may become <code>null</code> again when the
     * cache is cleared by {@link #clearHandinCache()}.
     */
    private List<File> _handins = null;

    public Handin(Assignment asgn, TimeInformation timeInfo)
    {
        _assignment = asgn;
        _timeInfo = timeInfo;
    }

    public Assignment getAssignment()
    {
        return _assignment;
    }

    public TimeInformation getTimeInformation()
    {
        return _timeInfo;
    }

    /**
     * Clears the cached list of the files that are the handins.
     */
    public void clearHandinCache()
    {
        _handins = null;
    }

    /**
     * Returns an immutable list of the {@link File}s for each handin for this
     * assignment. If this method has not been called before it will load all
     * of the handins. Subsequent calls of this method will return the same list
     * unless the cache has been cleared with {@link #clearHandinCache()}.
     *
     * @return handins
     */
    private List<File> getHandins() throws IOException
    {
        //If handins have not been requested yet, load them
        if(_handins == null)
        {
            File handinPath = Allocator.getPathServices().getHandinDir(this);
            FileFilter handinFilter = Allocator.getArchiveUtilities().getSupportedFormatsFilter();

            try
            {
                _handins = ImmutableList.copyOf(
                        Allocator.getFileSystemUtilities().getFiles(handinPath, handinFilter));
            }
            catch(IOException e)
            {
                throw new IOException("Unable to retrieve handins for " +
                        "assignment [" + this.getAssignment().getName() + "]", e);
            }
        }

        return _handins;
    }

    /**
     * Finds the most recent handin for the group. The most recent is selected
     * because multiple members of the group may have turned in the assignment.
     * If no handin exists <code>null</code> will be returned.
     *
     * @param group
     * @return
     */
    public File getHandin(Group group) throws IOException
    {
        //Valid names are the name of any group member or the login of any member
        ArrayList<String> validHandinNames = new ArrayList<String>(group.getMembers());
        validHandinNames.add(group.getName());

        //Get all handins for the group
        ArrayList<File> matchingHandins = new ArrayList<File>();
        List<File> allHandins;
        try
        {
            allHandins = this.getHandins();
        }
        catch(IOException e)
        {
            throw new IOException("Unable to retrieve handin for group [" +
                    group.getName() + "] for assignment [" +
                    this.getAssignment().getName() + "]", e);
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
     * Whether the handin exists.
     *
     * @param group
     * @return
     * @throws IOException
     */
    public boolean hasHandin(Group group) throws IOException
    {
        return (this.getHandin(group) != null);
    }

    /**
     * Returns the names of the files, without extensions, for each handin. This
     * will likely be either be a student login or the name of a group, but this
     * is not guaranteed - it is entirely dependent on the archive files in the
     * handin directory.
     *
     * @return
     */
    public List<String> getHandinNames() throws IOException
    {
        ArrayList<String> logins = new ArrayList<String>();
        for(File handin : this.getHandins())
        {
            //Split at the . in the filename
            //So if handin is "jak2.tar", will add the "jak2" part
            logins.add(handin.getName().split("\\.")[0]);
        }

        return logins;
    }
}