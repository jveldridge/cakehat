package support.utils.posix;

import org.junit.Test;

/**
 *
 * @author jak2
 */
public class RemoteConnectionTest
{
    @Test
    public void manualValidationIsUserRemotelyConnected() throws NativeException
    {
        boolean remotelyConnected = new NativeFunctions().isUserRemotelyConnected();
        System.out.println("Validate manually, user remotely connected? " + remotelyConnected);
    }
}