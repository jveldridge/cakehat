package backend;

import backend.assignmentdist.AssignmentdistView;
import backend.assignmentdist.ReassignView;
import backend.gradereport.GradeReportView;
import backend.stathist.StatHistView;
import components.ParameterizedJList;
import config.Assignment;
import config.HandinPart;
import config.Part;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import utils.Allocator;

/**
 *
 * @author jak2
 */
public class ProgrammaticNewBackend extends JFrame
{
    public static void main(String[] args)
    {
        new ProgrammaticNewBackend();
    }

    private class SelectedLabel extends JLabel
    {
        private String _boldedText, _multiDescriptor,
                       _defaultText = "none selected";

        public SelectedLabel(String boldedText, String multiDescriptor)
        {
            _boldedText = boldedText;
            _multiDescriptor = multiDescriptor;

            this.setText(new Vector());
        }

        public void setText(Collection objects)
        {
            if(objects.isEmpty())
            {
                this.setText(_defaultText);
            }
            else if(objects.size() == 1)
            {
                this.setText(objects.iterator().next().toString());
            }
            else
            {
                String text = objects.size() + " " + _multiDescriptor;
                this.setText(text);
            }

        }

        @Override
        public void setText(String text)
        {
            String msg = "<html><b> &nbsp;" + _boldedText + ": </b>";
            msg += text;
            msg += "</html>";

            super.setText(msg);
        }
    }

    private class AssignmentOption
    {
        private String _name;
        private DefaultListModel _model;

        public AssignmentOption(String name, Collection<Assignment> asgns)
        {
            _name = name;
            _model = new DefaultListModel();
            for(Assignment asgn : asgns)
            {
                _model.addElement(asgn);
            }
        }

        @Override
        public String toString()
        {
            return _name;
        }

        public DefaultListModel getModel()
        {
            return _model;
        }
    }

    //Size constants
    private static final Dimension MAIN_PANEL_SIZE = new Dimension(1150, 700);
    private static final Dimension LIST_PANEL_SIZE = new Dimension(185, MAIN_PANEL_SIZE.height);
    private static final Dimension GENERAL_COMMANDS_PANEL_SIZE = new Dimension(195, MAIN_PANEL_SIZE.height);
    private static final Dimension MIDDLE_PANEL_SIZE = new Dimension(MAIN_PANEL_SIZE.width -
                                                                     2 * LIST_PANEL_SIZE.width -
                                                                     GENERAL_COMMANDS_PANEL_SIZE.width,
                                                                     MAIN_PANEL_SIZE.height);
    private static final Dimension SELECTED_ASSIGNMENT_PANEL_SIZE = new Dimension(MIDDLE_PANEL_SIZE.width, 100);
    private static final Dimension STUDENT_BUTTON_PANEL_SIZE = new Dimension(200, MIDDLE_PANEL_SIZE.height -
                                                                                  SELECTED_ASSIGNMENT_PANEL_SIZE.height);
    private static final Dimension MULTI_PANEL_SIZE = new Dimension(MIDDLE_PANEL_SIZE.width - STUDENT_BUTTON_PANEL_SIZE.width,
                                                                   MIDDLE_PANEL_SIZE.height - SELECTED_ASSIGNMENT_PANEL_SIZE.height);



    private JButton //Assignment wide buttons
                    _createDistributionButton, _reassignGradingButton, _importLabsButton,
                    _previewRubricButton, _viewDeductionsButton, _runDemoButton,
                    //Student buttons
                    _chartsButton, _emailReportsButton, _extensionButton,
                    _exemptionButton, _openCodeButton, _runCodeButton,
                    _testCodeButton, _printCodeButton, _viewRubricButton,
                    _printRubricButton, _disableStudentButton,
                    //General command buttons
                    _modifyBlacklistButton, _editConfigurationButton, _exportGradesButton,
                    _resetDatabaseButton;
    private JButton[] _assignmentButtons, _generalCommandsButtons, _studentButtons;
    private SelectedLabel _selectedAssignmentLabel, _selectedStudentLabel;
    private ParameterizedJList<Assignment> _assignmentList;
    private ParameterizedJList<String> _studentList;
    private JPanel _cardPanel = new JPanel();
    private List<String> _studentLogins;
    private final static String WELCOME_PANEL_TAG = "Welcome panel",
                                MULTI_SELECT_PANEL_TAG = "Multiple selected students panel",
                                SINGLE_SELECT_PANEL_TAG = "Single selected students panel";
    private CardLayout _cardLayout;
    private SingleSelectionPanel _singleSelectionPanel;

    public ProgrammaticNewBackend()
    {
        super("[cakehat] backend - " + Allocator.getGeneralUtilities().getUserLogin());

        //student logins
        _studentLogins = new LinkedList(Allocator.getDatabaseIO().getAllStudents().keySet());
        Collections.sort(_studentLogins);

        this.initComponents();
        this.initButtonGroups();
        this.updateGUI();

        this.pack();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initButtonGroups()
    {
        _assignmentButtons = new JButton[]
        {
          _createDistributionButton, _reassignGradingButton,
          _importLabsButton, _previewRubricButton, _viewDeductionsButton,
          _runDemoButton
        };

        _generalCommandsButtons = new JButton[]
        {
          _modifyBlacklistButton, _editConfigurationButton, _exportGradesButton,
          _resetDatabaseButton
        };
        
        _studentButtons = new JButton[]
        {
          _chartsButton, _emailReportsButton, _extensionButton, _exemptionButton,
          _openCodeButton, _runCodeButton, _testCodeButton, _printCodeButton,
          _viewRubricButton, _printRubricButton, _disableStudentButton
        };
    }

    private void initComponents()
    {
        FlowLayout layout = new FlowLayout();
        layout.setHgap(0);
        layout.setVgap(0);
        JPanel mainPanel = new JPanel(layout);
        mainPanel.setPreferredSize(MAIN_PANEL_SIZE);
        this.add(mainPanel);

        //assignment list panel
        JPanel assignmentListPanel = new JPanel();
        assignmentListPanel.setPreferredSize(LIST_PANEL_SIZE);
        this.initAssignmentListPanel(assignmentListPanel);
        mainPanel.add(assignmentListPanel);

        //student list panel
        JPanel studentListPanel = new JPanel();
        studentListPanel.setPreferredSize(LIST_PANEL_SIZE);
        this.initStudentListPanel(studentListPanel);
        mainPanel.add(studentListPanel);

        //middle panel
        BorderLayout bLayout = new BorderLayout();
        bLayout.setHgap(0);
        bLayout.setVgap(0);
        JPanel middlePanel = new JPanel(bLayout);
        middlePanel.setPreferredSize(MIDDLE_PANEL_SIZE);
        middlePanel.setBackground(Color.BLUE);
        mainPanel.add(middlePanel);

        //selected assignment button panel
        JPanel selectedAssignmentPanel = new JPanel();
        selectedAssignmentPanel.setPreferredSize(SELECTED_ASSIGNMENT_PANEL_SIZE);
        this.initSelectedAssignmentPanel(selectedAssignmentPanel);
        middlePanel.add(selectedAssignmentPanel, BorderLayout.NORTH);

        //selected student panel / card panel
        JPanel multiPanel = new JPanel();
        multiPanel.setPreferredSize(MULTI_PANEL_SIZE);
        this.initMultiPanel(multiPanel);
        middlePanel.add(multiPanel, BorderLayout.WEST);

        //selected student button panel
        JPanel studentButtonPanel = new JPanel();
        studentButtonPanel.setPreferredSize(STUDENT_BUTTON_PANEL_SIZE);
        this.initStudentButtonPanel(studentButtonPanel);
        middlePanel.add(studentButtonPanel, BorderLayout.EAST);

        //general commands button
        JPanel generalCommandsPanel = new JPanel();
        generalCommandsPanel.setPreferredSize(GENERAL_COMMANDS_PANEL_SIZE);
        this.initGeneralCommandPanel(generalCommandsPanel);
        mainPanel.add(generalCommandsPanel);

        this.setResizable(false);
    }

    private static Dimension MULTI_PANEL_LABEL_SIZE = new Dimension(MULTI_PANEL_SIZE.width - 10, 20);
    static Dimension MULTI_PANEL_CARD_SIZE = new Dimension(MULTI_PANEL_SIZE.width - 10,
                                                                   MULTI_PANEL_SIZE.height -
                                                                   MULTI_PANEL_LABEL_SIZE.height - 15);
    private void initMultiPanel(JPanel panel)
    {
        //Student label
        _selectedStudentLabel = new SelectedLabel("Selected Student", "students selected");
        _selectedStudentLabel.setPreferredSize(MULTI_PANEL_LABEL_SIZE);
        panel.add(_selectedStudentLabel);

        //Card panel
        _cardLayout = new CardLayout();
        _cardPanel = new JPanel(_cardLayout);
        _cardPanel.setPreferredSize(MULTI_PANEL_CARD_SIZE);
        panel.add(_cardPanel);

        //Welcome card
        JPanel welcomeCard = new JPanel();
        welcomeCard.setPreferredSize(MULTI_PANEL_CARD_SIZE);
        _cardPanel.add(welcomeCard, WELCOME_PANEL_TAG);
        this.initWelcomePanel(welcomeCard);

        //Multiselect card
        JPanel multiSelectCard = new JPanel();
        multiSelectCard.setPreferredSize(MULTI_PANEL_CARD_SIZE);
        _cardPanel.add(multiSelectCard, MULTI_SELECT_PANEL_TAG);
        this.initMultiSelectPanel(multiSelectCard);

        //Singleselect card
        _singleSelectionPanel = new SingleSelectionPanel();
        _cardPanel.add(_singleSelectionPanel, SINGLE_SELECT_PANEL_TAG);
    }

    private void initMultiSelectPanel(JPanel panel)
    {
        
    }

    private void initWelcomePanel(JPanel panel)
    {
        JLabel welcomeLabel = new JLabel("<html><font size=\"6\"><b>Welcome to Cakehat!</b></font></html>");
        panel.add(welcomeLabel);

        JLabel instructionsLabel = new JLabel("<html><br/>To get started, select one " +
                                              "or more students<br/>or assignments" +
                                              " from the lists to the left. <br/>" +
                                              "<br/>For more information, consult " +
                                              "the Help menu.</html>");
        panel.add(instructionsLabel);
    }

    //List panel sizes (for both student & assignment)
    private static final Dimension LIST_CONTROL_PANEL_SIZE = new Dimension(LIST_PANEL_SIZE.width - 10, 80);
    private static final Dimension LIST_LABEL_SIZE = new Dimension(LIST_CONTROL_PANEL_SIZE.width, 20);
    private static final Dimension LIST_GAP_SPACE_SIZE = new Dimension(LIST_CONTROL_PANEL_SIZE.width, 5);
    private static final Dimension LIST_BUTTON_PANEL_SIZE = new Dimension(LIST_CONTROL_PANEL_SIZE.width, 25);
    private static final Dimension LIST_SELECTOR_SIZE = new Dimension(LIST_CONTROL_PANEL_SIZE.width, 25);
    private static final Dimension LIST_LIST_PANE_SIZE = new Dimension(LIST_PANEL_SIZE.width - 10,
                                                                       LIST_PANEL_SIZE.height -
                                                                       LIST_CONTROL_PANEL_SIZE.height - 10);

    private void initStudentListPanel(JPanel panel)
    {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //Top panel: Assignments label, select all / select none buttons, filter text area
        //Dimension controlPanelSize = new Dimension(STUDENT_LIST_PANEL_SIZE.width - 10, 80);
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        controlPanel.setPreferredSize(LIST_CONTROL_PANEL_SIZE);
        panel.add(controlPanel);

        JLabel studentLabel = new JLabel("<html><b>Students</b></html>");
        studentLabel.setPreferredSize(LIST_LABEL_SIZE);
        controlPanel.add(studentLabel, BorderLayout.NORTH);

        //Gap space
        controlPanel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1,2,5,5));
        buttonPanel.setPreferredSize(LIST_BUTTON_PANEL_SIZE);
        controlPanel.add(buttonPanel);

        //Select all
        JButton selectAllButton = new JButton("All");
        selectAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                _studentList.selectAll();
            }
        });
        buttonPanel.add(selectAllButton, BorderLayout.WEST);

        //Select none
        JButton selectNoneButton = new JButton("None");
        selectNoneButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                _studentList.clearSelection();
            }
        });
        buttonPanel.add(selectNoneButton, BorderLayout.EAST);
        
        //Gap space
        controlPanel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //Student filter
        final JTextField filterField = new JTextField();
        filterField.setPreferredSize(LIST_SELECTOR_SIZE);
        filterField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent ke)
            {
                //term to filter against
                String filterTerm = filterField.getText();

                List<String> matchingLogins;
                //if no filter term, include all logins
                if(filterTerm.isEmpty())
                {
                    matchingLogins = _studentLogins;
                }
                //otherwise compared against beginning of each login
                else
                {
                    matchingLogins = new Vector<String>();
                    for(String login : _studentLogins)
                    {
                        if(login.startsWith(filterTerm))
                        {
                            matchingLogins.add(login);
                        }
                    }
                }

                //display matching logins
                _studentList.setListData(matchingLogins);
                _studentList.selectFirst();

                if ((ke.getKeyCode() == KeyEvent.VK_ENTER) || (ke.getKeyCode() == KeyEvent.VK_TAB))
                {
                    filterField.setText(matchingLogins.get(0));
                }
            }
        });
        controlPanel.add(filterField, BorderLayout.SOUTH);

        //Gap space
        panel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //List
        _studentList = new ParameterizedJList<String>(_studentLogins);
        _studentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _studentList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    studentListValueChanged();
                }
            }
        });
        JScrollPane studentPane = new JScrollPane(_studentList);
        studentPane.setPreferredSize(LIST_LIST_PANE_SIZE);
        panel.add(studentPane);
    }

    private void initAssignmentListPanel(JPanel panel)
    {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //Top panel: Assignments label, select all / select none buttons, drop down list
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        controlPanel.setPreferredSize(LIST_CONTROL_PANEL_SIZE);
        panel.add(controlPanel);

        //Label
        JLabel assignmentLabel = new JLabel("<html><b>Assignments</b></html>");
        assignmentLabel.setPreferredSize(LIST_LABEL_SIZE);
        controlPanel.add(assignmentLabel);

        //Gap space
        controlPanel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1,2,5,5));
        buttonPanel.setPreferredSize(LIST_BUTTON_PANEL_SIZE);
        controlPanel.add(buttonPanel);

        //Select all
        JButton selectAllButton = new JButton("All");
        selectAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                _assignmentList.selectAll();
            }
        });
        buttonPanel.add(selectAllButton);

        //Select none
        JButton selectNoneButton = new JButton("None");
        selectNoneButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                _assignmentList.clearSelection();
            }
        });
        buttonPanel.add(selectNoneButton);

        //Gap space
        controlPanel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //Assignment box
        AssignmentOption[] options = {
                                        new AssignmentOption("All Assignments", Allocator.getCourseInfo().getAssignments()),
                                        new AssignmentOption("With Handin Part", Allocator.getCourseInfo().getHandinAssignments()),
                                        new AssignmentOption("With NonHandin Parts", Allocator.getCourseInfo().getNonHandinAssignments()),
                                        new AssignmentOption("With Lab Parts", Allocator.getCourseInfo().getLabAssignments())
                                     };
        final JComboBox assignmentsBox = new JComboBox(options);
        assignmentsBox.setPreferredSize(LIST_SELECTOR_SIZE);
        assignmentsBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                AssignmentOption option = (AssignmentOption) assignmentsBox.getSelectedItem();
                _assignmentList.setModel(option.getModel());
            }

        });
        controlPanel.add(assignmentsBox);

        //Gap space
        panel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //List
        _assignmentList = new ParameterizedJList<Assignment>(Allocator.getCourseInfo().getAssignments());
        _assignmentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _assignmentList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    assignmentListValueChanged();
                }
            }
        });
        JScrollPane assignmentPane = new JScrollPane(_assignmentList);
        assignmentPane.setPreferredSize(LIST_LIST_PANE_SIZE);
        panel.add(assignmentPane);
    }

    private void initStudentButtonPanel(JPanel panel)
    {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //buffer room
        int bufferHeight = 90;
        int bufferWidth = 10;

        //Add some vertical space
        panel.add(Box.createRigidArea(new Dimension(STUDENT_BUTTON_PANEL_SIZE.width, bufferHeight / 2)));

        //Button panel
        Dimension buttonPanelSize = new Dimension(STUDENT_BUTTON_PANEL_SIZE.width - bufferWidth,
                                                  STUDENT_BUTTON_PANEL_SIZE.height - bufferHeight / 2);

        int buttonSlots = 17;
        JPanel buttonPanel = new JPanel(new GridLayout(buttonSlots,1,0,2));
        buttonPanel.setPreferredSize(buttonPanelSize);
        panel.add(buttonPanel);

        //Charts & histograms
        _chartsButton = createButton("Charts &amp; Histograms", "/gradesystem/resources/icons/16x16/x-office-spreadsheet.png");
        _chartsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                chartsButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_chartsButton);

        //Email grade reports
        _emailReportsButton = createButton("Email Grade Reports", "/gradesystem/resources/icons/16x16/mail-message-new.png");
        _emailReportsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                emailReportsButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_emailReportsButton);

        buttonPanel.add(Box.createVerticalBox());//space

        //Extension Manager
        _extensionButton = createButton("Extension Manager", "/gradesystem/resources/icons/16x16/office-calendar.png");
        _extensionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                extensionButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_extensionButton);

        //Exemption Manager
        _exemptionButton = createButton("Exemption Manager", "/gradesystem/resources/icons/16x16/emblem-unreadable.png");
        _exemptionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                exemptionButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_exemptionButton);

        buttonPanel.add(Box.createVerticalBox());//space

        //Open Student Code
        _openCodeButton = createButton("Open Student Code", "/gradesystem/resources/icons/16x16/document-open.png");
        _openCodeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                openCodeButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_openCodeButton);

        //Run Student Code
        _runCodeButton = createButton("Run Student Code", "/gradesystem/resources/icons/16x16/go-next.png");
        _runCodeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                runCodeButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_runCodeButton);

        //Test Student Code
        _testCodeButton = createButton("Test Student Code", "/gradesystem/resources/icons/16x16/utilities-system-monitor.png");
        _testCodeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                testCodeButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_testCodeButton);

        //Print Student Code
        _printCodeButton = createButton("Print Student Code", "/gradesystem/resources/icons/16x16/printer.png");
        _printCodeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                printCodeButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_printCodeButton);

        buttonPanel.add(Box.createVerticalBox());//space

        //View student rubric
        _viewRubricButton = createButton("View Student Rubric", "/gradesystem/resources/icons/16x16/font-x-generic.png");
        _viewRubricButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewRubricButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_viewRubricButton);

        //Print student rubric
        _printRubricButton = createButton("Print Student Rubric", "/gradesystem/resources/icons/16x16/document-print.png");
        _printRubricButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                printRubricButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_printRubricButton);

        buttonPanel.add(Box.createVerticalBox());//space
        
        //Disable student
        _disableStudentButton = createButton("Disable Student", "/gradesystem/resources/icons/16x16/dialog-error.png");
        _disableStudentButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                disableStudentButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_disableStudentButton);
    }

    private static final Dimension SELECTED_ASSIGNMENT_LABEL_SIZE = new Dimension(SELECTED_ASSIGNMENT_PANEL_SIZE.width, 30);
    private static final Dimension SELECTED_ASSIGNMENT_BUTTON_PANEL_SIZE = new Dimension(SELECTED_ASSIGNMENT_PANEL_SIZE.width - 10,
                                                                                         SELECTED_ASSIGNMENT_PANEL_SIZE.height -
                                                                                         SELECTED_ASSIGNMENT_LABEL_SIZE.height);
    private void initSelectedAssignmentPanel(JPanel panel)
    {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //Label
        _selectedAssignmentLabel = new SelectedLabel("Selected Assignment", "assignments selected");
        _selectedAssignmentLabel.setPreferredSize(SELECTED_ASSIGNMENT_LABEL_SIZE);
        panel.add(_selectedAssignmentLabel);

        //Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2,3,5,5));
        buttonPanel.setPreferredSize(SELECTED_ASSIGNMENT_BUTTON_PANEL_SIZE);
        panel.add(buttonPanel);

        //Generate Distribution
        _createDistributionButton = createButton("Create Distribution", "/gradesystem/resources/icons/16x16/document-save-as.png");
        _createDistributionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                generateDistributionButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_createDistributionButton);

        //Reassign grading
        _reassignGradingButton = createButton("Reassign Grading", "/gradesystem/resources/icons/16x16/document-properties.png");
        _reassignGradingButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                reassignGradingButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_reassignGradingButton);

        //Import grades
        _importLabsButton = createButton("Import Lab Grades", "/gradesystem/resources/icons/16x16/mail-send-receive.png");
        _importLabsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                importGradesButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_importLabsButton);

        //Preview rubric
        _previewRubricButton = createButton("Preview Rubric", "/gradesystem/resources/icons/16x16/system-search.png");
        _previewRubricButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                previewRubricButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_previewRubricButton);

        //Deductions List
        _viewDeductionsButton = createButton("Deductions List", "/gradesystem/resources/icons/16x16/text-x-generic.png");
        _viewDeductionsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewDeductionsButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_viewDeductionsButton);

        //Run Demo
        _runDemoButton = createButton("Run Demo", "/gradesystem/resources/icons/16x16/applications-system.png");
        _runDemoButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                runDemoButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_runDemoButton);
    }

    private static final Dimension GENERAL_COMMANDS_LABEL_SIZE = new Dimension(GENERAL_COMMANDS_PANEL_SIZE.width, 30);
    private static final Dimension GENERAL_COMMANDS_BUTTON_PANEL_SIZE = new Dimension(GENERAL_COMMANDS_PANEL_SIZE.width - 10,
                                                                                      GENERAL_COMMANDS_PANEL_SIZE.height -
                                                                                      GENERAL_COMMANDS_LABEL_SIZE.height);

    private void initGeneralCommandPanel(JPanel panel)
    {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //Add label "General Commands"
        JLabel generalCommandsLabel = new JLabel("<html><b>&nbsp; General Commands</b></html>");
        generalCommandsLabel.setPreferredSize(GENERAL_COMMANDS_LABEL_SIZE);
        panel.add(generalCommandsLabel);

        //Buttons
        int buttonSlots = 17;
        JPanel buttonPanel = new JPanel(new GridLayout(buttonSlots,1,5,10));
        buttonPanel.setPreferredSize(GENERAL_COMMANDS_BUTTON_PANEL_SIZE);
        panel.add(buttonPanel);

        //Modify blacklist
        _modifyBlacklistButton = this.createButton("Modify Blacklist", "/gradesystem/resources/icons/16x16/format-text-strikethrough.png");
        _modifyBlacklistButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                modifyBlacklistButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_modifyBlacklistButton);

        //Edit configuration
        _editConfigurationButton = this.createButton("Edit Configuration", "/gradesystem/resources/icons/16x16/preferences-system.png");
        _editConfigurationButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                editConfigurationButtionActionPerformed();
            }
            
        });
        buttonPanel.add(_editConfigurationButton);

        //Export grades
        _exportGradesButton = this.createButton("Export Grades", "/gradesystem/resources/icons/16x16/edit-redo.png");
        _exportGradesButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                exportGradesButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_exportGradesButton);

        //Reset database
        _resetDatabaseButton = this.createButton("Reset Database", "/gradesystem/resources/icons/16x16/view-refresh.png");
        _resetDatabaseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                resetDatabaseButtonActionPerformed();
            }
            
        });
        buttonPanel.add(_resetDatabaseButton);

    }

    private void updateGUI()
    {
        Collection<Assignment> selectedAssignments = _assignmentList.getGenericSelectedValues();
        Collection<String> selectedStudents = _studentList.getGenericSelectedValues();

        //Update button states

        //                  STUDENT BUTTONS

        //Disable all, re-enable as appropriate
        for(JButton button : _studentButtons)
        {
            button.setEnabled(false);
        }

        //If one student selected
        if(selectedStudents.size() == 1)
        {
            //If zero or more assignments
            if(selectedAssignments.size() >= 0)
            {
                _disableStudentButton.setEnabled(true);
            }
            //If one or more assignments
            if(selectedAssignments.size() >= 1)
            {
                _chartsButton.setEnabled(true);
                _emailReportsButton.setEnabled(true);

                //TODO: Check if all students actually have a rubric
                boolean allHaveRubrics = true;
                for(Assignment asgn : selectedAssignments)
                {
                    if(asgn.hasHandinPart())
                    {
                        allHaveRubrics &= asgn.getHandinPart().hasRubric();
                    }
                    else
                    {
                        allHaveRubrics = false;
                    }
                }
                _printRubricButton.setEnabled(allHaveRubrics);
            }
            //If one assigment
            if(selectedAssignments.size() == 1)
            {
                _exemptionButton.setEnabled(true);

                //If it has a handin part
                if(_assignmentList.getSelectedValue().hasHandinPart())
                {
                    HandinPart part = _assignmentList.getSelectedValue().getHandinPart();

                    boolean hasHandin = part.hasHandin(_studentList.getSelectedValue());

                    _testCodeButton.setEnabled(hasHandin && part.hasTester());
                    _runCodeButton.setEnabled(hasHandin && part.hasRun() );
                    _openCodeButton.setEnabled(hasHandin && part.hasOpen());
                    _printCodeButton.setEnabled(hasHandin && part.hasPrint());

                    _extensionButton.setEnabled(true);

                    _viewRubricButton.setEnabled(part.hasRubric() &&
                            Allocator.getRubricManager().hasRubric(part, _studentList.getSelectedValue()));
                }
            }
        }
        //Multiple students selected
        else
        {            
            _chartsButton.setEnabled(true);
            _emailReportsButton.setEnabled(true);

            //TODO: Check if each student actually has a rubric
            boolean allHaveRubrics = true;
            for(Assignment asgn : selectedAssignments)
            {
                if(asgn.hasHandinPart())
                {
                    allHaveRubrics &= asgn.getHandinPart().hasRubric();
                }
                else
                {
                    allHaveRubrics = false;
                }
            }
            _printRubricButton.setEnabled(allHaveRubrics);

            boolean allHavePrintCode = true;
            for(Assignment asgn : selectedAssignments)
            {
                if(asgn.hasHandinPart())
                {
                    allHavePrintCode &= asgn.getHandinPart().hasPrint();
                }
                else
                {
                    allHavePrintCode = false;
                }
            }
            _printCodeButton.setEnabled(allHavePrintCode);
        }

         //                 ASSIGNMENT BUTTONS


        //Disable the assignment buttons, and then re-enable as appropriate
        for(JButton button : _assignmentButtons)
        {
            button.setEnabled(false);
        }
        //If one assignment is selected, enable assignment buttons as appropriate
        if(selectedAssignments.size() == 1)
        {
            _importLabsButton.setEnabled(_assignmentList.getSelectedValue().hasLabParts());

            if(_assignmentList.getSelectedValue().hasHandinPart())
            {
                HandinPart part = _assignmentList.getSelectedValue().getHandinPart();

                _createDistributionButton.setEnabled(true);
                _reassignGradingButton.setEnabled(true);

                _previewRubricButton.setEnabled(part.hasRubric());
                _viewDeductionsButton.setEnabled(part.hasDeductionList());
                _runDemoButton.setEnabled(part.hasDemo());
            }            
        }
        //If more than one assignment is selected, check if they have labs
        else
        {
            boolean allHaveLabs = true;
            for(Assignment asgn : selectedAssignments)
            {
                allHaveLabs &= asgn.hasLabParts();
            }

            _importLabsButton.setEnabled(allHaveLabs);
        }

        //Update which panel is showing

        //If no students or assignments selected, show welcome panel
        if(selectedAssignments.isEmpty() || selectedStudents.isEmpty())
        {
            _cardLayout.show(_cardPanel, WELCOME_PANEL_TAG);
        }
        //If one assignment and one student selected
        else if(selectedAssignments.size() == 1 && selectedStudents.size() == 1)
        {
            _singleSelectionPanel.updateView(_studentList.getSelectedValue(), _assignmentList.getSelectedValue());
            _cardLayout.show(_cardPanel, SINGLE_SELECT_PANEL_TAG);
        }
        //If multiple assignments and one or more students, OR
        //multiple students and one or more assignments
        else
        {
            _cardLayout.show(_cardPanel, MULTI_SELECT_PANEL_TAG);
        }

        //Update labels
        _selectedStudentLabel.setText(selectedStudents);
        _selectedAssignmentLabel.setText(selectedAssignments);
    }

    private JButton createButton(String text, String imagePath)
    {
        Icon icon = new ImageIcon(getClass().getResource(imagePath));
        JButton button = new JButton("<html><b>" + text + "</b></html>", icon);
        button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        button.setIconTextGap(10);

        return button;
    }



    //                          EVENTS BELOW                     //




    private void modifyBlacklistButtonActionPerformed()
    {
        new ModifyBlacklistView();
    }

    private void editConfigurationButtionActionPerformed()
    {
        JOptionPane.showMessageDialog(this, "This feature is not yet available");
    }

    private void exportGradesButtonActionPerformed()
    {
        Allocator.getCSVExporter().export();
    }

    private void resetDatabaseButtonActionPerformed()
    {
        JOptionPane.showMessageDialog(this, "This feature is not yet available");
    }

    public void generateDistributionButtonActionPerformed()
    {
        new AssignmentdistView(_assignmentList.getSelectedValue());
    }

    public void reassignGradingButtonActionPerformed()
    {
        new ReassignView(_assignmentList.getSelectedValue());
    }

    public void importGradesButtonActionPerformed()
    {
        JOptionPane.showMessageDialog(this, "This feature is not yet available");
    }

    public void previewRubricButtonActionPerformed()
    {
        if (_assignmentList.getSelectedValue().hasHandinPart() && _assignmentList.getSelectedValue().getHandinPart().hasRubric())
        {
            Allocator.getRubricManager().viewTemplate(_assignmentList.getSelectedValue().getHandinPart());
        }

    }

    public void viewDeductionsButtonActionPerformed()
    {
        if(_assignmentList.getSelectedValue().hasHandinPart() && _assignmentList.getSelectedValue().getHandinPart().hasDeductionList())
        {
            _assignmentList.getSelectedValue().getHandinPart().viewDeductionList();
        }
    }

    public void runDemoButtonActionPerformed()
    {
        if (_assignmentList.getSelectedValue().hasHandinPart() && _assignmentList.getSelectedValue().getHandinPart().hasDemo())
        {
            _assignmentList.getSelectedValue().getHandinPart().runDemo();
        }
    }

    private void chartsButtonActionPerformed()
    {
        new StatHistView(_assignmentList.getGenericSelectedValues(), _studentList.getGenericSelectedValues());
    }

    private void emailReportsButtonActionPerformed()
    {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new GridLayout(0,1));
        HashMap<Part,JCheckBox> boxMap = new HashMap<Part,JCheckBox>();
        for (Assignment a : _assignmentList.getGenericSelectedValues())
        {
            for (Part p : a.getParts())
            {
                JCheckBox partBox = new JCheckBox(a.getName() + ": " + p.getName());
                partBox.setSelected(true);
                boxMap.put(p, partBox);
                messagePanel.add(partBox);
            }
        }

        if (JOptionPane.showConfirmDialog(null, messagePanel,
                                            "Select Assignment Parts",
                                            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION )
        {
            Map<Assignment,Collection<Part>> map = new HashMap<Assignment,Collection<Part>>();
            for (Assignment a : _assignmentList.getGenericSelectedValues())
            {
                Vector<Part> parts = new Vector<Part>();
                for (Part p : a.getParts())
                {
                    if (boxMap.get(p).isSelected())
                    {
                        parts.add(p);
                    }
                }
                if (!parts.isEmpty())
                {
                    map.put(a, parts);
                }
            }

            GradeReportView grv = new GradeReportView(map, _studentList.getGenericSelectedValues());
            grv.setLocationRelativeTo(null);
            grv.setVisible(true);
        }
    }

    private void extensionButtonActionPerformed()
    {
        if(_studentList.getSelectedValue() != null && _assignmentList.getSelectedValue() != null &&
           _assignmentList.getSelectedValue().hasHandinPart())
        {
            new ExtensionView(_assignmentList.getSelectedValue().getHandinPart(), _studentList.getSelectedValue());
        }
    }

    private void exemptionButtonActionPerformed()
    {
        if(_studentList.getSelectedValue() != null && _assignmentList.getSelectedValue() != null &&
           _assignmentList.getSelectedValue().hasHandinPart())
        {
            new ExemptionView(_assignmentList.getGenericSelectedValues(), _studentList.getGenericSelectedValues());
        }
    }

    private void openCodeButtonActionPerformed()
    {
        if (_assignmentList.getSelectedValue().hasHandinPart() && _assignmentList.getSelectedValue().getHandinPart().hasOpen())
        {
            _assignmentList.getSelectedValue().getHandinPart().openCode(_studentList.getSelectedValue());
        }
    }

    private void runCodeButtonActionPerformed()
    {
        if (_assignmentList.getSelectedValue().hasHandinPart() && _assignmentList.getSelectedValue().getHandinPart().hasRun())
        {
            _assignmentList.getSelectedValue().getHandinPart().run(_studentList.getSelectedValue());
        }
    }

    private void testCodeButtonActionPerformed()
    {
        if (_assignmentList.getSelectedValue().hasHandinPart() &&
            _assignmentList.getSelectedValue().getHandinPart().hasTester())
        {
            _assignmentList.getSelectedValue().getHandinPart().runTester(_studentList.getSelectedValue());
        }
    }

    private void printCodeButtonActionPerformed()
    {
        if (_assignmentList.getSelectedValue().hasHandinPart() &&
            _assignmentList.getSelectedValue().getHandinPart().hasPrint())
        {
            _assignmentList.getSelectedValue().getHandinPart().printCode(_studentList.getSelectedValue(),
                                                                    Allocator.getGradingUtilities().getPrinter());
        }
    }

    private void viewRubricButtonActionPerformed()
    {
        if (_assignmentList.getSelectedValue().hasHandinPart() && _assignmentList.getSelectedValue().getHandinPart().hasRubric())
        {
            Allocator.getRubricManager().view(_assignmentList.getSelectedValue().getHandinPart(), _studentList.getSelectedValue(), true);
        }
    }

    private void printRubricButtonActionPerformed()
    {
        for (Assignment asgn : _assignmentList.getGenericSelectedValues())
        {
            Allocator.getRubricManager().convertToGRD(asgn.getHandinPart(), _studentList.getGenericSelectedValues());
            Allocator.getGradingUtilities().printGRDFiles(_studentList.getGenericSelectedValues(), asgn.getName());
        }
    }

    private void disableStudentButtonActionPerformed()
    {
        JOptionPane.showMessageDialog(this, "This feature is not yet available");
    }

    private void assignmentListValueChanged()
    {
        updateGUI();
    }

    private void studentListValueChanged()
    {
        updateGUI();
    }

}