package gradesystem.database;

import gradesystem.Allocator;
import java.util.Arrays;
import java.util.Collection;

/**
 * A group of students.
 *
 * @author jak2
 */
public class Group implements Comparable<Group>
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

    @Override
    public int compareTo(Group t) {
        return this.getName().compareTo(t.getName());
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Group) {
            Group group = (Group) o;
            return this.getName().equals(group.getName()) &&
                    Allocator.getGeneralUtilities().containSameElements(this.getMembers(), group.getMembers());
        }

        return false;
    }

}
