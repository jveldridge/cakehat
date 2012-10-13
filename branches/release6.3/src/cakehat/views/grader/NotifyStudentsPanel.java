package cakehat.views.grader;

import cakehat.Allocator;
import cakehat.CakehatSession;
import cakehat.assignment.Part;
import cakehat.database.DbPropertyValue;
import cakehat.database.DbPropertyValue.DbPropertyKey;
import cakehat.database.Group;
import cakehat.database.GroupGradingSheet;
import cakehat.database.Student;
import cakehat.email.EmailManager.EmailAccountStatus;
import cakehat.logging.ErrorReporter;
import cakehat.services.EmailGradingTask;
import cakehat.services.ServicesException;
import cakehat.views.grader.PartAndGroupPanel.PartAndGroupSelectionListener;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import support.ui.FormattedLabel;
import support.ui.PreferredHeightJPanel;
import support.ui.ProgressDialog;

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
    
    private final boolean _defaultAttachHandins;
    
    NotifyStudentsPanel(GraderView graderView, PartAndGroupPanel partAndGroupPanel)
    {
        _graderView = graderView;
        _partAndGroupPanel = partAndGroupPanel;
        
        _subjectField = new JTextField();
        _bodyTextArea = new JTextArea();
        _attachHandinCheckBox = new JCheckBox("Attach digital handins");
        _sendButton = new JButton("Send email");
        
        boolean defaultAttachHandins = false;
        try
        {
            DbPropertyValue<Boolean> prop = Allocator.getDatabase()
                    .getPropertyValue(DbPropertyKey.ATTACH_DIGITAL_HANDIN);
            defaultAttachHandins = prop == null ? false : prop.getValue();
        }
        catch(SQLException e)
        {
            defaultAttachHandins = false;
            ErrorReporter.report("Unable to determine default behavior for attaching digital handins, will default " +
                    "to false", e);
        }
        _defaultAttachHandins = defaultAttachHandins;
        
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
        _attachHandinCheckBox.setSelected(_defaultAttachHandins);
        _attachHandinCheckBox.setAlignmentX(LEFT_ALIGNMENT);
        _contentPanel.add(_attachHandinCheckBox);
        
        _contentPanel.add(Box.createVerticalStrut(10));
        
        //Body
        _bodyTextArea.setAlignmentX(LEFT_ALIGNMENT);
        _bodyTextArea.setBorder(BorderFactory.createEtchedBorder());
        _bodyTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                    ImmutableSet.of(KeyStroke.getKeyStroke("pressed TAB")));
        _bodyTextArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                    ImmutableSet.of(KeyStroke.getKeyStroke("shift pressed TAB")));
        //Setting minimum size is needed to work around issue with turning on line wrapping preventing text area from
        //decreasing in size once it has increased in size
        _bodyTextArea.setMinimumSize(new Dimension(0, 0));
        _bodyTextArea.setLineWrap(true);
        _bodyTextArea.setWrapStyleWord(true);
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
            String body = part.getFullDisplayName() + " has been graded.";
            _bodyTextArea.setText(body);
        }
        
        if(_attachHandinCheckBox.isEnabled())
        {
            _attachHandinCheckBox.setSelected(_defaultAttachHandins);
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
        
        for(GroupGradingSheet gradingSheet : unsubmittedGradingSheets)
        {
            _partAndGroupPanel.notifyGradingSheetSubmissionChanged(part, gradingSheet.getGroup(), true);
        }
    }
    
    private void sendEmail(Part part, Set<Group> groups)
    {
        Set<Part> parts = ImmutableSet.of(part);
        Set<Student> students = new HashSet<Student>();
        for(Group group : groups)
        {
            for(Student student : group)
            {
                students.add(student);
            }
        }
        
        EmailGradingTask task = new EmailGradingTask(parts, students, null, _subjectField.getText(),
            _bodyTextArea.getText(), _attachHandinCheckBox.isSelected(), true);

        _backAction.run();
        
        ProgressDialog.show(_graderView, _graderView, "Emailing Grading", task, ErrorReporter.getExceptionReporter());
    }
}