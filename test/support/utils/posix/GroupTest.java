package support.utils.posix;

import com.sun.jna.Platform;
import java.util.HashSet;
import java.util.Set;
import support.utils.ExternalProcessesUtilitiesImpl;
import support.utils.ExternalProcessesUtilities.ProcessResponse;
import support.utils.ExternalProcessesUtilities;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jak2
 */
public class GroupTest
{
    private static final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();
    private static final ExternalProcessesUtilities EXTERNAL_PROC_UTILS = new ExternalProcessesUtilitiesImpl();
    
    @Test
    public void testUserGroupId() throws IOException
    {
        int actualUserGroupId = NATIVE_FUNCTIONS.getPrimaryUserGroupId();
        
        ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously("id -rg", null);
        int expectedUserGroupId = Integer.parseInt(response.getOutputResponse().get(0));
        
        assertEquals(expectedUserGroupId, actualUserGroupId);
    }
    
    @Test
    public void testUserGroupIds() throws NativeException, IOException
    {
        Set<Integer> expectedUserGroupIds = NATIVE_FUNCTIONS.getAllUserGroupIds();
        
        ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously("id -G", null);
        String groupIdsAsSpaceSeparatedStr = response.getOutputResponse().get(0);
        String[] groupIdsStr = groupIdsAsSpaceSeparatedStr.split(" ");
        Set<Integer> actualUserGroupIds = new HashSet<Integer>();
        for(String groupId : groupIdsStr)
        {
            actualUserGroupIds.add(Integer.parseInt(groupId));
        }
        
        assertEquals(expectedUserGroupIds, actualUserGroupIds);
    }
    
    @Test
    public void testGetGroupName() throws IOException, NativeException
    {
        int userGroupId = NATIVE_FUNCTIONS.getPrimaryUserGroupId();
        String actualGroupName = NATIVE_FUNCTIONS.getGroupName(userGroupId);
        
        if(Platform.isLinux())
        {
            //Retrieve the group info entry from the group database, the format is colon separated
            //The first entry (0th with 0-indexing) is the group name
            String cmd = "getent group " + userGroupId;
            ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously(cmd, null);
            String groupInfoEntry = response.getOutputResponse().get(0);
            String[] groupInfo = groupInfoEntry.split(":");
            String expectedGroupName = groupInfo[0];
            
            //Validate
            assertEquals(expectedGroupName, actualGroupName);
        }
        else if(Platform.isMac())
        {
            //There's no simple command to look up a group name from it's id, so instead validate the other way around
            //by taking the group name we are trying to validate and read out its id - then check that against the
            //initial id
            
            //Format: "PrimaryGroupID: <ID HERE>"
            String cmd = "dscl . -read /Groups/" + actualGroupName + " PrimaryGroupID";
            ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously(cmd, null);
            int groupId = Integer.parseInt(response.getOutputResponse().get(0).split(":")[1].trim());
            
            //Validate
            assertEquals(userGroupId, groupId);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported operating system");
        }
    }
}