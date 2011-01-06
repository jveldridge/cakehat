package gradesystem.config;

/**
 *
 * @author jak2
 */
public class TA implements Comparable<TA>
{
    private String _login, _name;
    private boolean _isDefaultGrader, _isAdmin, _isHTA;

    TA(String login, String name, boolean isDefaultGrader, boolean isAdmin, boolean isHTA)
    {
        _login = login;
        _name = name;
        _isDefaultGrader = isDefaultGrader;
        _isHTA = isHTA;
        _isAdmin = isAdmin;
    }

    public String getLogin()
    {
        return _login;
    }

    public String getName()
    {
        return _name;
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

    @Override
    public int compareTo(TA other) {
        return this.getLogin().compareTo(other.getLogin());
    }

    @Override
    public int hashCode() {
        return _login.hashCode();
    }

    @Override
    public boolean equals(Object o) {
       if (!(o instanceof TA)) {
           return false;
       }

       return this._login.equals(((TA) o).getLogin());
    }

}