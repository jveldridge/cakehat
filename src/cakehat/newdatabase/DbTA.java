package cakehat.newdatabase;

/**
 *
 * @author jak2
 * @author jeldridg
 */
public class DbTA extends DbDataItem
{
    private volatile String _login;
    private volatile String _firstName;
    private volatile String _lastName;
    private volatile boolean _isDefaultGrader;
    private volatile boolean _isAdmin;
    
    public DbTA(int uid, String login, String firstName, String lastName, boolean isDefaultGrader, boolean isAdmin)
    {
        super(uid);
        
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _isDefaultGrader = isDefaultGrader;
        _isAdmin = isAdmin;
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
    
    public void setIsDefaultGrader(boolean isDefaultGrader)
    {
        _isDefaultGrader = isDefaultGrader;
    }
    
    public void setIsAdmin(boolean isAdmin)
    {
        _isAdmin = isAdmin;
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