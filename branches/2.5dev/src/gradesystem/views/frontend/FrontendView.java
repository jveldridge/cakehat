package gradesystem.views.frontend;

import gradesystem.components.GenericJList;
import gradesystem.handin.ActionException;
import gradesystem.rubric.RubricException;
import gradesystem.services.ServicesException;
import gradesystem.views.shared.ModifyBlacklistView;
import gradesystem.config.TA;
import gradesystem.CakehatAboutBox;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import gradesystem.Allocator;
import gradesystem.components.GenericJList.StringConverter;
import gradesystem.database.CakeHatDBIOException;
import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;
import gradesystem.resources.icons.IconLoader;
import gradesystem.resources.icons.IconLoader.IconImage;
import gradesystem.resources.icons.IconLoader.IconSize;
import gradesystem.views.shared.ErrorView;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A frontend view to be used by TAs that are grading.
 *
 * @author jak2
 */
public class FrontendView extends JFrame
{
    //Test main
    public static void main(String[] args)
    {
        new FrontendView();
    }

    /**
     * Label that displays the currently selected student.
     */
    private class CurrentlyGradingLabel extends JLabel
    {
        private final static String _begin ="<html><b>Currently Grading</b><br/>",
                                    _end = "</html>",
                                    _default = "None";

        public CurrentlyGradingLabel()
        {
            super(_begin + _default + _end);
        }

        public void update(Group group)
        {
            if(group == null)
            {
                this.setText(_begin + _default + _end);
            }
            else
            {
                this.setText(_begin + getGroupText(group) + _end);
            }
        }

        private String getGroupText(Group group) {
            if (group.getMembers().size() == 1) {
                return group.getMembers().iterator().next();
            }
            
            String text = group.getName();

            text += " " + group.getMembers();

            return text;
        }
    }

    public static void launch()
    {
        if(Allocator.getUserServices().isUserTA())
        {
            new FrontendView();
        }
        else
        {
            JOptionPane.showMessageDialog(null, "You [" +
                                         Allocator.getUserUtilities().getUserLogin() +
                                         "] are not an authorized user.");
            System.exit(0);
        }
    }

    private final TA USER = Allocator.getUserServices().getUser();

    private GenericJList<DistributablePart> _dpList;
    private GenericJList<Group> _groupList;
    private CurrentlyGradingLabel _currentlyGradingLabel;
    private JButton _runDemoButton, _viewDeductionsButton, _printAllButton,
                    _submitGradingButton, _viewReadmeButton, _openCodeButton,
                    _runTesterButton, _printStudentButton, _gradeAssignmentButton,
                    _runCodeButton;
    private JButton[] _allButtons, _studentButtons;

    private FrontendView()
    {
        //Frame title
        super("[cakehat] frontend - " + Allocator.getUserUtilities().getUserLogin());
        
        //Create the directory to work in
        try {
            Allocator.getGradingServices().makeUserWorkspace();
        } catch (ServicesException ex) {
            new ErrorView(ex, "Could not make user cakehat workspace directory; " +
                             "functionality will be significantly impaired.  " +
                             "You are advised to restart cakehat and to send an " +
                             "error report if the problem persists.");
        }

        //Initialize GUI components
        this.initializeFrameIcon();
        this.initializeMenuBar();
        this.initializeComponents();

        this.createButtonGroups();

        //Select first assignment
        _dpList.selectFirst();

        //Setup close property
        this.initializeWindowCloseProperty();

        //Update button states
        this.updateButtonStates();

        //Display
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
    }

    /**
     * Set up the groups of buttons that will be enabled or disabled.
     */
    private void createButtonGroups()
    {
        //Group buttons so they can be enabled/disabled appropriately
        _allButtons = new JButton[] {
                                      _runDemoButton, _viewDeductionsButton,
                                      _printAllButton,_submitGradingButton,
                                      _viewReadmeButton, _openCodeButton,
                                      _runTesterButton, _printStudentButton,
                                      _gradeAssignmentButton, _runCodeButton
                                    };
        _studentButtons = new JButton[]{
                                         _viewReadmeButton, _openCodeButton,
                                         _printStudentButton, _runTesterButton,
                                         _runCodeButton, _gradeAssignmentButton
                                       };
    }


    /**
     * Called when a differentDistributablePart is selected from the dpList
     * to update other GUI components
     */
    private void updateDPList()
    {
        //Create directory for the assignment so GRD files can be created,
        //even if no assignments have been unarchived
        File partDir = Allocator.getPathServices().getUserPartDir(_dpList.getSelectedValue());
        
        try
        {
            Allocator.getFileSystemServices().makeDirectory(partDir);
        }
        catch(ServicesException e)
        {
            new ErrorView(e, "Unable to create user part directory: " + partDir.getAbsolutePath());
        }

        //Get the groups assigned for this distributable part
        this.populateGroupsList();

        //Update buttons accordingly
        this.updateButtonStates();
    }

    /**
     * Enable or disable buttons based on the distributable part selected.
     */
    private void updateButtonStates()
    {
        DistributablePart part = _dpList.getSelectedValue();
 
        //If there is no handin part selected, disable all of the buttons
        if(part == null)
        {
            for(JButton button : _allButtons)
            {
                button.setEnabled(false);
            }
            return;
        }

        //General commands
        _runDemoButton.setEnabled(part.hasDemo());
        _viewDeductionsButton.setEnabled(part.hasDeductionList());
        _submitGradingButton.setEnabled(part.hasRubricTemplate());
        _printAllButton.setEnabled(part.hasPrint());

        //Get selected student
        Group group = _groupList.getSelectedValue();

        //If no student is selected, disable all student buttons
        if(group == null)
        {
            for(JButton button : _studentButtons)
            {
                button.setEnabled(false);
            }

            _submitGradingButton.setEnabled(false);
            _printAllButton.setEnabled(false);
        }
        //If there is a student, enable buttons appropriately
        else
        {
            //Student buttons
            _gradeAssignmentButton.setEnabled(part.hasRubricTemplate());
            _runTesterButton.setEnabled(part.hasTester());
            _runCodeButton.setEnabled(part.hasRun());
            _openCodeButton.setEnabled(part.hasOpen());
            _printStudentButton.setEnabled(part.hasPrint());

            boolean hasReadme = true;
            try {
                hasReadme = part.hasReadme(group);
            } catch (ActionException ex) {
                new ErrorView(ex, "Could not determine if group " + group + " has a README");
            }
            _viewReadmeButton.setEnabled(hasReadme);
        }
    }

    /**
     * Creates all of the GUI components aside from the menu bar
     */
    private void initializeComponents()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        this.add(outerPanel);
        
        Dimension mainPanelSize = new Dimension(950,400);
        JPanel mainPanel = new JPanel();
        mainPanel.setSize(mainPanelSize);
        mainPanel.setPreferredSize(mainPanelSize);
        outerPanel.add(mainPanel, BorderLayout.NORTH);

        int gapSpace = 5;

        mainPanel.add(Box.createHorizontalStrut(gapSpace));

        //Assignment
        Dimension assignmentListPanelSize = new Dimension((int) (mainPanelSize.width * 0.2), mainPanelSize.height);
        Dimension assignmentListSize = new Dimension(assignmentListPanelSize.width, (int) (mainPanelSize.height * 0.95));
        Dimension assignmentLabelSize = new Dimension(assignmentListPanelSize.width,
                mainPanelSize.height - assignmentListSize.height - 5);

        FlowLayout layout = new FlowLayout();
        layout.setVgap(0);
        JPanel assignmentPanel = new JPanel(layout);
        assignmentPanel.setSize(assignmentListPanelSize);
        assignmentPanel.setPreferredSize(assignmentListPanelSize);
        JLabel assignmentLabel = new JLabel("<html><b>Assignment</b></html>");
        assignmentLabel.setPreferredSize(assignmentLabelSize);

        Set<DistributablePart> partsToShow;
        try {
            partsToShow = Allocator.getDatabaseIO().getDPsWithAssignedStudents(USER);
        } catch (SQLException ex) {
            new ErrorView(ex, "Could not get the list of DistributableParts for which you " +
                              "have been assigned students.  Please send an error report if " +
                              "this problem persists.  Cakehat will now close.");
            this.dispose();
            return;
        }

        List<DistributablePart> sortedParts = new ArrayList<DistributablePart>(partsToShow);
        Collections.sort(sortedParts);
        _dpList = new GenericJList<DistributablePart>(sortedParts, new DPFullNameConverter());
        _dpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _dpList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    updateDPList();
                }
            }
        });
        assignmentPanel.add(assignmentLabel);
        JScrollPane assignmentPane = new JScrollPane(_dpList);
        assignmentPane.setSize(assignmentListSize);
        assignmentPane.setPreferredSize(assignmentListSize);
        assignmentPanel.add(assignmentPane);
        mainPanel.add(assignmentPanel);

        mainPanel.add(Box.createHorizontalStrut(gapSpace));

        //Group list
        Dimension groupListPanelSize = new Dimension((int) (mainPanelSize.width * 0.15), mainPanelSize.height);
        Dimension groupListSize = new Dimension(groupListPanelSize.width, (int) (mainPanelSize.height * 0.95));
        Dimension groupLabelSize = new Dimension(groupListPanelSize.width, mainPanelSize.height - groupListSize.height - 5);

        layout = new FlowLayout();
        layout.setVgap(0);
        JPanel groupPanel = new JPanel(layout);
        groupPanel.setSize(groupListPanelSize);
        groupPanel.setPreferredSize(groupListPanelSize);
        JLabel groupLabel = new JLabel("<html><b>Student</b></html>");
        groupLabel.setPreferredSize(groupLabelSize);
        _groupList = new GenericJList<Group>();
        _groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _groupList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    _currentlyGradingLabel.update(_groupList.getSelectedValue());
                    updateButtonStates(); //To check for README
                }
            }
        });
        groupPanel.add(groupLabel);
        JScrollPane groupPane = new JScrollPane(_groupList);
        groupPane.setSize(groupListSize);
        groupPane.setPreferredSize(groupListSize);
        groupPanel.add(groupPane);
        mainPanel.add(groupPanel);

        //When the left key is pressed, switch focus to the assignment list
        _groupList.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent ke) {}
            public void keyReleased(KeyEvent ke) {}

            public void keyPressed(KeyEvent ke)
            {
                if(KeyEvent.VK_LEFT == ke.getKeyCode())
                {
                    _dpList.grabFocus();
                }
            }
        });
        //When the right key is pressed, switch focus to the student list
        _dpList.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent ke) {}
            public void keyReleased(KeyEvent ke) {}

            public void keyPressed(KeyEvent ke)
            {
                if(KeyEvent.VK_RIGHT == ke.getKeyCode())
                {
                    _groupList.grabFocus();
                }
            }
        });

        mainPanel.add(Box.createHorizontalStrut(gapSpace));

        //Control Panel
        Dimension controlPanelSize = new Dimension(mainPanelSize.width -
                assignmentListPanelSize.width - groupListPanelSize.width -
                3 * gapSpace - 35, mainPanelSize.height);
        layout = new FlowLayout();
        JPanel controlPanel = new JPanel(layout);
        layout.setVgap(0);
        controlPanel.setSize(controlPanelSize);
        controlPanel.setPreferredSize(controlPanelSize);
        mainPanel.add(controlPanel);

        //Currently grading panel
        Dimension gradingPanelSize = new Dimension(controlPanelSize.width, 35);
        JPanel gradingPanel = new JPanel(new BorderLayout());
        gradingPanel.setSize(gradingPanelSize);
        gradingPanel.setPreferredSize(gradingPanelSize);
        _currentlyGradingLabel = new CurrentlyGradingLabel();
        gradingPanel.add(_currentlyGradingLabel, BorderLayout.WEST);
        controlPanel.add(gradingPanel);

        //General commands
        Dimension generalCommandsSize = new Dimension(controlPanelSize.width, 150);
        JPanel generalCommandsPanel = new JPanel(new BorderLayout());
        generalCommandsPanel.setSize(generalCommandsSize);
        generalCommandsPanel.setPreferredSize(generalCommandsSize);
        generalCommandsPanel.add(new JLabel("<html><b>General Commands</b></html>"), BorderLayout.WEST);
        //General command buttons
        Dimension generalButtonsSize = new Dimension(generalCommandsSize.width, generalCommandsSize.height - 30);
        JPanel generalButtonsPanel = new JPanel(new GridLayout(2,2,4,4));
        generalButtonsPanel.setSize(generalButtonsSize);
        generalButtonsPanel.setPreferredSize(generalButtonsSize);
        this.initializeGeneralCommandButtons(generalButtonsPanel);

        generalCommandsPanel.add(generalButtonsPanel, BorderLayout.SOUTH);
        controlPanel.add(generalCommandsPanel);

        //Selected student commands
        Dimension studentCommandsSize = new Dimension(controlPanelSize.width, 210);
        JPanel studentCommandsPanel = new JPanel(new BorderLayout());
        studentCommandsPanel.setSize(studentCommandsSize);
        studentCommandsPanel.setPreferredSize(studentCommandsSize);
        studentCommandsPanel.add(new JLabel("<html><b>Selected Student Commands</b></html>"), BorderLayout.WEST);
        //Selected student command buttons
        Dimension studentButtonsSize = new Dimension(studentCommandsSize.width, studentCommandsSize.height - 30);
        JPanel studentButtonsPanel = new JPanel(new GridLayout(3,2,4,4));
        studentButtonsPanel.setSize(studentButtonsSize);
        studentButtonsPanel.setPreferredSize(studentButtonsSize);
        this.initializeStudentCommandButtons(studentButtonsPanel);
        studentCommandsPanel.add(studentButtonsPanel, BorderLayout.SOUTH);
        controlPanel.add(studentCommandsPanel);
    }

    /**
     * Create the menu bar
     */
    private void initializeMenuBar()
    {
        //Menu bar
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        //File menu
        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        //Quit item
        JMenuItem menuItem = new JMenuItem("Modify Blacklist");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae) {
                Collection<TA> taList = new ArrayList();
                taList.add(USER);
                new ModifyBlacklistView(taList);
            }
        });
        menu.add(menuItem);

        //Quit item
        menuItem = new JMenuItem("Quit");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    Allocator.getGradingServices().removeUserWorkspace();
                }
                catch(ServicesException ex)
                {
                    new ErrorView(ex, "Unable to remove your cakehat workspace directory.");
                }

                System.exit(0);
            }
        });
        menu.add(menuItem);

        //Help menu
        menu = new JMenu("Help");
        menuBar.add(menu);

        //Help contents item
        menuItem = new JMenuItem("Help Contents");
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                JOptionPane.showMessageDialog(FrontendView.this, "This feature is not yet available");
            }
        });
        menu.add(menuItem);

        //About
        menuItem = new JMenuItem("About");
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                CakehatAboutBox.displayRelativeTo(FrontendView.this);
            }
        });
        menu.add(menuItem);
    }

    /**
     * Creates the assignment wide buttons
     *
     * @param generalButtonsPanel
     */
    private void initializeGeneralCommandButtons(JPanel generalButtonsPanel)
    {
        //Run Demo
        _runDemoButton = createButton(IconImage.APPLICATIONS_SYSTEM,
                                      "Run Demo", "Run the assignment demo");
        _runDemoButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                runDemoButtonActionPerformed();
            }

        });
        generalButtonsPanel.add(_runDemoButton);

        //Print All
        _printAllButton = createButton(IconImage.PRINTER,
                                       "Print All", "Print code for all students");
        _printAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                printAllButtonActionPerformed();
            }

        });
        generalButtonsPanel.add(_printAllButton);

        //View Deductions
        _viewDeductionsButton = createButton(IconImage.TEXT_X_GENERIC,
                                       "View Deductions", "Display the deductions list");
        _viewDeductionsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewDeductionsButtonActionPerformed();
            }

        });
        generalButtonsPanel.add(_viewDeductionsButton);

        //Submit Grading
        _submitGradingButton = createButton(IconImage.MAIL_SEND_RECEIVE,
                                            "Submit Grading", "Submit all graded assignments");
        _submitGradingButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                submitGradingButtonActionPerformed();
            }

        });
        generalButtonsPanel.add(_submitGradingButton);
    }

    /**
     * Creates the student specific buttons.
     *
     * @param studentButtonsPanel
     */
    private void initializeStudentCommandButtons(JPanel studentButtonsPanel)
    {
        //View Readme
        _viewReadmeButton = createButton(IconImage.DOCUMENT_PROPERTIES,
                                         "View Readme", "Display the student's readme");
        _viewReadmeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewReadmeButtonActionPerformed();
            }

        });
        studentButtonsPanel.add(_viewReadmeButton);

        //Open Code
        _openCodeButton = createButton(IconImage.DOCUMENT_OPEN,
                                       "Open Code", "Open the student's code");
        _openCodeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                openCodeButtonActionPerformed();
            }

        });
        studentButtonsPanel.add(_openCodeButton);

        //Run Tester
        _runTesterButton = createButton(IconImage.UTILITIES_SYSTEM_MONITOR,
                                        "Run Tester", "Run tester on the student's code");
        _runTesterButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                runTesterButtonActionPerformed();
            }

        });
        studentButtonsPanel.add(_runTesterButton);

        //Print Code
        _printStudentButton = createButton(IconImage.PRINTER,
                                           "Print Code", "Print the student's code");
        _printStudentButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                printStudentButtonActionPerformed();
            }

        });
        studentButtonsPanel.add(_printStudentButton);

        //Grade Assignment
        _gradeAssignmentButton = createButton(IconImage.FONT_X_GENERIC,
                                              "Grade Assignment", "Grade the student's assignment");
        _gradeAssignmentButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                gradeAssignmentButtonActionPerformed();
            }

        });
        studentButtonsPanel.add(_gradeAssignmentButton);

        //Run Code
        _runCodeButton = createButton(IconImage.GO_NEXT,
                                      "Run Code", "Run the student's code");
        _runCodeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                runCodeButtonActionPerformed();
            }

        });
        studentButtonsPanel.add(_runCodeButton);
    }

    /**
     * Creates a button with an image on the left hand side and then two lines
     * of text to the right of the image. The top line of text is bolded.
     *
     * @param image
     * @param topLine
     * @param bottomLine
     * @return the button created
     */
    private JButton createButton(IconImage image, String topLine, String bottomLine)
    {
        Icon icon = IconLoader.loadIcon(IconSize.s32x32, image);
        JButton button = new JButton("<html><b>" + topLine + "</b><br/>" + bottomLine + "</html>", icon);
        button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        button.setIconTextGap(10);

        return button;
    }

    /**
     * Called when the run code button is clicked.
     */
    private void runCodeButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.run(group);
        } catch (ActionException ex) {
            this.generateErrorView("run code", group, dp, ex);
        }
    }

    /**
     * Called when the grade assignment button is clicked.
     */
    private void gradeAssignmentButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        Allocator.getRubricManager().view(dp, group, false);
    }

    /**
     * Called when the print student code button is clicked.
     */
    private void printStudentButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.print(group);
        } catch (ActionException ex) {
            this.generateErrorView("print code", group, dp, ex);
        }
    }

    /**
     * Called when the run tester button is clicked.
     */
    private void runTesterButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.runTester(group);
        } catch (ActionException ex) {
            this.generateErrorView("run tester", group, dp, ex);
        }
    }

    /**
     * Called when the open code button is clicked.
     */
    private void openCodeButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.open(group);
        } catch (ActionException ex) {
            this.generateErrorView("open code", group, dp, ex);
        }
    }

    /**
     * Called when the view readme button is clicked.
     */
    private void viewReadmeButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.viewReadme(group);
        } catch (ActionException ex) {
            this.generateErrorView("view README", group, dp, ex);
        }
    }

    /**
     * Called when the run demo button is clicked.
     */
    private void runDemoButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        try {
            dp.runDemo();
        } catch (ActionException ex) {
            this.generateErrorView("run code", null, dp, ex);
        }
    }

    /**
     * Called when the print all button is clicked.
     */
    private void printAllButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Collection<Group> groups = _groupList.getItems();
        try {
            dp.print(groups);
        } catch (ActionException ex) {
            this.generateErrorViewMultiple("print code", groups, dp, ex);
        }
    }

    /**
     * Called when the view deductions button is clicked.
     */
    private void viewDeductionsButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        try {
            dp.viewDeductionList();
        } catch (FileNotFoundException ex) {
            this.generateErrorView("view deductions list", null, dp, ex);
        }
    }

    /**
     * Called when the submit grading button is clicked.
     */
    private void submitGradingButtonActionPerformed() {
        DistributablePart dp = _dpList.getSelectedValue();

        if (dp != null) {
            SubmitDialog sd = new SubmitDialog(dp.getAssignment(), _groupList.getItems(), Allocator.getConfigurationInfo().getSubmitOptions());
            if (sd.showDialog() == JOptionPane.OK_OPTION) {

                Collection<Group> selected = sd.getSelectedGroups();

                if (sd.submitChecked() || sd.printChecked()) {
                    try {
                        Allocator.getRubricManager().convertToGRD(dp.getHandin(), selected);
                    } catch (RubricException ex) {
                        this.generateErrorViewMultiple("enter grade", selected, dp, ex);
                    }
                }

                if (sd.submitChecked()) {
                    Map<Group, Double> handinTotals = Allocator.getRubricManager().getPartScores(dp, selected);
                    for (Group group : handinTotals.keySet()) {
                        try {
                            Allocator.getDatabaseIO().enterGrade(group, dp, handinTotals.get(group));
                        } catch (SQLException ex) {
                            this.generateErrorView("enter grade", group, dp, ex);
                        }
                    }
                }

                if (sd.printChecked()) {
                    try {
                        Allocator.getGradingServices().printGRDFiles(dp.getHandin(), selected);
                    } catch (ServicesException ex) {
                        new ErrorView(ex, "Could not print GRD files.");
                    }
                }

                if (sd.notifyChecked()) {
                    Allocator.getGradingServices().notifyStudents(dp.getHandin(), selected, sd.emailChecked());
                }
            }
        }
    }

    /**
     * Creates an ErrorView with the message "Could not <msgInsert> for group <group> on
     * part <dp> of assignment <dp.getAssignment()> and the given Exception.
     *
     * Use generateErrorViewMultiple when the failing operation was to be run on multiple groups.
     *
     * @param msgInsert
     * @param group
     * @param dp
     * @param cause
     */
    private void generateErrorView(String msgInsert, Group group, DistributablePart dp, Exception cause) {
        String message = "Could not " + msgInsert;
        
        if (group != null) {
            message += " for group " + group;
        }

        if (dp != null) {
            message += " on part " + dp + " of assignment " + dp.getAssignment();
        }

        message += ".";

        new ErrorView(cause, message);
    }

    /**
     * Creates an ErrorView with the message "Could not <msgInsert> for groups <groups> on
     * part <dp> of assignment <dp.getAssignment()> and the given Exception.
     *
     * Use generateErrorView when the failing operation was to be run on a single group.
     *
     * @param msgInsert
     * @param group
     * @param dp
     * @param cause
     */
    private void generateErrorViewMultiple(String msgInsert, Collection<Group> groups, DistributablePart dp, Exception cause) {
        String message = "Could not " + msgInsert;

        if (!groups.isEmpty()) {
            message += " for groups " + groups;
        }

        if (dp != null) {
            message += " on part " + dp + " of assignment " + dp.getAssignment();
        }

        message += ".";

        new ErrorView(cause, message);
    }


    /**
     * Ensures when the window closes the program terminates and that the
     * user's grading directory is removing.
     */
    private void initializeWindowCloseProperty()
    {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        this.addWindowListener(new WindowAdapter()
        {
            @Override 
            public void windowClosing(WindowEvent e)
            {
                //remove user grading directory when frontend is closed
                try
                {
                    Allocator.getGradingServices().removeUserWorkspace();
                }
                catch(ServicesException ex)
                {
                    new ErrorView(ex, "Unable to remove your cakehat workspace directory.");
                }
            }
        });
    }

    /**
     * Initializes this frame's icon. Only visible on certain operating systems
     * and window managers.
     */
    private void initializeFrameIcon()
    {
        try
        {
            //randomly selects one of 5 icons
            BufferedImage icon = null;
            switch ((int) (Math.random() * 5))
            {
                case 0:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_DEVILISH);
                    break;
                case 1:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_ANGEL);
                    break;
                case 2:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_SURPRISE);
                    break;
                case 3:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_CRYING);
                    break;
                case 4:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_MONKEY);
                    break;
                case 5:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_GLASSES);
                    break;
            }
            this.setIconImage(icon);
        }
        catch (IOException e) { }
    }

    /**
     * Populates the group list with the Groups that the TA has been assigned to
     * grade (as recorded in the database) for the selected DistributablePart.
     */
    private void populateGroupsList()
    {
        DistributablePart selected = _dpList.getSelectedValue();

        if(selected != null)
        {
            Collection<Group> assigned = Collections.emptyList();
            try {
                assigned = Allocator.getDatabaseIO().getGroupsAssigned(selected, USER);
            } catch (CakeHatDBIOException ex) {
                new ErrorView(ex, "Could not retrieve students/groups assigned to you " +
                                  "for distributable part " + selected + ".");
            } catch (SQLException ex) {
                new ErrorView(ex, "Could not retrieve students/groups assigned to you " +
                                  "for distributable part " + selected + ".");
            }

            _groupList.setListData(assigned);
            _groupList.selectFirst();

            _currentlyGradingLabel.update(_groupList.getSelectedValue());
        }
    }

    private static class DPFullNameConverter implements StringConverter<DistributablePart> {

        public String convertToString(DistributablePart item) {
            return item.getAssignment().getName() + ": "  + item.getName();
        }
        
    }
    
}