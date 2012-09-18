package support.utils.posix;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import support.utils.ExternalProcessesUtilities;
import support.utils.ExternalProcessesUtilitiesImpl;
import static org.junit.Assert.*;

/**
 *
 * @author jak2
 */
public class FileTypeTest
{
    private static final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();
    private static final ExternalProcessesUtilities EXTERNAL_PROC_UTILS = new ExternalProcessesUtilitiesImpl();
    
    @Test
    public void testFile() throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);
        
        try
        {
            //Read out the file info
            FileInformation info = NATIVE_FUNCTIONS.getFileInformation(tmpFile);

            //Validate the type is a file
            assertEquals(FileType.REGULAR_FILE, info.getFileType());
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }
    
    @Test
    public void testDirectory() throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);
        
        try
        {
            //Read out the file info
            FileInformation info = NATIVE_FUNCTIONS.getFileInformation(tmpFile.getParentFile());

            //Validate the type of the temporary's parent is a directory
            assertEquals(FileType.DIRECTORY, info.getFileType());
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }
    
    @Test
    public void testSymbolicLink() throws IOException, NativeException
    {
        //Create a temporary file that will serve as the source of the symbolic link
        File srcTmpFile = File.createTempFile("temp_file", null);
        
        try
        {
            //Create a temporary file that will be the name of the target file of the symbolic link
            //This file will be deleted, it was only created in order to generate a unique file name
            File targetTmpFile = File.createTempFile("temp_file", null);
            String targetFileName = targetTmpFile.getName();
            targetTmpFile.delete();

            //Create symbolic link with an external call
            String cmd = "ln -s " + srcTmpFile.getName() + " " + targetFileName;
            EXTERNAL_PROC_UTILS.executeSynchronously(cmd, srcTmpFile.getParentFile()); 

            //Read out the file info
            FileInformation info = NATIVE_FUNCTIONS.getFileInformation(targetTmpFile);

            //Validate the type of the symbolic link
            assertEquals(FileType.SYMBOLIC_LINK, info.getFileType());
        }
        finally
        {
            //Clean up temporary file (Java 6 cannot delete a symbolic link, it will end up deleting the target instead)
            srcTmpFile.delete();
        }
    }
}