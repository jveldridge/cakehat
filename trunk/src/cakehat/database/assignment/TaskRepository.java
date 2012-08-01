package cakehat.database.assignment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;

/**
 * Repository of all {@link Task} implementations.
 *
 * @author jak2
 */
public final class TaskRepository
{   
    private static final ImmutableSet<TaskProvider> PROVIDERS = ImmutableSet.of(
            new ApplicationTasks(),
            new ExternalTasks(),
            new JavaTasks(),
            new MatlabTasks(),
            new PythonTasks(),
            new PrintTasks(),
            new GradingGuideTasks(),
            new ReadmeTasks());
    
    private static final ImmutableMap<String, Task> TASKS = buildTaskMap();
    
    private static ImmutableMap<String, Task> buildTaskMap()
    {
        ImmutableMap.Builder<String, Task> tasksBuilder = ImmutableMap.builder();
        for(TaskProvider provider : PROVIDERS)
        {
            for(Task task : provider.getTasks())
            {
                tasksBuilder.put(task.getFullName(), task);
            }
        }
        
        return tasksBuilder.build();
    }

    private TaskRepository() { }
    
    /**
     * Returns an immutable map of all {@link Task}s from their names to their implementations.
     * 
     * @return 
     */
    public static Map<String, Task> getTasks()
    {
        return TASKS;
    }

    /**
     * Returns the {@link Task} implementation specified by {@code taskName}.
     *
     * @param taskName the full name of the task
     * @return task
     *
     * @throws IllegalArgumentException thrown if there is no task by the name of {@code taskName}
     */
    static Task getTask(String taskName)
    {
        Task task = TASKS.get(taskName);
        if(task == null)
        {
            throw new IllegalArgumentException("No task exists with name: " + taskName);
        }
        
        return task;
    }
}