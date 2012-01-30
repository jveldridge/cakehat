package cakehat.assignment;

import cakehat.Allocator;
import support.utils.AlwaysAcceptingFileFilter;
import support.utils.OrFileFilter;
import cakehat.database.Group;
import cakehat.services.ServicesException;
import cakehat.views.shared.TextViewerView;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import support.ui.ModalDialog;

/**
 * A {@code Part} belongs to a {@link GradableEvent}. A {@code Part} is an arbitrary portion of a gradable event. For
 * instance if a gradable event represents an interactive design check then one part could be the discussion with the TA
 * and another part could be code diagrams the student brought with them. If the gradable event represents a digital
 * handin each part could represent a group of files (potentially overlapping) such that each grouping is a solution
 * to an assigned problem. How a course chooses to divide a {@link GradableEvent} into {@code Part}s is entirely up to
 * them. In many cases a {@link GradableEvent} will only have one {@code Part}.
 * <br/><br/>
 * When a {@code Part} belongs to a {@code GradableEvent} that specifies a digital handin directory then a {@code Part}
 * may specify run, test, open, and print actions which operate on a {@link Group}'s digital handin. A {@code Part} may
 * always specify a demo action and grading guide. Either a GML template or an amount of points this part of may be
 * specified, but not both. If a number of points is specified then a quick name may be specified which allows a TA to
 * enter a grade for a part for a given group using a command line interface.
 *
 * @author jak2
 */
public class Part implements Comparable<Part>
{
    private final int _id;
    private final String _name;
    private final int _order;
    
    /**
     * If this Part has its out of determined by the GML template then this value will be loaded lazily. This field is
     * volatile so that once set other threads will be guaranteed to see the new value. It is still possible this value
     * could be computed more than once if the method that determines this value is called concurrently, but this will
     * not cause an issue, it will just be less than optimally efficient in that case. However, by not synchronizing
     * that method it makes it more efficient in the general case.
     */
    private volatile Double _outOf;
    private final File _gmlTemplate;
    private final String _quickName;
    
    private final File _gradingGuide;
    
    private final FilterProvider _filterProvider;

    private final PartAction _runAction;
    private final PartAction _demoAction;
    private final PartAction _testAction;
    private final PartAction _openAction;
    private final PartAction _printAction;
    
     /**
     * This value will be set after construction because the gradable event object will not be constructed until after
     * the construction of this object, and both objects need to know about each other. Because other threads will be
     * accessing this field, to ensure visibility this value must be volatile.
     */
    private volatile GradableEvent _gradableEvent;
    
    /**
     * Only groups that have had their digital handins unarchived are included.
     * <br/>
     * <strong>Key</strong>: Group
     * <br/>
     * <strong>Value</strong>: a description of the files and directories that are supposed to exist for this part, but
     * do not.
     */
    private final Map<Group, String> _unarchivedGroups = new HashMap<Group, String>();

    /**
     * Keeps track of which groups have readmes so this only needs to be determined once. If a group is not in the map
     * then it has not yet been determined if the group has a readme or not.
     */
    private final Map<Group, Boolean> _groupsWithReadme = new HashMap<Group, Boolean>();

    /**
     * Keeps track of the files that are readmes for each group.
     */
    private final Map<Group, Collection<File>> _readmes = new HashMap<Group, Collection<File>>();
    
    /**
     * Constructs a Part.
     * 
     * @param id unique identifier for this Part relative to all other Parts, stable regardless of changes
     * @param name human readable name of this Part, may not be {@code null}
     * @param order relative order of this Part to other Parts in the same GradableEvent, must be unique for that
     * GradableEvent
     * @param outOf must be {@code null} if {@code gmlTemplate} is not {@code null}, otherwise may not be {@code null}
     * @param gmlTemplate must be {@code null} if {@code outOf} is not {@code null}, otherwise must not be {@code null}
     * @param quickName must be {@code null} if {@code gmlTemplate} is not {@code null}
     * @param gradingGuide may be {@code null}, does not have to point to a valid file location
     * @param filterProvider may be {@code null}
     * @param runAction may be {@code null}
     * @param demoAction may be {@code null}
     * @param testAction may be {@code null}
     * @param openAction may be {@code null}
     * @param printAction may be {@code null}
     */
    Part(int id,
         String name,
         int order,
         Double outOf,
         File gmlTemplate,
         String quickName,
         File gradingGuide,
         FilterProvider filterProvider,
         PartAction runAction,
         PartAction demoAction,
         PartAction testAction,
         PartAction openAction,
         PartAction printAction)
    {
        //Validation
        if(name == null)
        {
            throw new NullPointerException("name may not be null");
        }
        if(quickName != null && gmlTemplate != null)
        {
            throw new IllegalArgumentException("quickName must be null if gmlTemplate is not null");
        }
        if(outOf != null && gmlTemplate != null)
        {
            throw new IllegalArgumentException("Exactly one of outOf and gmlTemplate must be null");
        }
        if(outOf == null && gmlTemplate == null)
        {
            throw new NullPointerException("outOf and gmlTemplate may not both be null");
        }
        
        _name = name;
        _order = order;
        _id = id;
        
        _outOf = outOf;
        _gmlTemplate = gmlTemplate;
        _quickName = quickName;
        
        _gradingGuide = gradingGuide;
        
        _filterProvider = (filterProvider == null ? new AlwaysAcceptingFilterProvider() : filterProvider);
        
        _runAction = runAction;
        _demoAction = demoAction;
        _testAction = testAction;
        _openAction = openAction;
        _printAction = printAction;
    }
    
    /**
     * A unique identifier for this part.
     * @return 
     */
    public int getId()
    {
        return _id;
    }
    
    /**
     * Sets the GradableEvent this Part belongs to.
     * 
     * @param asgn
     * @throws NullPointerException if {@code gradableEvent} is null
     * @throws IllegalStateException if this method has been called before for this instance
     */
    void setGradableEvent(GradableEvent gradableEvent)
    {
        if(gradableEvent == null)
        {
            throw new NullPointerException("Part cannot belong to a null GradableEvent");
        }
        
        if(_gradableEvent != null)
        {
            throw new IllegalStateException("GradableEvent may only be set once");
        }
        
        _gradableEvent = gradableEvent;
    }
    
    /**
     * The GradableEvent this Part belongs to.
     * 
     * @return 
     * @throws IllegalStateException if the GradableEvent this Part belongs to has not yet been set
     */
    public GradableEvent getGradableEvent()
    {
        if(_gradableEvent == null)
        {
            throw new IllegalStateException("GradableEvent has not yet been set");
        }
        
        return _gradableEvent;
    }
    
    public Assignment getAssignment()
    {
        return this.getGradableEvent().getAssignment();
    }
    
    /**
     * The name of this part.
     * 
     * @return 
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * If this part has a quick name.
     * 
     * @return 
     */
    public boolean hasQuickName()
    {
        return (_quickName != null);
    }
    
    /**
     * The quick name for this part. A quick name is a unique name given to this part that allows for interacting with
     * this part from a terminal.
     * 
     * @return quick name, may be {@code null}
     */
    public String getQuickName()
    {
        return _quickName;
    }
    
    /**
     * The amount of points this part is out of. If this is determined by a GML template and the specified template does
     * not exist or cannot be read then {@code 0} will be returned.
     * 
     * @return 
     */
    public double getOutOf()
    {
        if(_outOf == null)
        {
            if(_gmlTemplate.exists() && _gmlTemplate.isFile())
            {
                //TODO: Parse GML template to determine part's total out of
                _outOf = 0D;
            }
            else
            {
                _outOf = 0D;
            }
        }
        
        return _outOf;
    }
    
    /**
     * Returns a convenient human readable string describing this assignment. The format is "[Assignment Name] - 
     * [Gradable Event Name] - [Part Name]"
     * 
     * @return 
     */
    public String getFullDisplayName()
    {
        return this.getGradableEvent().getAssignment().getName() + " - " +
               this.getGradableEvent().getName() + " - " +
               this.getName();
    }
    
    /**
     * If there is a specified run mode for this part.
     *
     * @return
     */
    public boolean hasRun()
    {
        return (_runAction != null);
    }

    /**
     * Invokes the run mode for this part.
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
     * If there is a specified demo mode for this part.
     *
     * @return
     */
    public boolean hasDemo()
    {
        return (_demoAction != null);
    }

    /**
     * Invokes the demo mode for this part.
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
     * If there is a specified print mode for this part.
     *
     * @return
     */
    public boolean hasPrint()
    {
        return (_printAction != null);
    }

    /**
     * Invokes the print mode for this part.
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
            String groupsOrStudents = (this.getGradableEvent().getAssignment().hasGroups() ? "groups" : "students");

            String message = "The following " + groupsOrStudents + " are missing handins:";
            for(Group group : groupsWithMissingHandins)
            {
                message += "\n • " + group.getName();
            }
            message += "\n\nDo you want to print the " + groupsOrStudents + " with handins?";

            int result = JOptionPane.showConfirmDialog(null, message, "Missing Handins", JOptionPane.YES_NO_OPTION);

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
     * If there is a specified open mode for this part.
     *
     * @return
     */
    public boolean hasOpen()
    {
        return (_openAction != null);
    }

    /**
     * Invokes the open mode for this part.
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
     * If there is a specified test mode for this part.
     *
     * @return
     */
    public boolean hasTest()
    {
        return (_testAction != null);
    }

    /**
     * Invokes the test mode for this part.
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
     * Create an {@link ActionException} for a {@link PartAction} that does not exist.
     * <br/><br/>
     * Message will read:
     * <br/>
     * No {@code dneAction} action exists for {@code [Assignment Name] - [Gradable Event Name ] - [Part Name]}
     *
     * @param dneAction
     * @return exception
     */
    private ActionException createActionException(String dneAction)
    {
        return new ActionException("No " + dneAction + " action exists for " + this.getFullDisplayName());
    }

    /**
     * Views all readme files in the handin. Hidden files and files that end in ~ will be ignored. Files that have no
     * file extension or end in txt will be interpreted as text files. Files that end in pdf will be interpreted
     * as pdf files and will be opened with evince. If a readme of another file extension is found, the grader will be
     * informed the file cannot be opened by cakehat.
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
                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(this, group);
                    Allocator.getExternalProcessesUtilities().executeAsynchronously("evince '" +
                            readme.getAbsolutePath() + "'", unarchiveDir);
                }
                catch(IOException e)
                {
                    throw new ActionException("Unable to open readme in evince: " + readme.getAbsolutePath(), e);
                }
            }
            //Otherwise, the type is not supported, inform the grader
            else
            {
                ModalDialog.showMessage("Cannot open README", "Encountered README that cannot be opened by cakehat:\n" +
                        readme.getAbsolutePath());
            }
        }
    }

    /**
     * Returns files that begin with "readme" (case insensitive), do not end with ~, and are not hidden.
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
     * Determines if a readme exists for the group without unarchiving the handin.
     *
     * @throws ActionException
     * @throws MissingHandinException
     *
     * @param group
     * @return
     */
    public boolean hasReadme(Group group) throws ActionException, MissingHandinException, ArchiveException
    {
        if(!_groupsWithReadme.containsKey(group))
        {
            boolean hasReadme = false;

            File handin;
            try
            {
                handin = _gradableEvent.getDigitalHandin(group);
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
     * If there is a grading guide specified for this Part. The specified grading guide does not necessarily exist.
     *
     * @return
     */
    public boolean hasGradingGuide()
    {
        return _gradingGuide != null;
    }

    /**
     * Displays to the grader the grading guide for this DistributablePart.
     *
     * @throws FileNotFoundException if no grading guide was specified or the specified file does not exist
     */
    public void viewGradingGuide() throws FileNotFoundException
    {
        if(_gradingGuide == null)
        {
            throw new FileNotFoundException("No grading guide file specified for " + this.getFullDisplayName());
        }
        if(!_gradingGuide.exists())
        {
            throw new FileNotFoundException("Specified grading guide for " + this.getFullDisplayName() +
                    " does not exist.\n" +
                    "Specified file: " + _gradingGuide.getAbsolutePath());
        }

        new TextViewerView(_gradingGuide, this.getFullDisplayName() + " Grading Guide");
    }

    /**
     * If a GML template was specified for this part. Just because a GML template was specified does not mean the
     * specified file actually exists and is a valid GML template file.
     *
     * @return
     */
    public boolean hasSpecifiedGMLTemplate()
    {
        return (_gmlTemplate != null);
    }

    /**
     * Returns a file to the specified GML template.
     *
     * @return
     */
    public File getGMLTemplate()
    {
        return _gmlTemplate;
    }

    /**
     * Unarchives a group's most recent digital handin for the gradable event this part belongs to.
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

                //Determine if all of the files and directories that are expected are present
                File handin;
                try
                {
                    handin = _gradableEvent.getDigitalHandin(group);
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
                    //Determine if all of files and directories that belong to this part are present in the archive
                    Collection<ArchiveEntry> contents = Allocator.getArchiveUtilities().getArchiveContents(handin);
                    StringBuilder builder = new StringBuilder();

                    //Build a filter that only accepts files belonging to this part and readmes
                    //If not all required files are present, then accept all
                    FileFilter filter;
                    if(_filterProvider.areFilteredFilesPresent(contents, builder))
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

                    _unarchivedGroups.put(group, builder.toString());
                }
                catch (ArchiveException e)
                {
                    throw new ActionException("Unable to extract handin for group: " +
                            group.getName(), e);
                }
            }
            catch(ServicesException e)
            {
                throw new ActionException("Unable to create directory to unarchive the handin for group: " +
                        group.getName(), e);
            }
        }

        String failureReasons = _unarchivedGroups.get(group);
        if(!failureReasons.isEmpty())
        {
            String msg = "Not all files and/or directories this part expected were found in the digital handin. All " +
                         "files and directories will now be included for this part so that you can find files that " +
                         "may be misnamed or placed in the wrong directory.\n\n" +
                         "The missing files and/or directories are:\n" + failureReasons;
            ModalDialog.showMessage("Not Found", msg);
        }
    }
    
    /**
     * Compares this part to another based on its ordering.
     * 
     * @param p
     * @return
     */
    @Override
    public int compareTo(Part p)
    {
        return ((Integer)this._order).compareTo(p._order);
    }
}