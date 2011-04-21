package cakehat.database;

import cakehat.Allocator;
import cakehat.services.ServicesException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import support.utils.posix.NativeException;

/**
 * Implementation of {@link DataServices} interface.
 *
 * @author jeldridg
 */
public class DataServicesImpl implements DataServices {

    private Set<Student> _allStudents;
    @Override
    public Set<Student> getAllStudents() throws ServicesException {
        if (_allStudents == null) {
            try {
                _allStudents = new HashSet<Student>(Allocator.getDatabase().getStudents());
            } catch (SQLException ex) {
                throw new ServicesException("Could not retrieve students from the "
                        + "database.", ex);
            }
        }

        return Collections.unmodifiableSet(_allStudents);
    }

    private Set<Student> _enabledStudents;
    @Override
    public Set<Student> getEnabledStudents() throws ServicesException {
        if (_enabledStudents == null) {
            _enabledStudents = new HashSet<Student>();
            for (Student student : this.getAllStudents()) {
                if (student.isEnabled()) {
                    _enabledStudents.add(student);
                }
            }
        }

        return Collections.unmodifiableSet(_enabledStudents);
    }

    @Override
    public void addStudent(String studentLogin, ValidityCheck checkValidity) throws ServicesException {
        try {
            String name = Allocator.getUserUtilities().getUserName(studentLogin);
            String names[] = name.split(" ");
            String firstName = names[0];
            String lastName = names[names.length - 1];

            this.addStudent(studentLogin, firstName, lastName, checkValidity);
        } catch (NativeException e) {
            throw new ServicesException("Student will not be added to the database because " +
                                        "the user's real name cannot be retrieved", e);
        }
    }
    
    @Override
    public void addStudent(String studentLogin, String firstName, String lastName,
                           ValidityCheck checkValidity) throws ServicesException {
        if (checkValidity == ValidityCheck.CHECK) {
            String warningMessage = "";
            boolean isLoginValid = Allocator.getUserUtilities().isLoginValid(studentLogin);
            boolean isInStudentGroup = false;

            try {
                isInStudentGroup = Allocator.getUserServices().isInStudentGroup(studentLogin);
            } catch (NativeException e) {
                throw new ServicesException("Unable to retrieve student group", e);
            }

            if (!isLoginValid) {
                warningMessage += String.format("The login %s is not a valid login\n",
                                                studentLogin);
            }
            else if (!isInStudentGroup) {
                warningMessage += String.format("The login %s is not in the student group",
                                                studentLogin);
            }

            if (!isLoginValid || !isInStudentGroup) {
                Object[] options = {"Proceed", "Cancel"};
                int shouldContinue = JOptionPane.showOptionDialog(null, warningMessage,
                                                                  "Invalid Student Login",
                                                                  JOptionPane.OK_CANCEL_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE,
                                                                  null, options, options[0]);

                if (shouldContinue != JOptionPane.OK_OPTION) {
                    return;
                }
            }
        }

        try {
            boolean added = Allocator.getDatabase().addStudent(studentLogin, firstName, lastName);
            if (added) {
                Student newStudent = new Student(studentLogin, firstName, lastName, true);
                _allStudents.add(newStudent);
                _enabledStudents.add(newStudent);
            }
        } catch (SQLException e) {
            throw new ServicesException(String.format("Student %s (%s %s) could not "
                    + "be added to the database", studentLogin, firstName, lastName), e);
        }
    }

    public void disableStudent(Student student) throws ServicesException {
        try {
            Allocator.getDatabase().disableStudent(student.getLogin());
            _enabledStudents.remove(student);
        } catch (SQLException ex) {
            throw new ServicesException("Could not disable student " + student + ".", ex);
        }
    }

    public void enableStudent(Student student) throws ServicesException {
        try {
            Allocator.getDatabase().enableStudent(student.getLogin());
            _enabledStudents.add(student);
        } catch (SQLException ex) {
            throw new ServicesException("Could not enable student " + student + ".", ex);
        }
    }

}
