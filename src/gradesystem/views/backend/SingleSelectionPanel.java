package gradesystem.views.backend;

import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import gradesystem.config.LabPart;
import gradesystem.config.Part;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import gradesystem.Allocator;

/**
 * Single selection panel of the backend.
 *
 * @author jak2
 */
class SingleSelectionPanel extends JPanel
{
    //Dimensions
    private static final Dimension PANEL_SIZE = BackendView.MULTI_PANEL_CARD_SIZE;

    //Student and assignment this is displaying
    private String _studentLogin;
    private Assignment _asgn;

    //GUI components
    private JComboBox _nonHandinBox, _labBox;
    private ScoreField _nonHandinEarnedField, _nonHandinOutOfField,
                       _labEarnedField, _labOutOfField,
                       _handinEarnedField, _handinOutOfField;
    private JLabel _nonHandinScoreLabel, _labScoreLabel, _handinScoreLabel,
                   _overallEarnedPointsLabel, _overallTotalPointsLabel,
                   _overallScoreLabel;
    private JButton _submitGradeButton;
    private boolean _suppressUpdateScores = false;

    public SingleSelectionPanel()
    {
        this.setPreferredSize(PANEL_SIZE);

        this.initComponents();
    }

    private static final Dimension
    NON_HANDIN_PANEL_SIZE = new Dimension(PANEL_SIZE.width - 10, 130),
    LAB_PANEL_SIZE = new Dimension(PANEL_SIZE.width - 10, 130),
    HANDIN_PANEL_SIZE = new Dimension(PANEL_SIZE.width - 10, 110),
    OVERALL_PANEL_SIZE = new Dimension(PANEL_SIZE.width - 10, 130),
    UPDATE_PANEL_SIZE = new Dimension(PANEL_SIZE.width - 10,
                                      PANEL_SIZE.height -
                                      NON_HANDIN_PANEL_SIZE.height -
                                      LAB_PANEL_SIZE.height -
                                      HANDIN_PANEL_SIZE.height -
                                      OVERALL_PANEL_SIZE.height);
    private void initComponents()
    {
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //Non Handin
        JPanel nonHandinPanel = new JPanel();
        nonHandinPanel.setPreferredSize(NON_HANDIN_PANEL_SIZE);
        this.initNonHandinPanel(nonHandinPanel);
        this.add(nonHandinPanel);

        //Lab
        JPanel labPanel = new JPanel();
        labPanel.setPreferredSize(LAB_PANEL_SIZE);
        this.initLabPartsPanel(labPanel);
        this.add(labPanel);

        //Handin
        JPanel handinPanel = new JPanel();
        handinPanel.setPreferredSize(HANDIN_PANEL_SIZE);
        this.initHandinPanel(handinPanel);
        this.add(handinPanel);

        //Overall
        JPanel overallPanel = new JPanel();
        overallPanel.setPreferredSize(OVERALL_PANEL_SIZE);
        this.initOverallPanel(overallPanel);
        this.add(overallPanel);

        //Update
        JPanel updatePanel = new JPanel();
        updatePanel.setPreferredSize(UPDATE_PANEL_SIZE);
        this.initUpdatePanel(updatePanel);
        this.add(updatePanel);
    }

    private void initUpdatePanel(JPanel panel)
    {
        _submitGradeButton = new JButton("Submit Non-Handin Grade");
        _submitGradeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae) {
                if (_asgn.hasNonHandinParts()) {
                    double nonHandinEarned = _nonHandinEarnedField.getNumberValue();
                    Part noneHandinPart = (Part) _nonHandinBox.getSelectedItem();
                    Allocator.getDatabaseIO().enterGrade(_studentLogin, noneHandinPart, nonHandinEarned);
                }

                if (_asgn.hasLabParts()) {
                    double labEarned = _labEarnedField.getNumberValue();
                    Part labPart = (Part) _labBox.getSelectedItem();
                    Allocator.getDatabaseIO().enterGrade(_studentLogin, labPart, labEarned);
                    Allocator.getGradingServices().updateLabGradeFile((LabPart) labPart, labEarned, _studentLogin);
                }
            }
        });

        panel.add(_submitGradeButton);
    }

    private void initNonHandinPanel(JPanel panel)
    {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

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
        _nonHandinBox = new JComboBox();
        _nonHandinBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                Part part = (Part) _nonHandinBox.getSelectedItem();
                if(part != null)
                {
                    double earned = Allocator.getDatabaseIO().getStudentScore(_studentLogin, part);
                    double outOf = part.getPoints();

                    _nonHandinEarnedField.setNumberValue(Allocator.getGeneralUtilities().round(earned, 2));
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
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

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

                    _labEarnedField.setNumberValue(Allocator.getGeneralUtilities().round(earned, 2));
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
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

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
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

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
        _suppressUpdateScores = true;

        _studentLogin = studentLogin;
        _asgn = asgn;

        this.clearComponents();

        //Populate the comboboxes
        for(Part part : asgn.getNonHandinParts())
        {
            _nonHandinBox.addItem(part);
        }
        _submitGradeButton.setEnabled(asgn.hasNonHandinParts() || asgn.hasLabParts());
        
        _nonHandinBox.setEnabled(asgn.hasNonHandinParts());
        _nonHandinEarnedField.setEnabled(asgn.hasNonHandinParts());
        if (!asgn.hasNonHandinParts()) {
            _nonHandinEarnedField.setBackground(Color.LIGHT_GRAY);
        }
        else {
            _nonHandinEarnedField.setBackground(Color.WHITE);
        }

        for(LabPart part : asgn.getLabParts())
        {
            _labBox.addItem(part);
        }

        _labBox.setEnabled(asgn.hasLabParts());
        _labEarnedField.setEnabled(asgn.hasLabParts());
        if (!asgn.hasLabParts()) {
            _labEarnedField.setBackground(Color.LIGHT_GRAY);
        }
        else {
            _labEarnedField.setBackground(Color.WHITE);
        }

        if(asgn.hasHandinPart())
        {
            HandinPart handinPart = asgn.getHandinPart();

            double handinEarned = Allocator.getDatabaseIO().getStudentScore(studentLogin, handinPart);
            double handinOutOf = handinPart.getPoints();

            _handinEarnedField.setNumberValue(Allocator.getGeneralUtilities().round(handinEarned, 2));
            _handinOutOfField.setNumberValue(handinOutOf);
        }

        _suppressUpdateScores = false;
        this.updateScores();
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
        if(_asgn == null || _suppressUpdateScores)
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

        _overallEarnedPointsLabel.setText(Allocator.getGeneralUtilities().round(totalEarned, 2) + "");
        _overallTotalPointsLabel.setText(Allocator.getGeneralUtilities().round(totalOutOf, 2) + "");
    }

    public JFormattedTextField getNonHandinEarnedField()
    {
        return _nonHandinEarnedField;
    }

    public JFormattedTextField getLabEarnedField()
    {
        return _labEarnedField;
    }

    public JComboBox getNonHandinBox()
    {
        return _nonHandinBox;
    }

    public JButton getSubmitButton()
    {
        return _submitGradeButton;
    }
}