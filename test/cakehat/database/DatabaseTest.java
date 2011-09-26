package cakehat.database;

import com.google.common.collect.ImmutableList;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.List;
import cakehat.config.handin.DistributablePart;
import java.util.Iterator;
import java.util.Map;
import cakehat.config.Assignment;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.rubric.TimeStatus;
import com.google.common.collect.Iterables;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import support.utils.CalendarUtilitiesImpl;
/**
 *
 * @author aunger
 * @author hdrosen
 */
public class DatabaseTest {

    private Database _database;
    private double DELTA = .00001;

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
    public void testUnblacklistTwoStudents() throws SQLException {
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
    
    /**
     * Tests that there are no groups in the database initially.
     * 
     * @throws SQLException 
     */
    @Test
    public void testNoGroupsInDatabaseInitially() throws SQLException {
        Collection<GroupRecord> groups = _database.getAllGroups();
        assertEquals(0, groups.size());
    }
    
    @Test
    public void testGetAllGroupsWithOneGroup() throws SQLException, CakeHatDBIOException {
        NewGroup toAdd = ConfigurationData.generateRandomGroup();
        this.addMembersToDatabase(toAdd);
        GroupRecord record = _database.addGroup(toAdd);
        
        Collection<GroupRecord> groups = _database.getAllGroups();
        
        // check that there is only 1 group1 in the database
        assertEquals(1, groups.size());
        
        GroupRecord actualRecord = Iterables.get(groups, 0);
        
        // check that it is the group1 that was added by checking each field of group1 record
        assertEquals(record.getDbId(), actualRecord.getDbId());
        assertEquals(record.getAssignmentID(), actualRecord.getAssignmentID());
        assertEquals(record.getName(), actualRecord.getName());
        assertTrue(record.getMemberIDs().containsAll(actualRecord.getMemberIDs()));
    }
    
    @Test
    public void testGetAllGroupsWithTwoGroups() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "student1", "first1", "last1", true);
        Student student2 = new Student(2, "student2", "first2", "last2", true);
        Student student3 = new Student(3, "student3", "first3", "last3", true);
        Student student4 = new Student(4, "student4", "first4", "last4", true);
        
        NewGroup group1 = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2);
        NewGroup group2 = ConfigurationData.generateNewGroup(asgn, "group2", student3, student4);
        this.addMembersToDatabase(group1);
        this.addMembersToDatabase(group2);
        
        // add groups to database
        GroupRecord record1 = _database.addGroup(group1);
        GroupRecord record2 = _database.addGroup(group2);
        
        Collection<GroupRecord> groups = _database.getAllGroups();
        
        // check that there are 2 groups in the database
        assertEquals(2, groups.size());
        
        GroupRecord actualRecord1 = Iterables.get(groups, 0);
        GroupRecord actualRecord2 = Iterables.get(groups, 1);
        
        
        // check that it is the group1 that was added by checking each field of group1 record
        assertEquals(record1.getDbId(), actualRecord1.getDbId());
        assertEquals(record1.getAssignmentID(), actualRecord1.getAssignmentID());
        assertEquals(record1.getName(), actualRecord1.getName());
        assertTrue(record1.getMemberIDs().containsAll(actualRecord1.getMemberIDs()));
        
        assertEquals(record2.getDbId(), actualRecord2.getDbId());
        assertEquals(record2.getAssignmentID(), actualRecord2.getAssignmentID());
        assertEquals(record2.getName(), actualRecord2.getName());
        assertTrue(record2.getMemberIDs().containsAll(actualRecord2.getMemberIDs()));
    }
    
    /**
     * Tests adding a group1 not already in the database.
     * 
     * @throws SQLException
     * @throws CakeHatDBIOException 
     */
    @Test
    public void testAddGroupNotInDatabase() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "student1", "first1", "last1", true);
        Student student2 = new Student(2, "student2", "first2", "last2", true);
        Student student3 = new Student(3, "student3", "first3", "last3", true);
        NewGroup toAdd = ConfigurationData.generateNewGroup(asgn, "group", student1, student2, student3);
        this.addMembersToDatabase(toAdd);
        
        ArrayList members = new ArrayList();
        members.add(student1.getDbId());
        members.add(student2.getDbId());
        members.add(student3.getDbId());
        
        GroupRecord record = _database.addGroup(toAdd);
        assertEquals(1, record.getDbId());
        assertEquals(asgn.getDBID(), record.getAssignmentID());
        assertTrue(members.containsAll(record.getMemberIDs()));
        assertEquals(toAdd.getName(), record.getName());
    }
    
    @Test
    public void testAddGroupsNotInDatabase() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "student1", "first1", "last1", true);
        Student student2 = new Student(2, "student2", "first2", "last2", true);
        Student student3 = new Student(3, "student3", "first3", "last3", true);
        Student student4 = new Student(4, "student4", "first4", "last4", true);
        
        NewGroup group1 = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2, student3);
        NewGroup group2 = ConfigurationData.generateNewGroup(asgn, "group2", student4);
        this.addMembersToDatabase(group1);
        this.addMembersToDatabase(group2);
        
        Collection<GroupRecord> records = _database.addGroups(ImmutableList.of(group1, group2));
        
        Collection<GroupRecord> recordsInDatabase = _database.getAllGroups();
        
        // check that there are 2 groups in the database
        assertEquals(2, recordsInDatabase.size());
        
        GroupRecord actualRecord1 = Iterables.get(records, 0);
        GroupRecord actualRecord2 = Iterables.get(records, 1);
        GroupRecord record1 = Iterables.get(recordsInDatabase, 0);
        GroupRecord record2 = Iterables.get(recordsInDatabase, 1);
        
        // check that the 2 in the database are the groups that were added
        assertEquals(actualRecord1.getAssignmentID(), record1.getAssignmentID());
        assertEquals(actualRecord1.getDbId(), record1.getDbId());
        assertEquals(actualRecord1.getName(), record1.getName());
        assertTrue(actualRecord1.getMemberIDs().containsAll(record1.getMemberIDs()));
        assertEquals(actualRecord2.getAssignmentID(), record2.getAssignmentID());
        assertEquals(actualRecord2.getDbId(), record2.getDbId());
        assertEquals(actualRecord2.getName(), record2.getName());
        assertTrue(actualRecord2.getMemberIDs().containsAll(record2.getMemberIDs()));
    }
    
    @Test(expected=SQLException.class)
    public void testGroupNamesUniqueForAssignment() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        NewGroup goodGroup = new NewGroup(asgn, "group");
        NewGroup badGroup = new NewGroup(asgn, "group");
        
        //adding goodGroup should succeed
        boolean exception = false;
        try {
            _database.addGroup(goodGroup);
        } catch (SQLException e) {
            exception = true;
        }
        assertFalse(exception);
        
        //adding badGroup with same name for same assignment should fail
        _database.addGroup(badGroup);
    }
    
    /**
     * Tests that a SQLException is NOT thrown when you try to add a group1 
     * with the same name as a group already in the database for a different assignment.
     * 
     * @throws SQLException
     * @throws CakeHatDBIOException 
     */
    @Test
    public void testAddGroupWithSameNameForDifferentAssignment() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "student1", "first1", "last1", true);
        Student student2 = new Student(2, "student2", "first2", "last2", true);
        Student student3 = new Student(3, "student3", "first3", "last3", true);
        NewGroup toAdd = ConfigurationData.generateNewGroup(asgn, "group", student1, student2, student3);
        this.addMembersToDatabase(toAdd);
        
//         create a group with the same name as toAdd
        Student student4 = new Student(4, "student4", "first4", "last4", true);
        Student student5 = new Student(5, "student5", "first5", "last5", true);
        NewGroup sameName = ConfigurationData.generateNewGroup(
                ConfigurationData.generateAssignmentWithNameWithTwoDPs("weirdly named assignment"),
                toAdd.getName(),
                student4, student5);

        this.addMembersToDatabase(sameName);
        
//         Should NOT throw a SQLException
        _database.addGroup(sameName);
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
        
        Student student = ConfigurationData.generateStudent(studentID, "student", "first", "last", "email", true);
        NewGroup goodGroup = new NewGroup(asgn, student);
        
        //adding goodGroup should succeed
        _database.addGroup(goodGroup);
        
        NewGroup badGroup = new NewGroup(asgn, "badgroup", student);
        _database.addGroup(badGroup);
    }
    
    /**
     * Tests getGroup on students in a group with multiple students in it.
     * 
     * @throws SQLException
     * @throws CakeHatDBIOException 
     */
    @Test
    public void testGetGroupWithMultipleStudentGroup() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "login1", "first1", "last1", true);
        Student student2 = new Student(2, "login2", "first2", "last2", true);
        
        NewGroup group = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2);
        
        // add students and group1 to database
        _database.addStudent("login1", "first1", "last1");
        _database.addStudent("login2", "first2", "last2");
        GroupRecord record = _database.addGroup(group);
        
        // add a couple other groups
        Student student3 = new Student(3, "login3", "first3", "last3", true);
        Student student4 = new Student(4, "login4", "first4", "last4", true);
        Student student5 = new Student(5, "login5", "first5", "last5", true);
        Student student6 = new Student(6, "login6", "first6", "last6", true);
        NewGroup extraGroup1 = ConfigurationData.generateNewGroup(asgn, "group2", student3, student4);
        NewGroup extraGroup2 = ConfigurationData.generateNewGroup(asgn, "group3", student5, student6);
        this.addMembersToDatabase(extraGroup1);
        this.addMembersToDatabase(extraGroup2);
        _database.addGroup(extraGroup1);
        _database.addGroup(extraGroup2);
        
        // check that the dbID returned is correct when you get the group1 for student1 and student2
        assertEquals(record.getDbId(), _database.getGroup(asgn.getDBID(), student1.getDbId()));
        assertEquals(record.getDbId(), _database.getGroup(asgn.getDBID(), student2.getDbId()));
    }
   
    /**
     * Tests that 0 is returned if an invalid assignment ID is passed in.
     * 
     * @throws SQLException 
     */
    @Test
    public void testGetGroupWithFakeAssignmentID() throws SQLException {
        String asgnID = "fakeAsgn";
        int studentID = _database.addStudent("login", "first", "last");
        
        // check that 0 is returned
        assertEquals(0, _database.getGroup(asgnID, studentID));
    }
    
        /**
     * Tests that 0 is returned when an invalid studentID is passed in.
     * 
     * @throws SQLException 
     */
    @Test
    public void testGetGroupWithFakeStudentID() throws SQLException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();

        // should return 0 because 0 is not a valid student ID
        assertEquals(0, _database.getGroup(asgn.getDBID(), 0));
    }
    
     /**
     * Tests that an empty collection is returned when no groups have been made
     * for the given assignment.
     * 
     * @throws SQLException 
     */
    @Test
    public void testGetGroupsOnAssignmentWithoutGroups() throws SQLException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        
        // make sure an empty collection is returned
        assertTrue(_database.getGroups(asgn.getDBID()).isEmpty());
    }
    
    @Test
    public void testGetGroupsOnAssignmentWithTwoGroups() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "login1", "first1", "last1", true);
        Student student2 = new Student(2, "login2", "first2", "last2", true);
        Student student3 = new Student(3, "login3", "first3", "last3", true);
        Student student4 = new Student(4, "login4", "first4", "last4", true);
        
        NewGroup group1 = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2);
        NewGroup group2 = ConfigurationData.generateNewGroup(asgn, "group2", student3, student4);
        
        // add students and group1 to database
        this.addMembersToDatabase(group1);
        this.addMembersToDatabase(group2);
        GroupRecord record1 = _database.addGroup(group1);
        GroupRecord record2 = _database.addGroup(group2);
        
        // check that there are 2 groups for the assignment
        assertEquals(2, _database.getGroups(asgn.getDBID()).size());
        
        // check that they are the groups that were created
        assertTrue(_database.getGroups(asgn.getDBID()).contains(record1.getDbId()));
        assertTrue(_database.getGroups(asgn.getDBID()).contains(record2.getDbId()));
    }
    
    @Test
    public void testGetGroupsOnAssignmentWithOneGroup() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "login1", "first1", "last1", true);
        Student student2 = new Student(2, "login2", "first2", "last2", true);
        
        NewGroup group = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2);
        
        // add students and group to database
        _database.addStudent("login1", "first1", "last1");
        _database.addStudent("login2", "first2", "last2");
        GroupRecord record = _database.addGroup(group);
        
        Collection<Integer> groups = _database.getGroups(asgn.getDBID());
        
        // check that there is 1 group for the assignment
        assertEquals(1, groups.size());
        
        // check that it is the group that was created
        assertTrue(groups.contains(record.getDbId()));
    }
    
    @Test
    public void testGetGroupsForFakeAssignment() throws SQLException {
        String asgnID = "fakeAsgn";
        
        // check that a SQLException is NOT thrown
        assertTrue(_database.getGroups(asgnID).isEmpty());
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
    
    @Test
    public void testRemoveGroup() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "login1", "first1", "last1", true);
        Student student2 = new Student(2, "login2", "first2", "last2", true);
        Student student3 = new Student(3, "login3", "first3", "last3", true);
        Student student4 = new Student(4, "login4", "first4", "last4", true);
        
        NewGroup group1 = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2);
        NewGroup group2 = ConfigurationData.generateNewGroup(asgn, "group2", student3, student4);
        
        // add students and group1 to database
        this.addMembersToDatabase(group1);
        this.addMembersToDatabase(group2);
        GroupRecord record1 = _database.addGroup(group1);
        GroupRecord record2 = _database.addGroup(group2);
        
        _database.removeGroup(record1.getDbId());
        
        // check that there is only one group in database
        assertEquals(1, _database.getGroups(asgn.getDBID()).size());
        
        // check that it is the group that was not removed
        assertTrue(_database.getGroups(asgn.getDBID()).contains(record2.getDbId()));
        
    }
    
    @Test
    public void testRemoveFakeGroup() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "login1", "first1", "last1", true);
        Student student2 = new Student(2, "login2", "first2", "last2", true);
        Student student3 = new Student(3, "login3", "first3", "last3", true);
        Student student4 = new Student(4, "login4", "first4", "last4", true);
        
        NewGroup group1 = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2);
        NewGroup group2 = ConfigurationData.generateNewGroup(asgn, "group2", student3, student4);
        
        // add students and groups to database
        this.addMembersToDatabase(group1);
        this.addMembersToDatabase(group2);
        GroupRecord record1 = _database.addGroup(group1);
        GroupRecord record2 = _database.addGroup(group2);
        
        // try to remove a group with a fake dbid
        _database.removeGroup(-1);
        
        // this should have no effect so make sure other groups are still in database
        assertEquals(2, _database.getGroups(asgn.getDBID()).size());
        
        // check that they are the correct groups
        assertTrue(_database.getGroups(asgn.getDBID()).contains(record1.getDbId()));
        assertTrue(_database.getGroups(asgn.getDBID()).contains(record2.getDbId()));
    }
    
    public void testRemoveGroups() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        Student student1 = new Student(1, "login1", "first1", "last1", true);
        Student student2 = new Student(2, "login2", "first2", "last2", true);
        Student student3 = new Student(3, "login3", "first3", "last3", true);
        Student student4 = new Student(4, "login4", "first4", "last4", true);
                
        NewGroup group1 = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2);
        NewGroup group2 = ConfigurationData.generateNewGroup(asgn, "group2", student3, student4);
        
        // add students and groups to database
        this.addMembersToDatabase(group1);
        this.addMembersToDatabase(group2);
        _database.addGroup(group1);
        _database.addGroup(group2);
        
        _database.removeGroups(asgn.getDBID());
        
        // check that there are no groups for the assignment
        assertEquals(0, _database.getGroups(asgn.getDBID()).size());
    }
    
    @Test
    public void testIsDistEmptyWithNoDistributionSet() throws SQLException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        ArrayList<String> partIDs = new ArrayList<String>();
        for (DistributablePart part : asgn.getDistributableParts()) {
            partIDs.add(part.getDBID());
        }
        
        // distribution should be empty because no distribution was set
        assertTrue(_database.isDistEmpty(partIDs));
    }
    
    @Test
    public void testIsDistEmptyWithDistributionSet() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String dpID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String taLogin = "ta";
        int goodStudentID = _database.addStudent("student", "first", "last");
        Student student = ConfigurationData.generateStudent(goodStudentID, "student", "first", "last", "email", true);
        
        NewGroup group = ConfigurationData.generateNewGroup(asgn, "group", student);
        int groupID = _database.addGroup(group).getDbId();
        
        Map<String, Map<String, Collection<Integer>>> distribution = new HashMap<String, Map<String, Collection<Integer>>>();
        distribution.put(dpID, new HashMap<String, Collection<Integer>>());
        distribution.get(dpID).put(taLogin, new ArrayList<Integer>(2));
        distribution.get(dpID).get(taLogin).add(groupID);
        
        ArrayList<String> partIDs = new ArrayList<String>();
        partIDs.add(dpID);
        
        //setting the distribution should succeed
        _database.setDistribution(distribution);
        
        // isDistEmpty should return false
        assertFalse(_database.isDistEmpty(partIDs));
    }
    
    @Test
    public void testEmptyMapReturnedWhenNoDistributionSet() throws SQLException {
        String dpID = "part";
        
        // check that getDistribution returns an empty map
        assertTrue(_database.getDistribution(dpID).isEmpty());
    }
    
    @Test
    public void testGetDistribution() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String dpID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String taLogin = "ta";
        int goodStudentID = _database.addStudent("student", "first", "last");
        Student student = ConfigurationData.generateStudent(goodStudentID, "student", "first", "last", "email", true);
        
        NewGroup group = ConfigurationData.generateNewGroup(asgn, "group", student);
        int groupID = _database.addGroup(group).getDbId();
        
        Map<String, Map<String, Collection<Integer>>> distribution = new HashMap<String, Map<String, Collection<Integer>>>();
        distribution.put(dpID, new HashMap<String, Collection<Integer>>());
        distribution.get(dpID).put(taLogin, new ArrayList<Integer>(2));
        distribution.get(dpID).get(taLogin).add(groupID);
        
        ArrayList<String> partIDs = new ArrayList<String>();
        partIDs.add(dpID);

        _database.setDistribution(distribution);
        
        // test that getdistribution returns the correct map
        assertEquals(1, _database.getDistribution(dpID).get(taLogin).size());
        assertTrue(_database.getDistribution(dpID).get(taLogin).contains(groupID));
    }
    
    @Test
    public void testDistributionOverwrite() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String dpID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String taLogin1 = "ta1";
        int goodStudentID = _database.addStudent("student", "first", "last");
        Student student = ConfigurationData.generateStudent(goodStudentID, "student", "first", "last", "email", true);
        Student student2 = new Student(2, "login2", "first2", "last2", true);
        Student student3 = new Student(3, "login3", "first3", "last3", true);
        int student2ID = _database.addStudent("login2", "first2", "last2");
        int student3ID = _database.addStudent("login3", "first3", "last3");
        
        NewGroup group1 = ConfigurationData.generateNewGroup(asgn, "group1", student);
        int groupID = _database.addGroup(group1).getDbId();
        
        NewGroup group2 = ConfigurationData.generateNewGroup(asgn, "group2", student2, student3);
        int group2ID = _database.addGroup(group2).getDbId();
        
        Map<String, Map<String, Collection<Integer>>> distribution1 = new HashMap<String, Map<String, Collection<Integer>>>();
        distribution1.put(dpID, new HashMap<String, Collection<Integer>>());
        distribution1.get(dpID).put(taLogin1, new ArrayList<Integer>(2));
        distribution1.get(dpID).get(taLogin1).add(groupID);
        
        ArrayList<String> partIDs = new ArrayList<String>();
        partIDs.add(dpID);

        _database.setDistribution(distribution1);
        
        // check that the distribution is in the database
        assertEquals(1, _database.getDistribution(dpID).get(taLogin1).size());
        assertTrue(_database.getDistribution(dpID).get(taLogin1).contains(groupID));
        
        // set the distribution to a new distribution
        Map<String, Map<String, Collection<Integer>>> distribution2 = new HashMap<String, Map<String, Collection<Integer>>>();
        distribution1.put(dpID, new HashMap<String, Collection<Integer>>());
        distribution1.get(dpID).put(taLogin1, new ArrayList<Integer>(2));
        distribution1.get(dpID).get(taLogin1).add(group2ID);
        
        _database.setDistribution(distribution2);
        
        // check that the distribution in the database is the new distribution
        assertEquals(1, _database.getDistribution(dpID).get(taLogin1).size());
        assertTrue(_database.getDistribution(dpID).get(taLogin1).contains(groupID));
        
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
        //the one in the database should fail
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
    public void testAssignPreviouslyUnassignedGroup() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        Student student1 = new Student(1, "login1", "first1", "last1", true);
        Student student2 = new Student(2, "login2", "first2", "last2", true);
        Student student3 = new Student(3, "login3", "first3", "last3", true);
        Student student4 = new Student(4, "login4", "first4", "last4", true);
        
        NewGroup group1 = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2, student3, student4);
        this.addMembersToDatabase(group1);
        int groupID = _database.addGroup(group1).getDbId();
        
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String taLogin = "ta";
        
        _database.assignGroup(groupID, partID, taLogin);
        
        // check that the ta is now assigned the group
        assertTrue(_database.getAssignedGroups(partID, taLogin).contains(groupID));
    }
    
    @Test
    public void testAssignGroupAlreadyAssignedToSameTA() throws SQLException, CakeHatDBIOException{
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        Student student1 = new Student(1, "login1", "first1", "last1", true);
        Student student2 = new Student(2, "login2", "first2", "last2", true);
        Student student3 = new Student(3, "login3", "first3", "last3", true);
        Student student4 = new Student(4, "login4", "first4", "last4", true);
        
        NewGroup group1 = ConfigurationData.generateNewGroup(asgn, "group1", student1, student2, student3, student4);
        this.addMembersToDatabase(group1);
        int groupID = _database.addGroup(group1).getDbId();
        
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String taLogin = "ta";
        
        _database.assignGroup(groupID, partID, taLogin);
        
        // check that the ta is now assigned the group
        assertTrue(_database.getAssignedGroups(partID, taLogin).contains(groupID));
        
        // assigning the same group to the same ta should not cause an error
        _database.assignGroup(groupID, partID, taLogin);
        
        // check that the ta is still assigned the group
        assertTrue(_database.getAssignedGroups(partID, taLogin).contains(groupID));
    }
    
    /**
     * Assigning a group1 with an invalid group id should throw a SQLException.
     * 
     * @throws SQLException 
     */
    @Test(expected=SQLException.class)
    public void testAssignGroupWithInvalidGroup() throws SQLException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String taLogin = "ta";
        int invalidGroup = -1;
        
        _database.assignGroup(invalidGroup, partID, taLogin);
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
    
    @Test
    public void testUnassignGroup() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        int studentID = _database.addStudent("student", "first", "last");
        Student student = ConfigurationData.generateStudent(studentID, "student", "first", "last", "email", true);
        
        NewGroup toAdd = new NewGroup(asgn, student);
        GroupRecord added = _database.addGroup(toAdd);
        
        String partID = "part";
        String ta1Login = "ta1";
        _database.assignGroup(added.getDbId(), partID, ta1Login);
        assertTrue(_database.getAssignedGroups(partID, ta1Login).contains(added.getDbId()));
        
        // unassign the group from the ta
        _database.unassignGroup(added.getDbId(), partID, ta1Login);
        
        // the group should no longer be assigned to the ta
        assertFalse(_database.getAssignedGroups(partID, ta1Login).contains(added.getDbId()));
    }

    @Test
    public void testUnassignInvalidGroup() throws SQLException {
        String partID = "part";
        String ta1Login = "ta1";
        int invalidGroup = -1;
        
        // this should not cause an error
        _database.unassignGroup(invalidGroup, partID, ta1Login);
    }
    
    @Test
    public void testGetAssignedGroupsWhenNoDistributionSetForPart() throws SQLException {
        String partID = "part";
        String taLogin = "ta";
        
        // check that the collection returned is empty
        assertTrue(_database.getAssignedGroups(partID, taLogin).isEmpty());
    }
    
    /**
     * Tests that the collection returned is empty when there is a distribution but the 
     * ta has not been assigned any groups.
     * 
     * @throws SQLException
     * @throws CakeHatDBIOException 
     */
    @Test
    public void testGetAssignedGroupsWhenNoGroupsAssignedToTA() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        int studentID = _database.addStudent("student", "first", "last");
        Student student = ConfigurationData.generateStudent(studentID, "student", "first", "last", "email", true);
        
        NewGroup toAdd = new NewGroup(asgn, student);
        GroupRecord added = _database.addGroup(toAdd);
        
        String partID = "part";
        String ta1Login = "ta1";
        String ta2Login = "ta2";
        _database.assignGroup(added.getDbId(), partID, ta1Login);
        
        // check that the collection returned is empty
        assertTrue(_database.getAssignedGroups(partID, ta2Login).isEmpty());
    }
    
    @Test
    public void testGetAssignedGroupsWhenGroupsAssignedToTA() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        String partID = "part";
        String ta = "ta";
        _database.assignGroup(added1.getDbId(), partID, ta);
        _database.assignGroup(added2.getDbId(), partID, ta);
        
        Collection<Integer> assigned = _database.getAssignedGroups(partID, ta);
        
        // check that there are 2 groups in the returned collection
        assertEquals(2, assigned.size());
        
        // check that they are the correct groups
        assertTrue(assigned.contains(added1.getDbId()));
        assertTrue(assigned.contains(added2.getDbId()));
    }
    
    @Test
    public void testGetAssignedGroupsForPartWithNoDistribution() throws SQLException {
        String partID = "part";
        
        // should return empty collection
        assertTrue(_database.getAssignedGroups(partID).isEmpty());
    }
    
    @Test
    public void testGetAssignedGroupsForPartAssignedToOneTA() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        String partID = "part";
        String ta = "ta";
        _database.assignGroup(added1.getDbId(), partID, ta);
        _database.assignGroup(added2.getDbId(), partID, ta);
        
        Collection<Integer> assigned = _database.getAssignedGroups(partID);
        
        // check that there are 2 groups in the returned collection
        assertEquals(2, assigned.size());
        
        // check that they are the correct groups
        assertTrue(assigned.contains(added1.getDbId()));
        assertTrue(assigned.contains(added2.getDbId()));
    }
    
    @Test
    public void testGetAssignedGroupsForPartAssignedToMultipleTAs() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        String partID = "part";
        String ta1 = "ta1";
        String ta2 = "ta2";
        _database.assignGroup(added1.getDbId(), partID, ta1);
        _database.assignGroup(added2.getDbId(), partID, ta2);
        
        Collection<Integer> assigned = _database.getAssignedGroups(partID);
        
        // check that there are 2 groups in the returned collection
        assertEquals(2, assigned.size());
        
        // check that they are the correct groups
        assertTrue(assigned.contains(added1.getDbId()));
        assertTrue(assigned.contains(added2.getDbId()));
    }
    
    @Test
    public void testGetDPsWithAssignedGroupsWhenNoneAssignedToTA() throws SQLException {
        String taLogin = "talogin";
        
        // an empty collection should be returned
        assertTrue(_database.getDPsWithAssignedGroups(taLogin).isEmpty());
    }
    
    @Test
    public void testGetDPsWithAssignedGroupsWhenOneAssignedToTA() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
//        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        String partID = "part";
        String ta1 = "ta1";
        _database.assignGroup(added1.getDbId(), partID, ta1);
        
        // should have one part id in collection returned
        assertEquals(1, _database.getDPsWithAssignedGroups(ta1).size());
        
        // should have the correct part id
        assertTrue(_database.getDPsWithAssignedGroups(ta1).contains(partID));
    }
    
    @Test
    public void testGetDPsWithAssignedGroupsWhenMultipleAssignedToTA() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        String ta1 = "ta1";
        
        List<DistributablePart> parts = asgn.getDistributableParts();
        
        _database.assignGroup(added1.getDbId(), parts.get(0).getDBID(), ta1);
        _database.assignGroup(added2.getDbId(), parts.get(1).getDBID(), ta1);
        
        Set<String> assigned = _database.getDPsWithAssignedGroups(ta1);
        
        // check that there are 2 groups in the returned collection
        assertEquals(2, assigned.size());
        
        // check that they are the correct parts
        assertTrue(assigned.contains(parts.get(0).getDBID()));
        assertTrue(assigned.contains(parts.get(1).getDBID()));
    }
    
    @Test
    public void testGetGraderWhenNoneAssigned() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // getGrader should return null because no grader was assigned
        assertNull(_database.getGrader(partID, student1ID));
    }
    
    @Test
    public void testGetGraderWhenOneAssigned() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        String ta = "ta";
        _database.assignGroup(added1.getDbId(), partID, ta);
        
        // grader should be ta
        assertEquals(ta, _database.getGrader(partID, added1.getDbId()));
    }
    
    @Test
    public void testGetGraderForInvalidGroup() throws SQLException {
        String partID = "part";
        int invalidGroup = -1; 
        
        // null should be returned because the group1 is invalid
        assertNull(_database.getGrader(partID, invalidGroup));
    }
    
    @Test
    public void testGrantExtension() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        Calendar newDate = new GregorianCalendar();
        newDate.set(2011, 10, 31, 10, 30);
        _database.grantExtension(added1.getDbId(), asgn.getDBID(), newDate, null);
        
        // make sure that the group1 was granted the extension
        Calendar extendedDate = _database.getExtension(added1.getDbId());
        assertTrue(Allocator.getCalendarUtilities().areCalendarsEqual(newDate, extendedDate));
        assertNull(_database.getExtensionNote(added1.getDbId()));
    }
    
    @Test
    public void testGrantExtensionOverride() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        Calendar oldExtension = new GregorianCalendar();
        oldExtension.set(2011, 10, 31, 10, 30);
        _database.grantExtension(added1.getDbId(), asgn.getDBID(), oldExtension, null);
        
        Calendar newExtension = new GregorianCalendar();
        newExtension.set(2012, 11, 12, 14, 22);
        String note = "note";
        _database.grantExtension(added1.getDbId(), asgn.getDBID(), newExtension, "note");
        
        // make sure that the group1 was granted the extension
        Calendar extendedDate = _database.getExtension(added1.getDbId());
        assertTrue(Allocator.getCalendarUtilities().areCalendarsEqual(newExtension, extendedDate));
        assertEquals(note, _database.getExtensionNote(added1.getDbId()));
    }
    
    @Test(expected=SQLException.class)
    public void testGrantExtensionForInvalidGroup() throws SQLException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        int badGroup = -1;
        
        Calendar extension = new GregorianCalendar();
        extension.set(2012, 11, 12, 14, 22);
        String note = "note";
        _database.grantExtension(badGroup, asgn.getDBID(), extension, "note");
    }
    
    @Test
    public void testRemoveExtensionOnGroupWithExtension() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        Calendar newDate = new GregorianCalendar();
        newDate.set(2011, 10, 31, 10, 30);
        _database.grantExtension(added1.getDbId(), asgn.getDBID(), newDate, null);
        
        // make sure that the group1 was granted the extension
        Calendar extendedDate = _database.getExtension(added1.getDbId());
        assertTrue(Allocator.getCalendarUtilities().areCalendarsEqual(newDate, extendedDate));
        assertNull(_database.getExtensionNote(added1.getDbId()));
        
        // remove extension
        _database.removeExtension(added1.getDbId());
        
        // make sure the group1 doesn't have an extension
        assertNull(_database.getExtension(added1.getDbId()));
    }
    
    @Test
    public void testRemoveExtensionOnGroupWithoutExtension() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // removing extension when group1 doesn't have extension should not throw an exception
        _database.removeExtension(added1.getDbId());
        
        // make sure group1 doesn't have extension
        assertNull(_database.getExtension(added1.getDbId()));
    }
    
    @Test
    public void testGetExtensionWhenGroupDoesNotHaveExtension() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        assertNull(_database.getExtension(added1.getDbId()));
    }
    
    @Test
    public void testGetExtensionsWhenNoGroupHasExtension() throws SQLException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        
        // map returned should be empty
        assertTrue(_database.getExtensions(asgn.getDBID()).isEmpty());
    }
    
    @Test
    public void testGetExtensionsWhenOneGroupHasExtension() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        Calendar newDate = new GregorianCalendar();
        newDate.set(2011, 10, 31, 10, 30);
        _database.grantExtension(added1.getDbId(), asgn.getDBID(), newDate, null);
        
        Map<Integer, Calendar> extensions = _database.getExtensions(asgn.getDBID());
        
        // check that only group1 is in the map
        assertEquals(1, extensions.size());
        
        // check that it is in fact group1 1 and has the correct extension
        assertTrue(extensions.keySet().contains(added1.getDbId()));
        assertTrue(Allocator.getCalendarUtilities().areCalendarsEqual(newDate, extensions.get(added1.getDbId())));
    }
    
    @Test
    public void testGetExtensionsWhenMultipleGroupsHaveExtensions() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        Calendar newDate1 = new GregorianCalendar();
        newDate1.set(2011, 10, 31, 10, 30);
        _database.grantExtension(added1.getDbId(), asgn.getDBID(), newDate1, null);
        
        Calendar newDate2 = new GregorianCalendar();
        newDate1.set(2012, 9, 21, 9, 40);
        _database.grantExtension(added2.getDbId(), asgn.getDBID(), newDate2, null);
        
        Map<Integer, Calendar> extensions = _database.getExtensions(asgn.getDBID());
        
        // check that both groups are in the map
        assertEquals(2, extensions.size());
        
        // check that they are the correct groups
        assertTrue(extensions.keySet().contains(added1.getDbId()));
        assertTrue(Allocator.getCalendarUtilities().areCalendarsEqual(newDate1, extensions.get(added1.getDbId())));
        assertTrue(extensions.keySet().contains(added2.getDbId()));
        assertTrue(Allocator.getCalendarUtilities().areCalendarsEqual(newDate2, extensions.get(added2.getDbId())));
    }
    
    @Test
    public void testGetExtensionNoteWhenGroupDoesNotHaveExtension() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // extension note should be null because group1 does not have extension
        assertNull(_database.getExtensionNote(added1.getDbId()));
    }
    
    @Test
    public void testGrantExemption() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = asgn.getDistributableParts().get(0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // grant the exemption
        _database.grantExemption(added1.getDbId(), partID, null);
        
        // make sure group1 has exemption
        Set<Integer> exemptions = _database.getExemptions(partID);
        assertEquals(1, exemptions.size());
        assertTrue(exemptions.contains(added1.getDbId()));
        assertNull(_database.getExemptionNote(added1.getDbId(), partID));
    }
    
    @Test
    public void testGrantExemptionOverride() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = asgn.getDistributableParts().get(0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // grant the exemption
        _database.grantExemption(added1.getDbId(), partID, null);
        
        // make sure group1 has exemption
        Set<Integer> exemptions = _database.getExemptions(partID);
        assertEquals(1, exemptions.size());
        assertTrue(exemptions.contains(added1.getDbId()));
        assertNull(_database.getExemptionNote(added1.getDbId(), partID));
        
        // grant different exemption
        String note = "note";
        _database.grantExemption(added1.getDbId(), partID, note);
        
        // check that new exemption is there
        Set<Integer> newExemptions = _database.getExemptions(partID);
        assertEquals(1, newExemptions.size());
        assertTrue(newExemptions.contains(added1.getDbId()));
        assertEquals(note, _database.getExemptionNote(added1.getDbId(), partID));
    }
    
    @Test(expected=SQLException.class)
    public void testGrantExemptionForInvalidGroup() throws SQLException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = asgn.getDistributableParts().get(0).getDBID();
        int invalidGroup = -1;
        
        _database.grantExemption(invalidGroup, partID, null);
    }
    
    @Test
    public void testRemoveExemptionOnGroupWithoutExemption() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = asgn.getDistributableParts().get(0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // should not throw an exception
        _database.removeExemption(added1.getDbId(), partID);
        
        // group1 should not have an exemption
        assertEquals(0, _database.getExemptions(partID).size());
    }
    
    @Test
    public void testRemoveExemptionOnGroupWithExemption() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = asgn.getDistributableParts().get(0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // grant the exemption
        _database.grantExemption(added1.getDbId(), partID, null);
        
        // make sure group1 has exemption
        Set<Integer> exemptions = _database.getExemptions(partID);
        assertEquals(1, exemptions.size());
        assertTrue(exemptions.contains(added1.getDbId()));
        assertNull(_database.getExemptionNote(added1.getDbId(), partID));
        
        // remove exemption
        _database.removeExemption(added1.getDbId(), partID);
        
        // make sure group1 no longer has exemption
        assertEquals(0, _database.getExemptions(partID).size());
    }  
    
    @Test
    public void testGetExemptionsWhenNoGroupsHaveExemptions() throws SQLException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = asgn.getDistributableParts().get(0).getDBID();
        
        // make sure no groups have exemptions
        assertEquals(0, _database.getExemptions(partID).size());
    }
    
    @Test
    public void testGetExemptionsWhenOneGroupHasExemption() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = asgn.getDistributableParts().get(0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // grant the exemption
        _database.grantExemption(added1.getDbId(), partID, null);
        
        Set<Integer> exemptions = _database.getExemptions(partID);
        
        // make sure there is only 1 group1 with an exemption
        assertEquals(1, exemptions.size());
        
        // make sure it is the group1 with the exemption
        assertTrue(exemptions.contains(added1.getDbId()));
    }
    
    @Test
    public void testGetExemptionsWhenMultipleGroupsHaveExemptions() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = asgn.getDistributableParts().get(0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        int student3ID = _database.addStudent("student3", "first3", "last3");
        Student student3 = ConfigurationData.generateStudent(student3ID, "student3", "first3", "last3", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        NewGroup toAdd3 = new NewGroup(asgn, student3);
        GroupRecord added3 = _database.addGroup(toAdd3);
        
        // grant the exemption
        _database.grantExemption(added1.getDbId(), partID, null);
        _database.grantExemption(added2.getDbId(), partID, null);
        
        Set<Integer> exemptions = _database.getExemptions(partID);
        
        // make sure there are 2 groups with exemptions
        assertEquals(2, exemptions.size());
        
        // make sure they are the correct groups
        assertTrue(exemptions.contains(added1.getDbId()));
        assertTrue(exemptions.contains(added2.getDbId()));
    }
    
    @Test
    public void testGetExemptionNoteWhenGroupDoesNotHaveExemption() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = asgn.getDistributableParts().get(0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // exemption note sould be null because group1 does not have exemption
        assertNull(_database.getExemptionNote(added1.getDbId(), partID));
    }
    
    @Test
    public void testEnterGrade() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        double score = 74;
        
        _database.enterGrade(added1.getDbId(), partID, score);
        
        // check that grade is correct in database
        assertEquals(score, _database.getPartScore(added1.getDbId(), partID), DELTA);
    }
    
    @Test(expected=SQLException.class)
    public void testEnterGradeWithFakeGroup() throws SQLException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        
        int fakeGroup = -1;
        int score = 98;
        
        // try to enter grade for fake gorup
        _database.enterGrade(fakeGroup, partID, score);
    }
    
    @Test
    public void testGetPartScoreWhenNoneEnteredForGroup() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        // enter grade for group1 1 but not group1 2
        _database.enterGrade(added1.getDbId(), partID, 70);
        
        // null should be returned when trying to get grade for group1 2
        assertNull(_database.getPartScore(added2.getDbId(), partID));
        
    }
    
    @Test
    public void testGetPartScoreWhenNoneEnteredForPart() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String part1ID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String part2ID = Iterables.get(asgn.getDistributableParts(), 1).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // enter score for part 1 but not part 2
        _database.enterGrade(added1.getDbId(), part1ID, 50);
        
        // check that null is returned when trying to get score for part 2
        assertNull(_database.getPartScore(added1.getDbId(), part2ID));
    }
    
    @Test
    public void testGetScoreWhenOnePartDoesNotHaveScore() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String part1ID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String part2ID = Iterables.get(asgn.getDistributableParts(), 1).getDBID();
        
        ArrayList<String> parts = new ArrayList<String>();
        parts.add(part1ID);
        parts.add(part2ID);
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        int part1Score = 45;
        
        // enter score for part 1 but not part 2
        _database.enterGrade(added1.getDbId(), part1ID, part1Score);
        
        // check that total score is equal to part 1 score because part 2 does not have score
        assertEquals(part1Score, _database.getScore(added1.getDbId(), parts), DELTA);
        
    }
    
    @Test
    public void testGetScoreWhenAllPartsHaveScores() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String part1ID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String part2ID = Iterables.get(asgn.getDistributableParts(), 1).getDBID();
        
        ArrayList<String> parts = new ArrayList<String>();
        parts.add(part1ID);
        parts.add(part2ID);
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        int part1Score = 45;
        int part2Score = 34;
        
        // enter scores for both parts
        _database.enterGrade(added1.getDbId(), part1ID, part1Score);
        _database.enterGrade(added1.getDbId(), part2ID, part2Score);
        
        // make sure that the score is the total of the scores for the parts
        assertEquals(part1Score + part2Score, _database.getScore(added1.getDbId(), parts), DELTA);
    }
    
    @Test
    public void testGetScoreWhenNoPartsHaveScores() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String part1ID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String part2ID = Iterables.get(asgn.getDistributableParts(), 1).getDBID();
        
        ArrayList<String> parts = new ArrayList<String>();
        parts.add(part1ID);
        parts.add(part2ID);
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // make sure that the score is 0 when no part scores have been entered
        assertEquals(0, _database.getScore(added1.getDbId(), parts), DELTA);
    }
    
    
    // maps group1 ID -> score
    @Test
    public void testGetPartScoresIncludingGroupWithoutScore() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        ArrayList<Integer> groups = new ArrayList<Integer>();
        groups.add(added1.getDbId());
        groups.add(added2.getDbId());
        
        double group1Score = 70;
        
        // enter grade for group1 1 but not group1 2
        _database.enterGrade(added1.getDbId(), partID, group1Score);
        
        // make sure there is only one group1 in the returned map
        Map<Integer, Double> gradesReturned = _database.getPartScores(partID, groups);
        
        // make sure it is the correct group1
        assertTrue(gradesReturned.keySet().contains(added1.getDbId()));
        
        // make sure it has the correct score
        assertEquals(group1Score, gradesReturned.get(added1.getDbId()), DELTA);
    }
    
    @Test
    public void testGetPartScoresWhenAllGroupsHaveScores() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        ArrayList<Integer> groups = new ArrayList<Integer>();
        groups.add(added1.getDbId());
        groups.add(added2.getDbId());
        
        double group1Score = 70;
        double group2Score = 45;
        
        // enter grade for group1 1 but not group1 2
        _database.enterGrade(added1.getDbId(), partID, group1Score);
        _database.enterGrade(added2.getDbId(), partID, group2Score);
        
        Map<Integer, Double> gradesReturned = _database.getPartScores(partID, groups);
        
        // make sure there are 2 groups in there
        assertEquals(2, gradesReturned.keySet().size());
        
        // make sure they are the correct groups
        assertTrue(gradesReturned.keySet().contains(added1.getDbId()));
        assertTrue(gradesReturned.keySet().contains(added2.getDbId()));
        
        // make sure that they map to the correct scores
        assertEquals(group1Score, gradesReturned.get(added1.getDbId()), DELTA);
        assertEquals(group2Score, gradesReturned.get(added2.getDbId()), DELTA);
    }
    
    @Test
    public void testGetPartScoresNoGroupsHaveScores() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        ArrayList<Integer> groups = new ArrayList<Integer>();
        groups.add(added1.getDbId());
        groups.add(added2.getDbId());
        
        // the map returned should be empty because no part scores have been entered for any group1
        assertTrue(_database.getPartScores(partID, groups).isEmpty());
    }
    
    @Test
    public void testGetPartScoresWhenOneGroupIsInvalid() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String partID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        int invalidGroup = -1;
        
        ArrayList<Integer> groups = new ArrayList<Integer>();
        groups.add(added1.getDbId());
        groups.add(added2.getDbId());
        groups.add(invalidGroup);
        
        double group1Score = 70;
        double group2Score = 45;
        
        // enter grade for both valid groups
        _database.enterGrade(added1.getDbId(), partID, group1Score);
        _database.enterGrade(added2.getDbId(), partID, group2Score);
        
        // this should not throw an exception
        Map<Integer, Double> gradesReturned = _database.getPartScores(partID, groups);
        
        // make sure there are 2 groups in there
        assertEquals(2, gradesReturned.keySet().size());
        
        // make sure they are the correct groups
        assertTrue(gradesReturned.keySet().contains(added1.getDbId()));
        assertTrue(gradesReturned.keySet().contains(added2.getDbId()));
        
        // make sure that they map to the correct scores
        assertEquals(group1Score, gradesReturned.get(added1.getDbId()), DELTA);
        assertEquals(group2Score, gradesReturned.get(added2.getDbId()), DELTA);
    }  
    
    @Test
    public void testGetScoresWhenNoGroupsHaveScores() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String part1ID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String part2ID = Iterables.get(asgn.getDistributableParts(), 1).getDBID();
        
        ArrayList<String> parts = new ArrayList<String>();
        parts.add(part1ID);
        parts.add(part2ID);
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        ArrayList<Integer> groups = new ArrayList<Integer>();
        groups.add(added1.getDbId());
        groups.add(added2.getDbId());
        
        // make sure the map returned is empty
        assertTrue(_database.getScores(parts, groups).isEmpty());
    }
    
    @Test
    public void testGetScoresWhenOneGroupDoesNotHaveScore() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String part1ID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String part2ID = Iterables.get(asgn.getDistributableParts(), 1).getDBID();
        
        ArrayList<String> parts = new ArrayList<String>();
        parts.add(part1ID);
        parts.add(part2ID);
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        ArrayList<Integer> groups = new ArrayList<Integer>();
        groups.add(added1.getDbId());
        groups.add(added2.getDbId());
        int part1Score = 45;
        int part2Score = 34;
        
        // enter scores for both parts for group1 1
        _database.enterGrade(added1.getDbId(), part1ID, part1Score);
        _database.enterGrade(added1.getDbId(), part2ID, part2Score);
        
        Map<Integer, Double> scores = _database.getScores(parts, groups);
        
        // make sure that there is only one group1 in the map returned
        assertEquals(1, scores.keySet().size());
        
        // make sure that it is the group1 with the score
        assertTrue(scores.containsKey(added1.getDbId()));
        
        // make sure that it has the correct score for the group1
        assertEquals(part1Score + part2Score, _database.getScore(added1.getDbId(), parts), DELTA);
    }

    @Test
    public void testGetScoresWhenAllGroupsHaveScores() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String part1ID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String part2ID = Iterables.get(asgn.getDistributableParts(), 1).getDBID();
        
        ArrayList<String> parts = new ArrayList<String>();
        parts.add(part1ID);
        parts.add(part2ID);
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        ArrayList<Integer> groups = new ArrayList<Integer>();
        groups.add(added1.getDbId());
        groups.add(added2.getDbId());
        int part1ScoreGroup1 = 45;
        int part2ScoreGroup1 = 34;
        int part1ScoreGroup2 = 43;
        
        // enter scores for both parts for group1 1
        _database.enterGrade(added1.getDbId(), part1ID, part1ScoreGroup1);
        _database.enterGrade(added1.getDbId(), part2ID, part2ScoreGroup1);
        _database.enterGrade(added2.getDbId(), part1ID, part1ScoreGroup2);
        
        Map<Integer, Double> scores = _database.getScores(parts, groups);
        
        // make sure that there are 2 groups in the map returned
        assertEquals(2, scores.keySet().size());
        
        // make sure that they are the correct groups
        assertTrue(scores.containsKey(added1.getDbId()));
        assertTrue(scores.containsKey(added2.getDbId()));
        
        // make sure that each group1 has the correct score
        assertEquals(part1ScoreGroup1 + part2ScoreGroup1, scores.get(added1.getDbId()), DELTA);
        assertEquals(part1ScoreGroup2, scores.get(added2.getDbId()), DELTA);
    }
    
    @Test
    public void testGetScoresWhenOneGroupIsInvalid() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String part1ID = Iterables.get(asgn.getDistributableParts(), 0).getDBID();
        String part2ID = Iterables.get(asgn.getDistributableParts(), 1).getDBID();
        
        ArrayList<String> parts = new ArrayList<String>();
        parts.add(part1ID);
        parts.add(part2ID);
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        int invalidGroup = -1;
        
        ArrayList<Integer> groups = new ArrayList<Integer>();
        groups.add(added1.getDbId());
        groups.add(added2.getDbId());
        groups.add(invalidGroup);
        
        double group1ScorePart1 = 70;
        double group1ScorePart2 = 20;
        double group2ScorePart1 = 45;
        double group2ScorePart2 = 25;
        
        // enter grade for both valid groups
        _database.enterGrade(added1.getDbId(), part1ID, group1ScorePart1);
        _database.enterGrade(added2.getDbId(), part1ID, group2ScorePart1);
        _database.enterGrade(added1.getDbId(), part2ID, group1ScorePart2);
        _database.enterGrade(added2.getDbId(), part2ID, group2ScorePart2);
        
        // should not throw an exception
        Map<Integer, Double> scores = _database.getScores(parts, groups);
        
        // make sure that there are 2 groups in the map returned
        assertEquals(2, scores.keySet().size());
        
        // make sure that they are the correct groups
        assertTrue(scores.containsKey(added1.getDbId()));
        assertTrue(scores.containsKey(added2.getDbId()));
        
        // make sure that each group1 has the correct score
        assertEquals(group1ScorePart1 + group1ScorePart2, scores.get(added1.getDbId()), DELTA);
        assertEquals(group2ScorePart1 + group2ScorePart2, scores.get(added2.getDbId()), DELTA);
    }
    
    @Test
    public void testSetHandinStatus() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = ConfigurationData.generateNewGroup(asgn, "group", student1, student2);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        HandinStatus status = new HandinStatus(TimeStatus.EARLY, 0);
        
        _database.setHandinStatus(added1.getDbId(), status);
        
        // make sure the handin status was set for the group1
        assertEquals(status.getDaysLate(), _database.getHandinStatus(added1.getDbId()).getDaysLate());
        assertEquals(status.getTimeStatus(), _database.getHandinStatus(added1.getDbId()).getTimeStatus());
    }
    
    @Test
    public void testSetHandinStatusOverwrite() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = ConfigurationData.generateNewGroup(asgn, "group", student1, student2);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        HandinStatus status1 = new HandinStatus(TimeStatus.EARLY, 0);
        
        _database.setHandinStatus(added1.getDbId(), status1);
        
        // make sure the handin status was set for the group1
        assertEquals(status1.getDaysLate(), _database.getHandinStatus(added1.getDbId()).getDaysLate());
        assertEquals(status1.getTimeStatus(), _database.getHandinStatus(added1.getDbId()).getTimeStatus());
        
        // change handin status
        HandinStatus status2 = new HandinStatus(TimeStatus.LATE, 2);
        _database.setHandinStatus(added1.getDbId(), status2);
        
        // make sure old handin status was overwritten
        assertEquals(status2.getDaysLate(), _database.getHandinStatus(added1.getDbId()).getDaysLate());
        assertEquals(status2.getTimeStatus(), _database.getHandinStatus(added1.getDbId()).getTimeStatus());
    }
    
    @Test(expected=SQLException.class)
    public void testSetHandinStatusWithInvalidGroupID() throws SQLException {
        int invalidGroup = -1;
        HandinStatus status = new HandinStatus(TimeStatus.EARLY, 0);
        
        _database.setHandinStatus(invalidGroup, status);
    }
    
    @Test
    public void testSetHandinStatuses() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        Map<Integer, HandinStatus> statuses = new HashMap<Integer, HandinStatus>();
        
        HandinStatus status1 = new HandinStatus(TimeStatus.ON_TIME, 0);
        HandinStatus status2 = new HandinStatus(TimeStatus.NC_LATE, 5);
        statuses.put(added1.getDbId(), status1);
        statuses.put(added2.getDbId(), status2);
        
        _database.setHandinStatuses(statuses);
        
        // make sure that the handin statuses were set for the groups
        assertEquals(status1.getDaysLate(), _database.getHandinStatus(added1.getDbId()).getDaysLate());
        assertEquals(status1.getTimeStatus(), _database.getHandinStatus(added1.getDbId()).getTimeStatus());
        assertEquals(status2.getDaysLate(), _database.getHandinStatus(added2.getDbId()).getDaysLate());
        assertEquals(status2.getTimeStatus(), _database.getHandinStatus(added2.getDbId()).getTimeStatus());
    }
    
    @Test
    public void testSetHandinStatusesWhenOneGroupIsInvalid() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateRandomAssignment();
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = ConfigurationData.generateNewGroup(asgn, "group", student1, student2);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        int invalidGroup = -1;
        
        Map<Integer, HandinStatus> statuses = new HashMap<Integer, HandinStatus>();
        
        HandinStatus handinStatus = new HandinStatus(TimeStatus.ON_TIME, 0);
        statuses.put(invalidGroup, handinStatus);
        statuses.put(added1.getDbId(), handinStatus);
        
        try{
            _database.setHandinStatuses(statuses);
            fail();
        }
        catch(SQLException e) {}
        
        // make sure that the handin status was set for the valid group
        assertEquals(handinStatus.getDaysLate(), _database.getHandinStatus(added1.getDbId()).getDaysLate());
        assertEquals(handinStatus.getTimeStatus(), _database.getHandinStatus(added1.getDbId()).getTimeStatus());
    }
    
    @Test
    public void testSetHandinStatusesWithEmptyMap() throws SQLException {
        Map<Integer, HandinStatus> statuses = new HashMap<Integer, HandinStatus>();
        
        // make sure exception is not thrown
        _database.setHandinStatuses(statuses);
    }
    
    @Test
    public void testHandinStatusesOverwrite() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        HandinStatus originalStatus = new HandinStatus(TimeStatus.EARLY, 0);
        _database.setHandinStatus(added1.getDbId(), originalStatus);
        
        Map<Integer, HandinStatus> statuses = new HashMap<Integer, HandinStatus>();
        
        HandinStatus status1 = new HandinStatus(TimeStatus.ON_TIME, 0);
        HandinStatus status2 = new HandinStatus(TimeStatus.NC_LATE, 5);
        statuses.put(added1.getDbId(), status1);
        statuses.put(added2.getDbId(), status2);
        
        // this should overwrite the previous HandinStatus for group1 1
        _database.setHandinStatuses(statuses);
        
        // check that both groups have the correct new handin statuses
        assertEquals(status1.getDaysLate(), _database.getHandinStatus(added1.getDbId()).getDaysLate());
        assertEquals(status1.getTimeStatus(), _database.getHandinStatus(added1.getDbId()).getTimeStatus());
        assertEquals(status2.getDaysLate(), _database.getHandinStatus(added2.getDbId()).getDaysLate());
        assertEquals(status2.getTimeStatus(), _database.getHandinStatus(added2.getDbId()).getTimeStatus());
    }
    
    @Test
    public void testGetHandinStatusWhenNoStatusSet() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        // HandinStatus should be null for the group1 because no handin status was set
        assertNull(_database.getHandinStatus(added1.getDbId()));
    }
    
    @Test
    public void testAreHandinStatusesSetWhenNoneSet() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        
        // HandinStatuses should not be set
        assertFalse(_database.areHandinStatusesSet(asgn.getDBID()));
    }
    
    @Test
    public void testAreHandinStatusesSetWhenOneSet() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        _database.addGroup(toAdd2);
        
        HandinStatus originalStatus = new HandinStatus(TimeStatus.EARLY, 0);
        _database.setHandinStatus(added1.getDbId(), originalStatus);
        
        // HandinStatuses should be set
        assertTrue(_database.areHandinStatusesSet(asgn.getDBID()));
    }
    
    @Test
    public void testAreHandinStatusesSetWhenAllSet() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        HandinStatus status1 = new HandinStatus(TimeStatus.EARLY, 0);
        HandinStatus status2 = new HandinStatus(TimeStatus.LATE, 3);
        _database.setHandinStatus(added1.getDbId(), status1);
        _database.setHandinStatus(added2.getDbId(), status2);
        
        // HandinStatuses should be set
        assertTrue(_database.areHandinStatusesSet(asgn.getDBID()));
    }
    
    @Test
    public void testResetDatabase() throws SQLException, CakeHatDBIOException {
        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();
        String part1ID = asgn.getDistributableParts().get(0).getDBID();
        String part2ID = asgn.getDistributableParts().get(1).getDBID();
        
        String ta = "ta";
        int student1ID = _database.addStudent("student1", "first1", "last1");
        Student student1 = ConfigurationData.generateStudent(student1ID, "student1", "first1", "last1", "email", true);
        int student2ID = _database.addStudent("student2", "first2", "last2");
        Student student2 = ConfigurationData.generateStudent(student2ID, "student2", "first2", "last2", "email", true);
        
        NewGroup toAdd1 = new NewGroup(asgn, student1);
        GroupRecord added1 = _database.addGroup(toAdd1);
        
        NewGroup toAdd2 = new NewGroup(asgn, student2);
        GroupRecord added2 = _database.addGroup(toAdd2);
        
        ArrayList<Integer> groups = new ArrayList<Integer>();
        groups.add(added1.getDbId());
        groups.add(added2.getDbId());
        
        Collection<Integer> toBlacklist = new ArrayList<Integer>();
        toBlacklist.add(student2ID);
        
        _database.blacklistStudents(toBlacklist, ta);
        
        Map<String, Map<String, Collection<Integer>>> distribution = new HashMap<String, Map<String, Collection<Integer>>>();
        distribution.put(part1ID, new HashMap<String, Collection<Integer>>());
        distribution.get(part1ID).put(ta, new ArrayList<Integer>(2));
        distribution.get(part1ID).get(ta).add(added1.getDbId());
        
        _database.setDistribution(distribution);
        
        Calendar newDate = new GregorianCalendar();
        newDate.set(2011, 10, 31, 10, 30);
        _database.grantExtension(added1.getDbId(), asgn.getDBID(), newDate, null);
        
        _database.grantExemption(added2.getDbId(), part2ID, null);
        
        _database.enterGrade(added1.getDbId(), part2ID, 90);
        
        _database.setHandinStatus(added2.getDbId(), new HandinStatus(TimeStatus.LATE, 2));
        
        ArrayList<String> parts = new ArrayList<String>();
        parts.add(part1ID);
        parts.add(part2ID);
        
        // reset the database
        _database.resetDatabase();
        
        // make sure that there are no students
        assertEquals(0, _database.getAllStudents().size());
        
        // make sure there are no groups
        assertEquals(0, _database.getAllGroups().size());
        
        // make sure no students are blacklisted
        assertEquals(0, _database.getBlacklistedStudents().size());
        
        // make sure there are no distributions for the parts
        assertTrue(_database.isDistEmpty(parts));
        
        // make sure there are no extensions
        assertEquals(0, _database.getExtensions(asgn.getDBID()).size());
        
        // make sure there are no exemptions
        assertEquals(0, _database.getExemptions(asgn.getDBID()).size());
        
        // make sure no scores are in the database
        assertEquals(0, _database.getScores(parts, groups).size());
        
        // make sure handin statuses are not set
        assertFalse(_database.areHandinStatusesSet(asgn.getDBID()));
        
    }
    
    private void addMembersToDatabase(NewGroup group) throws SQLException {
        for (Student s: group.getMembers()) {
            _database.addStudent(s.getLogin(), s.getFirstName(), s.getLastName());
        }
    }
    
}
