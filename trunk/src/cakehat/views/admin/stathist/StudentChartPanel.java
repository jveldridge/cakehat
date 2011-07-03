package cakehat.views.admin.stathist;

import cakehat.config.Assignment;
import cakehat.config.Part;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Vector;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.DefaultXYDataset;
import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.views.shared.ErrorView;
import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 *
 * @author psastras
 * @author jeldridg
 * @author jak2
 */
public class StudentChartPanel extends JPanel {

    private JFreeChart _chart;

    public StudentChartPanel() {
        this.setBackground(Color.white);
        this.setPreferredSize(new Dimension(637, 309));
    }

    public void updateChart(Student student, Assignment[] assignments) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][assignments.length];
        double[][] avgData = new double[2][assignments.length];

        for (int i = 0; i < assignments.length; i++) {
            data[0][i] = i;
            double studentScore = 0;
            Assignment asgn = assignments[i];
            Group studentsGroup;
            try {
                studentsGroup = Allocator.getDatabase().getStudentsGroup(asgn, student);
            } catch (SQLException ex) {
                new ErrorView("Could read group for student " + student + 
                                 " for assignment " + asgn + " from the database.");
                continue;
            }
            if (studentsGroup == null) {
                continue;
            }
            for (Part p : asgn.getParts()) {

                double partScore = 0;
                try {
                    Double rawScore = Allocator.getDatabase().getGroupScore(studentsGroup, p);
                    partScore = (rawScore == null ? 0 : rawScore);
                } catch (SQLException ex) {
                    new ErrorView(ex, "Could not read the score for student " + student + " " +
                                      "on part " + p + ".  FOR THESE CHARTS AND STATISTICS, THE " +
                                      "SCORE WILL BE TREATED AS A 0.");
                }

                studentScore += partScore;
            }

            data[1][i] = studentScore / assignments[i].getTotalPoints() * 100;

            Vector<Double> scores = new Vector<Double>();
            Map<Group, Double> scoreMap;
            try {
                scoreMap = Allocator.getDatabase().getAssignmentScoresForGroups(asgn, Allocator.getDatabase().getGroupsForAssignment(asgn));
            } catch (SQLException ex) {
                new ErrorView(ex, "Could not get scores for assignment " + assignments[i] + ".");
                scoreMap = new HashMap<Group, Double>();
            }

            for (Group group : scoreMap.keySet()) {
                scores.add(scoreMap.get(group));
            }

            avgData[0][i] = i;
            avgData[1][i] = (Statistics.calculateMean(scores) / assignments[i].getTotalPoints()) * 100;
        }

        dataset.addSeries(student.getLogin() + "'s Scores", data);
        dataset.addSeries("Class Average", avgData);
        ValueAxis yAxis = new NumberAxis("Score (%)");
        yAxis.setRange(0.0, 110.0);

        String[] asgnNames = new String[assignments.length];
        for (int i = 0; i < assignments.length; i++) {
            asgnNames[i] = assignments[i].getName();
        }

        SymbolAxis sa = new SymbolAxis("Assignment Name", asgnNames);
        sa.setAutoRange(true);
        ValueAxis xAxis = sa;
        XYItemRenderer renderer = new XYLineAndShapeRenderer();
        DecimalFormat decimalformat1 = new DecimalFormat("##,###");
        renderer.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator("{2}", decimalformat1, decimalformat1));
        renderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);
        renderer.setSeriesItemLabelGenerator(1, new StandardXYItemLabelGenerator("{2}", decimalformat1, decimalformat1));
        renderer.setSeriesItemLabelsVisible(1, Boolean.TRUE);
        renderer.setBaseItemLabelsVisible(true);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        _chart = new JFreeChart(student.getLogin() + "'s Grade History", new Font("Sans-Serif", Font.BOLD, 14), plot, true);
        _chart.setBackgroundPaint(Color.white);
        this.repaint();
    }

    public BufferedImage getImage(int w, int h) {
        return _chart.createBufferedImage(w, h);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (_chart != null) {
            _chart.draw((Graphics2D) g, new Rectangle2D.Double(0.0, 0.0, (double) this.getWidth(), (double) this.getHeight()));
        }
    }
}