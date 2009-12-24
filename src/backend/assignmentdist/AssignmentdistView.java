/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AssignmentDistributorGUI.java
 *
 * Created on Sep 7, 2009, 12:53:58 PM
 */
package backend.assignmentdist;


import backend.DatabaseIO;
import frontend.grader.rubric.RubricManager;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import utils.AssignmentType;
import utils.ConfigurationManager;
import utils.Constants;
import utils.ErrorView;
import utils.Project;
import utils.ProjectManager;
import utils.Utils;

/**
 *
 * @author Paul
 */
public class AssignmentdistView extends javax.swing.JFrame {

    /** Creates new form AssignmentDistributorGUI */
    public AssignmentdistView() {
        initComponents();
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/accessories-text-editor.png")));
        } catch (Exception e) {
        }

        for(String s: DatabaseIO.getAssignmentNames()) {
            if(DatabaseIO.getAssignmentType(s) == AssignmentType.PROJECT) {
                assignmentNameComboBox.insertItemAt(s, assignmentNameComboBox.getItemCount());
            }
        }
        for(String s: DatabaseIO.getAssignmentNames())
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
                m.addRow(new String[] {s, "0"});
            }

        } catch (Exception e) {
            new ErrorView(e);
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
        mainTable = new backend.assignmentdist.AssignmentdistTable();
        setupGradingButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        mainMenuBar = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMcenu2 = new javax.swing.JMenu();

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

        jButton1.setText("ReAssign Grading");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jMenu1.setText("File");
        mainMenuBar.add(jMenu1);

        jMcenu2.setText("Edit");
        mainMenuBar.add(jMcenu2);

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
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 263, Short.MAX_VALUE)
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
                    .addComponent(setupGradingButton)
                    .addComponent(jButton1))
                .addGap(14, 14, 14))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void assignmentNameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignmentNameComboBoxActionPerformed
        this.setTitle(assignmentNameComboBox.getSelectedItem() + " - cs015 Assignment Distributor");
        fillTable();
    }//GEN-LAST:event_assignmentNameComboBoxActionPerformed

    private void generateDistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateDistButtonActionPerformed
               String asgn = (String)assignmentNameComboBox.getSelectedItem();

        if(!DatabaseIO.isDistEmpty(asgn)) {
            int n = JOptionPane.showConfirmDialog(new JFrame(),"A distribution already exists for " + asgn + ".\nAre you sure you want to overwrite the existing distribution?","Confirm Overwrite",JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return;
}
}

        String[] studNames = ProjectManager.getHandinLogins(Project.getInstance((String)assignmentNameComboBox.getSelectedItem())).toArray(new String[0]);//DatabaseInterops.getStudentNames();

        List<String> shuffledStudents = Arrays.asList(studNames);
        Collections.shuffle(shuffledStudents);
        studNames = shuffledStudents.toArray(new String[0]);

        ArrayDeque<String> students = new ArrayDeque<String>();
        for (String student : studNames) {
            students.add(student);
        }

        System.out.println(students);

        String[] taNames = ConfigurationManager.getGraderLogins();

        HashMap<String,String> distribution = new HashMap<String,String>();
        for (String grader : ConfigurationManager.getGraderLogins()) {
            distribution.put(grader, new String());
}

        double double_ceil_avg = Math.floor((double) studNames.length / (double) taNames.length);
        int avg = (int) double_ceil_avg;

        DefaultTableModel m = (DefaultTableModel) mainTable.getModel();

        Stack<String> gradeFewer = new Stack<String>();
        Stack<String> gradeAvg = new Stack<String>();
        Stack<String> gradeMore = new Stack<String>();

        HashMap<String,Integer> numStudsToGrade = new HashMap<String,Integer>();
        for (int rowCount = 0; rowCount < m.getRowCount(); rowCount++) {
            String taLogin = (String) m.getValueAt(rowCount,0);
            int numToGrade = (Integer) Integer.parseInt(m.getValueAt(rowCount, 1).toString()) + avg;

            numStudsToGrade.put((String) m.getValueAt(rowCount, 0), numToGrade);
            if (numToGrade < avg) {
                gradeFewer.push(taLogin);
}
            else if (numToGrade > avg) {
                gradeMore.push(taLogin);
}
            else {
                gradeAvg.push(taLogin);
}
}

        //String[] tasWithBlacklist = DatabaseIO.getColumnData("taLogin", "blacklist");

        while (!gradeMore.isEmpty()) {
            String ta = gradeMore.pop();
            System.out.println("ta is " + ta);
            String blacklist = this.getBlacklist(ta);
            System.out.println("blacklist: " + blacklist);
            if (blacklist == null) {
                blacklist = "";
            }
            String[] blacklistedStuds = blacklist.split(",");
            Arrays.sort(blacklistedStuds, new StringComparator());
            int numAssigned = 0;
            System.out.println("numToGrade is " + numStudsToGrade.get(ta));
            System.out.println("students is " + students + " and blacklistedStuds is " + blacklistedStuds);
            while (numAssigned <= numStudsToGrade.get(ta)) {
                if (numAssigned != 0) {
                    distribution.put(ta, distribution.get(ta) + ", ");
                }
                while (Arrays.binarySearch((String[]) blacklistedStuds, " " + ((String) students.peekFirst())) >= 0) {
                    System.out.println("while loop 3");
                    String s = students.removeFirst();
                    students.addLast(s);
                }
                if (!students.isEmpty()) {
                    distribution.put(ta, distribution.get(ta) + students.removeFirst());
                    numAssigned++;
                }
                else {
                    break;
                }
            }
        }

         while (!gradeAvg.isEmpty()) {
            String ta = gradeAvg.pop();
            System.out.println("ta is " + ta);
            String blacklist = this.getBlacklist(ta);
            System.out.println("blacklist: " + blacklist);
            if (blacklist == null) {
                blacklist = "";
            }
            String[] blacklistedStuds = blacklist.split(",");
            Arrays.sort(blacklistedStuds, new StringComparator());
            int numAssigned = 0;
            System.out.println("numToGrade is " + numStudsToGrade.get(ta));
            System.out.println("students is " + students + " and blacklistedStuds is " + blacklistedStuds);
            while (numAssigned < numStudsToGrade.get(ta)) {
                if (numAssigned != 0) {
                    distribution.put(ta, distribution.get(ta) + ", ");
                }
                while (Arrays.binarySearch((String[]) blacklistedStuds, " " + ((String) students.peekFirst())) >= 0) {
                    System.out.println("while loop 3");
                    String s = students.removeFirst();
                    students.addLast(s);
                }
                if (!students.isEmpty()) {
                    distribution.put(ta, distribution.get(ta) + students.removeFirst());
                    numAssigned++;
                }
                else {
                    break;
                }
            }
        }

         while (!gradeFewer.isEmpty()) {
            String ta = gradeFewer.pop();
            System.out.println("ta is " + ta);
            String blacklist = this.getBlacklist(ta);
            if (blacklist == null) {
                blacklist = "";
            }
            System.out.println("blacklist: " + blacklist);
            String[] blacklistedStuds = blacklist.split(",");
            Arrays.sort(blacklistedStuds, new StringComparator());
            int numAssigned = 0;
            System.out.println("numToGrade is " + numStudsToGrade.get(ta));
            System.out.println("students is " + students + " and blacklistedStuds is " + blacklistedStuds);
            while (numAssigned <= numStudsToGrade.get(ta)) {
                if (numAssigned != 0) {
                    distribution.put(ta, distribution.get(ta) + ", ");
                }
                while (Arrays.binarySearch((String[]) blacklistedStuds, " " + ((String) students.peekFirst())) >= 0) {
                    System.out.println("while loop 3");
                    String s = students.removeFirst();
                    students.addLast(s);
                }
                if (!students.isEmpty()) {
                    distribution.put(ta, distribution.get(ta) + students.removeFirst());
                    numAssigned++;
                }
                else {
                    break;
                }
            }
        }

        System.out.println("distribution is " + distribution);
        //convert the hashmap to an array of strings to be added to the database using Paul's code
        String[] studentsToGrade = new String[taNames.length];
        int j = 0;
        for (String grader : ConfigurationManager.getGraderLogins()) {
            String temp = distribution.get(grader);
            studentsToGrade[j] = temp;
            j++;
        }


//        String[] studentsToGrade = new String[taNames.length];
//        Arrays.fill(studentsToGrade, "");
//        String[] tasWithBlacklist = DatabaseIO.getColumnData("taLogin", "blacklist");
//        System.out.println(tasWithBlacklist);
//        ArrayDeque<String> ad = new ArrayDeque<String>();
//        for (String s : studNames) {
//            ad.add(s);
//        }
//        int index = (int) (Math.random() * taNames.length);
//        int loopCount = 0;
//        Arrays.sort(tasWithBlacklist);
//        while (!ad.isEmpty()) {
//            if (Arrays.binarySearch(tasWithBlacklist, taNames[index % studentsToGrade.length]) >= 0) {
//                String blacklistedStuds = getBlacklist(taNames[index % studentsToGrade.length]);
//                if (blacklistedStuds != null) {
//                    if (blacklistedStuds.contains(ad.peekLast())) {
//                        ad.addFirst(ad.pollLast());
//                        loopCount++;
//                        if (loopCount == ad.size()) {
//                            index++;
//                        }
//                        continue;
//                    }
//                }
//            }
//
//            if (studentsToGrade[index % studentsToGrade.length].split(",").length == numStudsToGrade.get(m.getValueAt(index % studentsToGrade.length, 0).toString())) {
//                index++;
//                continue;
//            }
////            if (studentsToGrade[index % studentsToGrade.length].split(",").length == Integer.parseInt(m.getValueAt(index % studentsToGrade.length, 1).toString())) {
////                index++;
////                continue;
////            }
//            if (studentsToGrade[index % studentsToGrade.length].isEmpty()) {
//                studentsToGrade[index++ % studentsToGrade.length] += ad.pollLast();
//            } else {
//                studentsToGrade[index++ % studentsToGrade.length] += ", " + ad.pollLast();
//            }
//            loopCount = 0;
//        }


        //database part
        try {
            System.out.println("hello!");
            String[] colNames = DatabaseIO.getColumnNames("assignment_dist");
            int colIndex = 0;
            for (int i = 0; i < colNames.length; i++, colIndex++) {
                if (colNames[i].compareToIgnoreCase((String) assignmentNameComboBox.getSelectedItem()) == 0) {
                    break;
                }
            }
            for (int i = 0; i < taNames.length; i++) {
                long rowid = DatabaseIO.getRowID("assignment_dist", "ta_login_dist", taNames[i]);
                Object[] o = DatabaseIO.getDataRow("assignment_dist", rowid);
                o[colIndex] = studentsToGrade[i];
                DatabaseIO.update(rowid, "assignment_dist", o);
            }
            JOptionPane.showMessageDialog(this, "Assignments have been successfully distributed to the grading TAs.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "There was an error distributing the assignments to the grading TAs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_generateDistButtonActionPerformed

    private void setupGradingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setupGradingButtonActionPerformed
        String asgn = (String)assignmentNameComboBox.getSelectedItem();

        //create grading directories if they do not exist
        for (String taLogin : ConfigurationManager.getTALogins()) {
            String directoryPath = Constants.GRADER_PATH + taLogin + "/" + asgn;
            Utils.makeDirectory(directoryPath);
        }

        ImageIcon icon = new javax.swing.ImageIcon("/gradesystem/resources/icons/32x32/accessories-text-editor.png"); // NOI18N
        String input = (String)JOptionPane.showInputDialog(new JFrame(),"Enter minutes of leniency:","Set Grace Period",JOptionPane.PLAIN_MESSAGE,icon,null,"");
        int minsLeniency = Constants.MINUTES_OF_LENIENCY;
        if ((input != null) && (input.length() != 0)) {
            minsLeniency = Integer.parseInt(input);
        }

        for (String taLogin : ConfigurationManager.getGraderLogins()) {
            String[] studsToGrade = DatabaseIO.getStudentsToGrade(taLogin, (String)assignmentNameComboBox.getSelectedItem());
            for (String stud : studsToGrade) {
                RubricManager.assignXMLToGrader(Project.getInstance((String)assignmentNameComboBox.getSelectedItem()), stud, taLogin, DatabaseIO.getStudentDQScore((String)assignmentNameComboBox.getSelectedItem(), stud), minsLeniency);
            }
       }
}//GEN-LAST:event_setupGradingButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        new ReassignView();
    }//GEN-LAST:event_jButton1ActionPerformed

    private String getBlacklist(String taName) {
        try {
            ISqlJetCursor cursor = DatabaseIO.getData("blacklist", "ta_blist_logins", taName);
            return cursor.getString("studLogins");
        } catch (Exception e) {
            new ErrorView(e);
            return "";
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new AssignmentdistView().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox assignmentNameComboBox;
    private javax.swing.JButton generateDistButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMcenu2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuBar mainMenuBar;
    private backend.assignmentdist.AssignmentdistTable mainTable;
    private javax.swing.JButton setupGradingButton;
    // End of variables declaration//GEN-END:variables
}
