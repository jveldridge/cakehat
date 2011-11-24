package cakehat.database;

import cakehat.config.Part;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import cakehat.config.handin.DistributablePart;
import java.util.HashMap;
import java.util.Map;
import cakehat.config.Assignment;
import cakehat.config.TA;
import cakehat.database.DataServices.ValidityCheck;
import cakehat.Allocator.SingletonAllocation;
import cakehat.Allocator;
import cakehat.config.ConfigurationInfo;
import cakehat.rubric.TimeStatus;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Before;
import java.util.Collection;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * Tests the methods of {@link DataServices}.
 * 
 * @author jeldridg
 */
public class DataServicesTest {
    
    private Assignment _groupAsgn = ConfigurationData.generateRandomGroupAssignment();
    private Assignment _nonGroupAsgn = ConfigurationData.generateNonGroupAssignment();
    private Assignment _twoDpAsgn = ConfigurationData.generateGroupAssignmentWithNameWithTwoDPs("asgn");
    private TA _ta = ConfigurationData.generateRandomTA();
    private TA _ta2 = ConfigurationData.generateRandomTA();
    private DataServices _ds;
    
    private static final double MAX_DOUBLE_DIFF = .0001;
    
    public DataServicesTest() throws IOException {
        final Database db = new DatabaseImpl(Allocator.getFileSystemUtilities().createTempFile("tempDB", "db"));
        SingletonAllocation<Database> dbioAlloc =
            new SingletonAllocation<Database>()
            {
                public Database allocate() { return db; };
            };
        
        final ConfigurationInfo ci = createMock(ConfigurationInfo.class);
        expect(ci.getAssignments()).andReturn(ImmutableList.of(_groupAsgn, _nonGroupAsgn, _twoDpAsgn)).anyTimes();
        expect(ci.getAssignment(_groupAsgn.getDBID())).andReturn(_groupAsgn).anyTimes();
        expect(ci.getAssignment(_nonGroupAsgn.getDBID())).andReturn(_nonGroupAsgn).anyTimes();
        expect(ci.getAssignment(_twoDpAsgn.getDBID())).andReturn(_twoDpAsgn).anyTimes();
        expect(ci.getTA(_ta.getLogin())).andReturn(_ta).anyTimes();
        expect(ci.getTA(_ta2.getLogin())).andReturn(_ta2).anyTimes();
        expect(ci.getTA(null)).andReturn(null).anyTimes();
        expect(ci.getTAs()).andReturn(ImmutableList.of(_ta, _ta2)).anyTimes();
        replay(ci);
        
        SingletonAllocation<ConfigurationInfo> ciAlloc =
            new SingletonAllocation<ConfigurationInfo>() {
                public ConfigurationInfo allocate() { return ci; };
            };
        
        new Allocator.Customizer().setDatabase(dbioAlloc).setConfigurationInfo(ciAlloc).customize();
        
        _ds = new DataServicesImpl();
    }
    
    @Before
    public void setUp() throws ServicesException {
        _ds.resetDatabase();
    }
    
    @Test
    public void testStudentListStartsEmpty() {
        assertEquals(0, _ds.getAllStudents().size());
        assertEquals(0, _ds.getEnabledStudents().size());
    }
    
    @Test
    public void testAddedStudentIsEnabled() throws ServicesException {
        Student added = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        assertTrue(added.isEnabled());
        assertTrue(_ds.getEnabledStudents().contains(added));
    }
    
    @Test
    public void testGetSingleAddedStudent() throws ServicesException {
        Student added = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        //the student list should now contain the added student
        Collection<Student> students = _ds.getAllStudents();
        assertEquals(1, students.size());
        assertEquals(added, Iterables.get(students, 0));
    }
    
    @Test
    public void testAddingStudentAgainHasNoEffect() throws ServicesException {
        Student added = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        //the student list and enabled student list should now contain the added student
        Collection<Student> students = _ds.getAllStudents();
        assertEquals(1, students.size());
        assertEquals(added, Iterables.get(students, 0));
        
        Collection<Student> enabled = _ds.getEnabledStudents();
        assertEquals(1, enabled.size());
        assertEquals(added, Iterables.get(enabled, 0));
        
        //after adding the student again, the retrieved student and enabled lists should be no different
        _ds.addStudent(added.getLogin(), added.getFirstName(), added.getLastName(), ValidityCheck.BYPASS);
        students = _ds.getAllStudents();
        assertEquals(1, students.size());
        assertEquals(added, Iterables.get(students, 0));
        
        enabled = _ds.getEnabledStudents();
        assertEquals(1, enabled.size());
        assertEquals(added, Iterables.get(enabled, 0));
    }
    
    @Test
    public void testDisablingAndEnablingStudent() throws ServicesException {
        Student added = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        _ds.setStudentEnabled(added, false);
        assertFalse(added.isEnabled());
        assertFalse(_ds.getEnabledStudents().contains(added));
        
        _ds.setStudentEnabled(added, true);
        assertTrue(added.isEnabled());
        assertTrue(_ds.getEnabledStudents().contains(added));
    }
    
    @Test
    public void testInitiallyNoStudentsBlacklisted() throws ServicesException {
        assertEquals(0, _ds.getBlacklistedStudents().size());
    }
    
    @Test
    public void testTABlacklistStartsEmtpy() throws ServicesException {
        assertEquals(0, _ds.getBlacklist(_ta).size());
    }
    
    @Test
    public void testBlacklistStudent() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        _ds.blacklistStudents(ImmutableList.of(student), _ta);
        Collection<Student> taBlacklist = _ds.getBlacklist(_ta);
        Collection<Student> blacklisted = _ds.getBlacklistedStudents();
        assertEquals(1, taBlacklist.size());
        assertEquals(student, Iterables.get(taBlacklist, 0));
        assertEquals(1, blacklisted.size());
        assertEquals(student, Iterables.get(blacklisted, 0));
    }
    
    @Test
    public void testBlacklistStudentTwice() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        _ds.blacklistStudents(ImmutableList.of(student), _ta);
        Collection<Student> taBlacklist = _ds.getBlacklist(_ta);
        Collection<Student> blacklisted = _ds.getBlacklistedStudents();
        assertEquals(1, taBlacklist.size());
        assertEquals(student, Iterables.get(taBlacklist, 0));
        assertEquals(1, blacklisted.size());
        assertEquals(student, Iterables.get(blacklisted, 0));
        
        //re-blacklisting the same student should have no effect
        _ds.blacklistStudents(ImmutableList.of(student), _ta);
        taBlacklist = _ds.getBlacklist(_ta);
        assertEquals(1, taBlacklist.size());
        assertEquals(student, Iterables.get(taBlacklist, 0));
        assertEquals(1, blacklisted.size());
        assertEquals(student, Iterables.get(blacklisted, 0));
    }
    
    @Test
    public void testUnblacklistNonBlacklistedStudent() throws ServicesException {
        //unblacklisting a student who is not blacklisted should have no effect
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        _ds.unBlacklistStudents(ImmutableList.of(student), _ta);
        assertEquals(0, _ds.getBlacklist(_ta).size());
        assertEquals(0, _ds.getBlacklistedStudents().size());
    }
    
    @Test
    public void testUnblacklistBlacklistedStudent() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        _ds.blacklistStudents(ImmutableList.of(student), _ta);
        Collection<Student> taBlacklist = _ds.getBlacklist(_ta);
        Collection<Student> blacklistedStudents = _ds.getBlacklistedStudents();
        assertEquals(1, taBlacklist.size());
        assertEquals(1, blacklistedStudents.size());
        assertEquals(student, Iterables.get(taBlacklist, 0));
        assertEquals(student, Iterables.get(blacklistedStudents, 0));
    }
    
    @Test
    public void testGetBlacklistedForMultipleTAs() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Student student3 = _ds.addStudent("login3", "first3", "last3", ValidityCheck.BYPASS);
        
        _ds.blacklistStudents(ImmutableList.of(student1, student2), _ta);
        _ds.blacklistStudents(ImmutableList.of(student2, student3), _ta2);
        
        Collection<Student> blacklistedStudents = _ds.getBlacklistedStudents();
        assertEquals(3, blacklistedStudents.size());
        assertTrue(blacklistedStudents.contains(student1));
        assertTrue(blacklistedStudents.contains(student2));
        assertTrue(blacklistedStudents.contains(student3));
    }
    
    @Test
    public void testAddSingleStudentGroupSuccess() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        NewGroup ng = new NewGroup(_groupAsgn, student);
        Group group = _ds.addGroup(ng);
        assertEquals(_groupAsgn, group.getAssignment());
        assertEquals(student.getLogin(), group.getName());
        assertEquals(1, group.getMembers().size());
        assertEquals(student, Iterables.get(group.getMembers(), 0));
        assertEquals(1, group.getMemberLogins().size());
        assertEquals(student.getLogin(), Iterables.get(group.getMemberLogins(), 0));
    }
    
    @Test
    public void testAddMultStudentGroupSuccess() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        
        NewGroup ng = new NewGroup(_groupAsgn, "the_group", student1, student2);
        Group group = _ds.addGroup(ng);
        assertEquals(_groupAsgn, group.getAssignment());
        assertEquals("the_group", group.getName());
        assertEquals(2, group.getMembers().size());
        assertEquals(2, group.getMemberLogins().size());
        assertTrue(group.getMembers().contains(student1));
        assertTrue(group.getMembers().contains(student2));
        assertTrue(group.getMemberLogins().contains(student1.getLogin()));
        assertTrue(group.getMemberLogins().contains(student2.getLogin()));
    }
    
    @Test(expected=ServicesException.class)
    public void testAddDuplicateGroupNameFails() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        
        NewGroup ng1 = new NewGroup(_groupAsgn, "the_group", student1, student2);
        try {
            _ds.addGroup(ng1);
        } catch (ServicesException e) {
            fail();
        }
        
        //adding ng2 should fail because it has the same name as ng1
        NewGroup ng2 = new NewGroup(_groupAsgn, "the_group", student1);
        _ds.addGroup(ng2);
    }
    
    @Test(expected=ServicesException.class)
    public void testGroupAddWithMemberInOtherGroupFails() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        
        NewGroup ng1 = new NewGroup(_groupAsgn, "group1", student1, student2);
        try {
            _ds.addGroup(ng1);
        } catch (ServicesException e) {
            fail();
        }
        
        //adding ng2 should fail because student1 is already in a group
        NewGroup ng2 = new NewGroup(_groupAsgn, student1);
        _ds.addGroup(ng2);
    }
    
    @Test
    public void testAddGroupsSuccess() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        
        NewGroup ng1 = new NewGroup(_groupAsgn, student1);
        NewGroup ng2 = new NewGroup(_groupAsgn, student2);
        
        Collection<Group> groups = _ds.addGroups(ImmutableList.of(ng1, ng2));
        //NOTE: the DataServices method documentation makes no guarantees as to
        //      the order of Group objects in the returned Collection
        Group g1 = Iterables.get(groups, 0);
        Group g2 = Iterables.get(groups, 1);
        assertTrue(g1.getName().equals(ng1.getName()) || g1.getName().equals(ng2.getName()));
        if (g1.getName().equals(ng1.getName())) {
            assertTrue(g2.getName().equals(ng2.getName()));
        }
        else {
            assertTrue(g2.getName().equals(ng1.getName()));
            Group tmp = g1;
            g1 = g2;
            g2 = tmp;
        }
        
        assertEquals(_groupAsgn, g1.getAssignment());
        assertEquals(student1.getLogin(), g1.getName());
        assertEquals(1, g1.getMembers().size());
        assertEquals(student1, Iterables.get(g1.getMembers(), 0));
        assertEquals(1, g1.getMemberLogins().size());
        assertEquals(student1.getLogin(), Iterables.get(g1.getMemberLogins(), 0));
        
        assertEquals(_groupAsgn, g2.getAssignment());
        assertEquals(student2.getLogin(), g2.getName());
        assertEquals(1, g2.getMembers().size());
        assertEquals(student2, Iterables.get(g2.getMembers(), 0));
        assertEquals(1, g2.getMemberLogins().size());
        assertEquals(student2.getLogin(), Iterables.get(g2.getMemberLogins(), 0));
    }
    
    @Test
    public void addGroupsWithSameNameFails() throws ServicesException, SQLException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        
        NewGroup ng1 = new NewGroup(_groupAsgn, "the_group", student1);
        NewGroup ng2 = new NewGroup(_groupAsgn, "the_group", student2);
        
        //adding the groups should fail because they have the same name
        try {
            _ds.addGroups(ImmutableList.of(ng1, ng2));
            fail();
        } catch (ServicesException e) {}
                
        //no groups should have been added
        assertEquals(0, _ds.getGroups(_groupAsgn).size());
    }
    
    @Test
    public void testAddGroupsWhereOneHasSameNameAsExistingGroupFails() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Student student3 = _ds.addStudent("login3", "first3", "last3", ValidityCheck.BYPASS);
        
        NewGroup ng1 = new NewGroup(_groupAsgn, "the_group", student1);
        Group g1 = _ds.addGroup(ng1);
        
        NewGroup ng2 = new NewGroup(_groupAsgn, "another_group", student2);
        NewGroup ng3 = new NewGroup(_groupAsgn, "the_group", student3);
        
        //adding ng2 and ng3 should fail because ng3 and ng1 have the same name
        try {
            _ds.addGroups(ImmutableList.of(ng2, ng3));
            fail();
        } catch (ServicesException e) {}
        
        //only one group, ng1, should be present in the database
        Collection<Group> inDB = _ds.getGroups(_groupAsgn);
        assertEquals(1, inDB.size());
        assertEquals(g1, Iterables.get(inDB, 0));
    }
    
    @Test
    public void testAddGroupsWhereOneHasMemberAlreadyInGroupFails() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        
        NewGroup ng1 = new NewGroup(_groupAsgn, "group1", student1);
        Group g1 = _ds.addGroup(ng1);
        
        NewGroup ng2 = new NewGroup(_groupAsgn, "group2", student2);
        NewGroup ng3 = new NewGroup(_groupAsgn, "group3", student1);
        
        //adding ng2 and ng3 should fail because student1 is in both ng1 and ng3
        try {
            _ds.addGroups(ImmutableList.of(ng2, ng3));
            fail();
        } catch (ServicesException e) {}
        
        //only one group, ng1, should be present in the database
        Collection<Group> inDB = _ds.getGroups(_groupAsgn);
        assertEquals(1, inDB.size());
        assertEquals(g1, Iterables.get(inDB, 0));
    }
    
    @Test
    public void testGetGroupSuccess() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        NewGroup ng = new NewGroup(_groupAsgn, student);
        Group addedGroup = _ds.addGroup(ng);
        
        Group retrievedGroup = _ds.getGroup(_groupAsgn, student);
        assertEquals(addedGroup, retrievedGroup);
    }
    
    @Test
    public void testGetNonExistentGroupFails() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        try {
            _ds.getGroup(_groupAsgn, student);
            fail();
        } catch (ServicesException e) { }  
    }
    
    @Test
    public void testGroupCreatedForNonGroupProject() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        
        //since the assignment is not a group assignment, a Group (of 1) for the student
        //should be created and returned even though it was not explicitly added
        Group group = _ds.getGroup(_nonGroupAsgn, student);
        assertNotNull(group);
        assertEquals(student.getLogin(), group.getName());
        assertEquals(1, group.getMembers().size());
        assertTrue(group.getMembers().contains(student));
        assertEquals(1, group.getMemberLogins().size());
        assertTrue(group.getMemberLogins().contains(student.getLogin()));
    }
    
    @Test
    public void testGroupsCreatedForNonGroupProject() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        
        //since the assignment is not a group assignment, a Group (of 1) for each student
        //should be created and returned even though they were not explicitly added
        Collection<Group> groups = _ds.getGroups(_nonGroupAsgn);
        assertEquals(2, groups.size());
        Group group1 = Iterables.get(groups, 0);
        Group group2 = Iterables.get(groups, 1);
        assertTrue(group1.getName().equals(student1.getLogin()) || group1.getName().equals(student2.getLogin()));
        if (group1.getName().equals(student1.getLogin())) {
            assertTrue(group2.getName().equals(student2.getLogin()));
        }
        else {
            assertTrue(group2.getName().equals(student1.getLogin()));
            Group tmp = group1;
            group1 = group2;
            group2 = tmp;
        }
        
        assertEquals(student1.getLogin(), group1.getName());
        assertEquals(1, group1.getMembers().size());
        assertTrue(group1.getMembers().contains(student1));
        assertEquals(1, group1.getMemberLogins().size());
        assertTrue(group1.getMemberLogins().contains(student1.getLogin()));
        
        assertEquals(student2.getLogin(), group2.getName());
        assertEquals(1, group2.getMembers().size());
        assertTrue(group2.getMembers().contains(student2));
        assertEquals(1, group2.getMemberLogins().size());
        assertTrue(group2.getMemberLogins().contains(student2.getLogin()));
    }
    
    @Test
    public void testGetGroupsEmptyWhenNoGroupsCreatedForGroupProject() throws ServicesException {
        //TODO document this behavior
        assertEquals(0, _ds.getGroups(_groupAsgn).size());
    }
    
    @Test
    public void testRemoveNoGroupsHasNoEffect() throws ServicesException {
        //no exceptions should be thrown
        _ds.removeGroups(_groupAsgn);
        _ds.removeGroups(_nonGroupAsgn);
    }
    
    @Test
    public void testRemoveGroups() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Student student3 = _ds.addStudent("login3", "first3", "last3", ValidityCheck.BYPASS);
        
        _ds.addGroup(new NewGroup(_groupAsgn, "group1", student1, student2));
        _ds.addGroup(new NewGroup(_groupAsgn, student3));
        
        //after adding groups, there should be 2 groups for the assignment
        assertEquals(2, _ds.getGroups(_groupAsgn).size());
        
        //after removing the groups, there should be no groups for the assignment
        _ds.removeGroups(_groupAsgn);
        assertEquals(0, _ds.getGroups(_groupAsgn).size());
    }
    
    @Test
    public void testRemoveGroupsOnlyAffectsGivenAssignment() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        
        _ds.addGroup(new NewGroup(_nonGroupAsgn, student1));
        _ds.addGroup(new NewGroup(_nonGroupAsgn, student2));
        _ds.addGroup(new NewGroup(_groupAsgn, "group", student1, student2));
        
        //after adding groups, there should be 2 groups for _nonGroupAsgn and 1 for _groupAsgn
        assertEquals(2, _ds.getGroups(_nonGroupAsgn).size());
        assertEquals(1, _ds.getGroups(_groupAsgn).size());
        
        //after removing groups for _groupAsgn, there should be no groups for
        //_groupAsgn and still 2 group for _nonGroupAsgn
        _ds.removeGroups(_groupAsgn);
        assertEquals(2, _ds.getGroups(_nonGroupAsgn).size());
        assertEquals(0, _ds.getGroups(_groupAsgn).size());
    }
    
    @Test
    public void testDistsStartEmpty() throws ServicesException {
        assertTrue(_ds.isDistEmpty(_groupAsgn));
        assertTrue(_ds.isDistEmpty(_nonGroupAsgn));
    }
    
    @Test
    public void testEntryInDistMapForEachTA() throws ServicesException {
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
        
        Map<TA, Collection<Group>> dpDist = _ds.getDistribution(dp);
        
        //each of the 2 TAs should have an entry in the map
        assertEquals(2, dpDist.size());
        assertTrue(dpDist.keySet().contains(_ta));
        assertTrue(dpDist.keySet().contains(_ta2));
    }
    
    @Test
    public void testEntryInDistMapIsEmptyCollectionWhenTAHasNoStudentsAssigned() throws ServicesException {
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
        
        Map<TA, Collection<Group>> dpDist = _ds.getDistribution(dp);

        assertEquals(0, dpDist.get(_ta).size());
        assertEquals(0, dpDist.get(_ta2).size());
    }
    
    @Test
    public void testGetSetDistributionForOneDPOneTAOneGroup() throws ServicesException {
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(dp.getAssignment(), student));
        
        Map<DistributablePart, Map<TA, Collection<Group>>> dist = new HashMap<DistributablePart, Map<TA, Collection<Group>>>();
        Multimap<TA, Group> taDist = ArrayListMultimap.create();
        taDist.put(_ta, group);
        dist.put(dp, taDist.asMap());
        
        _ds.setDistribution(dist);
        Map<TA, Collection<Group>> taDistOut = _ds.getDistribution(dp);
        assertEquals(1, taDistOut.get(_ta).size());
        assertEquals(group, Iterables.get(taDistOut.get(_ta), 0));
    }
    
    @Test
    public void testGetSetDistributionForOneDPOneTAMultipleGroups() throws ServicesException {
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Student student3 = _ds.addStudent("login3", "first3", "last3", ValidityCheck.BYPASS);
        Group group1 = _ds.addGroup(new NewGroup(dp.getAssignment(), student1));
        Group group2 = _ds.addGroup(new NewGroup(dp.getAssignment(), student2));
        Group group3 = _ds.addGroup(new NewGroup(dp.getAssignment(), student3));
        
        Map<DistributablePart, Map<TA, Collection<Group>>> dist = new HashMap<DistributablePart, Map<TA, Collection<Group>>>();
        Multimap<TA, Group> taDist = ArrayListMultimap.create();
        taDist.putAll(_ta, ImmutableList.of(group1, group2, group3));
        dist.put(dp, taDist.asMap());
        
        _ds.setDistribution(dist);
        Map<TA, Collection<Group>> taDistOut = _ds.getDistribution(dp);
        
        assertEquals(3, taDistOut.get(_ta).size());
        assertTrue(taDistOut.get(_ta).contains(group1));
        assertTrue(taDistOut.get(_ta).contains(group2));
        assertTrue(taDistOut.get(_ta).contains(group3));        
    }
    
    @Test 
    public void testDistributionOverwrites() throws ServicesException {
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Group group1 = _ds.addGroup(new NewGroup(dp.getAssignment(), student1));
        Group group2 = _ds.addGroup(new NewGroup(dp.getAssignment(), student2));
        
        Map<DistributablePart, Map<TA, Collection<Group>>> dist = new HashMap<DistributablePart, Map<TA, Collection<Group>>>();
        Multimap<TA, Group> dpDist = ArrayListMultimap.create();
        dpDist.put(_ta, group1);
        dist.put(dp, dpDist.asMap());
        
        _ds.setDistribution(dist);
        Map<TA, Collection<Group>> dpDistOut = _ds.getDistribution(dp);
        
        assertEquals(1, dpDistOut.get(_ta).size());
        assertTrue(dpDistOut.get(_ta).contains(group1));
        
        dist = new HashMap<DistributablePart, Map<TA, Collection<Group>>>();
        dpDist = ArrayListMultimap.create();
        dpDist.put(_ta, group2);
        dist.put(dp, dpDist.asMap());
        
        //setting the new distribution should succeed
        _ds.setDistribution(dist);
        dpDistOut = _ds.getDistribution(dp);
        
        //and the TA should now be assigned group2
        assertEquals(1, dpDistOut.get(_ta).size());
        assertTrue(dpDistOut.get(_ta).contains(group2));
    }
    
    @Test
    public void testAssignGroup() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
                
        _ds.assignGroup(group, dp, _ta);
        TA grader = _ds.getGrader(dp, group);
        assertEquals(_ta, grader);
    }
    
    @Test
    public void testAssignPreviouslyAssignedGroup() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
                
        _ds.assignGroup(group, dp, _ta);
        TA grader = _ds.getGrader(dp, group);
        assertEquals(_ta, grader);
        
        _ds.assignGroup(group, dp, _ta2);
        grader = _ds.getGrader(dp, group);
        assertEquals(_ta2, grader);
    }
    
    @Test
    public void testUnassignGroup() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
                
        _ds.assignGroup(group, dp, _ta);
        TA grader = _ds.getGrader(dp, group);
        assertEquals(_ta, grader);
        
        _ds.unassignGroup(group, dp, _ta);
        assertNull(_ds.getGrader(dp, group));
    }
    
    @Test
    public void testUnassignUnassignedGroup() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);

        _ds.unassignGroup(group, dp, _ta);
        assertNull(_ds.getGrader(dp, group));
    }
    
    @Test
    public void testInitiallyNoGroupsAssigned() throws ServicesException {
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
        assertEquals(0, _ds.getAssignedGroups(dp).size());
    }
    
    @Test
    public void testInitiallyNoGroupsAssignedToTA() throws ServicesException {
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
        assertEquals(0, _ds.getAssignedGroups(dp, _ta).size());
    }
    
    @Test
    public void testGetAssignedGroups1GroupForTA() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
                
        _ds.assignGroup(group, dp, _ta);
        Collection<Group> assignedGroups = _ds.getAssignedGroups(dp, _ta);
        assertEquals(1, assignedGroups.size());
        assertEquals(group, Iterables.get(assignedGroups, 0));
    }
    
    @Test
    public void testGetAssignedGroupsMultipleGroupsForTA() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Student student3 = _ds.addStudent("login3", "first3", "last3", ValidityCheck.BYPASS);
        Group group1 = _ds.addGroup(new NewGroup(_groupAsgn, student1));
        Group group2 = _ds.addGroup(new NewGroup(_groupAsgn, "group", student2, student3));
        DistributablePart dp = _groupAsgn.getDistributableParts().get(0);
                
        _ds.assignGroup(group1, dp, _ta);
        _ds.assignGroup(group2, dp, _ta);
        Collection<Group> assignedGroups = _ds.getAssignedGroups(dp, _ta);
        assertEquals(2, assignedGroups.size());
        assertTrue(assignedGroups.contains(group1));
        assertTrue(assignedGroups.contains(group2));
    }
    
    @Test
    public void testGetAssignedGroupsForMultipleTAs() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Group group1 = _ds.addGroup(new NewGroup(_nonGroupAsgn, student1));
        Group group2 = _ds.addGroup(new NewGroup(_nonGroupAsgn, student2));
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
                
        _ds.assignGroup(group1, dp, _ta);
        _ds.assignGroup(group2, dp, _ta2);
        Collection<Group> assignedGroups = _ds.getAssignedGroups(dp);
        assertEquals(2, assignedGroups.size());
        assertTrue(assignedGroups.contains(group1));
        assertTrue(assignedGroups.contains(group2));
    }
    
    @Test
    public void testGetGraderForUnassignedGroup() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        DistributablePart dp = _nonGroupAsgn.getDistributableParts().get(0);
                
        assertNull(_ds.getGrader(dp, group));
    }
    
    @Test
    public void testGrantExemptionWithNote() throws ServicesException {
        Part part = _groupAsgn.getParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        String note = "exemptions are fun!";
        
        _ds.grantExemption(group, part, note);
        assertTrue(_ds.getExemptions(part).contains(group));
        assertEquals(note, _ds.getExemptionNote(group, part));
    }
    
    @Test
    public void testGrantExemptionWithoutNote() throws ServicesException {
        Part part = _groupAsgn.getParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        
        //granting exemption with null note should succeed
        _ds.grantExemption(group, part, null);
        
        assertNull(_ds.getExemptionNote(group, part));

        //but should not affect the exemption being granted
        assertTrue(_ds.getExemptions(part).contains(group));
    }
    
    @Test
    public void testInitiallyNoExemptionsGranted() throws ServicesException {
        Part part = _nonGroupAsgn.getParts().get(0);
        assertEquals(0, _ds.getExemptions(part).size());
    }
    
    @Test
    public void testGroupNotGrantedExemptionDoesNotHaveExemption() throws ServicesException {
        Part part = _nonGroupAsgn.getParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student)); 
        
        assertFalse(_ds.getExemptions(part).contains(group));
    }
    
    @Test
    public void testNoExemptionNoteForGroupWithoutExemption() throws ServicesException {
        Part part = _nonGroupAsgn.getParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student)); 
        
        assertNull(_ds.getExemptionNote(group, part));
    }
    
    @Test
    public void testRemoveExemptionForGroupWithoutExemptionSucceeds() throws ServicesException {
        Part part = _nonGroupAsgn.getParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student)); 
        
        _ds.removeExemption(group, part);
    }
    
    @Test
    public void testRemoveExemptionForGroupWithExemption() throws ServicesException {
        Part part = _nonGroupAsgn.getParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        
        _ds.grantExemption(group, part, null);
        _ds.removeExemption(group, part);
        assertFalse(_ds.getExemptions(part).contains(group));
    }
    
    @Test
    public void testNoExemptionNoteAfterExemptionRemoved() throws ServicesException {
        Part part = _nonGroupAsgn.getParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        String note = "exemptions are fun!";
        
        _ds.grantExemption(group, part, note);
        _ds.removeExemption(group, part);
        
        assertNull(_ds.getExemptionNote(group, part));
    }
    
    @Test
    public void testGetExemptionsWhenMultipleExemptionsGranted() throws ServicesException {
        Part part = _nonGroupAsgn.getParts().get(0);
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Group group1 = _ds.addGroup(new NewGroup(_nonGroupAsgn, student1));
        Group group2 = _ds.addGroup(new NewGroup(_nonGroupAsgn, student2));
        
        _ds.grantExemption(group1, part, null);
        _ds.grantExemption(group2, part, null);
        
        Collection<Group> groupsWithExemption = _ds.getExemptions(part);
        assertEquals(2, groupsWithExemption.size());
        assertTrue(groupsWithExemption.contains(group1));
        assertTrue(groupsWithExemption.contains(group2));
    }
    
    @Test
    public void testGetExemptionsWhenMultiplePartsHaveExemptionsGranted() throws ServicesException {
        Part part1 = _nonGroupAsgn.getParts().get(0);
        Part part2 = _groupAsgn.getParts().get(0);
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Group group1ng = _ds.addGroup(new NewGroup(_nonGroupAsgn, student1));
        Group group2ng = _ds.addGroup(new NewGroup(_nonGroupAsgn, student2));
        Group group1g = _ds.addGroup(new NewGroup(_groupAsgn, "group", student1, student2));
        
        _ds.grantExemption(group1ng, part1, null);
        _ds.grantExemption(group2ng, part1, null);
        _ds.grantExemption(group1g, part2, null);
        
        Collection<Group> groupsWithExemptionNGA = _ds.getExemptions(part1);
        assertEquals(2, groupsWithExemptionNGA.size());
        assertTrue(groupsWithExemptionNGA.contains(group1ng));
        assertTrue(groupsWithExemptionNGA.contains(group2ng));
        
        Collection<Group> groupsWithExemptionGA = _ds.getExemptions(part2);
        assertEquals(1, groupsWithExemptionGA.size());
        assertTrue(groupsWithExemptionGA.contains(group1g));        
    }
    
    @Test
    public void testSetGetScoreForPart() throws ServicesException {
        Part part = _nonGroupAsgn.getParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        double score = 86.2;
        
        _ds.enterGrade(group, part, score);
        assertEquals(score, _ds.getScore(group, part), MAX_DOUBLE_DIFF);
    }
    
    @Test
    public void testGetPartScoreForGroupWithoutScoreReturnsNull() throws ServicesException {
        Part part = _nonGroupAsgn.getParts().get(0);
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        
        assertNull(_ds.getScore(group, part));
    }
    
    @Test
    public void testGetAsgnScoreForGroupWithoutScoreReturnsZero() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        
        assertEquals(0, _ds.getScore(group), MAX_DOUBLE_DIFF);
    }
    
    @Test
    public void testSetGetAsgnScoreForSinglePart() throws ServicesException { 
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        double score = 10.4;
        
        _ds.enterGrade(group, _nonGroupAsgn.getDistributableParts().get(0), score);
        
        assertEquals(score, _ds.getScore(group), MAX_DOUBLE_DIFF);
    }
    
    @Test
    public void testSetGetAsgnScoreForMultipleParts() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_twoDpAsgn, student));
        double score1 = 10.4, score2 = 8.5;
        
        _ds.enterGrade(group, _twoDpAsgn.getDistributableParts().get(0), score1);
        _ds.enterGrade(group, _twoDpAsgn.getDistributableParts().get(1), score2);
        
        assertEquals(score1+score2, _ds.getScore(group), MAX_DOUBLE_DIFF);
    }
    
    @Test
    public void testSetGetEarlyHandinStatus() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        HandinStatus hsIn = new HandinStatus(TimeStatus.EARLY, 0);
        
        _ds.setHandinStatus(group, hsIn);
        HandinStatus hsOut = _ds.getHandinStatus(group);
        assertEquals(hsIn.getTimeStatus(), hsOut.getTimeStatus());
        assertEquals(hsIn.getDaysLate(), hsOut.getDaysLate());
    }
    
    @Test
    public void testSetGetOnTimeHandinStatus() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        HandinStatus hsIn = new HandinStatus(TimeStatus.ON_TIME, 0);
        
        _ds.setHandinStatus(group, hsIn);
        HandinStatus hsOut = _ds.getHandinStatus(group);
        assertEquals(hsIn.getTimeStatus(), hsOut.getTimeStatus());
        assertEquals(hsIn.getDaysLate(), hsOut.getDaysLate());
    }
    
    @Test
    public void testSetGetLateHandinStatus() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        HandinStatus hsIn = new HandinStatus(TimeStatus.LATE, 2);
        
        _ds.setHandinStatus(group, hsIn);
        HandinStatus hsOut = _ds.getHandinStatus(group);
        assertEquals(hsIn.getTimeStatus(), hsOut.getTimeStatus());
        assertEquals(hsIn.getDaysLate(), hsOut.getDaysLate());
    }
    
    @Test
    public void testSetMultipleHandinStatuses() throws ServicesException {
        Student student1 = _ds.addStudent("login1", "first1", "last1", ValidityCheck.BYPASS);
        Student student2 = _ds.addStudent("login2", "first2", "last2", ValidityCheck.BYPASS);
        Group group1 = _ds.addGroup(new NewGroup(_nonGroupAsgn, student1));
        Group group2 = _ds.addGroup(new NewGroup(_nonGroupAsgn, student2));
        
        HandinStatus hs1In = new HandinStatus(TimeStatus.EARLY, 0);
        HandinStatus hs2In = new HandinStatus(TimeStatus.LATE, 2);
        Map<Group, HandinStatus> hsMap = new HashMap<Group, HandinStatus>();
        hsMap.put(group1, hs1In);
        hsMap.put(group2, hs2In);
        
        _ds.setHandinStatuses(hsMap);
        
        HandinStatus hs1Out = _ds.getHandinStatus(group1);
        assertEquals(hs1In.getTimeStatus(), hs1Out.getTimeStatus());
        assertEquals(hs1In.getDaysLate(), hs1Out.getDaysLate());
        
        HandinStatus hs2Out = _ds.getHandinStatus(group2);
        assertEquals(hs2In.getTimeStatus(), hs2Out.getTimeStatus());
        assertEquals(hs2In.getDaysLate(), hs2Out.getDaysLate());
    }
    
    @Test
    public void testHandinStatusesSet() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        Group group = _ds.addGroup(new NewGroup(_nonGroupAsgn, student));
        HandinStatus hsIn = new HandinStatus(TimeStatus.ON_TIME, 0);
        _ds.setHandinStatus(group, hsIn);
        
        assertTrue(_ds.areHandinStatusesSet(_nonGroupAsgn.getHandin()));
    }
    
    @Test
    public void testHandinStatusesNotSet() throws ServicesException {
        assertFalse(_ds.areHandinStatusesSet(_nonGroupAsgn.getHandin()));
    }
    
    @Test
    public void testGetStudentFromLogin() throws ServicesException {
        Student student = _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        assertEquals(student, _ds.getStudentFromLogin("login"));
    }
    
    @Test
    public void testGetNonExistentStudentFromLogin() throws ServicesException {
        assertNull(_ds.getStudentFromLogin("login"));
    }
    
    @Test
    public void testStudentLoginInDatabase() throws ServicesException {
        _ds.addStudent("login", "first", "last", ValidityCheck.BYPASS);
        assertTrue(_ds.isStudentLoginInDatabase("login"));
    }
    
    @Test
    public void testStudentLoginNotInDatabase() throws ServicesException {
        assertFalse(_ds.isStudentLoginInDatabase("nonexistent"));
    }
    
}
