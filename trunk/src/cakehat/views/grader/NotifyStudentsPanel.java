package cakehat.views.grader;

import cakehat.Allocator;
import cakehat.CakehatSession;
import cakehat.assignment.Part;
import cakehat.database.Group;
import cakehat.database.GroupGradingSheet;
import cakehat.database.Student;
import cakehat.email.EmailManager.EmailAccountStatus;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import cakehat.views.grader.PartAndGroupPanel.PartAndGroupSelectionListener;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import support.ui.FormattedLabel;
import support.ui.ModalDialog;
import support.ui.PreferredHeightJPanel;
import support.utils.AlwaysAcceptingFileFilter;
import support.utils.ArchiveUtilities.ArchiveFormat;

/**
 *
 * @author jak2
 */
class NotifyStudentsPanel extends JPanel
{
    private final PartAndGroupPanel _partAndGroupPanel;
    private final GraderView _graderView;
    private Runnable _backAction;
    
    private final JPanel _contentPanel;
    
    private final JTextField _subjectField;
    private final JTextArea _bodyTextArea;
    private final JCheckBox _attachHandinCheckBox;
    private final JButton _sendButton;
    
    NotifyStudentsPanel(GraderView graderView, PartAndGroupPanel partAndGroupPanel)
    {
        _graderView = graderView;
        _partAndGroupPanel = partAndGroupPanel;
        
        _subjectField = new JTextField();
        _bodyTextArea = new JTextArea();
        _attachHandinCheckBox = new JCheckBox("Attach student's digital handin");
        _sendButton = new JButton("Send email");
        
        this.setLayout(new BorderLayout(0, 0));
        _contentPanel = new JPanel();
        this.add(_contentPanel, BorderLayout.CENTER);
        this.initUI();
        
        _partAndGroupPanel.addSelectionListener(new PartAndGroupSelectionListener()
        {
            @Override
            public void selectionChanged(Part part, Set<Group> groups)
            {
                populateUI(part, groups);
            }
        });
        this.resetUI();
    }
    
    void setBackAction(Runnable backAction)
    {
        _backAction = backAction;
    }
    
    private void initUI()
    {
        _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
        
        //Header
        _contentPanel.add(FormattedLabel.asHeader("Notify Students"), BorderLayout.NORTH);
        
        _contentPanel.add(Box.createVerticalStrut(5));
        
        //Subject
        _subjectField.setAlignmentX(LEFT_ALIGNMENT);
        _subjectField.setBorder(BorderFactory.createEtchedBorder());
        _subjectField.setMinimumSize(new Dimension(0, 25));
        _subjectField.setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        _subjectField.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
        _contentPanel.add(_subjectField);
        
        //Attach handin
        _attachHandinCheckBox.setAlignmentX(LEFT_ALIGNMENT);
        _contentPanel.add(_attachHandinCheckBox);
        
        _contentPanel.add(Box.createVerticalStrut(10));
        
        //Body
        _bodyTextArea.setAlignmentX(LEFT_ALIGNMENT);
        _bodyTextArea.setBorder(BorderFactory.createEtchedBorder());
        _contentPanel.add(_bodyTextArea);
        
        _contentPanel.add(Box.createVerticalStrut(5));
        
        //Send
        JPanel sendPanel = new PreferredHeightJPanel(new BorderLayout(0, 0), _contentPanel.getBackground());
        sendPanel.add(_sendButton, BorderLayout.EAST);
        _sendButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                sendEmailActionPerformed();
            }
        });
        _contentPanel.add(sendPanel);
    }
    
    final void resetUI()
    {
        Part part = _partAndGroupPanel.getSelectedPart();
        Set<Group> groups = _partAndGroupPanel.getSelectedGroups();
        
        this.populateUI(part, groups);
    }
    
    private void populateUI(Part part, Set<Group> groups)
    {
        _sendButton.setEnabled(Allocator.getEmailManager().getEmailAccountStatus() == EmailAccountStatus.AVAILABLE &&
                part != null && !groups.isEmpty());
        if(Allocator.getEmailManager().getEmailAccountStatus() == EmailAccountStatus.NOT_CONFIGURED)
        {
            _sendButton.setToolTipText("Your course has not configured email sending");
        }
        
        _attachHandinCheckBox.setSelected(false);
        
        if(part == null)
        {
            _subjectField.setText("");
            _bodyTextArea.setText("");
            _attachHandinCheckBox.setEnabled(false);
        }
        else
        {
            _attachHandinCheckBox.setEnabled(part.getGradableEvent().hasDigitalHandins());
            _subjectField.setText("[" + CakehatSession.getCourse() + "] " + part.getFullDisplayName() + " Graded");
            String body = part.getFullDisplayName() + " has been graded." +
                          "\n\n--\n" +
                          Allocator.getUserServices().getUser().getName();
            _bodyTextArea.setText(body);
        }       
    }
    
    private void sendEmailActionPerformed()
    {   
        Part part = _partAndGroupPanel.getSelectedPart();
        Set<Group> groups = _partAndGroupPanel.getSelectedGroups();
        
        try
        {
            this.submitUnsubmittedGroups(part, groups);
            this.sendEmail(part, groups);
        }
        catch(ServicesException e)
        {
            ErrorReporter.report("Unable to submit unsubmitted grading sheets. No email was sent.", e);
        }
    }
    
    private void submitUnsubmittedGroups(Part part, Set<Group> groups) throws ServicesException
    {
        SetMultimap<Part, Group> toRetrieve = HashMultimap.create();
        toRetrieve.putAll(part, groups);
        
        Collection<GroupGradingSheet> gradingSheets = 
                    Allocator.getDataServices().getGroupGradingSheets(toRetrieve).get(part).values();
        
        Set<GroupGradingSheet> unsubmittedGradingSheets = new HashSet<GroupGradingSheet>();
        for(GroupGradingSheet sheet : gradingSheets)
        {
            if(!sheet.isSubmitted())
            {
                unsubmittedGradingSheets.add(sheet);
            }
        }
        Allocator.getDataServices().setGroupGradingSheetsSubmitted(unsubmittedGradingSheets, true);
    }
    
    private void sendEmail(Part part, Set<Group> groups)
    {
        Set<Student> successStudents = new HashSet<Student>();
        Set<Student> failStudents = new HashSet<Student>();
        
        for(Group group : groups)
        {
            boolean proceed = true;
            Set<DataSource> attachments = new HashSet<DataSource>();
            if(_attachHandinCheckBox.isSelected())
            {
                File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);
                //No unarchive dir may exist - it is valid for a group to not have a handin for gradable event that
                //does have digital handins (for example - student did not turn in the assignment)
                if(unarchiveDir.exists())
                {
                   try
                    {
                        DataSource digitalHandin = Allocator.getArchiveUtilities().createArchiveDataSource(
                                ArchiveFormat.ZIP, unarchiveDir, new AlwaysAcceptingFileFilter());
                        attachments.add(digitalHandin);
                    }
                    catch(IOException e)
                    {
                        proceed = false;
                        failStudents.addAll(group.getMembers());
                        ErrorReporter.report("Unable to zip up digital handin\n" +
                                "Part: " + part.getFullDisplayName() + "\n" +
                                "Group: " + group + "\n" +
                                "Unarchive Dir: " + unarchiveDir.getAbsolutePath(), e);
                    } 
                }
            }
            
            if(proceed)
            {   
                for(Student student : group)
                {
                    try
                    {
                        Allocator.getEmailManager().send(Allocator.getUserServices().getUser().getEmailAddress(), //from
                            ImmutableSet.of(student.getEmailAddress()), //to
                            null, //cc
                            null, //bcc
                            _subjectField.getText(), //subject
                            _bodyTextArea.getText(), //body
                            attachments); //attachments
                        successStudents.add(student);
                    }
                    catch(MessagingException e)
                    {
                        failStudents.add(student);
                        ErrorReporter.report("Unable to send email to " + student.getEmailAddress(), e);
                    }
                }
            }
        }
        
        //Show confirmation of email sending
        StringBuilder sendCompleteMessage = new StringBuilder("Email sending complete\n\n");
        if(!successStudents.isEmpty())
        {
            sendCompleteMessage.append("Succesfully sent to:\n");
            for(Student student : successStudents)
            {
                sendCompleteMessage.append(student.getLogin());
                sendCompleteMessage.append(" - ");
                sendCompleteMessage.append(student.getName());
                sendCompleteMessage.append(" (");
                sendCompleteMessage.append(student.getEmailAddress().getAddress());
                sendCompleteMessage.append(")\n");
            }
        }
        if(!failStudents.isEmpty())
        {
            sendCompleteMessage.append("Failed to send to:\n");
            for(Student student : failStudents)
            {
                sendCompleteMessage.append(student.getLogin());
                sendCompleteMessage.append(" - ");
                sendCompleteMessage.append(student.getName());
                sendCompleteMessage.append(" (");
                sendCompleteMessage.append(student.getEmailAddress().getAddress());
                sendCompleteMessage.append(")\n");
            }
        }
        ModalDialog.showMessage(null, "Email Sent", sendCompleteMessage.toString());
        _backAction.run();
    }
}