/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DesignQGrader.java
 *
 * Created on Sep 16, 2009, 12:40:19 PM
 */
package cs015.tasupport.grading.addGrades;

import cs015.tasupport.grading.config.AssignmentType;
import cs015.tasupport.grading.rubric.RubricManager;
import cs015.tasupport.utils.Utils;
import cs015Database.DatabaseInterops;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Paul
 */
public class AddGradesGUI extends javax.swing.JFrame {

    /** Creates new form DesignQGrader */
    public AddGradesGUI() {
        initComponents();
        this.setLocationRelativeTo(null);
        try {
            addStudentDialog.setIconImage(ImageIO.read(getClass().getResource("/database_editor/dialog-warning.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        jTextField2.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c == KeyEvent.VK_PERIOD) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE) || (c == KeyEvent.VK_ENTER) || (c == KeyEvent.VK_TAB) || (Character.isDigit(c)))) {
                    e.consume();
                } else if ((c == KeyEvent.VK_PERIOD)) {
                    if (!jTextField2.getText().contains(".")) {
                        if (jTextField2.getText().length() < 1) {
                            jTextField2.setText("0");
                        }
                    } else {
                        e.consume();
                    }
                }

            }
        });

        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/cs015Database/accessories-calculator.png")));
            DefaultTableModel m = (DefaultTableModel) studentTable.getModel();
            m.addColumn("Students");
            for (String s : DatabaseInterops.getStudentNames()) {
                m.insertRow(studentTable.getRowCount(), new String[]{s});
            }
            m.insertRow(0, new String[]{""});
            m = (DefaultTableModel) assignmentTable.getModel();
            m.addColumn("Assignments");
            for (String s : DatabaseInterops.getAssignmentNames()) {
                if (DatabaseInterops.getAssignmentType(s) == AssignmentType.HOMEWORK || (DatabaseInterops.getAssignmentType(s) == AssignmentType.PROJECT && DatabaseInterops.getAssignmentDQ(s) != 0)) {
                    m.insertRow(assignmentTable.getRowCount(), new String[]{s});
                }
            }
            this.setTitle(Utils.getUserLogin() + " - Add Grades to Database");
            studentTable.getSelectionModel().setSelectionInterval(0, 0);
            studentTable.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
            assignmentTable.getSelectionModel().setSelectionInterval(0, 0);
            assignmentTable.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
            jTextField1.setText((String) studentTable.getModel().getValueAt(studentTable.getSelectedRow(), studentTable.getSelectedColumn()));
            jTextField1.setSelectionStart(0);
            jTextField1.setSelectionEnd(jTextField1.getText().length());
            statusLabel.setText("Ready");
            if (jTextField1.getText().length() == 0) {
                jButton1.setEnabled(false);
            } else {
                jButton1.setEnabled(true);
            }
            update();
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

        addStudentDialog = new javax.swing.JDialog();
        jLabel7 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        studentTable = new cs015Database.Table();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        assignmentTable = new cs015Database.Table();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        getAllFromXMLButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        addStudentDialog.setTitle("Add Student Dialog");
        addStudentDialog.setMinimumSize(new java.awt.Dimension(466, 130));
        addStudentDialog.setModal(true);
        addStudentDialog.setResizable(false);

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database_editor/dialog-warning.png"))); // NOI18N
        jLabel7.setText("<html>The selected student <b>" + jTextField1.getText() + "</b> was not found.<br />Add the student to the database?");
        jLabel7.setFocusable(false);
        jLabel7.setIconTextGap(20);

        jButton2.setMnemonic('Y');
        jButton2.setText("Yes");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setMnemonic('N');
        jButton3.setText("No");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout addStudentDialogLayout = new javax.swing.GroupLayout(addStudentDialog.getContentPane());
        addStudentDialog.getContentPane().setLayout(addStudentDialogLayout);
        addStudentDialogLayout.setHorizontalGroup(
            addStudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStudentDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addStudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addStudentDialogLayout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStudentDialogLayout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13))))
        );
        addStudentDialogLayout.setVerticalGroup(
            addStudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStudentDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        studentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        studentTable.setFocusable(false);
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                studentTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(studentTable);

        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField1FocusLost(evt);
            }
        });
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

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 12));
        jLabel1.setText("Select Student");

        assignmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        assignmentTable.setFocusable(false);
        assignmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                assignmentTableMouseClicked(evt);
            }
        });
        assignmentTable.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                assignmentTableAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        assignmentTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                assignmentTableKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                assignmentTableKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(assignmentTable);

        jLabel2.setFont(new java.awt.Font("SansSerif", 1, 12));
        jLabel2.setText("Select Assignment");

        jLabel3.setText("Earned Points");

        jTextField2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField2MouseClicked(evt);
            }
        });
        jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField2FocusGained(evt);
            }
        });
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField2KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField2KeyTyped(evt);
            }
        });

        jLabel4.setText("Total Points");

        jTextField3.setEditable(false);
        jTextField3.setFocusable(false);

        jLabel5.setText("Score (%)");

        jTextField4.setEditable(false);
        jTextField4.setFocusable(false);
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("SansSerif", 1, 12));
        jLabel6.setText("Enter Score");

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("light")));

        statusLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 11));
        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        statusLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(359, Short.MAX_VALUE)
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
        );

        jButton1.setMnemonic('E');
        jButton1.setText("Enter Grade");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        getAllFromXMLButton.setText("Get All Grades from XML Files");
        getAllFromXMLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getAllFromXMLButtonActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem1.setText("Quit");
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(getAllFromXMLButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 139, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(getAllFromXMLButton))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped
//        table1.applyFilterSorter();
//        table1.filter(jTextField1.getText());
//        table1.setColumnSelectionAllowed(true);
//        table1.getSelectionModel().setSelectionInterval(0, 0);
//        table1.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
//        System.out.println(jTextField1.getText());
    }//GEN-LAST:event_jTextField1KeyTyped

    public void updateStatus(String message) {
        statusLabel.setText(message);
        Timer t = new Timer();
        class NewTask extends TimerTask {

            @Override
            public void run() {
                statusLabel.setText("Ready");
                this.cancel();
            }
        }

        t.schedule(new NewTask(), 1000);
    }

    private void update() {
        //determine whether the add XML grades button should be enabled
        getAllFromXMLButton.setEnabled(false);
        for (String s : DatabaseInterops.getProjectNames()) {
            if (s.equalsIgnoreCase((String)assignmentTable.getModel().getValueAt(assignmentTable.getSelectedRow(), 0))) {
                getAllFromXMLButton.setEnabled(true);
                break;
            }
        }

        if (jTextField1.getText().length() == 0) {
            jButton1.setEnabled(false);
        } else {
            jButton1.setEnabled(true);
        }
        if (studentTable.getRowCount() == 0 || ((String) studentTable.getValueAt(studentTable.getSelectedRow(), studentTable.getSelectedColumn())).length() == 0) {
            jTextField2.setText("");
            jTextField4.setText("");
            if (DatabaseInterops.getAssignmentDQ((String) assignmentTable.getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn())) == 0) {
                jTextField3.setText("" + DatabaseInterops.getAssignmentTotal((String) assignmentTable.getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn())));
            } else {
                jTextField3.setText("" + DatabaseInterops.getAssignmentDQ((String) assignmentTable.getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn())));
            }
            return;
        }

        jTextField1.setText((String) studentTable.getValueAt(studentTable.getSelectedRow(), studentTable.getSelectedColumn()));
        jTextField1.setCaretPosition(0);
        jTextField1.setSelectionStart(0);
        jTextField1.setSelectionEnd(jTextField1.getText().length());
        if (DatabaseInterops.getAssignmentDQ((String) assignmentTable.getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn())) == 0) {
            jTextField2.setText("" + DatabaseInterops.getStudentProjectScore((String) assignmentTable.getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn()), jTextField1.getText()));
            jTextField3.setText("" + DatabaseInterops.getAssignmentTotal((String) assignmentTable.getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn())));
        } else {
            jTextField2.setText("" + DatabaseInterops.getStudentDQScore((String) assignmentTable.getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn()), jTextField1.getText()));
            jTextField3.setText("" + DatabaseInterops.getAssignmentDQ((String) assignmentTable.getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn())));
        }
        if (jTextField2.getText().length() > 0 && jTextField3.getText().length() > 0) {
            jTextField4.setText("" + (Double.parseDouble(jTextField2.getText()) / Double.parseDouble(jTextField3.getText()) * 100));
        } else {
            jTextField4.setText("");
        }

       
    }

    private void jTextField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusLost
        //update();
    }//GEN-LAST:event_jTextField1FocusLost

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed

        if (evt.getKeyCode() == 38 && studentTable.getSelectedRow() != 0) { //up
            studentTable.getSelectionModel().setSelectionInterval(studentTable.getSelectedRow() - 1, studentTable.getSelectedRow() - 1);
        //update();
        } else if (evt.getKeyCode() == 40 && studentTable.getSelectedRow() != studentTable.getRowCount() - 1) { //down
            studentTable.getSelectionModel().setSelectionInterval(studentTable.getSelectedRow() + 1, studentTable.getSelectedRow() + 1);
        //update();
        } else if (evt.getKeyCode() == 10) { //Enter key
            jTextField2.requestFocus();
        }
    }//GEN-LAST:event_jTextField1KeyPressed

    private void jTextField2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyTyped
    }//GEN-LAST:event_jTextField2KeyTyped

    private void studentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_studentTableMouseClicked
        if (studentTable.getRowCount() == 0 || ((String) studentTable.getValueAt(studentTable.getSelectedRow(), studentTable.getSelectedColumn())).length() == 0) {
            jTextField1.setText("");
        }
        update();
}//GEN-LAST:event_studentTableMouseClicked

    private void assignmentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_assignmentTableMouseClicked
        update();
}//GEN-LAST:event_assignmentTableMouseClicked

    private void jTextField2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyPressed
        if (evt.getKeyCode() == 10) {
            jButton1ActionPerformed(null);
        }
    }//GEN-LAST:event_jTextField2KeyPressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            String assignmentName = (String) assignmentTable.getModel().getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn());
            String studentLogin = jTextField1.getText();
            long row = DatabaseInterops.getRowID("grades_" + assignmentName, "stud_login_" + assignmentName, studentLogin);
            if (DatabaseInterops.getDataCell("grades_" + assignmentName, row, "studLogins").compareToIgnoreCase(studentLogin) == 0) {
                String[] s = (String[]) DatabaseInterops.getDataRow("grades_" + assignmentName, row);
                s[1] = jTextField2.getText();
                DatabaseInterops.update(row, "grades_" + assignmentName, (Object[]) s);
                updateStatus("Written to database");
            } else {
                jLabel7.setText("<html>The selected student <b>" + jTextField1.getText() + "</b> was not found.<br />Add the student to the database?");
                addStudentDialog.setLocationRelativeTo(null);
                addStudentDialog.setVisible(true);
            }
            jTextField1.requestFocus();
            jTextField1.setText("");
            jTextField2.setText("");
            jTextField4.setText("");
            jTextField1KeyTyped(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusGained
        update();
        jTextField2.setSelectionStart(0);
        jTextField2.setSelectionEnd(jTextField1.getText().length());
    }//GEN-LAST:event_jTextField2FocusGained

    private void jTextField2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField2MouseClicked
        // TODO add your handling code here
    }//GEN-LAST:event_jTextField2MouseClicked

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        if (evt.getKeyCode() == 27) { //Esc key
            studentTable.applyFilterSorter();
            studentTable.filter("");
            studentTable.setColumnSelectionAllowed(true);
            studentTable.getSelectionModel().setSelectionInterval(0, 0);
            studentTable.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
            return;
        }
        if (evt.getKeyCode() == 38 || evt.getKeyCode() == 40) { //up

            jTextField1.setText("");
            update();
            return;
        }
        if (jTextField1.getText().length() == 0) {
            jButton1.setEnabled(false);
        } else {
            jButton1.setEnabled(true);
        }
        studentTable.applyFilterSorter();
        studentTable.filter(jTextField1.getText());
        studentTable.setColumnSelectionAllowed(true);
        studentTable.getSelectionModel().setSelectionInterval(0, 0);
        studentTable.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void assignmentTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_assignmentTableKeyReleased
        // TODO add your handling code here:
}//GEN-LAST:event_assignmentTableKeyReleased

    private void assignmentTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_assignmentTableKeyPressed
        update();
}//GEN-LAST:event_assignmentTableKeyPressed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        DatabaseInterops.addStudent(jTextField1.getText());
        String assignmentName = (String) assignmentTable.getModel().getValueAt(assignmentTable.getSelectedRow(), assignmentTable.getSelectedColumn());
        String studentLogin = jTextField1.getText();
        long row = DatabaseInterops.getRowID("grades_" + assignmentName, "stud_login_" + assignmentName, studentLogin);
        String[] s = (String[]) DatabaseInterops.getDataRow("grades_" + assignmentName, row);
        s[1] = jTextField2.getText();
        try {
            DatabaseInterops.update(row, "grades_" + assignmentName, (Object[]) s);
            updateStatus("Written to database");
        } catch (Exception e) {
            e.printStackTrace();
        }
        addStudentDialog.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
        if (jTextField2.getText().length() > 0 && jTextField3.getText().length() > 0) {
            jTextField4.setText("" + (Double.parseDouble(jTextField2.getText()) / Double.parseDouble(jTextField3.getText()) * 100));
        } else {
            jTextField4.setText("");
        }
    }//GEN-LAST:event_jTextField2KeyReleased

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        addStudentDialog.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void assignmentTableAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_assignmentTableAncestorAdded
        // TODO add your handling code here:
}//GEN-LAST:event_assignmentTableAncestorAdded

    private void getAllFromXMLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getAllFromXMLButtonActionPerformed
        RubricManager.getAllScores((String)assignmentTable.getModel().getValueAt(assignmentTable.getSelectedRow(), 0));
    }//GEN-LAST:event_getAllFromXMLButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new AddGradesGUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog addStudentDialog;
    private cs015Database.Table assignmentTable;
    private javax.swing.JButton getAllFromXMLButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JLabel statusLabel;
    private cs015Database.Table studentTable;
    // End of variables declaration//GEN-END:variables
}