package cakehat.config;

/**
 *
 * @author jak2
 */
public class TA implements Comparable<TA>
{
    private final String _login, _name;
    private final boolean _isDefaultGrader, _isAdmin;

    TA(String login, String name, boolean isDefaultGrader, boolean isAdmin)
    {
        _login = login;
        _name = name;
        _isDefaultGrader = isDefaultGrader;
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