package cakehat.database.assignment;

import cakehat.Allocator;
import support.utils.AlwaysAcceptingFileFilter;
import cakehat.database.Group;
import cakehat.gml.GMLParser;
import cakehat.gml.GradingSheetException;
import cakehat.gml.InMemoryGML;
import cakehat.gml.InMemoryGML.Section;
import cakehat.gml.InMemoryGML.Subsection;
import java.awt.Window;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.commons.compress.archivers.ArchiveEntry;
import support.ui.ModalDialog;
import support.utils.ArchiveExtractionException;

/**
 * A {@code Part} belongs to a {@link GradableEvent}. A {@code Part} is an arbitrary portion of a gradable event. For
 * instance if a gradable event represents an interactive design check then one part could be the discussion with the TA
 * and another part could be code diagrams the student brought with them. If the gradable event represents a digital
 * handin each part could represent a group of files (potentially overlapping) such that each grouping is a solution
 * to an assigned problem. How a course chooses to divide a {@link GradableEvent} into {@code Part}s is entirely up to
 * them. In many cases a {@link GradableEvent} will only have one {@code Part}.
 * <br/><br/>
 * Either a GML template or an amount of points this part is out of may be specified, but not both. If a number of
 * points is specified then a quick name may be specified which allows a TA to enter a grade for a part for a given
 * group using a command line interface.
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
    
    private final FilterProvider _filterProvider;

    private final List<Action> _actions;
    
     /**
     * This value will be set after construction because the gradable event object will not be constructed until after
     * the construction of this object, and both objects need to know about each other. Because other threads will be
     * accessing this field, to ensure visibility this value must be volatile.
     */
    private volatile GradableEvent _gradableEvent;
    
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
     * @param filterProvider may be {@code null}
     * @param actions may not be {@code null}
     */
    Part(int id,
         String name,
         int order,
         Double outOf,
         File gmlTemplate,
         String quickName,
         FilterProvider filterProvider,
         List<Action> actions)
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
        if(actions == null)
        {
            throw new NullPointerException("actions may not be null");
        }
        
        _name = name;
        _order = order;
        _id = id;
        
        _outOf = outOf;
        _gmlTemplate = gmlTemplate;
        _quickName = quickName;
        
        _filterProvider = (filterProvider == null ? new AlwaysAcceptingFilterProvider() : filterProvider);
        
        _actions = actions;
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
     * @param gradableEvent
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
    
    public List<Action> getActions()
    {
        return _actions;
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
            if(_gmlTemplate.exists() && _gmlTemplate.isFile() && _gmlTemplate.canRead())
            {
                double totalOutOf = 0;
                try
                {
                    InMemoryGML gml = GMLParser.parse(_gmlTemplate, this, null);
                    for(Section section : gml.getSections())
                    {
                        for(Subsection subsection : section.getSubsections())
                        {
                            totalOutOf += subsection.getOutOf();
                        }
                    }
                }
                //TODO: Figure out how to communicate this case of user error from an incorrectly formatted GML template
                catch(GradingSheetException e) { }
                
                _outOf = totalOutOf;
            }
            else
            {
                _outOf = 0D;
            }
        }
        
        return _outOf;
    }
    
    /**
     * Returns a convenient human readable string describing this part. The format is "[Assignment Name] - 
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
     * @throws IOException
     *
     * @param group
     * @param owner the graphical owner of any dialogs shown while unarchiving
     */
    void unarchive(Window owner, Group group) throws IOException
    {
        File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(this, group);
        if(!unarchiveDir.exists())
        {
            //Access the digital handin
            File handin = _gradableEvent.getDigitalHandin(group);
            if(handin == null || !handin.exists())
            {
                throw new IOException("Expected to be able to access digital handin\n" +
                        "Group: " + group + "\n" +
                        "Part: " + this.getFullDisplayName());
            }

            //Determine if all of files and directories that belong to this part are present in the archive
            //If not, then create a filter that accepts all files and directory, and notify the user
            FileFilter filter;
            Collection<ArchiveEntry> contents = Allocator.getArchiveUtilities().getArchiveContents(handin);
            StringBuilder builder = new StringBuilder();
            if(_filterProvider.areFilteredFilesPresent(contents, builder))
            {
                filter = _filterProvider.getFileFilter(unarchiveDir);
            }
            else
            {
                filter = new AlwaysAcceptingFileFilter();

                String msg = "Not all files and/or directories this part expected were found in the digital " +
                        "handin. All files and directories will now be included for this part so that you " +
                        "can find files that may be misnamed or placed in the wrong directory.\n\n" +
                        "The missing files and/or directories are:\n" + builder.toString();
                ModalDialog.showMessage(owner, group + "'s Digital Handin - Missing Expected Contents", msg);
            }

            //Extract
            try
            {
                Allocator.getArchiveUtilities().extractArchive(handin, unarchiveDir, filter,
                    Allocator.getCourseInfo().getTAGroup());
            }
            catch(ArchiveExtractionException e)
            {
                throw new IOException("Unable to extract digital handin\n" +
                        "Group: " + group + "\n" +
                        "Part: " + this.getFullDisplayName() + " \n" +
                        "Archive: " + handin.getAbsolutePath() + "\n" +
                        "Unarchive Directory: " + unarchiveDir.getAbsolutePath());
            }
        }
    }
    
    @Override
    public String toString()
    {
        return _name;
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
        int comparison = this.getGradableEvent().compareTo(p.getGradableEvent());
        if(comparison == 0)
        {
            comparison = ((Integer)this._order).compareTo(p._order);
        }
        
        return comparison;
    }
}