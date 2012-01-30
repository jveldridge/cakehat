package cakehat.database;

import cakehat.database.HandinRecord;
import cakehat.database.Database;
import cakehat.database.DbNotifyAddress;
import cakehat.database.Student;
import cakehat.database.GradeRecord;
import cakehat.database.CakeHatDBIOException;
import cakehat.database.DbPart;
import cakehat.database.DbGroup;
import cakehat.database.DbPropertyValue;
import cakehat.database.DbPartAction;
import cakehat.database.DbActionProperty;
import cakehat.database.DbTA;
import cakehat.database.DbInclusionFilter;
import cakehat.database.DbGradableEvent;
import cakehat.database.DbStudent;
import cakehat.database.DatabaseImpl;
import cakehat.database.DbAssignment;
import cakehat.database.DbDataItem;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import cakehat.assignment.Assignment;
import com.google.common.collect.Iterables;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import cakehat.Allocator.SingletonAllocation;
import cakehat.Allocator;
import cakehat.assignment.PartActionDescription.ActionType;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import support.utils.SingleElementSet;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * Tests the methods of {@link Database}.
 * 
 * @author jeldridg
 */
public class DatabaseTest {
    
    private Database _database;
    
    @Rule
    public ExpectedException _thrown = ExpectedException.none();
    
    public DatabaseTest() throws IOException {
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
    public void testSetGetStringProperty() throws SQLException {
        String account = "cakehat@cs.brown.edu";
        _database.putPropertyValue(DbPropertyValue.DbPropertyKey.EMAIL_ACCOUNT,
                                   new DbPropertyValue<String>(account));
        
        DbPropertyValue<String> property = _database.getPropertyValue(DbPropertyValue.DbPropertyKey.EMAIL_ACCOUNT);
        assertEquals(account, property.getValue());
    }
    
    @Test
    public void testSetGetBooleanProperty() throws SQLException {
        boolean attach = true;
        _database.putPropertyValue(DbPropertyValue.DbPropertyKey.ATTACH_GRADING_SHEET,
                                   new DbPropertyValue<Boolean>(true));
        
        DbPropertyValue<Boolean> property = _database.getPropertyValue(DbPropertyValue.DbPropertyKey.ATTACH_GRADING_SHEET);
        assertEquals(attach, property.getValue());
    }
    
    @Test
    public void testGetNotifyAddressesBeforeAnyPut() throws SQLException {
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(0, addresses.size());
    }
    
    @Test
    public void testPutNotifyAddressWithInvalidId() throws SQLException {
        String addressString = "cakehat@cs.brown.edu";
        int invalidId = -1;
        
        _thrown.expect(SQLException.class);
        _thrown.expectMessage(this.getInvalidIdErrMsg(invalidId));
        
        DbNotifyAddress addressIn = new DbNotifyAddress(invalidId, addressString);
        
        _database.putNotifyAddresses(SingleElementSet.of(addressIn));
    }
    
    @Test
    public void testPutNotifyAddressWithNullAddress() throws SQLException {
        _thrown.expect(SQLException.class);
        _thrown.expectMessage(this.getNonNullConstraintViolationErrMsg("notifyaddresses", "address"));
        
        String nullAddress = null;
        
        DbNotifyAddress addressIn = new DbNotifyAddress(nullAddress);
        _database.putNotifyAddresses(SingleElementSet.of(addressIn));
    }
    
    private final EqualityAsserter<DbNotifyAddress> DB_NOTIFY_ADDRESS_EQ_C = 
            new EqualityAsserter<DbNotifyAddress>() {
                @Override
                public void assertEqual(DbNotifyAddress t1, DbNotifyAddress t2) {
                    assertEquals(t1.getId(), t2.getId());
                    assertEquals(t1.getAddress(), t2.getAddress());
                }
            };
    
    @Test
    public void testPutGetSingleNotifyAddress() throws SQLException {
        String addressString = "cakehat@cs.brown.edu";
        DbNotifyAddress addressIn = new DbNotifyAddress(addressString);
        
        _database.putNotifyAddresses(SingleElementSet.of(addressIn));
        //database method must update the ID of the element it is given
        assertNotNull(addressIn.getId());
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        
        this.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, addressIn);
    }
    
    @Test
    public void testPutGetPutGetSingleNotifyAddress() throws SQLException {
        String initialAddressString = "cakehat@cs.brown.edu";
        String subsequentAddressString = "another@email.com";
        
        DbNotifyAddress address = new DbNotifyAddress(initialAddressString);
        _database.putNotifyAddresses(SingleElementSet.of(address));
        Integer id = address.getId();
        assertNotNull(id);
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        this.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address);
        
        address.setAddress(subsequentAddressString);
        _database.putNotifyAddresses(SingleElementSet.of(address));
        //ID should not have changed
        assertEquals(id, address.getId());
        
        addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        this.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address);
    }

    @Test
    public void testPutGetMultipleNotifyAddressesSimultaneously() throws SQLException {
        String address1String = "cakehat@cs.brown.edu";
        String address2String = "another@email.com";
        
        DbNotifyAddress address1In = new DbNotifyAddress(address1String);
        DbNotifyAddress address2In = new DbNotifyAddress(address2String);
        
        _database.putNotifyAddresses(ImmutableSet.of(address1In, address2In));
        
        assertNotNull(address1In.getId());
        assertNotNull(address2In.getId());
        assertFalse(address1In.getId().equals(address2In.getId()));
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(2, addresses.size());
        this.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1In, address2In);
    }
    
    @Test
    public void testPutGetMultipleNotifyAddressesSequentially() throws SQLException {
        String address1String = "cakehat@cs.brown.edu";
        DbNotifyAddress address1In = new DbNotifyAddress(address1String);
        
        _database.putNotifyAddresses(SingleElementSet.of(address1In));
        assertNotNull(address1In.getId());
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        this.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1In); 
        
        String address2String = "another@email.com";
        DbNotifyAddress address2In = new DbNotifyAddress(address2String);
        
        _database.putNotifyAddresses(SingleElementSet.of(address2In));
        assertNotNull(address2In.getId());
        assertFalse(address1In.getId().equals(address2In.getId()));
        
        addresses = _database.getNotifyAddresses();
        assertEquals(2, addresses.size());
        this.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1In, address2In); 
    }
    
    @Test
    public void testPutGetNotifyAddressesFailureAtomicity() throws SQLException {
        String address1String = "cakehat@cs.brown.edu";
        String address2String = "another@email.com";
        int invalidID = -1;
        
        DbNotifyAddress address1In = new DbNotifyAddress(address1String);
        DbNotifyAddress address2In = new DbNotifyAddress(invalidID, address2String);
        
        try {
            _database.putNotifyAddresses(ImmutableSet.of(address1In, address2In));
            fail();
        } catch (SQLException ex) {
            assertEquals(this.getInvalidIdErrMsg(invalidID), ex.getMessage());
        }
        
        //no notify addresses should be added to the database
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(0, addresses.size());
    }
    
    @Test
    public void testPutSameNotifyAddressTwice() throws SQLException {
        String addressString = "cakehat@cs.brown.edu";
        
        DbNotifyAddress address1In = new DbNotifyAddress(addressString);
        DbNotifyAddress address2In = new DbNotifyAddress(addressString);
        
        _database.putNotifyAddresses(ImmutableSet.of(address1In, address2In));
        assertNotNull(address1In.getId());
        assertNotNull(address2In.getId());
        assertFalse(address1In.getId().equals(address2In.getId()));
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(2, addresses.size());
        this.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1In, address2In);
    }
    
    @Test
    public void testRemoveNonExistentNotifyAddress() throws SQLException {
        int nonExistentId = 1;
        String addressString = "cakehat@cs.brown.edu";
        
        _thrown.expect(SQLException.class);
        _thrown.expectMessage(this.getInvalidIdErrMsg(nonExistentId));
        
        DbNotifyAddress toRemove = new DbNotifyAddress(nonExistentId, addressString);
        _database.removeNotifyAddresses(SingleElementSet.of(toRemove));
    }
    
    @Test
    public void testPutMultipleGetRemoveOneGetNotifyAddress() throws SQLException {
        DbNotifyAddress address1 = new DbNotifyAddress("cakehat@cs.brown.edu");
        DbNotifyAddress address2 = new DbNotifyAddress("another@email.com");
        
        _database.putNotifyAddresses(ImmutableSet.of(address1, address2));
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(2, addresses.size());
        this.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1, address2);
        
        _database.removeNotifyAddresses(SingleElementSet.of(address1));
        //database method must updated the ID of the removed element
        assertNull(address1.getId());
        
        addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        this.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address2);
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
        
        _database.putTAs(SingleElementSet.of(ta));
        //ID should not have changed
        assertEquals(taId, (int) ta.getId());
        
        Set<DbTA> tas = _database.getTAs();
        assertEquals(1, tas.size());
        this.assertSetContainsGivenElements(DB_TA_EQ_C, tas, ta);
    }
    
    @Test
    public void testUpdateTA() throws SQLException {
       int taId = 1;
        DbTA ta = new DbTA(taId, "login", "first", "last", true, false);
        
        _database.putTAs(SingleElementSet.of(ta));
        //ID should not have changed
        assertEquals(taId, (int) ta.getId());
        
        Set<DbTA> tas = _database.getTAs();
        this.assertSetContainsGivenElements(DB_TA_EQ_C, tas, ta); 
        
        ta.setLogin("newlogin");
        ta.setFirstName("New");
        ta.setLastName("Login");
        ta.setIsAdmin(true);
        ta.setIsDefaultGrader(false);
        
        _database.putTAs(SingleElementSet.of(ta));
        tas = _database.getTAs();
        this.assertSetContainsGivenElements(DB_TA_EQ_C, tas, ta); 
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
        
        _database.putStudents(SingleElementSet.of(student));
        assertNotNull(student.getId());
        
        Set<DbStudent> students = _database.getStudents();
        assertEquals(1, students.size());
        this.assertSetContainsGivenElements(STUDENT_EQC, students, student);
    }
    
    @Test
    public void testUpdateSingleStudent() throws SQLException {
        DbStudent student = new DbStudent("login", "first", "last", "email");
        assertTrue(student.isEnabled());
        assertFalse(student.hasCollabPolicy());
        
        _database.putStudents(SingleElementSet.of(student));
        Integer id = student.getId();
        assertNotNull(id);
        
        Set<DbStudent> students = _database.getStudents();
        this.assertSetContainsGivenElements(STUDENT_EQC, students, student);
        
        student.setLogin("different");
        student.setHasCollabPolicy(true);
        
        _database.putStudents(SingleElementSet.of(student));
        assertEquals(id, student.getId());
        
        students = _database.getStudents();
        this.assertSetContainsGivenElements(STUDENT_EQC, students, student);
    }
    
    private final EqualityAsserter<DbAssignment> ASGN_EQC = new EqualityAsserter<DbAssignment>() {
        @Override
        public void assertEqual(DbAssignment t1, DbAssignment t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getName(), t2.getName());
            assertEquals(t1.getOrder(), t2.getOrder());
            assertEquals(t1.hasGroups(), t2.hasGroups());
            assertSetsEqual(GRADABLE_EVENT_EQC, t1.getGradableEvents(), t2.getGradableEvents());
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
            assertSetsEqual(PART_EQC, t1.getParts(), t2.getParts());
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
            assertEquals(t1.getGradingGuide(), t2.getGradingGuide());
            assertSetsEqual(INCLUSION_FILTER_EQC, t1.getInclusionFilters(), t2.getInclusionFilters());
            assertSetsEqual(PART_ACTION_EQC, t1.getActions(), t2.getActions());
        }
    };
    
    private final EqualityAsserter<DbPartAction> PART_ACTION_EQC = new EqualityAsserter<DbPartAction>() {
        @Override
        public void assertEqual(DbPartAction t1, DbPartAction t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getPartId(), t2.getPartId());
            assertEquals(t1.getType(), t2.getType());
            assertEquals(t1.getName(), t2.getName());
            assertSetsEqual(ACTION_PROPERTY_EQC, t1.getActionProperties(), t2.getActionProperties());
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
    
    private final EqualityAsserter<GradeRecord> GRADE_RECORD_EQC = new EqualityAsserter<GradeRecord>() {
        @Override
        public void assertEqual(GradeRecord t1, GradeRecord t2) {
            assertEquals(t1.doesMatchGml(), t2.doesMatchGml());
            assertEquals(t1.getDateRecorded(), t2.getDateRecorded());
            assertEquals(t1.getEarned(), t2.getEarned(), 10E-10);
            assertEquals(t1.getTAId(), t2.getTAId());
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
        
        _database.putAssignments(SingleElementSet.of(asgn));
        assertNotNull(asgn.getId());
        
        Set<DbAssignment> assignments = _database.getAssignments();
        assertEquals(1, assignments.size());
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testUpdateSingleAssignmentWithNoGEs() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        
        _database.putAssignments(SingleElementSet.of(asgn));
        int id = asgn.getId();
        assertNotNull(id);
        
        Set<DbAssignment> assignments = _database.getAssignments();
        assertEquals(1, assignments.size());
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
        
        asgn.setName("newname");
        asgn.setOrder(2);
        
        _database.putAssignments(SingleElementSet.of(asgn));
        assertEquals(id, (int) asgn.getId());
        
        assignments = _database.getAssignments();
        assertEquals(1, assignments.size());
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testSwitchAssignmentOrders() throws SQLException {
        DbAssignment asgn1 = new DbAssignment("asgn1", 1);
        DbAssignment asgn2 = new DbAssignment("asgn2", 2);
        
        _database.putAssignments(ImmutableSet.of(asgn1, asgn2));
        Set<DbAssignment> assignments = _database.getAssignments();
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn1, asgn2);
        
        asgn1.setOrder(2);
        asgn2.setOrder(1);
        
        _database.putAssignments(ImmutableSet.of(asgn1, asgn2));
        assignments = _database.getAssignments();
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn1, asgn2);
    }
    
    @Test
    public void testAddGradableEventToAsgnInDatabase() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(SingleElementSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        assertEquals(asgn.getId(), ge.getAssignmentId());
        
        _database.putGradableEvents(SingleElementSet.of(ge));
        assertNotNull(ge.getId());
        
        Set<DbAssignment> assignments = _database.getAssignments();
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddGradableEventToAsgnNotInDatabase() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        assertEquals(asgn.getId(), ge.getAssignmentId());
        
        _database.putAssignments(SingleElementSet.of(asgn));
        _database.putGradableEvents(SingleElementSet.of(ge));
        assertNotNull(asgn.getId());
        assertEquals(asgn.getId(), ge.getAssignmentId());
        assertNotNull(ge.getId());
        
        Set<DbAssignment> assignments = _database.getAssignments();
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddMultipleGradableEvents() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        
        DbGradableEvent ge1 = DbGradableEvent.build(asgn, "ge1", 1);
        DbGradableEvent ge2 = DbGradableEvent.build(asgn, "ge2", 2);
        
        _database.putAssignments(SingleElementSet.of(asgn));
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
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddPart() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(SingleElementSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _database.putGradableEvents(SingleElementSet.of(ge));
        
        DbPart part = DbPart.build(ge, "part", 1);
        _database.putParts(SingleElementSet.of(part));
        assertNotNull(part.getId());
        assertEquals(ge.getId(), part.getGradableEventId());
        
        Set<DbAssignment> assignments = _database.getAssignments();
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddUpdatedPart() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(SingleElementSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _database.putGradableEvents(SingleElementSet.of(ge));
        
        DbPart part = DbPart.build(ge, "part", 1);
        _database.putParts(SingleElementSet.of(part));
        assertNotNull(part.getId());
        assertEquals(ge.getId(), part.getGradableEventId());
        
        part.setOutOf(15.0);
        part.setQuickName("lab");
        _database.putParts(SingleElementSet.of(part));
        
        Set<DbAssignment> assignments = _database.getAssignments();
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddOneAllTheWayDown() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(SingleElementSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _database.putGradableEvents(SingleElementSet.of(ge));
        
        DbPart part = DbPart.build(ge, "part", 1);
        _database.putParts(SingleElementSet.of(part));
        
        DbInclusionFilter filter = DbInclusionFilter.build(part);
        filter.setType(DbInclusionFilter.FilterType.FILE);
        filter.setPath("/path/to/file");
        _database.putInclusionFilters(SingleElementSet.of(filter));
        
        DbPartAction action = DbPartAction.build(part, ActionType.RUN);
        action.setName("java:compile-and-run");
        _database.putPartActions(SingleElementSet.of(action));
        
        DbActionProperty actionProperty = DbActionProperty.build(action, "key");
        actionProperty.setValue("value");
        _database.putPartActionProperties(SingleElementSet.of(actionProperty));
        
        Set<DbAssignment> assignments = _database.getAssignments();
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
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
        
        _database.putAssignments(SingleElementSet.of(asgn));
        _database.putGradableEvents(SingleElementSet.of(ge));
        _database.putParts(SingleElementSet.of(part));
        _database.putInclusionFilters(SingleElementSet.of(filter));
        _database.putPartActions(SingleElementSet.of(action));
        _database.putPartActionProperties(SingleElementSet.of(actionProperty));
        
        Set<DbAssignment> assignments = _database.getAssignments();
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
    }
    
    @Test
    public void testAddOneAllTheWayDownRemoveAsgn() throws SQLException {
        DbAssignment asgn = new DbAssignment("asgn", 1);
        _database.putAssignments(SingleElementSet.of(asgn));
        
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _database.putGradableEvents(SingleElementSet.of(ge));
        
        DbPart part = DbPart.build(ge, "part", 1);
        _database.putParts(SingleElementSet.of(part));
        
        DbInclusionFilter filter = DbInclusionFilter.build(part);
        filter.setType(DbInclusionFilter.FilterType.FILE);
        filter.setPath("/path/to/file");
        _database.putInclusionFilters(SingleElementSet.of(filter));
        
        DbPartAction action = DbPartAction.build(part, ActionType.RUN);
        action.setName("java:compile-and-run");
        _database.putPartActions(SingleElementSet.of(action));
        
        DbActionProperty actionProperty = DbActionProperty.build(action, "key");
        actionProperty.setValue("value");
        _database.putPartActionProperties(SingleElementSet.of(actionProperty));
        
        Set<DbAssignment> assignments = _database.getAssignments();
        this.assertSetContainsGivenElements(ASGN_EQC, assignments, asgn);
        
        _database.removeAssignments(SingleElementSet.of(asgn));
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
    
     @Test
    public void testBlacklist() throws SQLException {
        int taId = 1;
        DbTA ta = new DbTA(taId, "taLogin", "taFirst", "taLast", true, false);
        _database.putTAs(SingleElementSet.of(ta));
        
        DbStudent student1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent student2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(student1, student2));
        
        Set<Integer> studentIDs = ImmutableSet.of(student1.getId(), student2.getId());
        _database.blacklistStudents(studentIDs, taId);
        Set<Integer> blacklistedStudents = _database.getBlacklistedStudents();
        this.assertIntCollectionsEqual(blacklistedStudents, studentIDs);
        
          
        _database.unBlacklistStudents(SingleElementSet.of(student1.getId()), taId);
        blacklistedStudents = _database.getBlacklistedStudents();
        assertEquals(blacklistedStudents.size(), 1);
        
        Set<Integer> blacklist = _database.getBlacklist(taId);
        assertEquals(blacklist.size(), 1);
        assertEquals(student2.getId(), blacklist.iterator().next());
    }

    @Test
    public void testGetAllGroupsEmpty() throws SQLException {
        Set<DbGroup> groups = _database.getGroups();
        assertEquals(0,groups.size());
    }


    private Assignment createNewAssignmentInDb (String name, int order) throws SQLException, CakeHatDBIOException
    {

        DbAssignment dbAsgn = new DbAssignment(name, order);
        _database.putAssignments(SingleElementSet.of(dbAsgn));

       
        Assignment asgn = createMock(Assignment.class);
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getId()).andReturn(dbAsgn.getId()).anyTimes();
        expect(asgn.hasGroups()).andReturn(dbAsgn.hasGroups()).anyTimes();

        replay(asgn);

        return asgn;
            
    }


    @Test
    public void testGetAllGroupsWithOneGroup() throws SQLException, CakeHatDBIOException {

        Assignment asgn = this.createNewAssignmentInDb("asgn", 1);

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        Student stud = new Student(_database.getStudents().iterator().next());

        DbGroup toAdd = new DbGroup(asgn, stud);
        _database.addGroups(ImmutableSet.of(toAdd));
        assertNotNull(toAdd.getId());

        Set<DbGroup> groups = _database.getGroups();

        // check that there is only 1 group in the database
        assertEquals(1, groups.size());

        DbGroup actualRecord = Iterables.get(groups, 0);

        // check that it is the group that was added by checking each field of group record
        assertEquals(toAdd.getId(), actualRecord.getId());
        assertEquals(toAdd.getAssignmentId(), actualRecord.getAssignmentId());
        assertEquals(toAdd.getName(), actualRecord.getName());
        assertSetsEqual(toAdd.getMemberIds(), actualRecord.getMemberIds());
    }

    @Test
    public void testGetGroupsWithOneGroup() throws SQLException, CakeHatDBIOException {
        Assignment asgn = this.createNewAssignmentInDb("asgn",1);

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        Student stud = new Student(_database.getStudents().iterator().next());

        DbGroup toAdd = new DbGroup(asgn, stud);
        _database.addGroups(ImmutableSet.of(toAdd));
        assertNotNull(toAdd.getId());

        Set<Integer> agids = _database.getGroups(asgn.getId());

        // check that there is only 1 group1 in the database
        assertEquals(1, agids.size());

        int agid = Iterables.get(agids, 0);

        // check that the correct group is returned
        assertEquals(toAdd.getAssignmentId(), agid);
    }

    @Test
    public void testRemoveGroups() throws SQLException, CakeHatDBIOException {
         Assignment asgn1 = this.createNewAssignmentInDb("asgn",1);
         Assignment asgn2 = this.createNewAssignmentInDb("asgn",2);

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        Student stud = new Student(_database.getStudents().iterator().next());

        DbGroup group1 = new DbGroup(asgn1, stud);
        DbGroup group2 = new DbGroup(asgn2, stud);
        
        //add groups
        _database.addGroups(ImmutableSet.of(group1, group2));

        Set<DbGroup> groups = _database.getGroups();
        assertEquals(2, groups.size());

        //remove group for assignment 1
        _database.removeGroups(asgn1.getId());

        groups = _database.getGroups();

        //make sure one group was removed
        assertEquals(1,groups.size());

        //check that it is the correct group by checking its fields
        DbGroup actualRecord = Iterables.get(groups, 0);

        assertEquals(group2.getId(), actualRecord.getId());
        assertEquals(group2.getAssignmentId(), actualRecord.getAssignmentId());
        assertEquals(group2.getName(), actualRecord.getName());
        assertSetsEqual(group2.getMemberIds(), actualRecord.getMemberIds());
    }

    @Test
    public void testDistribution() throws SQLException, CakeHatDBIOException {
        DatabaseContentWrapper wrapper = new DatabaseContentWrapper();
        
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
        
        Map<Integer, Collection<Integer>> part1Dist = 
                _database.getDistribution(wrapper._part1.getId());
        assertEquals(1, part1Dist.size());
        assertEquals(true, part1Dist.containsKey(wrapper._taId1));
        assertEquals(2, part1Dist.get(wrapper._taId1).size());
    }
    
    @Test
    public void testAssignGroup() throws SQLException, CakeHatDBIOException{
        DatabaseContentWrapper wrapper = new DatabaseContentWrapper();
        
        _database.assignGroup(wrapper._dbGroup1.getId(), 
                              wrapper._part1.getId(), wrapper._taId1);
        _database.assignGroup(wrapper._dbGroup2.getId(), 
                              wrapper._part1.getId(), wrapper._taId1);
        Set<Integer> assignedGroupsFB = 
                _database.getAssignedGroups(wrapper._part1.getId(), wrapper._taId1);
        this.assertIntCollectionsEqual(ImmutableSet.of(wrapper._dbGroup1.getId(), 
                                                        wrapper._dbGroup2.getId()), 
                                       assignedGroupsFB);
        assertEquals(true, _database.getAssignedGroups(wrapper._part1.getId(), wrapper._taId2).isEmpty());
        
        _database.assignGroup(wrapper._dbGroup1.getId(), 
                              wrapper._part1.getId(), wrapper._taId2);
        assertEquals((int)wrapper._taId2, (int)_database.getGrader(wrapper._part1.getId(), wrapper._dbGroup1.getId()));
        assignedGroupsFB = _database.getAssignedGroups(wrapper._part1.getId(), wrapper._taId1);
        this.assertIntCollectionsEqual(SingleElementSet.of(wrapper._dbGroup2.getId()), 
                                       assignedGroupsFB);
        assignedGroupsFB = _database.getAssignedGroups(wrapper._part1.getId());
        this.assertIntCollectionsEqual(ImmutableSet.of(wrapper._dbGroup1.getId(), 
                                                        wrapper._dbGroup2.getId()), 
                                       assignedGroupsFB);
        
        _database.unassignGroup(wrapper._dbGroup1.getId(), 
                                wrapper._part1.getId(), wrapper._taId2);
        assertEquals(null, _database.getGrader(wrapper._part1.getId(), wrapper._dbGroup1.getId()));
        assertEquals(true, _database.getAssignedGroups(wrapper._part1.getId(), wrapper._taId2).isEmpty());
        assertEquals(true, _database.getPartsWithAssignedGroups(wrapper._taId2).isEmpty());
        
        Set<Integer> assignPartsDB = _database.getPartsWithAssignedGroups(wrapper._taId1);
        this.assertIntCollectionsEqual(assignPartsDB, SingleElementSet.of(wrapper._part1.getId()));
        assertEquals(true, _database.getAssignedGroups(wrapper._part2.getId()).isEmpty());
        
        _database.assignGroup(wrapper._dbGroup1.getId(), 
                              wrapper._part2.getId(), wrapper._taId1);
        assignPartsDB = _database.getPartsWithAssignedGroups(wrapper._taId1);
        this.assertIntCollectionsEqual(assignPartsDB, ImmutableSet.of(wrapper._part1.getId(), 
                                                                      wrapper._part2.getId()));
    }
    
    @Test
    public void testEarned() throws SQLException, CakeHatDBIOException{
        DatabaseContentWrapper wrapper = new DatabaseContentWrapper();
        
        GradeRecord gradeRecord1 = new GradeRecord("date1", wrapper._taId1, 42.0, true);
        GradeRecord gradeRecord2 = new GradeRecord("date2", wrapper._taId1, 79.5, false);
        
        _database.setEarned(wrapper._dbGroup1.getId(), wrapper._part1.getId(), 
                            gradeRecord1.getTAId(), gradeRecord1.getEarned(), 
                            gradeRecord1.doesMatchGml(), gradeRecord1.getDateRecorded());
        _database.setEarned(wrapper._dbGroup2.getId(), wrapper._part1.getId(), 
                            gradeRecord2.getTAId(), gradeRecord2.getEarned(), 
                            gradeRecord2.doesMatchGml(), gradeRecord2.getDateRecorded());
        
        GRADE_RECORD_EQC.assertEqual(gradeRecord1, _database.getEarned(
                        wrapper._dbGroup1.getId(), wrapper._part1.getId()));
        GRADE_RECORD_EQC.assertEqual(gradeRecord2, _database.getEarned(
                        wrapper._dbGroup2.getId(), wrapper._part1.getId()));
        
        Set<Integer> groupIDs = ImmutableSet.of(wrapper._dbGroup1.getId(),
                                                wrapper._dbGroup2.getId());
        assertEquals(true, _database.getEarned(wrapper._part2.getId(), groupIDs).isEmpty());
        
        Map<Integer, GradeRecord> groupToGrade = 
                _database.getEarned(wrapper._part1.getId(), groupIDs);
        assertEquals(2, groupToGrade.size());
        GRADE_RECORD_EQC.assertEqual(gradeRecord1, groupToGrade.get(wrapper._dbGroup1.getId()));
        GRADE_RECORD_EQC.assertEqual(gradeRecord2, groupToGrade.get(wrapper._dbGroup2.getId()));
    }

    @Test
    public void testGetSetHandinTime() throws SQLException, CakeHatDBIOException{
        DbAssignment dbAsgn = new DbAssignment("asgn", 1);
        _database.putAssignments(SingleElementSet.of(dbAsgn));

        DbGradableEvent ge = DbGradableEvent.build(dbAsgn, "ge", 1);
        assertEquals(dbAsgn.getId(), ge.getAssignmentId());

        _database.putGradableEvents(SingleElementSet.of(ge));

        Assignment asgn = createMock(Assignment.class);
        expect(asgn.getName()).andReturn(dbAsgn.getName()).anyTimes();
        expect(asgn.getId()).andReturn(dbAsgn.getId()).anyTimes();
        expect(asgn.hasGroups()).andReturn(dbAsgn.hasGroups()).anyTimes();

        replay(asgn);

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        Student stud = new Student(_database.getStudents().iterator().next());

        DbGroup toAdd = new DbGroup(asgn, stud);
        _database.addGroups(ImmutableSet.of(toAdd));

        int geid = ge.getId();
        int agid = toAdd.getId();
        String date = "1/28/2012";
        String time = "10:05";
        int tid = 1;

        _database.setHandinTime(geid, agid, time, date, tid);

        HandinRecord record = _database.getHandinTime(geid, agid);

        assertEquals(geid, record.getGradeableEventId());
        assertEquals(agid, record.getAsgnGroupId());
        assertEquals(date, record.getDateRecorded());
        assertEquals(time, record.getTime());
        assertEquals(tid, record.getTaId());
    }

    @Test
    public void testGetSetHandinTimeOverwrite() throws SQLException, CakeHatDBIOException{
        DbAssignment dbAsgn = new DbAssignment("asgn", 1);
        _database.putAssignments(SingleElementSet.of(dbAsgn));

        DbGradableEvent ge = DbGradableEvent.build(dbAsgn, "ge", 1);
        assertEquals(dbAsgn.getId(), ge.getAssignmentId());

        _database.putGradableEvents(SingleElementSet.of(ge));


        Assignment asgn = createMock(Assignment.class);
        expect(asgn.getName()).andReturn(dbAsgn.getName()).anyTimes();
        expect(asgn.getId()).andReturn(dbAsgn.getId()).anyTimes();
        expect(asgn.hasGroups()).andReturn(dbAsgn.hasGroups()).anyTimes();

        replay(asgn);

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        Student stud = new Student(_database.getStudents().iterator().next());


        DbGroup toAdd = new DbGroup(asgn, stud);
        _database.addGroups(ImmutableSet.of(toAdd));

        int geid = ge.getId();
        int agid = toAdd.getId();
        String date1 = "1/28/2012";
        String time1 = "10:05";
        int tid = 1;

        _database.setHandinTime(geid, agid, time1, date1, tid);

        String date2 = "1/29/2012";
        String time2 = "10:06";

        _database.setHandinTime(geid, agid, time2, date2, tid);

        HandinRecord record = _database.getHandinTime(geid, agid);

        assertEquals(geid, record.getGradeableEventId());
        assertEquals(agid, record.getAsgnGroupId());
        assertEquals(date2, record.getDateRecorded());
        assertEquals(time2, record.getTime());
        assertEquals(tid, record.getTaId());
    }

    private interface EqualityAsserter<T> {
        void assertEqual(T t1, T t2);
    }
    
    //note: elements are assumed to be unique
    private <T extends DbDataItem> void assertSetContainsGivenElements(EqualityAsserter<T> comparator, Set<T> set, T... elements) {
        Set<T> set2 = new HashSet<T>(Arrays.asList(elements));
        
        this.assertSetsEqual(comparator, set2, set);
    }
    
    private <T> void assertSetsEqual(Set<T> set1, Set<T> set2) {
        assertEquals(set1.size(), set2.size());
        assertTrue(set1.containsAll(set2));
    }
    
    private <T extends DbDataItem> void assertSetsEqual(EqualityAsserter<T> comparator, Set<T> set1, Set<T> set2) {
        assertEquals(set1.size(), set2.size());
        
        List<T> list1 = new ArrayList<T>(set1);
        Collections.sort(list1, ID_COMPARATOR);
        
        List<T> list2 = new ArrayList<T>(set2);
        Collections.sort(list2, ID_COMPARATOR);
        
        this.assertListsEqual(comparator, list1, list2);
    }
    
    private Comparator<DbDataItem> ID_COMPARATOR = new Comparator<DbDataItem>() {
        @Override
        public int compare(DbDataItem item1, DbDataItem item2) {
            if (item1.getId() == null) {
                return item2.getId() == null ? 0 : -1;
            }
            else if (item2.getId() == null) {
                return 1;
            }
            return item1.getId().compareTo(item2.getId());
        }
    };
    
    private <T> void assertListsEqual(EqualityAsserter<T> comparator, List<T> list1, List<T> list2) {
        assertEquals(list1.size(), list2.size());
        
        for (int i = 0; i < list1.size(); i++) {
            comparator.assertEqual(list1.get(i), list2.get(i));
        }
    }
    
    private void assertIntCollectionsEqual(Collection<Integer> col1, Collection<Integer> col2) {
        assertEquals(col1.size(), col2.size());
        
        List<Integer> list1 = new ArrayList<Integer>(col1);
        List<Integer> list2 = new ArrayList<Integer>(col2);
        Collections.sort(list1);
        Collections.sort(list2);
        for (int i = 0; i < list1.size(); i++) {
            assertEquals(list1.get(i), list2.get(i));
        }
    }
    
    private String getInvalidIdErrMsg(int id) {
        return String.format("There was no row in the table with ID %d, or some unknown insidious database "
                + "issue occurred. No rows have been inserted or updated.", id);
    }
    
    private String getNonNullConstraintViolationErrMsg(String tableName, String fieldName) {
        return String.format("%s.%s may not be NULL", tableName, fieldName);
    }
    
    private class DatabaseContentWrapper {
        private Set<Integer> _partIDs;
        private DbPart _part1, _part2;
        private int _taId1, _taId2;
        private DbGroup _dbGroup1, _dbGroup2;
        
        public DatabaseContentWrapper() throws SQLException, CakeHatDBIOException {
            DbAssignment dbAsgn = new DbAssignment("asgn", 1);
            _database.putAssignments(SingleElementSet.of(dbAsgn));
        
            Assignment asgn = createMock(Assignment.class);
            expect(asgn.getName()).andReturn("asgn").anyTimes();
            expect(asgn.getId()).andReturn(dbAsgn.getId()).anyTimes();
            expect(asgn.hasGroups()).andReturn(dbAsgn.hasGroups()).anyTimes();
            replay(asgn);
        
            DbGradableEvent ge = DbGradableEvent.build(dbAsgn, "ge", 1);
            _database.putGradableEvents(SingleElementSet.of(ge));
        
            _part1 = DbPart.build(ge, "part1", 1);
            _part2 = DbPart.build(ge, "part2", 2);
            _database.putParts(ImmutableSet.of(_part1, _part2));
        
            _partIDs = ImmutableSet.of(_part1.getId(), _part2.getId());
            
            _taId1 = 1;
            DbTA ta1 = new DbTA(_taId1, "taLogin1", "taFirst1", "taLast1", true, false);
            _taId2 = 2;
            DbTA ta2 = new DbTA(_taId2, "taLogin2", "taFirst2", "taLast2", true, false);
            _database.putTAs(ImmutableSet.of(ta1, ta2));
        
            DbStudent student1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
            DbStudent student2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
            _database.putStudents(ImmutableSet.of(student1, student2));
        
            _dbGroup1 = new DbGroup(asgn, new Student(student1));
            _dbGroup2 = new DbGroup(asgn, new Student(student2));
            _database.addGroups(ImmutableSet.of(_dbGroup1, _dbGroup2));
        }
    }
}
