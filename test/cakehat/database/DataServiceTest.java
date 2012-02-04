package cakehat.database;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import support.utils.SingleElementSet;
import static org.easymock.EasyMock.*;

/**
 *
 * @author Yudi
 */
public class DataServiceTest {
    
    private DataServices _dataService;
    private Database _database;
    private DbTA _dbTA1, _dbTA2;
    private DbAssignment _dbAsgnA, _dbAsgnB;
    private DbGradableEvent _geA, _geB;
    private DbPart _partA1, _partA2, _partB1;
    
    @Before
    public void setUp() throws ServicesException, SQLException, IOException {
        _database = new DatabaseImpl(Allocator.getFileSystemUtilities().createTempFile("tempDB", "db"));
        SingletonAllocation<Database> dbioAlloc =
            new SingletonAllocation<Database>()
            {
                public Database allocate() { return _database; };
            };
        new Allocator.Customizer().setDatabase(dbioAlloc).customize(); 
        _database.resetDatabase();
        
        //set-up ta
        int taId1 = 1;
        _dbTA1 = new DbTA(taId1, "taLogin1", "taFirst1", "taLast1", true, false);
        int taId2 = 2;
        _dbTA2 = new DbTA(taId2, "taLogin2", "taFirst2", "taLast2", true, false);
        _database.putTAs(ImmutableSet.of(_dbTA1, _dbTA2));
        
        //set up assignment
        _dbAsgnA = new DbAssignment("asgnA", 1);
        _dbAsgnB = new DbAssignment("asgnB", 2);
        _dbAsgnA.setHasGroups(true);
        _dbAsgnB.setHasGroups(false);
        _database.putAssignments(ImmutableSet.of(_dbAsgnA, _dbAsgnB));
        
        _geA = DbGradableEvent.build(_dbAsgnA, "geA", 1);
        _geB = DbGradableEvent.build(_dbAsgnB, "geB", 2);
        _database.putGradableEvents(ImmutableSet.of(_geA, _geB));
        
        _partA1 = DbPart.build(_geA, "partA1", 1);
        _partA2 = DbPart.build(_geA, "partA2", 2);
        _partB1 = DbPart.build(_geB, "partB1", 11);
        _partA1.setOutOf(10.0);
        _partA2.setOutOf(12.0);
        _partB1.setOutOf(15.0);
        _database.putParts(ImmutableSet.of(_partA1, _partA2, _partB1));
        
        _dataService = new DataServicesImpl();
    }
    
    @Test
    public void testGetStudentsFromEmptyDatabase() throws SQLException, ServicesException {
        Collection<Student> students = _dataService.getStudents();
        assertEquals(0, students.size());
    }
      
    @Test
    public void testGet1Students() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        _database.putStudents(SingleElementSet.of(dbstudent1));
        _dataService.updateDataCache();
        Collection<Student> students = _dataService.getStudents();
        this.assertDbStudentCollectionEqual(SingleElementSet.of(dbstudent1), students);
    }
    
    @Test
    public void testGet2Students() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataService.updateDataCache();
        Collection<Student> students = _dataService.getStudents();
        this.assertDbStudentCollectionEqual(ImmutableSet.of(dbstudent1, dbstudent2), students);
    }
    
    @Test
    public void testIsStudentLoginInDatabase() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        _database.putStudents(SingleElementSet.of(dbstudent1));
        _dataService.updateDataCache();
        
        assertTrue(_dataService.isStudentLoginInDatabase(dbstudent1.getLogin()));
        assertFalse(_dataService.isStudentLoginInDatabase("imaginary login"));
    }
    
    @Test
    public void testGetStudentFromLogin() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        _database.putStudents(SingleElementSet.of(dbstudent1));
        _dataService.updateDataCache();
        
        this.assertStudentEqual(dbstudent1, _dataService.getStudentFromLogin(dbstudent1.getLogin()));
        assertEquals(null, _dataService.getStudentFromLogin("imaginary login"));
    }
    
    @Test
    public void testAddSingleStudent() throws ServicesException {
        DbStudent toAdd = new DbStudent("login", "first", "last", "email");
        _dataService.addStudents(ImmutableSet.of(toAdd));
        assertNotNull(toAdd.getId());
        
        Set<Student> students = _dataService.getStudents();
        assertEquals(1, students.size());
        Student student = Iterables.get(students, 0);
        
        assertStudentEqual(toAdd, student);
    }
    
    @Test
    public void testEnabledStudents() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataService.updateDataCache();
        
        Collection<Student> students = _dataService.getEnabledStudents();

        this.assertDbStudentCollectionEqual(ImmutableSet.of(dbstudent1, dbstudent2), students);
    }
    
    @Test
    public void testTA() throws SQLException, ServicesException {
        this.assertTAEqual(_dbTA2, _dataService.getTA(_dbTA2.getId()));
        
        Set<TA> tas = _dataService.getTAs();
        this.assertTACollectionEqual(ImmutableSet.of(_dbTA1, _dbTA2), tas);
    }
    
    @Test
    public void testEmptyBlacklist() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataService.updateDataCache();
        
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        TA ta2 = _dataService.getTA(_dbTA2.getId());
        
        this.assertStudentCollectionEqual(Collections.<Student>emptyList(), _dataService.getBlacklistedStudents());
        this.assertStudentCollectionEqual(Collections.<Student>emptyList(), _dataService.getBlacklist(ta1));
        this.assertStudentCollectionEqual(Collections.<Student>emptyList(), _dataService.getBlacklist(ta2));
    }
    
    @Test
    public void testBlacklistOne() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataService.updateDataCache();
        
        Student student1 = _dataService.getStudentFromLogin(dbstudent1.getLogin());
        
        _dataService.blacklistStudents(SingleElementSet.of(student1), _dataService.getTA(_dbTA1.getId()));
        Collection<Student> blacklistedStuds = _dataService.getBlacklistedStudents();
        this.assertStudentCollectionEqual(SingleElementSet.of(student1), blacklistedStuds);
    }
    
    @Test
    public void testBlacklistTwo() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataService.updateDataCache();
        
        Student student1 = _dataService.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataService.getStudentFromLogin(dbstudent2.getLogin());
        
        _dataService.blacklistStudents(ImmutableSet.of(student1, student2), _dataService.getTA(_dbTA1.getId()));
        Collection<Student> blacklistedStuds = _dataService.getBlacklistedStudents();
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), blacklistedStuds);
    }
    
    @Test
    public void testBlacklistMultiTA() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataService.updateDataCache();
        
        Student student1 = _dataService.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataService.getStudentFromLogin(dbstudent2.getLogin());
        
        _dataService.blacklistStudents(SingleElementSet.of(student1), _dataService.getTA(_dbTA1.getId()));        
        _dataService.blacklistStudents(SingleElementSet.of(student2), _dataService.getTA(_dbTA2.getId()));
        Collection<Student> blacklistedStuds = _dataService.getBlacklistedStudents();
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), blacklistedStuds);
    }
    
    @Test
    public void testBlacklistOfTA() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataService.updateDataCache();
        
        Student student1 = _dataService.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataService.getStudentFromLogin(dbstudent2.getLogin());
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        TA ta2 = _dataService.getTA(_dbTA2.getId());
        
        _dataService.blacklistStudents(SingleElementSet.of(student1), ta1);
        _dataService.blacklistStudents(SingleElementSet.of(student2), ta2);
        
        this.assertStudentCollectionEqual(SingleElementSet.of(student1), _dataService.getBlacklist(ta1));
        this.assertStudentCollectionEqual(SingleElementSet.of(student2), _dataService.getBlacklist(ta2));
    }
    
    @Test
    public void testBlacklistOfTATwo() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        DbStudent dbstudent3 = new DbStudent("login3", "first2", "last3", "email3");
        DbStudent dbstudent4 = new DbStudent("login4", "first2", "last4", "email4");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2, dbstudent3, dbstudent4));
        _dataService.updateDataCache();
        
        Student student1 = _dataService.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataService.getStudentFromLogin(dbstudent2.getLogin());
        Student student3 = _dataService.getStudentFromLogin(dbstudent3.getLogin());
        Student student4 = _dataService.getStudentFromLogin(dbstudent4.getLogin());
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        TA ta2 = _dataService.getTA(_dbTA2.getId());
        
        _dataService.blacklistStudents(ImmutableSet.of(student1, student2), ta1);
        _dataService.blacklistStudents(ImmutableSet.of(student2, student3), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), _dataService.getBlacklist(ta1));
        this.assertStudentCollectionEqual(ImmutableSet.of(student2, student3), _dataService.getBlacklist(ta2));
    }
    
    @Test
    public void testUnBlacklist() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        DbStudent dbstudent3 = new DbStudent("login3", "first2", "last3", "email3");
        DbStudent dbstudent4 = new DbStudent("login4", "first2", "last4", "email4");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2, dbstudent3, dbstudent4));
        _dataService.updateDataCache();
        
        Student student1 = _dataService.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataService.getStudentFromLogin(dbstudent2.getLogin());
        Student student3 = _dataService.getStudentFromLogin(dbstudent3.getLogin());
        Student student4 = _dataService.getStudentFromLogin(dbstudent4.getLogin());
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        TA ta2 = _dataService.getTA(_dbTA2.getId());
        
        _dataService.blacklistStudents(ImmutableSet.of(student1, student2),ta1);
        _dataService.blacklistStudents(ImmutableSet.of(student2, student3), ta2);
        _dataService.unBlacklistStudents(SingleElementSet.of(student2), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), _dataService.getBlacklist(ta1));
        this.assertStudentCollectionEqual(SingleElementSet.of(student3), _dataService.getBlacklist(ta2));
    }
    
    @Test
    public void testUnBlacklistTwo() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        DbStudent dbstudent3 = new DbStudent("login3", "first2", "last3", "email3");
        DbStudent dbstudent4 = new DbStudent("login4", "first2", "last4", "email4");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2, dbstudent3, dbstudent4));
        _dataService.updateDataCache();
        
        Student student1 = _dataService.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataService.getStudentFromLogin(dbstudent2.getLogin());
        Student student3 = _dataService.getStudentFromLogin(dbstudent3.getLogin());
        Student student4 = _dataService.getStudentFromLogin(dbstudent4.getLogin());
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        TA ta2 = _dataService.getTA(_dbTA2.getId());
        
        _dataService.blacklistStudents(ImmutableSet.of(student1, student2), ta1);
        _dataService.blacklistStudents(ImmutableSet.of(student2, student3), ta2);
        _dataService.unBlacklistStudents(ImmutableSet.of(student1, student2), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), _dataService.getBlacklist(ta1));
        this.assertStudentCollectionEqual(SingleElementSet.of(student3), _dataService.getBlacklist(ta2));
    }
    
    @Test
    public void testUnBlacklistThree() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        DbStudent dbstudent3 = new DbStudent("login3", "first2", "last3", "email3");
        DbStudent dbstudent4 = new DbStudent("login4", "first2", "last4", "email4");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2, dbstudent3, dbstudent4));
        _dataService.updateDataCache();
        
        Student student1 = _dataService.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataService.getStudentFromLogin(dbstudent2.getLogin());
        Student student3 = _dataService.getStudentFromLogin(dbstudent3.getLogin());
        Student student4 = _dataService.getStudentFromLogin(dbstudent4.getLogin());
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        TA ta2 = _dataService.getTA(_dbTA2.getId());
        
        _dataService.blacklistStudents(ImmutableSet.of(student1, student2), ta1);
        _dataService.blacklistStudents(ImmutableSet.of(student2, student3), ta2);
        _dataService.unBlacklistStudents(ImmutableSet.of(student1, student2, student3), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), _dataService.getBlacklist(ta1));
        this.assertStudentCollectionEqual(Collections.<Student>emptySet(), _dataService.getBlacklist(ta2));
    }
    
    @Test
    public void testGetAssignments() throws SQLException, ServicesException {
        List<Assignment> assignments = _dataService.getAssignments();
        this.assertAsgnCollectionEqual(ImmutableSet.of(_dbAsgnA, _dbAsgnB), assignments);
    }
    
    @Test
    public void testGetNoGroup() throws SQLException, ServicesException {
        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        replay(asgnA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));  
        _dataService.updateDataCache();
        
        //an assignment has group, but without any group created should return an empty set
        Set<Group> groups = _dataService.getGroups(asgnA);
        assertEquals(0, groups.size());            
    }
    
    @Test
    public void testGetGroupAutoGroupCreation() throws SQLException, ServicesException {
        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        replay(asgnA);
            
        Assignment asgnB = createMock(Assignment.class);
        expect(asgnB.getName()).andReturn(_dbAsgnB.getName()).anyTimes();
        expect(asgnB.getId()).andReturn(_dbAsgnB.getId()).anyTimes();
        expect(asgnB.hasGroups()).andReturn(_dbAsgnB.hasGroups()).anyTimes();
        replay(asgnB);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
        
        //an assignment has no group, groups of one will be created and returned
        Set<Group> groups = _dataService.getGroups(asgnB);
        assertEquals(2, groups.size());
        Set<Student> studs = new HashSet<Student>();
        for (Group g : groups) {
            studs.addAll(g.getMembers());
        }
        this.assertDbStudentCollectionEqual(ImmutableSet.of(dbStudent1, dbStudent2), studs);
    }
    
    @Test
    public void testAddGetGroups() throws SQLException, ServicesException {
        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        replay(asgnA);
            
        Assignment asgnB = createMock(Assignment.class);
        expect(asgnB.getName()).andReturn(_dbAsgnB.getName()).anyTimes();
        expect(asgnB.getId()).andReturn(_dbAsgnB.getId()).anyTimes();
        expect(asgnB.hasGroups()).andReturn(_dbAsgnB.hasGroups()).anyTimes();
        replay(asgnB);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _dataService.addGroup(dbGroup1);

        Set<Group> groups = _dataService.getGroups(asgnA);
        this.assertGroupCollectionEqual(SingleElementSet.of(dbGroup1), groups);
    }
    
    @Test
    public void testGet2Groups() throws SQLException, ServicesException {
        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        replay(asgnA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _dataService.addGroup(dbGroup1);
        _dataService.addGroup(dbGroup2);
        
        Set<Group> groups = _dataService.getGroups(asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1, dbGroup2), groups);
    }
    
    @Test
    public void testAddGet2Groups() throws SQLException, ServicesException {
        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        replay(asgnA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();

            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _dataService.addGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Set<Group> groups = _dataService.getGroups(asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1, dbGroup2), groups);
    }
    
    @Test
    public void testGetGroup() throws SQLException, ServicesException {
        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        replay(asgnA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _dataService.addGroup(dbGroup1);
        
        //group doesn't exist
        assertNull(_dataService.getGroup(asgnA, student2));

        this.assertGroupEqual(dbGroup1, _dataService.getGroup(asgnA, student1));
    }
    
    @Test
    public void testRemoveGroups() throws SQLException, ServicesException {
        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        replay(asgnA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _dataService.addGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        _dataService.removeGroups(asgnA);
        assertEquals(0, _dataService.getGroups(asgnA).size());
    }
    
    @Test
    public void testAddAfterRemoveGroups() throws SQLException, ServicesException {
        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        replay(asgnA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _dataService.addGroups(SingleElementSet.of(dbGroup1));
        _dataService.addGroups(SingleElementSet.of(dbGroup2));
        _dataService.removeGroups(asgnA);        
        _dataService.addGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        Set<Group> groups = _dataService.getGroups(asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1, dbGroup2), groups);
    }
    
    private void assertDbStudentCollectionEqual(Collection<DbStudent> students1, Collection<Student> students2) {
        assertEquals(students1.size(), students2.size());
        Map<Integer, DbStudent> dbStudMap = new HashMap<Integer, DbStudent>();
        for (DbStudent stud : students1) {
            dbStudMap.put(stud.getId(), stud);
        }
        for (Student stud : students2) {
            assertTrue(dbStudMap.containsKey(stud.getId()));
            assertStudentEqual(dbStudMap.get(stud.getId()), stud);
        }
        
        Map<Integer, Student> studMap = new HashMap<Integer, Student>();
        for (Student stud : students2) {
            studMap.put(stud.getId(), stud);
        }
        for (DbStudent stud : students1) {
            assertTrue(studMap.containsKey(stud.getId()));
            assertStudentEqual(stud, studMap.get(stud.getId()));
        }
    }
            
    private void assertStudentCollectionEqual(Collection<Student> students1, Collection<Student> students2) {
        assertEquals(students1.size(), students2.size());
        Map<Integer, Student> studMap = new HashMap<Integer, Student>();
        for (Student stud : students1) {
            studMap.put(stud.getId(), stud);
        }
        for (Student stud : students2) {
            assertTrue(studMap.containsKey(stud.getId()));
            assertStudentEqual(stud, studMap.get(stud.getId()));
        }
        
        studMap.clear();
        for (Student stud : students2) {
            studMap.put(stud.getId(), stud);
        }
        for (Student stud : students1) {
            assertTrue(studMap.containsKey(stud.getId()));
            assertStudentEqual(stud, studMap.get(stud.getId()));
        }
    }
    
    private void assertIDStudentCollectionEqual(Collection<Integer> studentIDs, Collection<Student> students) {
        assertEquals(studentIDs.size(), students.size());
        for (Student stud : students) {
            assertTrue(studentIDs.contains(stud.getId()));
        }
        
        Set<Integer> ids = new HashSet<Integer>();
        for (Student stud : students) {
            ids.add(stud.getId());
        }
        for (int id : studentIDs) {
            assertTrue(ids.contains(id));
        }
    }
    
    private void assertGroupCollectionEqual(Collection<DbGroup> groups1, Collection<Group> groups2) {
        assertEquals(groups1.size(), groups2.size());
        Map<Integer, DbGroup> dbGroupMap = new HashMap<Integer, DbGroup>();
        for (DbGroup group : groups1) {
            dbGroupMap.put(group.getId(), group);
        }
        for (Group group : groups2) {
            assertTrue(dbGroupMap.containsKey(group.getId()));
            DbGroup dbGroup = dbGroupMap.get(group.getId());
            this.assertGroupEqual(dbGroup, group);    
        }
        
        Map<Integer, Group> groupMap = new HashMap<Integer, Group>();
        for (Group group : groups2) {
            groupMap.put(group.getId(), group);
        }
        for (DbGroup dbgroup : groups1) {
            assertTrue(groupMap.containsKey(dbgroup.getId()));
            Group group = groupMap.get(dbgroup.getId());
            this.assertGroupEqual(dbgroup, group);    
        }
        
    } 
    
    private void assertTACollectionEqual(Collection<DbTA> tas1, Collection<TA> tas2) {
        assertEquals(tas1.size(), tas2.size());
        Map<Integer, DbTA> dbTAMap = new HashMap<Integer, DbTA>();
        for (DbTA ta : tas1) {
            dbTAMap.put(ta.getId(), ta);
        }
        for (TA ta : tas2) {
            assertTrue(dbTAMap.containsKey(ta.getId()));
            assertTAEqual(dbTAMap.get(ta.getId()), ta);
        }
        
        Map<Integer, TA> taMap = new HashMap<Integer, TA>();
        for (TA ta : tas2) {
            taMap.put(ta.getId(), ta);
        }
        for (DbTA ta : tas1) {
            assertTrue(taMap.containsKey(ta.getId()));
            assertTAEqual(ta, taMap.get(ta.getId()));
        }
    }
    
    private void assertAsgnCollectionEqual(Collection<DbAssignment> asgns1, Collection<Assignment> asgns2) {
        assertEquals(asgns1.size(), asgns2.size());
        Map<Integer, DbAssignment> dbAsgnMap = new HashMap<Integer, DbAssignment>();
        for (DbAssignment asgn : asgns1) {
            dbAsgnMap.put(asgn.getId(), asgn);
        }
        for (Assignment asgn : asgns2) {
            assertTrue(dbAsgnMap.containsKey(asgn.getId()));
            assertAssignmentEqual(dbAsgnMap.get(asgn.getId()), asgn);
        }
        
        Map<Integer, Assignment> asgnMap = new HashMap<Integer, Assignment>();
        for (Assignment asgn : asgns2) {
            asgnMap.put(asgn.getId(), asgn);
        }
        for (DbAssignment asgn : asgns1) {
            assertTrue(asgnMap.containsKey(asgn.getId()));
            assertAssignmentEqual(asgn, asgnMap.get(asgn.getId()));
        }
    }
    
    private void assertGECollectionEqual(Collection<DbGradableEvent> ges1, Collection<GradableEvent> ges2) {
        assertEquals(ges1.size(), ges2.size());
        Map<Integer, DbGradableEvent> dbGeMap = new HashMap<Integer, DbGradableEvent>();
        for (DbGradableEvent ge : ges1) {
            dbGeMap.put(ge.getId(), ge);
        }
        for (GradableEvent ge : ges2) {
            assertTrue(dbGeMap.containsKey(ge.getId()));
            assertGradableEventEqual(dbGeMap.get(ge.getId()), ge);
        }
        
        Map<Integer, GradableEvent> geMap = new HashMap<Integer, GradableEvent>();
        for (GradableEvent ge : ges2) {
            geMap.put(ge.getId(), ge);
        }
        for (DbGradableEvent ge : ges1) {
            assertTrue(geMap.containsKey(ge.getId()));
            assertGradableEventEqual(ge, geMap.get(ge.getId()));
        }
    }
    
    private void assertPartCollectionEqual(Collection<DbPart> parts1, Collection<Part> parts2) {
        assertEquals(parts1.size(), parts2.size());
        Map<Integer, DbPart> dbPartMap = new HashMap<Integer, DbPart>();
        for (DbPart part : parts1) {
            dbPartMap.put(part.getId(), part);
        }
        for (Part part : parts2) {
            assertTrue(dbPartMap.containsKey(part.getId()));
            assertPartEqual(dbPartMap.get(part.getId()), part);
        }
        
        Map<Integer, Part> partMap = new HashMap<Integer, Part>();
        for (Part part : parts2) {
            partMap.put(part.getId(), part);
        }
        for (DbPart part : parts1) {
            assertTrue(partMap.containsKey(part.getId()));
            assertPartEqual(part, partMap.get(part.getId()));
        }
    }
        
    private void assertStudentEqual(DbStudent dbstudent, Student student) {
        assertEquals((int)dbstudent.getId(), (int)student.getId());
        assertEquals(dbstudent.getLogin(), student.getLogin());
        assertEquals(dbstudent.getFirstName(), student.getFirstName());
        assertEquals(dbstudent.getLastName(), student.getLastName());
        assertEquals(dbstudent.getEmailAddress(), student.getEmailAddress().getAddress());
    }
    
    private void assertStudentEqual(Student student1, Student student2) {
        assertEquals((int)student1.getId(), (int)student2.getId());
        assertEquals(student1.getLogin(), student2.getLogin());
        assertEquals(student1.getFirstName(), student2.getFirstName());
        assertEquals(student1.getLastName(), student2.getLastName());
        assertEquals(student1.getEmailAddress().getAddress(), student2.getEmailAddress().getAddress());
    }
    
    private void assertGroupEqual(DbGroup dbGroup, Group group) {
        assertEquals((int)dbGroup.getId(), (int)group.getId());
        assertEquals(dbGroup.getName(), group.getName());
        assertEquals(dbGroup.getAssignmentId(), group.getAssignment().getId());
        assertEquals(dbGroup.getMemberIds().size(), group.size());
        this.assertIDStudentCollectionEqual(dbGroup.getMemberIds(), group.getMembers());

    }
    
    private void assertTAEqual(DbTA dbTA, TA ta) {
        assertEquals((int)dbTA.getId(), (int)ta.getId());
        assertEquals(dbTA.getLogin(), ta.getLogin());
        assertEquals(dbTA.getFirstName(), ta.getFirstName());
        assertEquals(dbTA.getLastName(), ta.getLastName());
        assertEquals(dbTA.isAdmin(), ta.isAdmin());
        assertEquals(dbTA.isDefaultGrader(), ta.isDefaultGrader());
    }
    
    private void assertAssignmentEqual(DbAssignment dbAsgn, Assignment asgn) {
        assertEquals((int)dbAsgn.getId(), (int)asgn.getId());
        assertEquals(dbAsgn.getName(), asgn.getName());
        this.assertGECollectionEqual(dbAsgn.getGradableEvents(), asgn.getGradableEvents());
    }
    
    private void assertGradableEventEqual(DbGradableEvent dbGE, GradableEvent ge) {
        assertEquals((int)dbGE.getId(), (int)ge.getId());
        assertEquals(dbGE.getName(), ge.getName());
        this.assertPartCollectionEqual(dbGE.getParts(), ge.getParts());
    }
    
    private void assertPartEqual(DbPart dbpart, Part part) {
        assertEquals((int) dbpart.getId(), (int) part.getId());
        assertEquals(dbpart.getName(), part.getName());
        assertEquals(dbpart.getOutOf(), part.getOutOf(), 10E-10);
        assertEquals(dbpart.getQuickName(), part.getQuickName());
    }
}
