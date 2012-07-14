package cakehat.database.assignment;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * The externally visible effects of performing an action.
 *
 * @author jak2
 */
class ActionResult
{
    static final ActionResult NO_CHANGES = new ActionResult(Collections.<File>emptySet());
    
    private final Set<File> _filesAdded;
    
    ActionResult(Set<File> filesAdded)
    {
        _filesAdded = filesAdded;
    }
    
    Set<File> getFilesAdded()
    {
        return _filesAdded;
    }
}