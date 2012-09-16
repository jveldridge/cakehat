package support.utils.posix;

import java.util.Set;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jak2
 */
public class FileOwnershipTest
{
    private static final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();

    @Test
    public void testUserOwner() throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);

        try
        {
            //Read out the file info
            FileInformation info = NATIVE_FUNCTIONS.getFileInformation(tmpFile);

            //Validate the user id of the file matches the user id of the user that is running this test
            //The temp file was created by the user running the test so it will be belong to that user
            assertEquals(NATIVE_FUNCTIONS.getUserId(), info.getUserId());
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }

    @Test
    public void testGroupOwner() throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);

        try
        {
            //Read out the file info
            FileInformation info = NATIVE_FUNCTIONS.getFileInformation(tmpFile);

            //Validate the group id of the file matches the group id of the user that is running this test
            //The temp file was created by the user running the test so it will be belong to that user's group
            assertEquals(NATIVE_FUNCTIONS.getPrimaryUserGroupId(), info.getGroupId());
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }

    @Test
    public void testChangeGroupOwner() throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);

        try
        {
            //Find a group the user belongs to that is not their default group as the file will be created with that
            //group ownership
            Set<Integer> userGroups = NATIVE_FUNCTIONS.getAllUserGroupIds();
            Integer defaultUserGroup = NATIVE_FUNCTIONS.getPrimaryUserGroupId();

            Integer userGroupToChangeTo = null;
            for(Integer groupId : userGroups)
            {
                if(!groupId.equals(defaultUserGroup))
                {
                    userGroupToChangeTo = groupId;
                    break;
                }
            }
            if(userGroupToChangeTo == null)
            {
                throw new RuntimeException("Unable to run test, user only belongs to one group");
            }

            //Change the group ownership
            NATIVE_FUNCTIONS.changeGroup(tmpFile, NATIVE_FUNCTIONS.getGroupName(userGroupToChangeTo));


            //Read out the file info
            FileInformation info = NATIVE_FUNCTIONS.getFileInformation(tmpFile);

            //Validate the group id of the file matches the group id it was changed to
            assertEquals((int) userGroupToChangeTo, info.getGroupId());
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }
}