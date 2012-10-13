package cakehat.assignment;

import java.awt.Window;

/**
 * The context in which an task is running.
 *
 * @author jak2
 */
class TaskContext
{
    private final Window _graphicalOwner;
    
    TaskContext(Window graphicalOwner)
    {
        _graphicalOwner = graphicalOwner;
    }
    
    Window getGraphicalOwner()
    {
        return _graphicalOwner;
    }
}