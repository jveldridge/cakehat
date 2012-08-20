package support.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Exception that represents a failure to copy a {@link java.io.File}.
 *
 * @author jak2
 */
public class FileCopyingException extends IOException
{
    private final Set<File> _remainingFiles;
    
    private final boolean _failedDueToExistingFile;
        
    //The source and destination involved in the failure of the copy
    private final File _src, _dst;

    public FileCopyingException(File src, File dst, Set<File> remainingFiles, boolean failedDueToExistingFile,
            String message)
    {
        this(src, dst, remainingFiles, failedDueToExistingFile, message, null);
    }

    public FileCopyingException(File src, File dst, Set<File> remainingFiles, boolean failedDueToExistingFile, 
            String message, Throwable cause)
    {
        super(buildMessage(message, remainingFiles, src, dst), cause);

        _remainingFiles = remainingFiles == null ? ImmutableSet.<File>of() : ImmutableSet.copyOf(remainingFiles);
        
        _failedDueToExistingFile = failedDueToExistingFile;
        
        _src = src;
        _dst = dst;
    }
    
    private static String buildMessage(String beginningMessage, Set<File> remainingFiles, File src, File dst)
    {
        StringBuilder messageBuilder = new StringBuilder(beginningMessage);
        
        messageBuilder.append("\n");
        messageBuilder.append("Source: ");
        messageBuilder.append(src.getAbsolutePath());
        messageBuilder.append("\n");
        messageBuilder.append("Destination: ");
        messageBuilder.append(dst.getAbsolutePath());
        messageBuilder.append("\n");
        
        if(remainingFiles == null || remainingFiles.isEmpty())
        {
            messageBuilder.append("No files and/or directories created by this failed copy remain.");
        }
        else
        {
            messageBuilder.append("Some files and/or directories created by this failed copy remain:");
            for(File remainingFile : remainingFiles)
            {
                messageBuilder.append("\n");
                messageBuilder.append(remainingFile.getAbsolutePath());
            }
        }
        
        return messageBuilder.toString();
    }
    
    /**
     * If a copy fails for any reason, an attempt will be made to delete all files created as a result of the copy. This
     * attempt may not necessarily succeed. If {@code false} is returned then all files and directories created as a
     * result of the failed copy no longer exist. It may be treated as if the copy never occurred. If {@code true} is
     * returned then some number of directories and files (including possibly an incomplete file) from the copy remain.
     *
     * @return
     */
    public boolean isPartialCopy()
    {
        return !_remainingFiles.isEmpty();
    }
    
    /**
     * The files and directories that remain from the failed copy. This will be an empty set if {@link #isPartialCopy()}
     * return {@code false}.
     * 
     * @return 
     */
    public Set<File> getRemainingFiles()
    {
        return _remainingFiles;
    }
    
    /**
     * The source file that failed to be copied.
     * 
     * @return 
     */
    public File getSourceFile()
    {
        return _src;
    }

    /**
     * The destination of where the source file was to be copied. This file may or may not exist on disk, and it if does
     * exist it may not be be a valid copy of the file at the source.
     * 
     * @return 
     */
    public File getDestinationFile()
    {
        return _dst;
    }
    
    /**
     * If the file copy could not succeed because copying would overwrite the destination file and overwrite permission
     * was not granted.
     * 
     * @return 
     */
    public boolean isFailureDueToExistingFile()
    {
        return _failedDueToExistingFile;
    }
    
    /**
     * The source file that could not be copied because it would have overwritten an existing file. This file may not
     * be the same as {@link #getSourceFile()} if the cause of this exception is another {@link FileCopyingException}.
     * 
     * @return 
     */
    public File getSourceFileForFailureDueToExistingFile()
    {
        FileCopyingException cause = getCauseForFailureDueToExistingFile();
                
        return cause == null ? null : cause.getSourceFile();
    }
        
    /**
     * The destination file that could not be overwritten with a file to be copied. This file may not be the same as
     * {@link #getDestinationFile()} if the cause of this exception is another {@link FileCopyingException}.
     * 
     * @return 
     */
    public File getDestinationFileForFailureDueToExistingFile()
    {
        FileCopyingException cause = getCauseForFailureDueToExistingFile();
                
        return cause == null ? null : cause.getDestinationFile();
    }

    /**
     * Helper method that finds the exception that is the cause for the copy failure due to an existing file.
     * 
     * @return 
     */
    private FileCopyingException getCauseForFailureDueToExistingFile()
    {
        FileCopyingException failureCause = null;
        
        if(this.isFailureDueToExistingFile())
        {
            //Search the cause change to find the nearest FileCopyingException that failued due to an existing
            failureCause = this;
            for(Throwable cause = this.getCause(); cause != null; cause = cause.getCause())
            {
                if(cause instanceof FileCopyingException && ((FileCopyingException) cause).isFailureDueToExistingFile())
                {
                    failureCause = (FileCopyingException) cause;
                }
                else
                {
                    break;
                }
            }
        }
        
        return failureCause;
    }
}