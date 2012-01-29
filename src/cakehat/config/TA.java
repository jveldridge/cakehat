package cakehat.config;

import cakehat.Allocator;

/**
 *
 * @author jak2
 */
@Deprecated
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

    public String getEmailAddress() {
        return _login + "@" + Allocator.getConstants().getEmailDomain();
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

}