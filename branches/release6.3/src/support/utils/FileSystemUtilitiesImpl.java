package support.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import support.utils.posix.NativeFunctions;
import support.utils.posix.FilePermission;
import support.utils.posix.NativeException;

public class FileSystemUtilitiesImpl implements FileSystemUtilities
{
    private static final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();
    
    private static final Set<FilePermission> READ_WRITE_PERMISSIONS = ImmutableSet.of(
        FilePermission.OWNER_READ, FilePermission.OWNER_WRITE,
        FilePermission.GROUP_READ, FilePermission.GROUP_WRITE);

    private static final Set<FilePermission> READ_WRITE_EXECUTE_PERMISSIONS = ImmutableSet.of(
        FilePermission.OWNER_READ, FilePermission.OWNER_WRITE, FilePermission.OWNER_EXECUTE,
        FilePermission.GROUP_READ, FilePermission.GROUP_WRITE, FilePermission.GROUP_EXECUTE);
    
    private static final Set<FilePermission> READ_WRITE_EXECUTE_SETGID_PERMISSIONS = ImmutableSet.of(
        FilePermission.OWNER_READ, FilePermission.OWNER_WRITE, FilePermission.OWNER_EXECUTE,
        FilePermission.GROUP_READ, FilePermission.GROUP_WRITE, FilePermission.GROUP_EXECUTE,
        FilePermission.SET_GROUP_ID_UPON_EXECUTION);
    
    /******************************************************************************************************************\
    |*                                                    Temp                                                         |  
    \******************************************************************************************************************/
    
    @Override
    public File createTempFile(String prefix, String suffix) throws IOException
    {
        File tmpFile = File.createTempFile(prefix, suffix);
        tmpFile.deleteOnExit();
        
        return tmpFile;
    }
    
    @Override
    public File createTempFile(String prefix, String suffix, File directory) throws IOException
    {
        File tmpFile = File.createTempFile(prefix, suffix, directory);
        tmpFile.deleteOnExit();
        
        return tmpFile;
    }
    
    /******************************************************************************************************************\
    |*                                                    Copy                                                         |  
    \******************************************************************************************************************/
    
    @Override
    public Set<File> copy(File src, File dst, OverwriteMode overwrite, boolean preserveDate, String groupOwner,
            FileCopyPermissions copyPermissions) throws FileCopyingException
    {
        Set<File> created;

        if(src.isFile())
        {
            created = this.copyFile(src, dst, overwrite, preserveDate, groupOwner, copyPermissions);
        }
        else if(src.isDirectory())
        {
            created = this.copyDirectory(src, dst, overwrite, preserveDate, groupOwner, copyPermissions);
        }
        else
        {
            throw new FileCopyingException(src, dst, null, false, "Source is neither a file nor a directory.");
        }

        return created;
    }

    /**
     * Copies {@code srcFile} to {@code dstDir}. If {@code dstDir} exists and {@code overWrite} is {@code false} then an
     * exception will be thrown.
     *
     * @param srcFile
     * @param dstFile
     * @param overwrite
     * @param preserveDate
     * @param groupOwner
     * @param copyPermissions
     *
     * @throws FileCopyingException
     *
     * @return
     */
    private Set<File> copyFile(File srcFile, File dstFile, OverwriteMode overwrite, boolean preserveDate,
            String groupOwner, FileCopyPermissions copyPermissions) throws FileCopyingException
    {
        //Perform validation
        if(!srcFile.exists())
        {
            throw new FileCopyingException(srcFile, dstFile, null, false, 
                    "Source file cannot be copied because it does not exist.");
        }
        if(!srcFile.isFile())
        {
            throw new FileCopyingException(srcFile, dstFile, null, false, 
                    "Source is not a file, this method only copies files.");
        }

        ImmutableSet.Builder<File> created = ImmutableSet.builder();
        if(dstFile.exists())
        {
            if(overwrite == OverwriteMode.REPLACE_EXISTING)
            {
                if(!dstFile.delete())
                {
                    throw new FileCopyingException(srcFile, dstFile, null, false, 
                            "Cannot overwrite destination file; unable to delete it.");
                }
            }
            else if(overwrite == OverwriteMode.FAIL_ON_EXISTING)
            {   
                throw new FileCopyingException(srcFile, dstFile, null, true, 
                        "Cannot copy source file to destination file location because destination file exists and " +
                        "fail on existing overwrite mode was set.");
            }
            else if(overwrite == OverwriteMode.KEEP_EXISTING)
            {
                return created.build();
            }
            else
            {
                throw new FileCopyingException(srcFile, dstFile, null, false,
                        "Invalid " + OverwriteMode.class.getCanonicalName() + ": " + overwrite + ".");
            }
        }

        //If the destination location needs directories to be created
        if(!dstFile.getParentFile().exists())
        {
            try
            {
                created.addAll(makeDirectory(dstFile.getParentFile(), groupOwner));
            }
            catch(DirectoryCreationException e)
            {   
                throw new FileCopyingException(srcFile, dstFile, e.getRemainingDirectories(), false, 
                        "Unable to create the necessary directories in order to perform the copy.", e);
            }
        }

        //Attempt to copy
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try
        {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(dstFile);
            input  = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while(pos < size)
            {
                count = (size - pos);
                pos += output.transferFrom(input, pos, count);
            }
        }
        catch(IOException e)
        {
            if(dstFile.exists())
            {
                created.add(dstFile);
            }

            throw this.cleanupFailedCopy(srcFile, dstFile, created.build(), false,
                    "Error occurred during copying the file.", e);
        }
        finally
        {
            //Attempt to close the streams and channels, but if it fails that does not actually mean anything went wrong
            //with copying, so there is no need to do anything about it
            try
            {
                if(output != null)
                {
                    output.close();
                }
                if(fos != null)
                {
                    fos.close();
                }
                if(input != null)
                {
                    input.close();
                }
                if(input != null)
                {
                    fis.close();
                }
            }
            catch(IOException e) { }
        }

        //File has now been created
        created.add(dstFile);

        //If failed to copy the entire file
        if(srcFile.length() != dstFile.length())
        {
            throw this.cleanupFailedCopy(srcFile, dstFile, created.build(), false,
                    "Unable to copy the full content of the file.", null);
        }

        //If requested, set the destination's modified date to that of the source
        if(preserveDate)
        {
            if(!dstFile.setLastModified(srcFile.lastModified()))
            {
                throw this.cleanupFailedCopy(srcFile, dstFile, created.build(), false,
                        "Unable to preserve the modification date.", null);
            }
        }

        //Set the specified group owner
        try
        {
            this.changeGroup(dstFile, groupOwner);
        }
        catch(IOException e)
        {
            throw this.cleanupFailedCopy(srcFile, dstFile, created.build(), false, "Unable to set the group owner.", e);
        }

        //Set the specified permissions
        Set<FilePermission> permissions;
        if(copyPermissions == FileCopyPermissions.READ_WRITE ||
            (copyPermissions == FileCopyPermissions.READ_WRITE_PRESERVE_EXECUTE && !srcFile.canExecute()))
        {
            permissions = READ_WRITE_PERMISSIONS;
        }
        else if(copyPermissions == FileCopyPermissions.READ_WRITE_EXECUTE ||
                 (copyPermissions == FileCopyPermissions.READ_WRITE_PRESERVE_EXECUTE && srcFile.canExecute()))
        {
            permissions = READ_WRITE_EXECUTE_PERMISSIONS;
        }
        //This should never arise, but if another FileCopyPermission enum value was added and this code was not updated
        //then this block could be reached
        else
        {
            throw this.cleanupFailedCopy(srcFile, dstFile, created.build(), false,
                    "Invalid " + FileCopyPermissions.class.getCanonicalName() + ": " + copyPermissions + ".", null);
        }

        try
        {
            this.chmod(dstFile, permissions);
        }
        catch(IOException e)
        {
            throw this.cleanupFailedCopy(srcFile, dstFile, created.build(), false,
                    "Unable to set permissions: " + permissions + ".", e);
        }

        created.add(dstFile);

        return created.build();
    }

    /**
     * Recursively copies {@code srcDir} and all of its contents into {@code dstDir}. Directories will be merged such
     * that if the destination directory or a directory in the destination directory needs to be created and already
     * exist, it will not be deleted. If {@code overwrite} is {@code true} then files may be overwritten in the copying
     * process.
     *
     * @param srcDir directory to copy
     * @param dstDir
     * @param overwrite
     * @param preserveDate
     * @param groupOwner
     * @param copyPermissions
     *
     * @return all files and directories created in performing the copy
     * @throws IOException
     */
    private Set<File> copyDirectory(File srcDir, File dstDir, OverwriteMode overwrite, boolean preserveDate,
            String groupOwner, FileCopyPermissions copyPermissions) throws FileCopyingException
    {
        //Perform validation
        if(!srcDir.exists())
        {
            throw new FileCopyingException(srcDir, dstDir, null, false,
                    "Source directory cannot be copied because it does not exist.");
        }
        if(!srcDir.isDirectory())
        {
            throw new FileCopyingException(srcDir, dstDir, null, false, 
                    "Source is not a directory, this method only copies directories.");
        }

        ImmutableSet.Builder<File> created = ImmutableSet.builder();
        //If the directory exists, that is ok, the contents of the source directory will be merged into the destination
        //directory. However if the destination directory does not exist, create it and record all directories created
        //in the process.
        if(!dstDir.exists())
        {
            //Create directory and parent directories (as needed)
            Set<File> dirsCreated;
            try
            {
                dirsCreated = this.makeDirectory(dstDir, groupOwner);
                created.addAll(dirsCreated);
            }
            catch(DirectoryCreationException e)
            {
                created.addAll(e.getRemainingDirectories());
                throw this.cleanupFailedCopy(srcDir, dstDir, created.build(), false,
                        "Unable to create directory or parent directory.", e);
            }

            if(preserveDate)
            {
                for(File dirCreated : dirsCreated)
                {
                    if(!dirCreated.setLastModified(srcDir.lastModified()))
                    {
                        throw this.cleanupFailedCopy(srcDir, dstDir, dirsCreated, false,
                                "Unable to preserve modification date.", null);
                    }
                }
            }
        }

        //Retrieve contents of the source directory
        File[] srcEntries;
        try
        {
            srcEntries = listFiles(srcDir);
        }
        catch(FilePermissionException e)
        {
            throw this.cleanupFailedCopy(srcDir, dstDir, created.build(), false,
                    "Unable to list contents of source directory", e);
        }
        
        //Copy files and directories inside of this directory
        for(File entry : srcEntries)
        {
            //Build destination path
            String relativePath = entry.getAbsolutePath().replace(srcDir.getAbsolutePath(), "");
            File entryDst = new File(dstDir, relativePath);

            if(entry.isFile())
            {
                try
                {
                    created.addAll(this.copyFile(entry, entryDst, overwrite, preserveDate, groupOwner,
                            copyPermissions));
                }
                catch(FileCopyingException e)
                {
                    throw this.cleanupFailedCopy(srcDir, dstDir, created.build(), e.isFailureDueToExistingFile(),
                            "Unable to copy file contained in directory", e);
                }
            }
            else if(entry.isDirectory())
            {
                try
                {
                    created.addAll(this.copyDirectory(entry, entryDst, overwrite, preserveDate, groupOwner,
                            copyPermissions));
                }
                catch(FileCopyingException e)
                {
                    throw this.cleanupFailedCopy(srcDir, dstDir, created.build(), e.isFailureDueToExistingFile(),
                            "Unable to copy directory contained in directory", e);
                }
            }
        }

        return created.build();
    }

    /**
     * Helper method used by copy methods to delete files if an issue has been encountered in copying. This should be
     * used when a copy has failed and an exception must be thrown, but before doing so all of the files created
     * so far in the copy should be deleted. The exception to be thrown will be generated and its contents will differ
     * depending on the success of deleting the files.
     *
     * @param src
     * @param dst
     * @param toDelete files that need to be deleted
     * @param failedDueToExistingFile 
     * @param message explanation of what went wrong
     * @param cause the reason that the copy is being aborted, may be {@code null}
     *
     * @returns FileCopyingException an exception built from the parameters passed and whether deleting the files
     * succeeded
     */
    private FileCopyingException cleanupFailedCopy(File src, File dst, Set<File> toDelete,
            boolean failedDueToExistingFile, String message, Throwable cause)
    {   
        try
        {
            this.deleteFiles(toDelete);
            return new FileCopyingException(src, dst, null, failedDueToExistingFile, message, cause);
        }
        catch(FileDeletingException e)
        {
            return new FileCopyingException(src, dst, e.getFilesNotDeleted(), failedDueToExistingFile, message, cause);
        }
    }
    
    /******************************************************************************************************************\
    |*                                                   Delete                                                        |  
    \******************************************************************************************************************/
    
    @Override
    public void deleteFiles(Iterable<File> files) throws FileDeletingException
    {
        Set<File> failedToDelete = deleteFilesHelper(files);
        
        if(!failedToDelete.isEmpty())
        {
            throw new FileDeletingException(failedToDelete);
        }
    }
    
    @Override
    public void deleteFilesSilently(Iterable<File> files)
    {
        deleteFilesHelper(files);
    }

    /**
     * Helper method that attempts to delete all the {@code files}. The files which could not be deleted are returned.
     *
     * @param files
     * @return files that could not be deleted
     */
    private static Set<File> deleteFilesHelper(Iterable<File> files)
    {
        ImmutableSet.Builder<File> failedToDelete = ImmutableSet.builder();
        for(File file : files)
        {
            if(!file.exists())
            {
                failedToDelete.add(file);
            }
            else if(file.isFile())
            {
                if(!file.delete())
                {
                    failedToDelete.add(file);
                }
            }
            else if(file.isDirectory())
            {
                //To delete a directory succesfully, all of contents must first be deleted
                File[] entries = file.listFiles();

                //This can occur if the permission to see the directory's entries was not given
                if(entries == null)
                {
                    failedToDelete.add(file);
                }
                else
                {
                    //Recursively delete contents
                    Set<File> nonDeletedEntries = deleteFilesHelper(Arrays.asList(entries));

                    //Delete directory if all contents were deleted
                    if(nonDeletedEntries.isEmpty())
                    {
                        if(!file.delete())
                        {
                            failedToDelete.add(file);
                        }
                    }
                    //Not all contents were deleted, so it will not be possible to delete this directory
                    else
                    {
                        failedToDelete.addAll(nonDeletedEntries);
                        failedToDelete.add(file);
                    }
                }
            }
            else
            {
                failedToDelete.add(file);
            }
        }

        return failedToDelete.build();
    }
    
    @Override
    public void deleteFilesOnExit(Iterable<File> files)
    {
        DeleteFilesOnExitThread.INSTANCE.registerFilesToDeleteOnExit(files);
    }
    
    private static class DeleteFilesOnExitThread extends Thread
    {
        public static final DeleteFilesOnExitThread INSTANCE = new DeleteFilesOnExitThread();
        static
        {
            Runtime.getRuntime().addShutdownHook(INSTANCE);
        }
        
        //A concurrent hash set backed by a concurrent hash map (there is no built-in concurrent hash set)
        private final Set<File> _filesToDelete = Collections.newSetFromMap(new ConcurrentHashMap<File, Boolean>());
        
        @Override
        public void run()
        {
            //Intentionally call the helper which fails by returning the set of files instead of throwing an exception
            //because during JVM termination there's no chance to recover or report
            deleteFilesHelper(_filesToDelete);
        }
        
        public void registerFilesToDeleteOnExit(Iterable<File> files)
        {
            for(File file : files)
            {
                _filesToDelete.add(file);
            }
        }
    }
            
    /******************************************************************************************************************\
    |*                                               Permissions                                                       |  
    \******************************************************************************************************************/
    
    @Override
    public void chmod(File file, Set<FilePermission> permissions) throws IOException
    {
        int permissionsValue = 0;
        for(FilePermission permission : permissions)
        {
            permissionsValue += permission.getValue();
        }
        
        try
        {
            NATIVE_FUNCTIONS.chmod(file, permissionsValue);
        }
        catch(NativeException ex)
        {
            throw new IOException("Unable to change permissions of file\n" +
                    "File: " + file.getAbsolutePath() + "\n" +
                    "Permissions: " + permissions, ex);
        }
    }
    
    @Override
    public void changeGroup(File file, String group) throws IOException
    {
        try
        {
            NATIVE_FUNCTIONS.changeGroup(file, group);
        }
        catch(NativeException ex)
        {
            throw new IOException("Unable to change group owner of file\n" +
                    "File: " + file.getAbsolutePath() + "\n" +
                    "Group Owner: "  + group, ex);
        }
    }
            
    /******************************************************************************************************************\
    |*                                                Filtering                                                        |  
    \******************************************************************************************************************/

    @Override
    public Set<File> getFiles(File file, FileFilter filter) throws FilePermissionException
    {
        GetFilesResult result = getFilesExhaustive(file, filter);
        if(!result.unexplorableDirs.isEmpty())
        {
            throw new FilePermissionException(result.unexplorableDirs, "One or more directories were " +
                    "encountered whose contents could not be accessed");
        }
        
        return ImmutableSet.copyOf(result.acceptedFiles);
    }

    @Override
    public List<File> getFiles(File file, FileFilter filter, Comparator<File> comparator) throws FilePermissionException
    {
        List<File> files = new ArrayList<File>(this.getFiles(file, filter));
        Collections.sort(files, comparator);

        return ImmutableList.copyOf(files);
    }
     
    private static class GetFilesResult
    {
        private final Set<File> acceptedFiles = new HashSet<File>();
        private final Set<File> unexplorableDirs = new HashSet<File>();
    }
    
    /**
     * Recursively searches for all files that are accepted by the {@code filter}. If {@code file} is a directory then
     * its contents will be searched. If a directory's contents cannot be searched then it is added to
     * {@link GetFilesResult#unexplorableDirs} and the search continues.
     * 
     * @param file
     * @param filter
     * @return 
     */
    private GetFilesResult getFilesExhaustive(File file, FileFilter filter)
    {
        GetFilesResult result = new GetFilesResult();
        
        if(filter.accept(file))
        {
            result.acceptedFiles.add(file);
        }
        
        if(file.isDirectory())
        {
            File[] entries = file.listFiles();
            if(entries == null)
            {
                result.unexplorableDirs.add(file);
            }
            else
            {
                for(File entry : entries)
                {
                    GetFilesResult entryResult = this.getFilesExhaustive(entry, filter);
                    result.acceptedFiles.addAll(entryResult.acceptedFiles);
                    result.unexplorableDirs.addAll(entryResult.unexplorableDirs);
                }
            }
        }
        
        return result;
    }
    
    /******************************************************************************************************************\
    |*                                                   Misc                                                          |  
    \******************************************************************************************************************/
    
    @Override
    public Set<File> makeDirectory(File dir, String groupOwner) throws DirectoryCreationException
    {
        ImmutableSet.Builder<File> dirsCreated = ImmutableSet.builder();

        if(dir != null && !dir.exists())
        {
            dirsCreated.addAll(this.makeDirectory(dir.getParentFile(), groupOwner));

            try
            {
                if(!dir.mkdir())
                {
                    //Throw an exception so that the cleanup login in the catch clause is run
                    throw new IOException("Unable to create directory: " + dir.getAbsolutePath());
                }
            
                dirsCreated.add(dir);

                this.chmod(dir, READ_WRITE_EXECUTE_SETGID_PERMISSIONS);
                this.changeGroup(dir, groupOwner);
            }
            catch(IOException cause)
            {
                try
                {
                    //Attempt cleanup
                    this.deleteFiles(dirsCreated.build());
                    
                    throw new DirectoryCreationException(dir, ImmutableSet.<File>of(), cause);
                }
                catch(FileDeletingException e)
                {
                    throw new DirectoryCreationException(dir, e.getFilesNotDeleted(), cause);
                }
            }
        }

        return dirsCreated.build();
    }
    
    @Override
    public String readFile(File file) throws FileNotFoundException, IOException
    {
        StringBuilder text = new StringBuilder();
        BufferedReader input = new BufferedReader(new FileReader(file));
        try
        {
            String line = null;
            while ((line = input.readLine()) != null)
            {
                text.append(line);
                text.append(System.getProperty("line.separator"));
            }
        }
        finally
        {
            input.close();
        }

        return text.toString();
    }
    
    /******************************************************************************************************************\
    |*                                             Shared Helpers                                                      |  
    \******************************************************************************************************************/
    
    /**
     * Calls {@link File#listFiles()} on {@code file} and throws an exception if {@code null} is returned.
     * 
     * @param file
     * @return
     * @throws FileAccessException 
     */
    private File[] listFiles(File file) throws FilePermissionException
    {
        File[] entries = file.listFiles();
        if(entries == null)
        {
            throw new FilePermissionException(ImmutableSet.of(file), "Unable to list contents of directory");
        }
        
        return entries;
    }
}