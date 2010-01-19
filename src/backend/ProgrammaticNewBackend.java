package backend;

import backend.assignmentdist.AssignmentdistView;
import backend.assignmentdist.ReassignView;
import backend.gradereport.GradeReportView;
import backend.stathist.StatHistView;
import components.ParameterizedJList;
import config.Assignment;
import config.HandinPart;
import config.LabPart;
import config.Part;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
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
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    private static final Dimension MAIN_PANEL_SIZE = new Dimension(1150, 700);
    private static final Dimension ASSIGNMENT_LIST_PANEL_SIZE = new Dimension(185, MAIN_PANEL_SIZE.height);
    private static final Dimension STUDENT_LIST_PANEL_SIZE = new Dimension(185, MAIN_PANEL_SIZE.height);
    private static final Dimension GENERAL_COMMANDS_PANEL_SIZE = new Dimension(195, MAIN_PANEL_SIZE.height);
    private static final Dimension MIDDLE_PANEL_SIZE = new Dimension(MAIN_PANEL_SIZE.width -
                                                                     ASSIGNMENT_LIST_PANEL_SIZE.width -
                                                                     STUDENT_LIST_PANEL_SIZE.width -
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
    private JButton[] _assignmentButtons, _generalCommandsButtons, _studentButtons, _multipleStudentButtons;
    private SelectedLabel _selectedAssignmentLabel, _selectedStudentLabel;
    private ParameterizedJList<Assignment> _assignmentList;
    private ParameterizedJList<String> _studentList;
    private JPanel _cardPanel = new JPanel();
    private List<String> _studentLogins;
    private final static String WELCOME_PANEL_TAG = "Welcome panel",
                                MULTI_SELECT_PANEL_TAG = "Multiple selected students panel",
                                SINGLE_SELECT_PANEL_TAG = "Single selected students panel";
    private CardLayout _cardLayout;

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

        _multipleStudentButtons = new JButton[]
        {
            _chartsButton, _emailReportsButton, _printCodeButton, _printRubricButton
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
        assignmentListPanel.setPreferredSize(ASSIGNMENT_LIST_PANEL_SIZE);
        this.initializeAssignmentListPanel(assignmentListPanel);
        mainPanel.add(assignmentListPanel);

        //student list panel
        JPanel studentListPanel = new JPanel();
        studentListPanel.setPreferredSize(STUDENT_LIST_PANEL_SIZE);
        this.initializeStudentListPanel(studentListPanel);
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
        this.initializeSelectedAssignmentPanel(selectedAssignmentPanel);
        middlePanel.add(selectedAssignmentPanel, BorderLayout.NORTH);

        //selected student panel / card panel
        JPanel multiPanel = new JPanel();
        multiPanel.setPreferredSize(MULTI_PANEL_SIZE);
        this.initializeMultiPanel(multiPanel);
        middlePanel.add(multiPanel, BorderLayout.WEST);

        //selected student button panel
        JPanel studentButtonPanel = new JPanel();
        studentButtonPanel.setPreferredSize(STUDENT_BUTTON_PANEL_SIZE);
        this.initializeStudentButtonPanel(studentButtonPanel);
        middlePanel.add(studentButtonPanel, BorderLayout.EAST);

        //general commands button
        JPanel generalCommandsPanel = new JPanel();
        generalCommandsPanel.setPreferredSize(GENERAL_COMMANDS_PANEL_SIZE);
        this.initializeGeneralCommandPanel(generalCommandsPanel);
        mainPanel.add(generalCommandsPanel);

        this.setResizable(false);
    }

    private void initializeMultiPanel(JPanel panel)
    {
        //Student label
        Dimension labelSize = new Dimension(MULTI_PANEL_SIZE.width - 10, 20);
        _selectedStudentLabel = new SelectedLabel("Selected Student", "students selected");
        _selectedStudentLabel.setPreferredSize(labelSize);
        panel.add(_selectedStudentLabel);

        //Card panel
        Dimension cardPanelSize = new Dimension(MULTI_PANEL_SIZE.width - 10,
                                                MULTI_PANEL_SIZE.height -
                                                labelSize.height - 15);
        _cardLayout = new CardLayout();
        _cardPanel = new JPanel(_cardLayout);
        _cardPanel.setPreferredSize(cardPanelSize);
        panel.add(_cardPanel);

        //Welcome card
        JPanel welcomeCard = new JPanel();
        welcomeCard.setPreferredSize(cardPanelSize);
        _cardPanel.add(welcomeCard, WELCOME_PANEL_TAG);
        this.initializeWelcomePanel(welcomeCard);

        //Multiselect card
        JPanel multiSelectCard = new JPanel();
        multiSelectCard.setPreferredSize(cardPanelSize);
        _cardPanel.add(multiSelectCard, MULTI_SELECT_PANEL_TAG);
        this.initializeMultiSelectPanel(multiSelectCard);

        //Singleselect card
        _singleSelectionPanel = new SingleSelectionPanel(cardPanelSize);
        _cardPanel.add(_singleSelectionPanel, SINGLE_SELECT_PANEL_TAG);
    }

    private SingleSelectionPanel _singleSelectionPanel;

    private class SingleSelectionPanel extends JPanel
    {
        private String _studentLogin;
        private Assignment _asgn;
        private Dimension _size;

        private JComboBox _nonHandinBox, _labBox;
        private ScoreField _nonHandinEarnedField, _nonHandinOutOfField,
                                    _labEarnedField, _labOutOfField,
                                    _handinEarnedField, _handinOutOfField;
        private JLabel _nonHandinScoreLabel, _labScoreLabel, _handinScoreLabel,
                       _overallEarnedPointsLabel, _overallTotalPointsLabel,
                       _overallScoreLabel;
        private JButton _updateGradeButton;

        public SingleSelectionPanel(Dimension size)
        {
            _size = size;
            this.setPreferredSize(size);

            this.initComponents();
        }

        private void initComponents()
        {
            FlowLayout fLayout = new FlowLayout();
            fLayout.setHgap(0);
            fLayout.setVgap(0);
            this.setLayout(fLayout);

            //Non Handin
            Dimension nonHandinPanelSize = new Dimension(_size.width - 10, 130);
            JPanel nonHandinPanel = new JPanel();
            nonHandinPanel.setPreferredSize(nonHandinPanelSize);
            initNonHandinPanel(nonHandinPanel);
            this.add(nonHandinPanel);

            //Lab
            Dimension labPanelSize = new Dimension(_size.width - 10, 130);
            JPanel labPanel = new JPanel();
            labPanel.setPreferredSize(labPanelSize);
            initLabPartsPanel(labPanel);
            this.add(labPanel);

            //Handin
            Dimension handinPanelSize = new Dimension(_size.width - 10, 110);
            JPanel handinPanel = new JPanel();
            handinPanel.setPreferredSize(handinPanelSize);
            initHandinPanel(handinPanel);
            this.add(handinPanel);

            //Overall
            Dimension overallPanelSize = new Dimension(_size.width -10, 130);
            JPanel overallPanel = new JPanel();
            overallPanel.setPreferredSize(overallPanelSize);
            this.initOverallPanel(overallPanel);
            this.add(overallPanel);

            //Update
            Dimension updatePanelSize = new Dimension(_size.width - 10,
                                                      _size.height -
                                                      nonHandinPanelSize.height -
                                                      labPanelSize.height -
                                                      handinPanelSize.height -
                                                      overallPanelSize.height);
            JPanel updatePanel = new JPanel();
            updatePanel.setPreferredSize(updatePanelSize);
            this.initUpdatePanel(updatePanel);
            this.add(updatePanel);
        }

        private void initUpdatePanel(JPanel panel)
        {
            _updateGradeButton = new JButton("Submit Non-Handin Grade");
            _updateGradeButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    double earned = _nonHandinEarnedField.getNumberValue();
                    Part part = (Part) _nonHandinBox.getSelectedItem();
                    Allocator.getDatabaseIO().enterGrade(_studentLogin, part, earned);
                }
            });

            panel.add(_updateGradeButton);
        }

        private void initNonHandinPanel(JPanel panel)
        {
            FlowLayout fLayout = new FlowLayout();
            fLayout.setHgap(0);
            fLayout.setVgap(0);
            panel.setLayout(fLayout);

            Dimension panelSize = panel.getPreferredSize();

            //title label
            JLabel titleLabel = new JLabel("<html><b>Non-Handin Parts</b><html>");
            Dimension titleLabelSize = new Dimension(panelSize.width, 20);
            titleLabel.setPreferredSize(titleLabelSize);
            panel.add(titleLabel);

            //sections
            JPanel sectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension sectionPanelSize = new Dimension(panelSize.width - 10,
                                                       panelSize.height - titleLabelSize.height);
            sectionPanel.setPreferredSize(sectionPanelSize);
            panel.add(sectionPanel);

            //Part selection
            Dimension selectionPanelSize = new Dimension(sectionPanelSize.width, 30);
            JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            selectionPanel.setPreferredSize(selectionPanelSize);
            sectionPanel.add(selectionPanel);

            Dimension selectLabelSize = new Dimension(50, 30);
            JLabel selectLabel = new JLabel("Select:");
            selectLabel.setPreferredSize(selectLabelSize);
            selectionPanel.add(selectLabel);

            Dimension boxSize = new Dimension(sectionPanelSize.width - selectLabelSize.width,
                                              22);
            _nonHandinBox = new JComboBox(new String[]{ "Some text here" });
            _nonHandinBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    Part part = (Part) _nonHandinBox.getSelectedItem();
                    if(part != null)
                    {
                        double earned = Allocator.getDatabaseIO().getStudentScore(_studentLogin, part);
                        double outOf = part.getPoints();

                        _nonHandinEarnedField.setNumberValue(earned);
                        _nonHandinOutOfField.setNumberValue(outOf);
                    }
                }
            });
            _nonHandinBox.setPreferredSize(boxSize);
            selectionPanel.add(_nonHandinBox);
            
            //Points
            JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension pointsPanelSize = new Dimension(sectionPanelSize.width, 30);
            pointsPanel.setPreferredSize(pointsPanelSize);
            sectionPanel.add(pointsPanel);

            Dimension earnedLabelSize = new Dimension(100, 30);
            Dimension earnedFieldSize = new Dimension(70, 22);
            Dimension outOfLabelSize = new Dimension(50, 30);
            Dimension outOfFieldSize = new Dimension(70, 22);
            Dimension pointsGapSize = new Dimension(sectionPanelSize.width -
                                                    earnedLabelSize.width -
                                                    earnedFieldSize.width -
                                                    outOfLabelSize.width -
                                                    outOfFieldSize.width, 30);

            JLabel earnedLabel = new JLabel("Earned Points:");
            earnedLabel.setPreferredSize(earnedLabelSize);
            pointsPanel.add(earnedLabel);

            _nonHandinEarnedField = new ScoreField();
            _nonHandinEarnedField.setPreferredSize(earnedFieldSize);
            pointsPanel.add(_nonHandinEarnedField);

            pointsPanel.add(Box.createRigidArea(pointsGapSize));

            JLabel outOfLabel = new JLabel("Out Of:");
            outOfLabel.setPreferredSize(outOfLabelSize);
            pointsPanel.add(outOfLabel);

            _nonHandinOutOfField = new ScoreField();
            _nonHandinOutOfField.setPreferredSize(outOfFieldSize);
            _nonHandinOutOfField.setBackground(Color.LIGHT_GRAY);
            _nonHandinOutOfField.setEditable(false);
            pointsPanel.add(_nonHandinOutOfField);

            //Score
            JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension scorePanelSize = new Dimension(sectionPanelSize.width, 30);
            scorePanel.setPreferredSize(scorePanelSize);
            sectionPanel.add(scorePanel);

            Dimension scoreTextLabelSize = new Dimension(100, 30);
            Dimension scoreReceivedLabelSize = new Dimension(scorePanelSize.width - scoreTextLabelSize.width, 30);

            JLabel scoreTextLabel = new JLabel("Score (%)");
            scoreTextLabel.setPreferredSize(scoreTextLabelSize);
            scorePanel.add(scoreTextLabel);

            _nonHandinScoreLabel = new JLabel("0");
            _nonHandinScoreLabel.setPreferredSize(scoreReceivedLabelSize);
            _nonHandinScoreLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            _nonHandinScoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            scorePanel.add(_nonHandinScoreLabel);
        }

        private void initLabPartsPanel(JPanel panel)
        {
            FlowLayout fLayout = new FlowLayout();
            fLayout.setHgap(0);
            fLayout.setVgap(0);
            panel.setLayout(fLayout);

            Dimension panelSize = panel.getPreferredSize();

            //title label
            JLabel titleLabel = new JLabel("<html><b>Lab Parts</b><html>");
            Dimension titleLabelSize = new Dimension(panelSize.width, 20);
            titleLabel.setPreferredSize(titleLabelSize);
            panel.add(titleLabel);

            //sections
            JPanel sectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension sectionPanelSize = new Dimension(panelSize.width - 10,
                                                       panelSize.height - titleLabelSize.height);
            sectionPanel.setPreferredSize(sectionPanelSize);
            panel.add(sectionPanel);

            //Part selection
            Dimension selectionPanelSize = new Dimension(sectionPanelSize.width, 30);
            JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            selectionPanel.setPreferredSize(selectionPanelSize);
            sectionPanel.add(selectionPanel);

            Dimension selectLabelSize = new Dimension(50, 30);
            JLabel selectLabel = new JLabel("Select:");
            selectLabel.setPreferredSize(selectLabelSize);
            selectionPanel.add(selectLabel);

            Dimension boxSize = new Dimension(sectionPanelSize.width - selectLabelSize.width,
                                              22);
            _labBox = new JComboBox();
            _labBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    Part part = (Part) _labBox.getSelectedItem();
                    if(part != null)
                    {
                        double earned = Allocator.getDatabaseIO().getStudentScore(_studentLogin, part);
                        double outOf = part.getPoints();

                        _labEarnedField.setNumberValue(earned);
                        _labOutOfField.setNumberValue(outOf);
                    }
                }
            });
            _labBox.setPreferredSize(boxSize);
            selectionPanel.add(_labBox);

            //Points
            JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension pointsPanelSize = new Dimension(sectionPanelSize.width, 30);
            pointsPanel.setPreferredSize(pointsPanelSize);
            sectionPanel.add(pointsPanel);

            Dimension earnedLabelSize = new Dimension(100, 30);
            Dimension earnedFieldSize = new Dimension(70, 22);
            Dimension outOfLabelSize = new Dimension(50, 30);
            Dimension outOfFieldSize = new Dimension(70, 22);
            Dimension pointsGapSize = new Dimension(sectionPanelSize.width -
                                                    earnedLabelSize.width -
                                                    earnedFieldSize.width -
                                                    outOfLabelSize.width -
                                                    outOfFieldSize.width, 30);

            JLabel earnedLabel = new JLabel("Earned Points:");
            earnedLabel.setPreferredSize(earnedLabelSize);
            pointsPanel.add(earnedLabel);

            _labEarnedField = new ScoreField();
            _labEarnedField.setPreferredSize(earnedFieldSize);
            _labEarnedField.setBackground(Color.LIGHT_GRAY);
            _labEarnedField.setEditable(false);
            pointsPanel.add(_labEarnedField);

            pointsPanel.add(Box.createRigidArea(pointsGapSize));

            JLabel outOfLabel = new JLabel("Out Of:");
            outOfLabel.setPreferredSize(outOfLabelSize);
            pointsPanel.add(outOfLabel);

            _labOutOfField = new ScoreField();
            _labOutOfField.setPreferredSize(outOfFieldSize);
            _labOutOfField.setBackground(Color.LIGHT_GRAY);
            _labOutOfField.setEditable(false);
            pointsPanel.add(_labOutOfField);

            //Score
            JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension scorePanelSize = new Dimension(sectionPanelSize.width, 30);
            scorePanel.setPreferredSize(scorePanelSize);
            sectionPanel.add(scorePanel);

            Dimension scoreTextLabelSize = new Dimension(100, 30);
            Dimension scoreReceivedLabelSize = new Dimension(scorePanelSize.width - scoreTextLabelSize.width, 30);

            JLabel scoreTextLabel = new JLabel("Score (%)");
            scoreTextLabel.setPreferredSize(scoreTextLabelSize);
            scorePanel.add(scoreTextLabel);

            _labScoreLabel = new JLabel("0");
            _labScoreLabel.setPreferredSize(scoreReceivedLabelSize);
            _labScoreLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            _labScoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            scorePanel.add(_labScoreLabel);
        }

        private void initHandinPanel(JPanel panel)
        {
            FlowLayout fLayout = new FlowLayout();
            fLayout.setHgap(0);
            fLayout.setVgap(0);
            panel.setLayout(fLayout);

            Dimension panelSize = panel.getPreferredSize();

            //title label
            JLabel titleLabel = new JLabel("<html><b>Handin Part</b><html>");
            Dimension titleLabelSize = new Dimension(panelSize.width, 20);
            titleLabel.setPreferredSize(titleLabelSize);
            panel.add(titleLabel);

            //sections
            JPanel sectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension sectionPanelSize = new Dimension(panelSize.width - 10,
                                                       panelSize.height - titleLabelSize.height);
            sectionPanel.setPreferredSize(sectionPanelSize);
            panel.add(sectionPanel);

            //Points
            JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension pointsPanelSize = new Dimension(sectionPanelSize.width, 30);
            pointsPanel.setPreferredSize(pointsPanelSize);
            sectionPanel.add(pointsPanel);

            Dimension earnedLabelSize = new Dimension(100, 30);
            Dimension earnedFieldSize = new Dimension(70, 22);
            Dimension outOfLabelSize = new Dimension(50, 30);
            Dimension outOfFieldSize = new Dimension(70, 22);
            Dimension pointsGapSize = new Dimension(sectionPanelSize.width -
                                                    earnedLabelSize.width -
                                                    earnedFieldSize.width -
                                                    outOfLabelSize.width -
                                                    outOfFieldSize.width, 30);

            JLabel earnedLabel = new JLabel("Earned Points:");
            earnedLabel.setPreferredSize(earnedLabelSize);
            pointsPanel.add(earnedLabel);

            _handinEarnedField = new ScoreField();
            _handinEarnedField.setPreferredSize(earnedFieldSize);
            _handinEarnedField.setBackground(Color.LIGHT_GRAY);
            _handinEarnedField.setEditable(false);
            pointsPanel.add(_handinEarnedField);

            pointsPanel.add(Box.createRigidArea(pointsGapSize));

            JLabel outOfLabel = new JLabel("Out Of:");
            outOfLabel.setPreferredSize(outOfLabelSize);
            pointsPanel.add(outOfLabel);

            _handinOutOfField = new ScoreField();
            _handinOutOfField.setPreferredSize(outOfFieldSize);
            _handinOutOfField.setBackground(Color.LIGHT_GRAY);
            _handinOutOfField.setEditable(false);
            pointsPanel.add(_handinOutOfField);

            //Score
            JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension scorePanelSize = new Dimension(sectionPanelSize.width, 30);
            scorePanel.setPreferredSize(scorePanelSize);
            sectionPanel.add(scorePanel);

            Dimension scoreTextLabelSize = new Dimension(100, 30);
            Dimension scoreReceivedLabelSize = new Dimension(scorePanelSize.width - scoreTextLabelSize.width, 30);

            JLabel scoreTextLabel = new JLabel("Score (%)");
            scoreTextLabel.setPreferredSize(scoreTextLabelSize);
            scorePanel.add(scoreTextLabel);

            _handinScoreLabel = new JLabel("0");
            _handinScoreLabel.setPreferredSize(scoreReceivedLabelSize);
            _handinScoreLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            _handinScoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            scorePanel.add(_handinScoreLabel);
        }

        private void initOverallPanel(JPanel panel)
        {
            FlowLayout fLayout = new FlowLayout();
            fLayout.setHgap(0);
            fLayout.setVgap(0);
            panel.setLayout(fLayout);

            Dimension panelSize = panel.getPreferredSize();

            //title label
            JLabel titleLabel = new JLabel("<html><b>Overall Grade</b><html>");
            Dimension titleLabelSize = new Dimension(panelSize.width, 20);
            titleLabel.setPreferredSize(titleLabelSize);
            panel.add(titleLabel);

            //sections
            JPanel sectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension sectionPanelSize = new Dimension(panelSize.width - 10,
                                                       panelSize.height - titleLabelSize.height);
            sectionPanel.setPreferredSize(sectionPanelSize);
            panel.add(sectionPanel);

            //Earned points
            JPanel earnedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension earnedPanelSize = new Dimension(sectionPanelSize.width, 30);
            earnedPanel.setPreferredSize(earnedPanelSize);
            sectionPanel.add(earnedPanel);

            Dimension earnedTextLabelSize = new Dimension(100, 30);
            Dimension earnedReceivedLabelSize = new Dimension(earnedPanelSize.width - earnedTextLabelSize.width, 30);

            JLabel earnedTextLabel = new JLabel("Earned Points");
            earnedTextLabel.setPreferredSize(earnedTextLabelSize);
            earnedPanel.add(earnedTextLabel);

            _overallEarnedPointsLabel = new JLabel("0");
            _overallEarnedPointsLabel.setPreferredSize(earnedReceivedLabelSize);
            _overallEarnedPointsLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            _overallEarnedPointsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            earnedPanel.add(_overallEarnedPointsLabel);

            //Total points
            JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension totalPanelSize = new Dimension(sectionPanelSize.width, 30);
            totalPanel.setPreferredSize(totalPanelSize);
            sectionPanel.add(totalPanel);

            Dimension totalTextLabelSize = new Dimension(100, 30);
            Dimension totalReceivedLabelSize = new Dimension(totalPanelSize.width - totalTextLabelSize.width, 30);

            JLabel totalTextLabel = new JLabel("Total Points");
            totalTextLabel.setPreferredSize(totalReceivedLabelSize);
            totalPanel.add(totalTextLabel);

            _overallTotalPointsLabel = new JLabel("0");
            _overallTotalPointsLabel.setPreferredSize(new Dimension(100,30));
            _overallTotalPointsLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            _overallTotalPointsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            totalPanel.add(_overallTotalPointsLabel);

            //Score
            JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            Dimension scorePanelSize = new Dimension(sectionPanelSize.width, 30);
            scorePanel.setPreferredSize(scorePanelSize);
            sectionPanel.add(scorePanel);

            Dimension scoreTextLabelSize = new Dimension(100, 30);
            Dimension scoreReceivedLabelSize = new Dimension(scorePanelSize.width - scoreTextLabelSize.width, 30);

            JLabel scoreTextLabel = new JLabel("Score (%)");
            scoreTextLabel.setPreferredSize(scoreTextLabelSize);
            scorePanel.add(scoreTextLabel);

            _overallScoreLabel = new JLabel("0");
            _overallScoreLabel.setPreferredSize(scoreReceivedLabelSize);
            _overallScoreLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            _overallScoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            scorePanel.add(_overallScoreLabel);
        }

        private void clearComponents()
        {
            _nonHandinBox.removeAllItems();
            _nonHandinOutOfField.setText("");
            _nonHandinEarnedField.setText("");
            _nonHandinScoreLabel.setText("0");

            _labBox.removeAllItems();
            _labOutOfField.setText("");
            _labEarnedField.setText("");
            _labScoreLabel.setText("0");

            _handinOutOfField.setText("");
            _handinEarnedField.setText("");
            _handinScoreLabel.setText("0");
        }

        public void updateView(String studentLogin, Assignment asgn)
        {
            _studentLogin = studentLogin;
            _asgn = asgn;

            this.clearComponents();

            //Populate the comboboxes
            for(Part part : asgn.getNonHandinParts())
            {
                _nonHandinBox.addItem(part);
            }
            _updateGradeButton.setEnabled(asgn.hasNonHandinParts());
            _nonHandinBox.setEnabled(asgn.hasNonHandinParts());

            for(LabPart part : asgn.getLabParts())
            {
                _labBox.addItem(part);
            }
            _labBox.setEnabled(asgn.hasLabParts());

            if(asgn.hasHandinPart())
            {
                HandinPart handinPart = asgn.getHandinPart();

                double handinEarned = Allocator.getDatabaseIO().getStudentScore(studentLogin, handinPart);
                double handinOutOf = handinPart.getPoints();

                _handinEarnedField.setNumberValue(handinEarned);
                _handinOutOfField.setNumberValue(handinOutOf);
            }
        }

        private class ScoreField extends JFormattedTextField
        {
            public ScoreField()
            {
                super(NumberFormat.getNumberInstance());

                this.getDocument().addDocumentListener(new DocumentListener()
                {
                    public void insertUpdate(DocumentEvent de)
                    {
                        updateScores();
                    }

                    public void removeUpdate(DocumentEvent de)
                    {
                        updateScores();
                    }

                    public void changedUpdate(DocumentEvent de) {}

                });

                this.addFocusListener(new FocusListener()
                {
                    public void focusGained(FocusEvent fe) {}

                    public void focusLost(FocusEvent fe)
                    {
                        updateScores();
                    }
                });
            }

            public void setNumberValue(double value)
            {
                this.setText(value + "");
            }

            public double getNumberValue()
            {
                double value = 0;
                try
                {
                    value = Double.parseDouble(this.getText());
                }
                catch(Exception e) { }

                return value;
            }
        }

        private void updateScores()
        {
            if(_asgn == null)
            {
                return;
            }

            //Update non-handin part score
            double nonHandinEarned = _nonHandinEarnedField.getNumberValue();
            double nonHandinOutOf = _nonHandinOutOfField.getNumberValue();
            if(nonHandinOutOf != 0)
            {
                double nonHandinPercent = Allocator.getGeneralUtilities().round(nonHandinEarned / nonHandinOutOf * 100.0, 2);
                _nonHandinScoreLabel.setText(nonHandinPercent + "");
            }

            //Update lab parts
            double labEarned = _labEarnedField.getNumberValue();
            double labOutOf = _labOutOfField.getNumberValue();
            if(labOutOf != 0)
            {
                double labPercent = Allocator.getGeneralUtilities().round(labEarned / labOutOf * 100.0, 2);
                _labScoreLabel.setText(labPercent + "");
            }

            //Update handin parts
            double handinEarned = _handinEarnedField.getNumberValue();
            double handinOutOf = _handinOutOfField.getNumberValue();
            if(handinOutOf != 0)
            {
                double handinPercent = Allocator.getGeneralUtilities().round(handinEarned / handinOutOf * 100.0, 2);
                _handinScoreLabel.setText(handinPercent + "");
            }

            //Overall
            double totalEarned = 0;
            double totalOutOf = 0;

            //Non-handin
            if(_asgn.hasHandinPart())
            {
                Part selectedPart = (Part) _nonHandinBox.getSelectedItem();
                totalEarned += nonHandinEarned;
                totalOutOf += nonHandinOutOf;


                for(Part part : _asgn.getNonHandinParts())
                {
                    if(part != selectedPart)
                    {
                        totalEarned += Allocator.getDatabaseIO().getStudentScore(_studentLogin, part);
                        totalOutOf += part.getPoints();
                    }
                }
            }
            //Lab
            if(_asgn.hasLabParts())
            {
                Part selectedPart = (Part) _labBox.getSelectedItem();
                totalEarned += labEarned;
                totalOutOf += labOutOf;

                for(Part part : _asgn.getLabParts())
                {
                    if(part != selectedPart)
                    {
                        totalEarned += Allocator.getDatabaseIO().getStudentScore(_studentLogin, part);
                        totalOutOf += part.getPoints();
                    }
                }
            }
            //Handin
            if(_asgn.hasHandinPart())
            {
                totalEarned += handinEarned;
                totalOutOf += handinOutOf;
            }

            if(totalOutOf != 0)
            {
                double totalScore = Allocator.getGeneralUtilities().round(totalEarned / totalOutOf * 100.0, 2);
                _overallScoreLabel.setText(totalScore + "");
            }

            _overallEarnedPointsLabel.setText(totalEarned + "");
            _overallTotalPointsLabel.setText(totalOutOf + "");
        }
    }

    private void initializeMultiSelectPanel(JPanel panel)
    {
        
    }

    private void initializeWelcomePanel(JPanel panel)
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

    private void initializeStudentListPanel(JPanel panel)
    {
        FlowLayout fLayout = new FlowLayout();
        fLayout.setVgap(0);
        fLayout.setHgap(0);
        panel.setLayout(fLayout);

        //Top panel: Assignments label, select all / select none buttons, filter text area
        Dimension controlPanelSize = new Dimension(STUDENT_LIST_PANEL_SIZE.width - 10, 80);
        fLayout = new FlowLayout();
        fLayout.setVgap(0);
        fLayout.setHgap(0);
        JPanel controlPanel = new JPanel(fLayout);
        controlPanel.setPreferredSize(controlPanelSize);
        panel.add(controlPanel);

        JLabel studentLabel = new JLabel("<html><b>Students</b></html>");
        studentLabel.setPreferredSize(new Dimension(controlPanelSize.width, 20));
        controlPanel.add(studentLabel, BorderLayout.NORTH);

        //Gap space
        controlPanel.add(Box.createRigidArea(new Dimension(controlPanelSize.width, 5)));

        //Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1,2,5,5));
        buttonPanel.setPreferredSize(new Dimension(controlPanelSize.width, 25));
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
        controlPanel.add(Box.createRigidArea(new Dimension(controlPanelSize.width, 5)));

        //Student filter
        final JTextField filterField = new JTextField();
        filterField.setPreferredSize(new Dimension(controlPanelSize.width, 25));
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
        panel.add(Box.createRigidArea(new Dimension(STUDENT_LIST_PANEL_SIZE.width, 5)));

        //List
        Dimension listPaneSize = new Dimension(STUDENT_LIST_PANEL_SIZE.width - 10,
                                               STUDENT_LIST_PANEL_SIZE.height -
                                               controlPanelSize.height - 10);
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
        studentPane.setPreferredSize(listPaneSize);
        panel.add(studentPane);
    }

    private void initializeAssignmentListPanel(JPanel panel)
    {
        FlowLayout fLayout = new FlowLayout();
        fLayout.setVgap(0);
        fLayout.setHgap(0);
        panel.setLayout(fLayout);

        //Top panel: Assignments label, select all / select none buttons, drop down list
        Dimension controlPanelSize = new Dimension(ASSIGNMENT_LIST_PANEL_SIZE.width - 10, 80);
        //BorderLayout bLayout = new BorderLayout();
        fLayout = new FlowLayout();
        fLayout.setVgap(0);
        fLayout.setHgap(0);
        JPanel controlPanel = new JPanel(fLayout);
        controlPanel.setPreferredSize(controlPanelSize);
        panel.add(controlPanel);

        //Label
        JLabel assignmentLabel = new JLabel("<html><b>Assignments</b></html>");
        assignmentLabel.setPreferredSize(new Dimension(controlPanelSize.width, 20));
        controlPanel.add(assignmentLabel);

        //Gap space
        controlPanel.add(Box.createRigidArea(new Dimension(controlPanelSize.width, 5)));

        //Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1,2,5,5));
        buttonPanel.setPreferredSize(new Dimension(controlPanelSize.width, 25));
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
        controlPanel.add(Box.createRigidArea(new Dimension(controlPanelSize.width, 5)));

        //Assignment box
        AssignmentOption[] options = {
                                        new AssignmentOption("All Assignments", Allocator.getCourseInfo().getAssignments()),
                                        new AssignmentOption("With Handin Part", Allocator.getCourseInfo().getHandinAssignments()),
                                        new AssignmentOption("With NonHandin Parts", Allocator.getCourseInfo().getNonHandinAssignments()),
                                        new AssignmentOption("With Lab Parts", Allocator.getCourseInfo().getLabAssignments())
                                     };
        final JComboBox assignmentsBox = new JComboBox(options);
        assignmentsBox.setPreferredSize(new Dimension(controlPanelSize.width, 25));
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
        panel.add(Box.createRigidArea(new Dimension(ASSIGNMENT_LIST_PANEL_SIZE.width, 5)));

        //List
        Dimension listPaneSize = new Dimension(ASSIGNMENT_LIST_PANEL_SIZE.width - 10,
                                               ASSIGNMENT_LIST_PANEL_SIZE.height -
                                               controlPanelSize.height - 10);
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
        assignmentPane.setPreferredSize(listPaneSize);
        panel.add(assignmentPane);
    }

    private void initializeStudentButtonPanel(JPanel panel)
    {
        FlowLayout fLayout = new FlowLayout();
        fLayout.setVgap(0);
        fLayout.setHgap(0);
        panel.setLayout(fLayout);

        //buffer room
        int bufferHeight = 90;
        int bufferWidth = 10;

        //Add some vertical space
        panel.add(Box.createRigidArea(new Dimension(STUDENT_BUTTON_PANEL_SIZE.width, bufferHeight / 2)));

        //Button panel
        Dimension buttonPanelSize = new Dimension(STUDENT_BUTTON_PANEL_SIZE.width - bufferWidth,
                                                  STUDENT_BUTTON_PANEL_SIZE.height - bufferHeight / 2);

        int buttonSlots = 17;
        GridLayout gLayout = new GridLayout(buttonSlots,1,0,2);
        JPanel buttonPanel = new JPanel(gLayout);
        //buttonPanel.setBackground(Color.CYAN);
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
    
    private void initializeSelectedAssignmentPanel(JPanel panel)
    {
        FlowLayout fLayout = new FlowLayout();
        fLayout.setVgap(0);
        fLayout.setHgap(0);
        panel.setLayout(fLayout);

        //Label
        Dimension labelSize = new Dimension(SELECTED_ASSIGNMENT_PANEL_SIZE.width, 30);
        _selectedAssignmentLabel = new SelectedLabel("Selected Assignment", "assignments selected");
        _selectedAssignmentLabel.setPreferredSize(labelSize);
        panel.add(_selectedAssignmentLabel);

        //Button panel
        Dimension buttonPanelSize = new Dimension(SELECTED_ASSIGNMENT_PANEL_SIZE.width - 10,
                                                  SELECTED_ASSIGNMENT_PANEL_SIZE.height -
                                                  labelSize.height);
        GridLayout gLayout = new GridLayout(2,3,5,5);
        JPanel buttonPanel = new JPanel(gLayout);
        buttonPanel.setPreferredSize(buttonPanelSize);
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

    private void initializeGeneralCommandPanel(JPanel panel)
    {
        FlowLayout fLayout = new FlowLayout();
        fLayout.setVgap(0);
        fLayout.setHgap(0);
        panel.setLayout(fLayout);

        //Add label "General Commands"
        Dimension labelSize = new Dimension(GENERAL_COMMANDS_PANEL_SIZE.width, 30);
        JLabel generalCommandsLabel = new JLabel("<html><b>&nbsp; General Commands</b></html>");
        generalCommandsLabel.setPreferredSize(labelSize);
        panel.add(generalCommandsLabel);

        //Buttons
        int buttonSlots = 17;
        Dimension buttonPanelSize = new Dimension(GENERAL_COMMANDS_PANEL_SIZE.width - 10,
                                                  GENERAL_COMMANDS_PANEL_SIZE.height -
                                                  labelSize.height);
        GridLayout gLayout = new GridLayout(buttonSlots,1,5,10);
        JPanel buttonPanel = new JPanel(gLayout);
        buttonPanel.setPreferredSize(buttonPanelSize);
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

    }

    private void exportGradesButtonActionPerformed()
    {
        Allocator.getCSVExporter().export();
    }

    private void resetDatabaseButtonActionPerformed()
    {

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