package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.config.Part;
import cakehat.config.TA;
import cakehat.database.CakeHatDBIOException;
import cakehat.database.Group;
import cakehat.config.handin.DistributablePart;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel part of the {@link AdminView} that shows the grading status of a single
 * selected Part when no students are selected.  The SinglePartPanel shows the
 * number of students who have grades out of the total number of enabled students.
 * If the selected Part is a DistributablePart, it also shows the number of students
 * who have handins and the number of students who have been distributed.
 *
 * @author jeldridg
 */
public class SinglePartPanel extends JPanel {

    private TextAndValueLabel _partLabel;
    private TextAndValueLabel _numHandinsLabel;
    private TextAndValueLabel _numDistributedLabel;
    private TextAndValueLabel _numSubmittedGradesLabel;
    private TextAndValueLabel _numNonZeroRubricsLabel;
    
    private JButton _refreshButton;
    private Part _part;

    public SinglePartPanel() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));

        _partLabel = new TextAndValueLabel("", "Selected Part", false);

        _numHandinsLabel = new TextAndValueLabel("Number of", "Handins", false);
        _numDistributedLabel = new TextAndValueLabel("Number of", "Distributed", true);
        _numSubmittedGradesLabel = new TextAndValueLabel("Number of", "with Submitted Grades", true);
        
        _numNonZeroRubricsLabel = new TextAndValueLabel("Number of", "with Nonzero Rubric Scores", true);
        _numNonZeroRubricsLabel.setToolTipText("<html>Calculating this value can be time-consuming.<br/>" +
                                            "Thus, it will not be performed unless the \"Refresh\" " +
                                            "button is clicked.</html>");

        _refreshButton = new JButton("Refresh Status Information");
        _refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updatePart(_part);
                if (_part instanceof DistributablePart) {
                    updatePart(_part);
                    updateNonzeroRubricScoreCount((DistributablePart) _part);
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        
        panel.add(_partLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(_numHandinsLabel);
        panel.add(_numDistributedLabel);
        panel.add(_numSubmittedGradesLabel);
        panel.add(_numNonZeroRubricsLabel);
        panel.add(_refreshButton);
        this.add(panel, BorderLayout.WEST);
    }

    public void updatePart(Part p) {
        _part = p;
        
        boolean hasGroups = p.getAssignment().hasGroups();
         _partLabel.setHasGroups(hasGroups);
         _numSubmittedGradesLabel.setHasGroups(hasGroups);
        
        _partLabel.updateValue(p.getAssignment().getName() + " - " + p.getName());

        try {
            Collection<Group> groupsForAssignment = Allocator.getDatabaseIO().getGroupsForAssignment(p.getAssignment());
            _numSubmittedGradesLabel.updateValue(Allocator.getDatabaseIO().getPartScoresForGroups(p, groupsForAssignment).size());
        } catch (SQLException ex) {
            _numSubmittedGradesLabel.setUnknownValue();
            new ErrorView(ex, "Could not determine the number of groups with grades for" +
                              "part " + p.getName() + " of assignment " + p.getAssignment().getName() + ".");
        }

        if (p instanceof DistributablePart) {
            _numHandinsLabel.setHasGroups(hasGroups);
            _numDistributedLabel.setHasGroups(hasGroups);
            _numNonZeroRubricsLabel.setHasGroups(hasGroups);
            
            DistributablePart dp = (DistributablePart) p;

            try {
                _numHandinsLabel.updateValue(dp.getHandin().getHandinNames().size());
            } catch (IOException e) {
                _numHandinsLabel.setUnknownValue();
                new ErrorView(e, "Could not determine the number of handins for " +
                        "part " + p.getName() + " of assignment " + p.getAssignment().getName() + ".");
            }

            try {
                _numDistributedLabel.updateValue(Allocator.getDatabaseIO().getAllAssignedGroups(dp).size());
            } catch (SQLException ex) {
                _numDistributedLabel.setUnknownValue();
                new ErrorView(ex, "Could not determine the number of distributed groups for" +
                              "part " + p.getName() + " of assignment " + p.getAssignment().getName() + ".");
            } catch (CakeHatDBIOException ex) {
                _numDistributedLabel.setUnknownValue();
                new ErrorView(ex, "Could not determine the number of distributed groups for" +
                              "part " + p.getName() + " of assignment " + p.getAssignment().getName() + ".");
            }

            _numNonZeroRubricsLabel.setUnknownValue();

            _numHandinsLabel.setVisible(true);
            _numDistributedLabel.setVisible(true);
            _numNonZeroRubricsLabel.setVisible(true);
        }
        else {
            _numHandinsLabel.setVisible(false);
            _numDistributedLabel.setVisible(false);
            _numNonZeroRubricsLabel.setVisible(false);
        }
    }

    private void updateNonzeroRubricScoreCount(DistributablePart dp) {
        List<Group> distributedGroups = new ArrayList<Group>();
        try {
            Map<TA, Collection<Group>> dist = Allocator.getDatabaseIO().getDistribution(dp);
            for (TA ta : dist.keySet()) {
                distributedGroups.addAll(dist.get(ta));
            }

            int numNonZero = 0;
            Map<Group, Double> rubricScores = Allocator.getRubricManager().getPartScores(dp, distributedGroups);
            for (Group group : rubricScores.keySet()) {
                if (rubricScores.get(group) != 0) {
                    numNonZero++;
                }
            }
            _numNonZeroRubricsLabel.updateValue(numNonZero);
        } catch (SQLException ex) {
            _numNonZeroRubricsLabel.setUnknownValue();
            new ErrorView(ex, "Could not determine groups with rubrics.");
        } catch (CakeHatDBIOException ex) {
            _numNonZeroRubricsLabel.setUnknownValue();
            new ErrorView(ex, "Could not determine groups with rubrics.");
        }
    }

    private class TextAndValueLabel extends JLabel {

        private String _preText;
        private String _unitText = "";
        private String _postText;
        private boolean _displayUnitText;

        public TextAndValueLabel(String preText, String postText, boolean displayUnitText) {
            _preText = preText;
            _postText = postText;
            _displayUnitText = displayUnitText;
        }

        public void setHasGroups(boolean hasGroups) {
            if (_displayUnitText) {
                _unitText = (hasGroups ? "Groups" : "Students");
            }
        }

        public void updateValue(int value) {
            this.setText("<html><b>" + _preText + " " + _unitText + " " +
                         _postText + ":</b> " + value + "</html>");
        }

        public void updateValue(String value) {
            this.setText("<html><b>" + _preText + " " + _unitText + " " +
                         _postText + ":</b> " + value + "</html>");
        }

        public void setUnknownValue() {
            this.setText("<html><b>" + _preText + " " + _unitText + " " +
                         _postText + ":</b> ???</html>");
        }
    }
}