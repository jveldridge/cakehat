package cakehat.config.handin;

import cakehat.database.Group;

/**
 * Represents a handin that is expected to exist but no longer does. This does
 * <strong>not</strong> represent a failure of cakehat, but instead the course
 * staff having deleted or moved the handin.
 *
 * @author jak2
 */
public class MissingHandinException extends Exception
{
    private final Group _group;

    MissingHandinException(Group group)
    {
        super("The handin for the group: " + group + " could not be found. " +
                "This could be because the file was moved or you do not have " +
                "access to that file.");

        _group = group;
    }

    public Group getGroup()
    {
        return _group;
    }
}