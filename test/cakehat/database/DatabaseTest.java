package cakehat.database;

import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.config.Assignment;
import cakehat.config.TA;
import cakehat.config.handin.Handin;
import cakehat.database.DataServices.ValidityCheck;
import cakehat.services.ServicesException;
import com.google.common.collect.Iterables;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
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

    private Database _database;
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
        _database = new DatabaseImpl(_connProvider);

        SingletonAllocation<Database> dbioAlloc =
            new SingletonAllocation<Database>()
            {
                public Database allocate() { return _database; };
            };
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();

        _database.resetDatabase();
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
    public void testBlacklistStudents() throws SQLException, ServicesException {

        //setup data in DB
        TA ta = ConfigurationData.generateRandomTA();

        String student1Login = ConfigurationData.generateRandomString();
        String student2Login = ConfigurationData.generateRandomString();
        Allocator.getDataServices().addStudent(student1Login, "FName", "LName", ValidityCheck.BYPASS);
        Allocator.getDataServices().addStudent(student2Login, "FName2", "LName2", ValidityCheck.BYPASS);

        //verify that the blacklist starts empty
        Collection<Integer> emptyBL = _database.getTABlacklist(ta);
        assertTrue(emptyBL.isEmpty());

        //add the blacklist to be checked for
        Collection<Student> studentsToBlacklist = new ArrayList<Student>(2);
        studentsToBlacklist.add(Allocator.getDataServices().getStudentFromLogin(student1Login));
        studentsToBlacklist.add(Allocator.getDataServices().getStudentFromLogin(student2Login));
        _database.blacklistStudents(studentsToBlacklist, ta);

        //get the blacklist back out
        Collection<Integer> returnedBlacklist = _database.getTABlacklist(ta);
        
        Collection<Integer> expectedBlacklist = new ArrayList<Integer>(2);
        expectedBlacklist.add(Allocator.getDataServices().getStudentFromLogin(student1Login).getDbId());
        expectedBlacklist.add(Allocator.getDataServices().getStudentFromLogin(student2Login).getDbId());
        
        //make sure that the two lists are the same
        assertTrue(expectedBlacklist.containsAll(returnedBlacklist));
        assertTrue(returnedBlacklist.containsAll(expectedBlacklist));
        assertEquals(expectedBlacklist.size(), returnedBlacklist.size());
    }

    /**
     * Test of unblacklistStudents method, of class DBWrapper.
     */
    @Test
    public void testUnblacklistStudents() throws SQLException, ServicesException {

        /* setup data in DB
         */
        TA ta = ConfigurationData.generateRandomTA();

        String student1Login = ConfigurationData.generateRandomString();
        String student2Login = ConfigurationData.generateRandomString();
        String student3Login = ConfigurationData.generateRandomString();
        String student4Login = ConfigurationData.generateRandomString();
        Allocator.getDataServices().addStudent(student1Login, "FName", "LName", ValidityCheck.BYPASS);
        Allocator.getDataServices().addStudent(student2Login, "FName2", "LName2", ValidityCheck.BYPASS);
        Allocator.getDataServices().addStudent(student3Login, "FName3", "LName3", ValidityCheck.BYPASS);
        Allocator.getDataServices().addStudent(student4Login, "FName4", "LName4", ValidityCheck.BYPASS);

        /* add the blacklist to be checked for and then remove two from the blacklist
         */
        Collection<Student> blacklist = new ArrayList<Student>();
        blacklist.add(Allocator.getDataServices().getStudentFromLogin(student1Login));
        blacklist.add(Allocator.getDataServices().getStudentFromLogin(student2Login));
        blacklist.add(Allocator.getDataServices().getStudentFromLogin(student3Login));
        blacklist.add(Allocator.getDataServices().getStudentFromLogin(student4Login));
        _database.blacklistStudents(blacklist, ta);

        Collection<Student> unblacklist = new ArrayList<Student>();
        unblacklist.add(Allocator.getDataServices().getStudentFromLogin(student2Login));
        unblacklist.add(Allocator.getDataServices().getStudentFromLogin(student4Login));
        _database.unBlacklistStudents(unblacklist, ta);

        /* get the blacklist back out
         */
        Collection<Integer> returnedBlacklist = _database.getTABlacklist(ta);

        /* make sure that the two lists are the same
         * if this is true then it was unblacklisted correctly
         */
        Collection<Integer> expected = new ArrayList<Integer>();
        expected.add(Allocator.getDataServices().getStudentFromLogin(student1Login).getDbId());
        expected.add(Allocator.getDataServices().getStudentFromLogin(student3Login).getDbId());
        assertTrue(expected.containsAll(returnedBlacklist));
        assertTrue(returnedBlacklist.containsAll(expected));
        assertEquals(expected.size(), returnedBlacklist.size());
    }

    /**
     * Test of getTABlacklist method, of class DBWrapper.
     */
    @Test
    public void testGetTABlacklist() throws SQLException, ServicesException {

        /* setup data in DB
         */
        TA ta1 = ConfigurationData.generateRandomTA();
        TA ta2 = ConfigurationData.generateRandomTA();
        TA ta3 = ConfigurationData.generateRandomTA();

        String student1Login = ConfigurationData.generateRandomString();
        String student2Login = ConfigurationData.generateRandomString();
        String student3Login = ConfigurationData.generateRandomString();
        Allocator.getDataServices().addStudent(student1Login, "FName", "LName", ValidityCheck.BYPASS);
        Allocator.getDataServices().addStudent(student2Login, "FName2", "LName2", ValidityCheck.BYPASS);
        Allocator.getDataServices().addStudent(student3Login, "FName3", "LName3", ValidityCheck.BYPASS);

        /* add a blacklist to make sure that it does not just return all the
         * students regardless of blacklistedness
         */
        Collection<Student> blacklist1 = new ArrayList<Student>();
        blacklist1.add(Allocator.getDataServices().getStudentFromLogin(student1Login));
        blacklist1.add(Allocator.getDataServices().getStudentFromLogin(student3Login));
        _database.blacklistStudents(blacklist1, ta1);

        /* add the blacklist to be checked for
         */
        Collection<Student> blacklist2 = new ArrayList<Student>();
        blacklist2.add(Allocator.getDataServices().getStudentFromLogin(student1Login));
        blacklist2.add(Allocator.getDataServices().getStudentFromLogin(student2Login));
        _database.blacklistStudents(blacklist2, ta2);
        _database.blacklistStudents(blacklist2, ta2); //verify that second add is ignored

        /* get the blacklist back out
         */
        Collection<Integer> returnedBlacklist = _database.getTABlacklist(ta2);
        Collection<Integer> expectedBlacklist = new ArrayList<Integer>(2);
        expectedBlacklist.add(Allocator.getDataServices().getStudentFromLogin(student1Login).getDbId());
        expectedBlacklist.add(Allocator.getDataServices().getStudentFromLogin(student2Login).getDbId());
        
        //make sure that the two lists are the same
        assertTrue(expectedBlacklist.containsAll(returnedBlacklist));
        assertTrue(returnedBlacklist.containsAll(expectedBlacklist));
        assertEquals(expectedBlacklist.size(), returnedBlacklist.size());

        /* make sure that a TA with no blacklist returns an empty blacklist
         */
        Collection<Integer> emptyBL = _database.getTABlacklist(ta3);
        assertTrue(emptyBL.isEmpty());
    }

    /**
     * Test of getBlacklistedStudents method, of class DBWrapper.
     */
    @Test
    public void testGetBlacklistedStudents() throws SQLException, ServicesException {

        /* setup data in DB
         */
        TA ta1 = ConfigurationData.generateRandomTA();
        TA ta2 = ConfigurationData.generateRandomTA();

        String student1Login = ConfigurationData.generateRandomString();
        String student2Login = ConfigurationData.generateRandomString();
        String student3Login = ConfigurationData.generateRandomString();
        Allocator.getDataServices().addStudent(student1Login, "FName", "LName", ValidityCheck.BYPASS);
        Allocator.getDataServices().addStudent(student2Login, "FName2", "LName2", ValidityCheck.BYPASS);
        Allocator.getDataServices().addStudent(student3Login, "FName3", "LName3", ValidityCheck.BYPASS);

        /* add a blacklist to make sure that it does not just return all the
         * blacklisted students
         */
        Collection<Student> blacklist1 = new ArrayList<Student>();
        blacklist1.add(Allocator.getDataServices().getStudentFromLogin(student1Login));
        blacklist1.add(Allocator.getDataServices().getStudentFromLogin(student3Login));
        _database.blacklistStudents(blacklist1, ta1);

        /* add a second TA's blacklist
         */
        Collection<Student> blacklist2 = new ArrayList<Student>();
        blacklist2.add(Allocator.getDataServices().getStudentFromLogin(student1Login));
        blacklist2.add(Allocator.getDataServices().getStudentFromLogin(student2Login));
        _database.blacklistStudents(blacklist2, ta2);

        /* get the blacklist backout
         */
        Collection<Integer> returnedBlacklist = _database.getBlacklistedStudents();

        /* make sure that the two lists are the same
         * if this is true then the list of blacklisted students was returned successfully
         */
        Collection<Integer> expectedBlacklistedStudents = new ArrayList<Integer>();
        expectedBlacklistedStudents.add(Allocator.getDataServices().getStudentFromLogin(student1Login).getDbId());
        expectedBlacklistedStudents.add(Allocator.getDataServices().getStudentFromLogin(student2Login).getDbId());
        expectedBlacklistedStudents.add(Allocator.getDataServices().getStudentFromLogin(student3Login).getDbId());
        assertTrue(expectedBlacklistedStudents.containsAll(returnedBlacklist));
        assertTrue(returnedBlacklist.containsAll(expectedBlacklistedStudents));
        assertEquals(expectedBlacklistedStudents.size(), returnedBlacklist.size());
    }

    /**
     * Test of isDistEmpty method, of class DBWrapper.
     */
    @Test
    public void testIsDistEmpty() throws SQLException, CakeHatDBIOException, ServicesException {

        /* setup data in DB
         */
        TA ta = ConfigurationData.generateRandomTA();

        Assignment asgn = ConfigurationData.generateAssignmentWithTwoDistributableParts();

        String student1Login = ConfigurationData.generateRandomString();
        String student2Login = ConfigurationData.generateRandomString();
        Allocator.getDataServices().addStudent(student1Login, "FName", "LName", ValidityCheck.BYPASS);
        Allocator.getDataServices().addStudent(student2Login, "FName2", "LName2", ValidityCheck.BYPASS);

        Group group = new Group("testGroup",
                                Allocator.getDataServices().getStudentFromLogin(student1Login),
                                Allocator.getDataServices().getStudentFromLogin(student2Login));
        _database.setGroup(asgn, group);

        /* verify that the dist is empty when start
         */
        assertTrue(_database.isDistEmpty(asgn));

        /* add the group to the TAs dist
         */
        _database.assignGroupToGrader(group, asgn.getDistributableParts().get(0), ta);

        /* verify that the dist is not empty now that something has been added
         */
        assertTrue(!_database.isDistEmpty(asgn));
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
        _database.addStudent(s1login, s1first, s1last);

        //adding the same student again should not cause an error
        _database.addStudent(s1login, s1first, s1last);

        Collection<StudentRecord> students = _database.getAllStudents();
        assertEquals(students.size(), 1);
        StudentRecord student1 = Iterables.get(students, 0);
        assertEquals(s1login, student1.getLogin());
        assertEquals(s1first, student1.getFirstName());
        assertEquals(s1last, student1.getLastName());

        String s2login = "arose";
        String s2first = "Anna";
        String s2last = "Rose";

        //adding the student should not cause an error
        _database.addStudent(s2login, s2first, s2last);

        //check that the database now contains 2 students
        students = _database.getAllStudents();
        assertEquals(2, students.size());

    }

    @Test
    public void testGrantExtension() throws SQLException {
        Handin handin1 = ConfigurationData.generateHandin();
        Group group1 = ConfigurationData.generateRandomGroup();
        GregorianCalendar date1 = new GregorianCalendar(2011, 5, 25, 11, 30, 0);
        for (Student student : group1.getMembers()) {
            _database.addStudent(student.getDbId(), student.getLogin(), student.getFirstName(), student.getLastName());
        }
        _database.setGroup(handin1.getAssignment(), group1);
        _database.grantExtension(group1, handin1, date1, "note1");

        assertEquals(date1, _database.getExtension(group1, handin1));

        Handin handin2 = ConfigurationData.generateHandin();
        Group group2 = ConfigurationData.generateRandomGroup();
        GregorianCalendar date2 = new GregorianCalendar(2011, 3, 15, 10, 30, 0);
        for (Student student : group2.getMembers()) {
            _database.addStudent(student.getDbId(), student.getLogin(), student.getFirstName(), student.getLastName());
        }
        _database.setGroup(handin2.getAssignment(), group2);
        _database.grantExtension(group2, handin2, date2, "note2");

        assertEquals(date2, _database.getExtension(group2, handin2));
        assertEquals("note2", _database.getExtensionNote(group2, handin2));
    }

    @Test
    public void testRemoveExtension() throws SQLException {
        Handin handin1 = ConfigurationData.generateHandin();
        Group group1 = ConfigurationData.generateRandomGroup();
        GregorianCalendar date = new GregorianCalendar(2011, 5, 25, 11, 30, 0);
        for (Student student : group1.getMembers()) {
            _database.addStudent(student.getDbId(), student.getLogin(), student.getFirstName(), student.getLastName());
        }
        _database.setGroup(handin1.getAssignment(), group1);
        assertEquals(null, _database.getExtension(group1, handin1));
        //removing extension from group without extension should not throw error
        _database.removeExtension(group1, handin1);
        assertEquals(null, _database.getExtension(group1, handin1));



        Group group2 = ConfigurationData.generateRandomGroup();
         for (Student student : group2.getMembers()) {
            _database.addStudent(student.getDbId(), student.getLogin(), student.getFirstName(), student.getLastName());
        }
        _database.setGroup(handin1.getAssignment(), group2);
        _database.grantExtension(group2, handin1, date, "note");
        assertEquals(date, _database.getExtension(group2, handin1));
        //removing extension from group with extension should not throw error
        _database.removeExtension(group2, handin1);
        assertEquals(null, _database.getExtension(group2, handin1));
    }
    
}
