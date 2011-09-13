package cakehat.views.admin.stathist;

import cakehat.config.Assignment;
import cakehat.config.Part;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jfree.data.statistics.Statistics;
import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author psastras
 * @author jeldridg
 * @author jak2
 */
public class AssignmentChartPanel extends JPanel {

    private HistogramPanel _chartPanel;
    private JLabel _meanLabel;
    private JLabel _medianLabel;
    private JLabel _numStudentsLabel;
    private JLabel _stdDevLabel;

    public AssignmentChartPanel() {
        initComponents();
    }

    public void initComponents() {
        this.setLayout(new BorderLayout(0, 0));
        this.setBackground(Color.WHITE);

        //Chart
        _chartPanel = new HistogramPanel();
        _chartPanel.setPreferredSize(new Dimension(587, 262));
        this.add(_chartPanel, BorderLayout.NORTH);

        //Padding to the right and left of statistics
        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setBackground(Color.WHITE);
        this.add(lowerPanel, BorderLayout.CENTER);
        lowerPanel.add(Box.createHorizontalStrut(50), BorderLayout.WEST);
        lowerPanel.add(Box.createHorizontalStrut(50), BorderLayout.EAST);

        //Statistics
        JPanel statsPanel = new JPanel(new GridLayout(2, 4));
        statsPanel.setBackground(Color.WHITE);
        lowerPanel.add(statsPanel, BorderLayout.CENTER);

        statsPanel.add(new JLabel("<html><b>Number of Students</b></html>"));
        _numStudentsLabel = new JLabel("0");
        statsPanel.add(_numStudentsLabel);

        statsPanel.add(new JLabel("<html><b>Median</b></html>"));
        _medianLabel = new JLabel("0");
        statsPanel.add(_medianLabel);

        statsPanel.add(new JLabel("<html><b>Mean / Average</b></html>"));
        _meanLabel = new JLabel("0");
        statsPanel.add(_meanLabel);

        statsPanel.add(new JLabel("<html><b>Standard Deviation</b></html>"));
        _stdDevLabel = new JLabel("0");
        statsPanel.add(_stdDevLabel);

        //Padding below statistics
        this.add(Box.createVerticalStrut(25), BorderLayout.SOUTH);
    }

    public void updateChartData(Assignment asgn, Collection<Group> groups) {
        Map<Group, Double> scoreMap;
        try {
            scoreMap = Allocator.getDataServices().getScores(asgn, groups);
        } catch (ServicesException ex) {
            new ErrorView(ex, "Could not retrieve scores on assignment " + asgn + " " +
                              "for groups " + groups + ".");
            scoreMap = new HashMap<Group, Double>();
        }

        double outOf = asgn.getTotalPoints();

        List<Double> scores = new ArrayList<Double>();
        for (Group group : scoreMap.keySet()) {
            double earned = scoreMap.get(group);
            scores.add(earned / outOf * 100);
        }

        this.updateChartData(asgn.getName(), scores);
    }

    public void updateChartData(Part part, Collection<Group> groups) {
        Map<Group, Double> scoreMap;
        try {
            scoreMap = Allocator.getDataServices().getScores(part, groups);
        } catch (ServicesException ex) {
            new ErrorView(ex, "Could not get scores on part " + part + " " +
                              "for groups " + groups + ".");
            scoreMap = new HashMap<Group, Double>();
        }

        double outOf = part.getPoints();

        List<Double> scores = new ArrayList<Double>();
        for (Group group : scoreMap.keySet()) {
            double earned = scoreMap.get(group);
            scores.add(earned / outOf * 100);
        }

        this.updateChartData(part.getAssignment().getName() + ": " + part.getName(), scores);
    }

    private void updateChartData(String asgnName, List<Double> scores) {
        if(scores.size() == 0){
            _chartPanel.loadData(asgnName, new double[]{});
            return;
        }

        double[] data = new double[scores.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = scores.get(i);
        }
        Number[] dataAsNumber = new Number[data.length];
        for (int i = 0; i < dataAsNumber.length; i++) {
            dataAsNumber[i] = (Number) data[i];
        }

        _numStudentsLabel.setText("" + data.length);
        _medianLabel.setText(Allocator.getGeneralUtilities().doubleToString(Statistics.calculateMedian(scores)));
        _meanLabel.setText(Allocator.getGeneralUtilities().doubleToString(Statistics.calculateMean(scores)));
        _stdDevLabel.setText(Allocator.getGeneralUtilities().doubleToString(Statistics.getStdDev(dataAsNumber)));
        _chartPanel.loadData(asgnName, data);
    }

    public BufferedImage getImage(int w, int h) {
        return _chartPanel.getImage(w, h);
    }
}