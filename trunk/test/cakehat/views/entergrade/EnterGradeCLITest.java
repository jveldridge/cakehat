package cakehat.views.entergrade;

import java.util.HashSet;
import java.util.ArrayList;
import org.junit.Test;
import cakehat.newdatabase.TA;
import org.joda.time.DateTime;
import cakehat.Allocator.SingletonAllocation;
import cakehat.Allocator;
import java.util.List;
import java.util.Arrays;
import cakehat.newdatabase.PartGrade;
import cakehat.newdatabase.DataServicesV5;
import cakehat.assignment.Part;
import cakehat.newdatabase.Group;
import cakehat.newdatabase.Student;
import cakehat.services.ServicesException;
import cakehat.views.entergrade.EnterGradeCLI.EnterGradeException;
import cakehat.assignment.Assignment;
import cakehat.database.ConfigurationData;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Basic tests for {@link EnterGradeCLI} to verify that check off works when
 * supplied with the correct arguments. As of now, there are no JUnit tests for
 * improper input.
 *
 * @author hdrosen
 */
public class EnterGradeCLITest {
    
    @Test
    public void testAbortOverwrite() throws ServicesException, EnterGradeException {
        Student stud = ConfigurationData.generateNewDatabaseStudent();
        final double pointsGiven = 9;
        
        Assignment asgn = ConfigurationData.generateAsgnWithQuickNamePart();
        Part part = asgn.getGradableEvents().get(0).getParts().get(0);
        Group group = ConfigurationData.generateGroupWithStudent(stud, asgn);
        
        TA ta = createMock(TA.class);
        expect(ta.getLogin()).andReturn("hdrosen").anyTimes();
        expect(ta.getName()).andReturn("Hannah Rosen").anyTimes();
        expect(ta.getFirstName()).andReturn("Hannah").anyTimes();
        expect(ta.getLastName()).andReturn("Rosen").anyTimes();
        replay(ta);
        
        PartGrade grade = createMock(PartGrade.class);
        expect(grade.getDateRecorded()).andReturn(new DateTime());
        expect(grade.getEarned()).andReturn(pointsGiven).anyTimes();
        expect(grade.getGroup()).andReturn(group).anyTimes();
        expect(grade.getPart()).andReturn(part).anyTimes();
        expect(grade.getSubmissionStatus()).andReturn(PartGrade.SubmissionStatus.SUBMITTED_MATCHING).anyTimes();
        expect(grade.getTA()).andReturn(ta).anyTimes();
        replay(grade);
        
        HashSet<Student> enabledStuds = new HashSet<Student>();
        enabledStuds.add(stud);
        
        ArrayList<Assignment> asgns = new ArrayList<Assignment>();
        asgns.add(asgn);
        
        final DataServicesV5 ds = createMock(DataServicesV5.class);
        expect(ds.isStudentLoginInDatabase(stud.getLogin())).andReturn(true).anyTimes();
        expect(ds.getStudentFromLogin(stud.getLogin())).andReturn(stud).anyTimes();
        expect(ds.getEnabledStudents()).andReturn(enabledStuds).anyTimes();
        expect(ds.getEarned(group, part)).andReturn(grade).anyTimes();
        expect(ds.getAssignments()).andReturn(asgns).anyTimes();
        expect(ds.getGroup(asgn, stud)).andReturn(group).anyTimes();
        replay(ds);
        
        SingletonAllocation<DataServicesV5> dsAlloc =
            new SingletonAllocation<DataServicesV5>()
            {
                public DataServicesV5 allocate() { return ds; };
            };
        new Allocator.Customizer().setDataServicesV5(dsAlloc).customize();

        EnterGradeCLI.EnterGradeInteractor interactor = new EnterGradeCLI.EnterGradeInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };
        
        List<String> args = Arrays.asList(new String[] { part.getQuickName(),
            stud.getLogin(), Double.toString(pointsGiven) });

        EnterGradeCLI.EnterGradeResult result = EnterGradeCLI.performEnterGrade(args, interactor);
        assertEquals(EnterGradeCLI.EnterGradeResult.ABORTED, result); 
    }
    
    @Test
    public void testGroupCheckOff() throws ServicesException, EnterGradeException {
        Student hannah = ConfigurationData.generateNewStudent("hdrosen", "Hannah", "Rosen", 1);
        Student yudi = ConfigurationData.generateNewStudent("yf6", "Yudi", "Fu", 2);
        Student josh = ConfigurationData.generateNewStudent("jak2", "Josh", "Kaplan", 3);
        Student jonathan = ConfigurationData.generateNewStudent("jeldridg", "Yohonathan", "Eldridge", 4);
        Student wil = ConfigurationData.generateNewStudent("wyegelwe", "Wil", "Yegelwel", 5);
        
        Assignment asgn = ConfigurationData.generateAsgnWithQuickNamePart();
        Part part = asgn.getGradableEvents().get(0).getParts().get(0);
        
        Group group = ConfigurationData.generateGroupWithStudents(asgn, hannah, yudi, josh, jonathan);
        final double pointsGiven = 9;
        
        TA ta = createMock(TA.class);
        expect(ta.getLogin()).andReturn("aunger").anyTimes();
        expect(ta.getName()).andReturn("Alex Unger").anyTimes();
        expect(ta.getFirstName()).andReturn("Alex").anyTimes();
        expect(ta.getLastName()).andReturn("Unger").anyTimes();
        replay(ta);
        
        HashSet<Student> enabledStuds = new HashSet<Student>();
        enabledStuds.add(hannah);
        enabledStuds.add(yudi);
        enabledStuds.add(wil);
        enabledStuds.add(josh);
        enabledStuds.add(jonathan);
        
        ArrayList<Assignment> asgns = new ArrayList<Assignment>();
        asgns.add(asgn);
        
        final DataServicesV5 ds = createMock(DataServicesV5.class);
        expect(ds.isStudentLoginInDatabase(hannah.getLogin())).andReturn(true).anyTimes();
        expect(ds.getStudentFromLogin(hannah.getLogin())).andReturn(hannah).anyTimes();
        expect(ds.getGroup(asgn, hannah)).andReturn(group).anyTimes();
        expect(ds.isStudentLoginInDatabase(yudi.getLogin())).andReturn(true).anyTimes();
        expect(ds.getStudentFromLogin(yudi.getLogin())).andReturn(yudi).anyTimes();
        expect(ds.getGroup(asgn, yudi)).andReturn(group).anyTimes();
        expect(ds.isStudentLoginInDatabase(wil.getLogin())).andReturn(true).anyTimes();
        expect(ds.getStudentFromLogin(wil.getLogin())).andReturn(wil).anyTimes();
        expect(ds.getGroup(asgn, wil)).andReturn(group).anyTimes();
        expect(ds.isStudentLoginInDatabase(josh.getLogin())).andReturn(true).anyTimes();
        expect(ds.getStudentFromLogin(josh.getLogin())).andReturn(josh).anyTimes();
        expect(ds.getGroup(asgn, josh)).andReturn(group).anyTimes();
        expect(ds.isStudentLoginInDatabase(jonathan.getLogin())).andReturn(true).anyTimes();
        expect(ds.getStudentFromLogin(jonathan.getLogin())).andReturn(jonathan).anyTimes();
        expect(ds.getGroup(asgn, jonathan)).andReturn(group).anyTimes();
        expect(ds.getEnabledStudents()).andReturn(enabledStuds).anyTimes();
        expect(ds.getEarned(group, part)).andReturn(null).anyTimes();
        expect(ds.getAssignments()).andReturn(asgns).anyTimes();
        ds.setEarned(eq(group), eq(part), eq(pointsGiven), eq(true));
        expectLastCall();

        replay(ds);
        
        SingletonAllocation<DataServicesV5> dsAlloc =
            new SingletonAllocation<DataServicesV5>()
            {
                public DataServicesV5 allocate() { return ds; };
            };
        new Allocator.Customizer().setDataServicesV5(dsAlloc).customize();

        EnterGradeCLI.EnterGradeInteractor interactor = new EnterGradeCLI.EnterGradeInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };
        
        List<String> args = Arrays.asList(new String[] { part.getQuickName(),
            hannah.getLogin(), Double.toString(pointsGiven) });
        
        EnterGradeCLI.EnterGradeResult result = EnterGradeCLI.performEnterGrade(args, interactor);
        assertEquals(EnterGradeCLI.EnterGradeResult.SUCCEEDED, result);

    }
    
    @Test
    public void testIndividualCheckoff() throws EnterGradeException, ServicesException {
        Student stud = ConfigurationData.generateNewDatabaseStudent();
        final double pointsGiven = 9;
        
        Assignment asgn = ConfigurationData.generateAsgnWithQuickNamePart();
        Part part = asgn.getGradableEvents().get(0).getParts().get(0);
        Group group = ConfigurationData.generateGroupWithStudent(stud, asgn);
        
        TA ta = createMock(TA.class);
        expect(ta.getLogin()).andReturn("hdrosen").anyTimes();
        expect(ta.getName()).andReturn("Hannah Rosen").anyTimes();
        expect(ta.getFirstName()).andReturn("Hannah").anyTimes();
        expect(ta.getLastName()).andReturn("Rosen").anyTimes();
        replay(ta);
        
        HashSet<Student> enabledStuds = new HashSet<Student>();
        enabledStuds.add(stud);
        
        ArrayList<Assignment> asgns = new ArrayList<Assignment>();
        asgns.add(asgn);
        
        final DataServicesV5 ds = createMock(DataServicesV5.class);
        expect(ds.isStudentLoginInDatabase(stud.getLogin())).andReturn(true).anyTimes();
        expect(ds.getStudentFromLogin(stud.getLogin())).andReturn(stud).anyTimes();
        expect(ds.getEnabledStudents()).andReturn(enabledStuds).anyTimes();
        expect(ds.getEarned(group, part)).andReturn(null).anyTimes();
        expect(ds.getAssignments()).andReturn(asgns).anyTimes();
        expect(ds.getGroup(asgn, stud)).andReturn(group).anyTimes();
        ds.setEarned(eq(group), eq(part), eq(pointsGiven), eq(true));
        expectLastCall();
        replay(ds);
        
        SingletonAllocation<DataServicesV5> dsAlloc =
            new SingletonAllocation<DataServicesV5>()
            {
                public DataServicesV5 allocate() { return ds; };
            };
        new Allocator.Customizer().setDataServicesV5(dsAlloc).customize();

        EnterGradeCLI.EnterGradeInteractor interactor = new EnterGradeCLI.EnterGradeInteractor()
        {
            public boolean shouldOverwriteScore() { return false; }
            public void println(String msg) { }
            public void print(String msg) { }
        };
        
        List<String> args = Arrays.asList(new String[] { part.getQuickName(),
            stud.getLogin(), Double.toString(pointsGiven) });

        EnterGradeCLI.EnterGradeResult result = EnterGradeCLI.performEnterGrade(args, interactor);
        assertEquals(EnterGradeCLI.EnterGradeResult.SUCCEEDED, result); 
        
    }
    
}
