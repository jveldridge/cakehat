package support.utils.posix;

import com.google.common.collect.ImmutableSet;
import com.sun.jna.Platform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import support.utils.ExternalProcessesUtilities;
import support.utils.ExternalProcessesUtilities.ProcessResponse;
import support.utils.ExternalProcessesUtilitiesImpl;

/**
 * 
 * @author jak2
 */
@RunWith(Parameterized.class)
public class FilePermissionsTest
{   
    /**
     * This method is called by JUnit via reflection. For each entry in the returned collection the object array is used
     * to construct an instance of this class via reflection and pass the arguments in the object array into the
     * constructor.
     * <br/><br/>
     * Returns all possible combinations of permission sets with each set as the only entry in the object array for that
     * entry in the collection.
     * 
     * @return 
     */
    @Parameters
    public static Collection<Object[]> getAllPermissionSets()
    {
        Set<Set<FilePermission>> allPermissionSets = generateAllSets(ImmutableSet.copyOf(FilePermission.values()));
        
        Collection<Object[]> data = new ArrayList<Object[]>(allPermissionSets.size());
        for(Set<FilePermission> permissionSet : allPermissionSets)
        {
            data.add(new Object[] { permissionSet });
        }
        
        return data;
    }
    
    /**
     * Helper function to generate all subsets for a given set of elements.
     * 
     * @param <E>
     * @param setElements
     * @return 
     */
    private static <E> Set<Set<E>> generateAllSets(Set<E> setElements)
    {
        Set<Set<E>> allSubsets = new HashSet<Set<E>>();
        
        //Take each element in setElements and put it into its own set
        Set<Set<E>> initialSubsets = new HashSet<Set<E>>();
        for(E elem : setElements)
        {
            initialSubsets.add(ImmutableSet.of(elem));
        }
        allSubsets.addAll(initialSubsets);
        
        Set<Set<E>> currSubsets = initialSubsets;
        for(int i = 0; i < setElements.size(); i++)
        {
            Set<Set<E>> nextSubsets = new HashSet<Set<E>>();
            for(Set<E> currSubset : currSubsets)
            {
                for(E elem : setElements)
                {
                    Set<E> nextSubset = new HashSet<E>(currSubset);
                    nextSubset.add(elem);
                    nextSubsets.add(nextSubset);
                }
            }
            
            allSubsets.addAll(nextSubsets);
            currSubsets = nextSubsets;
        }
        
        return allSubsets;
    }
    
    private static final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();
    private static final ExternalProcessesUtilities EXTERNAL_PROC_UTILS = new ExternalProcessesUtilitiesImpl();
    
    /**
     * The permissions to be used in testing.
     */
    private final Set<FilePermission> _permissions;
    
    /**
     * Constructed via reflection by JUnit. Argument passed in comes from {@link #getAllPermissionSets()}.
     * 
     * @param permissionSet 
     */
    public FilePermissionsTest(Set<FilePermission> permissionSet)
    {
        _permissions = permissionSet;
    }
    
    /**
     * Sets the permissions using Java JNA code.
     * Gets the permissions using Java JNA code.
     * 
     * @throws IOException
     * @throws NativeException 
     */
    @Test
    public void nativeSet_nativeGet() throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);
        
        try
        {
            //Set permissions - native
            int modeToSet = 0;
            for(FilePermission permission : _permissions)
            {
                modeToSet += permission.getValue();
            }
            NATIVE_FUNCTIONS.chmod(tmpFile, modeToSet);

            //Read out the permissions - native
            FileInformation info = NATIVE_FUNCTIONS.getFileInformation(tmpFile);

            //Validate
            assertEquals(_permissions, info.getFilePermissions());
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }
    
    /**
     * Sets the permissions using an external process call.
     * Gets the permissions using Java JNA code.
     * 
     * @throws IOException
     * @throws NativeException 
     */
    @Test
    public void externalSet_nativeGet() throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);
        
        try
        {
            //Set permissions - external
            int modeToSet = 0;
            for(FilePermission permission : _permissions)
            {
                modeToSet += permission.getValue();
            }
            String cmd = "chmod " + Integer.toString(modeToSet, 8) + " " + tmpFile.getName();
            EXTERNAL_PROC_UTILS.executeSynchronously(cmd, tmpFile.getParentFile());

            //Read out the permissions - native
            FileInformation info = NATIVE_FUNCTIONS.getFileInformation(tmpFile);

            //Validate
            assertEquals(_permissions, info.getFilePermissions());
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }
    
    /**
     * Sets the permissions using Java JNA code.
     * Gets the permissions using an external process call.
     * 
     * @throws IOException
     * @throws NativeException 
     */
    @Test
    public void nativeSet_externalGet() throws IOException, NativeException
    {
        //Create a temporary file
        File tmpFile = File.createTempFile("temp_file", null);
        
        try
        {
            //Set permissions - native
            int modeToSet = 0;
            for(FilePermission permission : _permissions)
            {
                modeToSet += permission.getValue();
            }
            NATIVE_FUNCTIONS.chmod(tmpFile, modeToSet);

            //Read out the permissions - external
            int modeRetrieved;
            if(Platform.isLinux())
            {
                //Command causes just the access permission values (in octal) to be output
                String cmd = "stat -c%a " + tmpFile.getName();
                ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously(cmd, tmpFile.getParentFile());
                modeRetrieved = Integer.parseInt(response.getOutputResponse().get(0), 8);
            }
            else if(Platform.isMac())
            {
                //Command causes just the mode (in octal), which contains the access permission values, to be output
                String cmd = "stat -f%p " + tmpFile.getName();
                ProcessResponse response = EXTERNAL_PROC_UTILS.executeSynchronously(cmd, tmpFile.getParentFile());

                //The permissions information is represented by the last 4 characters of the mode
                String line = response.getOutputResponse().get(0);
                String permissionStr = line.substring(line.length() - 4);
                modeRetrieved = Integer.parseInt(permissionStr, 8);
            }
            else
            {
                throw new UnsupportedOperationException("Platform not supported");
            }

            //Validate
            assertEquals(modeRetrieved, modeToSet);
        }
        finally
        {
            //Clean up temporary file
            tmpFile.delete();
        }
    }
}