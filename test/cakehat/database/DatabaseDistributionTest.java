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
    
    @Test
    public void testAssignGroup() throws SQLException {
        DatabaseContentWrapper wrapper = new DatabaseContentWrapper(_database);
        
        _database.assignGroup(wrapper._dbGroup1.getId(), 
                              wrapper._part1.getId(), wrapper._taId1);
        _database.assignGroup(wrapper._dbGroup2.getId(), 
                              wrapper._part1.getId(), wrapper._taId1);
        Set<Integer> assignedGroupsFB = 
                _database.getAssignedGroups(wrapper._part1.getId(), wrapper._taId1);
        DatabaseTestHelpers.assertIntCollectionsEqual(ImmutableSet.of(wrapper._dbGroup1.getId(), 
                                                        wrapper._dbGroup2.getId()), 
                                       assignedGroupsFB);
        assertEquals(true, _database.getAssignedGroups(wrapper._part1.getId(), wrapper._taId2).isEmpty());
        
        _database.assignGroup(wrapper._dbGroup1.getId(), 
                              wrapper._part1.getId(), wrapper._taId2);
        assertEquals((int)wrapper._taId2, (int)_database.getGrader(wrapper._part1.getId(), wrapper._dbGroup1.getId()));
        assignedGroupsFB = _database.getAssignedGroups(wrapper._part1.getId(), wrapper._taId1);
        DatabaseTestHelpers.assertIntCollectionsEqual(ImmutableSet.of(wrapper._dbGroup2.getId()), 
                                       assignedGroupsFB);
        assignedGroupsFB = _database.getAssignedGroups(wrapper._part1.getId());
        DatabaseTestHelpers.assertIntCollectionsEqual(ImmutableSet.of(wrapper._dbGroup1.getId(), 
                                                        wrapper._dbGroup2.getId()), 
                                       assignedGroupsFB);
        
        _database.unassignGroup(wrapper._dbGroup1.getId(), wrapper._part1.getId());
        assertEquals(null, _database.getGrader(wrapper._part1.getId(), wrapper._dbGroup1.getId()));
        assertEquals(true, _database.getAssignedGroups(wrapper._part1.getId(), wrapper._taId2).isEmpty());
        assertEquals(true, _database.getPartsWithAssignedGroups(wrapper._taId2).isEmpty());
        
        Set<Integer> assignPartsDB = _database.getPartsWithAssignedGroups(wrapper._taId1);
        DatabaseTestHelpers.assertIntCollectionsEqual(assignPartsDB, ImmutableSet.of(wrapper._part1.getId()));
        assertEquals(true, _database.getAssignedGroups(wrapper._part2.getId()).isEmpty());
        
        _database.assignGroup(wrapper._dbGroup1.getId(), 
                              wrapper._part2.getId(), wrapper._taId1);
        assignPartsDB = _database.getPartsWithAssignedGroups(wrapper._taId1);
        DatabaseTestHelpers.assertIntCollectionsEqual(assignPartsDB, ImmutableSet.of(wrapper._part1.getId(), 
                                                                      wrapper._part2.getId()));
    }
    
}
