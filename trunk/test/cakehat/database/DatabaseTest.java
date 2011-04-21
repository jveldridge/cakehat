package cakehat.database;

import cakehat.config.Assignment;
import cakehat.config.handin.Handin;
import cakehat.config.TA;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author aunger
 * @author hdrosen
 */
public class DatabaseTest {

    private Database _instance;
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
        _instance = new DatabaseImpl(_connProvider);

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

    /**
     * Test of addStudent method, of class DBWrapper.
     */
    @Test
    public void testAddStudent() throws SQLException {
        String s1login = "jstudent";
        String s1first = "Joe";
        String s1last = "Student";

        //adding the student should not cause an error
        _instance.addStudent(s1login, s1first, s1last);

        //adding the same student again should not cause an error
        _instance.addStudent(s1login, s1first, s1last);

        //make sure student is enabled
        assertTrue(_instance.isStudentEnabled(s1login));

        String s2login = "arose";
        String s2first = "Anna";
        String s2last = "Rose";

        //adding the student should not cause an error
        _instance.addStudent(s2login, s2first, s2last);

        //make sure student is enabled
        assertTrue(_instance.isStudentEnabled(s2login));

        Map<String, String> students = _instance.getAllStudents();

        //check that both students are there
        assertTrue(students.containsKey(s1login));
        assertTrue(students.containsKey(s2login));
        assertEquals(2, students.size());

    }

    @Test
    public void testDisableStudent() throws SQLException {
        String s1login = "hdunne";
        String s1first = "Heather";
        String s1last = "Dunne";

        _instance.addStudent(s1login, s1first, s1last);
        //check that student is enabled after adding
        assertTrue(_instance.isStudentEnabled(s1login));

        _instance.disableStudent(s1login);
        //check that student is disabled after disabling
        assertFalse(_instance.isStudentEnabled(s1login));

        String s2login = "bross";
        String s2first = "Brian";
        String s2last = "Ross";

        _instance.addStudent(s2login, s2first, s2last);
        //check that student is enabled after adding
        assertTrue(_instance.isStudentEnabled(s2login));

        _instance.disableStudent(s2login);
        //check that student is disabled after disabling
        assertFalse(_instance.isStudentEnabled(s2login));

    }

    @Test
    public void testEnableStudent() throws SQLException {
        String s1login = "hdunne";
        String s1first = "Heather";
        String s1last = "Dunne";

        _instance.addStudent(s1login, s1first, s1last);
        assertTrue(_instance.isStudentEnabled(s1login));

        _instance.disableStudent(s1login);
        assertFalse(_instance.isStudentEnabled(s1login));

        _instance.enableStudent(s1login);
        assertTrue(_instance.isStudentEnabled(s1login));

        String s2login = "bross";
        String s2first = "Brian";
        String s2last = "Ross";

        _instance.addStudent(s2login, s2first, s2last);
        assertTrue(_instance.isStudentEnabled(s2login));

        _instance.disableStudent(s2login);
        assertFalse(_instance.isStudentEnabled(s2login));

        _instance.enableStudent(s2login);
        assertTrue(_instance.isStudentEnabled(s2login));
    }

     @Test
     public void testIsStudentEnabled() throws SQLException {
        String s1login = "hdunne";
        String s1first = "Heather";
        String s1last = "Dunne";

        _instance.addStudent(s1login, s1first, s1last);
        _instance.enableStudent(s1login);
        assertTrue(_instance.isStudentEnabled(s1login));

        String s2login = "bross";
        String s2first = "Brian";
        String s2last = "Ross";

        _instance.addStudent(s2login, s2first, s2last);
        _instance.enableStudent(s2login);
        assertTrue(_instance.isStudentEnabled(s2login));

    }

    @Test
    public void testGetStudents() throws SQLException {
        String s1login = "hdunne";
        String s1first = "Heather";
        String s1last = "Dunne";

        _instance.addStudent(s1login, s1first, s1last);

        String s2login = "bross";
        String s2first = "Brian";
        String s2last = "Ross";

        _instance.addStudent(s2login, s2first, s2last);

        String s3login = "trock";
        String s3first = "Timothy";
        String s3last = "Rock";

        _instance.addStudent(s3login, s3first, s3last);
        _instance.disableStudent(s3login);

        Collection<Student> students = _instance.getStudents();
        assertEquals(3, students.size());

        Student s1 = null, s2 = null, s3 = null;
        for (Student s : students) {
            if (s.getLogin().equals(s1login)) {
                s1 = s;
            }
            else if(s.getLogin().equals(s2login)) {
                s2 = s;
            }
            else if (s.getLogin().equals(s3login)) {
                s3 = s;
            }
            else {
                fail("Student object with unexpected login");
            }
        }

        assertNotNull(s1);
        assertNotNull(s2);
        assertNotNull(s3);

        assertEquals(s1first, s1.getFirstName());
        assertEquals(s1last, s1.getLastName());
        assertEquals(true, s1.isEnabled());

        assertEquals(s2first, s2.getFirstName());
        assertEquals(s2last, s2.getLastName());
        assertEquals(true, s2.isEnabled());

        assertEquals(s3first, s3.getFirstName());
        assertEquals(s3last, s3.getLastName());
        assertEquals(false, s3.isEnabled());

    }

    @Test
    public void testGetAllStudents() throws SQLException {
        String s1login = "hdunne";
        String s1first = "Heather";
        String s1last = "Dunne";

        _instance.addStudent(s1login, s1first, s1last);

        String s2login = "bross";
        String s2first = "Brian";
        String s2last = "Ross";

        _instance.addStudent(s2login, s2first, s2last);

        String s3login = "trock";
        String s3first = "Timothy";
        String s3last = "Rock";

        _instance.addStudent(s3login, s3first, s3last);

        Map<String, String> students = _instance.getAllStudents();
        assertEquals(3, students.size());

        //checks that all added students are in the map
        assertTrue(students.containsKey(s1login));
        assertTrue(students.containsKey(s2login));
        assertTrue(students.containsKey(s3login));

        //checks that logins are mapped to the right names
        assertEquals("Heather Dunne", students.get(s1login));
        assertEquals("Brian Ross", students.get(s2login));
        assertEquals("Timothy Rock", students.get(s3login));
    }

    @Test
    public void testGetEnabledStudents() throws SQLException {
        String s1login = "hdunne";
        String s1first = "Heather";
        String s1last = "Dunne";

        _instance.addStudent(s1login, s1first, s1last);

        String s2login = "bross";
        String s2first = "Brian";
        String s2last = "Ross";

        _instance.addStudent(s2login, s2first, s2last);

        String s3login = "trock";
        String s3first = "Timothy";
        String s3last = "Rock";

        _instance.addStudent(s3login, s3first, s3last);

        Map<String, String> enabled1 = _instance.getEnabledStudents();
        assertEquals(3, enabled1.size());

        //checks that all added students are in the map
        assertTrue(enabled1.containsKey(s1login));
        assertTrue(enabled1.containsKey(s2login));
        assertTrue(enabled1.containsKey(s3login));

        //checks that logins are mapped to the right names
        assertEquals("Heather Dunne", enabled1.get(s1login));
        assertEquals("Brian Ross", enabled1.get(s2login));
        assertEquals("Timothy Rock", enabled1.get(s3login));

        _instance.disableStudent(s2login);
        _instance.disableStudent(s1login);

        Map<String, String> enabled2 = _instance.getEnabledStudents();
        assertEquals(1, enabled2.size());

        assertTrue(enabled2.containsKey(s3login));
        assertFalse(enabled2.containsKey(s1login));
        assertFalse(enabled2.containsKey(s2login));

        assertEquals("Timothy Rock", enabled2.get(s3login));
    }

    @Test
    public void testGrantExtension() throws SQLException {
        Handin handin1 = ConfigurationData.generateHandin();
        Group group1 = ConfigurationData.generateRandomGroup();
        GregorianCalendar date1 = new GregorianCalendar(2011, 5, 25, 11, 30, 0);
        for (String login : group1.getMembers()) {
            _instance.addStudent(login, "test", "test");
        }
        _instance.setGroup(handin1.getAssignment(), group1);
        _instance.grantExtension(group1, handin1, date1, "note1");

        assertEquals(date1, _instance.getExtension(group1, handin1));

        Handin handin2 = ConfigurationData.generateHandin();
        Group group2 = ConfigurationData.generateRandomGroup();
        GregorianCalendar date2 = new GregorianCalendar(2011, 3, 15, 10, 30, 0);
        for (String login : group2.getMembers()) {
            _instance.addStudent(login, "test", "test");
        }
        _instance.setGroup(handin2.getAssignment(), group2);
        _instance.grantExtension(group2, handin2, date2, "note2");

        assertEquals(date2, _instance.getExtension(group2, handin2));
        assertEquals("note2", _instance.getExtensionNote(group2, handin2));
    }

    @Test
    public void testRemoveExtension() throws SQLException {
        Handin handin1 = ConfigurationData.generateHandin();
        Group group1 = ConfigurationData.generateRandomGroup();
        GregorianCalendar date = new GregorianCalendar(2011, 5, 25, 11, 30, 0);
        for (String login : group1.getMembers()) {
            _instance.addStudent(login, "test", "test");
        }
        _instance.setGroup(handin1.getAssignment(), group1);
        assertEquals(null, _instance.getExtension(group1, handin1));
        //removing extension from group without extension should not throw error
        _instance.removeExtension(group1, handin1);
        assertEquals(null, _instance.getExtension(group1, handin1));



        Group group2 = ConfigurationData.generateRandomGroup();
         for (String login : group2.getMembers()) {
            _instance.addStudent(login, "test", "test");
        }
        _instance.setGroup(handin1.getAssignment(), group2);
        _instance.grantExtension(group2, handin1, date, "note");
        assertEquals(date, _instance.getExtension(group2, handin1));
        //removing extension from group with extension should not throw error
        _instance.removeExtension(group2, handin1);
        assertEquals(null, _instance.getExtension(group2, handin1));
    }
}
