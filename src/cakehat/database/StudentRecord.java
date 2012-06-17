package cakehat.database;

/**
 * Represents information about a student as stored in the database. Unlike the {@link Student} object, instances of
 * this class are not managed and are not guaranteed to be valid. This class should used only as a return type for
 * {@link Database} methods supporting {@link DataServices} methods.
 * 
 * @author jeldridg
 */
class StudentRecord {
    
    private final int _dbId;
    private final String _login, _firstName, _lastName, _email;
    private final boolean _isEnabled, _hasCollab;

    StudentRecord(int dbId, String login, String firstName, String lastName, String email, boolean isEnabled,
            boolean hasCollab) {
        _dbId = dbId;
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _email = email;
        _isEnabled = isEnabled;
        _hasCollab = hasCollab;
    }
    
    public int getDbId() {
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
    
    public String getEmail() {
        return _email;
    }

    public boolean isEnabled() {
        return _isEnabled;
    }
    
    public boolean hasCollab() {
        return _hasCollab;
    }   
}