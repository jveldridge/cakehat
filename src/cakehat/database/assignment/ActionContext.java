package cakehat.database.assignment;

import cakehat.Allocator;
import cakehat.database.Group;
import java.awt.Window;
import java.io.File;

/**
 * The context in which an action is running.
 *
 * @author jak2
 */
class ActionContext
{
    private final Part _part;
    private final Window _graphicalOwner;
    
    ActionContext(Part part, Window graphicalOwner)
    {
        _part = part;
        _graphicalOwner = graphicalOwner;
    }
    
    Window getGraphicalOwner()
    {
        return _graphicalOwner;
    }
    
    File getUnarchiveHandinDir(Group group)
    {
        return Allocator.getPathServices().getUnarchiveHandinDir(_part, group);
    }
}