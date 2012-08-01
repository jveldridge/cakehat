package cakehat.database.assignment;

import java.util.Set;

/**
 * A provider of {@link Task}s.
 *
 * @author jak2
 */
interface TaskProvider
{
    /**
     * This name should describe the types of tasks provided, such as "java" or "matlab". It will become the prefix
     * for the names of the task descriptions provided.
     * 
     * @return
     */
    public String getNamespace();

    /**
     * The {@link Task}s provided by this TaskProvider.
     *
     * @return
     */
    public Set<? extends Task> getTasks();
}