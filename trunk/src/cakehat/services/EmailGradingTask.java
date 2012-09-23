package cakehat.services;

import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.assignment.Part;
import cakehat.database.Group;
import cakehat.database.Student;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import support.utils.AlwaysAcceptingFileFilter;
import support.utils.ArchiveUtilities.ArchiveFormat;
import support.utils.LongRunningTask;

/**
 * A task that sends grading sheets and optionally digital handins on an internally managed thread.
 *
 * @author jak2
 */
public class EmailGradingTask extends LongRunningTask
{
    private final Set<Part> _parts;
    private final Assignment _asgn;
    private final Set<Student> _students;
    private final InternetAddress _alternateAddress;
    private final String _subject;
    private final String _body;
    private final boolean _attachHandins;
    private final boolean _notify;
    
    /**
     * Creates a task which emails grading.
     * 
     * @param parts may not be {@code null} or empty. Must all belong to the same assignment. Grading sheet included
     * in email will be for the entire assignment. For each part in {@code parts} if the part has a digital handin then
     * that handin will be attached if {@code attachHandins} is {@code true}.
     * @param students may not be {@code null} or empty. The students for which grading is being sent. If
     * {@code alternateAddress} is {@code null} then individual emails will be sent to each student.
     * @param alternateAddress may be {@code null}. If not {@code null} then instead of sending individual emails to
     * each student in {@code students}, one email will be sent to this address.
     * @param subject may be {@code null}. The subject of the email.
     * @param body may be {@code null}. The body of the email.
     * @param attachHandins if {@code true} then for each part that the student's group has a digital handin for, it
     * will be attached to their email.
     * @param notify if {@code true} an email will be sent to the notify addresses with a summary of the grading sent.
     */
    public EmailGradingTask(Set<Part> parts, Set<Student> students, InternetAddress alternateAddress, String subject,
            String body, boolean attachHandins, boolean notify)
    {
        if(parts == null || parts.isEmpty())
        {
            throw new IllegalArgumentException("parts may not be null or empty");
        }
        Assignment asgn = null;
        for(Part part : parts)
        {
            if(asgn != null && asgn != part.getAssignment())
            {
                throw new IllegalArgumentException("Not all parts belong to the same assignment");
            }
            asgn = part.getAssignment();
        }
        _parts = ImmutableSet.copyOf(parts);
        _asgn = asgn;
        
        if(students == null || students.isEmpty())
        {
            throw new IllegalArgumentException("students may not be null or empty");
        }
        _students = ImmutableSet.copyOf(students);
        
        try
        {
            _alternateAddress = (alternateAddress == null) ? null : new InternetAddress(alternateAddress.getAddress());
        }
        catch(AddressException e)
        {
            throw new IllegalArgumentException("Unable to make copy of alternateAddress", e);
        }
        
        _subject = (subject == null) ? "" : subject;
        _body = (body == null) ? "" : body.replaceAll("\n", "<br>");
        _attachHandins = attachHandins;
        _notify = notify && !Allocator.getEmailManager().getNotifyAddresses().isEmpty();
    }
    
    @Override
    protected void startTask()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    emailGrading();
                }
                catch(RuntimeException e)
                {
                    notifyTaskFailed(e, "Unexpected runtime exception encountered while emailing grading");
                }
                catch(ServicesException e)
                {
                    notifyTaskFailed(e, "Unable to retrieve needed information needed to send email");
                }
                //This isn't a problem state - it is used to signal that the task was cancelled
                catch(InterruptedException ex)
                {
                    notifyTaskCanceled();
                }
            }
        }.start();
    }
    
    private void emailGrading() throws ServicesException, InterruptedException
    {
        //Groups
        Set<Group> groups = new HashSet<Group>();
        
        for(Student student : _students)
        {
            groups.add(Allocator.getDataServices().getGroup(_asgn, student));
        }
        
        checkAttemptCancel();
        
        //Number of steps
        int totalSteps = 1; //generating the grading sheets
        if(_alternateAddress == null)
        {
            totalSteps += _students.size(); //email per student
        }
        else
        {
            totalSteps += 1; //alternate address email
        }
        if(_attachHandins)
        {
            for(Part part : _parts)
            {
                if(part.getGradableEvent().hasDigitalHandins())
                {
                    totalSteps += groups.size(); //digital handin to zip up per group per part
                }
            }
        }
        if(_notify)
        {
            totalSteps += 1; //notify email
        }
        notifyTaskDetermined(totalSteps);
        
        //Grading sheets
        notifyTaskStepStarted("Generating grading sheets");
        Map<Student, String> gradingSheets = Allocator.getGradingServices().generateGRD(_asgn, _students);
        notifyTaskStepCompleted();
        
        checkAttemptCancel();
        
        //Email and track success
        Set<Student> successStudents;
        Set<Student> failStudents;
        boolean alternateAddressSucceeded;
        if(_alternateAddress == null)
        {
            alternateAddressSucceeded = false;
            failStudents = sendStudentsEmail(groups, gradingSheets);
            successStudents = new HashSet<Student>(_students);
            successStudents.removeAll(failStudents);
        }
        else
        {
            alternateAddressSucceeded = sendAlternateAddressEmail(groups, gradingSheets);
            failStudents = new HashSet<Student>();
            successStudents = new HashSet<Student>();
        }
        
        checkAttemptCancel();
        
        //Send a notification email
        boolean notifyEmailSent = false;
        if(_notify && (!successStudents.isEmpty() || (_alternateAddress != null && alternateAddressSucceeded)))
        {
            notifyEmailSent = this.sendNotifyEmail(successStudents);
        }
        
        checkAttemptCancel();
        
        //Report result of actions taken
        this.showResults(successStudents, failStudents, alternateAddressSucceeded, notifyEmailSent);
    }
    
    private Set<Student> sendStudentsEmail(Set<Group> groups, Map<Student, String> gradingSheets)
            throws InterruptedException
    {
        Set<Student> failStudents = new HashSet<Student>();
        
        for(Group group : groups)
        {
            checkAttemptCancel();
            
            Set<DataSource> attachments = new HashSet<DataSource>();
            if(_attachHandins)
            {
                for(Part part : _parts)
                {
                    checkAttemptCancel();

                    notifyTaskStepStarted("Zipping up" + group.getName() + "'s digital handin for " +
                            part.getFullDisplayName());
                    try
                    {
                        DataSource handin = getDigitalHandin(part, group);
                        if(handin != null)
                        {
                            attachments.add(handin);
                        }

                        notifyTaskStepCompleted();
                    }
                    catch(IOException e)
                    {
                        for(Student student : group)
                        {
                            if(_students.contains(student))
                            {
                                failStudents.add(student);
                            }
                        }

                        notifyTaskStepFailed(e, "Unable to zip up digital handin\n" +
                            "Part: " + part.getFullDisplayName() + "\n" +
                            "Group: " + group);
                    }
                }
            }

            for(Student student : group)
            {
                checkAttemptCancel();
            
                if(_students.contains(student) && !failStudents.contains(student))
                {
                    notifyTaskStepStarted("Emailing " + student.getLogin() + " - " + student.getName() + " (" +
                            student.getEmailAddress().getAddress() + ")");
                    
                    StringBuilder messageBuilder = new StringBuilder(_body);
                    messageBuilder.append("<br><br>").append(gradingSheets.get(student));
                    try
                    {
                        Allocator.getEmailManager().send(Allocator.getUserServices().getUser().getEmailAddress(),
                            ImmutableSet.of(student.getEmailAddress()),
                            null,
                            null,
                            _subject,
                            messageBuilder.toString(),
                            attachments);
                        
                        notifyTaskStepCompleted();
                    }
                    catch(MessagingException e)
                    {
                        failStudents.add(student);
                        
                        notifyTaskStepFailed(e, "Unable to send email to " + student.getEmailAddress());
                    }
                    
                }
            }
        }
        
        return failStudents;
    }
    
    
    private boolean sendAlternateAddressEmail(Set<Group> groups, Map<Student, String> gradingSheets)
            throws InterruptedException
    {
        boolean success = true;
        
        Set<DataSource> attachments = new HashSet<DataSource>();
        if(_attachHandins)
        {
            for(Group group : groups)
            {
                checkAttemptCancel();

                for(Part part : _parts)
                {
                    checkAttemptCancel();

                    notifyTaskStepStarted("Zipping up" + group.getName() + "'s digital handin for " +
                            part.getFullDisplayName());

                    try
                    {
                        DataSource handin = getDigitalHandin(part, group);
                        if(handin != null)
                        {
                            attachments.add(handin);
                        }
                        
                        notifyTaskStepCompleted();
                    }
                    catch(IOException e)
                    {
                        success = false;
                        
                        notifyTaskStepFailed(e, "Unable to zip up digital handin\n" +
                            "Part: " + part.getFullDisplayName() + "\n" +
                            "Group: " + group);
                    }
                }
            }
        }

        if(success)
        {
            notifyTaskStepStarted("Emailing " + _alternateAddress.getAddress());
            
            StringBuilder messageBuilder = new StringBuilder(_body);
            for(Student student : _students)
            {
                messageBuilder.append(gradingSheets.get(student));
                messageBuilder.append("<br><hr><br>");
            }
            
            try
            {
                Allocator.getEmailManager().send(Allocator.getUserServices().getUser().getEmailAddress(),
                    ImmutableSet.of(_alternateAddress),
                    null,
                    null,
                    _subject,
                    messageBuilder.toString(),
                    attachments);
            
                notifyTaskStepCompleted();
            }
            catch(MessagingException e)
            {
                success = false;
                notifyTaskFailed(e, "Unable to send email to " + _alternateAddress.getAddress());
            }
        }
        
        return success;
    }
    
    private boolean sendNotifyEmail(Set<Student> successStudents) throws InterruptedException
    {
        notifyTaskStepStarted("Emailing notify addresses");
        
        StringBuilder notifyMessage = new StringBuilder();
        notifyMessage.append("At ")
                .append(DateTimeFormat.shortDateTime().print(DateTime.now()))
                .append(", ")
                .append(Allocator.getUserServices().getUser().getLogin())
                .append(" sent grading sheets ");
        if(_attachHandins)
        {
            notifyMessage.append("and digital handins ");
        }
        notifyMessage.append("for parts:<ul>");
        for(Part part : _parts)
        {
            notifyMessage.append("<li>").append(part.getFullDisplayName()).append("</li>");
        }
        notifyMessage.append("</ul>");

        if(_alternateAddress == null)
        {
            notifyMessage.append("To the following students:<ul>");
            for(Student student : successStudents)
            {
                notifyMessage.append("<li>")
                        .append(student.getLogin())
                        .append(" - ")
                        .append(student.getName())
                        .append(" (")
                        .append(student.getEmailAddress().getAddress())
                        .append(")</li>");
            }
            notifyMessage.append("</ul>");
        }
        else
        {
            notifyMessage.append("Sent to ").append(_alternateAddress.getAddress()).append("<br>");
        }
        
        notifyMessage.append("With the message:<blockquote>").append(_body).append("</blockquote>");

        checkAttemptCancel();
        
        boolean success = true;
        try
        {
            Allocator.getEmailManager().send(Allocator.getUserServices().getUser().getEmailAddress(),
                                             Allocator.getEmailManager().getNotifyAddresses(),
                                             ImmutableSet.of(Allocator.getUserServices().getUser().getEmailAddress()),
                                             null,
                                             "[Notification] " + _subject,
                                             notifyMessage.toString(),
                                             null);
            notifyTaskStepCompleted();
        }
        catch(MessagingException e)
        {
            notifyTaskStepFailed(e, "Unable to send an email to the notify addresses");
            success = false;
        }
        
        return success;
    }
    
    
    private DataSource getDigitalHandin(Part part, Group group) throws IOException, InterruptedException
    {   
        DataSource digitalHandin = null;
            
        //No digital handin may exist - it is valid for a group to not have a handin for gradable event that does have
        //digital handins (for example - student did not turn in the assignment)
        if(part.getGradableEvent().hasDigitalHandin(group))
        {
            //It is possible the group's digital handin has not yet been unarchived (if no action has been taken on it)
            part.unarchive(null, group, true);

            checkAttemptCancel();

            //Create zip attachment of the unarchive directory 
            String archiveName = part.getFullDisplayName() + " [" + group.getName() + "]";
            File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);
            digitalHandin = Allocator.getArchiveUtilities().createArchiveDataSource(archiveName, ArchiveFormat.ZIP,
                    unarchiveDir, new AlwaysAcceptingFileFilter());
        }

        return digitalHandin;
    }
    
    private void showResults(Set<Student> successStudents, Set<Student> failStudents, boolean alternateAddressSucceeded,
            boolean notifyEmailSent)
    {
        StringBuilder sendCompleteMessage = new StringBuilder();
        sendCompleteMessage.append("<h2><font face='dialog'>Email sending complete</font></h2>");
        if(_alternateAddress == null)
        {
            if(!failStudents.isEmpty())
            {
                sendCompleteMessage.append("<font face='dialog'>").append("Failed to send to:").append("<ul>");
                for(Student student : failStudents)
                {
                    sendCompleteMessage.append("<li>")
                            .append(student.getLogin())
                            .append(" - ")
                            .append(student.getName())
                            .append(" (")
                            .append(student.getEmailAddress().getAddress())
                            .append(")")
                            .append("</li>");
                }
                sendCompleteMessage.append("</ul></font>");
            }
            if(!successStudents.isEmpty())
            {
                sendCompleteMessage.append("<font face='dialog'>").append("Succesfully sent to:").append("<ul>");
                for(Student student : successStudents)
                {
                    sendCompleteMessage.append("<li>")
                            .append(student.getLogin())
                            .append(" - ")
                            .append(student.getName())
                            .append(" (")
                            .append(student.getEmailAddress().getAddress())
                            .append(")")
                            .append("</li>");
                }
                sendCompleteMessage.append("</ul></font>");
            }
        }
        else
        {
            if(alternateAddressSucceeded)
            {
                sendCompleteMessage.append("<font face='dialog'>")
                        .append("Succesfully sent to: ")
                        .append(_alternateAddress.getAddress())
                        .append("</font>");
            }
            else
            {
                sendCompleteMessage.append("<font face='dialog'>")
                        .append("Failed to send email to: ")
                        .append(_alternateAddress.getAddress())
                        .append("</font>");
            }
        }
        
        if(_notify)
        {
            sendCompleteMessage.append("<font face='dialog'>").append("<br>");
            if(notifyEmailSent)
            {
                sendCompleteMessage.append("Notification email sent succesfully");
            }
            else
            {
                sendCompleteMessage.append("Failed to send notification email");
            }       
            sendCompleteMessage.append("</font>");
        }
        
        notifyTaskCompleted(sendCompleteMessage.toString());
    }
}