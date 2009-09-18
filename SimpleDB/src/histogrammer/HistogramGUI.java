/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HistogramGUI.java
 *
 * Created on Sep 7, 2009, 10:01:49 PM
 */
package histogrammer;

import codesupport.Utils;
import cs015Database.DatabaseInterops;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

/**
 *
 * @author Paul
 */
public class HistogramGUI extends javax.swing.JFrame {

    private ChartDataPanel[] _charts;
    private StudentDataPanel sdp;

    /** Creates new form HistogramGUI */
    public HistogramGUI() {
        initComponents();
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/cs015Database/x-office-drawing.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String[] columnNames = DatabaseInterops.getColumnNames("assignment_dist");
            DefaultListModel model = new DefaultListModel();
            for (int i = 1; i < columnNames.length; i++) {
                model.add(i - 1, columnNames[i]);
            }

            jList1.setModel(model);
            if (columnNames.length > 0) {
                jList1.setSelectedIndex(0);
            }
            _charts = new ChartDataPanel[columnNames.length - 1];
            for (int i = 0; i < _charts.length; i++) {
                _charts[i] = new ChartDataPanel();
            }
            javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
            jPanel2.setLayout(jPanel2Layout);
            ParallelGroup pg = jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
            sdp = new StudentDataPanel();
            sdp.setVisible(true);
            pg.addComponent(sdp, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE);
            for (ChartDataPanel c : _charts) {
                pg.addComponent(c, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE);
            }
            jPanel2Layout.setHorizontalGroup(pg);
            SequentialGroup sg = jPanel2Layout.createSequentialGroup();
            sg.addComponent(sdp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
            for (ChartDataPanel c : _charts) {
                sg.addComponent(c, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
                sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
            }
            jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(sg));
        } catch (SqlJetException e) {
            e.printStackTrace();
        }
        this.setLocationRelativeTo(null);

        try {

            DefaultTableModel m = (DefaultTableModel) table1.getModel();
            m.addColumn("Students");
            ISqlJetCursor cursor = DatabaseInterops.getAllData("studList");
            while (!cursor.eof()) {
                String data[] = {cursor.getString("studLogin")};
                m.insertRow(table1.getRowCount(), data);
                cursor.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setTitle(Utils.getUserLogin() + " - cs015 Grade Report Generator");
        updateCharts();
    }

    private void updateCharts() {
                        
        for (ChartDataPanel c : _charts) {
            c.setVisible(false);
        }
        sdp.setVisible(false);
        Object[] assgns = jList1.getSelectedValues();
        String[] assgnsAsString = new String[assgns.length];
        for (int i = 0; i < assgns.length; i++) {
            assgnsAsString[i] = assgns[i].toString();
        }
        if (table1.getSelectedRow() >= 0 && table1.getSelectedColumn() >= 0 && jCheckBox2.isSelected() == true) {
            sdp.setVisible(true);
            sdp.updateChart((String) table1.getValueAt(table1.getSelectedRow(), table1.getSelectedColumn()), assgnsAsString);
        }
        for (int i = 0; i < assgnsAsString.length; i++) {
            if (jCheckBox1.isSelected()) {
                _charts[i].updateChartData(assgnsAsString[i]);
                _charts[i].setVisible(true);
            }
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        table1 = new cs015Database.Table();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        fromTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        toTextField = new javax.swing.JTextField();
        subjectTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        jTabbedPane1.setFocusable(false);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1215, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1654, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(jPanel2);

        jLabel2.setText("<html><b>Student</b></html>");

        table1.setAutoCreateRowSorter(true);
        table1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        table1.setDoubleBuffered(true);
        table1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                table1MouseClicked(evt);
            }
        });
        table1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                table1KeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(table1);

        jLabel1.setText("<html><b>Assignments (Multi Select)</b></html>");

        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jList1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jList1KeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField1KeyTyped(evt);
            }
        });

        jLabel3.setText("Filter");

        jCheckBox1.setSelected(true);
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jCheckBox2.setSelected(true);
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                        .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                        .addComponent(jCheckBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBox1, 0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBox2, 0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, 0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("1. Select Data To Email", jPanel1);

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        fromTextField.setText("cs015tas@cs.brown.edu");

        jLabel4.setText("From");

        jLabel5.setText("To");

        subjectTextField.setText("[cs015] Grade Report");

        jLabel6.setText("Subject");

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jTextArea1.setRows(5);
        jScrollPane4.setViewportView(jTextArea1);

        jLabel7.setText("Note: The selected data will automatically be attached to the current email.  This currently does not work from the iLab because mail-relay.brown.edu is not accessible from the iLab network.");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 961, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(subjectTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 825, Short.MAX_VALUE)
                            .addComponent(toTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 825, Short.MAX_VALUE)
                            .addComponent(fromTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 825, Short.MAX_VALUE)))
                    .addComponent(jLabel7))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fromTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(toTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(subjectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("2. Email Settings", jPanel4);

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Quit");
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 986, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jList1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jList1KeyReleased
        updateCharts();
    }//GEN-LAST:event_jList1KeyReleased

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        updateCharts();
    }//GEN-LAST:event_jList1MouseClicked

    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped
        table1.applyFilterSorter();
        table1.filter(jTextField1.getText());
        
        table1.setColumnSelectionAllowed(true);
        table1.getSelectionModel().setSelectionInterval(0, 0);
        table1.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
        updateCharts();

    }//GEN-LAST:event_jTextField1KeyTyped

    private void table1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table1MouseClicked
        updateCharts();
    }//GEN-LAST:event_table1MouseClicked

    private void table1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_table1KeyReleased
        updateCharts();
    }//GEN-LAST:event_table1KeyReleased

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        updateCharts();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        if (jCheckBox2.isSelected()) {
            table1.setEnabled(true);
            jTextField1.setEnabled(true);
        } else {
            table1.setEnabled(false);
            jTextField1.setEnabled(false);
        }
        updateCharts();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String[] s = toTextField.getText().split(",");
        for (int i = 0; i < s.length; i++) {
            s[i].trim();
        }
        try {
            List<BufferedImage> bil = new ArrayList<BufferedImage>();
            List<String> sl = new ArrayList<String>();
            List<File> fl = new ArrayList<File>();
            if (sdp.isVisible()) {
                bil.add(sdp.getImage(1000, 250));
                fl.add(new File("histogram_" + bil.size() + ".png"));
                sl.add("histogram_" + bil.size() + ".png");
                ImageIO.write(bil.get(bil.size() - 1), "png", fl.get(fl.size() - 1));
            }
            for (ChartDataPanel c : _charts) {
                if (c.isVisible()) {
                    bil.add(c.getImage(200, 150));
                    fl.add(new File("histogram_" + bil.size() + ".png"));
                    sl.add("histogram_" + bil.size() + ".png");
                    ImageIO.write(bil.get(bil.size() - 1), "png", fl.get(fl.size() - 1));
                }
            }
            String imgString = "";
            int index = 0;
            for (String ss : sl) {
                imgString += "<img src=\"cid:" + ss + "\">";
                if (index++ % 2 == 0) {
                    imgString += "<br>";
                }
            }
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Utils.sendMail(s, new String[0], new String[0], subjectTextField.getText(), "<html><body style=\"margin: 0px;\"><div style=\" padding: 0px 50px 0px 50px; background-color: #fff; font-family: Sans-Serif;\"><p style=\"font-size: 10pt\">" + jTextArea1.getText() + "</p><div style=\"background-color:#fff; font-size: 8pt; text-align: center; color: #999\">" + imgString + "<hr>This is an autogenerated email from cs015 Grade Report.<br>Sent on " + sdf.format(cal.getTime()) + ".</div></div></body></html>", sl.toArray(new String[0]));
            
            }
            catch(Exception e) {
            }
            for (File f : fl) {
                f.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
}//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new HistogramGUI().setVisible(true);
            }
        });
    }

    public void paintComponent(Graphics g) {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField fromTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField subjectTextField;
    private cs015Database.Table table1;
    private javax.swing.JTextField toTextField;
    // End of variables declaration//GEN-END:variables
}
