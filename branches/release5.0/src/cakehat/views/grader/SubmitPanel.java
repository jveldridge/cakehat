package cakehat.views.grader;

import cakehat.Allocator;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.DbPropertyValue;
import cakehat.database.DbPropertyValue.DbPropertyKey;
import cakehat.database.Group;
import cakehat.database.PartGrade;
import cakehat.database.Student;
import cakehat.database.TA;
import cakehat.email.EmailManager.EmailAccountStatus;
import cakehat.gml.GMLParser;
import cakehat.gml.GradingSheetException;
import cakehat.gml.InMemoryGML;
import cakehat.gml.InMemoryGML.Section;
import cakehat.gml.InMemoryGML.Subsection;
import cakehat.printing.CITPrinter;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import support.ui.AlphaJPanel;
import support.ui.AlphaJScrollPane;
import support.ui.GenericJCheckBox;
import support.ui.GenericJComboBox;
import support.utils.Pair;

/**
 * A panel hosted inside of {@link GraderView}'s frame. The view is shown for a specific {@link Part} and a list of
 * {@link Group}s. If the {@code Part} being shown by this view is the only {@code Part} for the {@link Assignment},
 * then the user may print and email the grading sheet that belongs to the {@code Assignment}. Otherwise the user will
 * be shown a message explaining the grading sheet cannot be printed or emailed, but can be done by an administrator.
 *
 * @author jak2
 */
class SubmitPanel extends AlphaJPanel
{
    private final GraderView _graderView;

    /** The part having grades submitted */
    private final Part _part;

    /** If the part is the only one for this assignment */
    private final boolean _onlyPart;

    // Split the groups into categories

    /** Groups with grading sheets that have not been submitted (or have changed since last submission) and have been
     *  modified */
    private final List<Group> _notSubmittedModified = new ArrayList<Group>();

    /** Groups with grading sheets that have not been submitted (or have changed since last submission) and have not
     * been modified */
    private final List<Group> _notSubmittedNotModified = new ArrayList<Group>();

    /** Groups with grading sheets that have been submitted previously and have not been modified since */
    private final List<Group> _previouslySubmitted = new ArrayList<Group>();

    /** The groups submitted with this view */
    private final Set<Group> _justSubmittedGroups = new HashSet<Group>();

    // Buttons that submit, print, & email grades
    private JButton _submitButton, _printButton, _emailButton;


    public SubmitPanel(Dimension size, Color backgroundColor, GraderView graderView, Part part, Set<Group> groups)
            throws ServicesException
    {
        this.setPreferredSize(size);
        this.setBackground(backgroundColor);

        _graderView = graderView;
        _part = part;
        
        //Determine if there are other parts that belong to this assignment
        int numParts = 0;
        for(GradableEvent ge : part.getAssignment())
        {
            numParts += ge.getParts().size();
        }
        _onlyPart = numParts == 1;

        // Sort groups into different categories
        this.sortGroupsIntoCategories(groups, part);

        this.initUI();
    }

    private void sortGroupsIntoCategories(Set<Group> groups, Part part) throws ServicesException
    {
        // Load part grades
        Map<Group, PartGrade> partGrades = Allocator.getDataServices().getEarned(groups, part); 

        // Sort groups alphabetically and then into categories
        List<Group> sortedGroups = new ArrayList<Group>(groups);
        Collections.sort(sortedGroups);
        for(Group group : sortedGroups)
        {
            PartGrade partGrade = partGrades.get(group);
            
            //If the grading sheet has been modified, upon saving it a write to the database will occur resulting in a
            //part grade existing for the group
            if(partGrade == null)
            {
                _notSubmittedNotModified.add(group);
            }
            else
            {
                if(partGrade.isSubmitted())
                {
                    _previouslySubmitted.add(group);
                }
                else
                {
                    _notSubmittedModified.add(group);
                }
            }
        }
    }

    private void initUI()
    {
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // Determine sizes as percentages of this panel's size
        Dimension size = this.getPreferredSize();

        int titleHeight = (int) (size.height * .1);
        int contentHeight = size.height - titleHeight;

        int submitWidth = (int) (size.width * .34);
        int printAndEmailWidth = (int) (size.width * .65);
        int gapWidth = size.width - submitWidth - printAndEmailWidth;

        int printHeight = (int) (contentHeight * .20);
        int emailHeight = (int) (contentHeight * .79);
        int gapHeight = contentHeight - printHeight - emailHeight;

        // Title
        AlphaJPanel titlePanel = new AlphaJPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(this.getBackground());
        titlePanel.setPreferredSize(new Dimension(size.width, titleHeight));
        String title;
        if(_onlyPart)
        {
            title = _part.getAssignment().getName() + " - " + _part.getName();
        }
        else
        {
            title = _part.getFullDisplayName();
        }
        JLabel titleLabel = new JLabel("<html><font size=4>Grading for " +  title + "</font></html>");
        titlePanel.add(titleLabel);
        this.add(titlePanel);

        // Submit
        this.add(this.createSubmitPanel(new Dimension(submitWidth, contentHeight)));
        
        // Horizontal gap space
        this.add(Box.createHorizontalStrut(gapWidth));
        
        if(_onlyPart)
        {
            // Panel to hold print panel above & email panel below
            AlphaJPanel optionsPanel = new AlphaJPanel();
            optionsPanel.setBackground(this.getBackground());
            optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
            optionsPanel.setPreferredSize(new Dimension(printAndEmailWidth, contentHeight));
            this.add(optionsPanel);

            // Print
            optionsPanel.add(this.createPrintPanel(new Dimension(printAndEmailWidth, printHeight)));

            // Vertical gap
            optionsPanel.add(Box.createVerticalStrut(gapHeight));

            // Email
            optionsPanel.add(this.createEmailPanel(new Dimension(printAndEmailWidth, emailHeight)));
        }
        else
        {
            Dimension unavailableSize = new Dimension(printAndEmailWidth, contentHeight);
            this.add(this.createUnavailablePanel(unavailableSize));
        }
    }

    private AlphaJPanel createSubmitPanel(Dimension size)
    {
        AlphaJPanel submitPanel = new AlphaJPanel();
        submitPanel.setLayout(new BoxLayout(submitPanel, BoxLayout.Y_AXIS));
        submitPanel.setBorder(BorderFactory.createTitledBorder("Submit Grading"));
        submitPanel.setBackground(this.getBackground());
        submitPanel.setPreferredSize(size);
        this.add(submitPanel);

        // Panel holding check boxes
        final AlphaJPanel boxesPanel = new AlphaJPanel();
        boxesPanel.setLayout(new BoxLayout(boxesPanel, BoxLayout.Y_AXIS));
        boxesPanel.setBackground(this.getBackground());

        // Scroll pane so to allow scrolling if more boxes than fit on screen
        AlphaJScrollPane boxesScrollPane = new AlphaJScrollPane(boxesPanel);
        boxesScrollPane.setBackground(this.getBackground());
        boxesScrollPane.getViewport().setOpaque(false);
        boxesScrollPane.setBorder(BorderFactory.createEmptyBorder());
        boxesScrollPane.setPreferredSize(size);
        submitPanel.add(boxesScrollPane);

        // Check boxes for each category of groups
        final List<GenericJCheckBox<Group>> groupBoxes = new ArrayList<GenericJCheckBox<Group>>();
        this.addGroupBoxes("Not Submitted - Graded", boxesPanel, groupBoxes, true, _notSubmittedModified);
        this.addGroupBoxes("Not Submitted - Not Graded", boxesPanel, groupBoxes, false, _notSubmittedNotModified,
                _notSubmittedModified);
        this.addGroupBoxes("Previously Submitted - Graded", boxesPanel, groupBoxes, false, _previouslySubmitted,
                _notSubmittedModified, _notSubmittedModified);

        boxesPanel.add(Box.createVerticalStrut(5));

        // Submit selected
        AlphaJPanel submitButtonPanel = new AlphaJPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        submitButtonPanel.setPreferredSize(new Dimension(size.width, 25));
        submitPanel.add(submitButtonPanel);

        _submitButton = new JButton("Submit Selected");
        _submitButton.setToolTipText("Grades may be resubmitted at any time");
        _submitButton.setPreferredSize(new Dimension(size.width, 25));
        _submitButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                // Build the list of groups to submit
                List<Group> groupsToSubmit = new ArrayList<Group>();
                for(GenericJCheckBox<Group> groupBox : groupBoxes)
                {
                    if(groupBox.isSelected())
                    {
                        groupsToSubmit.add(groupBox.getItem());
                    }
                }
                Collections.sort(groupsToSubmit);

                // Submit grading
                if(_part.hasSpecifiedGMLTemplate())
                {
                    try
                    {
                        Map<Group, Pair<Double, Boolean>> earnedGrades = new HashMap<Group, Pair<Double, Boolean>>();
                        for(Group group : groupsToSubmit)
                        {
                            Double earned = null;
                            File gmlFile = Allocator.getPathServices().getGroupGMLFile(_part, group);
                            if(gmlFile.exists())
                            {
                                InMemoryGML gml = GMLParser.parse(gmlFile, _part, group);
                                double gmlEarned = 0;
                                for(Section section : gml.getSections())
                                {
                                    for(Subsection subsection : section.getSubsections())
                                    {
                                        gmlEarned += subsection.getEarned();
                                    }
                                }
                                earned = gmlEarned;
                            }
                            earnedGrades.put(group, Pair.of(earned, true));
                        }
                        
                        Allocator.getDataServices().setEarned(_part, earnedGrades);
                    }
                    catch(GradingSheetException ex)
                    {
                        new ErrorView(ex, "Unable to submit grade for part [" +_part.getFullDisplayName() + "]");
                        return;
                    }
                    catch(ServicesException ex)
                    {
                        new ErrorView(ex, "Unable to submit grade for part [" + _part.getFullDisplayName() + "]");
                        return;
                    }
                }
                else
                {
                    try
                    {
                        Map<Group, Boolean> submitted = new HashMap<Group, Boolean>();
                        for(Group group : groupsToSubmit)
                        {
                            submitted.put(group, true);
                        }
                        
                        Allocator.getDataServices().setEarnedSubmitted(_part, submitted);
                    }
                    catch(ServicesException ex)
                    {
                        new ErrorView(ex, "Unable to set submit status for part [" + _part.getFullDisplayName() + "]");
                        return;
                    }
                }
                
                _justSubmittedGroups.addAll(groupsToSubmit);

                // Tell the grader view to reload its data to reflect that these grades have been submitted
                _graderView.loadAssignedGrading();

                // Visually update list
                Component[] components = boxesPanel.getComponents();
                for(Component component : components)
                {
                    boxesPanel.remove(component);
                }

                boxesPanel.add(new JLabel("Submitted"));
                for(Group group : groupsToSubmit)
                {
                    boxesPanel.add(new JLabel(" • " + group.getName()));
                }
                boxesPanel.repaint();

                _submitButton.setToolTipText(null);
                _submitButton.setEnabled(false);
                _submitButton.setText("Submitted Successfully");

                if(_printButton != null)
                {
                    _printButton.setEnabled(true);
                    _printButton.setToolTipText(null);
                }

                if(_emailButton != null)
                {
                    EmailAccountStatus accountStatus = Allocator.getEmailManager().getEmailAccountStatus();
                    if(accountStatus != EmailAccountStatus.AVAILABLE)
                    {
                        _emailButton.setEnabled(false);

                        if(accountStatus == EmailAccountStatus.INITIALIZATION_ERROR)
                        {
                            _emailButton.setToolTipText("An error occurred while initializing the email account");
                        }
                        else if(accountStatus == EmailAccountStatus.NOT_CONFIGURED)
                        {
                            _emailButton.setToolTipText("Your course has not configured its email account");
                        }
                    }
                    else
                    {
                        _emailButton.setEnabled(true);
                        _emailButton.setToolTipText(null);
                    }
                }
            }
        });
        submitButtonPanel.add(_submitButton, BorderLayout.CENTER);

        return submitPanel;
    }

    private void addGroupBoxes(String labelText, AlphaJPanel panel, List<GenericJCheckBox<Group>> allGroupBoxes,
            boolean selectBox, List<Group> groups, List<Group>... aboveListsOfGroups)
    {
        if(!groups.isEmpty())
        {
            // If any of the above lists were not empty
            for(List<Group> aboveList : aboveListsOfGroups)
            {
                if(!aboveList.isEmpty())
                {
                    panel.add(Box.createVerticalStrut(10));
                    break;
                }
            }
            
            // Add the label
            final JLabel label = new JLabel(labelText);
            label.setToolTipText("Click to toggle check boxes");
            panel.add(label);

            // Add a check box for each group
            final List<GenericJCheckBox<Group>> groupBoxes = new ArrayList<GenericJCheckBox<Group>>();
            for(Group group : groups)
            {
                GenericJCheckBox<Group> checkBox = new GenericJCheckBox<Group>(group);
                allGroupBoxes.add(checkBox);
                groupBoxes.add(checkBox);

                checkBox.setSelected(selectBox);
                checkBox.setOpaque(false);
                panel.add(checkBox);
            }

            // Have label interact with check boxes
            label.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent me)
                {
                    for(GenericJCheckBox<Group> box : groupBoxes)
                    {
                        box.setSelected(!box.isSelected());
                    }
                }

                @Override
                public void mouseEntered(MouseEvent me)
                {
                    for(GenericJCheckBox<Group> box : groupBoxes)
                    {
                        box.getModel().setRollover(true);
                    }
                }

                @Override
                public void mouseExited(MouseEvent me)
                {
                    for(GenericJCheckBox<Group> box : groupBoxes)
                    {
                        box.getModel().setRollover(false);
                    }
                }
            });
        }
    }

    private AlphaJPanel createUnavailablePanel(Dimension size)
    {
        AlphaJPanel unavailablePanel = new AlphaJPanel(new BorderLayout());
        unavailablePanel.setPreferredSize(size);
        unavailablePanel.setBackground(this.getBackground());
        unavailablePanel.setBorder(BorderFactory.createTitledBorder("Print & Email"));

        JLabel label = new JLabel("<html><center>" +
                "Grade sheets for multipart assignments may only<br/>" +
                "be printed and emailed by administrators.<br/>" +
                "<br/>" + 
                "(Generating the grading sheets requires the " +
                "grades for all parts.)" +
                "</center></html>");
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setHorizontalAlignment(JLabel.CENTER);
        unavailablePanel.add(label, BorderLayout.CENTER);

        return unavailablePanel;
    }

    private AlphaJPanel createPrintPanel(Dimension size)
    {
        // Print
        final AlphaJPanel printPanel = new AlphaJPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        printPanel.setPreferredSize(size);
        printPanel.setAlignmentX(LEFT_ALIGNMENT);
        printPanel.setBackground(this.getBackground());
        printPanel.setBorder(BorderFactory.createTitledBorder("Print Submitted"));

        _printButton = new JButton("Print");
        _printButton.setEnabled(false);
        _printButton.setToolTipText("Grades must be submitted before printing");
        printPanel.add(_printButton);

        printPanel.add(new JLabel(" on printer "));

        final GenericJComboBox<CITPrinter> printerBox = new GenericJComboBox<CITPrinter>(
                Allocator.getGradingServices().getAllowedPrinters());
        printerBox.setGenericSelectedItem(Allocator.getGradingServices().getDefaultPrinter());
        printPanel.add(printerBox);

        _printButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Print the grading sheets
                try
                {
                    Allocator.getGradingServices().printGRDFiles(_part.getAssignment(), _justSubmittedGroups,
                            printerBox.getSelectedItem());
                    
                    // Disable printing, indicate success
                    _printButton.setEnabled(false);
                    printerBox.setEnabled(false);
                    _printButton.setText("Printed Successfully");
                    printPanel.repaint();
                }
                catch(ServicesException ex)
                {
                    new ErrorView(ex, "Unable to print grading sheets");
                }
            }
        });

        return printPanel;
    }

    private AlphaJPanel createEmailPanel(final Dimension size)
    {
        final AlphaJPanel emailPanel = new AlphaJPanel();
        emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.Y_AXIS));
        emailPanel.setPreferredSize(size);
        emailPanel.setAlignmentX(LEFT_ALIGNMENT);
        emailPanel.setBackground(this.getBackground());
        emailPanel.setBorder(BorderFactory.createTitledBorder("Email Submitted"));

        String subject = "[" + Allocator.getCourseInfo().getCourse() + "] " + _part.getAssignment().getName() +
                         " Graded";

        String body = _part.getAssignment().getName() + " has been graded.";
        // Add TA's name as a signature
        body += "\n\n--\n";
        body += Allocator.getUserServices().getUser().getName();

        final JTextField subjectField = new JTextField();
        subjectField.setText(subject);
        subjectField.setAlignmentX(LEFT_ALIGNMENT);
        emailPanel.add(subjectField);

        final AlphaJPanel attachGradesPanel = new AlphaJPanel(new BorderLayout(0, 0));
        attachGradesPanel.setPreferredSize(new Dimension(size.width, 25));
        attachGradesPanel.setBackground(emailPanel.getBackground());
        attachGradesPanel.setAlignmentX(LEFT_ALIGNMENT);
        emailPanel.add(attachGradesPanel);

        final JCheckBox attachGradesBox = new JCheckBox("Attach grading sheets");
        attachGradesBox.setOpaque(false);
        boolean attachGradingSheets = true;
        try
        {
            DbPropertyValue<Boolean> attachProp = Allocator.getDatabase()
                    .getPropertyValue(DbPropertyKey.ATTACH_GRADING_SHEET);
            if(attachProp != null)
            {
                attachGradingSheets = attachProp.getValue();
            }
        }
        catch(SQLException e)
        {
            new ErrorView(e, "Unable to retrieve database value for whether grading sheets should be attached by "
                    + "default");
        }
        attachGradesBox.setSelected(attachGradingSheets);
        attachGradesPanel.add(attachGradesBox);

        final JTextArea messageArea = new JTextArea();
        messageArea.setText(body);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setPreferredSize(size);
        messageScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        emailPanel.add(messageScrollPane);
        emailPanel.add(Box.createVerticalStrut(5));

        final AlphaJPanel sendPanel = new AlphaJPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sendPanel.setBackground(emailPanel.getBackground());
        sendPanel.setAlignmentX(LEFT_ALIGNMENT);
        emailPanel.add(sendPanel);

        _emailButton = new JButton("Send Email");
        _emailButton.setEnabled(false);
        _emailButton.setToolTipText("Grades must be submitted before emailing");
        sendPanel.add(_emailButton);

        sendPanel.add(Box.createHorizontalStrut(5));

        final JLabel individualEmailLabel = new JLabel("Individual emails will be sent to each student");
        sendPanel.add(individualEmailLabel);

        _emailButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sendEmails(subjectField.getText(), messageArea.getText(), attachGradesBox.isSelected());

                // Visually update to reflect success and disable components

                subjectField.setEditable(false);
                subjectField.setBackground(Color.LIGHT_GRAY);

                attachGradesPanel.remove(attachGradesBox);

                JLabel attachLabel = new JLabel();
                attachLabel.setAlignmentX(LEFT_ALIGNMENT);
                attachLabel.setAlignmentY(CENTER_ALIGNMENT);
                attachLabel.setPreferredSize(attachGradesPanel.getPreferredSize());
                attachGradesPanel.add(attachLabel, BorderLayout.CENTER);

                if(attachGradesBox.isSelected())
                {
                    attachLabel.setText("Grade sheets were attached");
                }
                else
                {
                    attachLabel.setText("Grade sheets were not attached");
                }

                messageArea.setEditable(false);
                messageArea.setBackground(Color.LIGHT_GRAY);
                
                _emailButton.setEnabled(false);
                _emailButton.setText("Emailed Successfully");

                individualEmailLabel.setText("Individual emails were sent to each student");
                
                sendPanel.repaint();
            }
        });

        return emailPanel;
    }

    private void sendEmails(String subject, String body, boolean attachGradingSheets)
    {
        Map<Student, File> gradingSheets = new HashMap<Student, File>();
        if(attachGradingSheets)
        {
            try
            {
                gradingSheets = Allocator.getGradingServices()
                        .generateGRDFiles(_part.getAssignment(), _justSubmittedGroups);
            }
            catch(ServicesException ex)
            {
                new ErrorView(ex, "Unable to generate grading sheet file attachments");
                return;
            }
        }
        
        // Notify each student
        TA user = Allocator.getUserServices().getUser();
        InternetAddress from = user.getEmailAddress();
        body = body.replace(System.getProperty("line.separator"), "<br/>");

        Set<Student> notifiedStudents = new HashSet<Student>();
        for(Group group : _justSubmittedGroups)
        {
            for(Student student : group)
            {
                if((attachGradingSheets && gradingSheets.containsKey(student)) || !attachGradingSheets)
                {
                    File gradingSheet = gradingSheets.get(student);
                    
                    try
                    {
                        Allocator.getEmailManager().send(from,
                                                         ImmutableSet.of(student.getEmailAddress()),
                                                         null,
                                                         null,
                                                         subject,
                                                         body,
                                                         gradingSheet == null ? null : ImmutableSet.of(gradingSheet));
                        notifiedStudents.add(student);
                    }
                    catch(MessagingException e)
                    {
                        new ErrorView(e, "Unable to send an email to student " + student);
                    }
                }
            }
        }

        if(!notifiedStudents.isEmpty())
        {   
            //Send an email to the notify addresses
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            String notificationMessage = "At " + now + ", " + user.getLogin() + " submitted grading for assignment " +
                    _part.getAssignment().getName() + " for the following students: <blockquote>";

            for(Student student : notifiedStudents)
            {
                notificationMessage += student.getLogin() + " (" + student.getName() + ")";
                if(gradingSheets.containsKey(student))
                {
                    notificationMessage += " [Grading Sheet Attached]";
                }
                notificationMessage += "<br/>";
            }

            notificationMessage += "</blockquote> The following message was sent to the students: <blockquote>" + body +
                    "</blockquote>";

            try
            {
                Allocator.getEmailManager().send(from,
                                                 Allocator.getEmailManager().getNotifyAddresses(),
                                                 ImmutableSet.of(from),
                                                 null,
                                                 subject,
                                                 notificationMessage,
                                                 gradingSheets.values());
            }
            catch(MessagingException e)
            {
                new ErrorView(e, "Unable to send an email to the notify addresses");
            }
        }
    }
}