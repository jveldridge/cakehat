package cakehat.database;

import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.database.DatabaseTestHelpers.EqualityAsserter;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.sql.SQLException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link Database} methods related to grading sheets and their constituent parts.
 * 
 * @author jeldridg
 */
public class DatabaseGradingSheetTest {
    
    private Database _database;
    private DbPart _part;
    
    public DatabaseGradingSheetTest() throws IOException, SQLException {
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
        
        DbAssignment asgn = new DbAssignment("asgn", 1);
        DbGradableEvent ge = DbGradableEvent.build(asgn, "ge", 1);
        _part = DbPart.build(ge, "part", 1);
        _database.putAssignments(ImmutableSet.of(asgn));
        _database.putGradableEvents(ImmutableSet.of(ge));
        _database.putParts(ImmutableSet.of(_part));
    }
 
    private final EqualityAsserter<DbGradingSheet> GRADING_SHEET_EQC = new EqualityAsserter<DbGradingSheet>() {
        @Override
        public void assertEqual(DbGradingSheet t1, DbGradingSheet t2) {
            assertEquals(t1.getId(), t2.getId());
            DatabaseTestHelpers.assertSetsEqual(GRADING_SHEET_SECTION_EQC, t1.getSections(), t2.getSections());
        }
    };
    
    private final EqualityAsserter<DbGradingSheetSection> GRADING_SHEET_SECTION_EQC = new EqualityAsserter<DbGradingSheetSection>() {
        @Override
        public void assertEqual(DbGradingSheetSection t1, DbGradingSheetSection t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getGradingSheet().getId(), t2.getGradingSheet().getId());
            assertEquals(t1.getName(), t2.getName());
            assertEquals(t1.getOrder(), t2.getOrder());
            assertEquals(t1.getOutOf(), t2.getOutOf());
            DatabaseTestHelpers.assertSetsEqual(GRADING_SHEET_SUBSECTION_EQC, t1.getSubsections(), t2.getSubsections());
        }
    };
    
    private final EqualityAsserter<DbGradingSheetSubsection> GRADING_SHEET_SUBSECTION_EQC = new EqualityAsserter<DbGradingSheetSubsection>() {
        @Override
        public void assertEqual(DbGradingSheetSubsection t1, DbGradingSheetSubsection t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getSection().getId(), t2.getSection().getId());
            assertEquals(t1.getText(), t2.getText());
            assertEquals(t1.getOrder(), t2.getOrder());
            assertEquals(t1.getOutOf(), t2.getOutOf());
            DatabaseTestHelpers.assertSetsEqual(GRADING_SHEET_DETAIL_EQC, t1.getDetails(), t2.getDetails());
        }
    };
    
    private final EqualityAsserter<DbGradingSheetDetail> GRADING_SHEET_DETAIL_EQC = new EqualityAsserter<DbGradingSheetDetail>() {
        @Override
        public void assertEqual(DbGradingSheetDetail t1, DbGradingSheetDetail t2) {
            assertEquals(t1.getId(), t2.getId());
            assertEquals(t1.getSubsection().getId(), t2.getSubsection().getId());
            assertEquals(t1.getText(), t2.getText());
            assertEquals(t1.getOrder(), t2.getOrder());
        }
    };
    
    @Test
    public void testGetGradingSheetForNonExistentPart() throws SQLException {
        DbPart part = new DbPart(null, 0, "part", 1, "quickname");
        
        assertNull(_database.getGradingSheet(part));
    }
    
    @Test
    public void testPutGetSingleGradingSheetWithNoSections() throws SQLException {
        DbGradingSheet sheet = DbGradingSheet.build(_part);
        
        _database.putGradingSheets(ImmutableSet.of(sheet));
        assertNotNull(sheet.getId());

        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
    }
    
    @Test
    public void testAddSectionToGradingSheetDatabase() throws SQLException {
        DbGradingSheet sheet = DbGradingSheet.build(_part);
        _database.putGradingSheets(ImmutableSet.of(sheet));
        
        DbGradingSheetSection section = DbGradingSheetSection.build(sheet, "section", 1, 15.0);
        assertEquals(sheet.getId(), section.getGradingSheet().getId());
        
        _database.putGradingSheetSections(ImmutableSet.of(section));
        assertNotNull(section.getId());
        
        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
    }
    
    @Test
    public void testUpdateSectionAssignmentWithNoSubsections() throws SQLException {
        DbGradingSheet sheet = DbGradingSheet.build(_part);
        _database.putGradingSheets(ImmutableSet.of(sheet));
        
        DbGradingSheetSection section = DbGradingSheetSection.build(sheet, "section", 1, null);
        assertEquals(sheet.getId(), section.getGradingSheet().getId());
        
        _database.putGradingSheetSections(ImmutableSet.of(section));
        int id = section.getId();
        
        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
        
        section.setName("newname");
        section.setOrder(2);
        section.setOutOf(15.0);
        
        _database.putGradingSheetSections(ImmutableSet.of(section));
        assertEquals(id, (int) section.getId());
        
        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
    }
    
    @Test
    public void testSwitchSectionOrders() throws SQLException {
        DbGradingSheet sheet = DbGradingSheet.build(_part);
        _database.putGradingSheets(ImmutableSet.of(sheet));
        
        DbGradingSheetSection section1 = DbGradingSheetSection.build(sheet, "section1", 1, null);
        DbGradingSheetSection section2 = DbGradingSheetSection.build(sheet, "section2", 2, null);
        
        _database.putGradingSheetSections(ImmutableSet.of(section1, section2));
        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
        
        section1.setOrder(2);
        section2.setOrder(1);
        
        DbAssignment asgn1 = new DbAssignment("asgn1", 1);
        DbAssignment asgn2 = new DbAssignment("asgn2", 2);
        
        _database.putGradingSheetSections(ImmutableSet.of(section1, section2));
        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
    }
    
    
    @Test
    public void testAddSectionToGradingSheetNotInDatabase() throws SQLException {
        DbGradingSheet sheet = DbGradingSheet.build(_part);

        DbGradingSheetSection section = DbGradingSheetSection.build(sheet, "section", 1, null);
        assertEquals(sheet.getId(), section.getGradingSheet().getId());
        
        _database.putGradingSheets(ImmutableSet.of(sheet));
        _database.putGradingSheetSections(ImmutableSet.of(section));
        assertNotNull(sheet.getId());
        assertEquals(sheet.getId(), section.getGradingSheet().getId());
        
        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
    }

    @Test
    public void testAddSubsection() throws SQLException {
        DbGradingSheet sheet = DbGradingSheet.build(_part);
        _database.putGradingSheets(ImmutableSet.of(sheet));
        
        DbGradingSheetSection section = DbGradingSheetSection.build(sheet, "section", 1, null);        
        _database.putGradingSheetSections(ImmutableSet.of(section));
        
        DbGradingSheetSubsection subsection = DbGradingSheetSubsection.build(section, "section", 1, null);
        _database.putGradingSheetSubsections(ImmutableSet.of(subsection));
        assertNotNull(subsection.getId());
        assertEquals(section.getId(), subsection.getSection().getId());
        
        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
    }
    
    @Test
    public void testAddOneAllTheWayDown() throws SQLException {
        DbGradingSheet sheet = DbGradingSheet.build(_part);
        _database.putGradingSheets(ImmutableSet.of(sheet));
        
        DbGradingSheetSection section = DbGradingSheetSection.build(sheet, "section", 1, null);        
        _database.putGradingSheetSections(ImmutableSet.of(section));
        
        DbGradingSheetSubsection subsection = DbGradingSheetSubsection.build(section, "subsection", 1, null);
        _database.putGradingSheetSubsections(ImmutableSet.of(subsection));
        
        DbGradingSheetDetail detail = DbGradingSheetDetail.build(subsection, "detail", 1);
        _database.putGradingSheetDetails(ImmutableSet.of(detail));
        
        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
    }
    
    @Test
    public void testAddOneAllTheWayDownCallingDbAtEnd() throws SQLException {
        DbGradingSheet sheet = DbGradingSheet.build(_part);
        DbGradingSheetSection section = DbGradingSheetSection.build(sheet, "section", 1, null);
        DbGradingSheetSubsection subsection = DbGradingSheetSubsection.build(section, "subsection", 1, null);
        DbGradingSheetDetail detail = DbGradingSheetDetail.build(subsection, "detail", 1);
        
        _database.putGradingSheets(ImmutableSet.of(sheet));
        _database.putGradingSheetSections(ImmutableSet.of(section));
        _database.putGradingSheetSubsections(ImmutableSet.of(subsection));
        _database.putGradingSheetDetails(ImmutableSet.of(detail));
        
        GRADING_SHEET_EQC.assertEqual(sheet, _database.getGradingSheet(_part));
    }
    
    @Test
    public void testAddOneAllTheWayRemoveGradingSheet() throws SQLException {
        DbGradingSheet sheet = DbGradingSheet.build(_part);
        _database.putGradingSheets(ImmutableSet.of(sheet));
        
        DbGradingSheetSection section = DbGradingSheetSection.build(sheet, "section", 1, null);        
        _database.putGradingSheetSections(ImmutableSet.of(section));
        
        DbGradingSheetSubsection subsection = DbGradingSheetSubsection.build(section, "subsection", 1, null);
        _database.putGradingSheetSubsections(ImmutableSet.of(subsection));
        
        DbGradingSheetDetail detail = DbGradingSheetDetail.build(subsection, "detail", 1);
        _database.putGradingSheetDetails(ImmutableSet.of(detail));
        
        _database.removeGradingSheets(ImmutableSet.of(sheet));
        assertNull(sheet.getId());
        assertNull(section.getId());
        assertNull(subsection.getId());
        assertNull(detail.getId());
        
        assertNull(_database.getGradingSheet(_part));
    }
    
}
