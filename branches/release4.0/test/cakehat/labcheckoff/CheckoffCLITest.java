package cakehat.labcheckoff;

import cakehat.config.ConfigurationInfo;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.junit.Before;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.config.Assignment;
import cakehat.config.ConfigurationException;
import cakehat.config.LabConfigurationParser;
import cakehat.config.LabPart;
import cakehat.database.ConfigurationData;
import cakehat.database.DataServices;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.labcheckoff.CheckoffCLI.CheckoffException;
import cakehat.services.ServicesException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
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
    
    private final Assignment _testAsgn;
    
    public CheckoffCLITest() {
        _testAsgn = createMock(Assignment.class);
        final String name = "Amazing Assignment";
        expect(_testAsgn.getName()).andReturn(name).anyTimes();
        expect(_testAsgn.getDBID()).andReturn(name).anyTimes();
        expect(_testAsgn.hasGroups()).andReturn(true).atLeastOnce();
        replay(_testAsgn);
    }
    
    @Before
    public void setUp() throws IOException, SQLException {      
        final ConfigurationInfo ci = createMock(ConfigurationInfo.class);
        expect(ci.getAssignments()).andReturn(ImmutableList.of(_testAsgn)).anyTimes();
        expect(ci.getAssignment(_testAsgn.getDBID())).andReturn(_testAsgn).anyTimes();
        replay(ci);
        SingletonAllocation<ConfigurationInfo> ciAlloc =
            new SingletonAllocation<ConfigurationInfo>()
            {
                public ConfigurationInfo allocate() { return ci; };
            };
        
        new Allocator.Customizer().setConfigurationInfo(ciAlloc).customize(); 
    }
    
    //Tests that if a score already exists and overwrite permission is not
    //given, the score will not be overwritten
    @Test
    public void testAbortOverwrite() throws ConfigurationException, ServicesException, CheckoffException
    {
        final int labNumber = 1;
        final Student student = ConfigurationData.generateStudent(1, "jak2", "Joshua", "Kaplan", "jak2@cs.brown.edu", true);
        final Group group = ConfigurationData.generateGroup(1, _testAsgn, student.getName(), student); 
        final double pointsGiven = 12;
 
        LabPart lab = createMock(LabPart.class);
        expect(lab.getLabNumber()).andReturn(1).anyTimes();
        expect(lab.getAssignment()).andReturn(_testAsgn).atLeastOnce();
        expect(lab.getPoints()).andReturn(15).atLeastOnce();
        replay(lab);

        LabConfigurationParser parser = createMock(LabConfigurationParser.class);
        expect(parser.getLabPart(1)).andReturn(lab);
        replay(parser);

        final DataServices ds = createMock(DataServices.class);
        expect(ds.isStudentLoginInDatabase(student.getLogin())).andReturn(true);
        expect(ds.getStudentFromLogin(student.getLogin())).andReturn(student);
        expect(ds.getGroup(_testAsgn, student)).andReturn(group);
        expect(ds.getScore(eq(group), eq(lab))).andReturn(11.2);
        replay(ds);

        SingletonAllocation<DataServices> dsAlloc =
            new SingletonAllocation<DataServices>()
            {
                public DataServices allocate() { return ds; };
            };
        new Allocator.Customizer().setDataServices(dsAlloc).customize();


        CheckoffCLI.CheckoffInteractor interactor = new CheckoffCLI.CheckoffInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };

        List<String> args = Arrays.asList(new String[] { Integer.toString(labNumber),
            student.getLogin(), Double.toString(pointsGiven) });

        CheckoffCLI.CheckoffResult result = CheckoffCLI.performCheckoff(args, interactor, parser);
        assertEquals(CheckoffCLI.CheckoffResult.ABORTED, result);

        verify(_testAsgn);
        verify(lab);
        verify(parser);
    }

    //Tests checking off a student that is part of a group of multiple students
    @Test
    public void testGroupCheckoff() throws ConfigurationException, CheckoffException, ServicesException
    {
        final int labNumber = 1;
        final Student josh = ConfigurationData.generateStudent(1, "jak2", "Joshua", "Kaplan", "jak2@cs.brown.edu", true);
        final Student jonathan = ConfigurationData.generateStudent(2, "jeldridg", "Jonathan", "Eldridge", "jeldridg@cs.brown.edu", true);
        final Student alex = ConfigurationData.generateStudent(3, "aunger", "Alex", "Unger", "aunger@cs.brown.edu", true);
        final Student hannah = ConfigurationData.generateStudent(4, "hdrosen", "Hannah", "Rosen", "hdrosen@cs.brown.edu", true);
        final Group group = ConfigurationData.generateGroup(1, _testAsgn, "test_group", josh, jonathan, alex, hannah);
        final double pointsGiven = 12;

        LabPart lab = createMock(LabPart.class);
        expect(lab.getLabNumber()).andReturn(1).anyTimes();
        expect(lab.getAssignment()).andReturn(_testAsgn).atLeastOnce();
        expect(lab.getPoints()).andReturn(15).atLeastOnce();
        replay(lab);

        LabConfigurationParser parser = createMock(LabConfigurationParser.class);
        expect(parser.getLabPart(1)).andReturn(lab);
        replay(parser);

        final DataServices ds = createMock(DataServices.class);
        expect(ds.isStudentLoginInDatabase(josh.getLogin())).andReturn(true);
        expect(ds.getStudentFromLogin(josh.getLogin())).andReturn(josh);
        expect(ds.getGroup(_testAsgn, josh)).andReturn(group);
        expect(ds.getScore(eq(group), eq(lab))).andReturn(null);
        ds.enterGrade(group, lab, pointsGiven);
        expectLastCall();
        replay(ds);

        SingletonAllocation<DataServices> dsAlloc =
            new SingletonAllocation<DataServices>()
            {
                public DataServices allocate() { return ds; };
            };
        new Allocator.Customizer().setDataServices(dsAlloc).customize();


        CheckoffCLI.CheckoffInteractor interactor = new CheckoffCLI.CheckoffInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };

        List<String> args = Arrays.asList(new String[] { Integer.toString(labNumber),
            josh.getLogin(), Double.toString(pointsGiven) });

        CheckoffCLI.CheckoffResult result = CheckoffCLI.performCheckoff(args, interactor, parser);
        assertEquals(CheckoffCLI.CheckoffResult.SUCCEEDED, result);

        verify(_testAsgn);
        verify(lab);
        verify(parser);
    }

    //Tests checking off an individual
    @Test
    public void testIndividualCheckoff() throws ConfigurationException, SQLException, CheckoffException, ServicesException
    {
        final int labNumber = 1;
        final Student student = ConfigurationData.generateStudent(1, "jak2", "Joshua", "Kaplan", "jak2@cs.brown.edu", true);
        final Group group = ConfigurationData.generateGroup(1, _testAsgn, student.getName(), student);
        final double pointsGiven = 12;

        LabPart lab = createMock(LabPart.class);
        expect(lab.getLabNumber()).andReturn(1).anyTimes();
        expect(lab.getDBID()).andReturn("Lab").anyTimes();
        expect(lab.getAssignment()).andReturn(_testAsgn).atLeastOnce();
        expect(lab.getPoints()).andReturn(15).atLeastOnce();
        replay(lab);

        LabConfigurationParser parser = createMock(LabConfigurationParser.class);
        expect(parser.getLabPart(1)).andReturn(lab);
        replay(parser);

        final DataServices ds = createMock(DataServices.class);
        expect(ds.isStudentLoginInDatabase(student.getLogin())).andReturn(true);
        expect(ds.getStudentFromLogin(student.getLogin())).andReturn(student);
        expect(ds.getGroup(_testAsgn, student)).andReturn(group);
        expect(ds.getScore(eq(group), eq(lab))).andReturn(null);
        ds.enterGrade(group, lab, pointsGiven);
        expectLastCall();
        replay(ds);

        SingletonAllocation<DataServices> dsAlloc =
            new SingletonAllocation<DataServices>()
            {
                public DataServices allocate() { return ds; };
            };
        new Allocator.Customizer().setDataServices(dsAlloc).customize();

        CheckoffCLI.CheckoffInteractor interactor = new CheckoffCLI.CheckoffInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };

        List<String> args = Arrays.asList(new String[] { Integer.toString(labNumber),
            student.getLogin(), Double.toString(pointsGiven) });

        CheckoffCLI.CheckoffResult result = CheckoffCLI.performCheckoff(args, interactor, parser);
        assertEquals(CheckoffCLI.CheckoffResult.SUCCEEDED, result);

        verify(_testAsgn);
        verify(lab);
        verify(parser);
    }

}