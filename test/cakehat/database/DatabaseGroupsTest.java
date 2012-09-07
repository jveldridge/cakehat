package cakehat.database;

import java.util.Collections;
import cakehat.assignment.Assignment;
import com.google.common.collect.Iterables;
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
 * Tests for {@link Database} methods related to groups.
 * 
 * @author jeldridg
 */
public class DatabaseGroupsTest {
    
    private Database _database;
    
    public DatabaseGroupsTest() throws IOException {
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
    public void testGetAllGroupsEmpty() throws SQLException {
        Set<DbGroup> groups = _database.getGroups();
        assertEquals(0,groups.size());
    }
    
    @Test
    public void testGetGroupWithNoStudents() throws SQLException {
        Assignment asgn = DatabaseTestHelpers.createNewAssignmentInDb(_database, "asgn", 1);
        
        DbGroup toAdd = new DbGroup(asgn, "group", Collections.<Student>emptySet());
        _database.putGroups(ImmutableSet.of(toAdd));
        assertNotNull(toAdd.getId());
        
        Set<DbGroup> groups = _database.getGroups();
        
        // check that there is only 1 group in the database
        assertEquals(1, groups.size());
        
        DbGroup actualRecord = Iterables.get(groups, 0);
        
        // check that it is the group that was added by checking each field of group record
        assertEquals(toAdd.getId(), actualRecord.getId());
        assertEquals(toAdd.getAssignmentId(), actualRecord.getAssignmentId());
        assertEquals(toAdd.getName(), actualRecord.getName());
        
        DatabaseTestHelpers.assertSetsEqual(toAdd.getMemberIds(), actualRecord.getMemberIds());
    }
    
    @Test
    public void testGetAllGroupsWithOneGroup() throws SQLException {
        Assignment asgn = DatabaseTestHelpers.createNewAssignmentInDb(_database, "asgn", 1);

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        Student stud = new Student(_database.getStudents().iterator().next());

        DbGroup toAdd = new DbGroup(asgn, stud);
        _database.putGroups(ImmutableSet.of(toAdd));
        assertNotNull(toAdd.getId());

        Set<DbGroup> groups = _database.getGroups();

        // check that there is only 1 group in the database
        assertEquals(1, groups.size());

        DbGroup actualRecord = Iterables.get(groups, 0);

        // check that it is the group that was added by checking each field of group record
        assertEquals(toAdd.getId(), actualRecord.getId());
        assertEquals(toAdd.getAssignmentId(), actualRecord.getAssignmentId());
        assertEquals(toAdd.getName(), actualRecord.getName());
        DatabaseTestHelpers.assertSetsEqual(toAdd.getMemberIds(), actualRecord.getMemberIds());
    }

    @Test
    public void testGetGroupsWithOneGroup() throws SQLException {
        Assignment asgn = DatabaseTestHelpers.createNewAssignmentInDb(_database, "asgn",1);

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        Student stud = new Student(_database.getStudents().iterator().next());

        DbGroup toAdd = new DbGroup(asgn, stud);
        _database.putGroups(ImmutableSet.of(toAdd));
        assertNotNull(toAdd.getId());

        Set<DbGroup> agids = _database.getGroups(asgn.getId());

        // check that there is only 1 group1 in the database
        assertEquals(1, agids.size());

        int agid = Iterables.get(agids, 0).getId();

        // check that the correct group is returned
        assertEquals(toAdd.getAssignmentId(), agid);
    }

    @Test
    public void testRemoveGroups() throws SQLException {
        Assignment asgn1 = DatabaseTestHelpers.createNewAssignmentInDb(_database, "asgn",1);
        Assignment asgn2 = DatabaseTestHelpers.createNewAssignmentInDb(_database, "asgn",2);

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        Student stud = new Student(_database.getStudents().iterator().next());

        DbGroup group1 = new DbGroup(asgn1, stud);
        DbGroup group2 = new DbGroup(asgn2, stud);
        
        //add groups
        _database.putGroups(ImmutableSet.of(group1));
        _database.putGroups(ImmutableSet.of(group2));

        Set<DbGroup> groups = _database.getGroups();
        assertEquals(2, groups.size());

        _database.removeGroups(ImmutableSet.of(group1));

        groups = _database.getGroups();

        //make sure one group was removed
        assertEquals(1, groups.size());

        //check that it is the correct group by checking its fields
        DbGroup actualRecord = Iterables.get(groups, 0);

        assertEquals(group2.getId(), actualRecord.getId());
        assertEquals(group2.getAssignmentId(), actualRecord.getAssignmentId());
        assertEquals(group2.getName(), actualRecord.getName());
        DatabaseTestHelpers.assertSetsEqual(group2.getMemberIds(), actualRecord.getMemberIds());
    }
    
}
