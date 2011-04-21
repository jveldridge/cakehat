package cakehat.views.frontend;

import cakehat.Allocator;
import cakehat.config.Assignment;
import cakehat.config.handin.DistributablePart;
import cakehat.database.Group;
import cakehat.printing.CITPrinter;
import cakehat.rubric.RubricException;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

/**
 * A panel hosted inside of {@link FrontendView}'s frame. The view is shown
 * for a specific {@link DistributablePart} and a list of {@link Group}s. If the
 * <code>DistributablePart</code> being shown by this view is the only
 * <code>DistributablePart</code> for the {@link Assignment}, then the user may
 * print and email the grading sheet that belongs to the
 * <code>Assignment</code>. Otherwise the user will be shown a message
 * explaining the grading sheet cannot be printed or emailed, but can be done
 * by an administrator.
 *
 * @author jak2
 */
class SubmitPanel extends AlphaJPanel
{
    private final FrontendView _frontend;

    /** The part having grades submitted */
    private final DistributablePart _part;

    /** If the distributable part is the only one for this assignment */
    private final boolean _onlyDistributablePart;

    /** Map of each group to the rubrics */
    private final Map<Group, Double> _rubricScores;

    // Split the groups into categories

    /** Groups who have never had a grade submitted, have a non-zero total */
    private final List<Group> _notSubmitted = new ArrayList<Group>();

    /** Groups who have never had a grade submitted, have a zero total */
    private final List<Group> _notSubmittedZeroGrade = new ArrayList<Group>();
    
    /** Groups already submitted, the grading sheet scores does NOT match the database value */
    private final List<Group> _submittedNewGrade = new ArrayList<Group>();

    /** Groups already submitted, the grading sheet score matches the database value */
    private final List<Group> _submittedUnchangedGrade = new ArrayList<Group>();

    /** Groups that do not have a grading sheet*/
    private final List<Group> _missingGradingSheet = new ArrayList<Group>();

    /** The groups submitted with this view */
    private final List<Group> _submittedGroups = new ArrayList<Group>();

    // Buttons that submit, print, & email grades
    private JButton _submitButton, _printButton, _emailButton;


    public SubmitPanel(Dimension size, Color backgroundColor, FrontendView frontend,
            DistributablePart part, List<Group> groups) throws SQLException
    {
        this.setPreferredSize(size);
        this.setBackground(backgroundColor);

        _frontend = frontend;
        _part = part;
        _onlyDistributablePart = _part.getAssignment().getDistributableParts().size() == 1;

        // Load rubric scores
        _rubricScores = Allocator.getRubricManager().getPartScores(part, groups);

        // Sort groups into different categories
        this.sortGroupsIntoCategories(groups);

        this.initUI();
    }

    private void sortGroupsIntoCategories(List<Group> groups) throws SQLException
    {
        Map<Group, Double> submittedScores = Allocator.getDatabase().getPartScoresForGroups(_part, groups);

        List<Group> sortedGroups = new ArrayList<Group>(groups);
        Collections.sort(sortedGroups);

        for(Group group : sortedGroups)
        {
            if(Allocator.getRubricManager().hasRubric(_part, group))
            {
                if(submittedScores.containsKey(group))
                {
                    if(submittedScores.get(group).equals(_rubricScores.get(group)))
                    {
                        _submittedUnchangedGrade.add(group);
                    }
                    else
                    {
                        _submittedNewGrade.add(group);
                    }
                }
                else
                {
                    if(_rubricScores.get(group) == 0)
                    {
                        _notSubmittedZeroGrade.add(group);
                    }
                    else
                    {
                        _notSubmitted.add(group);
                    }
                }
            }
            else
            {
                _missingGradingSheet.add(group);
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
        JLabel titleLabel = new JLabel("<html><font size=4>" +
                "Grading for " + 
                _part.getAssignment().getName() + " - " + _part.getName() +
                "</font></html>");
        titlePanel.add(titleLabel);
        this.add(titlePanel);

        // Submit
        this.add(this.createSubmitPanel(new Dimension(submitWidth, contentHeight)));
        
        // Horizontal gap space
        this.add(Box.createHorizontalStrut(gapWidth));
        
        if(_onlyDistributablePart)
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
        this.addGroupBoxes("Never Submitted", boxesPanel, groupBoxes,
                true, _notSubmitted);
        this.addGroupBoxes("Never Submitted (Grade of 0)", boxesPanel, groupBoxes,
                true, _notSubmittedZeroGrade,
                _notSubmitted);
        this.addGroupBoxes("Previously Submitted (New Grade)", boxesPanel, groupBoxes,
                true, _submittedNewGrade,
                _notSubmitted, _notSubmittedZeroGrade);
        this.addGroupBoxes("Previously Submitted (Unchanged)", boxesPanel, groupBoxes,
                false, _submittedUnchangedGrade,
                _notSubmitted, _notSubmittedZeroGrade, _submittedNewGrade);

        // List those missing rubrics, they cannot be checked off because there
        // is no grade sheet with which to calculate the score
        if(!_missingGradingSheet.isEmpty())
        {
            if(!_notSubmitted.isEmpty() || !_notSubmittedZeroGrade.isEmpty() ||
                    !_submittedNewGrade.isEmpty() || _submittedUnchangedGrade.isEmpty())
            {
                boxesPanel.add(Box.createVerticalStrut(10));
            }

            boxesPanel.add(new JLabel("Missing Grading Sheet"));
            for(Group group : _missingGradingSheet)
            {
                boxesPanel.add(new JLabel(group.getName()));
            }
        }

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
                for(Group group : groupsToSubmit)
                {
                    try
                    {
                        Allocator.getDatabase().enterGrade(group, _part, _rubricScores.get(group));
                    }
                    catch(SQLException ex)
                    {
                        new ErrorView(ex, "Unable to submit grade for group [" +
                                group + "] for part [" + _part.getAssignment() +
                                " - " + _part + "]");
                        return;
                    }
                }
                _submittedGroups.addAll(groupsToSubmit);

                // Tell the frontend to reload its data to reflect that these
                // grades have been submitted
                _frontend.loadAssignedGrading(true);

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
                    _emailButton.setEnabled(true);
                    _emailButton.setToolTipText(null);
                }
            }
        });
        submitButtonPanel.add(_submitButton, BorderLayout.CENTER);

        return submitPanel;
    }

    private void addGroupBoxes(String labelText, AlphaJPanel panel,
            List<GenericJCheckBox<Group>> allGroupBoxes, boolean selectBox,
            List<Group> groups, List<Group>... aboveListsOfGroups)
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
                "Grade sheets for multipart handins may only<br/>" +
                "be printed and emailed by administrators.<br/>" +
                "<br/>" + 
                "(Generating the grade sheets requires the " +
                "grades for each part.)" +
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
                // Generate grading sheets, return if unsuccessful
                if(!generateGradingSheets())
                {
                    return;
                }

                // Print the grading sheets
                try
                {
                    Allocator.getGradingServices().printGRDFiles(_part.getHandin(),
                            _submittedGroups, printerBox.getSelectedItem());
                }
                catch(ServicesException ex)
                {
                    new ErrorView(ex, "Unable to print grading sheets");
                    return;
                }

                // Disable printing, indicate success
                _printButton.setEnabled(false);
                printerBox.setEnabled(false);
                _printButton.setText("Printed Successfully");
                printPanel.repaint();
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

        String subject = "[" + Allocator.getCourseInfo().getCourse() + "] " +
                _part.getAssignment().getName() + " Graded";

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

        final JCheckBox attachGradesBox = new JCheckBox("Attach grade sheets");
        attachGradesBox.setOpaque(false);
        attachGradesBox.setSelected(Allocator.getConfigurationInfo().getSubmitOptions().isEmailGrdDefaultEnabled());
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
                // Generate grading sheets, return if unsuccessful
                if(!generateGradingSheets())
                {
                    return;
                }

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

    private boolean _generatedGradingSheets = false;
    private boolean generateGradingSheets()
    {
        if(!_generatedGradingSheets)
        {
            _generatedGradingSheets = true;

            for(Group group : _submittedGroups)
            {
                try
                {
                    Allocator.getRubricManager().convertToGRD(_part.getHandin(), group);
                }
                catch(RubricException ex)
                {
                    new ErrorView(ex, "Unable to generate grade sheet for group [" +
                            group + "] for part [" + _part.getAssignment() +
                            " - " + _part + "]");

                    _generatedGradingSheets = false;
                    break;
                }
            }
        }

        return _generatedGradingSheets;
    }

    private void sendEmails(String subject, String body, boolean attachGradingSheets)
    {
        String taLogin = Allocator.getUserUtilities().getUserLogin();

        // Notify each student
        String taEmailAddress = Allocator.getUserUtilities().getUserLogin() + "@" +
                Allocator.getConstants().getEmailDomain();
        body = body.replace(System.getProperty("line.separator"), "<br/>");

        for(Group group : _submittedGroups)
        {
            for(String student : group.getMembers())
            {
                String attachmentPath = null;
                if(attachGradingSheets)
                {
                    attachmentPath = Allocator.getPathServices().getGroupGRDFile(_part.getHandin(), group).getAbsolutePath();
                }

                Allocator.getConfigurationInfo().getEmailAccount().sendMail(
                        taEmailAddress,                // from
                        Arrays.asList(student),        // to
                        null,                          // cc
                        null,                          // bcc
                        subject,                       // subject
                        body,                          // body
                        Arrays.asList(attachmentPath));// attachments
            }
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String notificationMessage = "At " + now + ", " + taLogin +
                " submitted grading for assignment " +
                _part.getAssignment().getName() +
                " for the following students: <blockquote>";

        for(Group group : _submittedGroups)
        {
            for(String student : group.getMembers())
            {
                notificationMessage += student;
                if(attachGradingSheets)
                {
                    notificationMessage += " [Grading Sheet Attached]";
                }
                notificationMessage += "<br/>";
            }
        }

        notificationMessage += "</blockquote> The following message was sent " +
                "to the students: <blockquote>" + body + "</blockquote>";

        Allocator.getConfigurationInfo().getEmailAccount().sendMail(
                taEmailAddress,                                        // from
                Arrays.asList(taEmailAddress),                         // to
                Allocator.getConfigurationInfo().getNotifyAddresses(), // cc
                null,                                                  // bcc
                subject,                                               // subject
                notificationMessage,                                   // body
                null);                                                 //attachments
    }
}