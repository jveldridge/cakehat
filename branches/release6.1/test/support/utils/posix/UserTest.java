package support.utils.posix;

import com.sun.jna.Platform;
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
public class UserTest
{
    private static final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();
    private static final ExternalProcessesUtilities EXTERNAL_PROC_UTILS = new ExternalProcessesUtilitiesImpl();
    
    @Test
    public void testRealName() throws IOException, NativeException
    {
        String userLogin = NATIVE_FUNCTIONS.getUserLogin();
        
        String actualRealName = NATIVE_FUNCTIONS.getRealName(userLogin);
        
        String expectedRealName;
        if(Platform.isMac())
        {
            //Second line (1st with 0-indexing) of output will contain users real name
            String cmd = "dscl . -read /Users/" + userLogin + " RealName";
            ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously(cmd, null);
            expectedRealName = response.getOutputResponse().get(1).trim();
        }
        else if(Platform.isLinux())
        {
            //Retrieve the user info entry from the passwd database, the format is colon separated
            //The fifth entry (4th with 0-indexing) is the real user name
            String cmd = "getent passwd " + userLogin;
            ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously(cmd, null);
            String userInfoEntry = response.getOutputResponse().get(0);
            String[] userInfo = userInfoEntry.split(":");
            expectedRealName = userInfo[4];
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported operating system");
        }
        
        assertEquals(expectedRealName, actualRealName);
    }
    
    @Test
    public void testUserLogin() throws IOException
    {
        //Get user login from native functions
        String actualUserLogin = NATIVE_FUNCTIONS.getUserLogin();
        
        //Get user login from external call
        ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously("whoami", null);
        String expectedUserLogin = response.getOutputResponse().get(0);
        
        assertEquals(expectedUserLogin, actualUserLogin);
    }
    
    @Test
    public void testUserId() throws IOException
    {
        int actualUserId = NATIVE_FUNCTIONS.getUserId();
        
        ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously("id -ru", null);
        int expectedUserId = Integer.parseInt(response.getOutputResponse().get(0));
        
        assertEquals(expectedUserId, actualUserId);
    }
    
    @Test
    public void testIsLogin_ValidLogin()
    {
        assertTrue(NATIVE_FUNCTIONS.isLogin(NATIVE_FUNCTIONS.getUserLogin()));
    }
    
    @Test
    public void testIsLogin_InvalidLogin()
    {
        assertFalse(NATIVE_FUNCTIONS.isLogin("ghsf!@#$%^&&*(()//\\fasf"));
    }
}