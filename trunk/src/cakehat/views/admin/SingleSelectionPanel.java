package cakehat.views.admin;

import cakehat.config.Assignment;
import cakehat.config.Part;
import cakehat.rubric.RubricException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.SQLException;
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
import cakehat.Allocator;
import support.ui.GenericJComboBox;
import cakehat.database.Group;
import cakehat.config.handin.DistributablePart;
import cakehat.views.shared.ErrorView;
import java.util.ArrayList;

/**
 * Single selection panel of {@link AdminView}.
 *
 * @author jak2
 */
class SingleSelectionPanel extends JPanel
{
    //Dimensions
    private static final Dimension PANEL_SIZE = AdminView.MULTI_PANEL_CARD_SIZE;

    //Student and assignment this is displaying
    private String _studentLogin;
    private Group _group;
    private Assignment _asgn;

    //GUI components
    private GenericJComboBox<Part> _nonHandinBox;
    private ScoreField _nonHandinEarnedField, _nonHandinOutOfField,
                       _handinEarnedField, _handinOutOfField;
    private JLabel _nonHandinScoreLabel, _handinScoreLabel,
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
    HANDIN_PANEL_SIZE = new Dimension(PANEL_SIZE.width - 10, 110),
    OVERALL_PANEL_SIZE = new Dimension(PANEL_SIZE.width - 10, 130),
    UPDATE_PANEL_SIZE = new Dimension(PANEL_SIZE.width - 10,
                                      PANEL_SIZE.height -
                                      NON_HANDIN_PANEL_SIZE.height -
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

        //Update
        JPanel updatePanel = new JPanel();
        updatePanel.setPreferredSize(UPDATE_PANEL_SIZE);
        this.initUpdatePanel(updatePanel);
        this.add(updatePanel);

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
    }

    private void initUpdatePanel(JPanel panel)
    {
        _submitGradeButton = new JButton("Submit Grade");
        _submitGradeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae) {
                if (_asgn.hasNonHandinParts() || _asgn.hasLabParts()) {
                    double earned = _nonHandinEarnedField.getNumberValue();
                    Part part = _nonHandinBox.getSelectedItem();
                    try {
                        Allocator.getDatabase().enterGrade(_group, part, earned);
                    } catch (SQLException ex) {
                        new ErrorView(ex, "Saving the grade for student " + _studentLogin + " " +
                                          "on part " + part + " of assignment " +
                                          _asgn + " failed.");
                    }
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
        JLabel titleLabel = new JLabel("<html><b>Lab & Non-Handin Parts</b><html>");
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
        _nonHandinBox = new GenericJComboBox<Part>();
        _nonHandinBox.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent ae)
            {
                updateNonHandinInfo();
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

    private void updateNonHandinInfo()
    {
        Part part = _nonHandinBox.getSelectedItem();
        if(part != null)
        {
            Double earned = null;
            try
            {
                earned = Allocator.getDatabase().getGroupScore(_group, part);
            }
            catch (SQLException ex)
            {
                new ErrorView(ex, "Could not read score for student " + _studentLogin + " on " +
                                  "part " + part + " from the database.");
                _nonHandinEarnedField.setUnknownScoreValue();
            }

            double outOf = part.getPoints();

            if (earned != null)
            {
                _nonHandinEarnedField.setNumberValue(Allocator.getGeneralUtilities().round(earned, 2));
            }
            else
            {
                _nonHandinEarnedField.setNoScoreValue();
            }
            _nonHandinOutOfField.setNumberValue(outOf);
        }
    }

    private void clearComponents()
    {
        _nonHandinOutOfField.setText("");
        _nonHandinEarnedField.setText("");
        _nonHandinScoreLabel.setText("0");

        _handinOutOfField.setText("");
        _handinEarnedField.setText("");
        _handinScoreLabel.setText("0");
    }

    public void updateView(String studentLogin, Group group, Assignment asgn)
    {
        _suppressUpdateScores = true;

        boolean asgnChanged = (_asgn != asgn);
        
        _studentLogin = studentLogin;
        _group = group;
        _asgn = asgn;

        this.clearComponents();

        //Populate the combo box if the assignment has changed
        if(asgnChanged)
        {
            ArrayList<Part> partsToAdd = new ArrayList<Part>();
            partsToAdd.addAll(asgn.getNonHandinParts());
            partsToAdd.addAll(asgn.getLabParts());
            _nonHandinBox.setItems(partsToAdd);
        }
        this.updateNonHandinInfo();

        _submitGradeButton.setEnabled(asgn.hasNonHandinParts() || asgn.hasLabParts());
        
        _nonHandinBox.setEnabled(asgn.hasNonHandinParts() || asgn.hasLabParts());
        _nonHandinEarnedField.setEnabled(asgn.hasNonHandinParts() || asgn.hasLabParts());
        if (!asgn.hasNonHandinParts() && !asgn.hasLabParts()) {
            _nonHandinEarnedField.setBackground(Color.LIGHT_GRAY);
        }
        else {
            _nonHandinEarnedField.setBackground(Color.WHITE);
        }

        if(asgn.hasHandin())
        {
            boolean hasRubrics = false;
            Double handinEarned = null;
            try {
                for (DistributablePart dp : asgn.getDistributableParts()) {
                    Double dpScore = Allocator.getDatabase().getGroupScore(group, dp);
                    if (handinEarned == null) {
                        handinEarned = dpScore;
                    }
                    else {
                        handinEarned += (dpScore == null ? 0 : dpScore);
                    }
                    
                    if (Allocator.getRubricManager().hasRubric(dp, group)) {
                        hasRubrics = true;
                    }
                }
            } catch (SQLException ex) {
                new ErrorView(ex, "Could not read scores for student " + studentLogin + " on " +
                                  "distributable parts for assignment " + asgn + " from the database.");
                _nonHandinEarnedField.setUnknownScoreValue();
            }

            //if rubrics haven't been created, the handin time won't have been stored in the database,
            //so trying to calculate the handin penalty would fail
            if (hasRubrics) {
                try {
                    double handinPenalty = Allocator.getRubricManager().getHandinPenaltyOrBonus(asgn.getHandin(), group);
                    if (handinEarned != null) {
                        handinEarned += handinPenalty;
                    }
                } catch (RubricException ex) {
                    new ErrorView(ex, "Could not determine early/late handin penalty/bonus for student " +
                                      studentLogin + "'s group on assignment " + asgn + ".");
                    _handinEarnedField.setUnknownScoreValue();
                }
            }

            if (handinEarned != null) {
                _handinEarnedField.setNumberValue(Allocator.getGeneralUtilities().round(handinEarned, 2));
            }
            else {
                _handinEarnedField.setNoScoreValue();
            }

            int handinOutOf = 0;
            for (DistributablePart dp : asgn.getDistributableParts()) {
                handinOutOf += dp.getPoints();
            }

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
                public void focusGained(FocusEvent fe) {
                    ScoreField.this.selectAll();
                }

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

        /**
         * Has the ScoreField show a value that indicates the student does
         * not have a score for the corresponding assignment.  Note that after
         * this method is called, getNumberValue() will return 0.
         */
        public void setNoScoreValue() {
            this.setText("no score");
        }

        /**
         * Has the ScoreField show a value that indicates the student's score
         * for the corresponding assignment is unknown.  Note that after this
         * method is called, getNumberValue() will return 0.
         */
        public void setUnknownScoreValue() {
            this.setText("??");
        }

        public double getNumberValue()
        {
            double value = 0;
            try
            {
                value = Double.parseDouble(this.getText());
            }
            catch(NumberFormatException e) { }

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
        if(_asgn.hasNonHandinParts())
        {
            Part selectedPart = _nonHandinBox.getSelectedItem();
            totalEarned += nonHandinEarned;
            totalOutOf += nonHandinOutOf;

            for(Part part : _asgn.getNonHandinParts())
            {
                if(part != selectedPart)
                {
                    Double partScore = null;
                    try {
                        partScore = Allocator.getDatabase().getGroupScore(_group, part);
                    } catch (SQLException ex) {
                        new ErrorView(ex, "Could not read score for student " + _studentLogin + " on " +
                                          "part " + part + " from the database.  A SCORE OF 0 WILL BE " +
                                          "ASSUMED for displaying the student's overall assignment score.");
                    }

                    totalEarned += (partScore == null ? 0 : partScore);
                    totalOutOf += part.getPoints();
                }
            }
        }
        
        //Handin
        if(_asgn.hasHandin())
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

    public JComboBox getNonHandinBox()
    {
        return _nonHandinBox;
    }

    public JButton getSubmitButton()
    {
        return _submitGradeButton;
    }

    public void selectPart(Part part)
    {
        _nonHandinBox.setGenericSelectedItem(part);
    }
}