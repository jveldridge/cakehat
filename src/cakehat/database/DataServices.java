package cakehat.database;

import cakehat.config.TA;
import cakehat.services.ServicesException;
import java.util.Collection;

/**
 * Services methods relating to fundamental data.  These methods are ultimately
 * backed by the database (or other data store) and the methods of {@link DatabaseIO},
 * but are expected to implement caching.  In addition, these methods may perform
 * additional logic, including calls to other Services classes, to perform data validation
 * and to ensure synchronization with the backing data source.
 * <br/><br/>
 * <b>Methods in this class should be preferred to those in <code>DatabaseIO</code></b>
 * 
 * @author jeldridg
 */
public interface DataServices {

    public enum ValidityCheck {BYPASS, CHECK};

    /**
     * Returns an immutable Collection containing a Student object for each student
     * in the database at the time {@link DataServices#updateDataCache()} was called.
     * If the database contained no students, an empty Collection is returned.  Note
     * that the returned Collection is like a Set in that all elements are unique.
     * 
     * @param slcl
     * @return
     */
    public Collection<Student> getAllStudents();

    /**
     * Returns an immutable Collection containing a Student object for each enabled
     * student in the database at the time {@link DataServices#updateDataCache()} 
     * was called.  If the database contained no enabled students, an empty
     * Collection is returned.  Note that the returned Collection is like a Set
     * in that all elements are unique.
     *
     * @return
     */
    public Collection<Student> getEnabledStudents();

    /**
     * Adds the given studentLogin to the database.  A warning will be shown
     * if the given studentLogin is not a valid login or is not in the course's
     * student group; the user will then have the option of adding the student
     * anyway or cancelling the operation.  The students's first and last name
     * will be looked up.  If a student with the given login is already in the
     * database, this method has no effect.
     *
     * @param studentLogin
     * @param checkValidity parameter that indicates whether the student should be
     *                    added to the database without checking that the login is
     *                    valid and that the the student is in the course student
     *                    group.  This should be passed as BYPASS when both of
     *                    these conditions are known to be true (for example, when
     *                    adding all members of the course group)
     */
    public void addStudent(String studentLogin, ValidityCheck checkValidity) throws ServicesException;

    /**
     * Adds the given studentLogin to the database.  A warning will be shown
     * if the given studentLogin is not a valid login or is not in the course's
     * student group; the user will then have the option of adding the student
     * anyway or cancelling the operation.  The students's first and last name
     * will be set to the firstName and lastName parameters, respectively.  If a
     * student with the given login is already in the database, this method has
     * no effect.
     *
     * @param studentLogin
     * @param firstName
     * @param lastName
     * @param checkValidity parameter that indicates whether the student should be
     *                    added to the database without checking that the login is
     *                    valid and that the the student is in the course student
     *                    group.  This should be passed as BYPASS when both of
     *                    these conditions are known to be true (for example, when
     *                    adding all members of the course group)
     */
    public void addStudent(String studentLogin, String firstName, String lastName,
                           ValidityCheck checkValidity) throws ServicesException;

    /**
     * Sets the given Student's enabled status.  Students are enabled by default,
     * but students who have dropped the course should be disabled.  Disabled students
     * will not be sent grade reports.
     *
     * @param student
     * @param enabled
     * @throws ServicesException
     */
    public void setStudentEnabled(Student student, boolean enabled) throws ServicesException;
    
    public Collection<Student> getBlacklistedStudents() throws ServicesException;
    
    public Collection<Student> getTABlacklist(TA ta) throws ServicesException;

    @Deprecated
    /**
     * Temporary method for getting Student objects from student IDs.  This will
     * be removed once Group objects are converted such that there is only one
     * instance per group.
     */
    public Student getStudentFromID(int id);
    
    /**
     * Returns the Student object corresponding to the given studentLogin.
     * If no such student exists in the database, <code>null</code> will be
     * returned.
     *
     * @param studentLogin
     * @return
     */
    public Student getStudentFromLogin(String studentLogin);

    /**
     * Returns whether or not the given student login corresponds to
     * a valid Student object.
     * 
     * @param studentLogin
     * @return
     */
    public boolean isStudentLoginInDatabase(String studentLogin);
    
    /**
     * Loads Student objects into memory for all students in the database.
     *
     * @throws ServicesException
     */
    public void updateDataCache() throws ServicesException;

}
