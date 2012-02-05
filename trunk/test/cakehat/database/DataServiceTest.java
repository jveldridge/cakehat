package cakehat.database;

import support.utils.UserUtilities;
import java.util.ArrayList;
import java.io.File;
import org.joda.time.DateTime;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
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
public class DataServiceTest {

    private DataServices _dataService;
    private Database _database;
    private DbTA _dbTA1, _dbTA2;
    private DbAssignment _dbAsgnA, _dbAsgnB;
    private DbGradableEvent _geA, _geB;
    private DbPart _partA1, _partA2, _partB1;
    private Assignment _asgnA, _asgnB;
    
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
        
        //set-up tas
        _dbTA1 = new DbTA(Allocator.getUserUtilities().getUserId(), "taLogin1", "taFirst1", "taLast1", true, false);
        int taId2 = 2;
        _dbTA2 = new DbTA(taId2, "taLogin2", "taFirst2", "taLast2", true, false);
        _database.putTAs(ImmutableSet.of(_dbTA1, _dbTA2));
        
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
    public void testSetHandinTimes() throws SQLException, ServicesException, CakeHatDBIOException {
        DbStudent student1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent student2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2");
        DbStudent student3 = new DbStudent("sLogin3", "sFirst3", "sLast3", "sEmail3");
        DbStudent student4 = new DbStudent("sLogin4", "sFirst4", "sLast4", "sEmail4");
        _database.putStudents(ImmutableSet.of(student1, student2, student3, student4));

        DbGroup dbGroup1 = new DbGroup(_asgnA, new Student(student1));
        DbGroup dbGroup2 = new DbGroup(_asgnA, new Student(student2));
        dbGroup2.addMember(new Student(student3));
        DbGroup dbGroup3 = new DbGroup(_asgnA, new Student(student4));
        _database.addGroups(ImmutableSet.of(dbGroup1, dbGroup2, dbGroup3));

        DateTime handinTime = new DateTime();

        Map<Group, DateTime> handinTimes = new HashMap<Group, DateTime>();

        //makes groups for handinTimes map
        Set<Student> studentSet = new HashSet<Student>();
        studentSet.add(new Student(student1));
        Group g = new Group(dbGroup1.getId(), _asgnA, dbGroup1.getName(), studentSet);
        handinTimes.put(g, handinTime);

        studentSet = new HashSet<Student>();
        studentSet.add(new Student(student2));
        studentSet.add(new Student(student3));
        g = new Group(dbGroup2.getId(), _asgnA, dbGroup1.getName(), studentSet);
        handinTimes.put(g, handinTime);

        //gets only gradableEvent from DB
        GradableEvent ge = _dataService.getAssignments().get(0).getGradableEvents().get(0);

        _dataService.setGradableEventOccurrences(ge, handinTimes);

        GradableEventOccurrenceRecord hr = _database.getGradableEventOccurrence(ge.getId(), dbGroup1.getId());
        assertHandinRecordEqual(hr, dbGroup1.getId(), ge.getId(), Allocator.getUserUtilities().getUserId(), handinTime.toString());

        hr = _database.getGradableEventOccurrence(ge.getId(), dbGroup2.getId());
        assertHandinRecordEqual(hr, dbGroup2.getId(), ge.getId(), Allocator.getUserUtilities().getUserId(), handinTime.toString());

        hr = _database.getGradableEventOccurrence(ge.getId(), dbGroup3.getId());
        assertNull(hr);


        //Check that changing only one handin time doesn't effect other handin times

        DateTime handinTime2 = new DateTime();
        handinTime2 = handinTime.minusHours(1); //make sure the two times are different by subtracting 1 hour

        handinTimes = new HashMap<Group, DateTime>();

        //makes groups for handinTimes map
        studentSet = new HashSet<Student>();
        studentSet.add(new Student(student1));
        g = new Group(dbGroup1.getId(), _asgnA, dbGroup1.getName(), studentSet);

        //add only group1 to the handinTimes
        handinTimes.put(g, handinTime2);

        _dataService.setGradableEventOccurrences(ge, handinTimes);

        hr = _database.getGradableEventOccurrence(ge.getId(), dbGroup1.getId());
        assertHandinRecordEqual(hr, dbGroup1.getId(), ge.getId(), Allocator.getUserUtilities().getUserId(), handinTime2.toString());

        hr = _database.getGradableEventOccurrence(ge.getId(), dbGroup2.getId());
        assertHandinRecordEqual(hr, dbGroup2.getId(), ge.getId(), Allocator.getUserUtilities().getUserId(), handinTime.toString());
    }

    @Test
    public void testSetHandinTime() throws SQLException, ServicesException, CakeHatDBIOException {
        DbStudent student1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent student2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2");
        DbStudent student3 = new DbStudent("sLogin3", "sFirst3", "sLast3", "sEmail3");
        _database.putStudents(ImmutableSet.of(student1, student2, student3));

        DbGroup dbGroup1 = new DbGroup(_asgnA, new Student(student1));
        DbGroup dbGroup2 = new DbGroup(_asgnA, new Student(student2));
        dbGroup2.addMember(new Student(student3));
        _database.addGroups(ImmutableSet.of(dbGroup1, dbGroup2));

        DateTime handinTime = new DateTime();

        //makes groups for handinTimes map
        Set<Student> studentSet = new HashSet<Student>();
        studentSet.add(new Student(student1));
        Group g = new Group(dbGroup1.getId(), _asgnA, dbGroup1.getName(), studentSet);

        //gets only gradableEvent from DB
        GradableEvent ge = _dataService.getAssignments().get(0).getGradableEvents().get(0);

        _dataService.setGradableEventOccurrence(ge, g, handinTime);

        GradableEventOccurrenceRecord hr = _database.getGradableEventOccurrence(ge.getId(), dbGroup1.getId());
        assertHandinRecordEqual(hr, dbGroup1.getId(), ge.getId(), Allocator.getUserUtilities().getUserId(), handinTime.toString());

        hr = _database.getGradableEventOccurrence(ge.getId(), dbGroup2.getId());
        assertNull(hr);


        //Check that changing only one handin time doesn't effect other handin times

        DateTime handinTime2 = new DateTime();
        handinTime2 = handinTime.minusHours(1); //make sure the two times are different by subtracting 1 hour

        //makes groups for handinTimes map
        studentSet = new HashSet<Student>();
        studentSet.add(new Student(student1));
        g = new Group(dbGroup1.getId(), _asgnA, dbGroup1.getName(), studentSet);

        _dataService.setGradableEventOccurrence(ge, g, handinTime2);

        hr = _database.getGradableEventOccurrence(ge.getId(), dbGroup1.getId());
        assertHandinRecordEqual(hr, dbGroup1.getId(), ge.getId(), Allocator.getUserUtilities().getUserId(), handinTime2.toString());
    }

    @Test
    public void testGetStudentsFromEmptyDatabase() throws SQLException, ServicesException {
        Collection<Student> students = _dataService.getStudents();
        assertEquals(0, students.size());
    }
    
    @Test
    public void testGet1Students() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        _database.putStudents(ImmutableSet.of(dbstudent1));
        _dataService.updateDataCache();
        Collection<Student> students = _dataService.getStudents();
        this.assertDbStudentCollectionEqual(ImmutableSet.of(dbstudent1), students);
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
        _database.putStudents(ImmutableSet.of(dbstudent1));
        _dataService.updateDataCache();
        
        assertTrue(_dataService.isStudentLoginInDatabase(dbstudent1.getLogin()));
        assertFalse(_dataService.isStudentLoginInDatabase("imaginary login"));
    }
    
    @Test
    public void testGetStudentFromLogin() throws SQLException, ServicesException {
        DbStudent dbstudent1 = new DbStudent("login1", "first1", "last1", "email1");
        _database.putStudents(ImmutableSet.of(dbstudent1));
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
        
        _dataService.blacklistStudents(ImmutableSet.of(student1), _dataService.getTA(_dbTA1.getId()));
        Collection<Student> blacklistedStuds = _dataService.getBlacklistedStudents();
        this.assertStudentCollectionEqual(ImmutableSet.of(student1), blacklistedStuds);
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
        
        _dataService.blacklistStudents(ImmutableSet.of(student1), _dataService.getTA(_dbTA1.getId()));        
        _dataService.blacklistStudents(ImmutableSet.of(student2), _dataService.getTA(_dbTA2.getId()));
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
        
        _dataService.blacklistStudents(ImmutableSet.of(student1), ta1);
        _dataService.blacklistStudents(ImmutableSet.of(student2), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1), _dataService.getBlacklist(ta1));
        this.assertStudentCollectionEqual(ImmutableSet.of(student2), _dataService.getBlacklist(ta2));
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
        _dataService.unBlacklistStudents(ImmutableSet.of(student2), ta2);
        
        this.assertStudentCollectionEqual(ImmutableSet.of(student1, student2), _dataService.getBlacklist(ta1));
        this.assertStudentCollectionEqual(ImmutableSet.of(student3), _dataService.getBlacklist(ta2));
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
        this.assertStudentCollectionEqual(ImmutableSet.of(student3), _dataService.getBlacklist(ta2));
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
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));  
        _dataService.updateDataCache();
        
        //an assignment has group, but without any group created should return an empty set
        Set<Group> groups = _dataService.getGroups(_asgnA);
        assertEquals(0, groups.size());            
    }
    
    @Test
    public void testGetGroupAutoGroupCreation() throws SQLException, ServicesException {        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
        
        //an assignment has no group, groups of one will be created and returned
        Set<Group> groups = _dataService.getGroups(_asgnB);
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
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        _dataService.addGroup(dbGroup1);

        Set<Group> groups = _dataService.getGroups(_asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1), groups);
    }
    
    @Test
    public void testGet2Groups() throws SQLException, ServicesException {
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _dataService.addGroup(dbGroup1);
        _dataService.addGroup(dbGroup2);
        
        Set<Group> groups = _dataService.getGroups(_asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1, dbGroup2), groups);
    }
    
    @Test
    public void testAddGet2Groups() throws SQLException, ServicesException {
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();

            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _dataService.addGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        
        Set<Group> groups = _dataService.getGroups(_asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1, dbGroup2), groups);
    }
    
    @Test
    public void testGetGroup() throws SQLException, ServicesException {
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _dataService.addGroup(dbGroup1);
        
        //group doesn't exist
        assertNull(_dataService.getGroup(_asgnA, student2));

        this.assertGroupEqual(dbGroup1, _dataService.getGroup(_asgnA, student1));
    }
    
    @Test
    public void testRemoveGroups() throws SQLException, ServicesException {
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _dataService.addGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        _dataService.removeGroups(_asgnA);
        assertEquals(0, _dataService.getGroups(_asgnA).size());
    }
    
    @Test
    public void testAddAfterRemoveGroups() throws SQLException, ServicesException {        
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2"); 
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));
        _dataService.updateDataCache();
            
        Student student1 = new Student(dbStudent1);
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _dataService.addGroups(ImmutableSet.of(dbGroup1));
        _dataService.addGroups(ImmutableSet.of(dbGroup2));
        _dataService.removeGroups(_asgnA);        
        _dataService.addGroups(ImmutableSet.of(dbGroup1, dbGroup2));
        Set<Group> groups = _dataService.getGroups(_asgnA);
        this.assertGroupCollectionEqual(ImmutableSet.of(dbGroup1, dbGroup2), groups);
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());
        
        
        _dataService.setGrader(part, group, ta);
        
        assertEquals(1, _dataService.getAssignedGroups(part, ta).size());
        assertTrue(_dataService.getAssignedGroups(part, ta).contains(group));
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        TA ta2 = _dataService.getTA(_dbTA2.getId());
        
        // first assign group1 to ta1
        _dataService.setGrader(part, group, ta1);
        
        assertEquals(1, _dataService.getAssignedGroups(part, ta1).size());
        assertTrue(_dataService.getAssignedGroups(part, ta1).contains(group));
        
        // reassign group1 to ta2
        _dataService.setGrader(part, group, ta2);
        
        // check that group1 is now assigned to ta2 and not ta1
        assertEquals(0, _dataService.getAssignedGroups(part, ta1).size());
        assertFalse(_dataService.getAssignedGroups(part, ta1).contains(group));
        
        assertEquals(1, _dataService.getAssignedGroups(part, ta2).size());
        assertTrue(_dataService.getAssignedGroups(part, ta2).contains(group));
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());

        _dataService.setGrader(part, group, ta);
        
        assertEquals(1, _dataService.getAssignedGroups(part, ta).size());
        assertTrue(_dataService.getAssignedGroups(part, ta).contains(group));
        
        // this shouldn't throw an exception or do anything 
        _dataService.setGrader(part, group, ta);
        
        assertEquals(1, _dataService.getAssignedGroups(part, ta).size());
        assertTrue(_dataService.getAssignedGroups(part, ta).contains(group));
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());

        _dataService.setGrader(part, group, ta);
        
        assertEquals(1, _dataService.getAssignedGroups(part, ta).size());
        assertTrue(_dataService.getAssignedGroups(part, ta).contains(group));
        
        // unassign the group1 and check that the group1 is no longer assigned to the ta1
        _dataService.setGrader(part, group, null);
        
        assertEquals(0, _dataService.getAssignedGroups(part, ta).size());
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());

        // group1 has not been assigned
        
        assertEquals(0, _dataService.getAssignedGroups(part, ta).size());
        
        // unassign the group1 and check that nothing happens
        _dataService.setGrader(part, group, null);
        
        assertEquals(0, _dataService.getAssignedGroups(part, ta).size());
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
        parts.add(partA);
        events.add(eventA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        replay(partA);
        
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());
        
        // make sure that getAssignedGroups returns an empty collection
        assertEquals(0, _dataService.getAssignedGroups(part, ta).size());
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());

        _dataService.setGrader(part, group, ta);
        
        // check that the list only has 1 group1 in it and it is the correct group1
        assertEquals(1, _dataService.getAssignedGroups(part, ta).size());
        assertTrue(_dataService.getAssignedGroups(part, ta).contains(group));
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        _dataService.addGroup(dbGroup2);
        
        Group group1 = _dataService.getGroup(asgnA, student1);
        Group group2 = _dataService.getGroup(asgnA, student2);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());

        _dataService.setGrader(part, group1, ta);
        _dataService.setGrader(part, group2, ta);
        
        // check that the list only has 1 group1 in it and it is the correct group1
        assertEquals(2, _dataService.getAssignedGroups(part, ta).size());
        assertTrue(_dataService.getAssignedGroups(part, ta).contains(group1));
        assertTrue(_dataService.getAssignedGroups(part, ta).contains(group2));
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        assertEquals(0, _dataService.getAssignedGroups(partA).size());
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());

        _dataService.setGrader(part, group, ta);
        
        // check that the list only has 1 group in it and it is the correct group
        assertEquals(1, _dataService.getAssignedGroups(part).size());
        assertTrue(_dataService.getAssignedGroups(part).contains(group));
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        _dataService.addGroup(dbGroup2);
        
        Group group1 = _dataService.getGroup(asgnA, student1);
        Group group2 = _dataService.getGroup(asgnA, student2);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        TA ta2 = _dataService.getTA(_dbTA2.getId());

        _dataService.setGrader(part, group1, ta1);
        _dataService.setGrader(part, group2, ta2);
        
        // check that the list only has 1 group1 in it and it is the correct group1
        assertEquals(2, _dataService.getAssignedGroups(part).size());
        assertTrue(_dataService.getAssignedGroups(part).contains(group1));
        assertTrue(_dataService.getAssignedGroups(part).contains(group2));
    }
    
    @Test
    public void testGetPartsWithAssignedGroupsStartsEmpty() throws ServicesException {
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        
        assertEquals(0, _dataService.getPartsWithAssignedGroups(ta1).size());
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());
        
        
        _dataService.setGrader(part, group, ta);
        
        assertEquals(1, _dataService.getPartsWithAssignedGroups(ta).size());

        // make sure that it is the correct part1
        this.assertPartEqual(_partA1, _dataService.getPartsWithAssignedGroups(ta).iterator().next());
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA1 = createMock(Part.class);
        expect(partA1.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA1.getId()).andReturn(_partA1.getId()).anyTimes();
        parts.add(partA1);
        
        Part partA2 = createMock(Part.class);
        expect(partA2.getName()).andReturn(_partA2.getName()).anyTimes();
        expect(partA2.getId()).andReturn(_partA2.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        _dataService.addGroup(dbGroup2);
        
        Group group1 = _dataService.getGroup(asgnA, student1);
        Group group2 = _dataService.getGroup(asgnA, student2);
        Part part1 = asgnA.getGradableEvents().get(0).getParts().get(0);
        Part part2 = asgnA.getGradableEvents().get(0).getParts().get(1);
        TA ta = _dataService.getTA(_dbTA1.getId());
        
        
        _dataService.setGrader(part1, group1, ta);
        _dataService.setGrader(part2, group2, ta);
        
        assertEquals(2, _dataService.getPartsWithAssignedGroups(ta).size());

        // make sure that it is the correct part1
        ArrayList<DbPart> dbParts = new ArrayList<DbPart>();
        dbParts.add(_partA1);
        dbParts.add(_partA2);
        this.assertPartCollectionEqual(dbParts, _dataService.getPartsWithAssignedGroups(ta));
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
        expect(event.getName()).andReturn(_geA.getName()).anyTimes();
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
        
        DeadlineInfo info = _dataService.getDeadlineInfo(eventA);
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
        
        DeadlineInfo info = _dataService.getDeadlineInfo(eventA);
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
        expect(event.getName()).andReturn(_geA.getName()).anyTimes();
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
        
        DeadlineInfo info = _dataService.getDeadlineInfo(eventA);
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
        parts.add(partA);
        
        expect(asgnA.getGradableEvents()).andReturn(events).anyTimes();
        expect(eventA.getParts()).andReturn(parts).anyTimes();
        
        replay(asgnA);
        replay(eventA);
        events.add(eventA);
        replay(partA);
        
        assertTrue(_dataService.isDistEmpty(eventA));
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());
        
        _dataService.setGrader(part, group, ta);
        
        assertFalse(_dataService.isDistEmpty(eventA));
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA = createMock(Part.class);
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        
        Group group = _dataService.getGroup(asgnA, student1);
        Part part = asgnA.getGradableEvents().get(0).getParts().get(0);
        TA ta = _dataService.getTA(_dbTA1.getId());
        
        assertEquals(0, _dataService.getDistribution(part).get(ta).size());
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
        expect(eventA.getName()).andReturn(_geA.getName()).anyTimes();
        expect(eventA.getId()).andReturn(_geA.getId()).anyTimes();
        
        Part partA1 = createMock(Part.class);
        expect(partA1.getName()).andReturn(_partA1.getName()).anyTimes();
        expect(partA1.getId()).andReturn(_partA1.getId()).anyTimes();
        parts.add(partA1);
        
        Part partA2 = createMock(Part.class);
        expect(partA2.getName()).andReturn(_partA2.getName()).anyTimes();
        expect(partA2.getId()).andReturn(_partA2.getId()).anyTimes();
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
        _dataService.addGroup(dbGroup1);
        _dataService.addGroup(dbGroup2);
        
        Group group1 = _dataService.getGroup(asgnA, student1);
        Group group2 = _dataService.getGroup(asgnA, student2);
        Part part1 = asgnA.getGradableEvents().get(0).getParts().get(0);
        Part part2 = asgnA.getGradableEvents().get(0).getParts().get(1);
        TA ta1 = _dataService.getTA(_dbTA1.getId());
        TA ta2 = _dataService.getTA(_dbTA2.getId());
        
        Map<Part, Map<TA, Collection<Group>>> dist = new HashMap<Part, Map<TA, Collection<Group>>>();
        Map<TA, Collection<Group>> part1Map = new HashMap<TA, Collection<Group>>();
        Map<TA, Collection<Group>> part2Map = new HashMap<TA, Collection<Group>>();
        
        ArrayList<Group> both = new ArrayList<Group>();
        both.add(group1);
        both.add(group2);
        
        ArrayList<Group> first = new ArrayList<Group>();
        first.add(group1);
        
        ArrayList<Group> second = new ArrayList<Group>();
        second.add(group2);
        
        part1Map.put(ta1, first);
        part1Map.put(ta2, second);
        part2Map.put(ta1, both);
        part2Map.put(ta2, new ArrayList<Group>());
        
        dist.put(part1, part1Map);
        dist.put(part2, part2Map);
        
        _dataService.setDistribution(dist);
        
        Map<TA, Collection<Group>> result1 = _dataService.getDistribution(part1);
        Map<TA, Collection<Group>> result2 = _dataService.getDistribution(part2);
        assertEquals(2, result1.size());
        assertEquals(2, result2.size());
        
        Collection<Group> part1TA1Groups = result1.get(ta1);
        Collection<Group> part1TA2Groups = result1.get(ta2);
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
        _dataService.addGroup(dbGroup1);

        Part partA = createMock(Part.class);
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
        replay(partA);

        Group group1 = createMock(Group.class);
        expect(group1.getId()).andReturn(dbGroup1.getId()).anyTimes();
        replay(group1);

        _dataService.updateDataCache();

        TA ta = _dataService.getGrader(partA, group1);

        assertNull(ta);
    }

    @Test
    public void testGetSetGrader() throws SQLException, ServicesException{
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        DbStudent dbStudent2 = new DbStudent("sLogin2", "sFirst2", "sLast2", "sEmail2");
        _database.putStudents(ImmutableSet.of(dbStudent1, dbStudent2));

        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        _dataService.addGroup(dbGroup1);
        
        Student student2 = new Student(dbStudent2);
        DbGroup dbGroup2 = new DbGroup(_asgnA, student2);
        _dataService.addGroup(dbGroup2);

        Part partA = createMock(Part.class);
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
        replay(partA);

        Group group1 = createMock(Group.class);
        expect(group1.getId()).andReturn(dbGroup1.getId()).anyTimes();
        replay(group1);

        TA ta = createMock(TA.class);
        expect(ta.getId()).andReturn(_dbTA1.getId()).anyTimes();
        replay(ta);

        _dataService.setGrader(partA, group1, ta);

        _dataService.updateDataCache();

        TA returnedTA = _dataService.getGrader(partA, group1);

        assertTAEqual(_dbTA1, returnedTA);

        //make sure setting one grader doesn't effect others
        Group group2 = createMock(Group.class);
        expect(group2.getId()).andReturn(dbGroup2.getId()).anyTimes();
        replay(group2);
        
        TA returnedTANull = _dataService.getGrader(partA, group2);

        assertNull(returnedTANull);
    }

    @Test
    public void testGetEarnedNoGrade() throws SQLException, ServicesException{
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));

        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        _dataService.addGroup(dbGroup1);

        Part partA = createMock(Part.class);
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
        replay(partA);

        Group group1 = createMock(Group.class);
        expect(group1.getId()).andReturn(dbGroup1.getId()).anyTimes();
        replay(group1);

         _dataService.updateDataCache();

         PartGrade pg = _dataService.getEarned(group1, partA);

         assertNull(pg);
    }

    @Test
    public void testGetSetEarned() throws SQLException, ServicesException{
        DbStudent dbStudent1 = new DbStudent("sLogin1", "sFirst1", "sLast1", "sEmail1");
        _database.putStudents(ImmutableSet.of(dbStudent1));

        Student student1 = new Student(dbStudent1);
        DbGroup dbGroup1 = new DbGroup(_asgnA, student1);
        _dataService.addGroup(dbGroup1);

        Part partA = createMock(Part.class);
        expect(partA.getId()).andReturn(_partA1.getId()).anyTimes();
        expect(partA.getName()).andReturn(_partA1.getName()).anyTimes();
        replay(partA);

        Group group1 = createMock(Group.class);
        expect(group1.getId()).andReturn(dbGroup1.getId()).anyTimes();
        expect(group1.getName()).andReturn(dbGroup1.getName()).anyTimes();
        replay(group1);

        TA ta = createMock(TA.class);
        expect(ta.getId()).andReturn(_dbTA1.getId()).anyTimes();
        replay(ta);

        _dataService.setGrader(partA, group1, ta);

        _dataService.setEarned(group1, partA, _partA1.getOutOf(), true);

         PartGrade pg = _dataService.getEarned(group1, partA);

         assertNotNull(pg);
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
    
    private void assertHandinRecordEqual(GradableEventOccurrenceRecord hr1, int agID, int geID, int taID, String time) {
        assertEquals(hr1.getAsgnGroupId(), agID);
        assertEquals(hr1.getGradeableEventId(), geID);
        assertEquals(hr1.getTaId(), taID);
        assertEquals(0, hr1.getTime().toString().compareTo(time));
    }
}
