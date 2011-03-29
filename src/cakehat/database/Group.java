package cakehat.database;

import com.google.common.collect.ImmutableList;
import cakehat.Allocator;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A group of students.
 *
 * @author jak2
 */
public class Group implements Comparable<Group>
{
    private final String _name;
    private final List<String> _members;

    public Group(String name, Collection<String> members)
    {
        _name = name;
        _members = ImmutableList.copyOf(members);
    }

    public Group(String name, String... members)
    {
        this(name, Arrays.asList(members));
    }

    public String getName()
    {
        return _name;
    }

    public List<String> getMembers()
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