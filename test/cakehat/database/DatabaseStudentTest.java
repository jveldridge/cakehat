package cakehat.database;

import cakehat.database.DatabaseTestHelpers.EqualityAsserter;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests for {@link Database} methods related to students and blacklists.
 * 
 * @author jeldridg
 */
public class DatabaseStudentTest {
    
    private Database _database;
    
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
            assertEquals(s1.hasCollabPolicy(), s2.hasCollabPolicy());
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
        assertFalse(student.hasCollabPolicy());
        
        _database.putStudents(ImmutableSet.of(student));
        Integer id = student.getId();
        assertNotNull(id);
        
        Set<DbStudent> students = _database.getStudents();
        DatabaseTestHelpers.assertSetContainsGivenElements(STUDENT_EQC, students, student);
        
        student.setLogin("different");
        student.setHasCollabPolicy(true);
        
        _database.putStudents(ImmutableSet.of(student));
        assertEquals(id, student.getId());
        
        students = _database.getStudents();
        DatabaseTestHelpers.assertSetContainsGivenElements(STUDENT_EQC, students, student);
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
