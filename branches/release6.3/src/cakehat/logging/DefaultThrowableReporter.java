package cakehat.logging;

import cakehat.Allocator;
import cakehat.CakehatReleaseInfo;
import cakehat.CakehatSession;
import com.google.common.collect.ImmutableSet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.mail.MessagingException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author jak2
 */
class DefaultThrowableReporter implements ThrowableReporter
{
    @Override
    public boolean report(List<? extends Throwable> causalStack, String msg, boolean useGUI)
    {
        Throwable mostRecentThrowable = causalStack.isEmpty() ? null : causalStack.get(0);
        
        if(useGUI)
        {
            DefaultThrowableView.display(msg, mostRecentThrowable);
        }
        else
        {
            DefaultThrowableCLI.display(msg, mostRecentThrowable);
        }
        
        return true;
    }
    
    /**
     * Returns the {@code throwable}'s stack trace as a string.
     * 
     * @param throwable
     * @return stack trace
     */
    static String getStackTraceAsString(Throwable throwable)
    {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        
        return stringWriter.toString();
    }
    
    /**
     * Emails an error report with the provided arguments.
     * 
     * @param message may be {@code null}
     * @param error may be {@code null}
     * @param userComments may be {@code null}
     */
    static void emailErrorReport(String message, Throwable error, String userComments)
    {
        //Generate subject, include current time to prevent email from threading unrelated exception reports
        //together due to having the same subject
        String subject = "[cakehat] Error Report - " + DateTimeFormat.shortDateTime().print(new DateTime());

        //Generate body with error report information
        StringBuilder body = new StringBuilder("[This is an autogenerated error report]<br/><br/>");
        if(userComments != null && !userComments.isEmpty())
        {
            appendToBody(body, "User Provided Comments", userComments);
        }
        if(message != null && !message.isEmpty())
        {
            appendToBody(body, "Message", message);
        }
        if(error != null)
        {
            appendToBody(body, "Stack Trace", getStackTraceAsString(error));
        }
        appendToBody(body, "Course", CakehatSession.getCourse());
        appendToBody(body, "Run Mode", CakehatSession.getRunMode().toString());
        appendToBody(body, "Developer Mode", Boolean.toString(CakehatSession.isDeveloperMode()));
        appendToBody(body, "Version", CakehatReleaseInfo.getVersion());
        appendToBody(body, "Release Commit Number", CakehatReleaseInfo.getReleaseCommitNumber());
        appendToBody(body, "Release Date", CakehatReleaseInfo.getReleaseDate());
        
        //Email error report
        try
        {
            Allocator.getEmailManager().send(Allocator.getUserServices().getUser().getEmailAddress(),
                                            ImmutableSet.of(Allocator.getEmailManager().getCakehatEmailAddress()),
                                            null,
                                            null,
                                            subject,
                                            body.toString(),
                                            null);
        }
        catch(MessagingException e)
        {
            ErrorReporter.report("Unable to email error report", e);
        }
    }
    
    private static void appendToBody(StringBuilder builder, String title, String value)
    {
        value = value.replace(System.getProperty("line.separator"), "<br/>")
                     .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        builder.append("<strong>").append(title).append("</strong>").append("<br/>").append(value).append("<br/><br/>");
    }
}