package cakehat.database;

import cakehat.database.assignment.Assignment;
import com.google.common.collect.ImmutableSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Static helper methods for database tests.
 * 
 * @author jeldridg
 */
public class DatabaseTestHelpers {
    
    interface EqualityAsserter<T> {
        void assertEqual(T t1, T t2);
    }
    
    //note: elements are assumed to be unique
    static <T extends DbDataItem> void assertSetContainsGivenElements(EqualityAsserter<T> comparator, Set<T> set, T... elements) {
        Set<T> set2 = new HashSet<T>(Arrays.asList(elements));
        
        assertSetsEqual(comparator, set2, set);
    }
    
    static <T> void assertSetsEqual(Set<T> set1, Set<T> set2) {
        assertEquals(set1.size(), set2.size());
        assertTrue(set1.containsAll(set2));
    }
    
    static <T extends DbDataItem> void assertSetsEqual(EqualityAsserter<T> comparator, Set<T> set1, Set<T> set2) {
        assertEquals(set1.size(), set2.size());
        
        List<T> list1 = new ArrayList<T>(set1);
        Collections.sort(list1, ID_COMPARATOR);
        
        List<T> list2 = new ArrayList<T>(set2);
        Collections.sort(list2, ID_COMPARATOR);
        
        assertListsEqual(comparator, list1, list2);
    }
    
    static Comparator<DbDataItem> ID_COMPARATOR = new Comparator<DbDataItem>() {
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
    
    static <T> void assertListsEqual(EqualityAsserter<T> comparator, List<T> list1, List<T> list2) {
        assertEquals(list1.size(), list2.size());
        
        for (int i = 0; i < list1.size(); i++) {
            comparator.assertEqual(list1.get(i), list2.get(i));
        }
    }
    
    static void assertIntCollectionsEqual(Collection<Integer> col1, Collection<Integer> col2) {
        assertEquals(col1.size(), col2.size());
        
        List<Integer> list1 = new ArrayList<Integer>(col1);
        List<Integer> list2 = new ArrayList<Integer>(col2);
        Collections.sort(list1);
        Collections.sort(list2);
        for (int i = 0; i < list1.size(); i++) {
            assertEquals(list1.get(i), list2.get(i));
        }
    }
    
    static Assignment createNewAssignmentInDb(Database db, String name, int order) throws SQLException {

        DbAssignment dbAsgn = new DbAssignment(name, order);
        db.putAssignments(ImmutableSet.of(dbAsgn));

       
        Assignment asgn = createMock(Assignment.class);
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getId()).andReturn(dbAsgn.getId()).anyTimes();
        expect(asgn.hasGroups()).andReturn(dbAsgn.hasGroups()).anyTimes();

        replay(asgn);

        return asgn;
    }
 
    static String getInvalidIdErrMsg(int id) {
        return String.format("There was no row in the table with ID %d, or some unknown insidious database "
                + "issue occurred. No rows have been inserted or updated.", id);
    }
    
    static String getNonNullConstraintViolationErrMsg(String tableName, String fieldName) {
        return String.format("%s.%s may not be NULL", tableName, fieldName);
    }
    
    static class DatabaseContentWrapper {
        Set<Integer> _partIDs;
        DbPart _part1, _part2;
        int _taId1, _taId2;
        DbGroup _dbGroup1, _dbGroup2;
        
        public DatabaseContentWrapper(Database db) throws SQLException {
            DbAssignment dbAsgn = new DbAssignment("asgn", 1);
            db.putAssignments(ImmutableSet.of(dbAsgn));
        
            Assignment asgn = createMock(Assignment.class);
            expect(asgn.getName()).andReturn("asgn").anyTimes();
            expect(asgn.getId()).andReturn(dbAsgn.getId()).anyTimes();
            expect(asgn.hasGroups()).andReturn(dbAsgn.hasGroups()).anyTimes();
            replay(asgn);
        
            DbGradableEvent ge = DbGradableEvent.build(dbAsgn, "ge", 1);
            db.putGradableEvents(ImmutableSet.of(ge));
        
            _part1 = DbPart.build(ge, "part1", 1);
            _part2 = DbPart.build(ge, "part2", 2);
            db.putParts(ImmutableSet.of(_part1, _part2));
        
            _partIDs = ImmutableSet.of(_part1.getId(), _part2.getId());
            
            _taId1 = 1;
            DbTA ta1 = new DbTA(_taId1, "taLogin1", "taFirst1", "taLast1", true, false);
            _taId2 = 2;
            DbTA ta2 = new DbTA(_taId2, "taLogin2", "taFirst2", "taLast2", true, false);
            db.putTAs(ImmutableSet.of(ta1, ta2));
        
            DbStudent student1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
            DbStudent student2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
            db.putStudents(ImmutableSet.of(student1, student2));
        
            _dbGroup1 = new DbGroup(asgn, new Student(student1));
            _dbGroup2 = new DbGroup(asgn, new Student(student2));
            db.putGroups(ImmutableSet.of(_dbGroup1, _dbGroup2));
        }
    }
    
}
