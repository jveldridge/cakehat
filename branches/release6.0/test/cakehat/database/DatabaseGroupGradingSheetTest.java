package cakehat.database;

import java.util.HashSet;
import java.util.Set;
import cakehat.database.DbGroupGradingSheet.GroupSectionCommentsRecord;
import cakehat.database.DatabaseTestHelpers.EqualityAsserter;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.database.DbGroupGradingSheet.GroupSubsectionEarnedRecord;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link Database} methods related to group grading sheets.
 * 
 * @author jeldridg
 */
public class DatabaseGroupGradingSheetTest {
    
    private Database _database;
    private DbPart _part;
    private DbGroup _group;
    private DbTA _ta;
    
    private Set<Integer> _subsectionIds;
    private Set<Integer> _sectionIds;
    
    public DatabaseGroupGradingSheetTest() throws IOException, SQLException {
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
        
        DbGradingSheetSection section1 = DbGradingSheetSection.build(_part, "section1", 1, null);
        DbGradingSheetSection section2 = DbGradingSheetSection.build(_part, "section2", 2, null);
        DbGradingSheetSubsection subsection1a = DbGradingSheetSubsection.build(section1, "subsection1a", 1, 10.0);
        DbGradingSheetSubsection subsection1b = DbGradingSheetSubsection.build(section1, "subsection1b", 2, 5.0);
        DbGradingSheetSubsection subsection2a = DbGradingSheetSubsection.build(section2, "subsection2a", 1, 5.0);
        DbGradingSheetSubsection subsection2b = DbGradingSheetSubsection.build(section2, "subsection2b", 2, 10.0);
        _database.putGradingSheetSections(ImmutableSet.of(section1, section2));
        _database.putGradingSheetSubsections(ImmutableSet.of(subsection1a, subsection1b, subsection2a, subsection2b));
        
        _sectionIds = new HashSet<Integer>();
        _subsectionIds = new HashSet<Integer>();
        for (DbGradingSheetSection section : _part.getGradingSheetSections()) {
            _sectionIds.add(section.getId());

            for (DbGradingSheetSubsection subsection : section.getSubsections()) {
                _subsectionIds.add(subsection.getId());
            }
        }
        
        DbStudent student = new DbStudent("login", "first", "last", "email");
        _database.putStudents(ImmutableSet.of(student));
        _group = new DbGroup(asgn.getId(), "group", ImmutableSet.of(student.getId()));
        _database.putGroups(ImmutableSet.of(_group));
    }
    
    private final EqualityAsserter<DbGroupGradingSheet> GROUP_GRADING_SHEET_EQC = new EqualityAsserter<DbGroupGradingSheet>() {
        @Override
        public void assertEqual(DbGroupGradingSheet t1, DbGroupGradingSheet t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getGroupId(), t2.getGroupId());
            assertEquals(t1.getPartId(), t2.getPartId());
            assertEquals(t1.getAssignedToId(), t2.getAssignedToId());
            assertEquals(t1.getSubmittedById(), t2.getSubmittedById());
            assertEquals(t1.getSubmittedDate(), t2.getSubmittedDate());
            
            assertEquals(t1.getSubsectionEarnedPoints().size(), t2.getSubsectionEarnedPoints().size());
            assertTrue(t1.getSubsectionEarnedPoints().keySet().containsAll(t2.getSubsectionEarnedPoints().keySet()));
            for (int id : t1.getSubsectionEarnedPoints().keySet()) {
                EARNED_EQC.assertEqual(t1.getSubsectionEarnedPoints().get(id), t2.getSubsectionEarnedPoints().get(id));
            }
            
            assertEquals(t1.getSectionComments().size(), t2.getSectionComments().size());
            assertTrue(t1.getSectionComments().keySet().containsAll(t2.getSectionComments().keySet()));
            for (int id : t1.getSectionComments().keySet()) {
                COMMENTS_EQC.assertEqual(t1.getSectionComments().get(id), t2.getSectionComments().get(id));
            }
        }
    };
    
    private final EqualityAsserter<GroupSubsectionEarnedRecord> EARNED_EQC = new EqualityAsserter<GroupSubsectionEarnedRecord>() {
        @Override
        public void assertEqual(GroupSubsectionEarnedRecord t1, GroupSubsectionEarnedRecord t2) {
            assertEquals(t1.getEarnedPoints(), t2.getEarnedPoints());
            assertEquals(t1.getLastModifiedBy(), t2.getLastModifiedBy());
            assertEquals(t1.getLastModifiedTime(), t2.getLastModifiedTime());
        }
    };
    
    private final EqualityAsserter<GroupSectionCommentsRecord> COMMENTS_EQC = new EqualityAsserter<GroupSectionCommentsRecord>() {
        @Override
        public void assertEqual(GroupSectionCommentsRecord t1, GroupSectionCommentsRecord t2) {
            assertEquals(t1.getComments(), t2.getComments());
            assertEquals(t1.getLastModifiedBy(), t2.getLastModifiedBy());
            assertEquals(t1.getLastModifiedTime(), t2.getLastModifiedTime());
        }
    };
    
    @Test
    public void testGetGroupGradingSheetsWhenNoneInDb() throws SQLException {        
        Map<Integer, Map<Integer, DbGroupGradingSheet>> groupGradingSheets =
                _database.getGroupGradingSheets(ImmutableSet.of(_part.getId()), _subsectionIds, _sectionIds,
                                                ImmutableSet.of(_group.getId()));
        assertTrue(groupGradingSheets.isEmpty());
    }
    
    @Test
    public void testPutGetEmptyGroupGradingSheet() throws SQLException {
        DbGroupGradingSheet ggs = new DbGroupGradingSheet(_group.getId(), _part.getId());
        _database.putGroupGradingSheets(ImmutableSet.of(ggs));
        assertNotNull(ggs.getId());
        
        Map<Integer, Map<Integer, DbGroupGradingSheet>> groupGradingSheetsFromDb =
                _database.getGroupGradingSheets(ImmutableSet.of(_part.getId()), _subsectionIds, _sectionIds,
                                                ImmutableSet.of(_group.getId()));
        assertTrue(groupGradingSheetsFromDb.containsKey(_part.getId()));
        assertTrue(groupGradingSheetsFromDb.get(_part.getId()).containsKey(_group.getId()));
        
        DbGroupGradingSheet ggsFromDb = groupGradingSheetsFromDb.get(_part.getId()).get(_group.getId());
        GROUP_GRADING_SHEET_EQC.assertEqual(ggs, ggsFromDb);
    }

    @Test
    public void testPutGetGroupGradingSheetWithEarnedPoints() throws SQLException {
        DbGroupGradingSheet ggs = new DbGroupGradingSheet(_group.getId(), _part.getId());
        
        int subsectionId = _part.getGradingSheetSections().iterator().next().getSubsections().iterator().next().getId();
        String modifiedTime = DateTime.now().toString();
        ggs.setEarnedPoints(subsectionId, 13.0, _ta.getId(), modifiedTime);
        
        _database.putGroupGradingSheets(ImmutableSet.of(ggs));
        
        Map<Integer, Map<Integer, DbGroupGradingSheet>> groupGradingSheetsFromDb =
                _database.getGroupGradingSheets(ImmutableSet.of(_part.getId()), _subsectionIds, _sectionIds,
                                                ImmutableSet.of(_group.getId()));
        assertTrue(groupGradingSheetsFromDb.containsKey(_part.getId()));
        assertTrue(groupGradingSheetsFromDb.get(_part.getId()).containsKey(_group.getId()));
        
        DbGroupGradingSheet ggsFromDb = groupGradingSheetsFromDb.get(_part.getId()).get(_group.getId());
        GROUP_GRADING_SHEET_EQC.assertEqual(ggs, ggsFromDb);
    }
    
    @Test
    public void testPutGroupGradingSheetWithComments() throws SQLException {
        DbGroupGradingSheet ggs = new DbGroupGradingSheet(_group.getId(), _part.getId());
        
        int sectionId = _part.getGradingSheetSections().iterator().next().getId();
        ggs.setComments(sectionId, "comments", _ta.getId(), DateTime.now().toString());
        
        _database.putGroupGradingSheets(ImmutableSet.of(ggs));
        
        Map<Integer, Map<Integer, DbGroupGradingSheet>> groupGradingSheetsFromDb =
                _database.getGroupGradingSheets(ImmutableSet.of(_part.getId()), _subsectionIds, _sectionIds,
                                                ImmutableSet.of(_group.getId()));
        assertTrue(groupGradingSheetsFromDb.containsKey(_part.getId()));
        assertTrue(groupGradingSheetsFromDb.get(_part.getId()).containsKey(_group.getId()));
        
        DbGroupGradingSheet ggsFromDb = groupGradingSheetsFromDb.get(_part.getId()).get(_group.getId());
        GROUP_GRADING_SHEET_EQC.assertEqual(ggs, ggsFromDb);
    }
    
    @Test
    public void testSubmitGroupGradingSheet() throws SQLException {
        DbGroupGradingSheet ggs = new DbGroupGradingSheet(_group.getId(), _part.getId());
        
        String submittedTime = DateTime.now().toString();
        _database.putGroupGradingSheets(ImmutableSet.of(ggs));
        _database.submitGroupGradingSheets(ImmutableSet.of(ggs), _ta.getId(), submittedTime);
        
        assertEquals(_ta.getId(), ggs.getSubmittedById());
        assertEquals(submittedTime, ggs.getSubmittedDate());
        
        Map<Integer, Map<Integer, DbGroupGradingSheet>> groupGradingSheetsFromDb =
                _database.getGroupGradingSheets(ImmutableSet.of(_part.getId()), _subsectionIds, _sectionIds,
                                                ImmutableSet.of(_group.getId()));
        assertTrue(groupGradingSheetsFromDb.containsKey(_part.getId()));
        assertTrue(groupGradingSheetsFromDb.get(_part.getId()).containsKey(_group.getId()));
        
        DbGroupGradingSheet ggsFromDb = groupGradingSheetsFromDb.get(_part.getId()).get(_group.getId());
        GROUP_GRADING_SHEET_EQC.assertEqual(ggs, ggsFromDb);
    }
    
}
