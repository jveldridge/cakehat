/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StartupDialog.java
 *
 * Created on Sep 6, 2009, 1:52:56 PM
 */
package cs015Database;

import assignment_distributor.AssignmentDistributorGUI;
import database_editor.DatabaseGUI;
import designQGrader.DesignQGraderGUI;
import emailer.EmailGUI;
import histogrammer.HistogramGUI;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import nl.captcha.Captcha;

/**
 *
 * @author Paul
 */
public class StartupDialog extends javax.swing.JFrame {

    private Captcha _captcha;

    /** Creates new form StartupDialog */
    public StartupDialog() {
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/cs015Database/system-file-manager.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        initComponents();
        this.setLocationRelativeTo(null);

        try {

            FileInputStream fstream = new FileInputStream(getClass().getResource("/cs015Database/config.txt").getFile());

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (!strLine.startsWith("//")) {
                }
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
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

        warningDialog = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        imagePanel1 = new cs015Database.ImagePanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        databaseEditorButton = new javax.swing.JButton();
        gradeDistributorButton = new javax.swing.JButton();
        histogramButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        gradeDistributorButton1 = new javax.swing.JButton();
        emailButtonActionPerformed = new javax.swing.JButton();
        gradeDistributorButton2 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        warningDialog.setTitle("Database Regenerate Confirmation");
        warningDialog.setMinimumSize(new java.awt.Dimension(600, 210));
        warningDialog.setModal(true);
        warningDialog.setResizable(false);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database_editor/dialog-warning.png"))); // NOI18N
        jLabel2.setText("<html><b>Are you sure you want to reset the database?</b><br />Doing this will remove all data from the database and reset it according to the conf file.</html>");
        jLabel2.setIconTextGap(20);

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField1KeyTyped(evt);
            }
        });

        jLabel3.setText("<html><b>Enter the following captcha to proceed:</b></html>");

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Confirm");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout imagePanel1Layout = new javax.swing.GroupLayout(imagePanel1);
        imagePanel1.setLayout(imagePanel1Layout);
        imagePanel1Layout.setHorizontalGroup(
            imagePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 185, Short.MAX_VALUE)
        );
        imagePanel1Layout.setVerticalGroup(
            imagePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 41, Short.MAX_VALUE)
        );

        jLabel4.setForeground(new java.awt.Color(204, 0, 0));
        jLabel4.setText("Make sure to back up the old database file before confirming.");

        jLabel5.setText("P.S. Aren't captchas fun?");

        javax.swing.GroupLayout warningDialogLayout = new javax.swing.GroupLayout(warningDialog.getContentPane());
        warningDialog.getContentPane().setLayout(warningDialogLayout);
        warningDialogLayout.setHorizontalGroup(
            warningDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(warningDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(warningDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, warningDialogLayout.createSequentialGroup()
                        .addGroup(warningDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imagePanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, warningDialogLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 123, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE))
                .addContainerGap())
        );
        warningDialogLayout.setVerticalGroup(
            warningDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(warningDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(warningDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(imagePanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(warningDialogLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(warningDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jLabel4))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("cs015 Grades");
        setResizable(false);

        databaseEditorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cs015Database/x-office-spreadsheet.png"))); // NOI18N
        databaseEditorButton.setText("<html><b>Database Editor</b><br>Manually edit the database and database schema</html>");
        databaseEditorButton.setFocusable(false);
        databaseEditorButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        databaseEditorButton.setIconTextGap(20);
        databaseEditorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseEditorButtonActionPerformed(evt);
            }
        });

        gradeDistributorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cs015Database/accessories-text-editor.png"))); // NOI18N
        gradeDistributorButton.setText("<html><b>Grade Distributor</b><br>Distribute grading assignments to TAs</html>");
        gradeDistributorButton.setFocusable(false);
        gradeDistributorButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        gradeDistributorButton.setIconTextGap(20);
        gradeDistributorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gradeDistributorButtonActionPerformed(evt);
            }
        });

        histogramButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cs015Database/x-office-drawing.png"))); // NOI18N
        histogramButton.setText("<html><b>Grade Information</b><br>Generate and email grade information and histograms</html>");
        histogramButton.setFocusable(false);
        histogramButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        histogramButton.setIconTextGap(20);
        histogramButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                histogramButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("<html><b>Choose An Action Below</b></html>");

        gradeDistributorButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cs015Database/view-refresh.png"))); // NOI18N
        gradeDistributorButton1.setText("<html><b>Regenerate Database</b><br />Reset the database to the conf file</html>");
        gradeDistributorButton1.setFocusable(false);
        gradeDistributorButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        gradeDistributorButton1.setIconTextGap(20);
        gradeDistributorButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regenerateDatabaseActionPerformed(evt);
            }
        });

        emailButtonActionPerformed.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cs015Database/internet-news-reader.png"))); // NOI18N
        emailButtonActionPerformed.setText("<html><b>Notify Students</b><br>Send an email to all students.</html>");
        emailButtonActionPerformed.setFocusable(false);
        emailButtonActionPerformed.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        emailButtonActionPerformed.setIconTextGap(20);
        emailButtonActionPerformed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailButtonActionPerformedActionPerformed(evt);
            }
        });

        gradeDistributorButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cs015Database/accessories-calculator.png"))); // NOI18N
        gradeDistributorButton2.setText("<html><b>Enter Grades</b><br />Manually enter grades into the database.</html>");
        gradeDistributorButton2.setFocusable(false);
        gradeDistributorButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        gradeDistributorButton2.setIconTextGap(20);
        gradeDistributorButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eneterGradesButton(evt);
            }
        });

        jMenu1.setText("File");
        jMenu1.setMaximumSize(new java.awt.Dimension(27, 32767));
        jMenu1.setMinimumSize(new java.awt.Dimension(27, 19));

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem1.setText("Exit");
        jMenuItem1.setMinimumSize(new java.awt.Dimension(200, 0));
        jMenuItem1.setPreferredSize(new java.awt.Dimension(200, 22));
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 683, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(databaseEditorButton, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gradeDistributorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(emailButtonActionPerformed, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE))
                            .addComponent(histogramButton, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(gradeDistributorButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                            .addComponent(gradeDistributorButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(databaseEditorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gradeDistributorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(gradeDistributorButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(histogramButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(emailButtonActionPerformed, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(emailButtonActionPerformed, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(gradeDistributorButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void databaseEditorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databaseEditorButtonActionPerformed
        DatabaseGUI dg = new DatabaseGUI();
        dg.setVisible(true);
    }//GEN-LAST:event_databaseEditorButtonActionPerformed

    private void histogramButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_histogramButtonActionPerformed
        HistogramGUI hg = new HistogramGUI();
        hg.setVisible(true);
    }//GEN-LAST:event_histogramButtonActionPerformed

    private void gradeDistributorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gradeDistributorButtonActionPerformed

        assignment_distributor.AssignmentDistributorGUI g = new AssignmentDistributorGUI();
        g.setVisible(true);
    }//GEN-LAST:event_gradeDistributorButtonActionPerformed

    private void regenerateDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regenerateDatabaseActionPerformed
        _captcha = new Captcha.Builder(200, 50).addText().addNoise().gimp().build();
        jTextField1.setText("");
        imagePanel1.setImage(_captcha.getImage());
        warningDialog.setLocationRelativeTo(null);
        warningDialog.setVisible(true);
        

}//GEN-LAST:event_regenerateDatabaseActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        warningDialog.setVisible(false);
}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (_captcha.isCorrect(jTextField1.getText())) {
            try {
                DatabaseInterops.regenerateDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
            warningDialog.setVisible(false);
        } else {
            _captcha = new Captcha.Builder(200, 50).addText().addNoise().gimp().build();
            jTextField1.setText("");
            imagePanel1.setImage(_captcha.getImage());
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        if (evt.getKeyCode() == 10) {
            jButton2ActionPerformed(null);
        }
    }//GEN-LAST:event_jTextField1KeyPressed

    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped

    }//GEN-LAST:event_jTextField1KeyTyped

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        if (_captcha.isCorrect(jTextField1.getText())) {
            jTextField1.setBackground(new Color(220, 240, 220));

        } else if(jTextField1.getText().length() > 0) {
            jTextField1.setBackground(new Color(240, 220, 220));
        } else {
            jTextField1.setBackground(new Color(255, 255, 255));
        }
    }//GEN-LAST:event_jTextField1KeyReleased

    private void emailButtonActionPerformedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailButtonActionPerformedActionPerformed
        EmailGUI eg = new EmailGUI();
        eg.setVisible(true);
}//GEN-LAST:event_emailButtonActionPerformedActionPerformed

    private void eneterGradesButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eneterGradesButton
        designQGrader.DesignQGraderGUI dqg = new DesignQGraderGUI();
        dqg.setVisible(true);
    }//GEN-LAST:event_eneterGradesButton

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new StartupDialog().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton databaseEditorButton;
    private javax.swing.JButton emailButtonActionPerformed;
    private javax.swing.JButton gradeDistributorButton;
    private javax.swing.JButton gradeDistributorButton1;
    private javax.swing.JButton gradeDistributorButton2;
    private javax.swing.JButton histogramButton;
    private cs015Database.ImagePanel imagePanel1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JDialog warningDialog;
    // End of variables declaration//GEN-END:variables
}
