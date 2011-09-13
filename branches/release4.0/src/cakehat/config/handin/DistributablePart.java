package cakehat.config.handin;

import cakehat.Allocator;
import cakehat.config.Part;
import cakehat.database.Group;
import cakehat.config.handin.file.FilterProvider;
import cakehat.config.handin.file.OrFileFilter;
import cakehat.services.ServicesException;
import cakehat.views.shared.TextViewerView;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JOptionPane;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import cakehat.config.handin.file.AlwaysAcceptingFileFilter;

/**
 * Part of a group's handin.
 *
 * @author jak2
 */
public class DistributablePart extends Part
{
    private final Handin _handin;
    private final FilterProvider _filterProvider;

    private final File _gradingGuide;
    private final File _rubricTemplate;

    private final DistributableAction _runAction;
    private final DistributableAction _demoAction;
    private final DistributableAction _testAction;
    private final DistributableAction _openAction;
    private final DistributableAction _printAction;
    
    /**
     * Only groups that have had their handins unarchived are included.
     * Key: Group
     * Value: String a description of the files and directories that are
     * supposed to exist for this distributable part, but do not
     */
    private final HashMap<Group, String> _unarchivedGroups = new HashMap<Group, String>();

    /**
     * Keeps track of which groups have readmes so this only needs to be
     * determined once.
     */
    private final HashMap<Group, Boolean> _groupsWithReadme = new HashMap<Group, Boolean>();

    /**
     * Keeps track of the files that are readmes for each group.
     */
    private final HashMap<Group, Collection<File>> _readmes = new HashMap<Group, Collection<File>>();

    /**
     *
     * @param handin the handin this part belongs to
     * @param name unique name for this part, unique amongst all parts of this assignment
     * @param number if the same as another part that means the parts are
     * mutually exclusive (a student/group does only one of them)
     * @param points point value for this part
     * @param gradingGuide plain text file that contains grading guidelines
     * @param rubricTemplate template gml file
     * @param filterProvider provider of the FileFilter that describes which
     * files belong to this part. This is necessary because any number of
     * distributable parts may share a handin and may not wish to share all
     * files in common.
     * @param runAction action used to run the part for a given group, may be null
     * @param demoAction action used to demo the part, may be null
     * @param testAction action used to test the part for a given group, may be null
     * @param openAction action used to open the part for a given group, may be null
     * @param printAction action used to print the part for a given group, may be null
     */
    public DistributablePart(Handin handin, String name, int number, int points,
            File gradingGuide,
            File rubricTemplate,
            FilterProvider filterProvider,
            DistributableAction runAction,
            DistributableAction demoAction,
            DistributableAction testAction,
            DistributableAction openAction,
            DistributableAction printAction)
    {
        super(handin.getAssignment(), name, number, points);

        _handin = handin;
        _filterProvider = filterProvider;

        _gradingGuide = gradingGuide;
        _rubricTemplate = rubricTemplate;

        _runAction = runAction;
        _demoAction = demoAction;
        _testAction = testAction;
        _openAction = openAction;
        _printAction = printAction;
    }

    /**
     * Returns the Handin for this DistributablePart.
     *
     * @return
     */
    public Handin getHandin()
    {
        return _handin;
    }

    /**
     * If there is a specified run mode for this DistributablePart.
     *
     * @return
     */
    public boolean hasRun()
    {
        return (_runAction != null);
    }

    /**
     * Invokes the run mode for this DistributablePart.
     *
     * @throws ActionException
     * @throws MissingHandinException
     *
     * @param group
     */
    public void run(Group group) throws ActionException, MissingHandinException
    {
        if(_runAction == null)
        {
            throw this.createActionException("run");
        }

        this.unarchive(group);
        _runAction.performAction(this, group);
    }

    /**
     * If there is a specified demo mode for this DistributablePart.
     *
     * @return
     */
    public boolean hasDemo()
    {
        return (_demoAction != null);
    }

    /**
     * Invokes the demo mode for this DistributablePart.
     *
     * @throws ActionException
     *
     * @param group
     */
    public void demo() throws ActionException
    {
        if(_demoAction == null)
        {
            throw this.createActionException("demo");
        }

        _demoAction.performAction(this, (Group) null);
    }

    /**
     * If there is a specified print mode for this DistributablePart.
     *
     * @return
     */
    public boolean hasPrint()
    {
        return (_printAction != null);
    }

    /**
     * Invokes the print mode for this DistributablePart.
     *
     * @throws ActionException
     *
     * @param group
     */
    public void print(Collection<Group> groups) throws ActionException
    {
        if(_printAction == null)
        {
            throw this.createActionException("print");
        }

        //Unarchive each group's handin, keep track of those with missing handins
        ArrayList<Group> groupsWithMissingHandins = new ArrayList<Group>();
        for(Group group : groups)
        {
            try
            {
                this.unarchive(group);
            }
            catch(MissingHandinException e)
            {
                groupsWithMissingHandins.add(e.getGroup());
            }
        }

        //If some groups have missing handins
        if(!groupsWithMissingHandins.isEmpty())
        {
            String groupsOrStudents = (this.getAssignment().hasGroups() ? "groups" : "students");

            String message = "The following " + groupsOrStudents +
                    " are missing handins:";
            for(Group group : groupsWithMissingHandins)
            {
                message += "\n • " + group.getName();
            }
            message += "\n\nDo you want to print the " + groupsOrStudents + " with handins?";

            int result = JOptionPane.showConfirmDialog(null,
                    message, "Missing Handins", JOptionPane.YES_NO_OPTION);

            if(result == JOptionPane.YES_OPTION)
            {
                ArrayList<Group> groupsWithHandins = new ArrayList<Group>(groups);
                groupsWithHandins.removeAll(groupsWithMissingHandins);
                _printAction.performAction(this, groupsWithHandins);
            }
        }
        else
        {
            _printAction.performAction(this, groups);
        }
    }

    /**
     * Invokes the print mode for this DistributablePart.
     *
     * @throws ActionException
     * @throws MissingHandinException
     *
     * @param group
     */
    public void print(Group group) throws ActionException, MissingHandinException
    {
        if(_printAction == null)
        {
            throw this.createActionException("print");
        }

        this.unarchive(group);
        _printAction.performAction(this, group);
    }

    /**
     * If there is a specified open mode for this DistributablePart.
     *
     * @return
     */
    public boolean hasOpen()
    {
        return (_openAction != null);
    }

    /**
     * Invokes the open mode for this DistributablePart.
     * 
     * @throws ActionException
     * @throws MissingHandinException
     *
     * @param group
     */
    public void open(Group group) throws ActionException, MissingHandinException
    {
        if(_openAction == null)
        {
            throw this.createActionException("open");
        }

        this.unarchive(group);
        _openAction.performAction(this, group);
    }

    /**
     * If there is a specified test mode for this DistributablePart.
     *
     * @return
     */
    public boolean hasTest()
    {
        return (_testAction != null);
    }

    /**
     * Invokes the test mode for this DistributablePart.
     * 
     * @throws ActionException
     * @throws MissingHandinException
     *
     * @param group
     */
    public void test(Group group) throws ActionException, MissingHandinException
    {
        if(_testAction == null)
        {
            throw this.createActionException("test");
        }

        this.unarchive(group);
        _testAction.performAction(this, group);
    }

    /**
     * Create an {@link ActionException} for a {@link DistributableAction} that
     * does not exist.
     * <br/><br/>
     * Message will read:
     * <br/>
     * No <code>dneAction</code> action exists for <code>[Assignment Name] -
     * [Part Name]</code>
     *
     * @param missingAction
     * @return
     */
    private ActionException createActionException(String dneAction)
    {
        return new ActionException("No " + dneAction + " action exists for " +
                    this.getAssignment().getName() + " - " + this.getName());
    }

    /**
     * Views all readme files in the handin. Hidden files and files that end in
     * ~ will be ignored. Files that have no file extension or end in txt will
     * be interpreted as text files. Files that end in pdf will be interpreted
     * as pdf files and will be opened with KPDF. If a readme of another file
     * extension is found, the grader will be informed the file cannot be opened
     * by cakehat.
     *
     * @throws ActionException
     * @throws MissingHandinException
     *
     * @param group
     */
    public void viewReadme(Group group) throws ActionException, MissingHandinException
    {
        Collection<File> readmes = this.getReadmes(group);

        //For each readme
        for(File readme : readmes)
        {
            String name = readme.getName().toLowerCase();

            //If a text file
            if(!name.contains(".") || name.endsWith(".txt"))
            {
                new TextViewerView(readme, group.getName() +"'s Readme");
            }
            //If a PDF
            else if(readme.getAbsolutePath().toLowerCase().endsWith(".pdf"))
            {
                try
                {
                    File unarchiveDir = Allocator.getPathServices()
                            .getUnarchiveHandinDir(this, group);
                    Allocator.getExternalProcessesUtilities()
                            .executeAsynchronously("kpdf " + 
                            "'" + readme.getAbsolutePath() + "'", unarchiveDir);
                }
                catch(IOException e)
                {
                    throw new ActionException("Unable to open readme in kdpf: " +
                            readme.getAbsolutePath(), e);
                }
            }
            //Otherwise, the type is not supported, inform the grader
            else
            {
                JOptionPane.showMessageDialog(null, "Encountered README that cannot be opened by cakehat: \n"
                                                    + readme.getAbsolutePath());
            }
        }
    }

    /**
     * Returns Files that begin with "readme" (case insensitive), do not end
     * with ~, and are not hidden.
     *
     * @throws ActionException
     * @throws MissingHandinException
     *
     * @param group
     * @return
     */
    private Collection<File> getReadmes(Group group) throws ActionException, MissingHandinException
    {
        if(!_readmes.containsKey(group))
        {
            this.unarchive(group);

            //Get all of the readmes
            FileFilter filter = new FileFilter()
            {
                public boolean accept(File file)
                {
                    boolean accept = !file.isHidden() &&
                            file.getName().toUpperCase().startsWith("README") &&
                            !file.getName().endsWith("~");

                    return accept;
                }
            };

            try
            {
                Collection<File> readmes = Allocator.getFileSystemUtilities()
                    .getFiles(Allocator.getPathServices().getUnarchiveHandinDir(this, group), filter);
                _readmes.put(group, readmes);
            }
            catch(IOException e)
            {
                throw new ActionException("Unable to access READMEs", e);
            }
        }

        return _readmes.get(group);
    }

    /**
     * Determines if a readme exists for the group without unarchiving the
     * handin.
     *
     * @throws ActionException
     * @throws MissingHandinException
     *
     * @param group
     * @return
     */
    public boolean hasReadme(Group group) throws ActionException, MissingHandinException
    {
        if(!_groupsWithReadme.containsKey(group))
        {
            boolean hasReadme = false;

            File handin;
            try
            {
                handin = _handin.getHandin(group);
            }
            catch(IOException e)
            {
                throw new ActionException(e);
            }

            //Because the handins are cached, check it still exists
            if(handin == null || !handin.exists())
            {
                throw new MissingHandinException(group);
            }
            else
            {
                //Get contents of archive
                Collection<ArchiveEntry> contents;
                try
                {
                    contents = Allocator.getArchiveUtilities().getArchiveContents(handin);

                    //For each entry (file and directory) in the handin
                    for(ArchiveEntry entry : contents)
                    {
                        String path = entry.getName();

                        //Extract the file name
                        String filename = path.substring(path.lastIndexOf("/")+1);

                        //See if the file name begins with README, regardless of case and doesn't end with ~
                        if(!entry.isDirectory() &&
                           filename.toUpperCase().startsWith("README") &&
                           !filename.endsWith("~"))
                        {
                            hasReadme = true;
                            break;
                        }
                    }
                }
                catch (ArchiveException e)
                {
                    throw new ActionException("Cannot determine if a readme " +
                            "exists; unable to get archive contents.", e);
                }
            }

            _groupsWithReadme.put(group, hasReadme);
        }

        return _groupsWithReadme.get(group);
    }

    /**
     * If there is a grading guide for this DistributablePart.
     *
     * @return
     */
    public boolean hasGradingGuide()
    {
        boolean exists = (_gradingGuide != null) && (_gradingGuide.exists());

        return exists;
    }

    /**
     * Displays to the grader the grading guide for this DistributablePart.
     *
     * @throws FileNotFoundException if no grading guide was specified or
     * the specified file does not exist
     */
    public void viewGradingGuide() throws FileNotFoundException
    {
        if(_gradingGuide == null)
        {
            throw new FileNotFoundException("No grading guide file specified for " +
                    this.getAssignment() + " - " + this.getName());
        }
        if(!_gradingGuide.exists())
        {
            throw new FileNotFoundException("Specified grading guide for " +
                    this.getAssignment() + "-" + this.getName() + " does not exist. \n" +
                    "Specified file: " + _gradingGuide.getAbsolutePath());
        }

        new TextViewerView(_gradingGuide, this.getAssignment().getName() + " Grading Guide");
    }

    /**
     * If there is a rubric template (gml file) for this DistributlePart.
     *
     * @return
     */
    public boolean hasRubricTemplate()
    {
        boolean exists = (_rubricTemplate != null) && (_rubricTemplate.exists());

        return exists;
    }

    /**
     * Returns the rubric template for this DistributablePart.
     *
     * @return
     */
    public File getRubricTemplate()
    {
        return _rubricTemplate;
    }

    /**
     * Unarchives a group's most recent handin.
     *
     * @throws ActionException
     * @throws MissingHandinException
     *
     * @param group
     */
    private void unarchive(Group group) throws ActionException, MissingHandinException
    {
        if(!_unarchivedGroups.containsKey(group))
        {
            //Create an empty folder for grading compiled student code
            try
            {
                File groupDir = Allocator.getPathServices().getUnarchiveHandinDir(this, group);
                Allocator.getFileSystemServices().makeDirectory(groupDir);

                //Determine if all of the files and directories that are
                //expected are present
                File handin;
                try
                {
                    handin = _handin.getHandin(group);
                }
                catch(IOException e)
                {
                    throw new ActionException(e);
                }

                //Because the handins are cached, check it still exists
                if(handin == null || !handin.exists())
                {
                    throw new MissingHandinException(group);
                }

                try
                {
                    //Determine if all of files and directories that belong to
                    //this distributable part are present in the archive
                    Collection<ArchiveEntry> contents = Allocator.getArchiveUtilities().getArchiveContents(handin);
                    StringBuffer buffer = new StringBuffer();

                    //Build a filter that only accepts files belonging to this
                    //distributable part and readmes
                    //If not all required files are present, then accept all
                    FileFilter filter;
                    if(_filterProvider.areFilteredFilesPresent(contents, buffer))
                    {
                        FileFilter inclusionFilter = _filterProvider.getFileFilter(groupDir);
                        FileFilter readmeFilter = new FileFilter()
                        {
                            public boolean accept(File file)
                            {
                                return file.isFile() &&
                                       file.getName().toUpperCase().startsWith("README") &&
                                       !file.getName().endsWith("~");
                            }
                        };

                        filter = new OrFileFilter(inclusionFilter, readmeFilter);
                    }
                    else
                    {
                        filter = new AlwaysAcceptingFileFilter();
                    }

                    //Extract
                    Allocator.getArchiveUtilities().extractArchive(handin, groupDir, filter);

                    _unarchivedGroups.put(group, buffer.toString());
                }
                catch (ArchiveException e)
                {
                    throw new ActionException("Unable to extract handin for group: " +
                            group.getName(), e);
                }
            }
            catch(ServicesException e)
            {
                throw new ActionException("Unable to create directory to unarchive " +
                        "the handin for group: " + group.getName(), e);
            }
        }

        String failureReasons = _unarchivedGroups.get(group);
        if(!failureReasons.isEmpty())
        {
            String msg =
                    "Not all files and/or directories this distributable part\n" +
                    "expected were found in the handin. All files and directories \n" +
                    "will now be included in this distributable part so that you \n" +
                    "can find files that may be misnamed or placed in the wrong \n" +
                    "directory. \n\n" +
                    "The missing files and/or directories are: \n" +
                    failureReasons;
            JOptionPane.showMessageDialog(null, msg, "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }
}