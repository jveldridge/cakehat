package cakehat.newdatabase;

import cakehat.Allocator;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.mail.internet.InternetAddress;

/**
 * Represents a student in a course. Each student in the course is represented by exactly one {@code Student} object in
 * memory over the course of cakehat's execution. Student objects are compared and ordered by login.
 * 
 * @author jak2
 * @author jeldridg
 */
public class Student implements Comparable<Student>
{   
    private final CopyOnWriteArrayList<StudentListener> _listeners = new CopyOnWriteArrayList<StudentListener>();
    
    public static interface StudentListener
    {
        public void studentChanged(Student student);
    }
    
    private final int _id;
    private final String _login, _firstName, _lastName;
    private volatile boolean _isEnabled, _hasCollabPolicy;

    Student(int id, String login, String firstName, String lastName, boolean isEnabled, boolean hasCollabPolicy)
    {
        _id = id;
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _isEnabled = isEnabled;
        _hasCollabPolicy = hasCollabPolicy;
    }
    
    public int getId()
    {
        return _id;
    }

    public String getLogin()
    {
        return _login;
    }

    public String getFirstName()
    {
        return _firstName;
    }

    public String getLastName()
    {
        return _lastName;
    }

    /**
     * Returns the student's name in format "[first_name] [last_name]".
     * @return
     */
    public String getName()
    {
        return _firstName + " " + _lastName;
    }

    public InternetAddress getEmailAddress()
    {
        try
        {
            return new InternetAddress(_login + "@" + Allocator.getConstants().getEmailDomain(), this.getName());
        }
        catch(UnsupportedEncodingException ex)
        {
            throw new RuntimeException("Unable to form valid email address for student " + _login, ex);
        }
    }

    public boolean isEnabled()
    {
        return _isEnabled;
    }

    /**
     * Sets the _isEnabled flag for this Student object.  Note that this does <strong>not</strong> change the student's
     * status in the database; this method should be used only to ensure consistency between the cached data and the
     * database after a successful database call.
     *
     * @param enabled
     */
    void setEnabled(boolean enabled)
    {
        _isEnabled = enabled;
        
        notifyListeners();
    }
    
    public boolean hasCollabPolicy()
    {
        return _hasCollabPolicy;
    }
    
    /**
     * Sets the _hasCollabPolicy flag for this Student object.  Note that this does <strong>not</strong> change the
     * student's status in the database; this method should be used only to ensure consistency between the cached data
     * and the database after a successful database call.
     * 
     * @param hasCollabPolicy 
     */
    void setHasCollabPolicy(boolean hasCollabPolicy)
    {
        _hasCollabPolicy = hasCollabPolicy;
        
        notifyListeners();
    }
    
    @Override
    public String toString()
    {
        return _login;
    }

    /**
     * Compares based on login
     * 
     * @param s
     * @return 
     */
    @Override
    public int compareTo(Student other)
    {
        return _login.compareTo(other.getLogin());
    }
    
    public void addStudentListener(StudentListener listener)
    {
        _listeners.add(listener);
    }
    
    public void removeStudentListener(StudentListener listener)
    {
        _listeners.remove(listener);
    }
    
    void notifyListeners()
    {
        for(StudentListener listener : _listeners)
        {
            listener.studentChanged(this);
        }
    }
    
    public static final Comparator<Student> NAME_COMPARATOR = new Comparator<Student>()
    {
        @Override
        public int compare(Student s1, Student s2)
        {
            int comparison;
            if(s1.getLastName().equals(s2.getLastName()))
            {
                comparison =  s1.getFirstName().compareTo(s2.getFirstName());
            }
            else
            {
                comparison = s1.getLastName().compareTo(s2.getLastName());
            }
            
            return comparison;
        }
    };   
}