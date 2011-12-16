package cakehat.config.handin;

import cakehat.services.PathServices;
import java.util.Arrays;
import java.util.List;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableList;
import cakehat.database.Database;
import cakehat.database.DatabaseImpl;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Before;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.config.Assignment;
import cakehat.config.ConfigurationInfo;
import cakehat.database.ConfigurationData;
import java.io.File;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Tests the string replacement used in the custom commands.
 *
 * @author jak2
 */
public class ExternalActionsTest
{
    private Assignment _testAsgn = ConfigurationData.generateRandomGroupAssignment();
    
    @Before
    public void setUp() throws IOException, SQLException {
        final Database db = new DatabaseImpl(Allocator.getFileSystemUtilities().createTempFile("tempDB", "db"));
        SingletonAllocation<Database> dbioAlloc =
            new SingletonAllocation<Database>()
            {
                public Database allocate() { return db; };
            };
        db.resetDatabase(); 
        
        final ConfigurationInfo ci = createMock(ConfigurationInfo.class);
        expect(ci.getAssignments()).andReturn(ImmutableList.of(_testAsgn)).anyTimes();
        expect(ci.getAssignment(_testAsgn.getDBID())).andReturn(_testAsgn).anyTimes();
        replay(ci);
        SingletonAllocation<ConfigurationInfo> ciAlloc =
            new SingletonAllocation<ConfigurationInfo>()
            {
                public ConfigurationInfo allocate() { return ci; };
            };
        
        new Allocator.Customizer().setConfigurationInfo(ciAlloc).setDatabase(dbioAlloc).customize(); 
    }
    
    @Test
    public void testReplaceHandinSequences_StudentLogins() throws ServicesException
    {
        //Setup
        Student member1 = ConfigurationData.generateStudent(1, "member_1", "m1f", "m1l", "m1e", true);
        Student member2 = ConfigurationData.generateStudent(2, "member_2", "m2f", "m2l", "m2e", true);
        Group group = ConfigurationData.generateGroup(1, _testAsgn, "the group", member1, member2);
        
        //Assertions
        String command, modifiedCmd;

        command = "open ^student_logins^ close";
        modifiedCmd = ExternalActions.replaceHandinSequences(command, null, group);
        assertEquals("Could not replace single instance of ^student_logins^",
                "open [\"member_1\",\"member_2\"] close",
                modifiedCmd);

        command = "open ^student_logins^ middle ^student_logins^ close";
        modifiedCmd = ExternalActions.replaceHandinSequences(command, null, group);
        assertEquals("Could not replace multiple instances of ^student_logins^",
                "open [\"member_1\",\"member_2\"] middle [\"member_1\",\"member_2\"] close",
                modifiedCmd);
    }

    @Test
    public void testReplaceHandinSequences_Groups() throws ServicesException
    {
        //Setup
        Student member1 = ConfigurationData.generateStudent(1, "member_1", "m1f", "m1l", "m1e", true);
        Student member2 = ConfigurationData.generateStudent(2, "member_2", "m2f", "m2l", "m2e", true);
        Group group = ConfigurationData.generateGroup(1, _testAsgn, "the group", member1, member2);

        //Assertions
        String command, modifiedCmd;

        command = "open ^group_name^ close";
        modifiedCmd = ExternalActions.replaceHandinSequences(command, null, group);
        assertEquals("Could not replace single instance of ^group_name^",
                "open \"the group\" close",
                modifiedCmd);

        command = "open ^group_name^ middle ^group_name^ close";
        modifiedCmd = ExternalActions.replaceHandinSequences(command, null, group);
        assertEquals("Could not replace single instance of ^group_names^",
                "open \"the group\" middle \"the group\" close",
                modifiedCmd);
    }

    @Test
    public void testReplaceHandinSequences_UnarchiveDir() throws ServicesException
    {
        //Setup
        Student member1 = ConfigurationData.generateStudent(1, "member_1", "m1f", "m1l", "m1e", true);
        Student member2 = ConfigurationData.generateStudent(2, "member_2", "m2f", "m2l", "m2e", true);
        
        Group group = ConfigurationData.generateGroup(1, _testAsgn, "the group", member1, member2);
        File unarchiveDir = new File("/root/other/the group");

        //Mock up a path services that will return dummy unarchive directories
        final PathServices pathServices = createMock(PathServices.class);
        expect(pathServices.getUnarchiveHandinDir((DistributablePart)null, group)).andReturn(unarchiveDir).times(2);
        replay(pathServices);

        SingletonAllocation<PathServices> pathServicesAlloc =
            new SingletonAllocation<PathServices>()
            {
                public PathServices allocate() { return pathServices; };
            };
        new Allocator.Customizer().setPathServices(pathServicesAlloc).customize();

        //Assertions
        String command, modifiedCmd;

        command = "open ^unarchive_dir^ close";
        modifiedCmd = ExternalActions.replaceHandinSequences(command, null, group);
        assertEquals("Could not replace single instance of ^unarchive_dirs^",
                "open \"" + unarchiveDir.getAbsolutePath() + "\" close",
                modifiedCmd);

        command = "open ^unarchive_dir^ middle ^unarchive_dir^ close";
        modifiedCmd = ExternalActions.replaceHandinSequences(command, null, group);
        assertEquals("Could not replace multiple instances of ^unarchive_dirs^",
                "open \"" + unarchiveDir.getAbsolutePath() + "\" middle \"" +
                unarchiveDir.getAbsolutePath() + "\" close",
                modifiedCmd);
    }

    @Test
    public void testReplaceAssignmentSequences()
    {
        //Setup
        final String assignmentName = "asgn efsfsf";
        final int assignmentNumber = 1455;
        final String partName = "part dfhdfhds";
        final int partNumber = 325364;

        Assignment asgn = createMock(Assignment.class);
        expect(asgn.getName()).andReturn(assignmentName).anyTimes();
        expect(asgn.getNumber()).andReturn(assignmentNumber).anyTimes();
        replay(asgn);

        DistributablePart part = createMock(DistributablePart.class);
        expect(part.getName()).andReturn(partName).anyTimes();
        expect(part.getNumber()).andReturn(partNumber).anyTimes();
        expect(part.getAssignment()).andReturn(asgn).anyTimes();
        replay(part);

        //Assertions
        String command, modifiedCmd;

        command = "open ^assignment_name^ close";
        modifiedCmd = ExternalActions.replaceAssignmentSequences(command, part);
        assertEquals("Could not replace single instance of ^assignment_name^",
                "open \"" + assignmentName + "\" close",
                modifiedCmd);

        command = "open ^assignment_name^ middle ^assignment_name^ close";
        modifiedCmd = ExternalActions.replaceAssignmentSequences(command, part);
        assertEquals("Could not replace multiple instances of ^assignment_name^",
                "open \"" + assignmentName + "\" middle \"" + assignmentName + "\" close",
                modifiedCmd);


        command = "open ^assignment_number^ close";
        modifiedCmd = ExternalActions.replaceAssignmentSequences(command, part);
        assertEquals("Could not replace single instance of ^assignment_number^",
                "open " + assignmentNumber + " close",
                modifiedCmd);

        command = "open ^assignment_number^ middle ^assignment_number^ close";
        modifiedCmd = ExternalActions.replaceAssignmentSequences(command, part);
        assertEquals("Could not replace multiple instances of ^assignment_number^",
                "open " + assignmentNumber + " middle " + assignmentNumber + " close",
                modifiedCmd);

        command = "open ^part_name^ close";
        modifiedCmd = ExternalActions.replaceAssignmentSequences(command, part);
        assertEquals("Could not replace single instance of ^part_name^",
                "open \"" + partName + "\" close",
                modifiedCmd);

        command = "open ^part_name^ middle ^part_name^ close";
        modifiedCmd = ExternalActions.replaceAssignmentSequences(command, part);
        assertEquals("Could not replace multiple instances of ^part_name^",
                "open \"" + partName + "\" middle \"" + partName + "\" close",
                modifiedCmd);

        command = "open ^part_number^ close";
        modifiedCmd = ExternalActions.replaceAssignmentSequences(command, part);
        assertEquals("Could not replace single instance of ^part_number^",
                "open " + partNumber + " close",
                modifiedCmd);

        command = "open ^part_number^ middle ^part_number^ close";
        modifiedCmd = ExternalActions.replaceAssignmentSequences(command, part);
        assertEquals("Could not replace multiple instances of ^part_number^",
                "open " + partNumber + " middle " + partNumber + " close",
                modifiedCmd);
    }

    @Test
    public void testReplaceGroupInfoSequences() throws ServicesException
    {
        //Setup        
        Student member1a = ConfigurationData.generateStudent(1, "member_1a", "m1af", "m1al", "m1ae", true);
        Student member2a = ConfigurationData.generateStudent(2, "member_2a", "m2af", "m2al", "m2ae", true);
        Group groupA = ConfigurationData.generateGroup(1, _testAsgn, "group A", member1a, member2a);

        Student member1b = ConfigurationData.generateStudent(3, "member_1b", "m1bf", "m1bl", "m1be", true);
        Student member2b = ConfigurationData.generateStudent(4, "member_2b", "m2bf", "m2bl", "m2be", true);
        Group groupB = ConfigurationData.generateGroup(2, _testAsgn, "group B", member1b, member2b);

        List<Group> groups = Arrays.asList(new Group[] { groupA, groupB });
        File dirA = new File("/root/other/group A");
        File dirB = new File("/root/other/group B");

        //Mock up a path services that will return dummy unarchive directories
        final PathServices pathServices = createMock(PathServices.class);
        expect(pathServices.getUnarchiveHandinDir((DistributablePart)null, groupA)).andReturn(dirA).times(2);
        expect(pathServices.getUnarchiveHandinDir((DistributablePart)null, groupB)).andReturn(dirB).times(2);
        replay(pathServices);

        SingletonAllocation<PathServices> pathServicesAlloc =
            new SingletonAllocation<PathServices>()
            {
                public PathServices allocate() { return pathServices; };
            };
        new Allocator.Customizer().setPathServices(pathServicesAlloc).customize();


        //Assertions
        String command, modifiedCmd;

        command = "open ^groups_info^ close";
        modifiedCmd = ExternalActions.replaceGroupInfoSequences(command, null, groups);
        assertEquals("Could not replace single instance of ^groups_info^",
                "open [{\"name\":\"group A\",\"members\":[\"member_1a\",\"member_2a\"]," +
                "\"unarchive_dir\":\"" + dirA.getAbsolutePath() + "\"}," +
                "{\"name\":\"group B\",\"members\":[\"member_1b\",\"member_2b\"]," +
                "\"unarchive_dir\":\"" + dirB.getAbsolutePath() + "\"}] close",
                modifiedCmd);
    }
}