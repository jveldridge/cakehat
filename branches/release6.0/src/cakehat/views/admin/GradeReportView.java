//package cakehat.views.admin;
//
//import cakehat.Allocator;
//import cakehat.CakehatSession;
//import cakehat.database.DeadlineInfo;
//import cakehat.database.DeadlineInfo.DeadlineResolution;
//import cakehat.database.Extension;
//import cakehat.database.Group;
//import cakehat.database.PartGrade;
//import cakehat.database.Student;
//import cakehat.assignment.Assignment;
//import cakehat.assignment.GradableEvent;
//import cakehat.assignment.Part;
//import cakehat.email.EmailManager;
//import cakehat.logging.ErrorReporter;
//import cakehat.services.ServicesException;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.ImmutableSet;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.EventQueue;
//import java.awt.Point;
//import java.awt.Window;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import javax.mail.MessagingException;
//import javax.mail.internet.AddressException;
//import javax.mail.internet.InternetAddress;
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.ButtonGroup;
//import javax.swing.JButton;
//import javax.swing.JDialog;
//import javax.swing.JEditorPane;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JRadioButton;
//import javax.swing.JScrollPane;
//import javax.swing.JTextArea;
//import javax.swing.JTextField;
//import javax.swing.ListSelectionModel;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;
//import javax.swing.text.BadLocationException;
//import javax.swing.text.html.HTMLEditorKit;
//import org.joda.time.DateTime;
//import support.ui.DescriptionProvider;
//import support.ui.DocumentAdapter;
//import support.ui.FormattedLabel;
//import support.ui.GenericJComboBox;
//import support.ui.GenericJList;
//import support.ui.ProgressDialog;
//import support.ui.SelectionListener;
//import support.ui.SelectionListener.SelectionAction;
//import support.utils.LongRunningTask;
//
///**
// * A view that allows for sending student grade reports. Reports may either be sent to the students or an alternative
// * email address may be specified and then the reports will be sent to that address instead. The notify addresses will
// * be sent an email informing them that grade reports have been sent.
// * <br/><br/>
// * Because sending an email can take roughly one second per email, grade reports are sent on a separate thread so that
// * the user is not blocked.
// * 
// * @author jak2
// */
//class GradeReportView extends JDialog
//{
//    private static GradeReportView _currentlyDisplayedView;
//    
//    /**
//     * Displays this view relative to {@code owner} which may be {@code null}. If this view is already displayed it
//     * will be brought to the front. If the view is not displayed it will be owned to {@code owner}, otherwise ownership
//     * will not change.
//     * 
//     * @param owner 
//     */
//    static void display(Window owner)
//    {
//        if(_currentlyDisplayedView != null)
//        {
//            _currentlyDisplayedView.toFront();
//        }
//        else
//        {
//            try
//            {
//                _currentlyDisplayedView = new GradeReportView(owner);
//            }
//            catch(IOException ex)
//            {
//                ErrorReporter.report("Failed to retrieve data needed by this view", ex);
//                return;
//            }
//            catch(ServicesException ex)
//            {
//                ErrorReporter.report("Failed to retrieve data needed by this view", ex);
//                return;
//            }
//            
//            //null out the view on close
//            _currentlyDisplayedView.addWindowListener(new WindowAdapter()
//            {
//                @Override
//                public void windowClosing(WindowEvent e)
//                {
//                    _currentlyDisplayedView = null;
//                }
//            });
//        }
//        _currentlyDisplayedView.setLocationRelativeTo(owner);
//        _currentlyDisplayedView.setVisible(true);
//    }
//    
//    //All of the data needed to generate reports
//    private final Map<Assignment, Map<Student, Group>> _groups;
//    private final Map<Part, Map<Group, PartGrade>> _partGrades;
//    private final Map<GradableEvent, DeadlineInfo> _deadlines;
//    private final Map<GradableEvent, Map<Group, DateTime>> _occurrenceDates;
//    private final Map<GradableEvent, Map<Group, Extension>> _extensions;
//    
//    //Key visual elements
//    private final AssignmentList _asgnList;
//    private final StudentList _studentList;
//    private final JTextArea _messageArea;
//    private final GenericJComboBox<Student> _studentsComboBox;
//    private final JEditorPane _previewPane;
//    private final JScrollPane _previewScrollPane;
//    private final EmailAddressField _alternateAddressField;
//    private final JButton _sendReportsButton;
//    
//    private GradeReportView(Window owner) throws ServicesException, IOException
//    {
//        super(owner, "Grade Reports", ModalityType.MODELESS);
//        
//        //Load data into thread safe data structures (reports will be sent on a separate thread)
//        ImmutableMap.Builder<Assignment, Map<Student, Group>> groupsBuilder = ImmutableMap.builder();
//        ImmutableMap.Builder<Part, Map<Group, PartGrade>> partGradesBuilder = ImmutableMap.builder();
//        ImmutableMap.Builder<GradableEvent, DeadlineInfo> deadlinesBuilder = ImmutableMap.builder();
//        ImmutableMap.Builder<GradableEvent, Map<Group, DateTime>> occurrenceDatesBuilder = ImmutableMap.builder();
//        ImmutableMap.Builder<GradableEvent, Map<Group, Extension>> extensionsBuilder = ImmutableMap.builder();
//        for(Assignment asgn : Allocator.getDataServices().getAssignments())
//        {
//            Set<Group> groups = Allocator.getDataServices().getGroups(asgn);
//            
//            //Students to groups
//            ImmutableMap.Builder<Student, Group> studentToGroupBuilder = ImmutableMap.builder();
//            for(Group group : groups)
//            {
//                for(Student student : group)
//                {
//                    studentToGroupBuilder.put(student, group);
//                }
//            }
//            groupsBuilder.put(asgn, studentToGroupBuilder.build());
//            
//            for(GradableEvent ge : asgn)
//            {
//                //Deadlines
//                deadlinesBuilder.put(ge, Allocator.getDataServices().getDeadlineInfo(ge));
//                
//                //Occurrence dates
//                occurrenceDatesBuilder.put(ge, ImmutableMap.copyOf(Allocator.getGradingServices().getOccurrenceDates(ge, groups)));
//                
//                //Extensions
//                extensionsBuilder.put(ge, ImmutableMap.copyOf(Allocator.getDataServices().getExtensions(ge, groups)));
//                
//                //Part grades
//                for(Part part : ge)
//                {
//                    partGradesBuilder.put(part, ImmutableMap.copyOf(Allocator.getDataServices().getEarned(groups, part)));
//                }
//            }
//        }
//        _groups = groupsBuilder.build();
//        _partGrades = partGradesBuilder.build();
//        _deadlines = deadlinesBuilder.build();
//        _occurrenceDates = occurrenceDatesBuilder.build();
//        _extensions = extensionsBuilder.build();
//        
//        //Intialize UI
//        _asgnList = new AssignmentList();
//        _studentList = new StudentList();
//        _studentsComboBox = new GenericJComboBox<Student>();
//        _previewPane = new JEditorPane();
//        _previewScrollPane = new JScrollPane();
//        _messageArea = new JTextArea();
//        _alternateAddressField = new EmailAddressField();
//        _sendReportsButton = new JButton();
//        this.initUI();
//        
//        //Display
//        this.setMinimumSize(new Dimension(825, 450));
//        this.setPreferredSize(new Dimension(825, 450));
//        this.pack();
//        this.setResizable(true);
//        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//        
//        //Start with focus in the message area
//        EventQueue.invokeLater(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                _messageArea.grabFocus();
//            }
//        });
//    }
//    
//    private void initUI()
//    {
//        //Visual setup
//        this.setLayout(new BorderLayout(0, 0));
//        JPanel contentPanel = new JPanel();
//        this.add(contentPanel, BorderLayout.CENTER);
//        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
//        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
//        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
//        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
//        
//        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
//        
//        //Assignments
//        Dimension assignmentListSize = new Dimension(160, Short.MAX_VALUE);
//        _asgnList.setMinimumSize(assignmentListSize);
//        _asgnList.setPreferredSize(assignmentListSize);
//        _asgnList.setMaximumSize(assignmentListSize);
//        _asgnList.getList().addListSelectionListener(new ListSelectionListener()
//        {
//            @Override
//            public void valueChanged(ListSelectionEvent lse)
//            {
//                if(!lse.getValueIsAdjusting())
//                {
//                    notifyAssignmentSelectionChanged(_asgnList.getList().getGenericSelectedValues());
//                }
//            }
//        });
//        contentPanel.add(_asgnList);
//        
//        contentPanel.add(Box.createHorizontalStrut(5));
//        
//        //Students
//        Dimension studentListSize = new Dimension(140, Short.MAX_VALUE);
//        _studentList.setMinimumSize(studentListSize);
//        _studentList.setPreferredSize(studentListSize);
//        _studentList.setMaximumSize(studentListSize);
//        _studentList.addSelectionListener(new StudentList.StudentListListener()
//        {
//            @Override
//            public void selectionChanged(List<Student> selection)
//            {
//                notifyStudentListSelectionChanged(selection);
//            }
//        });
//        contentPanel.add(_studentList);
//        
//        contentPanel.add(Box.createHorizontalStrut(5));
//        
//        //Right most panel which holds the message and report preview
//        JPanel rightPanel = new JPanel();
//        contentPanel.add(rightPanel);
//        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
//        
//        rightPanel.add(FormattedLabel.asHeader("Message"));
//        
//        rightPanel.add(Box.createVerticalStrut(5));
//        
//        _messageArea.setRows(4);
//        _messageArea.setLineWrap(true);
//        _messageArea.setWrapStyleWord(true);
//        _messageArea.getDocument().addDocumentListener(new DocumentAdapter()
//        {
//            @Override
//            public void modificationOccurred(DocumentEvent de)
//            {
//                try
//                {   
//                    String text = de.getDocument().getText(0, de.getDocument().getLength());
//                    notifyMessageChanged(text);
//                }
//                catch(BadLocationException e) { }
//            }
//        });
//        JScrollPane commentScrollPane = new JScrollPane(_messageArea);
//        commentScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 63));
//        commentScrollPane.setMinimumSize(new Dimension(0, 63));
//        commentScrollPane.setAlignmentX(LEFT_ALIGNMENT);
//        rightPanel.add(commentScrollPane);
//        
//        //Vertical spacing between message and preview
//        rightPanel.add(Box.createVerticalStrut(10));
//        
//        //Preview
//        rightPanel.add(FormattedLabel.asHeader("Grade Report Preview"));
//        
//        rightPanel.add(Box.createVerticalStrut(5));
//        
//        JPanel previewForPanel = new JPanel()
//        {
//            @Override
//            public Dimension getMaximumSize()
//            {
//                Dimension size = getPreferredSize();
//                size.width = Short.MAX_VALUE;
//
//                return size;
//            }
//        };
//        previewForPanel.setLayout(new BoxLayout(previewForPanel, BoxLayout.X_AXIS));
//        previewForPanel.setAlignmentX(LEFT_ALIGNMENT);
//        previewForPanel.add(new JLabel("Preview for"));
//        previewForPanel.add(Box.createHorizontalStrut(3));
//        _studentsComboBox.setMaximumSize(new Dimension(103, 20));
//        _studentsComboBox.setDescriptionProvider(new DescriptionProvider<Student>()
//        {
//            @Override
//            public String getDisplayText(Student item)
//            {
//                String display;
//                if(item == null)
//                {
//                    display = "Template";
//                }
//                else
//                {
//                    display = item.getLogin();
//                }
//                
//                return display;
//            }
//
//            @Override
//            public String getToolTipText(Student item)
//            {
//                String toolTip;
//                if(item == null)
//                {
//                    toolTip = "Template";
//                }
//                else
//                {
//                    toolTip = item.getName();
//                }
//                
//                return toolTip;
//            }
//        });
//        _studentsComboBox.addSelectionListener(new SelectionListener<Student>()
//        {
//            @Override
//            public void selectionPerformed(Student currValue, Student newValue, SelectionAction action)
//            {
//                notifyStudentComboBoxSelectionChanged(newValue);
//            }
//        });
//        HashSet<Student> nullStudentSet = new HashSet<Student>();
//        nullStudentSet.add(null);
//        _studentsComboBox.setItems(nullStudentSet);
//        _studentList.addSelectionListener(new StudentList.StudentListListener()
//        {
//            @Override
//            public void selectionChanged(List<Student> selection)
//            {
//                ArrayList<Student> students = new ArrayList<Student>();
//                students.add(null);
//                students.addAll(selection);
//                _studentsComboBox.setItems(students);
//            }
//        });
//        previewForPanel.add(_studentsComboBox);
//        rightPanel.add(previewForPanel);
//        
//        rightPanel.add(Box.createVerticalStrut(5));
//        
//        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
//        _previewPane.setEditable(false);
//        _previewPane.setEditorKit(htmlEditorKit);
//        _previewPane.setDocument(htmlEditorKit.createDefaultDocument());
//        _previewScrollPane.setViewportView(_previewPane);
//        _previewScrollPane.setAlignmentX(LEFT_ALIGNMENT);
//        rightPanel.add(_previewScrollPane);
//        
//        //Vertical spacing between message and preview
//        rightPanel.add(Box.createVerticalStrut(5));
//        
//        //Send
//        ButtonGroup sendGroup = new ButtonGroup();
//        JRadioButton sendToStudentsButton = new JRadioButton("Send to students");
//        sendGroup.add(sendToStudentsButton);
//        sendToStudentsButton.setSelected(true);
//        rightPanel.add(sendToStudentsButton);
//        
//        JPanel sendPanel = new JPanel(new BorderLayout(5, 0));
//        sendPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
//        sendPanel.setAlignmentX(LEFT_ALIGNMENT);
//        rightPanel.add(sendPanel);
//        
//        final JRadioButton sendToAlternateAddressButton = new JRadioButton("Send to an alternate address");
//        sendGroup.add(sendToAlternateAddressButton);
//        sendToAlternateAddressButton.addChangeListener(new ChangeListener()
//        {
//            @Override
//            public void stateChanged(ChangeEvent ce)
//            {
//                _alternateAddressField.setEnabled(sendToAlternateAddressButton.isSelected());
//                updateSendButtonState(_studentList.getList().getSelectionSize());
//            }
//        });
//        sendPanel.add(sendToAlternateAddressButton, BorderLayout.WEST);
//        
//        _alternateAddressField.getDocument().addDocumentListener(new DocumentAdapter()
//        {
//            @Override
//            public void modificationOccurred(DocumentEvent de)
//            {
//                updateSendButtonState(_studentList.getList().getSelectionSize());
//            }
//        });
//        _alternateAddressField.setEnabled(false);
//        sendPanel.add(_alternateAddressField, BorderLayout.CENTER);
//        
//        updateSendButtonState(0);
//        _sendReportsButton.addActionListener(new ActionListener()
//        {
//            @Override
//            public void actionPerformed(ActionEvent ae)
//            {
//                notifySendReports();
//            }
//        });
//        sendPanel.add(_sendReportsButton, BorderLayout.EAST);
//        
//        
//        //Set the initial text into the message area - which will generate the initial preview
//        _messageArea.setText("Below are your current grades for " + CakehatSession.getCourse());
//    }
//    
//    /**
//     * Called when the text in the message text area changes.
//     * 
//     * @param message 
//     */
//    private void notifyMessageChanged(String message)
//    {
//        showPreview(message, _studentsComboBox.getSelectedItem(), _asgnList.getList().getGenericSelectedValues());
//    }
//    
//    /**
//     * Called when a check box is checked or unchecked in the assignment checklist.
//     * 
//     * @param assignments 
//     */
//    private void notifyAssignmentSelectionChanged(List<Assignment> assignments)
//    {
//        showPreview(_messageArea.getText(), _studentsComboBox.getSelectedItem(), assignments);
//    }
//    
//    /**
//     * Called when selection in the student combo box changes.
//     * 
//     * @param student 
//     */
//    private void notifyStudentComboBoxSelectionChanged(Student student)
//    {
//        showPreview(_messageArea.getText(), student, _asgnList.getList().getGenericSelectedValues());
//    }
//    
//    /**
//     * Called when selection in the student list changes.
//     * 
//     * @param students 
//     */
//    private void notifyStudentListSelectionChanged(List<Student> students)
//    {
//        updateSendButtonState(students.size());
//    }
//    
//    private void updateSendButtonState(int numSelectedStudents)
//    {
//        _sendReportsButton.setText(numSelectedStudents == 1 ? "Send Report" : "Send Reports");
//        
//        //Set button enabled state based on if the email account is configured and initialized
//        if(Allocator.getEmailManager().getEmailAccountStatus() == EmailManager.EmailAccountStatus.NOT_CONFIGURED)
//        {
//            _sendReportsButton.setEnabled(false);
//            _sendReportsButton.setToolTipText("Email has not been configured by your course");
//        }
//        else if(_alternateAddressField.isEnabled() && !_alternateAddressField.isValidEmailAddress())
//        {
//            _sendReportsButton.setEnabled(false);
//            _sendReportsButton.setToolTipText("Alternate email address is not valid");
//        }
//        else
//        {
//            _sendReportsButton.setEnabled(true);
//            _sendReportsButton.setToolTipText(null);
//        }
//        
//        //If the button is enabled, but no students are selected - disable it
//        if(_sendReportsButton.isEnabled() && numSelectedStudents == 0)
//        {
//            _sendReportsButton.setEnabled(false);
//            _sendReportsButton.setToolTipText("No students are selected");
//        }
//    }
//    
//    private void showPreview(String message, Student student, List<Assignment> assignments)
//    {
//        //Update the preview while maintaining the scroll offset
//        int initialOffset = _previewScrollPane.getVerticalScrollBar().getValue();
//        
//        _previewPane.setText(buildGradeReport(message, student, assignments));
//        
//        int maxOffset = _previewScrollPane.getVerticalScrollBar().getMaximum();
//        final int newOffset = initialOffset > maxOffset ? maxOffset : initialOffset;
//        EventQueue.invokeLater(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                _previewScrollPane.getViewport().setViewPosition(new Point(0, newOffset));
//            }
//        });
//    }
//    
//    /**
//     * Called when the send reports button is clicked.
//     */
//    private void notifySendReports()
//    {
//        sendReports(_messageArea.getText(), _studentList.getSelection(), _asgnList.getList().getGenericSelectedValues(),
//                _alternateAddressField.getAddress());
//    }
//    
//    private class SendReportsTask extends LongRunningTask
//    {
//        private final List<Student> _students;
//        private final List<Assignment> _assignments;
//        private final String _message;
//        private final InternetAddress _alternateAddress;
//        
//        private SendReportsTask(List<Student> students, List<Assignment> assignments, String message,
//                InternetAddress alternateAddress)
//        {
//            _students = students;
//            _assignments = assignments;
//            _message = message;
//            _alternateAddress = alternateAddress;
//        }
//        
//        @Override
//        protected void startTask()
//        {
//            new Thread()
//            {
//                @Override
//                public void run()
//                {
//                    Set<InternetAddress> notifyAddresses = Allocator.getEmailManager().getNotifyAddresses();
//                    
//                    notifyTaskDetermined(_students.size() + (notifyAddresses.isEmpty() ? 0 : 1));
//                    
//                    Map<Student, MessagingException> reportsFailedToSend = new HashMap<Student, MessagingException>();
//                    
//                    InternetAddress from = Allocator.getEmailManager().getHeadTAsEmailAddress();
//                    String subject = CakehatSession.getCourse() + " Grade Report";
//                    for(Student student : _students)
//                    {
//                        notifyTaskStepStarted("Emailing " + student.getName());
//                                
//                        Iterable<InternetAddress> to;
//                        if(_alternateAddress == null)
//                        {
//                            to = ImmutableSet.of(student.getEmailAddress());
//                        }
//                        else
//                        {
//                            to = ImmutableSet.of(_alternateAddress);
//                        }
//
//                        try
//                        {
//                            Allocator.getEmailManager().send(from,
//                                                             to,
//                                                             null,
//                                                             null,
//                                                             subject,
//                                                             buildGradeReport(_message, student, _assignments),
//                                                             null);
//                        }
//                        catch(MessagingException e)
//                        {
//                            reportsFailedToSend.put(student, e);
//                        }
//
//                        notifyTaskStepCompleted();
//                        if(isCancelAttempted())
//                        {
//                            break;
//                        }
//                    }
//
//                    //If some report could not be sent
//                    if(!reportsFailedToSend.isEmpty())
//                    {
//                        Exception e = reportsFailedToSend.values().iterator().next();
//                        notifyTaskFailed(e, "One or more grade reports failed " +
//                                "send. The stack trace for one of those failures is shown. Failed for students:\n" +
//                                reportsFailedToSend.keySet());
//                    }
//
//                    //Send email to the notify addresses letting them know grade reports were sent
//                    if(!isCancelAttempted() && !notifyAddresses.isEmpty())
//                    {
//                        notifyTaskStepStarted("Emailing notify addresses");
//                        
//                        String body = "Grade reports were sent by " + Allocator.getUserServices().getUser().getName() + 
//                                      "<br/><br/>Message was:<br/>" +
//                                      _message.replace("\n", "<br/>") +
//                                      "<br/><br/>Reports were sent for the following students:<br/><ul>";
//                        for(Student student : _students)
//                        {
//                            body += "<li>";
//                            body += student.getName();
//                            body += " (";
//                            if(_alternateAddress == null)
//                            {
//                                body += student.getEmailAddress().getAddress();
//                            }
//                            else
//                            {
//                                body += _alternateAddress.getAddress();
//                            }
//                            body += ")";
//
//                            if(reportsFailedToSend.containsKey(student))
//                            {
//                                body += " <font color='#FF0000'>[Failed to send]</font>";
//                            }
//
//                            body += "</li>";
//                        }
//                        body += "</ul>";
//
//                        try
//                        {
//                            Allocator.getEmailManager().send(Allocator.getUserServices().getUser().getEmailAddress(),
//                                                             notifyAddresses,
//                                                             null,
//                                                             null,
//                                                             "Grade Reports Sent",
//                                                             body,
//                                                             null);
//                        }
//                        catch(MessagingException e)
//                        {
//                            notifyTaskFailed(e, "Unable to send notification of grade report to the notify " +
//                                    "addresses:\n" + notifyAddresses);
//                        }
//
//                        notifyTaskStepCompleted();
//                    }
//                    
//                    if(isCancelAttempted())
//                    {
//                        notifyTaskCanceled();
//                    }
//                    else
//                    {
//                        notifyTaskCompleted();
//                    }
//                }
//            }.start();
//        }
//
//        @Override
//        protected void cancelTask() { }
//    }
//    
//    /**
//     * Emails out the grading reports with message {@code message} for the specified {@code students} and
//     * {@code assignments}. If {@code alternateAddress} is not {@code null} then all emails will be sent to that address
//     * instead of student email addresses.
//     * <br/><br/>
//     * Emails are sent on a separate thread because sending the emails takes a while, often in excess of 1 second per
//     * email.
//     * 
//     * @param message
//     * @param students
//     * @param assignments 
//     * @param alternateAddress may be {@code null}
//     */
//    private void sendReports(String message, List<Student> students, List<Assignment> assignments,
//            InternetAddress alternateAddress)
//    {
//        ProgressDialog.ExceptionReporter excReporter = new ProgressDialog.ExceptionReporter()
//        {
//            @Override
//            public void report(String message, Exception exception)
//            {
//                ErrorReporter.report(message, exception);
//            }
//        };
//        SendReportsTask sendTask = new SendReportsTask(students, assignments, message, alternateAddress);
//        
//        //Own the progress dialog to same owner as this window - if we instead owned the dialog to this window then it
//        //would close when this window closes - which will be done right after creating the dialog
//        ProgressDialog.show(this.getOwner(), this, "Sending Grade Reports",
//                "<html><center><h2>Sending student grade reports</h2></center></html>", sendTask, excReporter);
//        
//        this.dispose();
//    }
//    
//    private String buildGradeReport(String message, Student student, List<Assignment> assignments)
//    {
//        StringBuilder reportBuilder = new StringBuilder();
//        
//        //Top portion with course, student, and message text
//        reportBuilder.append("<body style='font-family: sans-serif; font-size: 10pt'>");
//        reportBuilder.append("<h1 style='font-weight: bold; font-size:11pt'>");
//        reportBuilder.append(CakehatSession.getCourse());
//        reportBuilder.append(" Grade Report for ");
//        reportBuilder.append((student == null ? "Template" : student.getName()));
//        reportBuilder.append("</h1>");
//        reportBuilder.append(message.replace("\n", "<br/>"));
//        reportBuilder.append("<br/><br/>");
//        
//        //Grades
//        reportBuilder.append("<table cellspacing='0' cellpadding='5' style='width: 100%'>");
//        for(Assignment asgn : assignments)
//        {
//            //Assignment header
//            reportBuilder.append("<tr style='font-weight: bold; background: #CCCCCC'>");
//            reportBuilder.append("<td>");
//            reportBuilder.append(asgn.getName());
//            reportBuilder.append("</td>");
//            reportBuilder.append("<td>Earned</td>");
//            reportBuilder.append("<td>Out Of</td>");
//            reportBuilder.append("</tr>");
//            
//            Group group = _groups.get(asgn).get(student);
//            //No group exists and not a template (represented by a null student)
//            if(group == null && student != null)
//            {
//                reportBuilder.append("<tr style='background: #FFFFFF'><td colspan=3>");
//                reportBuilder.append("This is a group assignment and you are not in a group.");
//                reportBuilder.append("</td></tr>");
//            }
//            //The group exists or template
//            //A template is represented by passing null for the student argument to this method, which results in a
//            //null group at this point in the code because there is no group mapped to the null student
//            else
//            {   
//                double assignmentTotalEarned = 0;
//                double assignmentTotalOutOf = 0;
//                for(int geIndex = 0; geIndex < asgn.getGradableEvents().size(); geIndex++)
//                {
//                    GradableEvent ge = asgn.getGradableEvents().get(geIndex); 
//                    
//                    //Gradable event header
//                    reportBuilder.append("<tr style='background: #F0F0F0'><td>");
//                    reportBuilder.append(ge.getName());
//                    reportBuilder.append("</td><td></td><td></td></tr>");
//                    
//                    double unadjustedGradableEventTotalEarned = 0;
//                    double gradableEventTotalOutOf = 0;
//                    for(Part part : ge)
//                    {
//                        PartGrade partGrade = _partGrades.get(part).get(group);
//                        
//                        String earnedString;
//                        if(group == null)
//                        {
//                            earnedString = "--";
//                        }
//                        else if(partGrade == null || partGrade.getEarned() == null || !partGrade.isSubmitted())
//                        {
//                            earnedString = "0 (No grade recorded)";
//                        }                        
//                        else
//                        {
//                            unadjustedGradableEventTotalEarned += partGrade.getEarned();
//                            earnedString = Double.toString(partGrade.getEarned());
//                        }
//                        
//                        double partOutOf = part.getOutOf();
//                        gradableEventTotalOutOf += partOutOf;
//                        
//                        //Part earned and out of
//                        reportBuilder.append("<tr style='background: #FFFFFF'><td>");
//                        reportBuilder.append(part.getName());
//                        reportBuilder.append("</td><td>");
//                        reportBuilder.append(earnedString);
//                        reportBuilder.append("</td><td>");
//                        reportBuilder.append(Double.toString(partOutOf));
//                        reportBuilder.append("</td></tr>");
//                    }
//                    
//                    //Deadline resolution
//                    DateTime occurrenceDate = _occurrenceDates.get(ge).get(group);
//                    Extension extension = _extensions.get(ge).get(group);
//                    DeadlineInfo deadlineInfo = _deadlines.get(ge);
//                    DeadlineResolution deadlineResolution = deadlineInfo.apply(occurrenceDate, extension);
//                    double penaltyOrBonus = deadlineResolution.getPenaltyOrBonus(unadjustedGradableEventTotalEarned);
//                    reportBuilder.append("<tr style='background: #F0F0F0'>");
//                    reportBuilder.append("<td>");
//                    reportBuilder.append("Deadline Resolution");
//                    reportBuilder.append("</td>");
//                    reportBuilder.append("<td>");
//                    if(group == null)
//                    {
//                        reportBuilder.append("--");
//                    }
//                    else
//                    {
//                        reportBuilder.append(Double.toString(penaltyOrBonus));
//                        reportBuilder.append(" (");
//                        reportBuilder.append(deadlineResolution.getTimeStatus());
//                        reportBuilder.append(")");
//                    }
//                    reportBuilder.append("</td>");
//                    reportBuilder.append("<td></td>");
//                    reportBuilder.append("</tr>");
//                    
//                    //Totals for the gradable event
//                    double gradableEventTotalEarned = unadjustedGradableEventTotalEarned + penaltyOrBonus;
//                    reportBuilder.append("<tr style='background: #F0F0F0'>");
//                    reportBuilder.append("<td>Total</td>");
//                    reportBuilder.append("<td>");
//                    if(group == null)
//                    {
//                        reportBuilder.append("--");
//                    }
//                    else
//                    {
//                        reportBuilder.append(Double.toString(gradableEventTotalEarned));
//                    }
//                    reportBuilder.append("</td>");
//                    reportBuilder.append("<td>");
//                    reportBuilder.append(Double.toString(gradableEventTotalOutOf));
//                    reportBuilder.append("</td>");
//                    reportBuilder.append("</tr>");
//                    
//                    assignmentTotalEarned += gradableEventTotalEarned;
//                    assignmentTotalOutOf += gradableEventTotalOutOf;
//                    
//                    //If not the last gradable event for assignment, put in an empty row
//                    if(geIndex != asgn.getGradableEvents().size() - 1)
//                    {
//                        reportBuilder.append("<tr style='background: #FFFFFF'></tr>");
//                    }
//                }
//                
//                //Totals for the assignment
//                reportBuilder.append("<tr style='font-weight: bold; background: #CCCCCC'>");
//                reportBuilder.append("<td>Total</td>");
//                reportBuilder.append("<td>");
//                if(group == null)
//                {
//                    reportBuilder.append("--");
//                }
//                else
//                {
//                    reportBuilder.append(Double.toString(assignmentTotalEarned));
//                }
//                reportBuilder.append("</td>");
//                reportBuilder.append("<td>");
//                reportBuilder.append(Double.toString(assignmentTotalOutOf));
//                reportBuilder.append("</td>");
//                reportBuilder.append("</tr>");
//            }
//            
//            //Spacing between assignments
//            reportBuilder.append("<tr style='background: #FFFFFF'><td colspan=3><br/></td></tr>");
//            
//        }
//        reportBuilder.append("</table>");
//        
//        return reportBuilder.toString();
//    }
//    
//    private static class EmailAddressField extends JTextField
//    {   
//        private EmailAddressField()
//        {
//            this.getDocument().addDocumentListener(new DocumentAdapter()
//            {
//                @Override
//                public void modificationOccurred(DocumentEvent de)
//                {
//                    updateColor();
//                }
//            });
//            
//            this.updateColor();
//        }
//        
//        private void updateColor()
//        {
//            if(isEnabled() && !isValidEmailAddress())
//            {
//                setBackground(new Color(255, 204, 204));
//            }
//            else
//            {
//                setBackground(Color.WHITE);
//            }
//        }
//        
//        boolean isValidEmailAddress()
//        {
//            boolean valid = true;
//            try
//            {
//                new InternetAddress(getText(), true);
//            }
//            catch(AddressException ex)
//            {
//                valid = false;
//            }
//            
//            return valid;
//        }
//        
//        @Override
//        public void setEnabled(boolean enabled)
//        {
//            super.setEnabled(enabled);
//            
//            this.updateColor();
//        }
//        
//        InternetAddress getAddress()
//        {
//            InternetAddress address = null;
//            
//            String text = getText();
//            if(!text.isEmpty())
//            {
//                try
//                {
//                    address = new InternetAddress(text);
//                }
//                catch(AddressException ex) { }
//            }
//            
//            return address;
//        }
//    }
//    
//    /**
//     * Assignment list
//     */
//    private static class AssignmentList extends JPanel
//    {   
//        private final GenericJList<Assignment> _assignmentList;
//        
//        private AssignmentList()
//        {
//            this.setLayout(new BorderLayout(0, 0));
//
//            //Header
//            JPanel headerPanel = new JPanel();
//            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
//            this.add(headerPanel, BorderLayout.NORTH);
//            headerPanel.add(FormattedLabel.asHeader("Assignments"));
//            headerPanel.add(Box.createVerticalStrut(5));
//
//            //Assignments list
//             _assignmentList = new GenericJList<Assignment>(Allocator.getDataServices().getAssignments());
//            _assignmentList.setAlignmentX(LEFT_ALIGNMENT);
//             _assignmentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//            JScrollPane scrollPane = new JScrollPane(_assignmentList);
//            scrollPane.setBackground(Color.WHITE);
//            this.add(scrollPane, BorderLayout.CENTER);
//        }
//        
//        GenericJList<Assignment> getList()
//        {
//            return _assignmentList;
//        }
//    }
//}