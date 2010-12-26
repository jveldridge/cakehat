package gradesystem.database;

import java.util.UUID;
import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import gradesystem.Allocator;
import static org.junit.Assert.*;

/**
 *
 * @author alexku
 */
public class DBWrapperTest {

    private DBWrapper _instance;
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
    public void setUp() {
        _connProvider = new InMemoryConnectionProvider();
        _instance = new DBWrapper(_connProvider);
        _instance.resetDatabase();
    }

    /**
     * runs after every test
     */
    @After
    public void tearDown() {
        try {
            _connProvider.closeConnection(_connProvider.createConnection());
        } catch (SQLException ex) {
            System.out.println("there was an error closing the test DB");
        }
    }

    private String generateUsername() {
        String uid = UUID.randomUUID().toString();
        uid.replaceAll("-", "");
        uid = uid.substring(8);
        return uid;
    }

    /**
     * Test of getTABlacklist method, of class DBWrapper.
     */
    @Test
    public void testGetTABlacklist() {
        System.out.println("getTABlacklist");

        //setup data in DB
        String ta = this.generateUsername();
        String student = this.generateUsername();
        _instance.addTA(ta, "Alex Unger");
        _instance.addStudent(student, "The", "Doctors");
        _instance.addStudent(this.generateUsername(), "Dasw", "Masdfasd");
        _instance.blacklistStudent(student, ta);

        //can get blacklist?
        Collection<String> expectedBlacklist = new ArrayList<String>();
        expectedBlacklist.add(student);
        Collection<String> blacklist = _instance.getTABlacklist(ta);

        assertEquals(expectedBlacklist, blacklist);
    }

    /**
     * Test of setAsgnDist method, of class DBWrapper.
     */
    @Test
    public void testSetAsgnDist() {
        System.out.println("setAsgnDist");

        //setup data in DB
        _instance.addTA("aunger", "Alex Unger");
        _instance.addTA("sefcda", "Sadfge Padfads");
        _instance.addStudent("dsr", "The", "Doctors");
        _instance.addStudent("dasf", "Daht", "Medsa");
        _instance.addStudent("ndawes", "Nads", "Haowdb");

        String asgn = "TASafeHouse";
        ArrayList<String> aunger = new ArrayList<String>();
        aunger.add("sefcda");
        aunger.add("dsr");
        ArrayList<String> sefcda = new ArrayList<String>();
        sefcda.add("ndawes");
        Map<String, Collection<String>> distribution = new HashMap<String, Collection<String>>();
        distribution.put("aunger", aunger);
        distribution.put("sefcda", sefcda);

        boolean result = false;//_instance.setAsgnDist(asgn, distribution);
        assertTrue(result);
    }

    /**
     * Test of addAssignment method, of class DBWrapper.
     */
    @Test
    public void testAddAssignment() {
        System.out.println("addAssignment");

        //no setup needed

        //can add assignment?
        Assignment asgn = (Assignment) Allocator.getCourseInfo().getHandinAssignments().toArray()[0];
        boolean result = _instance.addAssignment(asgn);
        
        assertTrue(result);
    }

    /**
     * Test of addStudent method, of class DBWrapper.
     */
    @Test
    public void testAddStudent() {
        System.out.println("addStudent");

        //setup data in DB
        _instance.addStudent("dsr", "The", "Doctors");
        _instance.addStudent("dmas", "Draws", "More");
        _instance.addStudent("nvasdsw", "Nick", "Vasdt");

        //can add student?
        String studentLogin = "hbhard";
        String studentFirstName = "CardBoard";
        String studentLastName = "Kid";
        boolean studentAdded = _instance.addStudent(studentLogin, studentFirstName, studentLastName);

        assertTrue(studentAdded);

        //can we get them back out?
        Map<String, String> all_students = _instance.getEnabledStudents();

        Map<String, String> expected_students = new HashMap<String, String>();
        expected_students.put("dsr", "The Doctors");
        expected_students.put("nvasdsw", "Nick Vasdt");
        expected_students.put("dmas", "Draws More");
        expected_students.put(studentLogin, studentFirstName + " " + studentLastName);
       
        assertEquals(expected_students, all_students);
    }

    /**
     * Test of disableStudent method, of class DBWrapper.
     */
    @Test
    public void testDisableStudent() {
        System.out.println("disableStudent");
        String studentLogin = "hbhard";
        boolean expResult = true;
        boolean result = _instance.disableStudent(studentLogin);
        assertEquals(expResult, result);
        Map<String, String> result2 = _instance.getEnabledStudents();
        Map<String, String> expResult2 = new HashMap<String, String>();
        expResult2.put("drs", "The Doctors");
        assertEquals(expResult2, result2);
    }

    /**
     * Test of enableStudent method, of class DBWrapper.
     */
    @Test
    public void testEnableStudent() {
        System.out.println("enableStudent");

        //setup data in DB
        _instance.addStudent("dsr", "The", "Doctors");
        _instance.addStudent("dmore", "Draws", "More");
        _instance.addStudent("nvasdsw", "Nick", "Vasdt");
        _instance.addStudent("hbhard", "CardBoard", "Kid");
        _instance.disableStudent("hbhard");

        //can enable student?
        String studentLogin = "hbhard";
        boolean canEnable = _instance.enableStudent(studentLogin);
        assertTrue(canEnable);

        //has the enable happened?
        Map<String, String> enabledStuds = _instance.getEnabledStudents();

        Map<String, String> expectedEnabled = new HashMap<String, String>();
        expectedEnabled.put("dsr", "The Doctors");
        expectedEnabled.put("dmore", "Draws More");
        expectedEnabled.put("nvasdsw", "Nick Vasdt");
        expectedEnabled.put("hbhard", "CardBoard Kid");
        assertEquals(expectedEnabled, enabledStuds);
    }

    /**
     * Test of addTA method, of class DBWrapper.
     */
    @Test
    public void testAddTA() {
        System.out.println("addTA");
        boolean expResult = true;
        boolean result = false;//_instance.addTA(taLogin);
        assertEquals(expResult, result);
    }

    /**
     * Test of geAlltStudents method, of class DBWrapper.
     */
    @Test
    public void testGetAllStudents() {
        System.out.println("getAllStudents");

        //setup data in DB
        _instance.addStudent("dsr", "The", "Doctors");
        _instance.addStudent("dmore", "Draws", "More");
        _instance.addStudent("nvasdsw", "Nick", "Vasdt");
        _instance.addStudent("hbhard", "CardBoard", "Kid");
        _instance.disableStudent("hbhard");

        //did all the students get returned?
        Map<String, String> result = _instance.getAllStudents();
        Map<String, String> expResult = new HashMap<String, String>();
        expResult.put("dsr", "The Doctors");
        expResult.put("dmore", "Draws More");
        expResult.put("nvasdsw", "Nick Vasdt");
        expResult.put("hbhard", "CardBoard Kid");
        assertEquals(expResult, result);
    }

    /**
     * Test of blacklistStudent method, of class DBWrapper.
     */
    @Test
    public void testBlacklistStudent() {
        System.out.println("blacklistStudent");
        String studentLogin = "drs";
        String taLogin = "ashley";
        boolean expResult = true;
        boolean result = _instance.blacklistStudent(studentLogin, taLogin);
        assertEquals(expResult, result);
        Collection<String> result2 = _instance.getTABlacklist(taLogin);
        Collection<String> expResult2 = new ArrayList<String>();
        expResult2.add(studentLogin);
        assertEquals(expResult2, result2);
    }

    /**
     * Test of isDistEmpty method, of class DBWrapper.
     */
    @Test
    public void testIsDistEmpty() {
        System.out.println("isDistEmpty");
        HandinPart asgn = ((Assignment) Allocator.getCourseInfo().getHandinAssignments().toArray()[0]).getHandinPart();
        System.out.println(asgn.getAssignment().getName());
        boolean expResult = false;
        boolean result = _instance.isDistEmpty(asgn);
        assertEquals(expResult, result);
        HandinPart asgn2 = ((Assignment) Allocator.getCourseInfo().getHandinAssignments().toArray()[1]).getHandinPart();
        boolean expResult2 = true;
        boolean result2 = _instance.isDistEmpty(asgn2);
        assertEquals(expResult2, result2);
    }

    /**
     * Test of getBlacklistedStudents method, of class DBWrapper.
     */
    @Test
    public void testGetBlacklistedStudents() {
        System.out.println("getBlacklistedStudents");
        Collection<String> expResult = new HashSet<String>();
        expResult.add("drs");
        Collection<String> result = _instance.getBlacklistedStudents();
        assertEquals(expResult, result);
    }
//
//    /**
//     * Test of assignStudentToGrader method, of class DBWrapper.
//     */
//    @Test
//    public void testAssignStudentToGrader() {
//        System.out.println("assignStudentToGrader");
//        String studentLogin = "";
//        String assignmentName = "";
//        String taLogin = "";
//        boolean expResult = false;
//        boolean result = instance.assignStudentToGrader(studentLogin, assignmentName, taLogin);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of unassignStudentFromGrader method, of class DBWrapper.
//     */
//    @Test
//    public void testUnassignStudentFromGrader() {
//        System.out.println("unassignStudentFromGrader");
//        String studentLogin = "";
//        String assignmentName = "";
//        String taLogin = "";
//        boolean expResult = false;
//        boolean result = instance.unassignStudentFromGrader(studentLogin, assignmentName, taLogin);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStudentsAssigned method, of class DBWrapper.
//     */
//    @Test
//    public void testGetStudentsAssigned() {
//        System.out.println("getStudentsAssigned");
//        String assignmentName = "";
//        String taLogin = "";
//        Collection expResult = null;
//        Collection result = instance.getStudentsAssigned(assignmentName, taLogin);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of grantExtension method, of class DBWrapper.
//     */
//    @Test
//    public void testGrantExtension() {
//        System.out.println("grantExtension");
//        String studentLogin = "";
//        String assignmentName = "";
//        Calendar newDate = null;
//        String note = "";
//        boolean expResult = false;
//        boolean result = instance.grantExtension(studentLogin, assignmentName, newDate, note);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of grantExemption method, of class DBWrapper.
//     */
//    @Test
//    public void testGrantExemption() {
//        System.out.println("grantExemption");
//        String studentLogin = "";
//        String assignmentName = "";
//        String note = "";
//        boolean expResult = false;
//        boolean result = instance.grantExemption(studentLogin, assignmentName, note);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getExtension method, of class DBWrapper.
//     */
//    @Test
//    public void testGetExtension() {
//        System.out.println("getExtension");
//        String studentLogin = "";
//        String assignmentName = "";
//        Calendar expResult = null;
//        Calendar result = instance.getExtension(studentLogin, assignmentName);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getExtensionNote method, of class DBWrapper.
//     */
//    @Test
//    public void testGetExtensionNote() {
//        System.out.println("getExtensionNote");
//        String studentLogin = "";
//        String assignmentName = "";
//        String expResult = "";
//        String result = instance.getExtensionNote(studentLogin, assignmentName);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getExemptionNote method, of class DBWrapper.
//     */
//    @Test
//    public void testGetExemptionNote() {
//        System.out.println("getExemptionNote");
//        String studentLogin = "";
//        String assignmentName = "";
//        String expResult = "";
//        String result = instance.getExemptionNote(studentLogin, assignmentName);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of enterGrade method, of class DBWrapper.
//     */
//    @Test
//    public void testEnterGrade() {
//        System.out.println("enterGrade");
//        String studentLogin = "";
//        String assignmentName = "";
//        double score = 0.0;
//        TimeStatus status = null;
//        boolean expResult = false;
//        boolean result = instance.enterGrade(studentLogin, assignmentName, score, status);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStudentScore method, of class DBWrapper.
//     */
//    @Test
//    public void testGetStudentScore() {
//        System.out.println("getStudentScore");
//        String studentLogin = "";
//        String assignmentName = "";
//        double expResult = 0.0;
//        double result = instance.getStudentScore(studentLogin, assignmentName);
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getNumberOfGrades method, of class DBWrapper.
//     */
//    @Test
//    public void testGetNumberOfGrades() {
//        System.out.println("getNumberOfGrades");
//        String assignmentName = "";
//        int expResult = 0;
//        int result = instance.getNumberOfGrades(assignmentName);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of exportDatabase method, of class DBWrapper.
//     */
//    @Test
//    public void testExportDatabase() {
//        System.out.println("exportDatabase");
//        File exportFile = null;
//        boolean expResult = false;
//        boolean result = instance.exportDatabase(exportFile);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of resetDatabase method, of class DBWrapper.
//     */
//    @Test
//    public void testResetDatabase() {
//        System.out.println("resetDatabase");
//        boolean expResult = false;
//        boolean result = instance.resetDatabase();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
