/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StudentDataPanel.java
 *
 * Created on Sep 9, 2009, 10:35:44 AM
 */
package histogrammer;

import cs015Database.DatabaseInterops;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

/**
 *
 * @author Paul
 */
public class StudentDataPanel extends javax.swing.JPanel {

    /** Creates new form StudentDataPanel */
    public StudentDataPanel() {
        this.setBackground(Color.white);
        initComponents();
    }
    private JFreeChart _chart;

    public void updateChart(String studName, String[] assignments) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][assignments.length];
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i].compareTo("") != 0 && assignments[i].compareTo("None") != 0) {
                try {
                    ISqlJetCursor cursor = DatabaseInterops.getAllData("grades_" + assignments[i]);
                    data[0][i] = i;
                    while (!cursor.eof()) {
                        if (cursor.getString("studLogins").compareToIgnoreCase(studName) == 0) {
                             double earned = (cursor.getString("Earned").length() == 0) ? 0.0 : Double.parseDouble(cursor.getString("Earned"));
                            data[1][i] = earned / DatabaseInterops.getAssignmentTotal(assignments[i]) * 100;
                            break;
                        }
                        cursor.next();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
            }
        }

        dataset.addSeries(studName + "'s Scores", data);
//        dataset.addSeries("Class Average", data);
        ValueAxis yAxis = new NumberAxis("Score (%)");
        yAxis.setRange(0.0, 110.0);
        SymbolAxis sa = new SymbolAxis("Assignment Name", assignments);
        sa.setAutoRange(true);
        ValueAxis xAxis = sa;
        XYItemRenderer renderer = new XYLineAndShapeRenderer();
        DecimalFormat decimalformat1 = new DecimalFormat("##,###");
        renderer.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator("{2}", decimalformat1, decimalformat1));
        renderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);
        renderer.setBaseItemLabelsVisible(true);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        _chart = new JFreeChart(studName + "'s Grade History", new Font("Sans-Serif", Font.BOLD, 14), plot, false);
        _chart.setBackgroundPaint(Color.white);
        this.repaint();

    }

    public BufferedImage getImage(int w, int h) {
       return  _chart.createBufferedImage(w, h);
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
