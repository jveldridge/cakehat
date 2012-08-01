package cakehat.database.assignment;

import cakehat.database.Group;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import java.io.File;
import java.util.Set;

/**
 * The externally visible effects of performing a task.
 *
 * @author jak2
 */
class TaskResult
{
    static final TaskResult NO_CHANGES = new TaskResult(ImmutableSetMultimap.<Group, File>of());
    
    private final SetMultimap<Group, File> _filesAdded;
    
    TaskResult(SetMultimap<Group, File> filesAdded)
    {
        _filesAdded = filesAdded;
    }
    
    TaskResult(Group group, Set<File> filesAdded)
    {
        _filesAdded = ImmutableSetMultimap.<Group, File>builder().putAll(group, filesAdded).build();
    }
    
    SetMultimap<Group, File> getFilesAdded()
    {
        return _filesAdded;
    }
}