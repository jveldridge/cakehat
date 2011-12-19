package cakehat.newdatabase;

/**
 *
 * @author jak2
 * @author jeldridg
 */
public class DbTA extends DbDataItem
{
    private String _login;
    private String _firstName;
    private String _lastName;
    private boolean _isDefaultGrader;
    private boolean _isAdmin;
    
    public DbTA()
    {
        super(false, null);
    }
    
    DbTA(int id, String login, String firstName, String lastName, boolean isDefaultGrader, boolean isAdmin)
    {
        super(true, id);
        
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _isDefaultGrader = isDefaultGrader;
        _isAdmin = isAdmin;
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
    
    public void setDefaultGrader(final boolean isDefaultGrader)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _isDefaultGrader = isDefaultGrader;
            }
        });
    }
    
    public void setIsAdmin(final boolean isAdmin)
    {
        updateUnderLock(new Runnable()
        {
            public void run()
            {
                _isAdmin = isAdmin;
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

    public boolean isDefaultGrader()
    {
        return _isDefaultGrader;
    }

    public boolean isAdmin()
    {
        return _isAdmin;
    }
}