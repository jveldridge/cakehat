package cakehat.assignment;

import cakehat.database.Group;

/**
 * Represents a digital handin that is expected to exist but no longer does. This does <strong>not</strong> represent a
 * failure of cakehat, but instead the course staff having deleted or moved the handin or a failure of the file system.
 *
 * @author jak2
 */
public class MissingHandinException extends Exception
{
    private final Group _group;

    MissingHandinException(Group group)
    {
        super("The handin for " + (group.getAssignment().hasGroups() ? "group" : "student") + " [" + group.getName() +
              "] could not be found. This could because the file was moved, you do not have access to the file, or " +
              "there is an issue with the file system.");

        _group = group;
    }

    public Group getGroup()
    {
        return _group;
    }
}