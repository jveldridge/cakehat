/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author alexku
 */
public class DBWrapperTest {

    private static Connection _connection;
    private static Statement _statement;
    private static String _dbPath = "jdbc:sqlite:/Users/alexku/NetBeansProjects/cakehat/db/testDB.sqlite";
    private static String _testDataPath = "/Users/alexku/NetBeansProjects/cakehat/db/cakehat_test_data.sql";
    private DBWrapper _instance;

    public DBWrapperTest() {
        _instance = new DBWrapper(_dbPath);
    }

    /**
     * runs before any tests
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        _connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            _connection = DriverManager.getConnection(_dbPath);

            _statement = _connection.createStatement();
            _statement.setQueryTimeout(30);  // set timeout to 30 sec.

        } catch (Exception e) {
            e.printStackTrace();
        }

        File sql = new File(_testDataPath);

        try {
            BufferedReader input = new BufferedReader(new FileReader(sql));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    _statement.addBatch(line);
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        _statement.executeBatch();
        try {
            if (_connection != null) {
                _connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    }

    /**
     * runs after every test
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of setAsgnDist method, of class DBWrapper.
     */
    @Test
    public void testSetAsgnDist() {
        System.out.println("setAsgnDist");
        String asgn = "TASafeHouse";
        ArrayList<String> aunger = new ArrayList<String>();
        aunger.add("dmorrill");
        aunger.add("drs");
        ArrayList<String> spoletto = new ArrayList<String>();
        spoletto.add("nvarone");
        Map<String, ArrayList<String>> distribution = new HashMap<String, ArrayList<String>>();
        distribution.put("aunger", aunger);
        distribution.put("spoletto", spoletto);

        boolean result = _instance.setAsgnDist(asgn, distribution);
        assertTrue(result);
    }

    /**
     * Test of getTABlacklist method, of class DBWrapper.
     */
    @Test
    public void testGetTABlacklist() {
        System.out.println("getTABlacklist");
        String taLogin = "aunger";
        Collection<String> expResult = new ArrayList<String>();
        expResult.add("drs");
        Collection<String> result = _instance.getTABlacklist(taLogin);
        assertEquals(expResult, result);
    }

    /**
     * Test of addAssignment method, of class DBWrapper.
     */
    @Test
    public void testAddAssignment() {
        System.out.println("addAssignment");
        String assignmentName = "newAsgn";
        boolean expResult = true;
        boolean result = _instance.addAssignment(assignmentName);
        assertEquals(expResult, result);
    }

    /**
     * Test of addStudent method, of class DBWrapper.
     */
    @Test
    public void testAddStudent() {
        System.out.println("addStudent");
        String studentLogin = "hbhardin";
        String studentFirstName = "CardBoard";
        String studentLastName = "Kid";
        boolean expResult = true;
        boolean result = _instance.addStudent(studentLogin, studentFirstName, studentLastName);
        assertEquals(expResult, result);
        Map<String, String> result2 = _instance.getStudents();
        Map<String, String> expResult2 = new HashMap<String, String>();
        expResult2.put("drs", "The Doctors");
        expResult2.put("dmorrill", "Drew Morrill");
        expResult2.put("nvarone", "Nick Varone");
        expResult2.put("hbhardin", "CardBoard Kid");
        assertEquals(result2, expResult2);
    }
//
//    /**
//     * Test of disableStudent method, of class DBWrapper.
//     */
//    @Test
//    public void testDisableStudent() {
//        System.out.println("disableStudent");
//        String studentLogin = "";
//        DBWrapper instance = new DBWrapper();
//        boolean expResult = false;
//        boolean result = instance.disableStudent(studentLogin);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of enableStudent method, of class DBWrapper.
//     */
//    @Test
//    public void testEnableStudent() {
//        System.out.println("enableStudent");
//        String studentLogin = "";
//        DBWrapper instance = new DBWrapper();
//        boolean expResult = false;
//        boolean result = instance.enableStudent(studentLogin);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addTA method, of class DBWrapper.
//     */
//    @Test
//    public void testAddTA() {
//        System.out.println("addTA");
//        String taLogin = "";
//        String taFirstName = "";
//        String taLastName = "";
//        String type = "";
//        DBWrapper instance = new DBWrapper();
//        boolean expResult = false;
//        boolean result = instance.addTA(taLogin, taFirstName, taLastName, type);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStudents method, of class DBWrapper.
//     */
//    @Test
//    public void testGetStudents() {
//        System.out.println("getStudents");
//        DBWrapper instance = new DBWrapper();
//        Map expResult = null;
//        Map result = instance.getStudents();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of blacklistStudent method, of class DBWrapper.
//     */
//    @Test
//    public void testBlacklistStudent() {
//        System.out.println("blacklistStudent");
//        String studentLogin = "";
//        String taLogin = "";
//        DBWrapper instance = new DBWrapper();
//        boolean expResult = false;
//        boolean result = instance.blacklistStudent(studentLogin, taLogin);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isDistEmpty method, of class DBWrapper.
//     */
//    @Test
//    public void testIsDistEmpty() {
//        System.out.println("isDistEmpty");
//        String asgn = "";
//        DBWrapper instance = new DBWrapper();
//        boolean expResult = false;
//        boolean result = instance.isDistEmpty(asgn);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBlacklistedStudents method, of class DBWrapper.
//     */
//    @Test
//    public void testGetBlacklistedStudents() {
//        System.out.println("getBlacklistedStudents");
//        DBWrapper instance = new DBWrapper();
//        Collection expResult = null;
//        Collection result = instance.getBlacklistedStudents();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
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
//        DBWrapper instance = new DBWrapper();
//        boolean expResult = false;
//        boolean result = instance.resetDatabase();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
