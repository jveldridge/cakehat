package config;

import java.util.Vector;

/**
 *
 * @author jak2
 */
public class TA
{
    private String _login;
    private boolean _isDefaultGrader, _isAdmin;
    private Vector<String> _blacklist = new Vector<String>();

    TA(String login, boolean isDefaultGrader, boolean isAdmin)
    {
        _login = login;
        _isDefaultGrader = isDefaultGrader;
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

    void addStudentToBlacklist(String studentLogin)
    {
        _blacklist.add(studentLogin);
    }

    public Iterable<String> getBlacklist()
    {
        return _blacklist;
    }
}