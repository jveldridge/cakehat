package cakehat.logging;

import cakehat.Allocator;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.mail.MessagingException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import support.utils.FilePermissionException;

/**
 * Reports issues with file permissions for files that are not in the .cakehat directory.
 *
 * @author jak2
 */
class FilePermissionExceptionReporter implements ThrowableReporter
{
    @Override
    public boolean report(List<? extends Throwable> causalStack, String msg, boolean useGUI)
    {
        FilePermissionException exception = findReportableException(causalStack);
        System.out.println(exception);
        if(exception != null)
        {
            if(useGUI)
            {
                reportGrapical(exception);
            }
            else
            {
                reportCLI(exception);
            }
        }
        
        return (exception != null);
    }
    
    private void reportCLI(FilePermissionException exception)
    {
        FilePermissionExceptionCLI.display(exception);
    }
    
    private void reportGrapical(FilePermissionException exception)
    {
        FilePermissionExceptionView.display(exception);
    }
    
    private FilePermissionException findReportableException(List<? extends Throwable> causalStack)
    {
        FilePermissionException reportableException = null;
        for(Throwable t : causalStack)
        {
            if(FilePermissionException.class.isAssignableFrom(t.getClass()))
            {
                FilePermissionException permissionException = FilePermissionException.class.cast(t);
                
                //If all files are not in the cakehat directory then the course has a permissions issue
                boolean noFilesInCakehatDir = true;
                for(File file : permissionException.getFiles())
                {
                    if(isInCakehatDirectory(file))
                    {
                        noFilesInCakehatDir = false;
                    }
                }
                
                if(noFilesInCakehatDir)
                {
                    reportableException = permissionException;
                    break;
                }
            }
        }
        
        return reportableException;
    }
    
    private boolean isInCakehatDirectory(File file)
    {
        boolean isInCakehatDir;
        try
        {
            String canonicalCakehatDirPath = Allocator.getPathServices().getCakehatDir().getCanonicalPath();
            String canonicalFilePath = file.getCanonicalPath();
            isInCakehatDir = canonicalFilePath.startsWith(canonicalCakehatDirPath);
        }
        //If unable to resolve canonical paths, then fall back to normal paths
        catch(IOException e)
        {
            String absoluteCakehatDirPath = Allocator.getPathServices().getCakehatDir().getAbsolutePath();
            String absoluteFilePath = file.getAbsolutePath();
            isInCakehatDir = absoluteFilePath.startsWith(absoluteCakehatDirPath);
        }
        
        return isInCakehatDir;
    }
    
    /**
     * Emails a report of the file issues to the HTAs.
     * 
     * @param files may not be {@code null}
     */
    static void sendEmail(Set<File> files)
    {
        //Generate subject, include current time to prevent email from threading unrelated exception reports
        //together due to having the same subject
        String subject = "[cakehat] File Permissions Issue - " + DateTimeFormat.shortDateTime().print(new DateTime());

        //Generate body with file permission information
        StringBuilder body = new StringBuilder();
        body.append("Due to permissions issues, ")
            .append(Allocator.getUserServices().getUser().getLogin())
            .append(" cannot access files that cakehat has been configured by your course to use.")
            .append("<br><br><strong>Files with Incorrect Permissions</strong><br>")
            .append("<ul>");
        for(File file : files)
        {
            body.append("<li>").append(file.getAbsolutePath()).append("</li>");
        }
        body.append("</ul>");
        
        //Email
        try
        {
            Allocator.getEmailManager().send(Allocator.getUserServices().getUser().getEmailAddress(),
                                            ImmutableSet.of(Allocator.getEmailManager().getHeadTAsEmailAddress()),
                                            ImmutableSet.of(Allocator.getUserServices().getUser().getEmailAddress()),
                                            ImmutableSet.of(Allocator.getEmailManager().getCakehatEmailAddress()),
                                            subject,
                                            body.toString(),
                                            null);
        }
        catch(MessagingException e)
        {
            ErrorReporter.report("Unable to email file permissions report", e);
        }
    }
}