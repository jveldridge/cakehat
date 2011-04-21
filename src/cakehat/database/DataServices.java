package cakehat.database;

import cakehat.services.ServicesException;
import java.util.Set;

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
     * Returns an immutable Set containing a Student object for each student
     * in the database.  If the database contains no students, an empty Set
     * is returned.  The first call to this method in a cakehat session will result
     * in a database call; all other calls will return cached data.
     *
     * @return
     * @throws ServicesException
     */
    public Set<Student> getAllStudents() throws ServicesException;

    /**
     * Returns an immutable Set containing a Student object for each enabled
     * student in the database.  If the database contains no enabled students, an
     * empty Set is returned.  The first call to this method in a cakehat session
     * will result in a database call; all other calls will return cached data.
     *
     * @return
     * @throws ServicesException
     */
    public Set<Student> getEnabledStudents() throws ServicesException;

    /**
     * Adds the given studentLogin to the database.  A warning will be shown
     * if the given studentLogin is not a valid login or is not in the course's
     * student group; the user will then have the option of adding the student
     * anyway or cancelling the operation.  The students's first and last name
     * will be looked up.
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
     * will be set to the firstName and lastName parameters, respectively.
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
     * Marks the student as disabled; use instead of removing if a student has dropped
     * the course.  Disabled students will not be sent grade reports.
     *
     * @param student
     */
    public void disableStudent(Student student) throws ServicesException;

    /**
     * "Undo" of disabling a student.  All active students must be enabled to
     * receive grade reports.
     *
     * @param student
     */
    public void enableStudent(Student student) throws ServicesException;

}
