package cakehat.database;

import java.util.Map;
import cakehat.config.Assignment;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import com.google.common.collect.Iterables;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author aunger
 * @author hdrosen
 */
public class DatabaseTest {

    private Database _database;

    /**
     * runs before every test
     */
    @Before
    public void setUp() throws SQLException, IOException {
        _database = new DatabaseImpl(Allocator.getFileSystemUtilities().createTempFile("tempDB", "db"));

        SingletonAllocation<Database> dbioAlloc =
            new SingletonAllocation<Database>()
            {
                public Database allocate() { return _database; };
            };
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();

        _database.resetDatabase();
    }
    
        
    @Test
    public void testAddStudentNotInDatabase() throws SQLException {
        String s1login = "jstudent";
        String s1first = "Joe";
        String s1last = "Student";
        
        // Since there is only 1 student in the database, its id should be 1.
        assertEquals(1, _database.addStudent(s1login, s1first, s1last));
    }
    
     
    /**
     * Tests that adding a student already in the database returns 0.
     *
     * @throws SQLException
     */
    @Test
    public void testAddStudentAlreadyInDatabase() throws SQLException {
        String s1login = "jstudent";
        String s1first = "Joe";
        String s1last = "Student";

        //adding the student the first time should return an ID
        assertEquals(1, _database.addStudent(s1login, s1first, s1last));

        //adding the same student again should return 0
        assertEquals(0, _database.addStudent(s1login, s1first, s1last));
    }
    
        
    /**
     * Tests that adding 2 students returns unique student IDs.
     *
     * @throws SQLException
     */
    @Test
    public void testAdd2DifferentStudents() throws SQLException {
        String s1login = "jstudent";
        String s1first = "Joe";
        String s1last = "Student";

        int student1ID = _database.addStudent(s1login, s1first, s1last);

        String s2login = "arose";
        String s2first = "Anna";
        String s2last = "Rose";

        int student2ID = _database.addStudent(s2login, s2first, s2last);

        //check that student IDs are correct for both students
        assertEquals(1, student1ID);
        assertEquals(2, student2ID);
    }
    
    /**
     * Tests that an empty collection is returned when there are no students
     * in the database.
     *
     * @throws SQLException
     */
    @Test
    public void testGetAllStudentsWhenDatabaseIsEmpty() throws SQLException {
        assertTrue(_database.getAllStudents().isEmpty());
    }
    
        
    /**
     * Tests that there is one student record when 1 student has been added to the
     * database and that the student record contains the correct information.
     *
     * @throws SQLException
     */
    @Test
    public void testGetAllStudentsWhenOneStudentInDatabase() throws SQLException {
        String s1login = "jstudent";
        String s1first = "Joe";
        String s1last = "Student";
        //add student to database
        int s1ID = _database.addStudent(s1login, s1first, s1last);
        Collection<StudentRecord> students = _database.getAllStudents();

        //check that there is only one student in database
        assertEquals(1, students.size());

        //check that the StudentRecord in the collection is for the student
        //in the database
        StudentRecord record = Iterables.get(students, 0);
        assertEquals(s1ID, record.getDbId());
        assertEquals(s1first, record.getFirstName());
        assertEquals(s1last, record.getLastName());
        assertEquals(s1login, record.getLogin());
    }
    
        /**
     * Tests that there are two student records when 2 students have been added to the
     * database and that the student records contain the correct information.
     *
     * @throws SQLException
     */
    @Test
    public void testGetAllStudentsWhen2StudentsInDatabase() throws SQLException {
        String s1login = "jstudent";
        String s1first = "Joe";
        String s1last = "Student";

        String s2login = "arose";
        String s2first = "Anna";
        String s2last = "Rose";

        //add students to database
        int s1ID = _database.addStudent(s1login, s1first, s1last);
        int s2ID = _database.addStudent(s2login, s2first, s2last);
        Collection<StudentRecord> students = _database.getAllStudents();

        //check that StudentRecord for student1 contains correct information
        StudentRecord record1 = Iterables.get(students, 0);
        assertEquals(s1ID, record1.getDbId());
        assertEquals(s1first, record1.getFirstName());
        assertEquals(s1last, record1.getLastName());
        assertEquals(s1login, record1.getLogin());
        
        //check that StudentRecord for student2 contains correct information
        StudentRecord record2 = Iterables.get(students, 1);
        assertEquals(s2ID, record2.getDbId());
        assertEquals(s2first, record2.getFirstName());
        assertEquals(s2last, record2.getLastName());
        assertEquals(s2login, record2.getLogin());
    }
    
    @Test
    public void testBlacklistStartsEmpty() throws SQLException {
        String taLogin = "the_ta";
        
        Collection<Integer> blacklist = _database.getBlacklist(taLogin);
        assertEquals(0, blacklist.size());
    }
    
    @Test
    public void testBlacklistSingleStudent() throws SQLException {
        int studentID = _database.addStudent("student", "first", "last");
        String taLogin = "the_ta";
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(studentID);
        
        _database.blacklistStudents(toBlacklist, taLogin);
        
        Collection<Integer> blacklist = _database.getBlacklist(taLogin);
        
        // there should only be one student in the TA's blacklist
        assertEquals(1, blacklist.size());
        
        // and it should be the student with ID studentID
        int blacklistedID = Iterables.get(blacklist, 0);
        assertEquals(studentID, blacklistedID);
    }
    
        @Test
    public void testBlacklistMultipleStudents() throws SQLException {
        int studentID1 = _database.addStudent("student1", "yohonathan", "eldridge");
        int studentID2 = _database.addStudent("student2", "josh", "kaplan");
        int studentID3 = _database.addStudent("student3", "hannah", "rosen");
        String taLogin = "taLogin";
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(3);
        toBlacklist.add(studentID1);
        toBlacklist.add(studentID2);
        toBlacklist.add(studentID3);
        
        _database.blacklistStudents(toBlacklist, taLogin);
        
        Collection<Integer> blacklist = _database.getBlacklist(taLogin);
        
        // there should be 3 students on the ta's blacklist
        assertEquals(3, blacklist.size());
        
        // they should be the students with studentID1, studentID2, studentID3
        assertTrue(blacklist.contains(studentID1));
        assertTrue(blacklist.contains(studentID2));
        assertTrue(blacklist.contains(studentID3));
    }
    
    @Test
    public void testSimultaneousDuplicateBlacklisting() throws SQLException {
        int studentID = _database.addStudent("student", "first", "last");
        String taLogin = "the_ta";
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(studentID);
        toBlacklist.add(studentID);
        
        _database.blacklistStudents(toBlacklist, taLogin);
        
        Collection<Integer> blacklist = _database.getBlacklist(taLogin);
        
        //there should be only one student in the TA's blacklist
        assertEquals(1, blacklist.size());
        
        //and it should be the student with ID studentID
        int blacklistedID = Iterables.get(blacklist, 0);
        assertEquals(studentID, blacklistedID);
    }
    
    @Test
    public void testSequentialDuplicateBlacklisting() throws SQLException {
        int studentID = _database.addStudent("student", "first", "last");
        String taLogin = "the_ta";
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(studentID);
        
        _database.blacklistStudents(toBlacklist, taLogin);
        Collection<Integer> blacklist = _database.getBlacklist(taLogin);
        
        //there should be only one student in the TA's blacklist
        assertEquals(1, blacklist.size());
        
        //and it should be the student with ID studentID
        int blacklistedID = Iterables.get(blacklist, 0);
        assertEquals(studentID, blacklistedID);
        
        //this should still be the case after re-blacklisting the same student
        _database.blacklistStudents(toBlacklist, taLogin);
        blacklist = _database.getBlacklist(taLogin);
        blacklistedID = Iterables.get(blacklist, 0);
        assertEquals(studentID, blacklistedID);
    }
    
    @Test(expected=SQLException.class)
    public void testBlacklistingNonExistentStudentFails() throws SQLException {
        int studentID = -1;
        String taLogin = "the_ta";
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(studentID);
        
        //call to blacklist should fail because there is no student ID studentID
        _database.blacklistStudents(toBlacklist, taLogin);
    }
    
    @Test
    public void testNoneAddedWhenBlacklistingNonExistentStudentFails() throws SQLException {
        int goodStudentID = _database.addStudent("student", "first", "last");
        int badStudentID = -1;
        String taLogin = "the_ta";
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(goodStudentID);
        toBlacklist.add(badStudentID);
        
        //call to blacklist should fail because there is no student ID studentID
        boolean exception = false;
        try {
            _database.blacklistStudents(toBlacklist, taLogin);
        } catch (SQLException e) {
            exception = true;
        }
        assertTrue(exception);
        
        //and neither student should have been blacklisted
        Collection<Integer> blacklist = _database.getBlacklist(taLogin);
        assertEquals(0, blacklist.size());
    }

            
    /**
     * Tests that unblacklisting 1 student removes the student from the ta's
     * blacklist.
     *
     * @throws SQLException
     */
    @Test
    public void testUnblacklistSingleStudent() throws SQLException {
        String taLogin = "the_ta";
        int student1ID = _database.addStudent("student1", "first1", "last1");
        int student2ID = _database.addStudent("student2", "first2", "last2");
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(student1ID);
        toBlacklist.add(student2ID);
        
        Collection<Integer> toUnBlacklist = new ArrayList<Integer>(1);
        toUnBlacklist.add(student1ID);
        
        _database.blacklistStudents(toBlacklist, taLogin);
        
        // make sure 2 students are blacklisted
        assertEquals(2, _database.getBlacklist(taLogin).size());
        
        _database.unBlacklistStudents(toUnBlacklist, taLogin);
        
        // make sure only 1 student in database
        assertEquals(1, _database.getBlacklist(taLogin).size());
        
        // make sure it is student 2
        assertTrue(_database.getBlacklist(taLogin).contains(student2ID));
    }
    
    /**
     * Tests that unblacklisting 2 students removes the students from the ta's
     * blacklist.
     *
     * @throws SQLException
     */
    @Test
    public void testUnblacklistTwoSTudents() throws SQLException {
        String taLogin = "the_ta";
        int student1ID = _database.addStudent("student1", "first1", "last1");
        int student2ID = _database.addStudent("student2", "first2", "last2");
        int student3ID = _database.addStudent("student3", "first3", "last3");
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(student1ID);
        toBlacklist.add(student2ID);
        toBlacklist.add(student3ID);
        
        Collection<Integer> toUnBlacklist = new ArrayList<Integer>(1);
        toUnBlacklist.add(student1ID);
        toUnBlacklist.add(student2ID);
        
        _database.blacklistStudents(toBlacklist, taLogin);
        
        // make sure 2 students are blacklisted
        assertEquals(3, _database.getBlacklist(taLogin).size());
        
        _database.unBlacklistStudents(toUnBlacklist, taLogin);
        
        // make sure only 1 student in database
        assertEquals(1, _database.getBlacklist(taLogin).size());
        
        // make sure it is student 2
        assertTrue(_database.getBlacklist(taLogin).contains(student3ID));
    }
    
    /**
     * Tests that an empty collection is returned when the ta's blacklist is empty.
     * 
     * @throws SQLException 
     */
    @Test
    public void testEmptyCollectionReturnedWhenNoStudentsBlacklisted() throws SQLException {
        String taLogin = "the_ta";
        
        // make sure the collection returned is empty
        assertTrue(_database.getBlacklist(taLogin).isEmpty());
    }
    
    /**
     * Tests that getting the blacklist when 1 student is blacklisted works.
     * 
     * @throws SQLException 
     */
    @Test
    public void testGetBlacklistedWithOneStudent() throws SQLException {
        String taLogin = "the_ta";
        int student1ID = _database.addStudent("student1", "first1", "last1");
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(student1ID);
        
        _database.blacklistStudents(toBlacklist, taLogin);
        
        // make sure that the collection returned has 1 student in it
        assertEquals(1, _database.getBlacklist(taLogin).size());
        
        // make sure that the collection contains the student that was blacklisted
        assertTrue(_database.getBlacklist(taLogin).contains(student1ID));
    }
    
    /**
     * Tests that getting the blacklist when 2 student is blacklisted works.
     * 
     * @throws SQLException 
     */
    @Test
    public void testGetBlacklistWithTwoStudents() throws SQLException {
        String taLogin = "the_ta";
        int student1ID = _database.addStudent("student1", "first1", "last1");
        int student2ID = _database.addStudent("student2", "first2", "last2");
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(student1ID);
        toBlacklist.add(student2ID);
        
        _database.blacklistStudents(toBlacklist, taLogin);
        
        // make sure the collection returned has 2 students in it
        assertEquals(2, _database.getBlacklist(taLogin).size());
        
        // make sure that the collection contains the students that were blacklisted
        assertTrue(_database.getBlacklist(taLogin).contains(student1ID));
        assertTrue(_database.getBlacklist(taLogin).contains(student2ID));
    }
    
    /**
     * Tests that getBlacklistedStudents works when only 1 TA has a blacklist.
     * 
     * @throws SQLException 
     */
    @Test
    public void testGetBlacklistedStudentsWhenOneTAHasBlacklist() throws SQLException {
        String taLogin = "the_ta";
        int student1ID = _database.addStudent("student1", "first1", "last1");
        int student2ID = _database.addStudent("student2", "first2", "last2");
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>(1);
        toBlacklist.add(student1ID);
        toBlacklist.add(student2ID);
        
        _database.blacklistStudents(toBlacklist, taLogin);
        
        // check that 2 students are in the list of all blacklisted students
        assertEquals(2, _database.getBlacklistedStudents().size());
        
        // check that the 2 students are the correct students
        assertTrue(_database.getBlacklistedStudents().contains(student1ID));
        assertTrue(_database.getBlacklistedStudents().contains(student2ID));
    }
   
    /**
     * Tests that getBlacklistedStudents works when 2 TAs have non-overlapping blacklists.
     * 
     * @throws SQLException 
     */
    @Test
    public void testGetBlacklistedStudentsWhenTwoTAsHaveBlacklists() throws SQLException {
        String ta1 = "ta1";
        String ta2 = "ta2";
        int student1ID = _database.addStudent("student1", "first1", "last1");
        int student2ID = _database.addStudent("student2", "first2", "last2");
        int student3ID = _database.addStudent("student3", "first3", "last3");
        
        Collection<Integer> ta1ToBlacklist = new ArrayList<Integer>(1);
        ta1ToBlacklist.add(student1ID);
        ta1ToBlacklist.add(student2ID);
        
        Collection<Integer> ta2ToBlacklist = new ArrayList<Integer>(1);
        ta2ToBlacklist.add(student3ID);

        _database.blacklistStudents(ta1ToBlacklist, ta1);
        _database.blacklistStudents(ta2ToBlacklist, ta2);
        
        // check that 3 students are in the collection
        assertEquals(3, _database.getBlacklistedStudents().size());
        
        // check that the 3 students are the correct students
        assertTrue(_database.getBlacklistedStudents().contains(student1ID));
        assertTrue(_database.getBlacklistedStudents().contains(student2ID));
        assertTrue(_database.getBlacklistedStudents().contains(student3ID)); 
    }
    
        /**
     * Tests that getBlacklistedStudents works when 2 TAs have overlapping blacklists.
     * 
     * @throws SQLException 
     */
    @Test
    public void testGetBlacklistedStudentsWhenTwoTAsHaveOverlappingBlacklists() throws SQLException {
        String ta1 = "ta1";
        String ta2 = "ta2";
        int student1ID = _database.addStudent("student1", "first1", "last1");
        int student2ID = _database.addStudent("student2", "first2", "last2");
        int student3ID = _database.addStudent("student3", "first3", "last3");
        
        Collection<Integer> ta1ToBlacklist = new ArrayList<Integer>(1);
        ta1ToBlacklist.add(student1ID);
        ta1ToBlacklist.add(student2ID);
        
        Collection<Integer> ta2ToBlacklist = new ArrayList<Integer>(1);
        ta2ToBlacklist.add(student2ID);
        ta2ToBlacklist.add(student3ID);

        _database.blacklistStudents(ta1ToBlacklist, ta1);
        _database.blacklistStudents(ta2ToBlacklist, ta2);
        
        // check that 3 students are in the collection
        assertEquals(3, _database.getBlacklistedStudents().size());
        
        // check that the 3 students are the correct students
        assertTrue(_database.getBlacklistedStudents().contains(student1ID));
        assertTrue(_database.getBlacklistedStudents().contains(student2ID));
        assertTrue(_database.getBlacklistedStudents().contains(student3ID)); 
    }
    
    @Test(expected=SQLException.class)
    public void testGroupNamesUniqueForAssignment() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        NewGroup goodGroup = new NewGroup(asgn, "group");
        NewGroup badGroup = new NewGroup(asgn, "group");
        
        //adding group should succeed
        boolean exception = false;
        try {
            _database.addGroup(goodGroup);
        } catch (SQLException e) {
            exception = true;
        }
        assertFalse(exception);
        
        //adding group with same name for same assignment should fail
        _database.addGroup(badGroup);
    }
    
    @Test(expected=CakeHatDBIOException.class)
    public void testOneGroupPerStudentPerAssignment() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        int studentID = 0;
        boolean exception = false;
        try {
            studentID = _database.addStudent("student", "first", "last");
        } catch (SQLException ex) {
            exception = true;
        }
        assertFalse(exception);
        
        Student student = ConfigurationData.generateStudent(studentID, "student", "fist", "last", "email", true);
        NewGroup goodGroup = new NewGroup(asgn, student);
        
        //adding group should succeed
        _database.addGroup(goodGroup);
        
        NewGroup badGroup = new NewGroup(asgn, "badgroup", student);
        _database.addGroup(badGroup);
    }
    
    @Test
    public void testDistributionFailureAtomicity() throws SQLException {
        String dpID = "dpID";
        String taLogin = "ta";
        int badStudentID = -1;
        int goodStudentID = _database.addStudent("student", "first", "last");
        
        Map<String, Map<String, Collection<Integer>>> distribution = new HashMap<String, Map<String, Collection<Integer>>>();
        distribution.put(dpID, new HashMap<String, Collection<Integer>>());
        distribution.get(dpID).put(taLogin, new ArrayList<Integer>(2));
        distribution.get(dpID).get(taLogin).add(goodStudentID);
        distribution.get(dpID).get(taLogin).add(badStudentID);
        
        //setting the distribution should fail b/c badStudentID is invalid
        try {
            _database.setDistribution(distribution);
            fail();
        } catch (SQLException ex) {}
        
        //since the distribution was previously empty, it still should be
        int distSize =_database.getDistribution(dpID).size();        
        assertEquals(0, distSize);
    }
    
    @Test(expected=SQLException.class)
    public void testDistributionFailurePreservation() throws SQLException {
        String dpID = "dpID";
        String taLogin = "ta";
        int goodStudentID = _database.addStudent("student", "first", "last");
        
        Map<String, Map<String, Collection<Integer>>> distribution = new HashMap<String, Map<String, Collection<Integer>>>();
        distribution.put(dpID, new HashMap<String, Collection<Integer>>());
        distribution.get(dpID).put(taLogin, new ArrayList<Integer>(2));
        distribution.get(dpID).get(taLogin).add(goodStudentID);
        
        //setting the distribution should succeed
        _database.setDistribution(distribution);
        
        //adding an invalid student to the distribution and attempting to overwrite
        //the one in the databse should fail
        int badStudentID = -1;
        distribution.get(dpID).get(taLogin).add(badStudentID);
        
        try {
            _database.setDistribution(distribution);
            fail();
        } catch (SQLException ex) {}
        
        //but the distribution stored in the database should still match the
        //one originally added
        Map<String, Collection<Integer>> returnedDist = _database.getDistribution(dpID);       
        assertEquals(1, returnedDist.size());
        int distributedStudentID = Iterables.get(returnedDist.get(dpID), 0);
        assertEquals(goodStudentID, distributedStudentID);
    }
            
    @Test
    public void testGroupAssignOverwrite() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        int studentID = _database.addStudent("student", "first", "last");
        Student student = ConfigurationData.generateStudent(studentID, "student", "first", "last", "email", true);
        
        NewGroup toAdd = new NewGroup(asgn, student);
        GroupRecord added = _database.addGroup(toAdd);
        
        String partID = "part";
        String ta1Login = "ta1";
        _database.assignGroup(added.getDbId(), partID, ta1Login);
        
        //reassgning the group to a new TA should succeed
        String ta2Login = "ta2";
        _database.assignGroup(added.getDbId(), partID, ta2Login);
        assertEquals(ta2Login, _database.getGrader(partID, added.getDbId()));
    }
    
}
