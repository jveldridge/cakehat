package cakehat.assignment;

import cakehat.database.Group;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;

/**
 * A task which operates on a part and groups.
 *
 * @author jak2
 */
public abstract class Task
{
    private final String _fullName;
    
    Task(TaskProvider provider, String taskName)
    {
        _fullName = provider.getNamespace() + ":" + taskName;
    }
    
    /**
     * The fully qualified name of this task.
     * 
     * @return 
     */
    public String getFullName()
    {
        return _fullName;
    }

    /**
     * A human readable description of the task. This description will be displayed to users in the cakehat
     * configuration manager.
     *
     * @return
     */
    public abstract String getDescription();
    
    /**
     * Returns a list of suggested action descriptions.
     * 
     * @return 
     */
    public abstract Set<ActionDescription> getSuggestedActionDescriptions();
    
    /**
     * Returns the properties this task either requires or optionally allows.
     * 
     * @return
     */
    abstract Set<TaskProperty> getProperties();
    
    /**
     * Returns the required properties of this task.
     * 
     * @return 
     */
    public Set<TaskProperty> getRequiredProperties()
    {
        ImmutableSet.Builder<TaskProperty> required = ImmutableSet.builder();
        for(TaskProperty prop : getProperties())
        {
            if(prop.isRequired())
            {
                required.add(prop);
            }
        }
       
        return required.build();
    }
    
    /**
     * Returns the optional properties of this task. 
     * 
     * @return 
     */
    public Set<TaskProperty> getOptionalProperties()
    {
       ImmutableSet.Builder<TaskProperty> optional = ImmutableSet.builder();
        for(TaskProperty prop : getProperties())
        {
            if(!prop.isRequired())
            {
                optional.add(prop);
            }
        }
       
        return optional.build(); 
    }
    
    /**
     * Whether the task operates on a digital handin.
     * 
     * @return 
     */
    public abstract boolean requiresDigitalHandin();

    /**
     * Whether the task is supported. If the task is not supported for the provided arguments then
     * {@link #performTask(Map<TaskProperty,String>, TaskContext, Action, Set<Group>)} should not be called with those
     * arguments.
     * 
     * @param action
     * @param groups
     * @return
     * @throws TaskException 
     */
    abstract boolean isTaskSupported(Action action, Set<Group> groups) throws TaskException;
    
    /**
     * Performs the task.
     * 
     * @param properties
     * @param context
     * @param action
     * @param groups
     * @throws TaskException 
     * @throws TaskConfigurationIssue
     */
    abstract void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action,
            Set<Group> groups) throws TaskException, TaskConfigurationIssue;
}