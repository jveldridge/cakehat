package cakehat.assignment;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import support.resources.icons.IconLoader.IconImage;
import support.ui.ModalDialog;
import support.utils.FileDeletingException;

/**
 *
 * @author jak2
 */
public class Action implements Comparable<Action>
{
    private final int _id;
    
    /**
     * The course provided name for this action. Shown in various UI.
     */
    private final String _name;
    
    /**
     * The course chosen icon for this action. Shown in various UI.
     */
    private final IconImage _icon;
    
    private final int _order;
    
    private final Task _task;
    private final Map<TaskProperty, String> _taskProperties;
    private final boolean _requiredPropertiesPresent;
    
    /**
     * This value will be set after construction because the part object will not be constructed until after the
     * construction of this object, and both objects need to know about each other. Because other threads will be
     * accessing this field, to ensure visibility this value must be volatile.
     */
    private volatile Part _part;
    
    Action(int id, String name, IconImage icon, int order, Task task, Map<TaskProperty, String> taskProperties)
    {
        if(name == null)
        {
            throw new NullPointerException("name may not be null");
        }
        if(icon == null)
        {
            throw new NullPointerException("icon may not be null");
        }
        if(taskProperties == null)
        {
            throw new NullPointerException("taskProperties may not be null");
        }
        
        _id = id;
        _name = name;
        _icon = icon;
        _order = order;
        
        _task = task;
        _taskProperties = taskProperties;
        
        //Determine if task can be run
        boolean requiredPropertiesPresent = true;
        if(_task != null)
        {
            for(TaskProperty requiredProperty : _task.getRequiredProperties())
            {
                if(!taskProperties.containsKey(requiredProperty))
                {
                    requiredPropertiesPresent = false;
                }
            }
        }
        _requiredPropertiesPresent = requiredPropertiesPresent;
    }
    
    /**
     * The name of this action.
     * 
     * @return 
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * The icon of this action.
     * 
     * @return 
     */
    public IconImage getIcon()
    {
        return _icon;
    }
    
    /**
     * A unique identifier for this action.
     * 
     * @return 
     */
    public int getId()
    {
        return _id;
    }
        
    /**
     * Sets the Part this action belongs to.
     * 
     * @param part
     * @throws NullPointerException if {@code part} is {@code null}
     * @throws IllegalStateException if this method has been called before for this instance
     */
    void setPart(Part part)
    {
        if(part == null)
        {
            throw new NullPointerException("Action cannot belong to a null Part");
        }
        
        if(_part != null)
        {
            throw new IllegalStateException("Part may only be set once");
        }
        
        _part = part;
    }
    
    /**
     * The Part this action belongs to.
     * 
     * @return 
     * @throws IllegalStateException if the Part this action belongs to has not yet been set
     */
    public Part getPart()
    {
        if(_part == null)
        {
            throw new IllegalStateException("Part has not yet been set");
        }
        
        return _part;
    }
    
    /**
     * Returns a string for use in debugging and exception messages. Format is:
     * [Assignment Name] - [Gradable Event Name] - [Part Name] - [Action Name] ([Assignment Id] - [Gradable Event Id] -
     * [Part Id] - [Action Id])
     * 
     * @return 
     */
    String getDebugName()
    {
        return this.getPart().getAssignment().getName() + " - " +
               this.getPart().getGradableEvent().getName() + " - " +
               this.getPart().getName() + " - " +
               this.getName() + " (" +
               this.getPart().getAssignment().getId() + " - " +
               this.getPart().getGradableEvent().getId() + " - " +
               this.getPart().getId() + " - " +
               this.getId() + ")";
    }
    
    /**
     * Whether the task supports the specified {@code groups}.
     * 
     * @param groups
     * @return
     * @throws TaskException 
     */
    public boolean isTaskSupported(Set<Group> groups) throws TaskException
    {
        boolean supported = false;
        
        if(_task != null && _requiredPropertiesPresent)
        {
            Set<Group> groupsWithHandins = new HashSet<Group>();
            for(Group group : groups)
            {
                try
                {
                    if(this.getPart().getGradableEvent().hasDigitalHandin(group))
                    {
                        groupsWithHandins.add(group);
                    }
                }
                catch(IOException e)
                {
                    throw new TaskException("Unable to determine if a digital handin exists\n" +
                            "Part: " + this.getPart().getFullDisplayName() + "\n" +
                            "Group: " + group, e);
                }
            }

            supported = _task.isTaskSupported(this, groupsWithHandins);
        }
        
        return supported;
    }
    
    /**
     * Performs the task. A task should not be performed if {@link #isTaskSupported(Set<Group>)} returns {@code false}.
     * 
     * @param graphicalOwner
     * @param groups
     * @throws TaskException 
     */
    public void performTask(Window graphicalOwner, Set<Group> groups) throws TaskException
    {   
        if(_task == null)
        {
            throw new TaskException("Action has no task specified\n" +
                    "Action: " + this.getName() + "\n" +
                    "Part: " + this.getPart().getFullDisplayName());
        }
        else if(!_requiredPropertiesPresent)
        {
            throw new TaskException("Action does not have all of its task's required properties specified\n" +
                    "Action: " + this.getName() + "\n" +
                    "Task: " + _task.getFullName() + "\n" +
                    "Task's Required Properties: " + _task.getRequiredProperties() + "\n" +
                    "Part: " + this.getPart().getFullDisplayName());
        }
        else
        {
            boolean proceed = true;
            Set<Group> taskGroups = groups;

            //Unarchive each group's handin, keep track of those with missing handins
            if(_task.requiresDigitalHandin())
            {
                Set<Group> groupsWithMissingHandins = new HashSet<Group>();
                for(Group group : groups)
                {
                    try
                    {
                        if(!this.getPart().getGradableEvent().hasDigitalHandin(group))
                        {
                            groupsWithMissingHandins.add(group);
                        }
                        else
                        {
                            this.getPart().unarchive(graphicalOwner, group);
                        }
                    }
                    catch(IOException e)
                    {
                        throw new TaskException("Unable to determine if a digital handin exists\n" +
                                "Part: " + this.getPart().getFullDisplayName() + "\n" +
                             "Group: " + group, e);
                    }
                }

                if(!groupsWithMissingHandins.isEmpty())
                {
                    proceed = resolveMissingHandins(graphicalOwner, groupsWithMissingHandins);
                    if(proceed)
                    {   
                        taskGroups = new HashSet<Group>();
                        taskGroups.addAll(groups);
                        taskGroups.removeAll(groupsWithMissingHandins);
                    }
                }
            }

            if(proceed)
            {
                //Check the task is supported before running it
                if(!_task.isTaskSupported(this, taskGroups))
                {
                    throw new IllegalArgumentException(this.getPart().getFullDisplayName() + " does not support " +
                            "groups " + taskGroups);
                }
                
                this.setupActionTempDir(groups);
                
                try
                {
                    _task.performTask(_taskProperties, new TaskContext(graphicalOwner), this, taskGroups);
                }
                catch(TaskConfigurationIssue issue)
                {
                    ModalDialog.showMessage(graphicalOwner, "Configuration Issue", issue.getMessage());
                }
            }
        }
    }
    
    private boolean resolveMissingHandins(Window owner, Set<Group> groupsWithMissingHandins)
    {
        String groupsOrStudents = (this.getPart().getAssignment().hasGroups() ? "groups" : "students");

        String message = "The following " + groupsOrStudents + " are missing handins:";
        List<Group> sortedGroupsWithMissingHandins = new ArrayList<Group>(groupsWithMissingHandins);
        Collections.sort(sortedGroupsWithMissingHandins);
        for(Group group : sortedGroupsWithMissingHandins)
        {
            message += "\n • " + group.getName();
        }
        message += "\n\nDo you want to proceed for the " + groupsOrStudents + " with handins?";

        return ModalDialog.showConfirmation(owner, "Missing Handins", message, "Proceed", "Cancel");
    }
    
    private void setupActionTempDir(Set<Group> groups) throws TaskException
    {
        //Build a set with all of the groups and null, null is used when operating on no groups (ex. a demo task)
        HashSet<Group> groupsAndNull = new HashSet<Group>();
        groupsAndNull.addAll(groups);
        groupsAndNull.add(null);
        
        //For each group create an empty temp directory
        for(Group group : groupsAndNull)
        {
            File groupTempDir = Allocator.getPathServices().getActionTempDir(this, group);
            try
            {
                if(groupTempDir.exists())
                {
                    Allocator.getFileSystemUtilities().deleteFiles(ImmutableSet.of(groupTempDir));
                }
                Allocator.getFileSystemServices().makeDirectory(groupTempDir);
            }
            catch(FileDeletingException e)
            {
                throw new TaskException("Unable to delete action temp dir\n" +
                        "Action: " + this.getDebugName() + "\n" +
                        "Group: " + group + "\n" +
                        "Directory: " + groupTempDir, e);
            }
            catch(ServicesException e)
            {
                throw new TaskException("Unable to create action temp dir\n" +
                        "Action: " + this.getDebugName() + "\n" +
                        "Group: " + group + "\n" +
                        "Directory: " + groupTempDir, e);
            }
        }
    }
    
    @Override
    public String toString()
    {
        return _name;
    }
    
    public boolean requiresDigitalHandin()
    {
        return (_task != null) && _task.requiresDigitalHandin();
    }
    
    /**
     * Compares this Action to another based on its ordering.
     * 
     * @param a
     * @return
     */
    @Override
    public int compareTo(Action a)
    {
        int comparison = this.getPart().compareTo(a.getPart());
        if(comparison == 0)
        {
            comparison = ((Integer)this._order).compareTo(a._order);
        }
        
        return comparison;
    }
}