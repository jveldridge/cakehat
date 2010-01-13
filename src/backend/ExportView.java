/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportView.java
 *
 * Created on Dec 17, 2009, 2:45:39 PM
 */
package backend;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import utils.Allocator;

/**
 *
 * @author psastras
 */
public class ExportView extends javax.swing.JFrame {

    /** Creates new form ExportView */
    public ExportView() {
        initComponents();
        this.setLocationRelativeTo(null);
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/edit-redo.png")));
        } catch (Exception e) {
        }
    //this.setVisible(true);
    }
    private Thread _exportdb;

    public synchronized void updateStatus(String message, int timeout, Color c) {
        lbl_status.setText(message);
        if (c == null) {
            c = java.awt.SystemColor.controlText;
        }
        lbl_status.setForeground(c);
        Timer t = new Timer();
        class NewTask extends TimerTask {

            @Override
            public void run() {
                lbl_status.setText("Ready");
                lbl_status.setForeground(java.awt.SystemColor.controlText);
                this.cancel();
            }
        }
        if (timeout > 0) {
            t.schedule(new NewTask(), timeout);
        }
    }

    public void start() {

        final JFileChooser fc = new JFileChooser(new File("~/" + Allocator.getGeneralUtilities().getUserLogin()));
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                return (f.getName().endsWith(".csv") || f.isDirectory());
            }

            @Override
            public String getDescription() {
                return "Comma separated values (.csv)";
            }
        });
        fc.showSaveDialog(this);
        File f = fc.getSelectedFile();
        if (f == null) {
            return;
        }
        if (!f.getName().endsWith(".csv")) { //append .csv extension if necessary
            f = new File(f.getAbsolutePath() + ".csv");
        }
        if (f.exists()) { //Add confirmation dialog
            Object[] options = {"Confirm", "Cancel"};
            if (JOptionPane.showOptionDialog(this, "Are you sure you want to overwrite this file?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]) != 0) {
                start();
            }
        }

        //First snoop everyones names....this is gonna take ages
        OldDatabaseOps.open();
        final String[] studlogins = OldDatabaseOps.getStudentNames();

        final File filetowrite = f;
        setVisible(true);
        _exportdb = new Thread(new Runnable() {

            public void run() {
                try {
                    class StudData implements Comparable {

                        private String _fname,  _lname,  _login;

                        public StudData(String first, String last, String login) {
                            _fname = first;
                            _lname = last;
                            _login = login;
                        }

                        public int compareTo(Object o) {
                            StudData s1 = (StudData) o;
                            return -(s1._lname).compareToIgnoreCase(_lname);
                        }
                    }

                    StudData[] logins_names = new StudData[studlogins.length];  //Last, first, login

                    FileWriter fw = new FileWriter(filetowrite);
                    BufferedWriter bw = new BufferedWriter(fw);
                    float progress = 0, inc = 100.0f / (float) (studlogins.length);
                    for (int i = 0; i < studlogins.length; i++) { // change max to logins_names.length

                        //if (logins_names[i] == null) {
                        String fullName = Allocator.getGeneralUtilities().getUserName(studlogins[i]);
                        int splitpos = fullName.lastIndexOf(" ");
                        String first = fullName.substring(0, splitpos);
                        String last = fullName.substring(splitpos + 1, fullName.length());
                        updateStatus("Parsing student logins and names (" + studlogins[i] + " - " + first + " " + last + ")", 0, null);
                        logins_names[i] = new StudData(first, last, studlogins[i]);
                        jProgressBar1.setValue((int) (progress += inc));
                    }
                    updateStatus("Sorting", 0, null);
                    Arrays.sort(logins_names);
                    jProgressBar1.setValue(100);
                    updateStatus("Done", 0, null);
                    jProgressBar1.setIndeterminate(true);
                    updateStatus("Writing database to file", 0, null);


                    StringBuilder sb = new StringBuilder();
                    String[] assignmentNames = OldDatabaseOps.getAssignmentNames();
                    int[] totals = new int[assignmentNames.length];
                    for (int i = 0; i < totals.length; i++) {
                        totals[i] = OldDatabaseOps.getAssignmentTotal(assignmentNames[i]);
                    }

                    sb.append("Last,First,Login,");
                    for (int i = 0; i < assignmentNames.length; i++) {
                        sb.append(assignmentNames[i] + ",Status");
                        if (i < assignmentNames.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append("\n");
                    sb.append("Total Points,,,");
                    for (int i = 0; i < totals.length; i++) {
                        sb.append(totals[i] + ",");
                        if (i < totals.length - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append("\n\n");
                    for (int j = 0; j < logins_names.length; j++) {
                        sb.append(logins_names[j]._lname + "," + logins_names[j]._fname + "," + logins_names[j]._login + ",");
                        for (int i = 0; i < assignmentNames.length; i++) {
                            sb.append(OldDatabaseOps.getStudentEarnedScore(assignmentNames[i], logins_names[j]._login) + ",");
                            try {
                                sb.append(Allocator.getProject(assignmentNames[i]).getTimeStatus(logins_names[j]._login, Allocator.getCourseInfo().getMinutesOfLeniency()));
                            } catch (Exception e) {
                            }
                            if (i < assignmentNames.length - 1) {
                                sb.append(",");
                            }
                        }
                        sb.append("\n");
                    }
                    bw.write(sb.toString());
                    bw.close();
                    jProgressBar1.setIndeterminate(true);
                    JOptionPane.showMessageDialog(null, "File written to " + filetowrite.getName() + ".", "File Written", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error while writing to file.", "Error", JOptionPane.ERROR_MESSAGE);
                    jProgressBar1.setIndeterminate(false);
                    e.printStackTrace();
                }
            }
        });
        _exportdb.start();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();
        lbl_status = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(ExportView.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jProgressBar1.setName("jProgressBar1"); // NOI18N
        jProgressBar1.setStringPainted(true);

        jLabel1.setFont(resourceMap.getFont("jButton1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        lbl_status.setFont(resourceMap.getFont("jButton1.font")); // NOI18N
        lbl_status.setText(resourceMap.getString("lbl_status.text")); // NOI18N
        lbl_status.setName("lbl_status"); // NOI18N

        jButton1.setFont(resourceMap.getFont("jButton1.font")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lbl_status)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 370, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lbl_status)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (_exportdb != null) {
            try {
                _exportdb.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ExportView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel lbl_status;
    // End of variables declaration//GEN-END:variables
}
