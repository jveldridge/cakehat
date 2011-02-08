package gradesystem.handin;

import com.google.common.collect.ImmutableList;
import gradesystem.Allocator;
import gradesystem.config.Assignment;
import gradesystem.config.TimeInformation;
import gradesystem.database.Group;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents all of the handins from all of the groups for a given assignment.
 * Each assignment has at most only one handin.
 *
 * @author jak2
 */
public class Handin
{
    private final Assignment _assignment;
    private final TimeInformation _timeInfo;

    /**
     * Populated with the files representin the handins. It will not exist until
     * it is first requested with a call to {@link #getHandins() }.
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
     * Returns an immutable list of the Files for each handin for this
     * assignment. If this method has not been called before it will load all of
     * the handins. Subsequent calls of this method will return the same list,
     * so changes to the underlying file system will not be taken into account.
     *
     * @return handins
     */
    private List<File> getHandins()
    {
        //If handins have not been requested yet, load them
        if(_handins == null)
        {
            File handinPath = Allocator.getPathServices().getHandinDir(this);
            FileFilter handinFilter = Allocator.getArchiveUtilities().getSupportedFormatsFilter();

            _handins = ImmutableList.copyOf(Allocator.getFileSystemUtilities().getFiles(handinPath, handinFilter));
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
    public File getHandin(Group group)
    {
        //Valid names are the name of any group member or the login of any member
        ArrayList<String> validHandinNames = new ArrayList<String>(group.getMembers());
        validHandinNames.add(group.getName());

        //Get all handins for the group
        ArrayList<File> matchingHandins = new ArrayList<File>();
        for(File handin : this.getHandins())
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
     * Returns the names of the files, without extensions, for each handin. This
     * will either be a student login or the name of a group.
     *
     * @return
     */
    public List<String> getHandinNames()
    {
        ArrayList<String> logins = new ArrayList<String>();
        for (File handin : this.getHandins())
        {
            //Split at the . in the filename
            //So if handin is "jak2.tar", will add the "jak2" part
            logins.add(handin.getName().split("\\.")[0]);
        }

        return logins;
    }
}