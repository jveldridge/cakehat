package cakehat.database;

import java.io.UnsupportedEncodingException;
import java.util.Comparator;
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
    
    private final int _id;
    private final String _login, _firstName, _lastName;
    private final InternetAddress _emailAddress;
    
    Student(DbStudent student)
    {
        _id = student.getId();
        _login = student.getLogin();
        _firstName = student.getFirstName();
        _lastName = student.getLastName();
        
        try
        {
            _emailAddress = new InternetAddress(student.getEmailAddress(), this.getName());
        }
        catch(UnsupportedEncodingException ex)
        {
            throw new RuntimeException("Unable to form valid email address for student " + _login, ex);
        }
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
        return _emailAddress;
    }
    
    @Override
    public String toString()
    {
        return _login;
    }

    /**
     * Compares based on login
     * 
     * @param other
     * @return 
     */
    @Override
    public int compareTo(Student other)
    {   
        return _login.compareToIgnoreCase(other.getLogin());
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