package cakehat.newdatabase;

import cakehat.assignment.Assignment;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jak2
 */
public class DbGroup extends DbDataItem
{
    private final int _asgnId;
    private String _name;
    private final Set<Integer> _studentIds;
    
    public DbGroup(Assignment asgn)
    {
        super(false, null);
        
        _asgnId = asgn.getID();
        _studentIds = new HashSet<Integer>();
    }
    
    DbGroup(int asgnId, int id, String name, Set<Integer> studentIds)
    {
        super(true, id);
        
        _asgnId = asgnId;
        _name = name;
        _studentIds = studentIds;
    }
    
    int getAssignmentId()
    {
        return _asgnId;
    }
    
    public void setName(final String name)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _name = name;
            }
        });
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void addStudent(final Student student)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _studentIds.add(student.getId());
            }
        });
    }
    
    public void removeStudent(final Student student)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _studentIds.remove(student.getId());
            }
        });
    }
    
    //TODO: Figure out if the get students method should return student objects or ids
}