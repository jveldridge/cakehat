package cakehat.database;

import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import com.google.common.collect.HashMultimap;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests for {@link Database} methods related to distribution.
 * 
 * @author jeldridg
 */
public class DatabaseDistributionTest {
    
    private Database _database;
    private DbPart _part;
    private DbTA _ta;
    private DbGroup _group;
    
    public DatabaseDistributionTest() throws IOException {
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
        
        _ta = new DbTA(1, "talogin", "first", "last", true, true);
        _database.putTAs(ImmutableSet.of(_ta));
        
        DbAssignment asgn = new DbAssignment("asgn", 1);
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _part = DbPart.build(ge, "part", 1);
        _database.putAssignments(ImmutableSet.of(asgn));
        _database.putGradableEvents(ImmutableSet.of(ge));
        _database.putParts(ImmutableSet.of(_part));
        
        DbStudent student = new DbStudent("login", "first", "last", "email");
        _database.putStudents(ImmutableSet.of(student));
        _group = new DbGroup(asgn.getId(), "group", ImmutableSet.of(student.getId()));
        _database.putGroups(ImmutableSet.of(_group));
    }
    
    @Test
    public void testSetGetDistributionForOneGroupForOnePart() throws SQLException {
        Map<Integer, SetMultimap<Integer, Integer>> dist = new HashMap<Integer, SetMultimap<Integer, Integer>>();
        dist.put(_part.getId(), HashMultimap.<Integer, Integer>create());
        dist.get(_part.getId()).put(_ta.getId(), _group.getId());
        
        _database.setDistribution(dist);
        
        Map<Integer, SetMultimap<Integer, Integer>> distFromDb = _database.getDistribution();
        assertEquals(1, distFromDb.size());
        assertTrue(distFromDb.containsKey(_part.getId()));
        assertEquals(1, distFromDb.get(_part.getId()).size());
        assertTrue(distFromDb.get(_part.getId()).containsKey(_ta.getId()));
        assertEquals(1, distFromDb.get(_part.getId()).get(_ta.getId()).size());
        assertTrue(distFromDb.get(_part.getId()).get(_ta.getId()).contains(_group.getId()));
    }
}