package cakehat.database;

import cakehat.database.DatabaseTestHelpers.EqualityAsserter;
import cakehat.database.DatabaseTestHelpers.DatabaseContentWrapper;
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
 * Tests for {@link Database} methods related to students.
 * 
 * @author jeldridg
 */
public class DatabaseGradesTest {
    
    private Database _database;
    
    public DatabaseGradesTest() throws IOException {
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
    
    private final EqualityAsserter<GradeRecord> GRADE_RECORD_EQC = new EqualityAsserter<GradeRecord>() {
        @Override
        public void assertEqual(GradeRecord t1, GradeRecord t2) {
            assertEquals(t1.isSubmitted(), t2.isSubmitted());
            assertEquals(t1.getDateRecorded(), t2.getDateRecorded());
            assertEquals(t1.getEarned(), t2.getEarned());
            assertEquals(t1.getTAId(), t2.getTAId());
        }
    };
    
    
    @Test
    public void testEarned() throws SQLException, CakeHatDBIOException{
        DatabaseContentWrapper wrapper = new DatabaseContentWrapper(_database);
        
        GradeRecord gradeRecord1 = new GradeRecord("date1", wrapper._taId1, 42.0, true);
        GradeRecord gradeRecord2 = new GradeRecord("date2", wrapper._taId1, 79.5, false);
        
        _database.setEarned(wrapper._dbGroup1.getId(), wrapper._part1.getId(), 
                            gradeRecord1.getTAId(), gradeRecord1.getEarned(), 
                            gradeRecord1.isSubmitted(), gradeRecord1.getDateRecorded());
        _database.setEarned(wrapper._dbGroup2.getId(), wrapper._part1.getId(), 
                            gradeRecord2.getTAId(), gradeRecord2.getEarned(), 
                            gradeRecord2.isSubmitted(), gradeRecord2.getDateRecorded());
        
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
    
}
