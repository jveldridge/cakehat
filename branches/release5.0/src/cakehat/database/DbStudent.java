package cakehat.database;

/**
 *
 * @author jak2
 * @author jeldridg
 */
public class DbStudent extends DbDataItem
{
    private volatile String _login;
    private volatile String _firstName;
    private volatile String _lastName;
    private volatile String _emailAddress;
    private volatile boolean _enabled;
    private volatile boolean _hasCollabPolicy;
    
    public DbStudent(String login, String firstName, String lastName, String emailAddress)
    {
        super(null);
        
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _emailAddress = emailAddress;
        _enabled = true;
        _hasCollabPolicy = false;
    }
    
    DbStudent(int id, String login, String firstName, String lastName, String emailAddress, boolean enabled,
              boolean hasCollabPolicy)
    {
        super(id);
        
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _emailAddress = emailAddress;
        _enabled = enabled;
        _hasCollabPolicy = hasCollabPolicy;
    }
    
    public void setLogin(String login)
    {
        _login = login;
    }
    
    public void setFirstName(String firstName)
    {
        _firstName = firstName;
    }
    
    public void setLastName(String lastName)
    {
        _lastName = lastName;
    }
    
    public void setEmailAddress(String emailAddress)
    {
        _emailAddress = emailAddress;
    }
    
    public void setEnabled(boolean enabled)
    {
        _enabled = enabled;
    }
    
    public void setHasCollabPolicy(boolean hasCollabPolicy)
    {
        _hasCollabPolicy = hasCollabPolicy;
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
    
    public String getEmailAddress()
    {
        return _emailAddress;
    }

    public boolean isEnabled()
    {
        return _enabled;
    }

    public boolean hasCollabPolicy()
    {
        return _hasCollabPolicy;
    }
}