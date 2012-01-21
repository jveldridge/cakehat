package cakehat.database;

import com.google.common.collect.ImmutableList;
import cakehat.config.Assignment;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents a group of students working together on an assignment.
 * Groups are compared and sorted based on Assignment and then group name.
 *
 * @author jak2
 * @author jeldridg
 */
@Deprecated
public class Group implements Comparable<Group>
{
    private final int _dbId;
    private final Assignment _assignment;
    private String _name;
    private List<Student> _members;
    private List<String> _memberLogins;

    Group(int dbId, Assignment asgn, String name, Collection<Student> members)
    {
        _dbId = dbId;
        _assignment = asgn;
        _name = name;
        _members = ImmutableList.copyOf(members);
        
        ImmutableList.Builder<String> loginsBuilder = ImmutableList.builder();
        for (Student member : _members) {
            loginsBuilder.add(member.getLogin());
        }
        _memberLogins = loginsBuilder.build();
    }

    Group(int dbId, Assignment asgn, String name, Student... members)
    {
        this(dbId, asgn, name, Arrays.asList(members));
    }
    
    int getDbId() {
        return _dbId;
    }
    
    public Assignment getAssignment() {
        return _assignment;
    }

    public String getName()
    {
        return _name;
    }

    public List<Student> getMembers()
    {
        return _members;
    }
    
    public List<String> getMemberLogins() {
        return _memberLogins;
    }

    public int size() {
        return _members.size();
    }
    
    /**
     * Updates the fields of this Group object to have the given values.
     * This method should be called only by {@link DataServices} to ensure
     * consistency with the database.
     * 
     * @param name
     * @param members
     */
    void update(String name, Collection<Student> members) {
        _name = name;
        _members = ImmutableList.copyOf(members);
        
        ImmutableList.Builder<String> loginsBuilder = ImmutableList.builder();
        for (Student member : _members) {
            loginsBuilder.add(member.getLogin());
        }
        _memberLogins = loginsBuilder.build();
    }

    @Override
    public String toString()
    {
        return _name;
    }

    @Override
    public int compareTo(Group g) {
        if (this.getAssignment().equals(g.getAssignment()))  {
            return this.getName().compareTo(g.getName());
        }
        return this.getAssignment().compareTo(g.getAssignment());
    }

}