/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AssignmentDistributorGUI.java
 *
 * Created on Sep 7, 2009, 12:53:58 PM
 */
package assignment_distributor;

import cs015.tasupport.grading.Constants;
import cs015.tasupport.grading.config.AssignmentType;
import cs015.tasupport.grading.config.ConfigurationManager;
import cs015.tasupport.grading.projects.ProjectManager;
import cs015.tasupport.grading.rubric.RubricManager;
import cs015Database.*;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

/**
 *
 * @author Paul
 */
public class AssignmentDistributorGUI extends javax.swing.JFrame {

    /** Creates new form AssignmentDistributorGUI */
    public AssignmentDistributorGUI() {
        initComponents();
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/cs015Database/accessories-text-editor.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String[] assignNames  = DatabaseInterops.getAssignmentNames();
////        assignmentNameComboBox.removeAllItems();
////        for (int i = 1; i < columnNames.length; i++) {
////            assignmentNameComboBox.insertItemAt(columnNames[i], assignmentNameComboBox.getItemCount());
////        }
        for(String s: DatabaseInterops.getAssignmentNames()) {
            if(DatabaseInterops.getAssignmentType(s) == AssignmentType.PROJECT)
            assignmentNameComboBox.insertItemAt(s, assignmentNameComboBox.getItemCount());
        }
        for(String s: DatabaseInterops.getAssignmentNames())
        if (assignmentNameComboBox.getItemCount() > 0) {
            assignmentNameComboBox.setSelectedIndex(0);
            this.setTitle(assignmentNameComboBox.getSelectedItem() + " - cs015 Assignment Distributor");
        }
        fillTable();
        this.setLocationRelativeTo(null);
    }

    private void fillTable() {

        try {
            mainTable.removeAll();
            mainTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][]{}, new String[]{}));
            mainTable.removeAll();
            DefaultTableModel m = (DefaultTableModel) mainTable.getModel();
            String[] taNames = ConfigurationManager.getGraderLogins();//DatabaseInterops.getTANames();
            m.addColumn("TA Login");
            m.addColumn("Max Number to Grade");
            for (String s : taNames) {
                m.addRow(new String[] {s, "-1"});
            }

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

        assignmentNameComboBox = new javax.swing.JComboBox();
        generateDistButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mainTable = new assignment_distributor.AssignmentDistributorTable();
        setupGradingButton = new javax.swing.JButton();
        mainMenuBar = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        assignmentNameComboBox.setFocusable(false);
        assignmentNameComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignmentNameComboBoxActionPerformed(evt);
            }
        });

        generateDistButton.setText("1. Distribute Students");
        generateDistButton.setFocusable(false);
        generateDistButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateDistButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("A negative value indicates no max number to grade.");

        mainTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(mainTable);

        setupGradingButton.setText("2. Set Up Grading");
        setupGradingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setupGradingButtonActionPerformed(evt);
            }
        });

        jMenu1.setText("File");
        mainMenuBar.add(jMenu1);

        jMenu2.setText("Edit");
        mainMenuBar.add(jMenu2);

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 671, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(assignmentNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(generateDistButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setupGradingButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(assignmentNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generateDistButton)
                    .addComponent(setupGradingButton))
                .addGap(14, 14, 14))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void assignmentNameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignmentNameComboBoxActionPerformed
        this.setTitle(assignmentNameComboBox.getSelectedItem() + " - cs015 Assignment Distributor");
        fillTable();
    }//GEN-LAST:event_assignmentNameComboBoxActionPerformed

    private void generateDistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateDistButtonActionPerformed

        String[] studNames = ProjectManager.getHandinLogins(ProjectManager.getProjectFromString((String)assignmentNameComboBox.getSelectedItem())).toArray(new String[0]);//DatabaseInterops.getStudentNames();

        List<String> shuffleList = Arrays.asList(studNames);
        Collections.shuffle(shuffleList);
        studNames = shuffleList.toArray(new String[0]);
        String[] taNames = ConfigurationManager.getGraderLogins();
        DefaultTableModel m = (DefaultTableModel) mainTable.getModel();
        String[] studentsToGrade = new String[taNames.length];
        Arrays.fill(studentsToGrade, "");
        String[] tasWithBlacklist = DatabaseInterops.getColumnData("taLogin", "blacklist");
        ArrayDeque<String> ad = new ArrayDeque<String>();
        for (String s : studNames) {
            ad.add(s);
        }
        int index = (int) (Math.random() * taNames.length);
        int loopCount = 0;
        Arrays.sort(tasWithBlacklist);
        while (!ad.isEmpty()) {
            if (Arrays.binarySearch(tasWithBlacklist, taNames[index % studentsToGrade.length]) >= 0) {
                String blacklistedStuds = getBlacklist(taNames[index % studentsToGrade.length]);
                if (blacklistedStuds != null) {
                    if (blacklistedStuds.contains(ad.peekLast())) {
                        ad.addFirst(ad.pollLast());
                        loopCount++;
                        if (loopCount == ad.size()) {
                            index++;
                        }
                        continue;
                    }
                }
            }

            if (studentsToGrade[index % studentsToGrade.length].split(",").length == Integer.parseInt(m.getValueAt(index % studentsToGrade.length, 1).toString())) {
                index++;
                continue;
            }
            if (studentsToGrade[index % studentsToGrade.length].isEmpty()) {
                studentsToGrade[index++ % studentsToGrade.length] += ad.pollLast();
            } else {
                studentsToGrade[index++ % studentsToGrade.length] += ", " + ad.pollLast();
            }
            loopCount = 0;
        }
        try {
            String[] colNames = DatabaseInterops.getColumnNames("assignment_dist");
            int colIndex = 0;
            for (int i = 0; i < colNames.length; i++, colIndex++) {
                if (colNames[i].compareToIgnoreCase((String) assignmentNameComboBox.getSelectedItem()) == 0) {
                    break;
                }
            }
            for (int i = 0; i < taNames.length; i++) {
                long rowid = DatabaseInterops.getRowID("assignment_dist", "ta_login_dist", taNames[i]);
                Object[] o = DatabaseInterops.getDataRow("assignment_dist", rowid);
                o[colIndex] = studentsToGrade[i];
                DatabaseInterops.update(rowid, "assignment_dist", o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_generateDistButtonActionPerformed

    private void setupGradingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setupGradingButtonActionPerformed
        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png"); // NOI18N
        String input = (String)JOptionPane.showInputDialog(new JFrame(),"Enter minutes of leniency:","Set Grace Period",JOptionPane.PLAIN_MESSAGE,icon,null,"");
        int minsLeniency = Constants.MINUTES_OF_LENIENCY;
        if ((input != null) && (input.length() != 0)) {
            minsLeniency = Integer.parseInt(input);
        }

        //for (String taLogin : ConfigurationManager.getGraderLogins()) {
        String taLogin = "jeldridg";
            String[] studsToGrade = DatabaseInterops.getStudentsToGrade(taLogin, (String)assignmentNameComboBox.getSelectedItem());
            for (String stud : studsToGrade) {
                RubricManager.assignXMLToGrader(ProjectManager.getProjectFromString((String)assignmentNameComboBox.getSelectedItem()), stud, taLogin, DatabaseInterops.getDQScore((String)assignmentNameComboBox.getSelectedItem(), stud), minsLeniency);
            }
       // }
}//GEN-LAST:event_setupGradingButtonActionPerformed

    private String getBlacklist(String taName) {
        try {
            ISqlJetCursor cursor = DatabaseInterops.getData("blacklist", "ta_blist_logins", taName);
            return cursor.getString("studLogins");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new AssignmentDistributorGUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox assignmentNameComboBox;
    private javax.swing.JButton generateDistButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuBar mainMenuBar;
    private assignment_distributor.AssignmentDistributorTable mainTable;
    private javax.swing.JButton setupGradingButton;
    // End of variables declaration//GEN-END:variables
}
