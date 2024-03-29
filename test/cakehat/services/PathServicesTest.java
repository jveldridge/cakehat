package cakehat.services;

import cakehat.CakehatRunMode;
import cakehat.database.Student;
import cakehat.assignment.Action;
import org.joda.time.DateTime;
import cakehat.assignment.Part;
import cakehat.Allocator;
import cakehat.CakehatSession;
import cakehat.TestCakehatSessionProvider;
import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.database.Group;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * Tests the paths generated by {@link PathServicesImpl}.
 *
 * @author jak2
 */
public class PathServicesTest
{
    private static final String COURSE = "cs001";
    private static final int YEAR = new DateTime().getYear();

    private static final int ASSIGNMENT_ID = 13;
    private static final int GRADABLE_EVENT_ID = 27;
    private static final int PART_ID = 39;
    private static final int ACTION_ID = 53;
    private static final int TA_ID = 501;
    private static final int GROUP_ID = 42;
    private static final String STUDENT_LOGIN = "jak2";

    private Student _student;
    private Group _group;
    private Assignment _assignment;
    private GradableEvent _gradableEvent;
    private Part _part;
    private Action _action;
    private PathServices _service;
    
    private CakehatRunMode _runMode;

    @Before
    public void setup() throws ServicesException, IOException, SQLException
    {
        //Mock student
        _student = createMock(Student.class);
        expect(_student.getLogin()).andReturn(STUDENT_LOGIN).anyTimes();
        replay(_student);
        
        //Mock group
        _group = createMock(Group.class);
        expect(_group.getId()).andReturn(GROUP_ID).anyTimes();
        replay(_group);

        //Create mocked assignment, gradable event, part, and action objects
        _assignment = createMock(Assignment.class);
        expect(_assignment.getId()).andReturn(ASSIGNMENT_ID).anyTimes();
        
        _gradableEvent = createMock(GradableEvent.class);
        expect(_gradableEvent.getId()).andReturn(GRADABLE_EVENT_ID).anyTimes();
        expect(_gradableEvent.getAssignment()).andReturn(_assignment).anyTimes();
        
        _part = createMock(Part.class);
        expect(_part.getId()).andReturn(PART_ID).anyTimes();
        expect(_part.getGradableEvent()).andReturn(_gradableEvent).anyTimes();
        expect(_part.getAssignment()).andReturn(_assignment).anyTimes();

        _action = createMock(Action.class);
        expect(_action.getId()).andReturn(ACTION_ID).anyTimes();
        expect(_action.getPart()).andReturn(_part).anyTimes();
        
        replay(_assignment);
        replay(_gradableEvent);
        replay(_part);
        replay(_action);
        
        CakehatSession.CakehatSessionProvider provider = new TestCakehatSessionProvider(COURSE, TA_ID);
        _runMode = provider.getRunMode();
        CakehatSession.setSessionProviderForTesting(provider);

        _service = Allocator.getPathServices();
    }
    
    @Test
    public void testGetCourseDir()
    {
        File expected = new File("/course/" + COURSE);
        
        assertEquals(expected, _service.getCourseDir());
    }
    
    @Test
    public void testGetTABinDir()
    {
        File expected = new File("/course/" + COURSE + "/tabin/");
        
        assertEquals(expected, _service.getTABinDir());
    }
    
    @Test
    public void testGetCakehatDir()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/");
        
        assertEquals(expected, _service.getCakehatDir());
    }
    
    @Test
    public void testGetDatabaseFile()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/" + YEAR + "/database/database.db");

        assertEquals(expected, _service.getDatabaseFile());
    }

    @Test
    public void testGetDatabaseBackupDir()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/" + YEAR + "/database/backups");

        assertEquals(expected, _service.getDatabaseBackupDir());
    }
    
    @Test
    public void testGetTempDir()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/" + YEAR + "/temp/" + TA_ID + "-" + _runMode);
        
        assertEquals(expected, _service.getTempDir());
    }   
    
    @Test
    public void testGetActionTempDir()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/" + YEAR + "/temp/" + TA_ID + "-" + _runMode + "/" +
                ASSIGNMENT_ID + "/" + GRADABLE_EVENT_ID + "/" +  PART_ID + "/" + ACTION_ID);
        
        assertEquals(expected, _service.getActionTempDir(_action));
    }
    
    @Test
    public void testGetActionTempDir_GroupNotNull()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/" + YEAR + "/temp/" + TA_ID + "-" + _runMode + "/" +
                ASSIGNMENT_ID + "/" + GRADABLE_EVENT_ID + "/" +  PART_ID + "/" + ACTION_ID + "/" + GROUP_ID);
        
        assertEquals(expected, _service.getActionTempDir(_action, _group));
    }
    
    @Test
    public void testGetActionTempDir_GroupNull()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/" + YEAR + "/temp/" + TA_ID + "-" + _runMode + "/" +
                ASSIGNMENT_ID + "/" + GRADABLE_EVENT_ID + "/" +  PART_ID + "/" + ACTION_ID + "/nogroup");
        
        assertEquals(expected, _service.getActionTempDir(_action, null));
    }
    
    @Test
    public void testGetUnarchiveHandinDir()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/" + YEAR + "/handin/" + ASSIGNMENT_ID + "/" +
                GRADABLE_EVENT_ID + "/" + PART_ID + "/" + GROUP_ID);

        assertEquals(expected, _service.getUnarchiveHandinDir(_part, _group));
    }
    
    @Test
    public void testGroupGMLFile()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/" + YEAR + "/gml/" + ASSIGNMENT_ID + "/" +
                GRADABLE_EVENT_ID + "/" + PART_ID + "/" + GROUP_ID + ".gml");

        assertEquals(expected, _service.getGroupGMLFile(_part, _group));
    }
    
    @Test
    public void testGetStudentGRDFile()
    {
        File expected = new File("/course/" + COURSE + "/.cakehat/" + YEAR + "/temp/" + TA_ID + "-" + _runMode + "/" +
                ASSIGNMENT_ID + "/" + STUDENT_LOGIN + ".txt");
        
        assertEquals(expected, _service.getStudentGRDFile(_assignment, _student));
    }
}