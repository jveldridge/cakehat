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
    public void testBlacklistStartsEmpty() throws SQLException {
        String taLogin = "the_ta";
        
        Collection<Integer> blacklist = _database.getBlacklist(taLogin);
        assertEquals(0, blacklist.size());
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
