package frontend;

import components.GenericJList;
import components.ModifyBlacklistView;
import config.Assignment;
import config.HandinPart;
import config.TA;
import gradesystem.GradeSystemAboutBox;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import utils.Allocator;
import utils.printing.Printer;

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

        public void update(String studentLogin)
        {
            if(studentLogin == null || studentLogin.isEmpty())
            {
                this.setText(_begin + _default + _end);
            }
            else
            {
                this.setText(_begin + studentLogin + _end);
            }
        }
    }

    public static void launch()
    {
        if(Allocator.getGradingUtilities().isUserTA())
        {
            new FrontendView();
        }
        else
        {
            JOptionPane.showMessageDialog(null, "You [" +
                                         Allocator.getGeneralUtilities().getUserLogin() +
                                         "] are not an authorized user.");
            System.exit(0);
        }
    }

    private GenericJList<Assignment> _assignmentList;
    private GenericJList<String> _studentList;
    private CurrentlyGradingLabel _currentlyGradingLabel;
    private JButton _runDemoButton, _viewDeductionsButton, _printAllButton,
                    _submitGradingButton, _viewReadmeButton, _openCodeButton,
                    _runTesterButton, _printStudentButton, _gradeAssignmentButton,
                    _runCodeButton;
    private JButton[] _allButtons, _studentButtons;
    private Map<String, Collection<String>> _studentGroups;

    private FrontendView()
    {
        //Frame title
        super("[cakehat] frontend - " + Allocator.getGeneralUtilities().getUserLogin());

        //Create the directory to work in
        Allocator.getGradingUtilities().makeUserGradingDirectory();

        //Initialize GUI components
        this.initializeFrameIcon();
        this.initializeMenuBar();
        this.initializeComponents();

        this.createButtonGroups();

        //Select first assignment
        _assignmentList.selectFirst();

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
     * Called when a different assignment is selected from the assignmentList
     * to update other GUI components
     */
    private void updateAssignmentList()
    {
        //Create directory for the assignment so GRD files can be created,
        //even if no assignments have been untarred
        Allocator.getGeneralUtilities().makeDirectory(Allocator.getGradingUtilities().getUserGradingDirectory()
                                                        + _assignmentList.getSelectedValue().getName());

        //Get the students assigned for this assignment
        this.populateStudentList();

        //Update buttons accordingly
        this.updateButtonStates();
    }

    /**
     * Enable or disable buttons based on the assignment selected. If there is
     * no students available to select for the assignment then all student
     * specific buttons are disabled.
     */
    private void updateButtonStates()
    {
        HandinPart part = this.getHandinPart();
 
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
        _submitGradingButton.setEnabled(part.hasRubric());
        _printAllButton.setEnabled(part.hasPrint());

        //Get selected student
        String stud = _studentList.getSelectedValue();
        if(stud != null && stud.isEmpty())
        {
            stud = null;
        }

        //If no student is selected, disable all student buttons
        if(stud == null)
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
            _gradeAssignmentButton.setEnabled(part.hasRubric());
            _runTesterButton.setEnabled(part.hasTester());
            _runCodeButton.setEnabled(part.hasRun());
            _openCodeButton.setEnabled(part.hasOpen());
            _printStudentButton.setEnabled(part.hasPrint());
            _viewReadmeButton.setEnabled(part.hasReadme(stud));
        }
    }

    /**
     * Create sall of the GUI components aside from the menu bar
     */
    private void initializeComponents()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        this.add(outerPanel);
        
        Dimension mainPanelSize = new Dimension(900,400);
        JPanel mainPanel = new JPanel();
        mainPanel.setSize(mainPanelSize);
        mainPanel.setPreferredSize(mainPanelSize);
        outerPanel.add(mainPanel, BorderLayout.NORTH);

        int gapSpace = 5;

        mainPanel.add(Box.createHorizontalStrut(gapSpace));

        //Sizes
        Dimension listPanelSize = new Dimension((int) (mainPanelSize.width * 0.15), mainPanelSize.height);
        Dimension listSize = new Dimension(listPanelSize.width, (int) (mainPanelSize.height * 0.95));
        Dimension labelSize = new Dimension(listPanelSize.width, mainPanelSize.height - listSize.height - 5);

        //Assignment
        FlowLayout layout = new FlowLayout();
        layout.setVgap(0);
        JPanel assignmentPanel = new JPanel(layout);
        assignmentPanel.setSize(listPanelSize);
        assignmentPanel.setPreferredSize(listPanelSize);
        JLabel assignmentLabel = new JLabel("<html><b>Assignment</b></html>");
        assignmentLabel.setPreferredSize(labelSize);
        _assignmentList = new GenericJList<Assignment>(Allocator.getCourseInfo().getHandinAssignments());
        _assignmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _assignmentList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    updateAssignmentList();
                }
            }
        });
        assignmentPanel.add(assignmentLabel);
        JScrollPane assignmentPane = new JScrollPane(_assignmentList);
        assignmentPane.setSize(listSize);
        assignmentPane.setPreferredSize(listSize);
        assignmentPanel.add(assignmentPane);
        mainPanel.add(assignmentPanel);

        mainPanel.add(Box.createHorizontalStrut(gapSpace));

        //Student list
        layout = new FlowLayout();
        layout.setVgap(0);
        JPanel studentPanel = new JPanel(layout);
        studentPanel.setSize(listPanelSize);
        studentPanel.setPreferredSize(listPanelSize);
        JLabel studentLabel = new JLabel("<html><b>Student</b></html>");
        studentLabel.setPreferredSize(labelSize);
        _studentList = new GenericJList<String>();
        _studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _studentList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    updateSelectedStudent();
                    updateButtonStates(); //To check for README
                }
            }
        });
        studentPanel.add(studentLabel);
        JScrollPane studentPane = new JScrollPane(_studentList);
        studentPane.setSize(listSize);
        studentPane.setPreferredSize(listSize);
        studentPanel.add(studentPane);
        mainPanel.add(studentPanel);

        //When the left key is pressed, switch focus to the assignment list
        _studentList.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent ke) {}
            public void keyReleased(KeyEvent ke) {}

            public void keyPressed(KeyEvent ke)
            {
                if(KeyEvent.VK_LEFT == ke.getKeyCode())
                {
                    _assignmentList.grabFocus();
                }
            }
        });
        //When the right key is pressed, switch focus to the student list
        _assignmentList.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent ke) {}
            public void keyReleased(KeyEvent ke) {}

            public void keyPressed(KeyEvent ke)
            {
                if(KeyEvent.VK_RIGHT == ke.getKeyCode())
                {
                    _studentList.grabFocus();
                }
            }
        });

        mainPanel.add(Box.createHorizontalStrut(gapSpace));

        //Control Panel
        Dimension controlPanelSize = new Dimension(mainPanelSize.width - 2 * listPanelSize.width - 3 * gapSpace - 35, mainPanelSize.height);
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

    private void updateSelectedStudent()
    {
        String students = null;
        if(_studentList.getSelectedValue() != null)
        {
            if(_studentGroups != null)
            {
                Collection<String> group = _studentGroups.get(_studentList.getSelectedValue());
                students = "";
                for(String student : group)
                {
                    if(group.size() != 1 && student != group.iterator().next())
                    {
                        students += ", ";
                    }
                    students += student;
                }
            }
            else
            {
                students = _studentList.getSelectedValue();
            }
        }
        _currentlyGradingLabel.update(students);
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
                TA user = null;
                String username = Allocator.getGeneralUtilities().getUserLogin();
                for (TA ta : Allocator.getCourseInfo().getTAs()) {
                    if (ta.getLogin().equals(username)) {
                        user = ta;
                    }
                }
                Collection<TA> taList = new ArrayList();
                taList.add(user);
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
                Allocator.getGradingUtilities().removeUserGradingDirectory();
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
                GradeSystemAboutBox aboutBox = new GradeSystemAboutBox(FrontendView.this);
                aboutBox.setLocationRelativeTo(FrontendView.this);
                aboutBox.setVisible(true);
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
        _runDemoButton = createButton("/gradesystem/resources/icons/32x32/applications-system.png",
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
        _printAllButton = createButton("/gradesystem/resources/icons/32x32/printer.png",
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
        _viewDeductionsButton = createButton("/gradesystem/resources/icons/32x32/text-x-generic.png",
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
        _submitGradingButton = createButton("/gradesystem/resources/icons/32x32/mail-send-receive.png",
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
        _viewReadmeButton = createButton("/gradesystem/resources/icons/32x32/document-properties.png",
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
        _openCodeButton = createButton("/gradesystem/resources/icons/32x32/document-open.png",
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
        _runTesterButton = createButton("/gradesystem/resources/icons/32x32/utilities-system-monitor.png",
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
        _printStudentButton = createButton("/gradesystem/resources/icons/32x32/printer.png",
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
        _gradeAssignmentButton = createButton("/gradesystem/resources/icons/32x32/font-x-generic.png",
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
        _runCodeButton = createButton("/gradesystem/resources/icons/32x32/go-next.png",
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
     * @param imagePath
     * @param topLine
     * @param bottomLine
     * @return the button created
     */
    private JButton createButton(String imagePath, String topLine, String bottomLine)
    {
        Icon icon = new ImageIcon(getClass().getResource(imagePath));
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
        this.getHandinPart().run(_studentList.getSelectedValue());
    }

    /**
     * Called when the grade assignment button is clicked.
     */
    private void gradeAssignmentButtonActionPerformed()
    {
        Allocator.getRubricManager().view(this.getHandinPart(), _studentList.getSelectedValue());

    }

    /**
     * Called when the print student code button is clicked.
     */
    private void printStudentButtonActionPerformed()
    {
        String printer = Allocator.getGradingUtilities().getPrinter();
        if (printer == null) {
            return;
        }
        
        this.getHandinPart().printCode(_studentList.getSelectedValue(), printer);
    }

    /**
     * Called when the run tester button is clicked.
     */
    private void runTesterButtonActionPerformed()
    {
        this.getHandinPart().runTester(_studentList.getSelectedValue());
    }

    /**
     * Called when the open code button is clicked.
     */
    private void openCodeButtonActionPerformed()
    {
        this.getHandinPart().openCode(_studentList.getSelectedValue());
    }

    /**
     * Called when the view readme button is clicked.
     */
    private void viewReadmeButtonActionPerformed()
    {
        this.getHandinPart().viewReadme(_studentList.getSelectedValue());
    }

    /**
     * Called when the run demo button is clicked.
     */
    private void runDemoButtonActionPerformed()
    {
        this.getHandinPart().runDemo();
    }

    /**
     * Called when the print all button is clicked.
     */
    private void printAllButtonActionPerformed()
    {
        String printer = Allocator.getGradingUtilities().getPrinter();
        if (printer == null) {
            return;
        }

        this.getHandinPart().printCode(_studentList.getItems(), printer);
    }

    /**
     * Called when the view deductions button is clicked.
     */
    private void viewDeductionsButtonActionPerformed()
    {
        this.getHandinPart().viewDeductionList();
    }

    /**
     * Called when the submit grading button is clicked.
     */
    private void submitGradingButtonActionPerformed()
    {
        Assignment asgn = _assignmentList.getSelectedValue();

        if(_assignmentList.getSelectedValue() != null)
        {
            
            Vector<String> students = new Vector<String>();
            Map<String,Collection<String>> groups = Allocator.getDatabaseIO().getGroups(this.getHandinPart());
            for (String s1 : _studentList.getItems()) {
                for (String s2 : groups.get(s1)) {
                    students.add(s2);
                }
            }
            SubmitDialog sd = new SubmitDialog(students);
            if (sd.showDialog() == JOptionPane.OK_OPTION)
            {

                Vector<String> selectedStudents = sd.getSelectedStudents();

                Allocator.getRubricManager().convertToGRD(asgn.getHandinPart(), selectedStudents);
                
                if (sd.submitChecked())
                {
                    Map<String, Double> handinTotals = Allocator.getRubricManager().getHandinTotals(asgn.getHandinPart(), selectedStudents);
                    for (String login : handinTotals.keySet())
                    {
                        Allocator.getDatabaseIO().enterGrade(login, asgn.getHandinPart(), handinTotals.get(login));
                    }
                }

                if (sd.printChecked())
                {
                    Allocator.getGradingUtilities().printGRDFiles(asgn.getHandinPart(), selectedStudents);
                }

                if (sd.notifyChecked())
                {
                    Allocator.getGradingUtilities().notifyStudents(asgn.getHandinPart(), selectedStudents, sd.emailChecked());
                }
            }
        }
    }

    /**
     * Returns the selected assignment's handin part.
     *
     * @return
     */
    private HandinPart getHandinPart()
    {
        if(_assignmentList.getSelectedValue() == null)
        {
            return null;
        }
        else
        {
            return _assignmentList.getSelectedValue().getHandinPart();
        }
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
                Allocator.getGradingUtilities().removeUserGradingDirectory();
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
                    icon = ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-devilish.png"));
                    break;
                case 1:
                    icon = ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-angel.png"));
                    break;
                case 2:
                    icon = ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-surprise.png"));
                    break;
                case 3:
                    icon = ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-crying.png"));
                    break;
                case 4:
                    icon = ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-monkey.png"));
                    break;
                case 5:
                    icon = ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-glasses.png"));
                    break;
            }
            this.setIconImage(icon);
        }
        catch (IOException e) { }
    }

    /**
     * Populates the student list with the logins of the students that the TA
     * has been assigned to grade (as recorded in the database) for the selected
     * assignment.
     */
    private void populateStudentList()
    {
        if(this.getHandinPart() != null)
        {
            Collection<String> students = Allocator.getDatabaseIO().getStudentsAssigned(this.getHandinPart(), Allocator.getGeneralUtilities().getUserLogin());
        
            _studentList.setListData(students);
            _studentList.selectFirst();

            //Get groups
            _studentGroups = Allocator.getDatabaseIO().getGroups(this.getHandinPart());

            updateSelectedStudent();
        }
    }
    
}