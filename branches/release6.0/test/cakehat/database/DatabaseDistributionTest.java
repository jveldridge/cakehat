package cakehat.database;

import cakehat.database.DatabaseTestHelpers.DatabaseContentWrapper;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.Map;
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
    public void testIsDistInitiallyEmptyForSinglePart() throws SQLException {
        assertTrue(_database.isDistEmpty(ImmutableSet.of(_part.getId())));
    }
    
    @Test
    public void testIsDistEmptyAfterAssigningOneGroupForOnePart() throws SQLException {       
        Map<Integer, Map<Integer, Set<Integer>>> dist = new HashMap<Integer, Map<Integer, Set<Integer>>>();
        dist.put(_part.getId(), new HashMap<Integer, Set<Integer>>());
        dist.get(_part.getId()).put(_ta.getId(), ImmutableSet.of(_group.getId()));
        
        _database.setDistribution(dist);
        
        assertFalse(_database.isDistEmpty(ImmutableSet.of(_part.getId())));
    }
    
    @Test
    public void testSetGetDistributionForOneGroupForOnePart() throws SQLException {
        Map<Integer, Map<Integer, Set<Integer>>> dist = new HashMap<Integer, Map<Integer, Set<Integer>>>();
        dist.put(_part.getId(), new HashMap<Integer, Set<Integer>>());
        dist.get(_part.getId()).put(_ta.getId(), ImmutableSet.of(_group.getId()));
        
        _database.setDistribution(dist);
        
        SetMultimap<Integer, Integer> distFromDb = _database.getDistribution(_part.getId());
        assertEquals(1, distFromDb.size());
        assertTrue(distFromDb.containsKey(_part.getId()));
        assertEquals(1, distFromDb.get(_part.getId()).size());
        assertEquals(_group.getId(), distFromDb.get(_part.getId()).iterator().next());
    }
    
    @Test
    public void testDistribution() throws SQLException {
        DatabaseContentWrapper wrapper = new DatabaseContentWrapper(_database);
        
        assertEquals(true, _database.isDistEmpty(wrapper._partIDs));
        assertEquals(true, _database.getDistribution(wrapper._part1.getId()).isEmpty());  
        
        Map<Integer, Map<Integer, Set<Integer>>> distribution =
                new HashMap<Integer, Map<Integer, Set<Integer>>>();
        Map<Integer, Set<Integer>> taToGroups = new HashMap<Integer, Set<Integer>>();
        taToGroups.put(wrapper._taId1, 
                ImmutableSet.of(wrapper._dbGroup1.getId(), wrapper._dbGroup2.getId()));
        distribution.put(wrapper._part1.getId(), taToGroups);
        _database.setDistribution(distribution);
        
        assertEquals(false, _database.isDistEmpty(wrapper._partIDs));
        assertEquals(true, _database.getDistribution(wrapper._part2.getId()).isEmpty());
        
        SetMultimap<Integer, Integer> part1Dist = _database.getDistribution(wrapper._part1.getId());
        assertEquals(1, part1Dist.keySet().size());
        assertEquals(true, part1Dist.containsKey(wrapper._taId1));
        assertEquals(2, part1Dist.get(wrapper._taId1).size());
    }
    
}
