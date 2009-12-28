/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BackendView.java
 *
 * Created on Sep 25, 2009, 4:38:38 PM
 */
package backend;

import backend.assignmentdist.AssignmentdistView;
import backend.assignmentdist.FinalProjectAssigner;
import backend.database.DatabaseView;
import backend.entergrade.EnterGradesView;
import backend.gradereport.GradeReportView;
import backend.histogram.HistogramView;
import backend.visualizer.TemplateVisualizer;
import gradesystem.GradeSystemApp;
import java.awt.Color;
import javax.imageio.ImageIO;
import nl.captcha.Captcha;
import org.jdesktop.application.Action;
import utils.ErrorView;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import utils.Allocator;
import utils.Assignment;
import utils.AssignmentType;
import utils.BashConsole;
import utils.ConfigurationManager;

/**
 *
 * @author psastras
 */
public class BackendView extends javax.swing.JFrame {

    private Captcha _captcha;

    /** Creates new form BackendView */
    public BackendView() {
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/utilities-terminal.png")));
        } catch (Exception e) {
        }
        initComponents();

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (!GradeSystemApp._testing) {
                    String cmd = "cp " + Allocator.getConstants().getDatabaseFilePath() + " " + Allocator.getConstants().getDatabaseBackupDir() + "cs015db_bk_" + Allocator.getGeneralUtilities().getCalendarAsString(Calendar.getInstance()).replaceAll("(\\s|:)", "_");
                    BashConsole.writeThreaded(cmd);
                }
            }
        });

        this.setTitle("Grading Backend");
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
        imagePanel1 = new backend.components.ImagePanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        m_databaseButton = new javax.swing.JButton();
        m_assgndistButton = new javax.swing.JButton();
        m_histogramButton = new javax.swing.JButton();
        m_entergradesButton = new javax.swing.JButton();
        m_gradeReportButton = new javax.swing.JButton();
        m_regenerateButton = new javax.swing.JButton();
        previewRubricButton = new javax.swing.JButton();
        m_importLabsButton = new javax.swing.JButton();
        m_gradeReportButton1 = new javax.swing.JButton();
        m_gradeReportButton2 = new javax.swing.JButton();
        m_menu = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(BackendView.class);
        warningDialog.setTitle(resourceMap.getString("warningDialog.title")); // NOI18N
        warningDialog.setMinimumSize(new java.awt.Dimension(600, 210));
        warningDialog.setModal(true);
        warningDialog.setName("warningDialog"); // NOI18N
        warningDialog.setResizable(false);

        jLabel2.setText("<html><b>Are you sure you want to reset the database?</b><br />Doing this will remove all data from the database and reset it according to the conf file.</html>");
        jLabel2.setIconTextGap(20);
        jLabel2.setName("jLabel2"); // NOI18N

        jTextField1.setName("jTextField1"); // NOI18N
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
        jLabel3.setName("jLabel3"); // NOI18N

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        imagePanel1.setName("imagePanel1"); // NOI18N

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

        jLabel4.setForeground(resourceMap.getColor("jLabel4.foreground")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout warningDialogLayout = new javax.swing.GroupLayout(warningDialog.getContentPane());
        warningDialog.getContentPane().setLayout(warningDialogLayout);
        warningDialogLayout.setHorizontalGroup(
            warningDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(warningDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(warningDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, warningDialogLayout.createSequentialGroup()
                        .addGroup(warningDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imagePanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, warningDialogLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 123, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE))
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
        setName("Form"); // NOI18N

        m_databaseButton.setIcon(resourceMap.getIcon("m_databaseButton.icon")); // NOI18N
        m_databaseButton.setText(resourceMap.getString("m_databaseButton.text")); // NOI18N
        m_databaseButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_databaseButton.setIconTextGap(20);
        m_databaseButton.setName("m_databaseButton"); // NOI18N
        m_databaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_databaseButtonActionPerformed(evt);
            }
        });

        m_assgndistButton.setIcon(resourceMap.getIcon("m_assgndistButton.icon")); // NOI18N
        m_assgndistButton.setText(resourceMap.getString("m_assgndistButton.text")); // NOI18N
        m_assgndistButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_assgndistButton.setIconTextGap(20);
        m_assgndistButton.setName("m_assgndistButton"); // NOI18N
        m_assgndistButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_assgndistButtonActionPerformed(evt);
            }
        });

        m_histogramButton.setIcon(resourceMap.getIcon("m_histogramButton.icon")); // NOI18N
        m_histogramButton.setText(resourceMap.getString("m_histogramButton.text")); // NOI18N
        m_histogramButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_histogramButton.setIconTextGap(20);
        m_histogramButton.setName("m_histogramButton"); // NOI18N
        m_histogramButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_histogramButtonActionPerformed(evt);
            }
        });

        m_entergradesButton.setIcon(resourceMap.getIcon("m_entergradesButton.icon")); // NOI18N
        m_entergradesButton.setText(resourceMap.getString("m_entergradesButton.text")); // NOI18N
        m_entergradesButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_entergradesButton.setIconTextGap(20);
        m_entergradesButton.setName("m_entergradesButton"); // NOI18N
        m_entergradesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_entergradesButtonActionPerformed(evt);
            }
        });

        m_gradeReportButton.setIcon(resourceMap.getIcon("m_gradeReportButton.icon")); // NOI18N
        m_gradeReportButton.setText(resourceMap.getString("m_gradeReportButton.text")); // NOI18N
        m_gradeReportButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_gradeReportButton.setIconTextGap(20);
        m_gradeReportButton.setName("m_gradeReportButton"); // NOI18N
        m_gradeReportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_gradeReportButtonActionPerformed(evt);
            }
        });

        m_regenerateButton.setIcon(resourceMap.getIcon("m_regenerateButton.icon")); // NOI18N
        m_regenerateButton.setText(resourceMap.getString("m_regenerateButton.text")); // NOI18N
        m_regenerateButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_regenerateButton.setIconTextGap(20);
        m_regenerateButton.setName("m_regenerateButton"); // NOI18N
        m_regenerateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_regenerateButtonActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getActionMap(BackendView.class, this);
        previewRubricButton.setAction(actionMap.get("previewRubricButtonActionPerformed")); // NOI18N
        previewRubricButton.setIcon(resourceMap.getIcon("previewRubricButton.icon")); // NOI18N
        previewRubricButton.setText(resourceMap.getString("previewRubricButton.text")); // NOI18N
        previewRubricButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        previewRubricButton.setIconTextGap(20);
        previewRubricButton.setName("previewRubricButton"); // NOI18N

        m_importLabsButton.setIcon(resourceMap.getIcon("m_importLabsButton.icon")); // NOI18N
        m_importLabsButton.setText(resourceMap.getString("m_importLabsButton.text")); // NOI18N
        m_importLabsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_importLabsButton.setIconTextGap(20);
        m_importLabsButton.setName("m_importLabsButton"); // NOI18N
        m_importLabsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_importLabsButtonActionPerformed(evt);
            }
        });

        m_gradeReportButton1.setIcon(resourceMap.getIcon("m_gradeReportButton1.icon")); // NOI18N
        m_gradeReportButton1.setText(resourceMap.getString("m_gradeReportButton1.text")); // NOI18N
        m_gradeReportButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_gradeReportButton1.setIconTextGap(20);
        m_gradeReportButton1.setName("m_gradeReportButton1"); // NOI18N
        m_gradeReportButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDatabase(evt);
            }
        });

        m_gradeReportButton2.setIcon(resourceMap.getIcon("m_gradeReportButton2.icon")); // NOI18N
        m_gradeReportButton2.setText(resourceMap.getString("m_gradeReportButton2.text")); // NOI18N
        m_gradeReportButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_gradeReportButton2.setIconTextGap(20);
        m_gradeReportButton2.setName("m_gradeReportButton2"); // NOI18N
        m_gradeReportButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_gradeReportButton2ActionPerformed(evt);
            }
        });

        m_menu.setName("m_menu"); // NOI18N

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N
        m_menu.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N
        m_menu.add(jMenu2);

        setJMenuBar(m_menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_gradeReportButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                    .addComponent(m_databaseButton, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                    .addComponent(m_histogramButton, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                    .addComponent(previewRubricButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                    .addComponent(m_gradeReportButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(m_regenerateButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addComponent(m_importLabsButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addComponent(m_entergradesButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addComponent(m_assgndistButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addComponent(m_gradeReportButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(m_assgndistButton, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_databaseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_entergradesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_histogramButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(m_importLabsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(previewRubricButton, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_gradeReportButton, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .addComponent(m_gradeReportButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(m_regenerateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .addComponent(m_gradeReportButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void m_databaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_databaseButtonActionPerformed
        DatabaseView dv = new DatabaseView();
        dv.setLocationRelativeTo(null);
        dv.setVisible(true);
    }//GEN-LAST:event_m_databaseButtonActionPerformed

    private void m_histogramButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_histogramButtonActionPerformed
        HistogramView hv = new HistogramView();
        hv.setLocationRelativeTo(null);
        hv.setVisible(true);
    }//GEN-LAST:event_m_histogramButtonActionPerformed

    private void m_regenerateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_regenerateButtonActionPerformed
        _captcha = new Captcha.Builder(200, 50).addText().addNoise().gimp().build();
        jTextField1.setText("");
        imagePanel1.setImage(_captcha.getImage());
        warningDialog.setLocationRelativeTo(null);
        warningDialog.setVisible(true);
    }//GEN-LAST:event_m_regenerateButtonActionPerformed

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        if (evt.getKeyCode() == 10) {
            jButton2ActionPerformed(null);
        }
}//GEN-LAST:event_jTextField1KeyPressed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        if (_captcha.isCorrect(jTextField1.getText())) {
            jTextField1.setBackground(new Color(220, 240, 220));
        } else if (jTextField1.getText().length() > 0) {
            jTextField1.setBackground(new Color(240, 220, 220));
        } else {
            jTextField1.setBackground(new Color(255, 255, 255));
        }
}//GEN-LAST:event_jTextField1KeyReleased

    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped
}//GEN-LAST:event_jTextField1KeyTyped

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        warningDialog.setVisible(false);
}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (_captcha.isCorrect(jTextField1.getText())) {
            try {
                DatabaseIO.regenerateDatabase();
            } catch (Exception e) {
                new ErrorView(e);
            }
            warningDialog.setVisible(false);
        } else {
            _captcha = new Captcha.Builder(200, 50).addText().addNoise().gimp().build();
            jTextField1.setText("");
            imagePanel1.setImage(_captcha.getImage());
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void m_entergradesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_entergradesButtonActionPerformed
        EnterGradesView egv = new EnterGradesView();
        egv.setLocationRelativeTo(null);
        egv.setVisible(true);
    }//GEN-LAST:event_m_entergradesButtonActionPerformed

    private void m_assgndistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_assgndistButtonActionPerformed
        AssignmentdistView av = new AssignmentdistView("Clock");    //sets starting asgn
        av.setLocationRelativeTo(null);
        av.setVisible(true);
}//GEN-LAST:event_m_assgndistButtonActionPerformed

    private void m_gradeReportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_gradeReportButtonActionPerformed
        GradeReportView grv = new GradeReportView();
        grv.setLocationRelativeTo(null);
        grv.setVisible(true);
}//GEN-LAST:event_m_gradeReportButtonActionPerformed

    private void m_importLabsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_importLabsButtonActionPerformed
        //THE LABS IN THE DATABASE MUST BE IN NUMERICAL ORDER OR THIS WILL NOT WORK

        File labDir = new File(Allocator.getConstants().getLabsDir());
        File[] fileList = labDir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().startsWith("lab") && pathname.getName().endsWith("txt");
            }
        });
        //Make a comparator that will work for names such that lab10, lab11 > lab2..lab9
        Comparator c = new Comparator() {

            public int compare(Object o1, Object o2) {
                int i1 = Integer.parseInt((((File) o1).getName()).replaceAll("[^0-9]", ""));
                int i2 = Integer.parseInt((((File) o2).getName()).replaceAll("[^0-9]", ""));
                return (i1 < i2) ? -1 : (i1 == i2) ? 0 : 1;
            }
        };
        Arrays.sort(fileList, c);
        //parse teh lab filez!
        String[] labNames = DatabaseIO.getLabNames();
        for (int i = 0; i < fileList.length; i++) {
            String total = String.valueOf(DatabaseIO.getAssignmentTotal(labNames[i]));
            String[] logins = Allocator.getGeneralUtilities().readFile(fileList[i]).replaceAll("\\s*-.*", "").split("\\n");
            for (String s : logins) {
                long row = DatabaseIO.getRowID("grades_" + labNames[i], "stud_login_" + labNames[i], s);
                String[] data = (String[]) DatabaseIO.getDataRow("grades_" + labNames[i], row);
                data[1] = total;
                try {
                    DatabaseIO.update(row, "grades_" + labNames[i], (Object[]) data);
                } catch (Exception e) {
                    new ErrorView(e);
                    return;
                }
            }
            System.out.print("Lab: " + labNames[i] + " - " + total + " - ");
            System.out.println(Arrays.toString(logins));
        }

    }//GEN-LAST:event_m_importLabsButtonActionPerformed

    //@TODO FInish this stuff
    private void exportDatabase(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDatabase
        new ExportView().start();

}//GEN-LAST:event_exportDatabase

    private void m_gradeReportButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_gradeReportButton2ActionPerformed
        FinalProjectAssigner fpa = new FinalProjectAssigner();
        fpa.setLocationRelativeTo(null);
        fpa.setVisible(true);
    }//GEN-LAST:event_m_gradeReportButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new BackendView().setVisible(true);
            }
        });
    }

    @Action
    public void previewRubricButtonActionPerformed() {
        Vector<Object> v = new Vector<Object>();
        for (String s : DatabaseIO.getAssignmentNames()) {
            if (DatabaseIO.getAssignmentType(s) == AssignmentType.PROJECT) {
                v.add(s);
            }
        }
        //Add Final projects based on data from config file
        if (v.contains("Final")) {
            v.remove("Final");
        }
        //Todo: Store final project data in the database somehow?
        for (Assignment asgn : ConfigurationManager.getAssignments()) {
            if (asgn.Type == AssignmentType.FINAL) {
                v.add(asgn.Name);
            }
        }
        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png"); // NOI18N
        String message = "Choose Project to Preview";
        String project = (String) JOptionPane.showInputDialog(new JFrame(), message, "Select Project", JOptionPane.PLAIN_MESSAGE, icon, v.toArray(), null);
        new TemplateVisualizer(project);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private backend.components.ImagePanel imagePanel1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton m_assgndistButton;
    private javax.swing.JButton m_databaseButton;
    private javax.swing.JButton m_entergradesButton;
    private javax.swing.JButton m_gradeReportButton;
    private javax.swing.JButton m_gradeReportButton1;
    private javax.swing.JButton m_gradeReportButton2;
    private javax.swing.JButton m_histogramButton;
    private javax.swing.JButton m_importLabsButton;
    private javax.swing.JMenuBar m_menu;
    private javax.swing.JButton m_regenerateButton;
    private javax.swing.JButton previewRubricButton;
    private javax.swing.JDialog warningDialog;
    // End of variables declaration//GEN-END:variables
}
