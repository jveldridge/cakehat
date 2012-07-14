package cakehat.database;

import cakehat.database.assignment.PartActionDescription.ActionType;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import cakehat.database.DatabaseTestHelpers.EqualityAsserter;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests for {@link Database} methods related to assignments and their constituent parts.
 * 
 * @author jeldridg
 */
public class DatabaseAssignmentTest {
    
    private Database _database;
    
    public DatabaseAssignmentTest() throws IOException {
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
 
    private final EqualityAsserter<DbAssignment> ASGN_EQC = new EqualityAsserter<DbAssignment>() {
        @Override
        public void assertEqual(DbAssignment t1, DbAssignment t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getName(), t2.getName());
            assertEquals(t1.getOrder(), t2.getOrder());
            assertEquals(t1.hasGroups(), t2.hasGroups());
            DatabaseTestHelpers.assertSetsEqual(GRADABLE_EVENT_EQC, t1.getGradableEvents(), t2.getGradableEvents());
        }
    };
    
    private final EqualityAsserter<DbGradableEvent> GRADABLE_EVENT_EQC = new EqualityAsserter<DbGradableEvent>() {
        @Override
        public void assertEqual(DbGradableEvent t1, DbGradableEvent t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getAssignmentId(), t2.getAssignmentId());
            assertEquals(t1.getName(), t2.getName());
            assertEquals(t1.getDirectory(), t2.getDirectory());
            assertEquals(t1.getDeadlineType(), t2.getDeadlineType());
            assertEquals(t1.getEarlyDate(), t2.getEarlyDate());
            assertEquals(t1.getEarlyPoints(), t2.getEarlyPoints());
            assertEquals(t1.getOnTimeDate(), t2.getOnTimeDate());
            assertEquals(t1.getLateDate(), t2.getLateDate());
            assertEquals(t1.getLatePoints(), t2.getLatePoints());
            assertEquals(t1.getLatePeriod(), t2.getLatePeriod());
            assertEquals(t1.getName(), t2.getName());
            DatabaseTestHelpers.assertSetsEqual(PART_EQC, t1.getParts(), t2.getParts());
        }
    };
    
    private final EqualityAsserter<DbPart> PART_EQC = new EqualityAsserter<DbPart>() {
        @Override
        public void assertEqual(DbPart t1, DbPart t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getGradableEventId(), t2.getGradableEventId());
            assertEquals(t1.getName(), t2.getName());
            assertEquals(t1.getOrder(), t2.getOrder());
            assertEquals(t1.getGmlTemplate(), t2.getGmlTemplate());
            assertEquals(t1.getOutOf(), t2.getOutOf());
            assertEquals(t1.getQuickName(), t2.getQuickName());
            DatabaseTestHelpers.assertSetsEqual(INCLUSION_FILTER_EQC, t1.getInclusionFilters(), t2.getInclusionFilters());
            DatabaseTestHelpers.assertSetsEqual(PART_ACTION_EQC, t1.getActions(), t2.getActions());
        }
    };
    
    private final EqualityAsserter<DbPartAction> PART_ACTION_EQC = new EqualityAsserter<DbPartAction>() {
        @Override
        public void assertEqual(DbPartAction t1, DbPartAction t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getPartId(), t2.getPartId());
            assertEquals(t1.getType(), t2.getType());
            assertEquals(t1.getName(), t2.getName());
            DatabaseTestHelpers.assertSetsEqual(ACTION_PROPERTY_EQC, t1.getActionProperties(), t2.getActionProperties());
        }
    };
    
    private final EqualityAsserter<DbActionProperty> ACTION_PROPERTY_EQC = new EqualityAsserter<DbActionProperty>() {
        @Override
        public void assertEqual(DbActionProperty t1, DbActionProperty t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getPartActionId(), t2.getPartActionId());
            assertEquals(t1.getKey(), t2.getKey());
            assertEquals(t1.getValue(), t2.getValue());
        }
    };
    
    private final EqualityAsserter<DbInclusionFilter> INCLUSION_FILTER_EQC = new EqualityAsserter<DbInclusionFilter>() {
        @Override
        public void assertEqual(DbInclusionFilter t1, DbInclusionFilter t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getPartId(), t2.getPartId());
            assertEquals(t1.getType(), t2.getType());
            assertEquals(t1.getPath(), t2.getPath());
        }
    };
    
    @Test
    public void testGetAssignmentsWithNoAssignments() throws SQLException {
        Set<DbAssignment> assignments = _database.getAssignments();
        assertEquals(0, assignments.size());
    }
    
    @Test
    public void testPutGetSingleAssignmentWithNoGEs() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        
        _database.putAssignments(ImmutableSet.of(asgn));
        assertNotNull(asgn.getId());
        
        Set<DbAssignment> assignments = _database.getAssignments();
        assertEquals(1, assignments.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testUpdateSingleAssignmentWithNoGEs() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        
        _database.putAssignments(ImmutableSet.of(asgn));
        int id = asgn.getId();
        assertNotNull(id);
        
        Set<DbAssignment> assignments = _database.getAssignments();
        assertEquals(1, assignments.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
        
        asgn.setName("newname");
        asgn.setOrder(2);
        
        _database.putAssignments(ImmutableSet.of(asgn));
        assertEquals(id, (int) asgn.getId());
        
        assignments = _database.getAssignments();
        assertEquals(1, assignments.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testSwitchAssignmentOrders() throws SQLException {
        DbAssignment asgn1 = new DbAssignment("asgn1", 1);
        DbAssignment asgn2 = new DbAssignment("asgn2", 2);
        
        _database.putAssignments(ImmutableSet.of(asgn1, asgn2));
        Set<DbAssignment> assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn1, asgn2);
        
        asgn1.setOrder(2);
        asgn2.setOrder(1);
        
        _database.putAssignments(ImmutableSet.of(asgn1, asgn2));
        assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn1, asgn2);
    }
    
    @Test
    public void testAddGradableEventToAsgnInDatabase() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(ImmutableSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        assertEquals(asgn.getId(), ge.getAssignmentId());
        
        _database.putGradableEvents(ImmutableSet.of(ge));
        assertNotNull(ge.getId());
        
        Set<DbAssignment> assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddGradableEventToAsgnNotInDatabase() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        assertEquals(asgn.getId(), ge.getAssignmentId());
        
        _database.putAssignments(ImmutableSet.of(asgn));
        _database.putGradableEvents(ImmutableSet.of(ge));
        assertNotNull(asgn.getId());
        assertEquals(asgn.getId(), ge.getAssignmentId());
        assertNotNull(ge.getId());
        
        Set<DbAssignment> assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddMultipleGradableEvents() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        
        DbGradableEvent ge1 = DbGradableEvent.build(asgn, "ge1", 1);
        DbGradableEvent ge2 = DbGradableEvent.build(asgn, "ge2", 2);
        
        _database.putAssignments(ImmutableSet.of(asgn));
        _database.putGradableEvents(ImmutableSet.of(ge1, ge2));
        assertNotNull(ge1.getId());
        assertNotNull(ge2.getId());
        assertFalse(ge1.getId() == ge2.getId());
        assertEquals(asgn.getId(), ge1.getAssignmentId());
        assertEquals(asgn.getId(), ge2.getAssignmentId());
        
        DbGradableEvent ge3 = DbGradableEvent.build(asgn, "ge3", 3);
        DbGradableEvent ge4 = DbGradableEvent.build(asgn, "ge4", 4);
        _database.putGradableEvents(ImmutableSet.of(ge3, ge4));
        assertNotNull(ge3.getId());
        assertNotNull(ge4.getId());
        assertFalse(ge1.getId() == ge3.getId() || ge1.getId() == ge4.getId());
        assertFalse(ge2.getId() == ge3.getId() || ge2.getId() == ge4.getId());
        assertFalse(ge3.getId() == ge4.getId());
        assertEquals(asgn.getId(), ge3.getAssignmentId());
        assertEquals(asgn.getId(), ge4.getAssignmentId());
        
        Set<DbAssignment> assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddPart() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(ImmutableSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _database.putGradableEvents(ImmutableSet.of(ge));
        
        DbPart part = DbPart.build(ge, "part", 1);
        _database.putParts(ImmutableSet.of(part));
        assertNotNull(part.getId());
        assertEquals(ge.getId(), part.getGradableEventId());
        
        Set<DbAssignment> assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddUpdatedPart() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(ImmutableSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _database.putGradableEvents(ImmutableSet.of(ge));
        
        DbPart part = DbPart.build(ge, "part", 1);
        _database.putParts(ImmutableSet.of(part));
        assertNotNull(part.getId());
        assertEquals(ge.getId(), part.getGradableEventId());
        
        part.setOutOf(15.0);
        part.setQuickName("lab");
        _database.putParts(ImmutableSet.of(part));
        
        Set<DbAssignment> assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddOneAllTheWayDown() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(ImmutableSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _database.putGradableEvents(ImmutableSet.of(ge));
        
        DbPart part = DbPart.build(ge, "part", 1);
        _database.putParts(ImmutableSet.of(part));
        
        DbInclusionFilter filter = DbInclusionFilter.build(part);
        filter.setType(DbInclusionFilter.FilterType.FILE);
        filter.setPath("/path/to/file");
        _database.putInclusionFilters(ImmutableSet.of(filter));
        
        DbPartAction action = DbPartAction.build(part, ActionType.RUN);
        action.setName("java:compile-and-run");
        _database.putPartActions(ImmutableSet.of(action));
        
        DbActionProperty actionProperty = DbActionProperty.build(action, "key");
        actionProperty.setValue("value");
        _database.putPartActionProperties(ImmutableSet.of(actionProperty));
        
        Set<DbAssignment> assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddOneAllTheWayDownOnlyCallingDbAtEnd() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);   
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        DbPart part = DbPart.build(ge, "part", 1);
        
        DbInclusionFilter filter = DbInclusionFilter.build(part);
        filter.setType(DbInclusionFilter.FilterType.FILE);
        filter.setPath("/path/to/file");
        DbPartAction action = DbPartAction.build(part, ActionType.RUN);
        action.setName("java:compile-and-run");
        DbActionProperty actionProperty = DbActionProperty.build(action, "key");
        actionProperty.setValue("value");
        
        _database.putAssignments(ImmutableSet.of(asgn));
        _database.putGradableEvents(ImmutableSet.of(ge));
        _database.putParts(ImmutableSet.of(part));
        _database.putInclusionFilters(ImmutableSet.of(filter));
        _database.putPartActions(ImmutableSet.of(action));
        _database.putPartActionProperties(ImmutableSet.of(actionProperty));
        
        Set<DbAssignment> assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddOneAllTheWayDownRemoveAsgn() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(ImmutableSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _database.putGradableEvents(ImmutableSet.of(ge));
        
        DbPart part = DbPart.build(ge, "part", 1);
        _database.putParts(ImmutableSet.of(part));
        
        DbInclusionFilter filter = DbInclusionFilter.build(part);
        filter.setType(DbInclusionFilter.FilterType.FILE);
        filter.setPath("/path/to/file");
        _database.putInclusionFilters(ImmutableSet.of(filter));
        
        DbPartAction action = DbPartAction.build(part, ActionType.RUN);
        action.setName("java:compile-and-run");
        _database.putPartActions(ImmutableSet.of(action));
        
        DbActionProperty actionProperty = DbActionProperty.build(action, "key");
        actionProperty.setValue("value");
        _database.putPartActionProperties(ImmutableSet.of(actionProperty));
        
        Set<DbAssignment> assignments = _database.getAssignments();
        DatabaseTestHelpers.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
        
        _database.removeAssignments(ImmutableSet.of(asgn));
        assertNull(asgn.getId());
        assertNull(ge.getId());
        assertNull(ge.getAssignmentId());
        assertNull(part.getId());
        assertNull(part.getGradableEventId());
        assertNull(filter.getId());
        assertNull(filter.getPartId());
        assertNull(action.getId());
        assertNull(action.getPartId());
        assertNull(actionProperty.getId());
        assertNull(actionProperty.getPartActionId());
        
        assignments = _database.getAssignments();
        assertEquals(0, assignments.size());
    }
    
}