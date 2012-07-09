package cakehat.database;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import java.util.Map;
import cakehat.database.DatabaseTestHelpers.EqualityAsserter;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests for {@link Database} methods related to students and blacklists.
 * 
 * @author jeldridg
 */
public class DatabaseStudentTest {
    
    private Database _database;
    
    @Rule
    public ExpectedException _thrown = ExpectedException.none();
    
    public DatabaseStudentTest() throws IOException {
        _database = new DatabaseImpl(Allocator.getFileSystemUtilities().createTempFile("tempDB", "db"));

        SingletonAllocation<Database> dbioAlloc =
            new SingletonAllocation<Database>() {
                @Override
                public Database allocate() { return _database; };
            };
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();
    }
    
    @Before
    public void setUp() throws SQLException {
        _database.resetDatabase();
    }
    
    private final EqualityAsserter<DbStudent> STUDENT_EQC = new EqualityAsserter<DbStudent>() {
        @Override
        public void assertEqual(DbStudent s1, DbStudent s2) {
            assertEquals(s1.getId(), s2.getId());
            assertEquals(s1.getFirstName(), s2.getFirstName());
            assertEquals(s1.getLastName(), s2.getLastName());
            assertEquals(s1.getEmailAddress(), s2.getEmailAddress());
            assertEquals(s1.isEnabled(), s2.isEnabled());
            assertEquals(s1.hasCollabContract(), s2.hasCollabContract());
        }
    };
    
    @Test
    public void testPutGetSingleStudent() throws SQLException {
        DbStudent student = new DbStudent("login", "first", "last", "email");
        
        _database.putStudents(ImmutableSet.of(student));
        assertNotNull(student.getId());
        
        Set<DbStudent> students = _database.getStudents();
        assertEquals(1, students.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(STUDENT_EQC, students, student);
    }
    
    @Test
    public void testUpdateSingleStudent() throws SQLException {
        DbStudent student = new DbStudent("login", "first", "last", "email");
        assertTrue(student.isEnabled());
        assertFalse(student.hasCollabContract());
        
        _database.putStudents(ImmutableSet.of(student));
        Integer id = student.getId();
        assertNotNull(id);
        
        Set<DbStudent> students = _database.getStudents();
        DatabaseTestHelpers.assertSetContainsGivenElements(STUDENT_EQC, students, student);
        
        student.setLogin("different");
        student.setHasCollabContract(true);
        
        _database.putStudents(ImmutableSet.of(student));
        assertEquals(id, student.getId());
        
        students = _database.getStudents();
        DatabaseTestHelpers.assertSetContainsGivenElements(STUDENT_EQC, students, student);
    }
    
    @Test
    public void testDisableSingleStudent() throws SQLException {       
        int studentId = DatabaseTestHelpers.createStudentGetId(_database, "sLogin1", "sFirst1", "sLast1", "sEmail1");

        _database.setStudentsAreEnabled(ImmutableMap.of(studentId, false));
        
        assertFalse(DatabaseTestHelpers.getDbDataItemFromIterableById(_database.getStudents(), studentId).isEnabled());
    }
    
    @Test
    public void testEnableSingleStudentAfterDisabling() throws SQLException {
        int studentId = DatabaseTestHelpers.createStudentGetId(_database, "sLogin1", "sFirst1", "sLast1", "sEmail1");
        
        _database.setStudentsAreEnabled(ImmutableMap.of(studentId, false));
        _database.setStudentsAreEnabled(ImmutableMap.of(studentId, true));
        
        assertTrue(DatabaseTestHelpers.getDbDataItemFromIterableById(_database.getStudents(), studentId).isEnabled());
    }
    
    @Test
    public void testEnablingAndDisablingMultipleStudents() throws SQLException {
        int student1Id = DatabaseTestHelpers.createStudentGetId(_database, "sLogin1", "sFirst1", "sLast1", "sEmail1");
        int student2Id = DatabaseTestHelpers.createStudentGetId(_database, "sLogin2", "sFirst2", "sLast2", "sEmail2");
        int student3Id = DatabaseTestHelpers.createStudentGetId(_database, "sLogin3", "sFirst3", "sLast3", "sEmail3");
        int student4Id = DatabaseTestHelpers.createStudentGetId(_database, "sLogin4", "sFirst4", "sLast4", "sEmail4");
        
        Map<Integer, Boolean> toUpdate = new HashMap<Integer, Boolean>();
        toUpdate.put(student1Id, false);
        toUpdate.put(student2Id, true);
        toUpdate.put(student3Id, true);
        toUpdate.put(student4Id, false);
        
        _database.setStudentsAreEnabled(toUpdate);
        Set<DbStudent> students = _database.getStudents();
        assertFalse(DatabaseTestHelpers.getDbDataItemFromIterableById(students, student1Id).isEnabled());
        assertTrue(DatabaseTestHelpers.getDbDataItemFromIterableById(students, student2Id).isEnabled());
        assertTrue(DatabaseTestHelpers.getDbDataItemFromIterableById(students, student3Id).isEnabled());
        assertFalse(DatabaseTestHelpers.getDbDataItemFromIterableById(students, student4Id).isEnabled());
    }
    
    @Test
    public void testDisablingInvalidStudentHasNoEffect() throws SQLException {      
        int badStudentId = -1;
        
        //no exception should be thrown
        _database.setStudentsAreEnabled(ImmutableMap.of(badStudentId, false));
    }
    
    @Test
    public void testDisabledInvalidStudentHasNoEffectAndOtherStudentsAreStillDisabled() throws SQLException {
        int badStudentId = DatabaseTestHelpers.createStudentGetId(_database, "badLogin", "badFirst", "badLast", "badEmail");
        int student1Id = DatabaseTestHelpers.createStudentGetId(_database, "sLogin1", "sFirst1", "sLast1", "sEmail1");
        int student2Id = DatabaseTestHelpers.createStudentGetId(_database, "sLogin2", "sFirst2", "sLast2", "sEmail2");
        
        _database.setStudentsAreEnabled(ImmutableMap.of(student1Id, false, badStudentId, false, student2Id, false));
        
        Set<DbStudent> students = _database.getStudents();
        assertFalse(DatabaseTestHelpers.getDbDataItemFromIterableById(students, student1Id).isEnabled());
        assertFalse(DatabaseTestHelpers.getDbDataItemFromIterableById(students, student2Id).isEnabled());
    }
    
    @Test
    public void testNullEnabledValueThrowsException() throws SQLException {
        _thrown.expect(NullPointerException.class);
        _thrown.expectMessage("Enabled value may not be set to null.");
        
        int studentId = DatabaseTestHelpers.createStudentGetId(_database, "sLogin1", "sFirst1", "sLast1", "sEmail1");
        
        Map<Integer, Boolean> toUpdate = new HashMap<Integer, Boolean>();
        toUpdate.put(studentId, null);
        _database.setStudentsAreEnabled(toUpdate);
    }
    
    @Test
    public void testNoStudentsUpdatedWhenNullValueGiven() throws SQLException {
        _thrown.expect(NullPointerException.class);
        _thrown.expectMessage("Enabled value may not be set to null.");
        
        int student1Id = DatabaseTestHelpers.createStudentGetId(_database, "sLogin1", "sFirst1", "sLast1", "sEmail1");
        int student2Id = DatabaseTestHelpers.createStudentGetId(_database, "sLogin2", "sFirst2", "sLast2", "sEmail2");
        
        Map<Integer, Boolean> toUpdate = new HashMap<Integer, Boolean>();
        toUpdate.put(student1Id, false);
        toUpdate.put(student2Id, null);
        
        _database.setStudentsAreEnabled(toUpdate);
        assertTrue(DatabaseTestHelpers.getDbDataItemFromIterableById(_database.getStudents(), student1Id).isEnabled());
        assertTrue(DatabaseTestHelpers.getDbDataItemFromIterableById(_database.getStudents(), student2Id).isEnabled());
    }
    
    @Test
    public void testSingleStudentHasCollabTrue() throws SQLException {       
        int studentId = DatabaseTestHelpers.createStudentGetStudent(_database);
        
        _database.setStudentsHasCollaborationContract(ImmutableMap.of(studentId, true));
        assertTrue(DatabaseTestHelpers.getDbDataItemFromIterableById(_database.getStudents(), studentId)
                .hasCollabContract());
    }
    
    @Test
    public void testSingleStudentHasCollabFalseAfterHasCollabTrue() throws SQLException {
        int studentId = DatabaseTestHelpers.createStudentGetStudent(_database);
        
        _database.setStudentsHasCollaborationContract(ImmutableMap.of(studentId, true));
        _database.setStudentsHasCollaborationContract(ImmutableMap.of(studentId, false));
        assertFalse(DatabaseTestHelpers.getDbDataItemFromIterableById(_database.getStudents(), studentId)
                .hasCollabContract());
    }
    
    @Test
    public void testMultipleStudentHasCollab() throws SQLException {
        Map<Integer, Boolean> toUpdate = ImmutableMap.of(
                DatabaseTestHelpers.createStudentGetStudent(_database), false,
                DatabaseTestHelpers.createStudentGetStudent(_database), true,
                DatabaseTestHelpers.createStudentGetStudent(_database), true,
                DatabaseTestHelpers.createStudentGetStudent(_database), false);
        
        _database.setStudentsHasCollaborationContract(toUpdate);
        Set<DbStudent> students = _database.getStudents();
        for(int id : toUpdate.keySet())
        {
            assertEquals(toUpdate.get(id), 
                DatabaseTestHelpers.getDbDataItemFromIterableById(students, id).hasCollabContract());
        }
    }
    
    @Test
    public void testHasCollabInvalidStudentHasNoEffect() throws SQLException {
        //no exception should be thrown for invalid student id of -1
        _database.setStudentsAreEnabled(ImmutableMap.of(-1, false));
    }
    
    @Test
    public void testHasCollabInvalidStudentHasNoEffectAndOtherStudentsAreUpdatedCorrectly() throws SQLException {
        int badStudentId = -1;
        int student1Id = DatabaseTestHelpers.createStudentGetStudent(_database);
        int student2Id = DatabaseTestHelpers.createStudentGetStudent(_database);
        
        _database.setStudentsHasCollaborationContract(
                ImmutableMap.of(student1Id, true, badStudentId, false, student2Id, true));
        
        Set<DbStudent> students = _database.getStudents();
        assertTrue(DatabaseTestHelpers.getDbDataItemFromIterableById(students, student1Id).hasCollabContract());
        assertTrue(DatabaseTestHelpers.getDbDataItemFromIterableById(students, student2Id).hasCollabContract());
    }    
    
    @Test
    public void testNullHasCollabKeyThrowsException() throws SQLException {
        _thrown.expect(NullPointerException.class);
        _thrown.expectMessage("Collaboration contract map may not contain null key or value");
        
        Map<Integer, Boolean> toUpdate = new HashMap<Integer, Boolean>();
        toUpdate.put(null, true);
        _database.setStudentsHasCollaborationContract(toUpdate);
    }
    
    @Test
    public void testNullHasCollabValueThrowsException() throws SQLException {
        _thrown.expect(NullPointerException.class);
        _thrown.expectMessage("Collaboration contract map may not contain null key or value");
        
        int studentId = DatabaseTestHelpers.createStudentGetStudent(_database);
        
        Map<Integer, Boolean> toUpdate = new HashMap<Integer, Boolean>();
        toUpdate.put(studentId, null);
        _database.setStudentsHasCollaborationContract(toUpdate);
    }
    
    @Test
    public void testNoStudentsHasCollabUpdatedWhenNullValueGiven() throws SQLException {
        _thrown.expect(NullPointerException.class);
        _thrown.expectMessage("Collaboration contract map may not contain null key or value");
        
        int student1Id = DatabaseTestHelpers.createStudentGetStudent(_database);
        int student2Id = DatabaseTestHelpers.createStudentGetStudent(_database);
        
        Map<Integer, Boolean> toUpdate = new HashMap<Integer, Boolean>();
        toUpdate.put(student1Id, true);
        toUpdate.put(student2Id, null);
        
        _database.setStudentsHasCollaborationContract(toUpdate);
        assertFalse(DatabaseTestHelpers.getDbDataItemFromIterableById(_database.getStudents(), student1Id)
                .hasCollabContract());
        assertFalse(DatabaseTestHelpers.getDbDataItemFromIterableById(_database.getStudents(), student2Id)
                .hasCollabContract());
    }
    
    @Test
    public void testBlacklist() throws SQLException {
        int taId = 1;
        DbTA ta = new DbTA(taId, "taLogin", "taFirst", "taLast", true, false);
        _database.putTAs(ImmutableSet.of(ta));
        
        DbStudent student1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent student2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(student1, student2));
        
        Set<Integer> studentIDs = ImmutableSet.of(student1.getId(), student2.getId());
        _database.blacklistStudents(studentIDs, taId);
        Set<Integer> blacklistedStudents = _database.getBlacklistedStudents();
        DatabaseTestHelpers.assertIntCollectionsEqual(blacklistedStudents, studentIDs);
        
          
        _database.unBlacklistStudents(ImmutableSet.of(student1.getId()), taId);
        blacklistedStudents = _database.getBlacklistedStudents();
        assertEquals(blacklistedStudents.size(), 1);
        
        Set<Integer> blacklist = _database.getBlacklist(taId);
        assertEquals(blacklist.size(), 1);
        assertEquals(student2.getId(), blacklist.iterator().next());
    }
}