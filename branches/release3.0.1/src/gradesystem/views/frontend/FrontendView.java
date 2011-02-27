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
import gradesystem.rubric.RubricSaveListener;
import gradesystem.views.shared.ErrorView;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A frontend view to be used by TAs that are grading.
 * <br/><br/>
 * In order to have a very responsive user interface, this class uses a non-UI
 * thread to load information. Whenver these changes lead to UI changes, the
 * code that will do this is placed on the AWT Event Queue. In particular,
 * {@link #_assignedGroups} can be loaded using a non-UI thread. This is done
 * via the {@link #loadAssignedGrading(boolean)} method.
 *
 * @author jak2
 */
public class FrontendView extends JFrame implements RubricSaveListener
{
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
                return group.getMembers().get(0);
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
    private JButton[] _allButtons, _groupButtons;
    private Map<DistributablePart, List<GroupGradedStatus>> _assignedGroups;

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

        //Initialize GUI components necessary to show database information
        this.initializeComponents();

        //Retrieve the database information in a separate thread
        this.loadAssignedGrading(true);

        //Initialize more GUI components
        this.initializeFrameIcon();
        this.initializeMenuBar();

        this.createButtonGroups();
        
        //Setup close property
        this.initializeWindowCloseProperty();

        //Update button states
        this.updateButtonStates();

        //Display
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
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
        _groupButtons = new JButton[]{
                                         _viewReadmeButton, _openCodeButton,
                                         _printStudentButton, _runTesterButton,
                                         _runCodeButton, _gradeAssignmentButton
                                       };
    }

    /**
     * Called when a different DistributablePart is selected from the dpList
     * to update other GUI components
     */
    private void updateDPList()
    {
        DistributablePart part = _dpList.getSelectedValue();

        if(part == null)
        {
            _groupList.clearList();
        }
        else
        {
            //Create directory for the part so GRD files can be created, even if
            //no handins have been unarchived
            File partDir = Allocator.getPathServices().getUserPartDir(part);

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
        }

        //Update buttons accordingly
        this.updateButtonStates();
    }

    /**
     * Populates the group list with the Groups that the TA has been assigned to
     * grade (as recorded in the database) for the selected DistributablePart.
     */
    private void populateGroupsList()
    {
        DistributablePart selected = _dpList.getSelectedValue();
        List<GroupGradedStatus> statuses = new ArrayList(_assignedGroups.get(selected));
        Collections.sort(statuses);
        List<Group> groups = new ArrayList<Group>();
        for(GroupGradedStatus status : statuses)
        {
            groups.add(status.getGroup());
        }
        _groupList.setListData(groups, new GroupConverter(selected), true);
        if(_groupList.isSelectionEmpty())
        {
            _groupList.selectFirst();
        }

        _currentlyGradingLabel.update(_groupList.getSelectedValue());
    }

    /**
     * Enable or disable buttons based on the distributable part selected.
     */
    private void updateButtonStates()
    {
        DistributablePart part = _dpList.getSelectedValue();
 
        //If there is no distributable part selected, disable all of the buttons
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

        //If no group is selected, disable all group buttons
        if(group == null)
        {
            for(JButton button : _groupButtons)
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

        //Distributable Part list
        Dimension dpListPanelSize = new Dimension((int) (mainPanelSize.width * 0.2), mainPanelSize.height);
        Dimension dpListSize = new Dimension(dpListPanelSize.width, (int) (mainPanelSize.height * 0.95));
        Dimension dpLabelSize = new Dimension(dpListPanelSize.width,
                mainPanelSize.height - dpListSize.height - 5);

        FlowLayout layout = new FlowLayout();
        layout.setVgap(0);
        JPanel dpPanel = new JPanel(layout);
        dpPanel.setSize(dpListPanelSize);
        dpPanel.setPreferredSize(dpListPanelSize);
        JLabel dpLabel = new JLabel("<html><b>Assignment</b></html>");
        dpLabel.setPreferredSize(dpLabelSize);

        _dpList = new GenericJList<DistributablePart>();
        _dpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _dpList.usePlainFont();
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
        dpPanel.add(dpLabel);
        JScrollPane dpPane = new JScrollPane(_dpList);
        dpPane.setSize(dpListSize);
        dpPane.setPreferredSize(dpListSize);
        dpPanel.add(dpPane);
        mainPanel.add(dpPanel);

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
        _groupList.usePlainFont();
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

        //When the left key is pressed, switch focus to the distributable part list
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
        //When the right key is pressed, switch focus to the group list
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
                dpListPanelSize.width - groupListPanelSize.width -
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

        //Refresh item
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        refreshItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                loadAssignedGrading(true);
            }
        });
        menu.add(refreshItem);

        //Blacklist item
        JMenuItem blacklistItem = new JMenuItem("Modify Blacklist");
        blacklistItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        blacklistItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                Collection<TA> taList = new ArrayList();
                taList.add(USER);
                new ModifyBlacklistView(taList);
            }
        });
        menu.add(blacklistItem);

        //Quit item
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        quitItem.addActionListener(new ActionListener()
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
        menu.add(quitItem);

        //Help menu
        menu = new JMenu("Help");
        menuBar.add(menu);

        //Help contents item
        JMenuItem helpItem = new JMenuItem("Help Contents");
        helpItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                JOptionPane.showMessageDialog(FrontendView.this, "This feature is not yet available");
            }
        });
        menu.add(helpItem);

        //About
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                CakehatAboutBox.displayRelativeTo(FrontendView.this);
            }
        });
        menu.add(aboutItem);
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
        Allocator.getRubricManager().view(dp, group, false, this);
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
        Collection<Group> groups = _groupList.getValues();
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

    private final ExecutorService _submitExecutor = Executors.newSingleThreadExecutor();
    /**
     * Called when the submit grading button is clicked.
     */
    private void submitGradingButtonActionPerformed()
    {
        final DistributablePart dp = _dpList.getSelectedValue();

        if(dp != null)
        {
            final SubmitDialog sd = new SubmitDialog(dp.getAssignment(), _groupList.getValues(),
                    Allocator.getConfigurationInfo().getSubmitOptions());

            if(sd.showDialog() == JOptionPane.OK_OPTION)
            {
                final Collection<Group> selected = sd.getSelectedGroups();

                //Enter grades into database
                if(sd.submitChecked())
                {
                    Runnable submitRunnable = new Runnable()
                    {
                        public void run()
                        {
                            Map<Group, Double> handinTotals = Allocator
                                    .getRubricManager().getPartScores(dp, selected);
                            for(Group group : handinTotals.keySet())
                            {
                                try
                                {
                                    Allocator.getDatabaseIO()
                                            .enterGrade(group, dp, handinTotals.get(group));
                                }
                                catch (SQLException ex)
                                {
                                    generateErrorView("enter grade", group, dp, ex);
                                }
                            }

                            //Retrieve updated database information to reflect submitted grades
                            loadAssignedGrading(false);
                        }
                    };

                    _submitExecutor.submit(submitRunnable);
                }

                //Generate GRD files if they will be emailed or printer
                if(sd.emailChecked() || sd.printChecked())
                {
                    Runnable convertRunnable = new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                Allocator.getRubricManager()
                                        .convertToGRD(dp.getHandin(), selected);
                            }
                            catch (RubricException ex)
                            {
                                generateErrorViewMultiple("enter grade", selected, dp, ex);
                            }
                        }
                    };
                    _submitExecutor.submit(convertRunnable);
                }

                //Print GRD files
                if(sd.printChecked())
                {
                    //This needs to be placed on the submission executor so that
                    //it is guaranteed run after the rubrics have been converted
                    //to GRD files
                    Runnable printRunnable = new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                Allocator.getGradingServices()
                                        .printGRDFiles(dp.getHandin(), selected);
                            }
                            catch (ServicesException ex)
                            {
                                new ErrorView(ex, "Could not print GRD files.");
                            }
                        }
                    };
                    _submitExecutor.submit(printRunnable);
                }

                //If groups/students are to be notified
                if(sd.notifyChecked())
                {
                    //This needs to be placed on the submission executor so that
                    //it is guaranteed run after the rubrics have been converted
                    //to GRD files
                    Runnable notifyRunnable = new Runnable()
                    {
                        public void run()
                        {
                           Allocator.getGradingServices().notifyStudents(
                                   dp.getHandin(), selected, sd.emailChecked());
                        }
                    };
                    _submitExecutor.submit(notifyRunnable);
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
     * Called when a rubric is saved. This method should not be called except
     * as a listener to rubric save events.
     * <br/><br/>
     * Updates the user interface to reflect this.
     *
     * @param part
     * @param group
     */
    @Override
    public void rubricSaved(DistributablePart savedPart, Group savedGroup)
    {
        Double score = Allocator.getRubricManager().getPartScore(savedPart, savedGroup);
        boolean hasRubricScore = (score != 0);

        //Build a new map that is identical to the map currently referenced by
        //_assignedGroups except that it will reflect whether or not the saved
        //rubric has a score
        //This map is built by copying from _assignedGroups, NOT by reading
        //from the database
        //This map is built instead of directly mutating _assignedGroups so
        //that the showAssignedGradingChanges(...) method can be called which
        //will update the UI, if necessary, by comparing against _assignedGroups
        Map<DistributablePart, List<GroupGradedStatus>> updatedMap =
                new HashMap<DistributablePart, List<GroupGradedStatus>>();

        for(DistributablePart part : _assignedGroups.keySet())
        {
            List<GroupGradedStatus> statuses = _assignedGroups.get(part);

            //If this is the part, find and update the status
            if(part.equals(savedPart))
            {
                GroupGradedStatus currStatus = null;
                for(GroupGradedStatus status : statuses)
                {
                    if(status.getGroup().equals(savedGroup))
                    {
                        currStatus = status;
                        break;
                    }
                }

                //For this to have occurred, the rubric must have been open for
                //a group that is no longer assigned to the TA
                if(currStatus == null)
                {
                    return;
                }

                GroupGradedStatus newStatus = new GroupGradedStatus(savedGroup,
                        currStatus.isSubmitted(), hasRubricScore);

                int currStatusIndex = statuses.indexOf(currStatus);
                ArrayList<GroupGradedStatus> newStatuses = new ArrayList<GroupGradedStatus>(statuses);
                newStatuses.set(currStatusIndex, newStatus);
                statuses = newStatuses;
            }

            updatedMap.put(part, statuses);
        }

        this.showAssignedGradingChanges(updatedMap);
    }

    private final ExecutorService _loadAssignedGradingExecutor = Executors.newSingleThreadExecutor();
    /**
     * Retrieves from the database all of the groups that have been assigned.
     * Then determines if the grade has been submitted and if the rubric has
     * been edited.
     * <br/><br/>
     * This method may run the retrieval using a separate thread. Typically this
     * will be the desired behavior because of the time involved in retrieving
     * this information. Once it is complete, if UI changes are to be made then
     * those updates will be enqueued on the UI thread.
     * <br/><br/>
     * The likely reason this method would not be run with a separate thread is
     * if the calling code is already running on a non-UI thread.
     *
     * @param useSeparateThread 
     */
    private void loadAssignedGrading(boolean useSeparateThread)
    {
        Runnable loadRunnable = new Runnable()
        {
            public void run()
            {
                long start = System.currentTimeMillis();

                Map<DistributablePart, List<GroupGradedStatus>> newMap;
                try
                {
                    newMap = new HashMap<DistributablePart, List<GroupGradedStatus>>();

                    Set<DistributablePart> parts = Allocator.getDatabaseIO().getDPsWithAssignedStudents(USER);
                    for(DistributablePart part : parts)
                    {
                        Collection<Group> groups = Allocator.getDatabaseIO().getGroupsAssigned(part, USER);
                        Map<Group, Double> submittedScores = Allocator.getDatabaseIO()
                                .getPartScoresForGroups(part, groups);
                        Map<Group, Double> rubricScores = Allocator.getRubricManager()
                                .getPartScores(part, groups);

                        List<GroupGradedStatus> statuses = new Vector<GroupGradedStatus>();
                        newMap.put(part, statuses);

                        for(Group group : groups)
                        {
                            GroupGradedStatus status = new GroupGradedStatus(group,
                                    submittedScores.containsKey(group),
                                    rubricScores.containsKey(group) &&
                                    rubricScores.get(group) != 0);
                            statuses.add(status);
                        }
                    }
                }
                catch (CakeHatDBIOException ex)
                {
                    new ErrorView(ex, "Unable to retrieve information on who you have " +
                            "been assigned to grade");
                    return;
                }
                catch (SQLException ex)
                {
                    new ErrorView(ex, "Unable to retrieve information on who you have " +
                            "been assigned to grade");
                    return;
                }

                showAssignedGradingChanges(newMap);
            }
        };

        if(useSeparateThread)
        {
            _loadAssignedGradingExecutor.submit(loadRunnable);
        }
        else
        {
            loadRunnable.run();
        }
    }

    /**
     * Determines if there are any differences between
     * <code>newAssignedGroups</code> and {@link #_assignedGroups}. If there
     * are changes, then visually updates to reflect the changes. In either
     * case, updates {@link #_assignedGroups} to reference the data referenced
     * by <code>newAssignedGroups</code>.
     *
     * @param newAssignedGroups
     */
    private void showAssignedGradingChanges(final Map<DistributablePart, List<GroupGradedStatus>> newAssignedGroups)
    {
        //Determine if any data has changed
        boolean dpChanged = false;
        boolean groupsChangedForSelectedDP = false;

        //If currently no data has been loaded
        if(_assignedGroups == null)
        {
            dpChanged = true;
            groupsChangedForSelectedDP = true;
        }
        else
        {
            //If groups for a new part has been assigned (or all groups
            //for an existing part were removed)
            dpChanged = !Allocator.getGeneralUtilities().containSameElements(
                    newAssignedGroups.keySet(), _assignedGroups.keySet());

            //If the group or the status of a group has changed
            DistributablePart selected = _dpList.getSelectedValue();
            if(selected != null)
            {
                List<GroupGradedStatus> currGroups = _assignedGroups.get(selected);
                List<GroupGradedStatus> newGroups = newAssignedGroups.get(selected);
                groupsChangedForSelectedDP = !Allocator.getGeneralUtilities()
                        .containSameElements(currGroups, newGroups);
            }
        }

        //If anything has changed, refresh it on the UI thread
        if(dpChanged || groupsChangedForSelectedDP)
        {
            //Store as final variables so that they may be referenced in the Runnable
            final boolean updateDPList = dpChanged;
            final boolean updateGrouplist = groupsChangedForSelectedDP;

            Runnable uiRunnable = new Runnable()
            {
                public void run()
                {
                    _assignedGroups = newAssignedGroups;

                    if(updateDPList)
                    {
                        List<DistributablePart> sortedParts =
                                new ArrayList<DistributablePart>(_assignedGroups.keySet());
                        Collections.sort(sortedParts);
                        _dpList.setListData(sortedParts, new DPConverter(), true);

                        if(_dpList.isSelectionEmpty())
                        {
                            _dpList.selectFirst();
                        }
                    }

                    if(updateGrouplist)
                    {
                        DistributablePart currPart = _dpList.getSelectedValue();
                        List<GroupGradedStatus> statuses = new ArrayList(_assignedGroups.get(currPart));
                        Collections.sort(statuses);
                        List<Group> groups = new ArrayList<Group>();
                        for(GroupGradedStatus status : statuses)
                        {
                            groups.add(status.getGroup());
                        }
                        _groupList.setListData(groups, new GroupConverter(currPart), true);

                        if(_groupList.isSelectionEmpty())
                        {
                            _groupList.selectFirst();
                        }

                        //If all groups are submitted, then the part list needs
                        //to be updated to reflect this
                        _dpList.refreshList();
                    }
                }
            };

            EventQueue.invokeLater(uiRunnable);
        }
        else
        {
            _assignedGroups = newAssignedGroups;
        }
    }

    private static class GroupGradedStatus implements Comparable<GroupGradedStatus>
    {
        private final Group _group;
        private final boolean _submitted;
        private final boolean _hasRubricScore;

        public GroupGradedStatus(Group group, boolean submitted, boolean hasRubricScore)
        {
            _group = group;
            _submitted = submitted;
            _hasRubricScore = hasRubricScore;
        }

        public Group getGroup()
        {
            return _group;
        }

        public boolean isSubmitted()
        {
            return _submitted;
        }

        public boolean hasRubricScore()
        {
            return _hasRubricScore;
        }

        @Override
        public boolean equals(Object obj)
        {
            boolean equals = false;
            if(obj instanceof GroupGradedStatus)
            {
                GroupGradedStatus other = (GroupGradedStatus) obj;

                equals = other._group.equals(_group) &&
                         other._submitted == _submitted &&
                         other._hasRubricScore == _hasRubricScore;
            }

            return equals;
        }

        @Override
        public int compareTo(GroupGradedStatus other)
        {
            //Comparison on group names breaks ties
            int groupComp = this.getGroup().getName().compareTo(other.getGroup().getName());

            //Heirarchy
            // - submitted
            // - rubric score
            // - nothing
            if(this.isSubmitted() && other.isSubmitted())
            {
                return groupComp;
            }
            else if(this.isSubmitted())
            {
                return -1;
            }
            else if(other.isSubmitted())
            {
                return 1;
            }
            else if(this.hasRubricScore() && other.hasRubricScore())
            {
                return groupComp;
            }
            else if(this.hasRubricScore())
            {
                return -1;
            }
            else if(other.hasRubricScore())
            {
                return 1;
            }
            else
            {
                return groupComp;
            }
        }
    }

    private class GroupConverter implements StringConverter<Group>
    {
        private final DistributablePart _part;

        public GroupConverter(DistributablePart part)
        {
            _part = part;
        }

        public String convertToString(Group group)
        {
            //Determine status of this group
            boolean hasRubricScore = false;
            boolean submitted = false;
            List<GroupGradedStatus> statuses = _assignedGroups.get(_part);
            for(GroupGradedStatus status : statuses)
            {
                if(status.getGroup().equals(group))
                {
                    submitted = status.isSubmitted();
                    hasRubricScore = status.hasRubricScore();
                }
            }

            //Build representation
            String pre = "";
            String post = "";
            if(submitted)
            {
                pre = "<font color=green>✓</font> <font color=#686868>";
                post = "</font>";
            }
            else if(hasRubricScore)
            {
                pre = "<strong><font color=#686868>";
                post = "</font><strong>";
            }
            else
            {
                pre = "<strong>";
                post = "<strong>";
            }

            String representation = "<html>" + pre + group.getName() + post + "</html>";

            return representation;
        }
    }

    private class DPConverter implements StringConverter<DistributablePart>
    {
        public String convertToString(DistributablePart part)
        {
            //Because all parts shown have at least one group, this will never
            //be vacuously true
            boolean allGroupsSubmitted = true;
            List<GroupGradedStatus> statuses = _assignedGroups.get(part);
            for(GroupGradedStatus status : statuses)
            {
                if(!status.isSubmitted())
                {
                    allGroupsSubmitted = false;
                    break;
                }
            }

            //Build representation
            String pre = "";
            String post = "";

            if(allGroupsSubmitted)
            {
                pre = "<font color=green>✓</font> <font color=#686868>";
                post = "</font>";
            }
            else
            {
                pre = "<strong>";
                post = "</strong>";
            }

            String name = part.getAssignment().getName() + ": "  + part.getName();
            String representation = "<html>" + pre + name + post + "</html>";

            return representation;
        }
    }
}