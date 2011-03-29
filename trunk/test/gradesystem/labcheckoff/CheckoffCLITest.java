package gradesystem.labcheckoff;

import gradesystem.Allocator;
import gradesystem.Allocator.SingletonAllocation;
import gradesystem.config.Assignment;
import gradesystem.config.ConfigurationException;
import gradesystem.config.LabConfigurationParser;
import gradesystem.config.LabPart;
import gradesystem.database.DatabaseIO;
import gradesystem.database.Group;
import gradesystem.labcheckoff.CheckoffCLI.CheckoffException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Basic tests for {@link CheckoffCLI} to verify that check off works when
 * supplied with the correct arguments. As of now, there are no JUnit tests for
 * improper input.
 *
 * @author jak2
 */
public class CheckoffCLITest
{
    //Tests that if a score already exists and overwrite permission is not
    //given, the score will not be overwritten
    @Test
    public void testAbortOverwrite() throws ConfigurationException, SQLException, CheckoffException
    {
        final int labNumber = 1;
        final String studentLogin = "jak2";
        final double pointsGiven = 12;
        final Group group = new Group(studentLogin, studentLogin);

        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(false).atLeastOnce();
        replay(asgn);

        LabPart lab = createMock(LabPart.class);
        expect(lab.getLabNumber()).andReturn(1).anyTimes();
        expect(lab.getAssignment()).andReturn(asgn).atLeastOnce();
        expect(lab.getPoints()).andReturn(15).atLeastOnce();
        replay(lab);

        LabConfigurationParser parser = createMock(LabConfigurationParser.class);
        expect(parser.getLabPart(1)).andReturn(lab);
        replay(parser);

        final DatabaseIO dbio = createMock(DatabaseIO.class);
        Map<String, String> allStudents = new HashMap<String, String>();
        allStudents.put(studentLogin, "Joshua Kaplan");
        expect(dbio.getAllStudents()).andReturn(allStudents);
        expect(dbio.isStudentEnabled(studentLogin)).andReturn(true);
        expect(dbio.getStudentsGroup(asgn, studentLogin)).andReturn(group);
        expect(dbio.getGroupScore(eq(group), eq(lab))).andReturn(11.2);
        replay(dbio);

        SingletonAllocation<DatabaseIO> dbioAlloc =
            new SingletonAllocation<DatabaseIO>()
            {
                public DatabaseIO allocate() { return dbio; };
            };
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();


        CheckoffCLI.CheckoffInteractor interactor = new CheckoffCLI.CheckoffInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };

        List<String> args = Arrays.asList(new String[] { Integer.toString(labNumber),
            studentLogin, Double.toString(pointsGiven) });

        CheckoffCLI.CheckoffResult result = CheckoffCLI.performCheckoff(args, interactor, parser);
        assertEquals(CheckoffCLI.CheckoffResult.ABORTED, result);

        verify(asgn);
        verify(lab);
        verify(parser);
        verify(dbio);
    }

    //Tests checking off a student that is part of a group of multiple students
    @Test
    public void testGroupCheckoff() throws ConfigurationException, SQLException, CheckoffException
    {
        final int labNumber = 1;
        final String studentLogin = "jak2";
        final double pointsGiven = 12;
        final Group group = new Group("test_group", studentLogin, "jeldridg", "aunger", "hdrosen");

        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(true).atLeastOnce();
        replay(asgn);

        LabPart lab = createMock(LabPart.class);
        expect(lab.getLabNumber()).andReturn(1).anyTimes();
        expect(lab.getAssignment()).andReturn(asgn).atLeastOnce();
        expect(lab.getPoints()).andReturn(15).atLeastOnce();
        replay(lab);

        LabConfigurationParser parser = createMock(LabConfigurationParser.class);
        expect(parser.getLabPart(1)).andReturn(lab);
        replay(parser);

        final DatabaseIO dbio = createMock(DatabaseIO.class);
        Map<String, String> allStudents = new HashMap<String, String>();
        allStudents.put(studentLogin, "Joshua Kaplan");
        expect(dbio.getAllStudents()).andReturn(allStudents);
        expect(dbio.isStudentEnabled(studentLogin)).andReturn(true);
        expect(dbio.getStudentsGroup(asgn, studentLogin)).andReturn(group);
        expect(dbio.getGroupScore(eq(group), eq(lab))).andReturn(null);
        dbio.enterGrade(group, lab, pointsGiven);
        expectLastCall();
        replay(dbio);

        SingletonAllocation<DatabaseIO> dbioAlloc =
            new SingletonAllocation<DatabaseIO>()
            {
                public DatabaseIO allocate() { return dbio; };
            };
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();

    
        CheckoffCLI.CheckoffInteractor interactor = new CheckoffCLI.CheckoffInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };

        List<String> args = Arrays.asList(new String[] { Integer.toString(labNumber),
            studentLogin, Double.toString(pointsGiven) });

        CheckoffCLI.CheckoffResult result = CheckoffCLI.performCheckoff(args, interactor, parser);
        assertEquals(CheckoffCLI.CheckoffResult.SUCCEEDED, result);

        verify(asgn);
        verify(lab);
        verify(parser);
        verify(dbio);
    }

    //Tests checking off an individual that belongs to a group of one that is
    //already in the database
    @Test
    public void testIndividualCheckoff_ExistingGroup() throws ConfigurationException, SQLException, CheckoffException
    {
        final int labNumber = 1;
        final String studentLogin = "jak2";
        final double pointsGiven = 12;
        final Group group = new Group(studentLogin, studentLogin);

        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(false).atLeastOnce();
        replay(asgn);

        LabPart lab = createMock(LabPart.class);
        expect(lab.getLabNumber()).andReturn(1).anyTimes();
        expect(lab.getAssignment()).andReturn(asgn).atLeastOnce();
        expect(lab.getPoints()).andReturn(15).atLeastOnce();
        replay(lab);

        LabConfigurationParser parser = createMock(LabConfigurationParser.class);
        expect(parser.getLabPart(1)).andReturn(lab);
        replay(parser);

        final DatabaseIO dbio = createMock(DatabaseIO.class);
        Map<String, String> allStudents = new HashMap<String, String>();
        allStudents.put(studentLogin, "Joshua Kaplan");
        expect(dbio.getAllStudents()).andReturn(allStudents);
        expect(dbio.isStudentEnabled(studentLogin)).andReturn(true);
        expect(dbio.getStudentsGroup(asgn, studentLogin)).andReturn(group);
        expect(dbio.getGroupScore(eq(group), eq(lab))).andReturn(null);
        dbio.enterGrade(group, lab, pointsGiven);
        expectLastCall();
        replay(dbio);

        SingletonAllocation<DatabaseIO> dbioAlloc =
            new SingletonAllocation<DatabaseIO>()
            {
                public DatabaseIO allocate() { return dbio; };
            };
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();


        CheckoffCLI.CheckoffInteractor interactor = new CheckoffCLI.CheckoffInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };

        List<String> args = Arrays.asList(new String[] { Integer.toString(labNumber),
            studentLogin, Double.toString(pointsGiven) });
        
        CheckoffCLI.CheckoffResult result = CheckoffCLI.performCheckoff(args, interactor, parser);
        assertEquals(CheckoffCLI.CheckoffResult.SUCCEEDED, result);

        verify(asgn);
        verify(lab);
        verify(parser);
        verify(dbio);
    }

    @Test
    public void testIndividualCheckoff_NoExistingGroup() throws ConfigurationException, SQLException, CheckoffException
    {
        final int labNumber = 1;
        final String studentLogin = "jak2";
        final double pointsGiven = 12;
        final Group group = new Group(studentLogin, studentLogin)
        {
            @Override
            public boolean equals(Object obj)
            {
                if(obj instanceof Group)
                {
                    Group other = (Group) obj;

                    //If this was a real equality test it should check the
                    //members are the same, but for the purposes of this test
                    //this suffices
                    return this.getName().equals(other.getName());
                }

                return false;
            }
        };

        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(false).atLeastOnce();
        replay(asgn);

        LabPart lab = createMock(LabPart.class);
        expect(lab.getLabNumber()).andReturn(1).anyTimes();
        expect(lab.getAssignment()).andReturn(asgn).atLeastOnce();
        expect(lab.getPoints()).andReturn(15).atLeastOnce();
        replay(lab);

        LabConfigurationParser parser = createMock(LabConfigurationParser.class);
        expect(parser.getLabPart(1)).andReturn(lab);
        replay(parser);

        final DatabaseIO dbio = createMock(DatabaseIO.class);
        Map<String, String> allStudents = new HashMap<String, String>();
        allStudents.put(studentLogin, "Joshua Kaplan");
        expect(dbio.getAllStudents()).andReturn(allStudents);
        expect(dbio.isStudentEnabled(studentLogin)).andReturn(true);
        expect(dbio.getStudentsGroup(asgn, studentLogin)).andReturn(null);
        dbio.setGroup(eq(asgn), eq(group));
        expectLastCall();
        expect(dbio.getGroupScore(eq(group), eq(lab))).andReturn(null);
        dbio.enterGrade(group, lab, pointsGiven);
        expectLastCall();
        replay(dbio);

        SingletonAllocation<DatabaseIO> dbioAlloc =
            new SingletonAllocation<DatabaseIO>()
            {
                public DatabaseIO allocate() { return dbio; };
            };
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();


        CheckoffCLI.CheckoffInteractor interactor = new CheckoffCLI.CheckoffInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };

        List<String> args = Arrays.asList(new String[] { Integer.toString(labNumber),
            studentLogin, Double.toString(pointsGiven) });

        CheckoffCLI.CheckoffResult result = CheckoffCLI.performCheckoff(args, interactor, parser);
        assertEquals(CheckoffCLI.CheckoffResult.SUCCEEDED, result);

        verify(asgn);
        verify(lab);
        verify(parser);
        verify(dbio);
    }
}