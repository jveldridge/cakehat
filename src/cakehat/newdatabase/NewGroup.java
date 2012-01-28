package cakehat.newdatabase;

import cakehat.assignment.Assignment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a group of students who will be working together on an Assignment.
 * Unlike instances of the {@link Group} object, instances of this class represent
 * groups that have not yet been stored in the database and thus may be changed
 * by editing their name or adding or removing members.
 *
 * @author jeldridg
 */
public class NewGroup
{
    private final Assignment _assignment;
    private String _name;
    private Set<Student> _members;

    public NewGroup(Assignment asgn, String name, Set<Student> members)
    {
        _assignment = asgn;
        _name = name;
        _members = members;
    }

    public NewGroup(Assignment asgn, String name, Student... members)
    {
        this(asgn, name, new HashSet<Student>(Arrays.asList(members)));
    }
    
    /**
     * Convenience constructor for creating a group of one.
     * 
     * @param asgn
     * @param student 
     */
    public NewGroup(Assignment asgn, Student student) {
        _assignment = asgn;
        _name = student.getLogin();
        _members = new HashSet<Student>(1);
        _members.add(student);
    }
    
    public Assignment getAssignment() {
        return _assignment;
    }

    public String getName()
    {
        return _name;
    }

    public Set<Student> getMembers()
    {
        return _members;
    }

    public int size() {
        return _members.size();
    }

    @Override
    public String toString()
    {
        return _name;
    }

}