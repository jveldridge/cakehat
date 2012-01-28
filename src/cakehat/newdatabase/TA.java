package cakehat.newdatabase;

import cakehat.Allocator;
import java.io.UnsupportedEncodingException;
import javax.mail.internet.InternetAddress;

/**
 * Represents a TA or HTA in a course. Each TA or HTA in the course is represented by exactly one {@code TA} object in
 * memory over the course of cakehat's execution. TA objects are compared and ordered by login.
 *
 * @author jak2
 */
public class TA implements Comparable<TA>
{
    private final int _id;
    private final String _login;
    private final String _firstName;
    private final String _lastName;
    private final boolean _isDefaultGrader;
    private final boolean _isAdmin;
    
    TA(DbTA ta) {
        _id = ta.getId();
        _login = ta.getLogin();
        _firstName = ta.getFirstName();
        _lastName = ta.getLastName();
        _isDefaultGrader = ta.isDefaultGrader();
        _isAdmin = ta.isAdmin();
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
    
    public String getName()
    {
        return _firstName + " " + _lastName;
    }

    public boolean isDefaultGrader()
    {
        return _isDefaultGrader;
    }

    public boolean isAdmin()
    {
        return _isAdmin;
    }

    public InternetAddress getEmailAddress()
    {
        try
        {
            return new InternetAddress(_login + "@" + Allocator.getConstants().getEmailDomain(), this.getName());
        }
        catch(UnsupportedEncodingException ex)
        {
            throw new RuntimeException("Unable to form valid email address for TA " + _login, ex);
        }
    }

    @Override
    public String toString()
    {
        return _login;
    }

    /**
     * Compares against login.
     * 
     * @param other
     * @return 
     */
    @Override
    public int compareTo(TA other)
    {
        return this.getLogin().compareTo(other.getLogin());
    }
}