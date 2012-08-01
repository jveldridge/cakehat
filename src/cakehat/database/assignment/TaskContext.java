package cakehat.database.assignment;

import cakehat.Allocator;
import cakehat.database.Group;
import java.awt.Window;
import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

/**
 * The context in which an task is running.
 *
 * @author jak2
 */
class TaskContext
{
    private final Part _part;
    private final Action _action;
    private final Window _graphicalOwner;
    
    TaskContext(Part part, Action action, Window graphicalOwner)
    {
        _part = part;
        _action = action;
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
    
    Set<File> getFilesAddedForTask(Group group)
    {
        return _action.getFilesAdded(group);
    }
    
    /**
     * Returns a filter that does not accept files which belong to other tasks.
     * 
     * @param group
     * @return 
     */
    FileFilter getOtherTasksFilter(Group group)
    {
        final Set<File> otherTaskFiles = new HashSet<File>();
        for(Action action : _part.getActions())
        {
            if(action != _action)
            {
                otherTaskFiles.addAll(action.getFilesAdded(group));
            }
        }
        
        FileFilter filter = new FileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                return !otherTaskFiles.contains(file);
            }
        };
        
        return filter;
    }
}