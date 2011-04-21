package cakehat.database;

/**
 * Represents a student in a course.
 * 
 * @author jeldridg
 */
public class Student implements Comparable<Student> {

    private final String _login, _firstName, _lastName;
    private final boolean _isEnabled;

    Student(String login, String firstName, String lastName, boolean isEnabled) {
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _isEnabled = isEnabled;
    }

    public String getLogin() {
        return _login;
    }

    public String getFirstName() {
        return _firstName;
    }

    public String getLastName() {
        return _lastName;
    }

    /**
     * Returns the student's name in format "<first_name> <last_name>"
     * @return
     */
    public String getName() {
        return _firstName + " " + _lastName;
    }

    public boolean isEnabled() {
        return _isEnabled;
    }

    @Override
    public String toString() {
        return _login;
    }

    public int compareTo(Student t) {
        return _login.compareTo(t._login);
    }

}
