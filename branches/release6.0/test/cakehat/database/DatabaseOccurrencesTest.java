package cakehat.database;

import cakehat.assignment.Assignment;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Before;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link Database} methods related to gradable event occurrences, extensions, and exemption.
 * 
 * @author jeldridg
 */
public class DatabaseOccurrencesTest {
    
    private Database _database;
    
    public DatabaseOccurrencesTest() throws IOException {
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
    public void testSetGetGradableEventOccurrences() throws SQLException {
        //Setup
        DbAssignment dbAsgn = new DbAssignment("asgn", 1);
        _database.putAssignments(ImmutableSet.of(dbAsgn));

        DbGradableEvent ge = DbGradableEvent.build(dbAsgn, "ge", 1);
        _database.putGradableEvents(ImmutableSet.of(ge));

        final int tid = 1;
        _database.putTAs(ImmutableSet.of(new DbTA(tid, "ta", "The", "Grader", true, false)));

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        int studentId = _database.getStudents().iterator().next().getId();

        DbGroup group = new DbGroup(dbAsgn.getId(), "The Group", ImmutableSet.of(studentId));
        _database.putGroups(ImmutableSet.of(group));
        group.getId();

        final String occurrenceDate = "Around now";
        final String dateRecorded = "Back in the day";

        //Set
        _database.setGradableEventOccurrences(ge.getId(),
                                              ImmutableMap.of(group.getId(), occurrenceDate), tid, dateRecorded);

        //Get
        Map<Integer, GradableEventOccurrenceRecord> records =
                _database.getGradableEventOccurrences(ge.getId(), ImmutableSet.of(group.getId()));

        //Validate
        assertEquals(1, records.size());
        assertEquals(tid, records.get(group.getId()).getTA());
        assertEquals(dateRecorded, records.get(group.getId()).getDateRecorded());
        assertEquals(occurrenceDate, records.get(group.getId()).getOccurrenceDate());
    }

    @Test
    public void testSetSetGetGradableEventOccurrences() throws SQLException {
        //Setup
        DbAssignment dbAsgn = new DbAssignment("asgn", 1);
        _database.putAssignments(ImmutableSet.of(dbAsgn));

        DbGradableEvent ge = DbGradableEvent.build(dbAsgn, "ge", 1);
        _database.putGradableEvents(ImmutableSet.of(ge));

        final int tid = 1;
        _database.putTAs(ImmutableSet.of(new DbTA(tid, "ta", "The", "Grader", true, false)));

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        int studentId = _database.getStudents().iterator().next().getId();

        DbGroup group = new DbGroup(dbAsgn.getId(), "The Group", ImmutableSet.of(studentId));
        _database.putGroups(ImmutableSet.of(group));
        group.getId();

        final String occurrenceDate = "Around now";
        final String dateRecorded = "Back in the day";

        //Set
        _database.setGradableEventOccurrences(ge.getId(),
                                              ImmutableMap.of(group.getId(), occurrenceDate), tid, dateRecorded);

        final String newOccurrenceDate = "In a bit";
        final String newDateRecorded = "Way back when";

        //Set - should cause overwrite
        _database.setGradableEventOccurrences(ge.getId(),
                                              ImmutableMap.of(group.getId(), newOccurrenceDate), tid, newDateRecorded);

        //Get
        Map<Integer, GradableEventOccurrenceRecord> records =
                _database.getGradableEventOccurrences(ge.getId(), ImmutableSet.of(group.getId()));

        //Validate
        assertEquals(1, records.size());
        assertEquals(tid, records.get(group.getId()).getTA());
        assertEquals(newDateRecorded, records.get(group.getId()).getDateRecorded());
        assertEquals(newOccurrenceDate, records.get(group.getId()).getOccurrenceDate());
    }

    @Test
    public void testSetDeleteGetGradableEventOccurrences() throws SQLException {
        //Setup
        DbAssignment dbAsgn = new DbAssignment("asgn", 1);
        _database.putAssignments(ImmutableSet.of(dbAsgn));

        DbGradableEvent ge = DbGradableEvent.build(dbAsgn, "ge", 1);
        _database.putGradableEvents(ImmutableSet.of(ge));

        final int tid = 1;
        _database.putTAs(ImmutableSet.of(new DbTA(tid, "ta", "The", "Grader", true, false)));

        _database.putStudents(ImmutableSet.of(new DbStudent("alinc", "abraham", "lincoln", "alinc@cs.brown.edu")));
        int studentId = _database.getStudents().iterator().next().getId();

        DbGroup group = new DbGroup(dbAsgn.getId(), "The Group", ImmutableSet.of(studentId));
        _database.putGroups(ImmutableSet.of(group));
        group.getId();

        final String occurrenceDate = "Around now";
        final String dateRecorded = "Back in the day";

        //Set
        _database.setGradableEventOccurrences(ge.getId(),
                                              ImmutableMap.of(group.getId(), occurrenceDate), tid, dateRecorded);

        //Delete
        _database.deleteGradableEventOccurrences(ge.getId(), ImmutableSet.of(group.getId()));

        //Get
        Map<Integer, GradableEventOccurrenceRecord> records =
                _database.getGradableEventOccurrences(ge.getId(), ImmutableSet.of(group.getId()));

        //Validate
        assertTrue(records.isEmpty());
    }

    @Test
    public void testSetGetExtensions() throws SQLException {
        //Set up
        DbAssignment dbAsgn = new DbAssignment("The Asgn", 27);
        DbGradableEvent dbEvent = DbGradableEvent.build(dbAsgn, "The Event", 42);
        _database.putAssignments(ImmutableSet.of(dbAsgn));
        _database.putGradableEvents(ImmutableSet.of(dbEvent));

        Assignment asgn = createMock(Assignment.class);
        expect(asgn.getId()).andReturn(dbAsgn.getId()).anyTimes();
        replay(asgn);

        DbGroup dbGroup1 = new DbGroup(asgn, "The Group", ImmutableSet.<Student>of());
        DbGroup dbGroup2 = new DbGroup(asgn, "Another Group", ImmutableSet.<Student>of());
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        Set<Integer> groupIds = ImmutableSet.of(dbGroup1.getId(), dbGroup2.getId());

        DbTA ta = new DbTA(57, "talogin", "FirstName", "LastName", true, true);
        _database.putTAs(ImmutableSet.of(ta));

        //Set, get, and verify extensions
        _database.setExtensions(dbEvent.getId(), "On Time", true, "A note", "Right Now", ta.getId(), groupIds);

        Map<Integer, ExtensionRecord> extensions = _database.getExtensions(dbEvent.getId(), groupIds);

        DatabaseTestHelpers.assertSetsEqual(groupIds, extensions.keySet());
        for (ExtensionRecord record : extensions.values()) {
            assertEquals("Right Now", record.getDateRecorded());
            assertEquals("A note", record.getNote());
            assertEquals("On Time", record.getOnTime());
            assertEquals(true, record.getShiftDates());
            assertEquals((Integer) ta.getId(), (Integer) record.getTAId());
        }
    }

    @Test
    public void testSetSetGetExtensions() throws SQLException {
        //Set up
        DbAssignment dbAsgn = new DbAssignment("The Asgn", 27);
        DbGradableEvent dbEvent = DbGradableEvent.build(dbAsgn, "The Event", 42);
        _database.putAssignments(ImmutableSet.of(dbAsgn));
        _database.putGradableEvents(ImmutableSet.of(dbEvent));

        Assignment asgn = createMock(Assignment.class);
        expect(asgn.getId()).andReturn(dbAsgn.getId()).anyTimes();
        replay(asgn);

        DbGroup dbGroup1 = new DbGroup(asgn, "The Group", ImmutableSet.<Student>of());
        DbGroup dbGroup2 = new DbGroup(asgn, "Another Group", ImmutableSet.<Student>of());
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        Set<Integer> groupIds = ImmutableSet.of(dbGroup1.getId(), dbGroup2.getId());

        DbTA ta = new DbTA(57, "talogin", "FirstName", "LastName", true, true);
        _database.putTAs(ImmutableSet.of(ta));

        //Set, set again with different values, get, and verify extensions
        _database.setExtensions(dbEvent.getId(), "Blah", false, "Lazy students", "Eventually", ta.getId(), groupIds);
        _database.setExtensions(dbEvent.getId(), "On Time", true, "A note", "Right Now", ta.getId(), groupIds);

        Map<Integer, ExtensionRecord> extensions = _database.getExtensions(dbEvent.getId(), groupIds);

        DatabaseTestHelpers.assertSetsEqual(groupIds, extensions.keySet());
        for (ExtensionRecord record : extensions.values()) {
            assertEquals("Right Now", record.getDateRecorded());
            assertEquals("A note", record.getNote());
            assertEquals("On Time", record.getOnTime());
            assertEquals(true, record.getShiftDates());
            assertEquals((Integer) ta.getId(), (Integer) record.getTAId());
        }
    }

    @Test
    public void testSetDeleteGetExtensions() throws SQLException {
        //Set up
        DbAssignment dbAsgn = new DbAssignment("The Asgn", 27);
        DbGradableEvent dbEvent = DbGradableEvent.build(dbAsgn, "The Event", 42);
        _database.putAssignments(ImmutableSet.of(dbAsgn));
        _database.putGradableEvents(ImmutableSet.of(dbEvent));

        Assignment asgn = createMock(Assignment.class);
        expect(asgn.getId()).andReturn(dbAsgn.getId()).anyTimes();
        replay(asgn);

        DbGroup dbGroup1 = new DbGroup(asgn, "The Group", ImmutableSet.<Student>of());
        DbGroup dbGroup2 = new DbGroup(asgn, "Another Group", ImmutableSet.<Student>of());
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        Set<Integer> groupIds = ImmutableSet.of(dbGroup1.getId(), dbGroup2.getId());

        DbTA ta = new DbTA(57, "talogin", "FirstName", "LastName", true, true);
        _database.putTAs(ImmutableSet.of(ta));

        //Set for two, delete for one, get extensions
        _database.setExtensions(dbEvent.getId(), "On Time", true, "A note", "Right Now", ta.getId(), groupIds);

        _database.deleteExtensions(dbEvent.getId(), ImmutableSet.of(dbGroup1.getId()));

        Map<Integer, ExtensionRecord> extensions = _database.getExtensions(dbEvent.getId(), groupIds);

        DatabaseTestHelpers.assertSetsEqual(ImmutableSet.of(dbGroup2.getId()), extensions.keySet());
        for (ExtensionRecord record : extensions.values()) {
            assertEquals("Right Now", record.getDateRecorded());
            assertEquals("A note", record.getNote());
            assertEquals("On Time", record.getOnTime());
            assertEquals(true, record.getShiftDates());
            assertEquals((Integer) ta.getId(), (Integer) record.getTAId());
        }
    }
    
}
