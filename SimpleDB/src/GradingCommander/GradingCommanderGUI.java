/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GradingCommanderGUI2.java
 *
 * Created on Sep 6, 2009, 5:14:50 PM
 */
package GradingCommander;

import cs015Database.*;
import java.io.IOException;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

/**
 *
 * @author psastras
 */
public class GradingCommanderGUI extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    /** Creates new form GradingCommanderGUI2 */
    public GradingCommanderGUI() {
        initComponents();
        updateFormComponents();
        this.setTitle(System.getProperty("user.name") + " - cs015 Grader");
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/GradingCommander/icons/icon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFormComponents() {
        try {
            ISqlJetCursor cursor = DatabaseInterops.getAllData("assignments");
            String[] columnNames = DatabaseInterops.getColumnNames("assignments");
            try {
                Vector v = new Vector<Object>();
                while (!cursor.eof()) {
                    v.add(cursor.getString(columnNames[0]));
                    cursor.next();
                }
                assignmentList.setListData(v);
                if (assignmentList.getModel().getSize() > 0) {
                    assignmentList.setSelectedIndex(0);
                }
            } finally {
                cursor.close();
            }
            populateStudentList();
        } catch (Exception e) {
            e.printStackTrace();
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

        assignmentListPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        assignmentList = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        generalCommandsLabel = new javax.swing.JLabel();
        selectedStudentLabel = new javax.swing.JLabel();
        openButton = new javax.swing.JButton();
        printButton = new javax.swing.JButton();
        compileButton = new javax.swing.JButton();
        runButton = new javax.swing.JButton();
        runTesterButton = new javax.swing.JButton();
        runDemoButton = new javax.swing.JButton();
        printAllButton = new javax.swing.JButton();
        submitGradesButton = new javax.swing.JButton();
        runButton1 = new javax.swing.JButton();
        studentListPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        studentList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        currentInfo = new javax.swing.JLabel();
        mainMenuBar = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        quitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Assignment");

        assignmentList.setForeground(new java.awt.Color(51, 51, 51));
        assignmentList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        assignmentList.setDoubleBuffered(true);
        assignmentList.setSelectionBackground(new java.awt.Color(194, 214, 246));
        assignmentList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                assignmentListMouseClicked(evt);
            }
        });
        assignmentList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                assignmentListKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(assignmentList);

        javax.swing.GroupLayout assignmentListPanelLayout = new javax.swing.GroupLayout(assignmentListPanel);
        assignmentListPanel.setLayout(assignmentListPanelLayout);
        assignmentListPanelLayout.setHorizontalGroup(
            assignmentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, assignmentListPanelLayout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addGroup(assignmentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        assignmentListPanelLayout.setVerticalGroup(
            assignmentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(assignmentListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE))
        );

        generalCommandsLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        generalCommandsLabel.setText("General Commands");

        selectedStudentLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        selectedStudentLabel.setText("Selected Student Commands");

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GradingCommander/icons/open.png"))); // NOI18N
        openButton.setText("<html><b>Open Files</b><br>Open files in Kate editor</html>");
        openButton.setFocusable(false);
        openButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        printButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GradingCommander/icons/print.png"))); // NOI18N
        printButton.setText("<html><b>Print</b><br>Print the student's files</html>");
        printButton.setFocusable(false);
        printButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        compileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GradingCommander/icons/compile.png"))); // NOI18N
        compileButton.setText("<html><b>Compile</b><br>Compile the student's project</html>");
        compileButton.setFocusable(false);
        compileButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        compileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileButtonActionPerformed(evt);
            }
        });

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GradingCommander/icons/run.png"))); // NOI18N
        runButton.setText("<html><b>Run</b><br>Run the project (must compile first)</html>");
        runButton.setFocusable(false);
        runButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        runTesterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GradingCommander/icons/runTester.png"))); // NOI18N
        runTesterButton.setText("<html><b>Run Tester</b><br>Run tester on the project</html>");
        runTesterButton.setFocusable(false);
        runTesterButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        runTesterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runTesterButtonActionPerformed(evt);
            }
        });

        runDemoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GradingCommander/icons/test.png"))); // NOI18N
        runDemoButton.setText("<html><b>Run Demo</b><br>Run the demo for the current project");
        runDemoButton.setFocusable(false);
        runDemoButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        runDemoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runDemoButtonActionPerformed(evt);
            }
        });

        printAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GradingCommander/icons/print.png"))); // NOI18N
        printAllButton.setText("<html><b>Print All</b><br>Print all assignments");
        printAllButton.setFocusable(false);
        printAllButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        printAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printAllButtonActionPerformed(evt);
            }
        });

        submitGradesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GradingCommander/icons/submit.png"))); // NOI18N
        submitGradesButton.setText("<html><b>Submit Grading</b><br>Submit all graded assignments");
        submitGradesButton.setFocusable(false);
        submitGradesButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        submitGradesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitGradesButtonActionPerformed(evt);
            }
        });

        runButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GradingCommander/icons/grade.png"))); // NOI18N
        runButton1.setText("<html><b>Grade</b><br>Grade the student's assignment</html>");
        runButton1.setFocusable(false);
        runButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        runButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(generalCommandsLabel)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(submitGradesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(runDemoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                                .addGap(3, 3, 3)
                                .addComponent(printAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(selectedStudentLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 373, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(compileButton, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                                    .addComponent(runTesterButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                                    .addComponent(openButton, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(runButton, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                                    .addComponent(printButton, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                                    .addComponent(runButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))))
                        .addGap(9, 9, 9))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalCommandsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(runDemoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitGradesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(selectedStudentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openButton, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                    .addComponent(printButton, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(runButton, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(runButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                            .addComponent(runTesterButton, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)))
                    .addComponent(compileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Student");

        studentList.setForeground(new java.awt.Color(51, 51, 51));
        studentList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        studentList.setSelectionBackground(new java.awt.Color(190, 214, 246));
        studentList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                studentListMouseClicked(evt);
            }
        });
        studentList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                studentListKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(studentList);

        javax.swing.GroupLayout studentListPanelLayout = new javax.swing.GroupLayout(studentListPanel);
        studentListPanel.setLayout(studentListPanelLayout);
        studentListPanelLayout.setHorizontalGroup(
            studentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(studentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                    .addComponent(jLabel2))
                .addContainerGap())
        );
        studentListPanelLayout.setVerticalGroup(
            studentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE))
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel3.setText("Currently Grading");

        currentInfo.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        currentInfo.setText("None");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 401, Short.MAX_VALUE)
                .addComponent(currentInfo)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(currentInfo))
                .addContainerGap(3, Short.MAX_VALUE))
        );

        jMenu1.setText("File");

        quitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        quitMenuItem.setText("Quit");
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(quitMenuItem);

        mainMenuBar.add(jMenu1);

        jMenu2.setText("Edit");
        mainMenuBar.add(jMenu2);

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(assignmentListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(studentListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(assignmentListPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(studentListPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        GradingCommander.openStudentProject((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
    }//GEN-LAST:event_openButtonActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        GradingCommander.printStudentProject((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
    }//GEN-LAST:event_printButtonActionPerformed

    private void compileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compileButtonActionPerformed
        GradingCommander.compileStudentProject((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
    }//GEN-LAST:event_compileButtonActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        GradingCommander.runStudentProject((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
    }//GEN-LAST:event_runButtonActionPerformed

    private void runTesterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runTesterButtonActionPerformed
        GradingCommander.runTester((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
    }//GEN-LAST:event_runTesterButtonActionPerformed

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_quitMenuItemActionPerformed

    private void assignmentListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_assignmentListKeyReleased
        populateStudentList();
    }//GEN-LAST:event_assignmentListKeyReleased

    private void assignmentListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_assignmentListMouseClicked
        populateStudentList();
    }//GEN-LAST:event_assignmentListMouseClicked

    private void runButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButton1ActionPerformed
        GradingCommander.runStudentProject((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
    }//GEN-LAST:event_runButton1ActionPerformed

    private void submitGradesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitGradesButtonActionPerformed
    }//GEN-LAST:event_submitGradesButtonActionPerformed

    private void runDemoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runDemoButtonActionPerformed
        GradingCommander.demoProject((String) assignmentList.getSelectedValue());
    }//GEN-LAST:event_runDemoButtonActionPerformed

    private void studentListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_studentListKeyReleased
        currentInfo.setText((String) studentList.getSelectedValue());
    }//GEN-LAST:event_studentListKeyReleased

    private void studentListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_studentListMouseClicked
        currentInfo.setText((String) studentList.getSelectedValue());
    }//GEN-LAST:event_studentListMouseClicked

    private void printAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printAllButtonActionPerformed
        
    }//GEN-LAST:event_printAllButtonActionPerformed


    private void populateStudentList() {

        String user = System.getProperty("user.name");

        try {
            ISqlJetCursor cursor = DatabaseInterops.getItemWithFilter("assignment_dist", "taLoginDist", user);
            if (cursor.eof()) {
                cursor.close();
                return;
            }
            try {
                String s = cursor.getString((String) assignmentList.getSelectedValue());
                if (s == null) {
                    s = "";
                }
                String[] ss = s.split(",");
                studentList.setListData(ss);
                if (studentList.getModel().getSize() > 0) {
                    studentList.setSelectedIndex(0);
                    currentInfo.setText((String) studentList.getSelectedValue());
                }

            } finally {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new GradingCommanderGUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList assignmentList;
    private javax.swing.JPanel assignmentListPanel;
    private javax.swing.JButton compileButton;
    private javax.swing.JLabel currentInfo;
    private javax.swing.JLabel generalCommandsLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JButton openButton;
    private javax.swing.JButton printAllButton;
    private javax.swing.JButton printButton;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JButton runButton;
    private javax.swing.JButton runButton1;
    private javax.swing.JButton runDemoButton;
    private javax.swing.JButton runTesterButton;
    private javax.swing.JLabel selectedStudentLabel;
    private javax.swing.JList studentList;
    private javax.swing.JPanel studentListPanel;
    private javax.swing.JButton submitGradesButton;
    // End of variables declaration//GEN-END:variables
}
