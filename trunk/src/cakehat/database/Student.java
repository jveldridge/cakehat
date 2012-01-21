package cakehat.database;

import cakehat.Allocator;
import java.util.Comparator;

/**
 * Represents a student in a course.  By default, Student objects are compared
 * and ordered by login.
 * 
 * @author jeldridg
 */
@Deprecated
public class Student implements Comparable<Student> {

    private final int _dbId;
    private String _login, _firstName, _lastName;
    private boolean _isEnabled;

    Student(int dbId, String login, String firstName, String lastName, boolean isEnabled) {
        _dbId = dbId;
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _isEnabled = isEnabled;
    }
    
    int getDbId() {
        return _dbId;
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

    public String getEmailAddress() {
        return _login + "@" + Allocator.getConstants().getEmailDomain();
    }

    public boolean isEnabled() {
        return _isEnabled;
    }

    /**
     * Sets the _isEnabled flag for this Student object.  Note that this does
     * <strong>not</strong> change the student's status in the database; this
     * method should be used only to ensure synchronization between the cached
     * data and the database after a successful database call.
     *
     * @param enabled
     */
    void setEnabled(boolean enabled) {
        _isEnabled = enabled;
    }

    /**
     * Updates the fields of this Student object to have the given values.
     * This method should be called only by {@link DataServices} to ensure
     * sychronization with the database. Returns true if any field of the
     * Student object has been changed, and false otherwise.
     * 
     * @param firstName
     * @param lastName
     * @param isEnabled
     * @return
     */
    boolean update(String firstName, String lastName, boolean isEnabled) {
        boolean hasChanged = false;
        if ((_firstName == null && firstName != null) || 
                (_firstName != null && !_firstName.equals(firstName)) ||
                (_lastName == null && lastName != null) ||
                (_lastName != null && !_lastName.equals(lastName)) ||
                _isEnabled != isEnabled) {
            hasChanged = true;
        }
        _firstName = firstName;
        _lastName = lastName;
        _isEnabled = isEnabled;
        
        return hasChanged;
    }

    @Override
    public String toString() {
        return _login;
    }

    @Override
    public int compareTo(Student t) {
        return _login.compareTo(t._login);
    }
    
    public static final Comparator<Student> NAME_COMPARATOR = new Comparator<Student>() {
        @Override
        public int compare(Student s1, Student s2) {
            if (s1.getLastName().equals(s2.getLastName())) {
                return s1.getFirstName().compareTo(s2.getFirstName());
            }
            else {
                return s1.getLastName().compareTo(s2.getLastName());
            }
        }
    };
    
}
