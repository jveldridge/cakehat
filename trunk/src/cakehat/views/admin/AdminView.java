package cakehat.views.admin;

import cakehat.export.ExportException;
import cakehat.config.handin.ActionException;
import cakehat.rubric.RubricException;
import javax.swing.event.DocumentEvent;
import support.ui.GenericJList;
import cakehat.config.Assignment;
import cakehat.config.LabPart;
import cakehat.config.Part;
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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import cakehat.Allocator;
import cakehat.CakehatMain;
import cakehat.database.Group;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.MissingHandinException;
import cakehat.database.DataServices.ValidityCheck;
import cakehat.database.Student;
import cakehat.printing.CITPrinter;
import cakehat.resources.icons.IconLoader;
import cakehat.resources.icons.IconLoader.IconImage;
import cakehat.resources.icons.IconLoader.IconSize;
import cakehat.services.ServicesException;
import cakehat.views.admin.stathist.StatHistView;
import cakehat.views.shared.ErrorView;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import support.utils.FileCopyingException;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;
import support.utils.posix.NativeException;

/**
 *
 * @author jak2
 */
public class AdminView extends JFrame
{
    public static void launch()
    {
        if(Allocator.getUserServices().isUserAdmin())
        {
            boolean isSSH = false;
            try
            {
                isSSH = Allocator.getUserUtilities().isUserRemotelyConnected();
            }
            catch(NativeException e){}

            new AdminView(isSSH);
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

    private JButton //Assignment wide buttons
                    _manageGroupsButton, _autoDistributorButton, _manualDistributorButton,
                    _previewRubricButton, _viewGradingGuideButton, _runDemoButton,
                    //Student buttons
                    _chartsButton, _emailReportsButton, _extensionsButton,
                    _openCodeButton, _runCodeButton, _exemptionsButton,
                    _testCodeButton, _printCodeButton, _viewReadmeButton,
                    _viewRubricButton, _emailStudentRubric, _printRubricButton,
                    _disableStudentButton,
                    //General command buttons
                    _modifyBlacklistButton, _addStudentsButton, _exportGradesButton,
                    _resetDatabaseButton;
    private JButton[] _assignmentButtons, _generalCommandsButtons, _studentButtons;
    private SelectedLabel _selectedAssignmentLabel, _selectedStudentLabel;
    private JLabel _messageLabel;
    private AssignmentTree _assignmentTree;
    private GenericJList<Student> _studentList;
    private JTextField _filterField;
    private JPanel _cardPanel = new JPanel();
    private List<Student> _students;
    private final static String WELCOME_PANEL_TAG = "Welcome panel",
                                MULTI_SELECT_PANEL_TAG = "Multiple selected students panel",
                                SINGLE_PART_PANEL_TAG = "Single part selected panel",
                                SINGLE_SELECT_PANEL_TAG = "Single selected students panel";
    private CardLayout _cardLayout;
    private SinglePartPanel _singlePartPanel;
    private SingleSelectionPanel _singleSelectionPanel;

    private AdminView(boolean isSSH)
    {
        super("cakehat (admin)" + (isSSH ? " [ssh]" : ""));
        
        _students = new LinkedList<Student>(Allocator.getDataServices().getAllStudents());
        Collections.sort(_students);

        try {
            //make the user's temporary grading directory
            Allocator.getGradingServices().makeUserWorkspace();
        } catch (ServicesException e) {
            new ErrorView(e, "Could not make user grading directory; " +
                             "functionality will be significantly impaired.  " +
                             "You are advised to restart cakehat and to send an " +
                             "error report if the problem persists.");
        }

        //on window close, remove user's temporary grading directory
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                //If in not developing cakehat, backup the database on close
                if(!CakehatMain.isDeveloperMode())
                {
                    try {
                        Allocator.getGradingServices().makeDatabaseBackup();
                    } catch (ServicesException ex) {
                        new ErrorView(ex);
                    }
                }
            }
        });

        //init
        this.initFrameIcon();
        this.initComponents();
        this.initButtonGroups();
        this.initFocusTraversalPolicy();

        this.updateGUI(_assignmentTree.getSelection());
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

    private void initFocusTraversalPolicy()
    {
        //Add custom focus traversal policy
        this.setFocusTraversalPolicy(new FocusTraversalPolicy()
        {
            private JButton _submitButton = _singleSelectionPanel.getSubmitButton();
            private JFormattedTextField _nonHandinEarnedField = _singleSelectionPanel.getNonHandinEarnedField();

            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt)
            {
                //Actions

                //If filter field, select first result and place result into field
                if(cmpnt == _filterField)
                {
                    if(_studentList.hasListData())
                    {
                        _studentList.selectFirst();
                        _filterField.setText(_studentList.getSelectedValue().getLogin());
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
          _autoDistributorButton, _manualDistributorButton,
          _previewRubricButton, _viewGradingGuideButton,
          _runDemoButton, _manageGroupsButton
        };

        _generalCommandsButtons = new JButton[]
        {
          _modifyBlacklistButton, _addStudentsButton, _exportGradesButton,
          _resetDatabaseButton
        };

        _studentButtons = new JButton[]
        {
          _chartsButton, _emailReportsButton, _extensionsButton,
          _openCodeButton, _runCodeButton, _testCodeButton, _printCodeButton,
          _viewReadmeButton, _viewRubricButton, _emailStudentRubric,
          _printRubricButton, _disableStudentButton, _exemptionsButton
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
        //general message label
        _messageLabel = new JLabel();
        _messageLabel.setPreferredSize(MULTI_PANEL_LABEL_SIZE);
        panel.add(_messageLabel);

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

        //single part selected card
        _singlePartPanel = new SinglePartPanel();
        _cardPanel.add(_singlePartPanel, SINGLE_PART_PANEL_TAG);

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
                                              " from the lists to the left.</html>");
        panel.add(instructionsLabel);
    }

    //List panel sizes (for both student & assignment)
    public static final Dimension
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
                _studentList.setListData(_students);
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
        _filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilterField();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilterField();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilterField();
            }
        });
        controlPanel.add(_filterField, BorderLayout.SOUTH);

        //Gap space
        panel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //List
        _studentList = new GenericJList<Student>(_students);
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

    private void applyFilterField() {
        //term to filter against
        String filterTerm = _filterField.getText();

        List<Student> matchingStudents;
        //if no filter term, include all logins
        if (filterTerm.isEmpty()) {
            matchingStudents = _students;
        } //otherwise compared against beginning of each login
        else {
            matchingStudents = new ArrayList<Student>();
            for (Student student : _students) {
                if (student.getLogin().startsWith(filterTerm)) {
                    matchingStudents.add(student);
                }
            }
        }

        //display matching logins
        _studentList.setListData(matchingStudents);
        _studentList.selectFirst();
    }

    private void initAssignmentListPanel(JPanel panel)
    {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //Top panel: Assignments label, select all / select none buttons, drop down list
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        controlPanel.setPreferredSize(LIST_CONTROL_PANEL_SIZE);

        //Label
        JLabel assignmentLabel = new JLabel("<html><b>Assignments</b></html>");
        assignmentLabel.setPreferredSize(LIST_LABEL_SIZE);
        controlPanel.add(assignmentLabel);

        panel.add(controlPanel);

        //Gap space
        controlPanel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));


        //Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2,1,5,5));
        Dimension buttonSize = new Dimension((int) LIST_BUTTON_PANEL_SIZE.getWidth(), (int) LIST_BUTTON_PANEL_SIZE.getHeight()*2);
        buttonPanel.setPreferredSize(buttonSize);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        //Expand All
        JButton expandAllButton = new JButton("Expand All");
        expandAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                _assignmentTree.expandAll();
            }
        });
        buttonPanel.add(expandAllButton);
        expandAllButton.setVisible(true);

        //Collapse All
        JButton collapseAllButton = new JButton("Collapse All");
        collapseAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                _assignmentTree.collapseAll();
            }
        });
        buttonPanel.add(collapseAllButton);
        collapseAllButton.setVisible(true);


        //Gap space
        controlPanel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //Gap space
        panel.add(Box.createRigidArea(LIST_GAP_SPACE_SIZE));

        //Assignment tree
        _assignmentTree = new AssignmentTree();
        _assignmentTree.addSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                assignmentTreeValueChanged(_assignmentTree.getSelection());
              }
          });

        _assignmentTree.setPreferredSize(LIST_LIST_PANE_SIZE);
        panel.add(_assignmentTree);
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
        _chartsButton = createButton("View Charts", IconImage.X_OFFICE_SPREADSHEET);
        _chartsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                chartsButtonActionPerformed();
            }

        });
        buttonPanel.add(_chartsButton);

        //Email grade reports
        _emailReportsButton = createButton("Email Reports", IconImage.MAIL_MESSAGE_NEW);
        _emailReportsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                emailReportsButtonActionPerformed();
            }

        });
        buttonPanel.add(_emailReportsButton);

        buttonPanel.add(Box.createVerticalBox());//space

        _exemptionsButton = createButton("Exemptions", IconImage.DIALOG_ERROR);
        _exemptionsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                exemptionsButtonActionPerformed();
            }

        });
        buttonPanel.add(_exemptionsButton);


        //Extensions & Exemptions

        _extensionsButton = createButton("Extensions", IconImage.OFFICE_CALENDAR);
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
        _openCodeButton = createButton("Open Student Code", IconImage.DOCUMENT_OPEN);
        _openCodeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                openCodeButtonActionPerformed();
            }

        });
        buttonPanel.add(_openCodeButton);

        //Run Student Code
        _runCodeButton = createButton("Run Student Code", IconImage.GO_NEXT);
        _runCodeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                runCodeButtonActionPerformed();
            }

        });
        buttonPanel.add(_runCodeButton);

        //Test Student Code
        _testCodeButton = createButton("Test Student Code", IconImage.UTILITIES_SYSTEM_MONITOR);
        _testCodeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                testCodeButtonActionPerformed();
            }

        });
        buttonPanel.add(_testCodeButton);

        //Print Student Code
        _printCodeButton = createButton("Print Student Code", IconImage.PRINTER);
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
        _viewReadmeButton =createButton("View Readme", IconImage.TEXT_X_GENERIC);
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
        _viewRubricButton = createButton("View Student Rubric", IconImage.FONT_X_GENERIC);
        _viewRubricButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewRubricButtonActionPerformed();
            }

        });
        buttonPanel.add(_viewRubricButton);

        //Email student rubric
        _emailStudentRubric = createButton("Email Student Rubric", IconImage.MAIL_FORWARD);
        _emailStudentRubric.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                emailRubricButtonActionPerformed();
            }

        });
        buttonPanel.add(_emailStudentRubric);


        //Print student rubric
        _printRubricButton = createButton("Print Student Rubric", IconImage.DOCUMENT_PRINT);
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
        _disableStudentButton = createButton("Disable Student", IconImage.LIST_REMOVE);
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

        //Manage groups
        _manageGroupsButton = createButton("Manage Groups", IconImage.SYSTEM_USERS);
        _manageGroupsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                manageGroupsButtonActionPerformed();
            }

        });
        buttonPanel.add(_manageGroupsButton);

        //Automatic Distributor
        _autoDistributorButton = createButton("Auto Distributor", IconImage.DOCUMENT_SAVE_AS);
        _autoDistributorButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                autoDistributorButtonActionPerformed();
            }

        });
        buttonPanel.add(_autoDistributorButton);

        //Manual Distributor
        _manualDistributorButton = createButton("Manual Distributor", IconImage.DOCUMENT_PROPERTIES);
        _manualDistributorButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                manualDistributorButtonActionPerformed();
            }

        });
        buttonPanel.add(_manualDistributorButton);

        //Preview rubric
        _previewRubricButton = createButton("Preview Rubric", IconImage.SYSTEM_SEARCH);
        _previewRubricButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                previewRubricButtonActionPerformed();
            }

        });
        buttonPanel.add(_previewRubricButton);

        //Deductions List
        _viewGradingGuideButton = createButton("Grading Guide", IconImage.TEXT_X_GENERIC);
        _viewGradingGuideButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewGradingGuideButtonActionPerformed();
            }

        });
        buttonPanel.add(_viewGradingGuideButton);

        //Run Demo
        _runDemoButton = createButton("Run Demo", IconImage.APPLICATIONS_SYSTEM);
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

        //Edit configuration
        _addStudentsButton = this.createButton("Add Students", IconImage.PREFERENCES_SYSTEM);
        _addStudentsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                addStudentsButtonActionPerformed();
            }

        });
        buttonPanel.add(_addStudentsButton);

        //Export grades
        _exportGradesButton = this.createButton("Export Grades", IconImage.EDIT_REDO);
        _exportGradesButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                exportGradesButtonActionPerformed();
            }

        });
        buttonPanel.add(_exportGradesButton);

        //Reset database
        _resetDatabaseButton = this.createButton("Reset Database", IconImage.VIEW_REFRESH);
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
    private void updateGUI(Map<Assignment, List<Part>> selection)
    {
        Collection<Student> selectedStudents = new ArrayList<Student>(_studentList.getGenericSelectedValues());
        
        //map a Student to Groups for that student for the selected assignments
        Map<Student, Map<Assignment, Group>> studentToGroups = new HashMap<Student, Map<Assignment, Group>>();
        StringBuilder errMsgBuilder = new StringBuilder("The following students do not have groups for the assignments listed.\n\n");
        
        boolean someGroupMissing = false;
        
        for (Student student : selectedStudents) {
            studentToGroups.put(student, new HashMap<Assignment, Group>());
            List<Assignment> missing = new ArrayList<Assignment>(selection.size());
            
            for (Assignment asgn : selection.keySet()) {
                try {
                    studentToGroups.get(student).put(asgn, Allocator.getDataServices().getGroup(asgn, student));
                } catch (ServicesException ex) {
                   missing.add(asgn);
                }
            }

            if (!missing.isEmpty()) {
                someGroupMissing = true;
                
                String missingForStudent = student + ": ";
                for (int i = 0; i < missing.size(); i++) {
                    missingForStudent += missing.get(i);
                    if (i < missing.size() - 1) {
                        missingForStudent += ", ";
                    }
                }
                errMsgBuilder.append(missingForStudent + "\n");
            }
        }

        if (someGroupMissing) {
            errMsgBuilder.append("\nThey will not be included in any actions performed on assignments for which they do not have groups.");
            JOptionPane.showMessageDialog(this, errMsgBuilder.toString(), "Missing Groups", JOptionPane.WARNING_MESSAGE);
        }

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
            Student student = _studentList.getSelectedValue();
            this.updateDisableEnableButton(student.isEnabled());
            _disableStudentButton.setEnabled(true);

            //If one or more assignments
            if(selection.size() >= 1)
            {
                _emailReportsButton.setEnabled(true);
            }
            
            //If one assigment
            if(selection.size() == 1)
            {
                Assignment selectedAsgn = this.getSingleSelectedAssignment(selection);
                Group group = studentToGroups.get(student).get(selectedAsgn);
                
                if (group != null) {
                    //if no part is selected
                    if (selection.get(selectedAsgn).isEmpty()) {
                        _exemptionsButton.setEnabled(true);
                        
                        if (selectedAsgn.hasHandin()) {
                            _extensionsButton.setEnabled(true);
                        }
                    }

                    //if a single DistributablePart is selected
                    DistributablePart selectedDP = this.getSingleSelectedDP(selection);
                    if (selectedDP != null) {
                        boolean hasHandin = false;
                        try {
                            hasHandin = selectedAsgn.getHandin().hasHandin(group);
                        } catch (IOException e) {
                            new ErrorView(e);
                        }

                        _testCodeButton.setEnabled(hasHandin && selectedDP.hasTest());
                        _runCodeButton.setEnabled(hasHandin && selectedDP.hasRun());
                        _openCodeButton.setEnabled(hasHandin && selectedDP.hasOpen());
                        try {
                            _viewReadmeButton.setEnabled(hasHandin && selectedDP.hasReadme(group));
                        } catch (ActionException ex) {
                            new ErrorView(ex, "Could not determine whether group "
                                    + group + " has a README for assignment "
                                    + selectedAsgn + ".");
                        } catch (MissingHandinException ex) {
                            this.notifyHandinMissing(ex);
                        }

                        boolean hasRubric = selectedDP.hasRubricTemplate()
                                && Allocator.getRubricManager().hasRubric(selectedDP, group);
                        _viewRubricButton.setEnabled(hasRubric);
                        _emailStudentRubric.setEnabled(hasRubric);
                    }
                }
            }
        }

        //Multiple students selected
        else if (!selectedStudents.isEmpty())
        {
            if(selection.size() >= 1)
            {
                _emailReportsButton.setEnabled(true);
            }
        }

        //no students selected
        else {
            _chartsButton.setEnabled(true);
        }

        //if one assignment selected
        if (selection.size() == 1) {
            Assignment selectedAsgn = selection.keySet().iterator().next();

            if (selectedStudents.isEmpty() && selection.get(selectedAsgn).isEmpty()) {
                _exemptionsButton.setEnabled(true);
            }

            //determine which DistributableParts are selected
            List<DistributablePart> selectedDPs = new LinkedList<DistributablePart>();
            for (Part p : selection.get(selectedAsgn)) {
                if (p instanceof DistributablePart) {
                    selectedDPs.add((DistributablePart) p);
                }
            }


            if (!selectedDPs.isEmpty()) {
                boolean anyDPRubric = false;
                boolean anyDPPrint = false;

                boolean anyGroupRubric = false;
                boolean anyGroupCode = false;

                //determine which selected students have groups for the selected Assignment
                Collection<Group> selectedGroups = new ArrayList<Group>(selectedStudents.size());
                for (Student student : studentToGroups.keySet()) {
                    if (studentToGroups.get(student).containsKey(selectedAsgn)) {
                        selectedGroups.add(studentToGroups.get(student).get(selectedAsgn));
                    }
                }

                for (DistributablePart dp : selectedDPs) {
                    if (dp.hasRubricTemplate()) {
                        anyDPRubric = true;
                    }

                    if (dp.hasPrint()) {
                        anyDPPrint = true;
                    }

                    for (Group group : selectedGroups) {
                        if (Allocator.getRubricManager().hasRubric(dp, group)) {
                            anyGroupRubric = true;
                        }

                        try {
                            if (selectedAsgn.getHandin().hasHandin(group)) {
                                anyGroupCode = true;
                            }
                        } catch(IOException e) {
                            new ErrorView(e);
                        }

                        if (anyGroupRubric && anyGroupCode) {
                            break;
                        }
                    }
                }

                _printRubricButton.setEnabled(anyDPRubric && anyGroupRubric);
                _emailStudentRubric.setEnabled(anyDPRubric && anyGroupRubric);
                _printCodeButton.setEnabled(anyDPPrint && anyGroupCode);
            }

        }
        

        //                 ASSIGNMENT BUTTONS


        //Disable the assignment buttons, and then re-enable as appropriate
        
        for(JButton button : _assignmentButtons)
        {
            button.setEnabled(false);
        }
        
        //If one assignment is selected, enable assignment buttons as appropriate
        if(selection.size() == 1)
        {
            Assignment selectedAsgn = this.getSingleSelectedAssignment(selection);

            //to avoid user confusion, only allow operations that function on the
            //whole assignment (i.e., not on individual parts) if just the assignment
            //and none of its parts are selected
            if (selection.get(selectedAsgn).isEmpty()) {
                if (selectedAsgn.hasDistributableParts()) {
                    _autoDistributorButton.setEnabled(true);
                }

                if (selectedAsgn.hasGroups()) {
                    _manageGroupsButton.setEnabled(true);
                }
            }

            DistributablePart selectedDP = this.getSingleSelectedDP(selection);

            //enable manual distribution button if either:
            //   1. no parts are selected and the assignment has DistributableParts
            //   2. exactly 1 DistributablePart is selected
            if ((selectedAsgn.hasDistributableParts() && selection.get(selectedAsgn).isEmpty())
                    || (selectedDP != null)) {
                _manualDistributorButton.setEnabled(true);
            }

            if (selectedDP != null) {
                _previewRubricButton.setEnabled(selectedDP.hasRubricTemplate());
                _viewGradingGuideButton.setEnabled(selectedDP.hasGradingGuide());
                _runDemoButton.setEnabled(selectedDP.hasDemo());
            }
        }

        //Update which panel is showing
        boolean panelUpdated = false;

        //If one assignment and one student selected
        if (selection.size() == 1 && selectedStudents.size() == 1) {
            Assignment asgn = this.getSingleSelectedAssignment(selection);
            Student student = _studentList.getSelectedValue();
            
            Group group = studentToGroups.get(student).get(asgn);
            if (group != null) {
                _singleSelectionPanel.updateView(student, group, asgn);

                if (selection.get(asgn).size() == 1 && getSingleSelectedDP(selection) == null) {
                    _singleSelectionPanel.selectPart(selection.get(asgn).get(0));
                }

                _cardLayout.show(_cardPanel, SINGLE_SELECT_PANEL_TAG);
                panelUpdated = true;
            }
            else {
                //if student doesn't have a group, we can't update the SSP, but we also
                //shouldn't show the SSP for the previously selected student, so let's clear the
                //student selection and update the panel using one of the options below
                _studentList.clearSelection();
                selectedStudents = Collections.emptyList();
            }
        }

         //If no students or assignments selected, show welcome panel
        if(selection.isEmpty() && selectedStudents.isEmpty())
        {
            _cardLayout.show(_cardPanel, WELCOME_PANEL_TAG);
            panelUpdated = true;
        }
        //if one part and no students selected
        if (getSingleSelectedPart(selection) != null && selectedStudents.isEmpty()) {
            _singlePartPanel.updatePart(getSingleSelectedPart(selection));
            _cardLayout.show(_cardPanel, SINGLE_PART_PANEL_TAG);
            panelUpdated = true;
        }
        //If multiple assignments and one or more students, OR
        //multiple students and one or more assignments
        else if (!panelUpdated)
        {
            _cardLayout.show(_cardPanel, MULTI_SELECT_PANEL_TAG);
        }

        //Update labels
        _selectedStudentLabel.setText(selectedStudents);
        _selectedAssignmentLabel.setText(selection.keySet());
    }

    /**
     * Creates a button with bold text and an image.
     *
     * @param text
     * @param image
     * @return
     */
    private JButton createButton(String text, IconImage image)
    {
        Icon icon = IconLoader.loadIcon(IconSize.s16x16, image);
        JButton button = new JButton("<html><b>" + text + "</b></html>", icon);
        button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        button.setIconTextGap(10);

        return button;
    }



    //                          EVENTS BELOW                     //


    private void addStudentsButtonActionPerformed()
    {
        AddStudentsView view = new AddStudentsView();
        view.setLocationRelativeTo(this);
        view.setVisible(true);
    }

    private void exportGradesButtonActionPerformed()
    {
        try {
            Allocator.getCSVExporter().export();
        } catch (ExportException ex) {
            new ErrorView(ex, "Export failed.");
        }
    }

    private void resetDatabaseButtonActionPerformed()
    {
        //check that user performing reset is an administrator
        if(!Allocator.getUserServices().getUser().isAdmin()) {
            JOptionPane.showMessageDialog(this, "You are not authorized to reset the database; " +
                    "only administrators may reset the database.", "Not Allowed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JCheckBox clearDatabaseRB = new JCheckBox("Clear Database");
        clearDatabaseRB.setSelected(true);

        JCheckBox addStudentsRB = new JCheckBox("Add All Students");
        addStudentsRB.setSelected(true);

        JPanel confirmDialogPanel = new JPanel();
        confirmDialogPanel.setLayout(new GridLayout(0, 1));

        confirmDialogPanel.add(new JLabel("Are you sure you want to reset the database?  " +
                "This will delete all data it stores."));
        confirmDialogPanel.add(clearDatabaseRB);
        confirmDialogPanel.add(addStudentsRB);

        //get confirmation
        int response = JOptionPane.showConfirmDialog(this, confirmDialogPanel,
                "Confirm Database Reset", JOptionPane.YES_NO_OPTION);
        if (response != JOptionPane.YES_OPTION) {
            return;
        }

        //clear database
        if (clearDatabaseRB.isSelected()) {
            try {
                Allocator.getDatabase().resetDatabase();
            } catch (SQLException ex) {
                new ErrorView(ex, "Resetting the database failed.");
                return;
            }
        }

        //add all students in group
        if (addStudentsRB.isSelected()) {
            try {
                Collection<String> studentsNotAdded = new LinkedList<String>();
                for (String login : Allocator.getUserServices().getStudentLogins()) {
                    try {
                        Allocator.getDataServices().addStudent(login, ValidityCheck.BYPASS);
                    } catch (ServicesException ex) {
                       studentsNotAdded.add(login);
                    }
                }

                if (!studentsNotAdded.isEmpty()) {
                    new ErrorView("The following students could not be added to " +
                                  "the database: " + studentsNotAdded + ".");
                }
            } catch(NativeException e) {
                new ErrorView(e, "Unable to add any students in the group " +
                        "because members of the student group could not be retrieved");
            }
        }

        JOptionPane.showMessageDialog(this, "Changes successful.  " +
                "Cakehat will now restart.", "Reset Successful", JOptionPane.INFORMATION_MESSAGE);
        this.dispose();
        AdminView.launch();
    }

    private void autoDistributorButtonActionPerformed()
    {
        AutomaticDistributorView view = new AutomaticDistributorView(this.getSingleSelectedAssignment(_assignmentTree.getSelection()));
        view.setLocationRelativeTo(this);
        view.setVisible(true);
    }

    private void manualDistributorButtonActionPerformed()
    {
        Map<Assignment, List<Part>> selection = _assignmentTree.getSelection();
        ManualDistributorView view = new ManualDistributorView(this.getSingleSelectedAssignment(selection), this.getSingleSelectedDP(selection));
        view.setLocationRelativeTo(this);
        view.setVisible(true);
    }

    private void previewRubricButtonActionPerformed()
    {
        try {
            Allocator.getRubricManager().viewTemplate(this.getSingleSelectedDP(_assignmentTree.getSelection()));
        } catch (RubricException ex) {
            new ErrorView(ex, "Could not show rubric preview.");
        }
    }

    private void viewGradingGuideButtonActionPerformed()
    {
        try {
            this.getSingleSelectedDP(_assignmentTree.getSelection()).viewGradingGuide();
        } catch (FileNotFoundException ex) {
            new ErrorView(ex);
        }
    }

    private void runDemoButtonActionPerformed()
    {
        DistributablePart dp = this.getSingleSelectedDP(_assignmentTree.getSelection());
        try {
            dp.demo();
        } catch (ActionException ex) {
            new ErrorView(ex);
        }
    }

    private void chartsButtonActionPerformed()
    {
        StatHistView view = new StatHistView(_assignmentTree.getSelection().keySet());
        view.setLocationRelativeTo(this);
        view.setVisible(true);
    }

    private void emailReportsButtonActionPerformed()
    {
        Collection<Student> enabledStudents = Allocator.getDataServices().getEnabledStudents();

        Collection<Student> selectedStudents = new ArrayList<Student>(_studentList.getGenericSelectedValues());
        Collection<Student> selectedButDisabled = new ArrayList<Student>();
        for (Student student : selectedStudents) {
            if (!enabledStudents.contains(student)) {
                selectedButDisabled.add(student);
            }
        }

        if (!selectedButDisabled.isEmpty()) {
            int proceed = JOptionPane.showConfirmDialog(this, "The following students were selected " +
                                                              "but are disabled: \n" + selectedButDisabled + "\n" +
                                                              "They will not be emailed reports.",
                                                        "Disabled Students Selected",
                                                        JOptionPane.OK_CANCEL_OPTION);
            if (proceed != JOptionPane.OK_OPTION) {
                return;
            }
            selectedStudents.removeAll(selectedButDisabled);
        }

        if (selectedStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No enabled students selected.");
        }
        else {
            new GradeReportView(_assignmentTree.getSelection(), selectedStudents);
        }
    }

    private void extensionsButtonActionPerformed() {
        Student student = _studentList.getSelectedValue();

        Map<Assignment, List<Part>> selection = _assignmentTree.getSelection();
        Assignment asgn = this.getSingleSelectedAssignment(selection);

        Group group;
        try {
            group = Allocator.getDataServices().getGroup(asgn, student);
        } catch (ServicesException ex) {
            new ErrorView(ex);
            return;
        }

        ExtensionView view = new ExtensionView(asgn, group);
        view.setLocationRelativeTo(this);
        view.setVisible(true);
    }

    private void exemptionsButtonActionPerformed() {
        Map<Assignment, List<Part>> selection = _assignmentTree.getSelection();
        Assignment asgn = this.getSingleSelectedAssignment(selection);
        Student selectedStudent = _studentList.getSelectedValue();
        Group group = null;
        if (selectedStudent != null) {
            try {
                group = Allocator.getDataServices().getGroup(asgn, selectedStudent);
            } catch (ServicesException ex) {
                new ErrorView(ex);
                return;
            }
        }

        new ExemptionView(asgn, group);
    }

    private void openCodeButtonActionPerformed() {
        Student student = _studentList.getSelectedValue();

        Map<Assignment, List<Part>> selection = _assignmentTree.getSelection();
        Assignment asgn = this.getSingleSelectedAssignment(selection);

        Group group;
        try {
            group = Allocator.getDataServices().getGroup(asgn, student);
        } catch (ServicesException ex) {
            new ErrorView(ex);
            return;
        }

        DistributablePart dp = this.getSingleSelectedDP(selection);
        try {
            dp.open(group);
        } catch (ActionException ex) {
            new ErrorView(ex);
        } catch (MissingHandinException ex) {
            this.notifyHandinMissing(ex);
        }
    }

    private void runCodeButtonActionPerformed() {
        Student student = _studentList.getSelectedValue();

        Map<Assignment, List<Part>> selection = _assignmentTree.getSelection();
        Assignment asgn = this.getSingleSelectedAssignment(selection);

        Group group;
        try {
            group = Allocator.getDataServices().getGroup(asgn, student);
        } catch (ServicesException ex) {
            new ErrorView(ex);
            return;
        }

        DistributablePart dp = this.getSingleSelectedDP(selection);
        try {
            dp.run(group);
        } catch (ActionException ex) {
            new ErrorView(ex);
        } catch (MissingHandinException ex) {
            this.notifyHandinMissing(ex);
        }
    }

    private void testCodeButtonActionPerformed() {
        Student student = _studentList.getSelectedValue();

        Map<Assignment, List<Part>> selection = _assignmentTree.getSelection();
        Assignment asgn = this.getSingleSelectedAssignment(selection);

        Group group;
        try {
            group = Allocator.getDataServices().getGroup(asgn, student);
        } catch (ServicesException ex) {
            new ErrorView(ex);
            return;
        }

        DistributablePart dp = this.getSingleSelectedDP(selection);
        try {
            dp.test(group);
        } catch (ActionException ex) {
            new ErrorView(ex);
        } catch (MissingHandinException ex) {
            this.notifyHandinMissing(ex);
        }
    }

    private void printCodeButtonActionPerformed() {

        Map<Assignment, List<Part>> selection = _assignmentTree.getSelection();
        Assignment asgn = this.getSingleSelectedAssignment(selection);
        DistributablePart dp = this.getSingleSelectedDP(selection);

        Collection<Group> groupsToPrint = new LinkedList<Group>();
        Collection<Group> groupsWithoutCode = new LinkedList<Group>();

        try {
            for (Student student : _studentList.getGenericSelectedValues()) {
                Group group = Allocator.getDataServices().getGroup(asgn, student);
                
                File handin = null;
                try {
                    handin = asgn.getHandin().getHandin(group);
                } catch (IOException e) {
                    new ErrorView(e);
                }

                if (handin != null) {
                    groupsToPrint.add(group);
                } else {
                    groupsWithoutCode.add(group);
                }
            }
        } catch (ServicesException ex) {
            new ErrorView(ex);
            return;
        }

        if (groupsWithoutCode.size() > 0) {
            String message = "The following groups do not have handins; \n" +
                             "thus, their handins cannot be printed:\n";
            for (Group group : groupsWithoutCode) {
                message += group.getName() + "(" + group.getMembers() + ")\n";
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
        try {
            dp.print(groupsToPrint);
        } catch (ActionException ex) {
            new ErrorView(ex);
        }
    }

    private void viewReadmeButtonActionPerformed() {
        Student student = _studentList.getSelectedValue();

        DistributablePart dp = this.getSingleSelectedDP(_assignmentTree.getSelection());
        Group group;
        try {
            group = Allocator.getDataServices().getGroup(dp.getAssignment(), student);
        } catch (ServicesException ex) {
            new ErrorView(ex);
            return;
        }

        try {
            dp.viewReadme(group);
        } catch (ActionException ex) {
            new ErrorView(ex);
        } catch (MissingHandinException ex) {
            this.notifyHandinMissing(ex);
        }
    }

    private void viewRubricButtonActionPerformed() {
        Student student = _studentList.getSelectedValue();

        Map<Assignment, List<Part>> selection = _assignmentTree.getSelection();
        Assignment asgn = this.getSingleSelectedAssignment(selection);

        Group group;
        try {
            group = Allocator.getDataServices().getGroup(asgn, student);
        } catch (ServicesException ex) {
            new ErrorView(ex);
            return;
        }

        DistributablePart part = this.getSingleSelectedDP(_assignmentTree.getSelection());
        try {
            Allocator.getRubricManager().view(part, group, true);
        } catch (RubricException ex) {
            new ErrorView(ex, "Could not view rubric for group [" + group + "] on "
                    + "part [" + part + "].");
        }
    }

    private void printRubricButtonActionPerformed() {
        Assignment asgn = this.getSingleSelectedAssignment(_assignmentTree.getSelection());
        List<Student> students = new ArrayList<Student>(_studentList.getGenericSelectedValues());

        Collection<Group> groupsToPrint = this.getGroupsToConvertToGRD(asgn, students);

        //return value of null means "Cancel" button was clicked
        if (groupsToPrint != null) {
            CITPrinter printer = Allocator.getGradingServices().getPrinter("Select printer to print rubric");

            try {
                Allocator.getGradingServices().printGRDFiles(asgn.getHandin(), groupsToPrint, printer);
            } catch (ServicesException ex) {
                new ErrorView(ex, "Could not print GRD files.");
            }
        }
    }

    private void emailRubricButtonActionPerformed() {
        Assignment asgn = this.getSingleSelectedAssignment(_assignmentTree.getSelection());
        List<Student> students = new ArrayList<Student>(_studentList.getGenericSelectedValues());

        Collection<Group> groupsToEmail = this.getGroupsToConvertToGRD(asgn, students);

        //return value of null means "Cancel" button was clicked
        if (groupsToEmail == null) {
            return;
        }

        Allocator.getGradingServices().notifyStudents(asgn.getHandin(), groupsToEmail, true);
    }

    private Collection<Group> getGroupsToConvertToGRD(Assignment asgn, List<Student> students) {
        List<Group> groupsConverted = new ArrayList<Group>(_studentList.getGenericSelectedValues().size());
        Collection<Student> studentsWithoutGroups = new LinkedList<Student>();
        Map<Group, Collection<DistributablePart>> partsMissingRubrics = new HashMap<Group, Collection<DistributablePart>>();

        for (Student student : students) {
            Group group;
            try {
                group = Allocator.getDataServices().getGroup(asgn, student);
            } catch (ServicesException ex) {
                studentsWithoutGroups.add(student);
                continue;
            }

            Collection<DistributablePart> missing = Allocator.getRubricManager().getMissingRubrics(asgn.getHandin(), group);
            if (!missing.isEmpty()) {
                partsMissingRubrics.put(group, missing);
            }
            else {
                groupsConverted.add(group);
            }
        }

        String errMsg = "";
        if (!studentsWithoutGroups.isEmpty()) {
            errMsg += "Groups could not be determined for the following students; thus, " +
                      "their GRD files cannot be produced:\n" + studentsWithoutGroups + "\n\n";
        }

        if (!partsMissingRubrics.isEmpty()) {
            errMsg += "Some groups are missing rubrics for some distributable parts, as " +
                      "listed below.  Their their GRD files cannot be produced.\n";
            for (Group group : partsMissingRubrics.keySet()) {
                errMsg += group + ": " + partsMissingRubrics.get(group) + "\n";
            }
        }

        if (!errMsg.isEmpty()) {
            Object[] options = {"Proceed", "Cancel"};
            int shouldContinue = JOptionPane.showOptionDialog(this, errMsg,
                    "Not all students have rubrics!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (shouldContinue != JOptionPane.OK_OPTION) {
                return null;
            }
        }

        try {
            Allocator.getRubricManager().convertToGRD(asgn.getHandin(), groupsConverted);
        } catch (RubricException ex) {
            new ErrorView(ex, "Could not create GRD files to email rubrics.");
            return null;
        }

        return groupsConverted;
    }

    private void disableStudentButtonActionPerformed() {
        Student student = _studentList.getSelectedValue();
        
        try {
            if (student.isEnabled()) {
                Allocator.getDataServices().setStudentEnabled(student, false);
                this.updateDisableEnableButton(false);
            }
            else {
                Allocator.getDataServices().setStudentEnabled(student, true);
                this.updateDisableEnableButton(true);
            }
        } catch (ServicesException ex) {
            new ErrorView(ex, "Enabling/disabling student " + student + " failed.");
        }
    }

    private void updateDisableEnableButton(boolean enabled) {
        if(enabled)
        {
            _disableStudentButton.setIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.LIST_REMOVE));
            _disableStudentButton.setText("Disable Student");
        }
        else
        {
            _disableStudentButton.setIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.LIST_ADD));
            _disableStudentButton.setText("Enable Student");
        }
    }

    private void manageGroupsButtonActionPerformed() {
        new GroupsView(this, this.getSingleSelectedAssignment(_assignmentTree.getSelection()));
    }

    private Assignment getSingleSelectedAssignment(Map<Assignment, List<Part>> selection) {
        if (selection.size() != 1) {
            return null;
        }
        return selection.keySet().iterator().next();
    }

    private Part getSingleSelectedPart(Map<Assignment, List<Part>> selection) {
        return this.getSingleSelectedPart(selection, Part.class);
    }

    private DistributablePart getSingleSelectedDP(Map<Assignment, List<Part>> selection) {
        return this.getSingleSelectedPart(selection, DistributablePart.class);
    }

    private LabPart getSingleSelectedLabPart(Map<Assignment, List<Part>> selection) {
        return this.getSingleSelectedPart(selection, LabPart.class);
    }

    private <T extends Part> T getSingleSelectedPart(Map<Assignment, List<Part>> selection, Class<T> partType) {
        if (selection.size() != 1) {
            return null;
        }

        Assignment singleAsgn = selection.keySet().iterator().next();
        List<Part> parts = selection.get(singleAsgn);

        if (parts.size() != 1) {
            return null;
        }

        Part singlePart = parts.get(0);
        if (partType.isAssignableFrom(singlePart.getClass())) {
            return (T) singlePart;
        }

        return null;
    }
    
    private void assignmentTreeValueChanged(Map<Assignment, List<Part>> selection) {
        _messageLabel.setText("");
        _studentList.setEnabled(true);

        DistributablePart dp = this.getSingleSelectedDP(selection);
        if (dp != null) {
            //Create directory for the assignment so GRD files can be created,
            //even if no assignments have been untarred

            File partDir = Allocator.getPathServices().getUserPartDir(dp);
            try {
                Allocator.getFileSystemServices().makeDirectory(partDir);
            } catch (ServicesException e) {
                new ErrorView(e, "Unable to create directory for assignment: " + this.getSingleSelectedAssignment(selection).getName());
            }
            
        }

        this.updateStudentListEnabledState();
        this.updateGUI(selection);
    }

    private void updateStudentListEnabledState() {
        //if any selected assignment has groups and groups have not yet been created,
        //disable the student list and clear its selection since no student-related
        //admin functionality works without groups set
        boolean needsToBeDisabled = false;
        for (Assignment asgn : _assignmentTree.getSelection().keySet()) {
            Collection<Group> groups;
            try {
                groups = Allocator.getDataServices().getGroups(asgn);

                if (asgn.hasGroups() && groups.isEmpty()) {
                    _studentList.clearSelection();
                    _studentList.setEnabled(false);
                    _messageLabel.setText("NOTE: No groups have yet been created for this assignment.");

                    needsToBeDisabled = true;
                    break;
                }
            } catch (ServicesException ex) {
                new ErrorView(ex);
            }
        }

        if (!needsToBeDisabled) {
            _messageLabel.setText(null);
            _studentList.setEnabled(true);
        }
    }

    private void studentListValueChanged()
    {
        updateGUI(_assignmentTree.getSelection());
    }

    private void notifyHandinMissing(MissingHandinException ex)
    {
        JOptionPane.showMessageDialog(this,
            "The handin for " + ex.getGroup().getName() + " could not be found.",
            "Handin Missing",
            JOptionPane.OK_OPTION);
    }
}