package cakehat.database;

import cakehat.assignment.Assignment;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jak2
 */
public class DbGroup extends DbDataItem
{
    private final int _asgnId;
    private volatile String _name;
    private final Set<Integer> _studentIds;
    
    public DbGroup(Assignment asgn, Student student)
    {
        this(asgn, student.getName(), ImmutableSet.of(student));
    }
    
    public DbGroup(Assignment asgn, String name, Set<Student> students)
    {
        super(null);
        
        _asgnId = asgn.getId();
        _name = name;
        _studentIds = new HashSet<Integer>();
        for (Student student : students)
        {
            _studentIds.add(student.getId());
        }
    }
    
    DbGroup(int asgnId, int id, String name, Set<Integer> studentIds)
    {
        super(id);
        
        _asgnId = asgnId;
        _name = name;
        _studentIds = studentIds;
    }
    
    int getAssignmentId()
    {
        return _asgnId;
    }
    
    public void setName(String name)
    {
        _name = name;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void addMember(Student student)
    {
        synchronized(_studentIds)
        {
            _studentIds.add(student.getId());
        }
    }
    
    public void removeMember(Student student)
    {
        synchronized(_studentIds)
        {
            _studentIds.remove(student.getId());
        }
    }
    
    public Set<Integer> getMemberIds()
    {
        synchronized(_studentIds)
        {
            return Collections.unmodifiableSet(_studentIds);
        }
    }
    
}