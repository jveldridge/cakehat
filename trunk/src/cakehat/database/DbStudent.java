package cakehat.database;

import java.util.Collections;

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
    private volatile boolean _hasCollabContract;
    
    public DbStudent(String login, String firstName, String lastName, String emailAddress)
    {
        super(null);
        
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _emailAddress = emailAddress;
        _enabled = true;
        _hasCollabContract = false;
    }
    
    DbStudent(int id, String login, String firstName, String lastName, String emailAddress, boolean enabled,
              boolean hasCollabContract)
    {
        super(id);
        
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _emailAddress = emailAddress;
        _enabled = enabled;
        _hasCollabContract = hasCollabContract;
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
    
    public void setHasCollabContract(boolean hasCollabContract)
    {
        _hasCollabContract = hasCollabContract;
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

    public boolean hasCollabContract()
    {
        return _hasCollabContract;
    }
    
}