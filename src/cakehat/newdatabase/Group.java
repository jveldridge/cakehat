package cakehat.newdatabase;

import cakehat.assignment.Assignment;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents a group of students working together on an assignment. Each group for an assignment is represented by
 * exactly one {@code Group} object in memory over the course of cakehat's execution. Groups are compared and sorted
 * based on {@link Assignment} and then group name.
 *
 * @author jak2
 * @author jeldridg
 */
public class Group implements Comparable<Group>, Iterable<Student>
{
    private final CopyOnWriteArrayList<GroupListener> _listeners = new CopyOnWriteArrayList<GroupListener>();
    
    public static interface GroupListener
    {
        public void groupChanged(Group group);
    }
    
    private final int _id;
    private final Assignment _assignment;
    private volatile String _name;
    private final Set<Student> _members;

    Group(int id, Assignment asgn, String name, Set<Student> members)
    {
        _id = id;
        _assignment = asgn;
        _name = name;
        _members = new CopyOnWriteArraySet<Student>(members);
    }
    
    public int getId()
    {
        return _id;
    }
    
    public Assignment getAssignment()
    {
        return _assignment;
    }

    public String getName()
    {
        return _name;
    }

    void setName(String name)
    {
        _name = name;
        
        notifyListeners();
    }
    
    public Set<Student> getMembers()
    {
        return Collections.unmodifiableSet(_members);
    }
    
    void addStudent(Student student)
    {
        _members.add(student);
        
        notifyListeners();
    }
    
    void removeStudent(Student student)
    {
        _members.remove(student);
        
        notifyListeners();
    }
    
    public int size()
    {
        return _members.size();
    }
    
    public boolean isGroupOfOne()
    {
        return _members.size() == 1;
    }
    
    /**
     * If this group has exactly 1 member, that student will be returned. Otherwise an {@link IllegalStateException}
     * will be thrown.
     * 
     * @throws IllegalStateException if not 1 member
     * @return student
     */
    public Student getOnlyMember()
    {
        if(_members.size() != 1)
        {
            throw new IllegalStateException("Attempted to retrieve only member from a group with multiple students.\n" +
                    "Assignment: " + _assignment.getName() + "\n" + 
                    "Group Members: " + _members + "\n" + 
                    "Group ID: " + _id);
        }
        
        return _members.iterator().next();
    }

    @Override
    public String toString()
    {
        return _name;
    }

    @Override
    public int compareTo(Group g)
    {
        if(this.getAssignment().equals(g.getAssignment()))
        {
            return this.getName().compareTo(g.getName());
        }
        return this.getAssignment().compareTo(g.getAssignment());
    }

    public Iterator<Student> iterator()
    {
        return _members.iterator();
    }
    
    public void addGroupListener(GroupListener listener)
    {
        _listeners.add(listener);
    }
    
    public void removeGroupListener(GroupListener listener)
    {
        _listeners.remove(listener);
    }
    
    void notifyListeners()
    {
        for(GroupListener listener : _listeners)
        {
            listener.groupChanged(this);
        }
    }
    
    /**
     * Updates the fields of this Group object to have the given values.
     * This method should be called only by {@link DataServices} to ensure
     * consistency with the database.
     * 
     * @param name
     * @param members
     */
    void update(String name, Set<Student> members) {
        _name = name;
        _members.clear();
        _members.addAll(members);
    }
    
}