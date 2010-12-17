package gradesystem.components;

/**
 * Represents a Student (name and login only)
 *
 * @author aunger
 */
public class Student implements Comparable<Student> {

    private String _login;
    private String _firstName;
    private String _lastName;

    public Student(String login, String firstName, String lastName) {
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
    }

    @Override
    public String toString() {
        return _lastName + ", " + _firstName + " (" + _login + ")";
    }

    public String getLogin() {
        return _login;
    }

    public String getName() {
        return _firstName + " " + _lastName;
    }

    public String getLastName() {
        return _lastName;
    }

    public String getFirstName() {
        return _firstName;
    }

    @Override
    public int compareTo(Student o) {
        return this._lastName.compareTo(o._lastName);
    }
}
