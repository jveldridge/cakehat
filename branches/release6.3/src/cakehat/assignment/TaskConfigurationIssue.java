package cakehat.assignment;

/**
 * An issue with how the course configured a task that prevents the task from proceeding. For example, a task needs to
 * show a file, but the file does not exist or cannot be accessed.
 * 
 * @author jak2
 */
class TaskConfigurationIssue extends Exception
{
    TaskConfigurationIssue(String message)
    {
        super(message);
    }
}