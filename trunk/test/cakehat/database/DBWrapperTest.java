package cakehat.database;

import cakehat.database.ConnectionProvider;
import cakehat.database.DatabaseIO;
import cakehat.database.DBWrapper;
import cakehat.database.CakeHatDBIOException;
import cakehat.database.Group;
import cakehat.config.Assignment;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import cakehat.config.TA;
import java.util.ArrayList;
import java.util.Collection;
import static org.junit.Assert.*;

/**
 *
 * @author alexku
 */
public class DBWrapperTest {

    private DatabaseIO _instance;
    private ConnectionProvider _connProvider;

    /**
     * runs before any tests
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * runs after all tests
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * runs before every test
     */
    @Before
    public void setUp() throws SQLException {
        _connProvider = new InMemoryConnectionProvider();
        _instance = new DBWrapper(_connProvider);

        boolean exception = false;
        _instance.resetDatabase();
    }

    /**
     * runs after every test
     */
    @After
    public void tearDown() throws SQLException {
        _connProvider.closeConnection(_connProvider.createConnection());
    }

    /**
     * Test of blacklistStudents method, of class DBWrapper.
     */
    @Test
    public void testBlacklistStudents() throws SQLException {

        /* setup data in DB
         */
        TA ta = ConfigurationData.generateRandomTA();

        String student1 = ConfigurationData.generateRandomString();
        _instance.addStudent(student1, "FName", "LName");
        String student2 = ConfigurationData.generateRandomString();
        _instance.addStudent(student2, "FName2", "LName2");

        /* verify that the blacklist starts empty
         */
        Collection<String> emptyBL = _instance.getTABlacklist(ta);
        assertTrue(emptyBL.isEmpty());

        /* add the blacklist to be checked for
         */
        Collection<String> blacklist = new ArrayList<String>();
        blacklist.add(student1);
        blacklist.add(student2);
        _instance.blacklistStudents(blacklist, ta);

        /* get the blacklist backout
         */
        Collection<String> returnedBlacklist = _instance.getTABlacklist(ta);

        /* make sure that the two lists are the same
         * if this is true then the student was blacklisted successfully
         */
        Collection<String> blacklistAsStrings = new ArrayList<String>();
        blacklistAsStrings.add(student1);
        blacklistAsStrings.add(student2);
        assertTrue(blacklistAsStrings.containsAll(returnedBlacklist));
        assertTrue(returnedBlacklist.containsAll(blacklistAsStrings));
        assertTrue(returnedBlacklist.size() == blacklistAsStrings.size());
    }

    /**
     * Test of unblacklistStudents method, of class DBWrapper.
     */
    @Test
    public void testUnblacklistStudents() throws SQLException {

        /* setup data in DB
         */
        TA ta = ConfigurationData.generateRandomTA();

        String student1 = ConfigurationData.generateRandomString();
        _instance.addStudent(student1, "FName", "LName");
        String student2 = ConfigurationData.generateRandomString();
        _instance.addStudent(student2, "FName2", "LName2");
        String student3 = ConfigurationData.generateRandomString();
        _instance.addStudent(student3, "FName3", "LName3");
        String student4 = ConfigurationData.generateRandomString();
        _instance.addStudent(student4, "FName4", "LName4");

        /* add the blacklist to be checked for and then remove two from the blacklist
         */
        Collection<String> blacklist = new ArrayList<String>();
        blacklist.add(student1);
        blacklist.add(student2);
        blacklist.add(student3);
        blacklist.add(student4);
        _instance.blacklistStudents(blacklist, ta);

        Collection<String> unblacklist = new ArrayList<String>();
        unblacklist.add(student2);
        unblacklist.add(student4);
        _instance.unBlacklistStudents(unblacklist, ta);

        /* get the blacklist back out
         */
        Collection<String> returnedBlacklist = _instance.getTABlacklist(ta);

        /* make sure that the two lists are the same
         * if this is true then it was unblacklisted correctly
         */
        Collection<String> blacklistAsStrings = new ArrayList<String>();
        blacklistAsStrings.add(student1);
        blacklistAsStrings.add(student3);
        assertTrue(blacklistAsStrings.containsAll(returnedBlacklist));
        assertTrue(returnedBlacklist.containsAll(blacklistAsStrings));
        assertTrue(returnedBlacklist.size() == blacklistAsStrings.size());
    }

    /**
     * Test of getTABlacklist method, of class DBWrapper.
     */
    @Test
    public void testGetTABlacklist() throws SQLException {

        /* setup data in DB
         */
        TA ta1 = ConfigurationData.generateRandomTA();
        TA ta2 = ConfigurationData.generateRandomTA();
        TA ta3 = ConfigurationData.generateRandomTA();

        String student1 = ConfigurationData.generateRandomString();
        _instance.addStudent(student1, "FName", "LName");
        String student2 = ConfigurationData.generateRandomString();
        _instance.addStudent(student2, "FName2", "LName2");
        String student3 = ConfigurationData.generateRandomString();
        _instance.addStudent(student3, "FName3", "LName3");

        /* add a blacklist to make sure that it does not just return all the
         * students regardless of blacklistedness
         */
        Collection<String> blacklist1 = new ArrayList<String>();
        blacklist1.add(student1);
        blacklist1.add(student3);
        _instance.blacklistStudents(blacklist1, ta1);

        /* add the blacklist to be checked for
         */
        Collection<String> blacklist2 = new ArrayList<String>();
        blacklist2.add(student1);
        blacklist2.add(student2);
        _instance.blacklistStudents(blacklist2, ta2);
        _instance.blacklistStudents(blacklist2, ta2); //verify that second add is ignored

        /* get the blacklist backout
         */
        Collection<String> returnedBlacklist = _instance.getTABlacklist(ta2);

        /* make sure that the two lists are the same
         * if this is true then the blacklist was returned successfully
         */
        Collection<String> blacklistAsStrings = new ArrayList<String>();
        blacklistAsStrings.add(student1);
        blacklistAsStrings.add(student2);
        assertTrue(blacklistAsStrings.containsAll(returnedBlacklist));
        assertTrue(returnedBlacklist.containsAll(blacklistAsStrings));
        assertTrue(returnedBlacklist.size() == blacklistAsStrings.size());

        /* make sure that a TA with no blacklist returns an empty blacklist
         */
        Collection<String> emptyBL = _instance.getTABlacklist(ta3);
        assertTrue(emptyBL.isEmpty());
    }

    /**
     * Test of getBlacklistedStudents method, of class DBWrapper.
     */
    @Test
    public void testGetBlacklistedStudents() throws SQLException {

        /* setup data in DB
         */
        TA ta1 = ConfigurationData.generateRandomTA();
        TA ta2 = ConfigurationData.generateRandomTA();

        String student1 = ConfigurationData.generateRandomString();
        _instance.addStudent(student1, "FName", "LName");
        String student2 = ConfigurationData.generateRandomString();
        _instance.addStudent(student2, "FName2", "LName2");
        String student3 = ConfigurationData.generateRandomString();
        _instance.addStudent(student3, "FName3", "LName3");

        /* add a blacklist to make sure that it does not just return all the
         * blacklisted students
         */
        Collection<String> blacklist1 = new ArrayList<String>();
        blacklist1.add(student1);
        blacklist1.add(student3);
        _instance.blacklistStudents(blacklist1, ta1);

        /* add a second TA's blacklist
         */
        Collection<String> blacklist2 = new ArrayList<String>();
        blacklist2.add(student1);
        blacklist2.add(student2);
        _instance.blacklistStudents(blacklist2, ta2);

        /* get the blacklist backout
         */
        Collection<String> returnedBlacklist = _instance.getBlacklistedStudents();

        /* make sure that the two lists are the same
         * if this is true then the list of blacklisted students was returned successfully
         */
        Collection<String> expectedBlacklistedStudents = new ArrayList<String>();
        expectedBlacklistedStudents.add(student1);
        expectedBlacklistedStudents.add(student2);
        expectedBlacklistedStudents.add(student3);
        assertTrue(expectedBlacklistedStudents.containsAll(returnedBlacklist));
        assertTrue(returnedBlacklist.containsAll(expectedBlacklistedStudents));
        assertTrue(returnedBlacklist.size() == expectedBlacklistedStudents.size());
    }

    /**
     * Test of isDistEmpty method, of class DBWrapper.
     */
    @Test
    public void testIsDistEmpty() throws SQLException, CakeHatDBIOException {

        /* setup data in DB
         */
        TA ta = ConfigurationData.generateRandomTA();

        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();

        String student1 = ConfigurationData.generateRandomString();
        _instance.addStudent(student1, "FName", "LName");
        String student2 = ConfigurationData.generateRandomString();
        _instance.addStudent(student2, "FName2", "LName2");

        Group group = new Group("testGroup", student1, student2);
        _instance.setGroup(asgn, group);

        /* verify that the dist is empty when start
         */
        assertTrue(_instance.isDistEmpty(asgn));

        /* add the group to the TAs dist
         */
        _instance.assignGroupToGrader(group, asgn.getDistributableParts().get(0), ta);

        /* verify that the dist is not empty now that something has been added
         */
        assertTrue(!_instance.isDistEmpty(asgn));
    }
}
