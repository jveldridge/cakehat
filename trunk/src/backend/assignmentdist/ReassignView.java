/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ReassignView.java
 *
 * Created on Nov 26, 2009, 7:48:08 PM
 */

package backend.assignmentdist;

import backend.OldDatabaseOps;
import config.Assignment;
import rubric.RubricManager;
import javax.swing.JList;
import rubric.RubricException;
import utils.Allocator;
import utils.ErrorView;

/**
 *
 * @author jeldridg
 */
public class ReassignView extends javax.swing.JFrame {

    private Assignment _asgn;

    /** Creates new form ReassignView */
    public ReassignView(Assignment asgn) {
        this.initComponents();
        titleLabel.setText("<html><b>Reassign Grading for Assignment: </b>" + asgn + "</html>");
        this.populateLists();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
        _asgn = asgn;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        jScrollPane2 = new javax.swing.JScrollPane();
        fromTAList = new javax.swing.JList();
        fromTALabel = new javax.swing.JLabel();
        reassignButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        fromStudentList = new javax.swing.JList();
        fromStudentLabel = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        toTAList = new javax.swing.JList();
        toTALabel = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        toStudentList = new javax.swing.JList();
        toStudentsLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        keepXMLRadioButton = new javax.swing.JRadioButton();
        newXMLRadioButton = new javax.swing.JRadioButton();
        studentFilter = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        fromTAList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        fromTAList.setName("fromTAList"); // NOI18N
        fromTAList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fromTAListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(fromTAList);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(ReassignView.class);
        fromTALabel.setText(resourceMap.getString("fromTALabel.text")); // NOI18N
        fromTALabel.setName("fromTALabel"); // NOI18N

        reassignButton.setText(resourceMap.getString("reassignButton.text")); // NOI18N
        reassignButton.setName("reassignButton"); // NOI18N
        reassignButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reassignButtonActionPerformed(evt);
            }
        });

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        fromStudentList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        fromStudentList.setName("fromStudentList"); // NOI18N
        jScrollPane3.setViewportView(fromStudentList);

        fromStudentLabel.setText(resourceMap.getString("fromStudentLabel.text")); // NOI18N
        fromStudentLabel.setName("fromStudentLabel"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        toTAList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        toTAList.setName("toTAList"); // NOI18N
        toTAList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                toTAListMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(toTAList);

        toTALabel.setText(resourceMap.getString("toTALabel.text")); // NOI18N
        toTALabel.setName("toTALabel"); // NOI18N

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        toStudentList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        toStudentList.setName("toStudentList"); // NOI18N
        jScrollPane5.setViewportView(toStudentList);

        toStudentsLabel.setText(resourceMap.getString("toStudentsLabel.text")); // NOI18N
        toStudentsLabel.setName("toStudentsLabel"); // NOI18N

        titleLabel.setText(resourceMap.getString("titleLabel.text")); // NOI18N
        titleLabel.setName("titleLabel"); // NOI18N

        buttonGroup.add(keepXMLRadioButton);
        keepXMLRadioButton.setSelected(true);
        keepXMLRadioButton.setText(resourceMap.getString("keepXMLRadioButton.text")); // NOI18N
        keepXMLRadioButton.setName("keepXMLRadioButton"); // NOI18N

        buttonGroup.add(newXMLRadioButton);
        newXMLRadioButton.setText(resourceMap.getString("newXMLRadioButton.text")); // NOI18N
        newXMLRadioButton.setName("newXMLRadioButton"); // NOI18N

        studentFilter.setText(resourceMap.getString("studentFilter.text")); // NOI18N
        studentFilter.setName("studentFilter"); // NOI18N

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N
        jMenuBar1.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fromTALabel)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(studentFilter)
                            .addComponent(fromStudentLabel)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(reassignButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(newXMLRadioButton)
                                    .addComponent(keepXMLRadioButton))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(toTALabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(toStudentsLabel)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(titleLabel))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fromStudentLabel)
                            .addComponent(fromTALabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(studentFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(toTALabel)
                            .addComponent(toStudentsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE))))
                .addGap(26, 26, 26))
            .addGroup(layout.createSequentialGroup()
                .addGap(143, 143, 143)
                .addComponent(reassignButton)
                .addGap(18, 18, 18)
                .addComponent(keepXMLRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newXMLRadioButton)
                .addContainerGap(154, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fromTAListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fromTAListMouseClicked
        this.updateFormComponents();
    }//GEN-LAST:event_fromTAListMouseClicked

    private void reassignButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reassignButtonActionPerformed
        String oldTA = (String) fromTAList.getSelectedValue();
        String newTA = (String) toTAList.getSelectedValue();
        String student = (String) fromStudentList.getSelectedValue();
        
        //update XML files
        if (keepXMLRadioButton.isSelected()) {
            RubricManager.reassignXML(Allocator.getProject(_asgn.getName()), oldTA, student, newTA);
        }
        else {
            //remove XML for old grader--TODO
            
            //create blank XML for new grader
            try {
                RubricManager.assignXMLToGrader(Allocator.getProject(_asgn.getName()), student, newTA, OldDatabaseOps.getStudentDQScore(_asgn.getName(), student), 10);
            }
            catch(RubricException e) {
                new ErrorView(e);
            }
       }

        //still need to remove old XMLs
    }//GEN-LAST:event_reassignButtonActionPerformed

    private void toTAListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toTAListMouseClicked
        this.updateFormComponents();
    }//GEN-LAST:event_toTAListMouseClicked


    private void populateLists() {

        fromTAList.setListData(OldDatabaseOps.getTANames());
        if (fromTAList.getModel().getSize() > 0) {
            fromTAList.setSelectedIndex(0);
        }

        toTAList.setListData(OldDatabaseOps.getTANames());
        if (toTAList.getModel().getSize() > 0) {
            toTAList.setSelectedIndex(0);
        }

        this.populateStudentList(fromStudentList, (String) fromTAList.getSelectedValue());
        this.populateStudentList(toStudentList, (String) toTAList.getSelectedValue());
    }

    private void populateStudentList(JList list, String user) {
        
    }

    private void updateFormComponents() {
//        assignmentList.setListData(DatabaseIO.getProjectNames());
//        if (assignmentList.getModel().getSize() > 0) {
//            assignmentList.setSelectedIndex(0);
//        }

        this.populateStudentList(fromStudentList, (String) fromTAList.getSelectedValue());
        this.populateStudentList(toStudentList, (String) toTAList.getSelectedValue());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel fromStudentLabel;
    private javax.swing.JList fromStudentList;
    private javax.swing.JLabel fromTALabel;
    private javax.swing.JList fromTAList;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JRadioButton keepXMLRadioButton;
    private javax.swing.JRadioButton newXMLRadioButton;
    private javax.swing.JButton reassignButton;
    private javax.swing.JTextField studentFilter;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JList toStudentList;
    private javax.swing.JLabel toStudentsLabel;
    private javax.swing.JLabel toTALabel;
    private javax.swing.JList toTAList;
    // End of variables declaration//GEN-END:variables

}
