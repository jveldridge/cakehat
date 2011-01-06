package gradesystem.database;

import java.util.Arrays;
import java.util.Collection;

/**
 * A group of students.
 *
 * @author jak2
 */
public class Group
{
    private final String _name;
    private final Collection<String> _members;

    public Group(String name, Collection<String> members)
    {
        _name = name;
        _members = members;
    }

    public Group(String name, String... members)
    {
        this(name, Arrays.asList(members));
    }

    public String getName()
    {
        return _name;
    }

    public Collection<String> getMembers()
    {
        return _members;
    }

    @Override
    public String toString()
    {
        return _name;
    }
}
