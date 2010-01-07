package config;

import java.util.Vector;

/**
 * Represents a TA for a course.
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

    /**
     * TA's login.
     *
     * @return
     */
    public String getLogin()
    {
        return _login;
    }

    /**
     * Whether the TA should by default be distributed grading.
     *
     * @return
     */
    public boolean isDefaultGrader()
    {
        return _isDefaultGrader;
    }

    /**
     * Whether this TA is an admin.
     *
     * @return
     */
    public boolean isAdmin()
    {
        return _isAdmin;
    }

    /**
     * Adds a student to the blacklist.
     *
     * @param studentLogin
     */
    void addStudentToBlacklist(String studentLogin)
    {
        _blacklist.add(studentLogin);
    }

    /**
     * Gets a list of student logins the TA has blacklisted.
     *
     * @return
     */
    public Iterable<String> getBlacklist()
    {
        return _blacklist;
    }
}