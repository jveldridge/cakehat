package gradesystem.views.backend.stathist;

import gradesystem.config.Assignment;
import gradesystem.config.Part;
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
import gradesystem.Allocator;
import gradesystem.views.shared.ErrorView;
import java.util.HashMap;

/**
 *
 * @author Paul
 * @author jeldridg
 */
public class StudentChartPanel extends javax.swing.JPanel {

    /** Creates new form StudentDataPanel */
    public StudentChartPanel() {
        this.setBackground(Color.white);
        initComponents();
    }
    private JFreeChart _chart;

    public void updateChart(String studName, Assignment[] assignments) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][assignments.length];
        double[][] avgData = new double[2][assignments.length];
        
        for (int i = 0; i < assignments.length; i++) {
            data[0][i] = i;
            double studentScore = 0;
            for (Part p : assignments[i].getParts()) {

                double partScore = 0;
                try {
                    Double rawScore = Allocator.getDatabaseIO().getStudentScore(studName, p);
                    partScore = (rawScore == null ? 0 : rawScore);
                } catch (SQLException ex) {
                    new ErrorView(ex, "Could not read the score for student " + studName + " " +
                                      "on part " + p + ".  FOR THESE CHARTS AND STATISTICS, THE " +
                                      "SCORE WILL BE TREATED AS A 0.");
                }

                studentScore += partScore;
            }

            data[1][i] = studentScore / assignments[i].getTotalPoints() * 100;
            
            Vector<Double> scores = new Vector<Double>();
            Map<String, Double> scoreMap;
            try {
                scoreMap = Allocator.getDatabaseIO().getAssignmentScores(assignments[i], Allocator.getDatabaseIO().getEnabledStudents().keySet());
            } catch (SQLException ex) {
                new ErrorView(ex, "Could not get scores for assignment " + assignments[i] + ".");
                scoreMap = new HashMap<String, Double>();
            }

            for (String student : scoreMap.keySet()) {
                scores.add(scoreMap.get(student));
            }
            
            avgData[0][i] = i;
            avgData[1][i] = (Statistics.calculateMean(scores) / assignments[i].getTotalPoints()) * 100;
        }
        
        dataset.addSeries(studName + "'s Scores", data);
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
        _chart = new JFreeChart(studName + "'s Grade History", new Font("Sans-Serif", Font.BOLD, 14), plot, true);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(637, 309));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 637, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 309, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
