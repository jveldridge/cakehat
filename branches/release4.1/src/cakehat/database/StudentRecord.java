package cakehat.database;

/**
 * Represents information about a student as stored in the database.  Unlike the
 * {@link Student} object, instances of this class are not managed and are not 
 * guaranteed to be valid.  This class should used only as a return type for
 * {@link Database} methods supporting {@link DataServices} methods.
 * 
 * @author jeldridg
 */
class StudentRecord {
    
    private final int _dbId;
    private final String _login, _firstName, _lastName;
    private final boolean _isEnabled;

    StudentRecord(int dbId, String login, String firstName, String lastName, boolean isEnabled) {
        _dbId = dbId;
        _login = login;
        _firstName = firstName;
        _lastName = lastName;
        _isEnabled = isEnabled;
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

    public boolean isEnabled() {
        return _isEnabled;
    }
    
}
