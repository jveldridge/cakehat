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
 * Tests for {@link Database} methods related to students.
 * 
 * @author jeldridg
 */
public class DatabaseTaTest {
    
    private Database _database;

    public DatabaseTaTest() throws IOException {
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
    
    private final EqualityAsserter<DbTA> DB_TA_EQ_C = new EqualityAsserter<DbTA>() {
        @Override
        public void assertEqual(DbTA t1, DbTA t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getLogin(), t2.getLogin());
            assertEquals(t1.getFirstName(), t2.getFirstName());
            assertEquals(t1.getLastName(), t2.getLastName());
            assertEquals(t1.isDefaultGrader(), t2.isDefaultGrader());
            assertEquals(t1.isAdmin(), t2.isAdmin());
        }
    };
    
    @Test
    public void testPutGetSingleTA() throws SQLException {
        int taId = 1;
        DbTA ta = new DbTA(taId, "login", "first", "last", true, false);
        
        _database.putTAs(ImmutableSet.of(ta));
        //ID should not have changed
        assertEquals(taId, (int) ta.getId());
        
        Set<DbTA> tas = _database.getTAs();
        assertEquals(1, tas.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_TA_EQ_C, tas, ta);
    }
    
    @Test
    public void testUpdateTA() throws SQLException {
       int taId = 1;
        DbTA ta = new DbTA(taId, "login", "first", "last", true, false);
        
        _database.putTAs(ImmutableSet.of(ta));
        //ID should not have changed
        assertEquals(taId, (int) ta.getId());
        
        Set<DbTA> tas = _database.getTAs();
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_TA_EQ_C, tas, ta); 
        
        ta.setLogin("newlogin");
        ta.setFirstName("New");
        ta.setLastName("Login");
        ta.setIsAdmin(true);
        ta.setIsDefaultGrader(false);
        
        _database.putTAs(ImmutableSet.of(ta));
        tas = _database.getTAs();
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_TA_EQ_C, tas, ta); 
    }
}
