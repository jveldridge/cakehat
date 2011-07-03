package cakehat.database;

import cakehat.Allocator;
import cakehat.config.TA;
import cakehat.services.ServicesException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JOptionPane;
import support.utils.posix.NativeException;

/**
 * Implementation of {@link DataServices} interface.
 *
 * @author jeldridg
 */
public class DataServicesImpl implements DataServices {

    /**
     * Maps a student's ID in the database to the corresponding Student object.
     */
    private final Map<Integer, Student> _idMap = new HashMap<Integer, Student>();
    
    /**
     * Maps a student's login to the corresponding Student object.
     */
    private final Map<String, Student> _loginMap = new HashMap<String, Student>();
    
    private final Set<Student> _enabledStudents = new HashSet<Student>();

    @Override
    public Collection<Student> getAllStudents() {
        return Collections.unmodifiableCollection(_idMap.values());
    }

    
    @Override
    public Collection<Student> getEnabledStudents() {
        return Collections.unmodifiableCollection(_enabledStudents);
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
            int addedID = Allocator.getDatabase().addStudent(studentLogin, firstName, lastName);
            if (addedID != 0) {
                Student newStudent = new Student(addedID, studentLogin, firstName, lastName, true);
                _idMap.put(addedID, newStudent);
                _loginMap.put(studentLogin, newStudent);
                _enabledStudents.add(newStudent);
            }
        } catch (SQLException e) {
            throw new ServicesException(String.format("Student %s (%s %s) could not "
                    + "be added to the database", studentLogin, firstName, lastName), e);
        }
    }

    @Override
    public void setStudentEnabled(Student student, boolean setEnabled) throws ServicesException {
        if (setEnabled) {       //attempt to enable student
            try {
                Allocator.getDatabase().enableStudent(student.getDbId());
                _enabledStudents.add(student);
                student.setEnabled(true);
            } catch (SQLException ex) {
                throw new ServicesException("Could not enable student " + student + ".", ex);
            }
        }
        else {                  //attempt to disable student
            try {
                Allocator.getDatabase().disableStudent(student.getDbId());
                _enabledStudents.remove(student);
                student.setEnabled(false);
            } catch (SQLException ex) {
                throw new ServicesException("Could not disable student " + student + ".", ex);
            }
        }
    }
    
    @Override
    public Collection<Student> getBlacklistedStudents() throws ServicesException {
        Collection<Integer> blacklistedIDs;
        try {
            blacklistedIDs = Allocator.getDatabase().getBlacklistedStudents();
        } catch (SQLException ex) {
            throw new ServicesException("Could not read blacklisted students from the database", ex);
        }
        
        return this.idsToStudents(blacklistedIDs, new ArrayList<Student>());
    }

    @Override
    public Collection<Student> getTABlacklist(TA ta) throws ServicesException {
        Collection<Integer> blacklistedIDs;
        try {
            blacklistedIDs = Allocator.getDatabase().getTABlacklist(ta);
        } catch (SQLException ex) {
            throw new ServicesException("Could not read blacklisted students from the database", ex);
        }
        
        return this.idsToStudents(blacklistedIDs, new ArrayList<Student>());
    }
    
    private <T extends Collection<Integer>, S extends Collection<Student>> S idsToStudents(T ids, S students) throws ServicesException {
        for (Integer id : ids) {
            if (!_idMap.containsKey(id)) {
                this.updateDataCache();
                if (!_idMap.containsKey(id)) {
                    throw new ServicesException("Student id [" + id + "] does not map to a Student object.");
                }
            }
            students.add(_idMap.get(id));
        }
        
        return students;
    }

    @Override
    public boolean isStudentLoginInDatabase(String studentLogin) {
        return _loginMap.containsKey(studentLogin);
    }
    
    public Student getStudentFromID(int id) {
        return _idMap.get(id);
    }
    
    @Override
    public Student getStudentFromLogin(String studentLogin) {
        return _loginMap.get(studentLogin);
    }
    
    @Override
    public void updateDataCache() throws ServicesException {
        Collection<StudentRecord> studentRecords;
        try {
            studentRecords = Allocator.getDatabase().getAllStudents();
        } catch (SQLException ex) {
            throw new ServicesException("Could not retrieve students from the "
                    + "database.", ex);
        }

        _enabledStudents.clear();

        //removing from the map any student that has been removed from the database
        Set<Integer> validIDs = new HashSet<Integer>();
        for (StudentRecord sr : studentRecords) {
            validIDs.add(sr.getDbId());
        }
        Iterator<Entry<Integer, Student>> mapIterator = _idMap.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Student student = mapIterator.next().getValue();
            if (!validIDs.contains(student.getDbId())) {
                mapIterator.remove();
                _loginMap.remove(student.getLogin());
            }
        }

        //update the students in the map and
        //add any student that has been added to the database
        for (StudentRecord studentRecord : studentRecords) {
            if (_idMap.containsKey(studentRecord.getDbId())) {
                Student student = _loginMap.get(studentRecord.getLogin());

                //calling Student.update(...) will mutate any out-of-date fields of the
                //Student object (and return true if any changes were made)
                student.update(studentRecord.getFirstName(), studentRecord.getLastName(),
                                                             studentRecord.isEnabled());
            }
            else {
                Student newStudent = new Student(studentRecord.getDbId(),
                                                 studentRecord.getLogin(),
                                                 studentRecord.getFirstName(),
                                                 studentRecord.getLastName(),
                                                 studentRecord.isEnabled());
                _idMap.put(newStudent.getDbId(), newStudent);
                _loginMap.put(newStudent.getLogin(), newStudent);
            }

            if (studentRecord.isEnabled()) {
                _enabledStudents.add(_idMap.get(studentRecord.getDbId()));
            }
        }
    }

}
