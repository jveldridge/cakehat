package cakehat.newdatabase;

/**
 *
 * @author jak2
 * @author jeldridg
 */
public class DbStudent extends DbDataItem
{
    private String _login;
    private String _firstName;
    private String _lastName;
    private boolean _enabled;
    private boolean _hasCollabPolicy;
    
    public DbStudent()
    {
        super(false, null);
    }
    
    DbStudent(int id, String login, String firstName, String lastName, boolean enabled, boolean hasCollabPolicy)
    {
        super(true, id);
        
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _enabled = enabled;
        _hasCollabPolicy = hasCollabPolicy;
    }
    
    public void setLogin(final String login)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _login = login;
            }
        });
    }
    
    public void setFirstName(final String firstName)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _firstName = firstName;
            }
        });
    }
    
    public void setLastName(final String lastName)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _lastName = lastName;
            }
        });
    }
    
    public void setEnabled(final boolean enabled)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _enabled = enabled;
            }
        });
    }
    
    public void setHasCollabPolicy(final boolean hasCollabPolicy)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _hasCollabPolicy = hasCollabPolicy;
            }
        });
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

    public boolean isEnabled()
    {
        return _enabled;
    }

    public boolean isHasCollabPolicy()
    {
        return _hasCollabPolicy;
    }
}