package support.utils.posix;

import support.utils.ExternalProcessesUtilities;
import support.utils.ExternalProcessesUtilitiesImpl;
import com.sun.jna.Platform;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;

/**
 * Tests functionality in {@link NativeFileStat} by comparing the values against that returned by executing the stat
 * command in an external process.
 *
 * @author jak2
 */
public class NativeFileStatTest
{
    private static final ExternalProcessesUtilities EXTERNAL_PROC_UTILS = new ExternalProcessesUtilitiesImpl();
    private static final LibCWrapper LIB_C_WRAPPER = new LibCWrapper();
    
    // The following two tests are more complicated than the others because the behavior of the stat command is
    // different on OS X and Linux and base conversions are required
    
    @Test
    public void testMode() throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);
        
        try
        {
            //Build the stat command
            String cmd = "stat ";
            int base;
            if(Platform.isMac())
            {
                cmd += "-f%p ";
                base = 8;
            }
            else if(Platform.isLinux())
            {
                cmd += "-c%f ";
                base = 16;
            }
            else
            {
                throw new UnsupportedOperationException("Platform not supported");
            }
            cmd += tmpFile.getName();
            
            //Get the expected (from external call) and actual (cakehat code being tested) and compare
            int expected = Integer.parseInt(EXTERNAL_PROC_UTILS.executeSynchronously(cmd, tmpFile.getParentFile())
                    .getOutputResponse().get(0), base);
            int mode = LIB_C_WRAPPER.lstat(tmpFile.getAbsolutePath()).getMode();
            //The stat command returns the file type and permissions values which are stored in the lower digits of
            //st_mode value, so apply this bit mask to only compare the last 8 octal digits (4 hex digits) 
            int actual = mode & 0xffff;
            assertEquals(expected, actual);
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }
    
    @Test
    public void testDeviceId() throws IOException, NativeException
    {
        //dev null is a special file on *NIX that has a device id
        File devNull = new File("/dev/null");
        
        if(Platform.isMac())
        {
            String cmd = "stat -f%r " + devNull.getName();
            long expected = Long.parseLong(EXTERNAL_PROC_UTILS.executeSynchronously(cmd, devNull.getParentFile())
                .getOutputResponse().get(0));
            long actual = LIB_C_WRAPPER.lstat(devNull.getAbsolutePath()).getDeviceId();
            assertEquals(expected, actual);
        }
        else if(Platform.isLinux())
        {
            String cmd = "stat -c%t:%T " + devNull.getName();
            String[] response = EXTERNAL_PROC_UTILS.executeSynchronously(cmd, devNull.getParentFile())
                .getOutputResponse().get(0).split(":");
            long major = Long.parseLong(response[0]);
            long minor = Long.parseLong(response[1]);
            //Adapted from the gnu_dev_makedev macro in /usr/include/sys/sysmacros.h
            long expected = ((minor & 0xff) | ((major & 0xfff) << 8) | ((minor & ~0xff) << 12) | ((major & ~0xfff) << 32));

            long actual = LIB_C_WRAPPER.lstat(devNull.getAbsolutePath()).getDeviceId();
            assertEquals(expected, actual);
        }
        else
        {
            throw new UnsupportedOperationException("Platform not supported");
        }
    }
    
    // The following are straightforward tests against the value returned by stat for the property - see the helper
    // functions they rely on for more details
    
    @Test
    public void testContainingDeviceId() throws IOException, NativeException
    {
        testStatProperty('d', 'd', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getContainingDeviceId();
            }
        });
    }
    
    @Test
    public void testNumberOfHardLinks() throws IOException, NativeException
    {
        testStatProperty('l', 'h', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getNumberOfHardLinks();
            }
        });
    }
    
    @Test
    public void testInode() throws IOException, NativeException
    {
        testStatProperty('i', 'i', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getInode();
            }
        });
    }
    
    @Test
    public void testBlockSize() throws IOException, NativeException
    {
        testStatProperty('k', 'o', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getBlockSize();
            }
        });
    }
    
    @Test
    public void testNumberOfBlocks() throws IOException, NativeException
    {   
        testStatProperty('b', 'b', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getNumberOfBlocks();
            }
        });
    }

    @Test
    public void testFileSize() throws IOException, NativeException
    {
        testStatProperty('z', 's', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getFileSize();
            }
        });
    }
    
    @Test
    public void testUserId() throws IOException, NativeException
    {
        testStatProperty('u', 'u', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getUserId();
            }
        });
    }

    @Test
    public void testGroupId() throws IOException, NativeException
    {
        testStatProperty('g', 'g', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getGroupId();
            }
        });
    }

    @Test
    public void testLastModifiedSeconds() throws IOException, NativeException
    {
        testStatProperty('m', 'Y', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getLastModifiedSeconds();
            }
        });
    }

    @Test
    public void testLastAccessedSeconds() throws IOException, NativeException
    {
        testStatProperty('a', 'X', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getLastAccessedSeconds();
            }
        });
    }

    @Test
    public void testLastStatusChangedSeconds() throws IOException, NativeException
    {
        testStatProperty('c', 'Z', new StatPropertyValue()
        {
            @Override
            public long getProperty(NativeFileStat stat)
            {
                return stat.getLastStatusChangedSeconds();
            }
        });
    }
    
    
    /******************************************************************************************************************\
    |*                                                TEST HELPERS                                                    *|
    \******************************************************************************************************************/
    
    
    private static interface StatPropertyValue
    {
        public long getProperty(NativeFileStat stat);
    }
    
    private static void testStatProperty(char mac, char linux, StatPropertyValue value) throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);
        
        try
        {
            NativeFileStat stat = LIB_C_WRAPPER.lstat(tmpFile.getAbsolutePath());
            assertEquals(stat(mac, linux, tmpFile), value.getProperty(stat));
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }
    
    private static long stat(char mac, char linux, File file) throws IOException
    {
        String cmd = "stat ";
        if(Platform.isLinux())
        {
            cmd += "-c%" + linux;
        }
        else if(Platform.isMac())
        {
            cmd += "-f%" + mac;
        }
        else
        {
            throw new UnsupportedOperationException("Platform not supported");
        }
        cmd += " " + file.getName();
        
        String response = EXTERNAL_PROC_UTILS.executeSynchronously(cmd, file.getParentFile()).getOutputResponse().get(0);
        
        return Long.parseLong(response);
    }
}