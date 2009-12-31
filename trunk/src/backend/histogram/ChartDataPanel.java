/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChartDataPanel.java
 *
 * Created on Sep 25, 2009, 5:24:46 PM
 */
package backend.histogram;

import backend.OldDatabaseOps;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.statistics.Statistics;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import utils.ErrorView;

/**
 *
 * @author psastras
 */
public class ChartDataPanel extends javax.swing.JPanel {

    /** Creates new form ChartDataPanel */
    public ChartDataPanel() {
        initComponents();
    }

    public void updateChartData(String asgnName) {
        try {
            ISqlJetCursor cursor = OldDatabaseOps.getAllData("grades_" + asgnName);
            //int cols = DatabaseIO.getColumnNames("grades_" + asgnName).length;
            double d = OldDatabaseOps.getAssignmentTotal(asgnName);
            List<Double> l = new ArrayList<Double>();
            while (!cursor.eof()) {
                double earned = (cursor.getString(OldDatabaseOps.GRADE_RUBRIC_FIELDS[1]).length() == 0) ? 0.0 : Double.parseDouble(cursor.getString(OldDatabaseOps.GRADE_RUBRIC_FIELDS[1]));
                if (earned / d * 100 != 0) { //ignore zero handins
                    l.add(earned / d * 100);
                }
                cursor.next();
            }
            if(l.size() == 0){
                chartPanel1.loadData(asgnName, new double[]{});
                return;
            }
            double[] data = new double[l.size()];
            for (int i = 0; i < data.length; i++) {
                data[i] = l.get(i);
            }
            Number[] dataAsNumber = new Number[data.length];
            for (int i = 0; i < dataAsNumber.length; i++) {
                dataAsNumber[i] = (Number) data[i];
            }
            nLabel.setText("" + data.length);
            medianLabel.setText("" + Statistics.calculateMedian(l));
            meanLabel.setText("" + Statistics.calculateMean(l));
            stdDevLabel.setText("" + Statistics.getStdDev(dataAsNumber));
            chartPanel1.loadData(asgnName, data);
        } catch (Exception e) {
            new ErrorView(e);
        }
    }

    public BufferedImage getImage(int w, int h) {
        return chartPanel1.getImage(w, h);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chartPanel1 = new backend.histogram.ChartPanel();
        jLabel1 = new javax.swing.JLabel();
        medianLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        nLabel = new javax.swing.JLabel();
        stdDevLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        meanLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(ChartDataPanel.class);
        setBackground(resourceMap.getColor("chartPanel1.background")); // NOI18N
        setName("Form"); // NOI18N

        chartPanel1.setBackground(resourceMap.getColor("chartPanel1.background")); // NOI18N
        chartPanel1.setName("chartPanel1"); // NOI18N

        javax.swing.GroupLayout chartPanel1Layout = new javax.swing.GroupLayout(chartPanel1);
        chartPanel1.setLayout(chartPanel1Layout);
        chartPanel1Layout.setHorizontalGroup(
            chartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 595, Short.MAX_VALUE)
        );
        chartPanel1Layout.setVerticalGroup(
            chartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 254, Short.MAX_VALUE)
        );

        jLabel1.setForeground(resourceMap.getColor("jLabel1.foreground")); // NOI18N
        jLabel1.setText("<html><b>Number of Students</b></html>");
        jLabel1.setName("jLabel1"); // NOI18N

        medianLabel.setForeground(resourceMap.getColor("medianLabel.foreground")); // NOI18N
        medianLabel.setText(resourceMap.getString("medianLabel.text")); // NOI18N
        medianLabel.setName("medianLabel"); // NOI18N

        jLabel5.setForeground(resourceMap.getColor("jLabel5.foreground")); // NOI18N
        jLabel5.setText("<html><b>Median</b></html>");
        jLabel5.setName("jLabel5"); // NOI18N

        nLabel.setForeground(resourceMap.getColor("nLabel.foreground")); // NOI18N
        nLabel.setText(resourceMap.getString("nLabel.text")); // NOI18N
        nLabel.setName("nLabel"); // NOI18N

        stdDevLabel.setForeground(resourceMap.getColor("stdDevLabel.foreground")); // NOI18N
        stdDevLabel.setText(resourceMap.getString("stdDevLabel.text")); // NOI18N
        stdDevLabel.setName("stdDevLabel"); // NOI18N

        jLabel7.setForeground(resourceMap.getColor("jLabel7.foreground")); // NOI18N
        jLabel7.setText("<html><b>Standard Deviation</b></html>");
        jLabel7.setName("jLabel7"); // NOI18N

        meanLabel.setForeground(resourceMap.getColor("meanLabel.foreground")); // NOI18N
        meanLabel.setText(resourceMap.getString("meanLabel.text")); // NOI18N
        meanLabel.setName("meanLabel"); // NOI18N

        jLabel3.setForeground(resourceMap.getColor("jLabel3.foreground")); // NOI18N
        jLabel3.setText("<html><b>Mean / Average</b></html>");
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chartPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(meanLabel)
                    .addComponent(nLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stdDevLabel)
                    .addComponent(medianLabel))
                .addContainerGap(187, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(chartPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel7)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(meanLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(medianLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stdDevLabel)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private backend.histogram.ChartPanel chartPanel1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel meanLabel;
    private javax.swing.JLabel medianLabel;
    private javax.swing.JLabel nLabel;
    private javax.swing.JLabel stdDevLabel;
    // End of variables declaration//GEN-END:variables
}
