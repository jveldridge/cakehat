package gradesystem.views.backend;

import gradesystem.views.backend.assignmentdist.AssignmentDistView;
import gradesystem.components.ModifyBlacklistView;
import gradesystem.views.backend.assignmentdist.ReassignView;
import gradesystem.views.backend.stathist.StatHistView;
import gradesystem.components.GenericJList;
import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import gradesystem.config.LabPart;
import gradesystem.config.Part;
import gradesystem.config.TA;
import gradesystem.GradeSystemApp;
import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.application.SingleFrameApplication;
import gradesystem.Allocator;
import gradesystem.services.UserServices.ValidityCheck;

/**
 *
 * @author jak2
 */
public class BackendView extends JFrame
{
    public static void main(String[] args)
    {
        new BackendView();
    }

    public static void launch()
    {
        if(Allocator.getUserServices().isUserAdmin())
        {
            new BackendView();
        }
        else
        {
            JOptionPane.showMessageDialog(null, "You [" +
                                         Allocator.getUserUtilities().getUserLogin() +
                                         "] are not an authorized user.");
            System.exit(0);
        }
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

    private JButton //Assignment wide buttons
                    _createDistributionButton, _reassignGradingButton, _importLabsButton,
                    _previewRubricButton, _viewDeductionsButton, _runDemoButton,
                    _manageGroupsButton,
                    //Student buttons
                    _chartsButton, _emailReportsButton, _extensionsButton,
                    _openCodeButton, _runCodeButton, _exemptionsButton,
                    _testCodeButton, _printCodeButton, _viewReadmeButton,
                    _viewRubricButton, _emailStudentRubric, _printRubricButton,
                    _disableStudentButton,
                    //General command buttons
                    _modifyBlacklistButton, _editConfigurationButton, _exportGradesButton,
                    _resetDatabaseButton;
    private JButton[] _assignmentButtons, _generalCommandsButtons, _studentButtons;
    private SelectedLabel _selectedAssignmentLabel, _selectedStudentLabel;
    private GenericJList<Assignment> _assignmentList;
    private GenericJList<String> _studentList;
    private JTextField _filterField;
    private JPanel _cardPanel = new JPanel();
    private List<String> _studentLogins;
    private final static String WELCOME_PANEL_TAG = "Welcome panel",
                                MULTI_SELECT_PANEL_TAG = "Multiple selected students panel",
                                SINGLE_SELECT_PANEL_TAG = "Single selected students panel";
    private CardLayout _cardLayout;
    private SingleSelectionPanel _singleSelectionPanel;

    private BackendView()
    {
        super("[cakehat] backend - " + Allocator.getUserUtilities().getUserLogin());

        //student logins
        _studentLogins = new LinkedList(Allocator.getDatabaseIO().getAllStudents().keySet());
        Collections.sort(_studentLogins);

        //make the user's temporary grading directory
        Allocator.getGradingServices().makeUserGradingDirectory();

        //on window close, remove user's temporary grading directory
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                Allocator.getGradingServices().removeUserGradingDirectory();
                if (!GradeSystemApp.inTestMode()) {
                    String bk_name = Allocator.getCourseInfo().getCourse() + "db_bk_" + Allocator.getCalendarUtilities().getCalendarAsString(Calendar.getInstance()).replaceAll("(\\s|:)", "_");
                    Allocator.getFileSystemUtilities().copyFile(Allocator.getCourseInfo().getDatabaseFilePath(), Allocator.getCourseInfo().getDatabaseBackupDir() + bk_name);
                }
            }
        });

        //init
        this.initFrameIcon();
        this.initComponents();
        this.initButtonGroups();
        this.initFocusTraversalPolicy();

        this.updateGUI();
    }

        /**
     * Initializes this frame's icon. Only visible on certain operating systems
     * and window managers.
     */
    private void initFrameIcon()
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

    private void initFocusTraversalPolicy()
    {
        //Add custom focus traversal policy
        this.setFocusTraversalPolicy(new FocusTraversalPolicy()
        {
            private JButton _submitButton = _singleSelectionPanel.getSubmitButton();
            private JFormattedTextField _nonHandinEarnedField = _singleSelectionPanel.getNonHandinEarnedField();
            private JFormattedTextField _labEarnedField = _singleSelectionPanel.getLabEarnedField();

            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt)
            {
                //Actions

                //If filter field, select first result and place result into field
                if(cmpnt == _filterField)
                {
                    if(_studentList.hasItems())
                    {
                        _studentList.selectFirst();
                        _filterField.setText(_studentList.getSelectedValue());
                    }
                }

                //If submit grade button, invoke it
                if(cmpnt == _submitButton)
                {
                    _submitButton.doClick();
                }

                //Next component

                if(cmpnt == _filterField && _nonHandinEarnedField.isEnabled())
                {
                    _nonHandinEarnedField.selectAll();
                    return _nonHandinEarnedField;
                }
                else if(cmpnt == _nonHandinEarnedField && _submitButton.isEnabled())
                {
                    return _submitButton;
                }
                else if(cmpnt == _labEarnedField && _submitButton.isEnabled())
                {
                    return _submitButton;
                }
                else if(cmpnt == _submitButton)
                {
                    _filterField.setText("");
                    return _filterField;
                }

                return _filterField;
            }

            @Override
            public Component getComponentBefore(Container cntnr, Component cmpnt)
            {
                return _filterField;
            }

            @Override
            public Component getFirstComponent(Container cntnr)
            {
                return _filterField;
            }

            @Override
            public Component getLastComponent(Container cntnr)
            {
                return _filterField;
            }

            @Override
            public Component getDefaultComponent(Container cntnr)
            {
                return _filterField;
            }
        });

        //Add enter as forward tab key
        Set<AWTKeyStroke> forwardKeys = this.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newForwardKeys = new HashSet(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);
    }

    private void initButtonGroups()
    {
        _assignmentButtons = new JButton[]
        {
          _createDistributionButton, _reassignGradingButton,
          _importLabsButton, _previewRubricButton, _viewDeductionsButton,
          _runDemoButton, _manageGroupsButton
        };

        _generalCommandsButtons = new JButton[]
        {
          _modifyBlacklistButton, _editConfigurationButton, _exportGradesButton,
          _resetDatabaseButton
        };

        _studentButtons = new JButton[]
        {
          _chartsButton, _emailReportsButton, _extensionsButton,
          _openCodeButton, _runCodeButton, _testCodeButton, _printCodeButton,
          _viewReadmeButton, _viewRubricButton, _emailStudentRubric,
          _printRubricButton, _disableStudentButton
        };
    }

    private static final Dimension
    MAIN_PANEL_SIZE = new Dimension(1150, 700),
    LIST_PANEL_SIZE = new Dimension(185, MAIN_PANEL_SIZE.height),
    GENERAL_COMMANDS_PANEL_SIZE = new Dimension(195, MAIN_PANEL_SIZE.height),
    MIDDLE_PANEL_SIZE = new Dimension(MAIN_PANEL_SIZE.width - 2 * LIST_PANEL_SIZE.width -
                                      GENERAL_COMMANDS_PANEL_SIZE.width, MAIN_PANEL_SIZE.height),
    SELECTED_ASSIGNMENT_PANEL_SIZE = new Dimension(MIDDLE_PANEL_SIZE.width, 130),
    STUDENT_BUTTON_PANEL_SIZE = new Dimension(200, MIDDLE_PANEL_SIZE.height -
                                              SELECTED_ASSIGNMENT_PANEL_SIZE.height),
    MULTI_PANEL_SIZE = new Dimension(MIDDLE_PANEL_SIZE.width - STUDENT_BUTTON_PANEL_SIZE.width,
                                     MIDDLE_PANEL_SIZE.height - SELECTED_ASSIGNMENT_PANEL_SIZE.height);
    private void initComponents()
    {
        //main panel - holds everything together
        JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
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
        JPanel middlePanel = new JPanel(new BorderLayout(0,0));
        middlePanel.setPreferredSize(MIDDLE_PANEL_SIZE);
        mainPanel.add(middlePanel);

        //selected assignment button panel
        JPanel selectedAssignmentPanel = new JPanel();
        selectedAssignmentPanel.setPreferredSize(SELECTED_ASSIGNMENT_PANEL_SIZE);
        this.initSelectedAssignmentPanel(selectedAssignmentPanel);
        middlePanel.add(selectedAssignmentPanel, BorderLayout.NORTH);

        //contains card layout for selected student / multiselect / intro card
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

        //display properties
        this.setResizable(false);
        this.pack();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private static Dimension MULTI_PANEL_LABEL_SIZE = new Dimension(MULTI_PANEL_SIZE.width - 10, 20);
    //intentionally package private so that SingleSelectionPanel can use this size
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
    private static final Dimension
    LIST_CONTROL_PANEL_SIZE = new Dimension(LIST_PANEL_SIZE.width - 10, 80),
    LIST_LABEL_SIZE = new Dimension(LIST_CONTROL_PANEL_SIZE.width, 20),
    LIST_GAP_SPACE_SIZE = new Dimension(LIST_CONTROL_PANEL_SIZE.width, 5),
    LIST_BUTTON_PANEL_SIZE = new Dimension(LIST_CONTROL_PANEL_SIZE.width, 25),
    LIST_SELECTOR_SIZE = new Dimension(LIST_CONTROL_PANEL_SIZE.width, 25),
    LIST_LIST_PANE_SIZE = new Dimension(LIST_PANEL_SIZE.width - 10,
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
                _studentList.setListData(_studentLogins);
                _filterField.setText("");
                _filterField.requestFocus();
            }
        });
        buttonPanel.add(selectNoneButton, BorderLayout.EAST);

        //Gap space
        controlPanel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //Student filter
        _filterField = new JTextField();
        _filterField.setPreferredSize(LIST_SELECTOR_SIZE);
        _filterField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent ke)
            {
                //term to filter against
                String filterTerm = _filterField.getText();

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
            }
        });
        controlPanel.add(_filterField, BorderLayout.SOUTH);

        //Gap space
        panel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //List
        _studentList = new GenericJList<String>(_studentLogins);
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
        _assignmentList = new GenericJList<Assignment>(Allocator.getCourseInfo().getAssignments());
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

    private static final int STUDENT_BUTTON_PANEL_BUFFER_HEIGHT = 90,
                             STUDENT_BUTTON_PANEL_BUFFER_WIDTH = 10,
                             STUDENT_BUTTON_PANEL_BUTTON_SLOTS = 18;
    private static final Dimension
    STUDENT_BUTTON_PANEL_GAP_SIZE = new Dimension(STUDENT_BUTTON_PANEL_SIZE.width,
                                                  STUDENT_BUTTON_PANEL_BUFFER_HEIGHT / 2),
    STUDENT_BUTTON_PANEL_BUTTON_PANEL_SIZE = new Dimension(STUDENT_BUTTON_PANEL_SIZE.width -
                                                           STUDENT_BUTTON_PANEL_BUFFER_WIDTH,
                                                           STUDENT_BUTTON_PANEL_SIZE.height -
                                                           STUDENT_BUTTON_PANEL_BUFFER_HEIGHT / 2);

    private void initStudentButtonPanel(JPanel panel)
    {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //Add some vertical space
        panel.add(Box.createRigidArea(STUDENT_BUTTON_PANEL_GAP_SIZE));

        //Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(STUDENT_BUTTON_PANEL_BUTTON_SLOTS,1,0,2));
        buttonPanel.setPreferredSize(STUDENT_BUTTON_PANEL_BUTTON_PANEL_SIZE);
        panel.add(buttonPanel);

        //Charts & histograms
        _chartsButton = createButton("View Charts", "/gradesystem/resources/icons/16x16/x-office-spreadsheet.png");
        _chartsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                chartsButtonActionPerformed();
            }

        });
        buttonPanel.add(_chartsButton);

        //Email grade reports
        _emailReportsButton = createButton("Email Reports", "/gradesystem/resources/icons/16x16/mail-message-new.png");
        _emailReportsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                emailReportsButtonActionPerformed();
            }

        });
        buttonPanel.add(_emailReportsButton);

        buttonPanel.add(Box.createVerticalBox());//space

        _exemptionsButton = createButton("Exemptions", "/gradesystem/resources/icons/16x16/dialog-error.png");
        _exemptionsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                exemptionsButtonActionPerformed();
            }

        });
        buttonPanel.add(_exemptionsButton);


        //Extensions & Exemptions

        _extensionsButton = createButton("Extensions", "/gradesystem/resources/icons/16x16/office-calendar.png");
        _extensionsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                extensionsButtonActionPerformed();
            }

        });
        buttonPanel.add(_extensionsButton);

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

        //View readme
        _viewReadmeButton =createButton("View Readme", "/gradesystem/resources/icons/16x16/text-x-generic.png");
        _viewReadmeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewReadmeButtonActionPerformed();
            }

        });
        buttonPanel.add(_viewReadmeButton);

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

        //Email student rubric
        _emailStudentRubric = createButton("Email Student Rubric", "/gradesystem/resources/icons/16x16/mail-forward.png");
        _emailStudentRubric.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                emailRubricButtonActionPerformed();
            }

        });
        buttonPanel.add(_emailStudentRubric);


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
        _disableStudentButton = createButton("Disable Student", "/gradesystem/resources/icons/16x16/list-remove.png");
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
        JPanel buttonPanel = new JPanel(new GridLayout(3,3,5,5));
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

        //Space
        buttonPanel.add(Box.createVerticalBox());//space

        //Manage groups
        _manageGroupsButton = createButton("Manage Groups", "/gradesystem/resources/icons/16x16/system-users.png");
        _manageGroupsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                manageGroupsButtonActionPerformed();
            }

        });
        buttonPanel.add(_manageGroupsButton);

        //Space
        buttonPanel.add(Box.createVerticalBox());//space
    }

    private static final Dimension
    GENERAL_COMMANDS_LABEL_SIZE = new Dimension(GENERAL_COMMANDS_PANEL_SIZE.width, 30),
    GENERAL_COMMANDS_BUTTON_PANEL_SIZE = new Dimension(GENERAL_COMMANDS_PANEL_SIZE.width - 10,
                                                       GENERAL_COMMANDS_PANEL_SIZE.height -
                                                       GENERAL_COMMANDS_LABEL_SIZE.height);
    private static final int GENERAL_COMMANDS_BUTTON_SLOTS = 17;

    private void initGeneralCommandPanel(JPanel panel)
    {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //Add label "General Commands"
        JLabel generalCommandsLabel = new JLabel("<html><b>&nbsp; General Commands</b></html>");
        generalCommandsLabel.setPreferredSize(GENERAL_COMMANDS_LABEL_SIZE);
        panel.add(generalCommandsLabel);

        //Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(GENERAL_COMMANDS_BUTTON_SLOTS,1,5,10));
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

    /**
     * Updates the buttons, the student assignment labels, and which panel is
     * shown in the center.
     */
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
                this.updateDisableEnableButton(Allocator.getDatabaseIO().isStudentEnabled(_studentList.getSelectedValue()));
                _disableStudentButton.setEnabled(true);
            }
            //If one or more assignments
            if(selectedAssignments.size() >= 1)
            {
                _chartsButton.setEnabled(true);
                _emailReportsButton.setEnabled(true);
                _extensionsButton.setEnabled(true);
            }
            //If one assigment
            if(selectedAssignments.size() == 1)
            {

                //If it has a handin part
                if(_assignmentList.getSelectedValue().hasHandinPart())
                {
                    HandinPart part = _assignmentList.getSelectedValue().getHandinPart();

                    String handinLogin = getHandinLogin(_studentList.getSelectedValue(), part);
                    boolean hasHandin = (handinLogin != null);

                    _testCodeButton.setEnabled(hasHandin && part.hasTester());
                    _runCodeButton.setEnabled(hasHandin && part.hasRun() );
                    _openCodeButton.setEnabled(hasHandin && part.hasOpen());
                    _viewReadmeButton.setEnabled(hasHandin && part.hasReadme(handinLogin));

                    boolean hasRubric = part.hasRubric() &&
                                        Allocator.getRubricManager().hasRubric(part, _studentList.getSelectedValue());
                    _viewRubricButton.setEnabled(hasRubric);
                    _emailStudentRubric.setEnabled(hasRubric);
                }
            }
        }
        //Multiple students selected
        else
        {
            if(selectedAssignments.size() >= 1)
            {
                _chartsButton.setEnabled(true);
                _emailReportsButton.setEnabled(true);
                _extensionsButton.setEnabled(true);
            }
        }

        if (selectedAssignments.size() == 1) {
            Assignment asgn = selectedAssignments.iterator().next();

            if (asgn.hasHandinPart()) {
                HandinPart handinPart = asgn.getHandinPart();

                boolean anyRubric = false;
                boolean anyCode = false;
                for (String student : selectedStudents) {
                    if (Allocator.getRubricManager().hasRubric(handinPart, student)) {
                        anyRubric = true;
                    }

                    Map<String, Collection<String>> groups = Allocator.getDatabaseIO().getGroups(handinPart);
                    for (String groupMember : groups.get(student)) {
                        if (handinPart.hasHandin(groupMember)) {
                            anyCode = true;
                        }
                    }

                    if (anyRubric && anyCode) {
                        break;
                    }
                }

                _printRubricButton.setEnabled(handinPart.hasRubric() && anyRubric);
                _emailStudentRubric.setEnabled(handinPart.hasRubric() && anyRubric);
                _printCodeButton.setEnabled(handinPart.hasPrint() && anyCode);
            }
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

                _manageGroupsButton.setEnabled(true);

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

    /**
     * Takes in a student login and a handin part and taking into account
     * groups returns the student login that the handin. If there is no
     * handin for any member of the group, null is returned;
     *
     * @param studentLogin
     * @param part
     * @return
     */
    private String getHandinLogin(String studentLogin, HandinPart part)
    {
        Collection<String> group = Allocator.getDatabaseIO().getGroup(part, studentLogin);

        for(String login : group)
        {
            if(part.hasHandin(login))
            {
                return login;
            }
        }

        return null;
    }

    /**
     * Creates a button with bold text and an image.
     *
     * @param text
     * @param imagePath
     * @return
     */
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
        new ModifyBlacklistView(Allocator.getCourseInfo().getTAs());
    }

    private void editConfigurationButtionActionPerformed()
    {
        new ConfigManager();
    }

    private void exportGradesButtonActionPerformed()
    {
        Allocator.getCSVExporter().export();
    }

    private void resetDatabaseButtonActionPerformed()
    {
        //check that user performing reset is an HTA
        if(!Allocator.getUserServices().isUserHTA()) {
            JOptionPane.showMessageDialog(this, "You are not authorized to reset the database; " +
                    "only HTAs may reset the database.", "Not Allowed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JCheckBox clearDatabaseRB = new JCheckBox("Clear Database");
        clearDatabaseRB.setSelected(true);

        JCheckBox addTAsRB = new JCheckBox("Add All TAs");
        addTAsRB.setSelected(true);

        JCheckBox addStudentsRB = new JCheckBox("Add All Students");
        addStudentsRB.setSelected(true);

        //often better not to add assignments until they're ready for grading,
        //as this makes making mid-semester config file changes easier
        JCheckBox addAssignmentsRB = new JCheckBox("Add All Assignments");
        addAssignmentsRB.setSelected(false);

        JPanel confirmDialogPanel = new JPanel();
        confirmDialogPanel.setLayout(new GridLayout(0, 1));

        confirmDialogPanel.add(new JLabel("Are you sure you want to reset the database?  " +
                "This will delete all data it stores."));
        confirmDialogPanel.add(clearDatabaseRB);
        confirmDialogPanel.add(addTAsRB);
        confirmDialogPanel.add(addStudentsRB);
        confirmDialogPanel.add(addAssignmentsRB);

        //get confirmation
        int response = JOptionPane.showConfirmDialog(this, confirmDialogPanel,
                "Confirm Database Reset", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.NO_OPTION) {
            return;
        }

        //clear database
        if (clearDatabaseRB.isSelected()) {
            Allocator.getDatabaseIO().clearDatabase();
        }

        //add all TAs
        if (addTAsRB.isSelected()) {
            for (TA ta : Allocator.getCourseInfo().getTAs()) {
                String login = ta.getLogin();
                String name = Allocator.getUserUtilities().getUserName(login);
                Allocator.getDatabaseIO().addTA(login, name);
            }
        }

        //add all assignments, and their parts
        if (addAssignmentsRB.isSelected()) {
            for (Assignment asgn : Allocator.getCourseInfo().getAssignments()) {
                Allocator.getDatabaseIO().addAssignment(asgn);
                for (Part part : asgn.getParts()) {
                    Allocator.getDatabaseIO().addAssignmentPart(part);
                }
            }
        }

        //add all students in group
        if (addStudentsRB.isSelected()) {
            for (String s : Allocator.getUserServices().getStudentLogins()) {
                String name = Allocator.getUserUtilities().getUserName(s);
                String names[] = name.split(" ");
                Allocator.getUserServices().addStudent(s, names[0],
                                                        names[names.length - 1],
                                                        ValidityCheck.BYPASS);
            }
        }

        JOptionPane.showMessageDialog(this, "Changes successful.  " +
                "Cakehat will now restart.", "Reset Successful", JOptionPane.INFORMATION_MESSAGE);
        this.dispose();
        SingleFrameApplication.launch(GradeSystemApp.class, new String[]{"backend"});
    }

    private void generateDistributionButtonActionPerformed()
    {
        new AssignmentDistView(_assignmentList.getSelectedValue());
    }

    private void reassignGradingButtonActionPerformed()
    {
        new ReassignView(_assignmentList.getSelectedValue());
    }

    private void importGradesButtonActionPerformed()
    {
        for(Assignment asgn : _assignmentList.getGenericSelectedValues())
        {
            for(LabPart part : asgn.getLabParts())
            {
                Allocator.getGradingServices().importLabGrades(part);
            }
        }
    }

    private void previewRubricButtonActionPerformed()
    {
        Allocator.getRubricManager().viewTemplate(_assignmentList.getSelectedValue().getHandinPart());
    }

    private void viewDeductionsButtonActionPerformed()
    {
        _assignmentList.getSelectedValue().getHandinPart().viewDeductionList();
    }

    private void runDemoButtonActionPerformed()
    {
        _assignmentList.getSelectedValue().getHandinPart().runDemo();
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

            Vector<String> students = new Vector<String>();
            for (String student : _studentList.getGenericSelectedValues()) {
                if (Allocator.getDatabaseIO().isStudentEnabled(student)) {
                    students.add(student);
                }
            }
            GradeReportView grv = new GradeReportView(map, students);
            grv.setLocationRelativeTo(null);
            grv.setVisible(true);
        }
    }

    private void extensionsButtonActionPerformed()
    {
        Assignment a = _assignmentList.getSelectedValue();
        if (a.hasHandinPart()) {
            new ExtensionView(a.getHandinPart(), _studentList.getSelectedValue());
        }

    }

    private void exemptionsButtonActionPerformed() {
        new ExemptionView(_assignmentList.getGenericSelectedValues(),
                          _studentList.getGenericSelectedValues());
    }

    private void openCodeButtonActionPerformed()
    {
        HandinPart part = _assignmentList.getSelectedValue().getHandinPart();
        String login = this.getHandinLogin(_studentList.getSelectedValue(), part);

        part.openCode(login);
    }

    private void runCodeButtonActionPerformed()
    {
        HandinPart part = _assignmentList.getSelectedValue().getHandinPart();
        String login = this.getHandinLogin(_studentList.getSelectedValue(), part);

        part.run(login);
    }

    private void testCodeButtonActionPerformed()
    {
        HandinPart part = _assignmentList.getSelectedValue().getHandinPart();
        String login = this.getHandinLogin(_studentList.getSelectedValue(), part);

        part.runTester(login);
    }

    private void printCodeButtonActionPerformed() {
        
        if (_assignmentList.getSelectedValue().hasHandinPart()) {

            String printer = Allocator.getGradingServices().getPrinter();

            //printer == null if "Cancel" button was clicked on printer select dialog
            if (printer == null) {
                return;
            }

            HandinPart handinPart = _assignmentList.getSelectedValue().getHandinPart();

            Collection<String> loginsToPrint = new LinkedList<String>();
            Collection<String> studentsWithoutCode = new LinkedList<String>();
            
            for (String student : _studentList.getGenericSelectedValues()) {
                String handinLogin = this.getHandinLogin(student, handinPart);

                if (handinLogin != null) {
                    loginsToPrint.add(handinLogin);
                }
                else {
                    studentsWithoutCode.add(student);
                }
            }

            if (studentsWithoutCode.size() > 0) {
                String message = "The following students do not have code\n"
                        + "and will not be printed:\n";
                for (String student : studentsWithoutCode) {
                    message += student + "\n";
                }

                Object[] options = {"Proceed", "Cancel"};
                int shouldContinue = JOptionPane.showOptionDialog(this, message,
                                                                  "Not all students have code!",
                                                                  JOptionPane.YES_NO_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE,
                                                                  null, options, options[0]);
                if (shouldContinue != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            handinPart.printCode(loginsToPrint, printer);
        }
    }

    private void viewReadmeButtonActionPerformed()
    {
        HandinPart part = _assignmentList.getSelectedValue().getHandinPart();
        String login = this.getHandinLogin(_studentList.getSelectedValue(), part);

        part.viewReadme(login);
    }

    private void viewRubricButtonActionPerformed()
    {
        if (_assignmentList.getSelectedValue().hasHandinPart() && _assignmentList.getSelectedValue().getHandinPart().hasRubric())
        {
            Allocator.getRubricManager().view(_assignmentList.getSelectedValue().getHandinPart(), _studentList.getSelectedValue(), true);
        }
    }

    private void emailRubricButtonActionPerformed() {
        Vector<String> students = new Vector<String>(_studentList.getGenericSelectedValues());
        if (_assignmentList.getSelectedValue().hasHandinPart()) {
            HandinPart handinPart = _assignmentList.getSelectedValue().getHandinPart();

            //remove any students who don't have rubrics
            Iterator<String> studentIterator = students.iterator();
            Collection<String> studentsWithoutRubrics = new LinkedList<String>();
            while (studentIterator.hasNext()) {
                String student = studentIterator.next();
                if (!Allocator.getRubricManager().hasRubric(handinPart, student)) {
                    studentIterator.remove();
                    studentsWithoutRubrics.add(student);
                }
            }

            if (studentsWithoutRubrics.size() > 0) {
                String message = "The following students do not have rubrics\n" +
                        "and will not be emailed:\n";
                for (String student : studentsWithoutRubrics) {
                    message += student + "\n";
                }

                int shouldContinue = JOptionPane.showConfirmDialog(this, message, "Not all students have rubrics!", JOptionPane.WARNING_MESSAGE);
                if (shouldContinue != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            Allocator.getRubricManager().convertToGRD(handinPart, students);
            Allocator.getGradingServices().notifyStudents(handinPart, students, true);
        }
    }

    private void printRubricButtonActionPerformed()
    {
        Vector<String> students = new Vector<String>(_studentList.getGenericSelectedValues());
        if (_assignmentList.getSelectedValue().hasHandinPart()) {
            HandinPart handinPart = _assignmentList.getSelectedValue().getHandinPart();

            //remove any students who don't have rubrics
            Iterator<String> studentIterator = students.iterator();
            Collection<String> studentsWithoutRubrics = new LinkedList<String>();
            while (studentIterator.hasNext()) {
                String student = studentIterator.next();
                if (!Allocator.getRubricManager().hasRubric(handinPart, student)) {
                    studentIterator.remove();
                    studentsWithoutRubrics.add(student);
                }
            }

            if (studentsWithoutRubrics.size() > 0) {
                String message = "The following students do not have rubrics\n" +
                        "and will not be printed:\n";
                for (String student : studentsWithoutRubrics) {
                    message += student + "\n";
                }

                Object[] options = {"Proceed", "Cancel"};
                int shouldContinue = JOptionPane.showOptionDialog(this, message,
                                                                  "Not all students have rubrics!",
                                                                  JOptionPane.YES_NO_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE,
                                                                  null, options, options[0]);
                if (shouldContinue != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            Allocator.getRubricManager().convertToGRD(handinPart, students);
            Allocator.getGradingServices().printGRDFiles(handinPart, students);
        }
    }

    private void disableStudentButtonActionPerformed()
    {
        String studentLogin = _studentList.getSelectedValue();

        if(Allocator.getDatabaseIO().isStudentEnabled(studentLogin))
        {
            Allocator.getDatabaseIO().disableStudent(studentLogin);
            this.updateDisableEnableButton(false);
        }
        else
        {
            Allocator.getDatabaseIO().enableStudent(studentLogin);
            this.updateDisableEnableButton(true);
        }
    }

    private void updateDisableEnableButton(boolean enabled)
    {
        if(enabled)
        {
            _disableStudentButton.setIcon(new ImageIcon(getClass().getResource("/gradesystem/resources/icons/16x16/list-remove.png")));
            _disableStudentButton.setText("Disable Student");
        }
        else
        {
            _disableStudentButton.setIcon(new ImageIcon(getClass().getResource("/gradesystem/resources/icons/16x16/list-add.png")));
            _disableStudentButton.setText("Enable Student");
        }
    }

    private void manageGroupsButtonActionPerformed()
    {
        new GroupsView(this._assignmentList.getSelectedValue().getHandinPart());
    }

    private void assignmentListValueChanged()
    {
        if (!_assignmentList.isSelectionEmpty()) {
            //Create directory for the assignment so GRD files can be created,
            //even if no assignments have been untarred
            Allocator.getFileSystemUtilities().makeDirectory(Allocator.getGradingServices().getUserGradingDirectory()
                                                            + _assignmentList.getSelectedValue().getName());
        }

        updateGUI();
    }

    private void studentListValueChanged()
    {
        updateGUI();
    }
}