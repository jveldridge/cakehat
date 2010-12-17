package gradesystem.config;

/**
 *
 * @author jak2
 */
public class TA implements Comparable<TA>
{
    private String _login;
    private boolean _isDefaultGrader, _isAdmin, _isHTA;

    TA(String login, boolean isDefaultGrader, boolean isAdmin, boolean isHTA)
    {
        _login = login;
        _isDefaultGrader = isDefaultGrader;
        _isHTA = isHTA;
        _isAdmin = isAdmin;
    }

    public String getLogin()
    {
        return _login;
    }

    public boolean isDefaultGrader()
    {
        return _isDefaultGrader;
    }

    public boolean isAdmin()
    {
        return _isAdmin;
    }

    public boolean isHTA()
    {
        return _isHTA;
    }

    @Override
    public String toString()
    {
        return _login;
    }

    public int compareTo(TA other) {
        return this.getLogin().compareTo(other.getLogin());
    }
}