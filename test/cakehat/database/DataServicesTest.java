package cakehat.database;

import java.util.ArrayList;
import java.io.File;
import org.joda.time.DateTime;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.CakehatSession;
import cakehat.TestCakehatSessionProvider;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author Yudi
 */
public class DataServicesTest {

    private static final int USER_TA_UID = 429;
    
    private DataServices _dataServices;
    private Database _database;
    private DbTA _dbUserTA, _dbTA2;
    private DbAssignment _dbAsgnA, _dbAsgnB;
    private DbGradableEvent _dbGeA, _dbGeB;
    private DbPart _dbPartA1, _dbPartA2, _dbPartB1;
    private Assignment _asgnA, _asgnB;
    private GradableEvent _geA, _geB;
    
    @Before
    public void setUp() throws ServicesException, SQLException, IOException {
        _database = new DatabaseImpl(Allocator.getFileSystemUtilities().createTempFile("tempDB", "db"));
        SingletonAllocation<Database> dbioAlloc =
            new SingletonAllocation<Database>()
            {
                public Database allocate() { return _database; };
            };
        _database.resetDatabase();
        
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();
        
        CakehatSession.setSessionProviderForTesting(new TestCakehatSessionProvider(USER_TA_UID)); 
        
        //set-up tas
        _dbUserTA = new DbTA(USER_TA_UID, "taLogin1", "taFirst1", "taLast1", true, false);
        _dbTA2 = new DbTA(137, "taLogin2", "taFirst2", "taLast2", true, false);
        _database.putTAs(ImmutableSet.of(_dbUserTA, _dbTA2));
        
        //set up assignment
        _dbAsgnA = new DbAssignment("asgnA", 1);
        _dbAsgnB = new DbAssignment("asgnB", 2);
        _dbAsgnA.setHasGroups(true);
        _dbAsgnB.setHasGroups(false);
        _database.putAssignments(ImmutableSet.of(_dbAsgnA, _dbAsgnB));
        
        _asgnA = createMock(Assignment.class);
        expect(_asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(_asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(_asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        replay(_asgnA);
        
        _asgnB = createMock(Assignment.class);
        expect(_asgnB.getName()).andReturn(_dbAsgnB.getName()).anyTimes();
        expect(_asgnB.getId()).andReturn(_dbAsgnB.getId()).anyTimes();
        expect(_asgnB.hasGroups()).andReturn(_dbAsgnB.hasGroups()).anyTimes();
        replay(_asgnB);
        
        _dbGeA = DbGradableEvent.build(_dbAsgnA, "geA", 1);
        _dbGeB = DbGradableEvent.build(_dbAsgnB, "geB", 2);
        _database.putGradableEvents(ImmutableSet.of(_dbGeA, _dbGeB));
        
        _geA = createMock(GradableEvent.class);
        expect(_geA.getAssignment()).andReturn(_asgnA).anyTimes();
        expect(_geA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        replay(_geA);
        
        _geB = createMock(GradableEvent.class);
        expect(_geB.getAssignment()).andReturn(_asgnB).anyTimes();
        expect(_geB.getId()).andReturn(_dbGeB.getId()).anyTimes();
        replay(_geB);
        
        _dbPartA1 = DbPart.build(_dbGeA, "partA1", 1);
        _dbPartA2 = DbPart.build(_dbGeA, "partA2", 2);
        _dbPartB1 = DbPart.build(_dbGeB, "partB1", 11);
        _dbPartA1.setOutOf(10.0);
        _dbPartA2.setOutOf(12.0);
        _dbPartB1.setOutOf(15.0);
        _database.putParts(ImmutableSet.of(_dbPartA1, _dbPartA2, _dbPartB1));
        
        _dataServices = new DataServicesImpl();
    }
    
    @Test
    public void testSetGetGradableEventOccurrences() throws ServicesException, SQLException
    {
        //Setup
        DbStudent student1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent student2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2");
        _dataServices.addStudents(ImmutableSet.of(student1, student2));
        
        DbGroup dbGroup1 = new DbGroup(_asgnA, "group 1", ImmutableSet.of(new Student(student1)));
        DbGroup dbGroup2 = new DbGroup(_asgnA, "group 2", ImmutableSet.of(new Student(student2)));
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        Set<Group> groups = _dataServices.getGroups(_asgnA);
        
        Map<Group, DateTime> occurrenceDates = new HashMap<Group, DateTime>();
        for(Group group : groups)
        {
            occurrenceDates.put(group, new DateTime());
        }
        
        //Set
        _dataServices.setGradableEventOccurrences(_geA, occurrenceDates);

        //Get
        Map<Group, GradableEventOccurrence> occurrences = _dataServices.getGradableEventOccurrences(_geA, groups);
        
        //Validate
        assertEquals(occurrenceDates.size(), occurrences.size());
        for(Group group : groups)
        {
            assertTrue(occurrences.containsKey(group));
            
            assertEquals(group, occurrences.get(group).getGroup());
            assertEquals(_geA, occurrences.get(group).getGradableEvent());
            assertEquals(occurrenceDates.get(group), occurrences.get(group).getOccurrenceDate());
        }
    }
    
    @Test
    public void testSetDeleteGetGradableEventOccurrences() throws ServicesException, SQLException
    {
        //Setup
        DbStudent student1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent student2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2");
        _dataServices.addStudents(ImmutableSet.of(student1, student2));
        
        DbGroup dbGroup1 = new DbGroup(_asgnA, "group 1", ImmutableSet.of(new Student(student1)));
        DbGroup dbGroup2 = new DbGroup(_asgnA, "group 2", ImmutableSet.of(new Student(student2)));
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        Set<Group> groups = _dataServices.getGroups(_asgnA);
        
        Map<Group, DateTime> occurrenceDates = new HashMap<Group, DateTime>();
        for(Group group : groups)
        {
            occurrenceDates.put(group, new DateTime());
        }
        
        //Set
        _dataServices.setGradableEventOccurrences(_geA, occurrenceDates);

        //Delete
        _dataServices.deleteGradableEventOccurrences(_geA, groups);
        
        //Get
        Map<Group, GradableEventOccurrence> occurrences = _dataServices.getGradableEventOccurrences(_geA, groups);
        
        //Validate
        assertTrue(occurrences.isEmpty());
    }

    @Test
    public void testGetStudentsFromEmptyDatabase() throws SQLException, ServicesException {
        Collection<Student> students = _dataServices.getStudents();
        assertEquals(0, students.size());
    }
    
    @Test
    public void testGet1Students() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        _database.putStudents(ImmutableSet.of(dbstudent1));
        _dataServices.updateDataCache();
        Collection<Student> students = _dataServices.getStudents();
        this.assertDbStudentCollectionEqual(ImmutableSet.of(dbstudent1), students);
    }
    
    @Test
    public void testGet2Students() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataServices.updateDataCache();
        Collection<Student> students = _dataServices.getStudents();
        this.assertDbStudentCollectionEqual(ImmutableSet.of(dbstudent1, dbstudent2), students);
    }
    
    @Test
    public void testIsStudentLoginInDatabase() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        _database.putStudents(ImmutableSet.of(dbstudent1));
        _dataServices.updateDataCache();
        
        assertTrue(_dataServices.isStudentLoginInDatabase(dbstudent1.getLogin()));
        assertFalse(_dataServices.isStudentLoginInDatabase("imaginary login"));
    }
    
    @Test
    public void testGetStudentFromLogin() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        _database.putStudents(ImmutableSet.of(dbstudent1));
        _dataServices.updateDataCache();
        
        this.assertStudentEqual(dbstudent1, _dataServices.getStudentFromLogin(dbstudent1.getLogin()));
        assertEquals(null, _dataServices.getStudentFromLogin("imaginary login"));
    }
    
    @Test
    public void testAddSingleStudent() throws ServicesException {
        DbStudent toAdd = new DbStudent("login", "first", "last", "email");
        _dataServices.addStudents(ImmutableSet.of(toAdd));
        assertNotNull(toAdd.getId());
        
        Set<Student> students = _dataServices.getStudents();
        assertEquals(1, students.size());
        Student student = Iterables.get(students, 0);
        
        assertStudentEqual(toAdd, student);
    }
    
    @Test
    public void testEnabledStudents() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataServices.updateDataCache();
        
        Collection<Student> students = _dataServices.getEnabledStudents();

        this.assertDbStudentCollectionEqual(ImmutableSet.of(dbstudent1, dbstudent2), students);
    }
    
    @Test
    public void testTA() throws SQLException, ServicesException {
        this.assertTAEqual(_dbTA2, _dataServices.getTA(_dbTA2.getId()));
        
        Set<TA> tas = _dataServices.getTAs();
        this.assertTACollectionEqual(ImmutableSet.of(_dbUserTA, _dbTA2), tas);
    }
    
    @Test
    public void testEmptyBlacklist() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataServices.updateDataCache();
        
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        TA ta2 = _dataServices.getTA(_dbTA2.getId());
        
        this.assertStudentCollectionEqual(Collections.<Student>emptyList(), _dataServices.getBlacklistedStudents());
        this.assertStudentCollectionEqual(Collections.<Student>emptyList(), _dataServices.getBlacklist(ta1));
        this.assertStudentCollectionEqual(Collections.<Student>emptyList(), _dataServices.getBlacklist(ta2));
    }
    
    @Test
    public void testBlacklistOne() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataServices.updateDataCache();
        
        Student student1 = _dataServices.getStudentFromLogin(dbstudent1.getLogin());
        
        _dataServices.blacklistStudents(ImmutableSet.of(student1), _dataServices.getTA(_dbUserTA.getId()));
        Collection<Student> blacklistedStuds = _dataServices.getBlacklistedStudents();
        this.assertStudentCollectionEqual(ImmutableSet.of(student1), blacklistedStuds);
    }
    
    @Test
    public void testBlacklistTwo() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataServices.updateDataCache();
        
        Student student1 = _dataServices.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataServices.getStudentFromLogin(dbstudent2.getLogin());
        
        _dataServices.blacklistStudents(ImmutableSet.of(student1, student2), _dataServices.getTA(_dbUserTA.getId()));
        Collection<Student> blacklistedStuds = _dataServices.getBlacklistedStudents();
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), blacklistedStuds);
    }
    
    @Test
    public void testBlacklistMultiTA() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataServices.updateDataCache();
        
        Student student1 = _dataServices.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataServices.getStudentFromLogin(dbstudent2.getLogin());
        
        _dataServices.blacklistStudents(ImmutableSet.of(student1), _dataServices.getTA(_dbUserTA.getId()));        
        _dataServices.blacklistStudents(ImmutableSet.of(student2), _dataServices.getTA(_dbTA2.getId()));
        Collection<Student> blacklistedStuds = _dataServices.getBlacklistedStudents();
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), blacklistedStuds);
    }
    
    @Test
    public void testBlacklistOfTA() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2));
        _dataServices.updateDataCache();
        
        Student student1 = _dataServices.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataServices.getStudentFromLogin(dbstudent2.getLogin());
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        TA ta2 = _dataServices.getTA(_dbTA2.getId());
        
        _dataServices.blacklistStudents(ImmutableSet.of(student1), ta1);
        _dataServices.blacklistStudents(ImmutableSet.of(student2), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1), _dataServices.getBlacklist(ta1));
        this.assertStudentCollectionEqual(ImmutableSet.of(student2), _dataServices.getBlacklist(ta2));
    }
    
    @Test
    public void testBlacklistOfTATwo() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        DbStudent dbstudent3 = new DbStudent("login3", "first2", "last3", "email3");
        DbStudent dbstudent4 = new DbStudent("login4", "first2", "last4", "email4");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2, dbstudent3, dbstudent4));
        _dataServices.updateDataCache();
        
        Student student1 = _dataServices.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataServices.getStudentFromLogin(dbstudent2.getLogin());
        Student student3 = _dataServices.getStudentFromLogin(dbstudent3.getLogin());
        Student student4 = _dataServices.getStudentFromLogin(dbstudent4.getLogin());
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        TA ta2 = _dataServices.getTA(_dbTA2.getId());
        
        _dataServices.blacklistStudents(ImmutableSet.of(student1, student2), ta1);
        _dataServices.blacklistStudents(ImmutableSet.of(student2, student3), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), _dataServices.getBlacklist(ta1));
        this.assertStudentCollectionEqual(ImmutableSet.of(student2, student3), _dataServices.getBlacklist(ta2));
    }
    
    @Test
    public void testUnBlacklist() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        DbStudent dbstudent3 = new DbStudent("login3", "first2", "last3", "email3");
        DbStudent dbstudent4 = new DbStudent("login4", "first2", "last4", "email4");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2, dbstudent3, dbstudent4));
        _dataServices.updateDataCache();
        
        Student student1 = _dataServices.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataServices.getStudentFromLogin(dbstudent2.getLogin());
        Student student3 = _dataServices.getStudentFromLogin(dbstudent3.getLogin());
        Student student4 = _dataServices.getStudentFromLogin(dbstudent4.getLogin());
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        TA ta2 = _dataServices.getTA(_dbTA2.getId());
        
        _dataServices.blacklistStudents(ImmutableSet.of(student1, student2),ta1);
        _dataServices.blacklistStudents(ImmutableSet.of(student2, student3), ta2);
        _dataServices.unBlacklistStudents(ImmutableSet.of(student2), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), _dataServices.getBlacklist(ta1));
        this.assertStudentCollectionEqual(ImmutableSet.of(student3), _dataServices.getBlacklist(ta2));
    }
    
    @Test
    public void testUnBlacklistTwo() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        DbStudent dbstudent3 = new DbStudent("login3", "first2", "last3", "email3");
        DbStudent dbstudent4 = new DbStudent("login4", "first2", "last4", "email4");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2, dbstudent3, dbstudent4));
        _dataServices.updateDataCache();
        
        Student student1 = _dataServices.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataServices.getStudentFromLogin(dbstudent2.getLogin());
        Student student3 = _dataServices.getStudentFromLogin(dbstudent3.getLogin());
        Student student4 = _dataServices.getStudentFromLogin(dbstudent4.getLogin());
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        TA ta2 = _dataServices.getTA(_dbTA2.getId());
        
        _dataServices.blacklistStudents(ImmutableSet.of(student1, student2), ta1);
        _dataServices.blacklistStudents(ImmutableSet.of(student2, student3), ta2);
        _dataServices.unBlacklistStudents(ImmutableSet.of(student1, student2), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), _dataServices.getBlacklist(ta1));
        this.assertStudentCollectionEqual(ImmutableSet.of(student3), _dataServices.getBlacklist(ta2));
    }
    
    @Test
    public void testUnBlacklistThree() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        DbStudent dbstudent2 = new DbStudent("login2", "first2", "last2", "email2");
        DbStudent dbstudent3 = new DbStudent("login3", "first2", "last3", "email3");
        DbStudent dbstudent4 = new DbStudent("login4", "first2", "last4", "email4");
        _database.putStudents(ImmutableSet.of(dbstudent1, dbstudent2, dbstudent3, dbstudent4));
        _dataServices.updateDataCache();
        
        Student student1 = _dataServices.getStudentFromLogin(dbstudent1.getLogin());
        Student student2 = _dataServices.getStudentFromLogin(dbstudent2.getLogin());
        Student student3 = _dataServices.getStudentFromLogin(dbstudent3.getLogin());
        Student student4 = _dataServices.getStudentFromLogin(dbstudent4.getLogin());
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        TA ta2 = _dataServices.getTA(_dbTA2.getId());
        
        _dataServices.blacklistStudents(ImmutableSet.of(student1, student2), ta1);
        _dataServices.blacklistStudents(ImmutableSet.of(student2, student3), ta2);
        _dataServices.unBlacklistStudents(ImmutableSet.of(student1, student2, student3), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), _dataServices.getBlacklist(ta1));
        this.assertStudentCollectionEqual(Collections.<Student>emptySet(), _dataServices.getBlacklist(ta2));
    }
    
    @Test
    public void testGetAssignments() throws SQLException, ServicesException {
        List<Assignment> assignments = _dataServices.getAssignments();
        this.assertAsgnCollectionEqual(ImmutableSet.of(_dbAsgnA, _dbAsgnB), assignments);
    }
    
    @Test
    public void testGetNoGroup() throws SQLException, ServicesException {
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));  
        _dataServices.updateDataCache();
        
        //an assignment has group, but without any group created should return an empty set
        Set<Group> groups = _dataServices.getGroups(_asgnA);
        assertEquals(0, groups.size());            
    }
    
    @Test
    public void testGetGroupAutoGroupCreation() throws SQLException, ServicesException {        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataServices.updateDataCache();
        
        //an assignment has no group, groups of one will be created and returned
        Set<Group> groups = _dataServices.getGroups(_asgnB);
        assertEquals(2, groups.size());
        Set<Student> studs = new HashSet<Student>();
        for (Group g : groups) {
            studs.addAll(g.getMembers());
        }
        this.assertDbStudentCollectionEqual(ImmutableSet.of(dbStudent1, dbStudent2), studs);
    }
    
    @Test
    public void testAddGetGroups() throws SQLException, ServicesException {
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataServices.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));

        Set<Group> groups = _dataServices.getGroups(_asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1), groups);
    }
    
    @Test
    public void testGet2Groups() throws SQLException, ServicesException {
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataServices.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Set<Group> groups = _dataServices.getGroups(_asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1, dbGroup2), groups);
    }
    
    @Test
    public void testAddGet2Groups() throws SQLException, ServicesException {
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataServices.updateDataCache();

            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Set<Group> groups = _dataServices.getGroups(_asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1, dbGroup2), groups);
    }
    
    @Test
    public void testGetGroup() throws SQLException, ServicesException {
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataServices.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        //group doesn't exist
        assertNull(_dataServices.getGroup(_asgnA, student2));

        this.assertGroupEqual(dbGroup1, _dataServices.getGroup(_asgnA, student1));
    }
    
    @Test
    public void testAssignUnassignedGroup() throws SQLException, ServicesException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());
        
        
        _dataServices.setGrader(part, group, ta);
        
        assertEquals(1, _dataServices.getAssignedGroups(part, ta).size());
        assertTrue(_dataServices.getAssignedGroups(part, ta).contains(group));
    }
    
    @Test
    public void testAssignPreviouslyAssignedGroup() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        TA ta2 = _dataServices.getTA(_dbTA2.getId());
        
        // first assign group1 to ta1
        _dataServices.setGrader(part, group, ta1);
        
        assertEquals(1, _dataServices.getAssignedGroups(part, ta1).size());
        assertTrue(_dataServices.getAssignedGroups(part, ta1).contains(group));
        
        // reassign group1 to ta2
        _dataServices.setGrader(part, group, ta2);
        
        // check that group1 is now assigned to ta2 and not ta1
        assertEquals(0, _dataServices.getAssignedGroups(part, ta1).size());
        assertFalse(_dataServices.getAssignedGroups(part, ta1).contains(group));
        
        assertEquals(1, _dataServices.getAssignedGroups(part, ta2).size());
        assertTrue(_dataServices.getAssignedGroups(part, ta2).contains(group));
    }
    
    @Test
    public void testNoEffectWhenAlreadyAssignedToSameTA() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());

        _dataServices.setGrader(part, group, ta);
        
        assertEquals(1, _dataServices.getAssignedGroups(part, ta).size());
        assertTrue(_dataServices.getAssignedGroups(part, ta).contains(group));
        
        // this shouldn't throw an exception or do anything 
        _dataServices.setGrader(part, group, ta);
        
        assertEquals(1, _dataServices.getAssignedGroups(part, ta).size());
        assertTrue(_dataServices.getAssignedGroups(part, ta).contains(group));
    }
    
    @Test
    public void testUnassignGroup() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        events.add(eventA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());

        _dataServices.setGrader(part, group, ta);
        
        assertEquals(1, _dataServices.getAssignedGroups(part, ta).size());
        assertTrue(_dataServices.getAssignedGroups(part, ta).contains(group));
        
        // unassign the group1 and check that the group1 is no longer assigned to the ta1
        _dataServices.setGrader(part, group, null);
        
        assertEquals(0, _dataServices.getAssignedGroups(part, ta).size());
    }
    
    @Test
    public void testUnassignGroupWhenGroupWasNotAssigned() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());

        // group1 has not been assigned
        
        assertEquals(0, _dataServices.getAssignedGroups(part, ta).size());
        
        // unassign the group1 and check that nothing happens
        _dataServices.setGrader(part, group, null);
        
        assertEquals(0, _dataServices.getAssignedGroups(part, ta).size());
    }
    
    @Test
    public void testGetAssignedGroupsForTAWhenNoneAssigned() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        events.add(eventA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        replay(partA);
        
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());
        
        // make sure that getAssignedGroups returns an empty collection
        assertEquals(0, _dataServices.getAssignedGroups(part, ta).size());
    }
    
    @Test
    public void testGetAssignedGroupsForTAWhenOneAssigned() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());

        _dataServices.setGrader(part, group, ta);
        
        // check that the list only has 1 group1 in it and it is the correct group1
        assertEquals(1, _dataServices.getAssignedGroups(part, ta).size());
        assertTrue(_dataServices.getAssignedGroups(part, ta).contains(group));
    }
    
    @Test
    public void testGetAssignedGroupsForTAWhenTwoAssigned() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Group group1 = _dataServices.getGroup(asgnA, student1);
        Group group2 = _dataServices.getGroup(asgnA, student2);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());

        _dataServices.setGrader(part, group1, ta);
        _dataServices.setGrader(part, group2, ta);
        
        // check that the list only has 1 group1 in it and it is the correct group1
        assertEquals(2, _dataServices.getAssignedGroups(part, ta).size());
        assertTrue(_dataServices.getAssignedGroups(part, ta).contains(group1));
        assertTrue(_dataServices.getAssignedGroups(part, ta).contains(group2));
    }
    
    @Test
    public void testGetAssignedGroupsStartsEmpty() throws ServicesException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        assertEquals(0, _dataServices.getAssignedGroups(partA).size());
    }
    
    @Test
    public void testGetAssignedGroupsWhenOnlyAssignedToOneTA() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        events.add(eventA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());

        _dataServices.setGrader(part, group, ta);
        
        // check that the list only has 1 group in it and it is the correct group
        assertEquals(1, _dataServices.getAssignedGroups(part).size());
        assertTrue(_dataServices.getAssignedGroups(part).contains(group));
    }
    
    @Test
    public void testGetAssignedGroupsWhenAssignedToMultipleTAs() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Group group1 = _dataServices.getGroup(asgnA, student1);
        Group group2 = _dataServices.getGroup(asgnA, student2);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        TA ta2 = _dataServices.getTA(_dbTA2.getId());

        _dataServices.setGrader(part, group1, ta1);
        _dataServices.setGrader(part, group2, ta2);
        
        // check that the list only has 1 group1 in it and it is the correct group1
        assertEquals(2, _dataServices.getAssignedGroups(part).size());
        assertTrue(_dataServices.getAssignedGroups(part).contains(group1));
        assertTrue(_dataServices.getAssignedGroups(part).contains(group2));
    }
    
    @Test
    public void testGetPartsWithAssignedGroupsStartsEmpty() throws ServicesException {
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        
        assertEquals(0, _dataServices.getPartsWithAssignedGroups(ta1).size());
    }
    
    @Test
    public void testGetPartsWithAssignedGroupsWhenOnePartAssigned() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());
        
        
        _dataServices.setGrader(part, group, ta);
        
        assertEquals(1, _dataServices.getPartsWithAssignedGroups(ta).size());

        // make sure that it is the correct part1
        this.assertPartEqual(_dbPartA1, _dataServices.getPartsWithAssignedGroups(ta).iterator().next());
    }
    
    @Test
    public void testGetPartsWithAssignedGroupsWhenMultiplePartsAssigned() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA1 = createMock(Part.class);
        expect(partA1.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA1.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA1);
        
        Part partA2 = createMock(Part.class);
        expect(partA2.getName()).andReturn(_dbPartA2.getName()).anyTimes();
        expect(partA2.getId()).andReturn(_dbPartA2.getId()).anyTimes();
        parts.add(partA2);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA1);
        replay(partA2);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Group group1 = _dataServices.getGroup(asgnA, student1);
        Group group2 = _dataServices.getGroup(asgnA, student2);
        Part part1 = asgnA.getGradableEvents().get(0).getParts().get(0);
        Part part2 = asgnA.getGradableEvents().get(0).getParts().get(1);
        TA ta = _dataServices.getTA(_dbUserTA.getId());
        
        
        _dataServices.setGrader(part1, group1, ta);
        _dataServices.setGrader(part2, group2, ta);
        
        assertEquals(2, _dataServices.getPartsWithAssignedGroups(ta).size());

        // make sure that it is the correct part1
        ArrayList<DbPart> dbParts = new ArrayList<DbPart>();
        dbParts.add(_dbPartA1);
        dbParts.add(_dbPartA2);
        this.assertPartCollectionEqual(dbParts, _dataServices.getPartsWithAssignedGroups(ta));
    }
    
    @Test
    public void testGetDeadlineInfoForFixedDeadline() throws SQLException, ServicesException {
        DateTime onTime = new DateTime(2012, 2, 14, 5, 5, 5);
        DateTime early = new DateTime(2012, 2, 12, 5, 5, 5);
        DateTime late = new DateTime(2012, 2, 16, 5, 5, 5);
        
        double earlyPoints = 10.0;
        double latePoints = -5.0;
        
        DbGradableEvent event = createMock(DbGradableEvent.class);
        expect(event.getAssignmentId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(event.getDeadlineType()).andReturn(DeadlineInfo.Type.FIXED).anyTimes();
        expect(event.getEarlyDate()).andReturn(early).anyTimes();
        expect(event.getOnTimeDate()).andReturn(onTime).anyTimes();
        expect(event.getLateDate()).andReturn(late).anyTimes();
        expect(event.getLatePoints()).andReturn(latePoints).anyTimes();
        expect(event.getEarlyPoints()).andReturn(earlyPoints).anyTimes();
        expect(event.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(event.getId()).andReturn(1).anyTimes();
        expect(event.getOrder()).andReturn(1).anyTimes();
        expect(event.getDirectory()).andReturn(new File("")).anyTimes();
        expect(event.getLatePeriod()).andReturn(null).anyTimes();
        replay(event);
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(event.getName()).anyTimes();
        expect(eventA.getId()).andReturn(event.getId()).anyTimes();
        replay(eventA);
        
        _database.putGradableEvents(ImmutableSet.of(event));
        
        DeadlineInfo info = _dataServices.getDeadlineInfo(eventA);
        assertEquals(DeadlineInfo.Type.FIXED, info.getType()); 
        assertTrue(info.getEarlyDate().equals(early));
        assertTrue(info.getOnTimeDate().equals(onTime));
        assertTrue(info.getLateDate().equals(late));
        assertEquals(earlyPoints, info.getEarlyPoints(), 0.00001);
        assertEquals(latePoints, info.getLatePoints(), 0.00001);  
        assertNull(info.getLatePeriod());
    }
    
    @Test
    public void testGetDeadlineInfoForVariableDeadline() throws SQLException, ServicesException {
        DateTime onTime = new DateTime(2012, 2, 14, 5, 5, 5);
        DateTime late = new DateTime(2012, 2, 16, 5, 5, 5);
        
        Period latePeriod = Period.days(3);
        
        double earlyPoints = 10.0;
        double latePoints = -5.0;
        
        DbGradableEvent event = createMock(DbGradableEvent.class);
        expect(event.getAssignmentId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(event.getDeadlineType()).andReturn(DeadlineInfo.Type.VARIABLE).anyTimes();
        expect(event.getEarlyDate()).andReturn(null).anyTimes();
        expect(event.getOnTimeDate()).andReturn(onTime).anyTimes();
        expect(event.getLateDate()).andReturn(late).anyTimes();
        expect(event.getLatePoints()).andReturn(latePoints).anyTimes();
        expect(event.getEarlyPoints()).andReturn(null).anyTimes();
        expect(event.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(event.getId()).andReturn(1).anyTimes();
        expect(event.getOrder()).andReturn(1).anyTimes();
        expect(event.getDirectory()).andReturn(new File("")).anyTimes();
        expect(event.getLatePeriod()).andReturn(latePeriod).anyTimes();
        replay(event);
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(event.getName()).anyTimes();
        expect(eventA.getId()).andReturn(event.getId()).anyTimes();
        replay(eventA);
        
        _database.putGradableEvents(ImmutableSet.of(event));
        
        DeadlineInfo info = _dataServices.getDeadlineInfo(eventA);
        assertEquals(DeadlineInfo.Type.VARIABLE, info.getType()); 
        assertNull(info.getEarlyDate());
        assertTrue(info.getOnTimeDate().equals(onTime));
        assertTrue(info.getLateDate().equals(late));
        assertNull(info.getEarlyPoints());
        assertEquals(latePoints, info.getLatePoints(), 0.00001);  
        assertEquals(latePeriod, info.getLatePeriod());
    }
    
    @Test
    public void testGetDeadlineInfoForNoDeadline() throws ServicesException, SQLException {
        DbGradableEvent event = createMock(DbGradableEvent.class);
        expect(event.getAssignmentId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(event.getDeadlineType()).andReturn(DeadlineInfo.Type.NONE).anyTimes();
        expect(event.getEarlyDate()).andReturn(null).anyTimes();
        expect(event.getOnTimeDate()).andReturn(null).anyTimes();
        expect(event.getLateDate()).andReturn(null).anyTimes();
        expect(event.getLatePoints()).andReturn(null).anyTimes();
        expect(event.getEarlyPoints()).andReturn(null).anyTimes();
        expect(event.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(event.getId()).andReturn(1).anyTimes();
        expect(event.getOrder()).andReturn(1).anyTimes();
        expect(event.getDirectory()).andReturn(new File("")).anyTimes();
        expect(event.getLatePeriod()).andReturn(null).anyTimes();
        replay(event);
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(event.getName()).anyTimes();
        expect(eventA.getId()).andReturn(event.getId()).anyTimes();
        replay(eventA);
        
        _database.putGradableEvents(ImmutableSet.of(event));
        
        DeadlineInfo info = _dataServices.getDeadlineInfo(eventA);
        assertEquals(DeadlineInfo.Type.NONE, info.getType()); 
        assertNull(info.getEarlyDate());
        assertNull(info.getOnTimeDate());
        assertNull(info.getLateDate());
        assertNull(info.getEarlyPoints());
        assertNull(info.getLatePoints());  
        assertNull(info.getLatePeriod());
    }
    
    @Test
    public void testIsDistEmptyWhenEmpty() throws ServicesException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        assertTrue(_dataServices.isDistEmpty(eventA));
    }
    
    @Test
    public void testIsDistEmptyWhenNotEmpty() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        events.add(eventA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        replay(partA);
        
         DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());
        
        _dataServices.setGrader(part, group, ta);
        
        assertFalse(_dataServices.isDistEmpty(eventA));
    }
    
    @Test
    public void testGetDistributionInitiallyEmptyForTA() throws ServicesException, SQLException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
         DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));
        
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Group group = _dataServices.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataServices.getTA(_dbUserTA.getId());
        
        assertEquals(0, _dataServices.getDistribution(part).get(ta).size());
    }
    
    @Test
    public void testGetSetDistribution() throws SQLException, ServicesException {
        ArrayList<GradableEvent> events = new ArrayList<GradableEvent>();
        ArrayList<Part> parts = new ArrayList<Part>();

        Assignment asgnA = createMock(Assignment.class);
        expect(asgnA.getName()).andReturn(_dbAsgnA.getName()).anyTimes();
        expect(asgnA.getId()).andReturn(_dbAsgnA.getId()).anyTimes();
        expect(asgnA.hasGroups()).andReturn(_dbAsgnA.hasGroups()).anyTimes();
        
        GradableEvent eventA = createMock(GradableEvent.class);
        expect(eventA.getName()).andReturn(_dbGeA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_dbGeA.getId()).anyTimes();
        
        Part partA1 = createMock(Part.class);
        expect(partA1.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        expect(partA1.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        parts.add(partA1);
        
        Part partA2 = createMock(Part.class);
        expect(partA2.getName()).andReturn(_dbPartA2.getName()).anyTimes();
        expect(partA2.getId()).andReturn(_dbPartA2.getId()).anyTimes();
        parts.add(partA2);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA1);
        replay(partA2);
        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(asgnA, student2);
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Group group1 = _dataServices.getGroup(asgnA, student1);
        Group group2 = _dataServices.getGroup(asgnA, student2);
        Part part1 = asgnA.getGradableEvents().get(0).getParts().get(0);
        Part part2 = asgnA.getGradableEvents().get(0).getParts().get(1);
        TA ta1 = _dataServices.getTA(_dbUserTA.getId());
        TA ta2 = _dataServices.getTA(_dbTA2.getId());
        
        Map<Part, Map<TA, Set<Group>>> dist = new HashMap<Part, Map<TA, Set<Group>>>();
        Map<TA, Set<Group>> part1Map = new HashMap<TA, Set<Group>>();
        Map<TA, Set<Group>> part2Map = new HashMap<TA, Set<Group>>();
        
        HashSet<Group> both = new HashSet<Group>();
        both.add(group1);
        both.add(group2);
        
        HashSet<Group> first = new HashSet<Group>();
        first.add(group1);
        
        HashSet<Group> second = new HashSet<Group>();
        second.add(group2);
        
        part1Map.put(ta1, first);
        part1Map.put(ta2, second);
        part2Map.put(ta1, both);
        part2Map.put(ta2, new HashSet<Group>());
        
        dist.put(part1, part1Map);
        dist.put(part2, part2Map);
        
        _dataServices.setDistribution(dist);
        
        Map<TA, Set<Group>> result1 = _dataServices.getDistribution(part1);
        Map<TA, Set<Group>> result2 = _dataServices.getDistribution(part2);
        assertEquals(2, result1.size());
        assertEquals(2, result2.size());
        
        Set<Group> part1TA1Groups = result1.get(ta1);
        Set<Group> part1TA2Groups = result1.get(ta2);
        assertEquals(1, part1TA1Groups.size());
        assertEquals(1, part1TA2Groups.size());
        assertTrue(part1TA1Groups.contains(group1));
        assertTrue(part1TA2Groups.contains(group2));
        
        Collection<Group> part2TA1Groups = result2.get(ta1);
        Collection<Group> part2TA2Groups = result2.get(ta2);
        assertEquals(2, part2TA1Groups.size());
        assertEquals(0, part2TA2Groups.size());
        assertTrue(part2TA1Groups.contains(group1));
        assertTrue(part2TA1Groups.contains(group2));
    }

    @Test
    public void testGetGraderNoGrader() throws SQLException, ServicesException{
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));

        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));

        Part partA = createMock(Part.class);
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        replay(partA);

        Group group1 = createMock(Group.class);
        expect(group1.getId()).andReturn(dbGroup1.getId()).anyTimes();
        replay(group1);

        _dataServices.updateDataCache();

        TA ta = _dataServices.getGrader(partA, group1);

        assertNull(ta);
    }

    @Test
    public void testGetSetGrader() throws SQLException, ServicesException{
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2");
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));

        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));
        
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _database.putGroups(ImmutableSet.of(dbGroup2));

        Part partA = createMock(Part.class);
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        replay(partA);

        Group group1 = createMock(Group.class);
        expect(group1.getId()).andReturn(dbGroup1.getId()).anyTimes();
        replay(group1);

        TA ta = createMock(TA.class);
        expect(ta.getId()).andReturn(_dbUserTA.getId()).anyTimes();
        replay(ta);

        _dataServices.setGrader(partA, group1, ta);

        _dataServices.updateDataCache();

        TA returnedTA = _dataServices.getGrader(partA, group1);

        assertTAEqual(_dbUserTA, returnedTA);

        //make sure setting one grader doesn't effect others
        Group group2 = createMock(Group.class);
        expect(group2.getId()).andReturn(dbGroup2.getId()).anyTimes();
        replay(group2);
        
        TA returnedTANull = _dataServices.getGrader(partA, group2);

        assertNull(returnedTANull);
    }

    @Test
    public void testGetEarnedNoGrade() throws SQLException, ServicesException{
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));

        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));

        Part partA = createMock(Part.class);
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        replay(partA);

        Group group1 = createMock(Group.class);
        expect(group1.getId()).andReturn(dbGroup1.getId()).anyTimes();
        replay(group1);

         _dataServices.updateDataCache();

         PartGrade pg = _dataServices.getEarned(group1, partA);

         assertNull(pg);
    }

    @Test
    public void testGetSetEarned() throws SQLException, ServicesException{
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));

        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        _database.putGroups(ImmutableSet.of(dbGroup1));

        Part partA = createMock(Part.class);
        expect(partA.getId()).andReturn(_dbPartA1.getId()).anyTimes();
        expect(partA.getName()).andReturn(_dbPartA1.getName()).anyTimes();
        replay(partA);

        Group group1 = createMock(Group.class);
        expect(group1.getId()).andReturn(dbGroup1.getId()).anyTimes();
        expect(group1.getName()).andReturn(dbGroup1.getName()).anyTimes();
        replay(group1);

        TA ta = createMock(TA.class);
        expect(ta.getId()).andReturn(_dbUserTA.getId()).anyTimes();
        replay(ta);

        _dataServices.setGrader(partA, group1, ta);

        _dataServices.setEarned(group1, partA, _dbPartA1.getOutOf(), true);

         PartGrade pg = _dataServices.getEarned(group1, partA);

         assertNotNull(pg);
    }
    
    @Test
    public void testSetGetExtensions() throws ServicesException, SQLException
    {
        DbGroup dbGroup1 = new DbGroup(_asgnA, "The Group", ImmutableSet.<Student>of());
        DbGroup dbGroup2 = new DbGroup(_asgnA, "Another Group", ImmutableSet.<Student>of());
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Set<Group> groups = _dataServices.getGroups(_asgnA);
        
        DateTime newOnTime = new DateTime();
        String note = "Just because";
        
        _dataServices.setExtensions(_geA, groups, newOnTime, true, note);
        
        Map<Group, Extension> extensions = _dataServices.getExtensions(_geA, groups);
        assertSetsEqual(groups, extensions.keySet());
        for(Map.Entry<Group, Extension> entry : extensions.entrySet())
        {
            Group group = entry.getKey();
            Extension extension = entry.getValue();
            
            assertEquals(newOnTime, extension.getNewOnTime());
            assertEquals(note, extension.getNote());
            assertEquals(_geA.getId(), extension.getGradableEvent().getId());
            assertEquals(group, extension.getGroup());
            assertEquals((Integer) _dbUserTA.getId(), (Integer) extension.getTA().getId());
        }
    }
    
    @Test
    public void testSetDeleteGetExtensions() throws ServicesException, SQLException
    {
        DbGroup dbGroup1 = new DbGroup(_asgnA, "The Group", ImmutableSet.<Student>of());
        DbGroup dbGroup2 = new DbGroup(_asgnA, "Another Group", ImmutableSet.<Student>of());
        _database.putGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Set<Group> groups = _dataServices.getGroups(_asgnA);
        
        _dataServices.setExtensions(_geA, groups, new DateTime(), true, "Just because");
        
        _dataServices.deleteExtensions(_geA, groups);
        
        Map<Group, Extension> extensions = _dataServices.getExtensions(_geA, groups);
        assertTrue(extensions.isEmpty());
    }
    
    private <T> void assertSetsEqual(Set<T> set1, Set<T> set2) {
        assertEquals(set1.size(), set2.size());
        assertTrue(set1.containsAll(set2));
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