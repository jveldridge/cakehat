/*
 * ReassignView.java
 *
 * Created on Nov 26, 2009, 7:48:08 PM
 */

package backend.assignmentdist;

import config.Assignment;
import config.TA;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JOptionPane;
import utils.Allocator;
import utils.ErrorView;

/**
 *
 * @author jeldridg
 */
public class ReassignView extends javax.swing.JFrame {

    private Assignment _asgn;
    private Vector<String> _unassignedStudents;

    /** Creates new form ReassignView */
    public ReassignView(Assignment asgn) {
        this.initComponents();
        _asgn = asgn;
        for (Assignment s : Allocator.getCourseInfo().getHandinAssignments()) {
            asgnComboBox.insertItemAt(s, asgnComboBox.getItemCount());
        }
        
        TA[] tas = Allocator.getCourseInfo().getTAs().toArray(new TA[0]);

        fromTAList.setListData(tas);

        toTAList.setListData(tas);

        //create dist for assignment passed in as parameter
        if (asgn != null) {
            asgnComboBox.setSelectedItem(asgn);
            this.setTitle(asgnComboBox.getSelectedItem() + " - " + Allocator.getCourseInfo().getCourse() +" Assignment Distributor");
        }
        
        _unassignedStudents = new Vector<String>(_asgn.getHandinPart().getHandinLogins());
        for (TA ta : tas) {
            for (String login : Allocator.getDatabaseIO().getStudentsAssigned(_asgn.getHandinPart(), ta.getLogin())) {
                _unassignedStudents.remove(login);
            }
        }

        manualDistrb.setSelected(true);
        this.updateGUI();
        
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fromButtonGroup = new javax.swing.ButtonGroup();
        XMLButtonGroup = new javax.swing.ButtonGroup();
        jScrollPane2 = new javax.swing.JScrollPane();
        fromTAList = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        fromStudentList = new javax.swing.JList();
        fromStudentLabel = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        toTAList = new javax.swing.JList();
        toTALabel = new javax.swing.JLabel();
        toStudentsLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        studentFilter = new javax.swing.JTextField();
        reassignFromrb = new javax.swing.JRadioButton();
        manualDistrb = new javax.swing.JRadioButton();
        asgnComboBox = new javax.swing.JComboBox();
        cardPanel = new javax.swing.JPanel();
        reassignPanel = new javax.swing.JPanel();
        assignButton = new javax.swing.JButton();
        newXMLRadioButton = new javax.swing.JRadioButton();
        keepXMLRadioButton = new javax.swing.JRadioButton();
        manualDistPanel = new javax.swing.JPanel();
        numStudentsSpinner = new javax.swing.JSpinner();
        randomAssignButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        selectedAssign = new javax.swing.JButton();
        unassignButton = new javax.swing.JButton();
        remainingLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jScrollPane5 = new javax.swing.JScrollPane();
        toStudentList = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        fromTAList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fromTAList.setName("fromTAList"); // NOI18N
        fromTAList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fromTAListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(fromTAList);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        fromStudentList.setName("fromStudentList"); // NOI18N
        jScrollPane3.setViewportView(fromStudentList);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(ReassignView.class);
        fromStudentLabel.setText(resourceMap.getString("fromStudentLabel.text")); // NOI18N
        fromStudentLabel.setName("fromStudentLabel"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        toTAList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        toTAList.setName("toTAList"); // NOI18N
        toTAList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                toTAListValueChanged(evt);
            }
        });
        toTAList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                toTAListKeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(toTAList);

        toTALabel.setText(resourceMap.getString("toTALabel.text")); // NOI18N
        toTALabel.setName("toTALabel"); // NOI18N

        toStudentsLabel.setText(resourceMap.getString("toStudentsLabel.text")); // NOI18N
        toStudentsLabel.setName("toStudentsLabel"); // NOI18N

        titleLabel.setText(resourceMap.getString("titleLabel.text")); // NOI18N
        titleLabel.setName("titleLabel"); // NOI18N

        studentFilter.setText(resourceMap.getString("studentFilter.text")); // NOI18N
        studentFilter.setName("studentFilter"); // NOI18N
        studentFilter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                studentFilterKeyReleased(evt);
            }
        });

        fromButtonGroup.add(reassignFromrb);
        reassignFromrb.setText(resourceMap.getString("reassignFromrb.text")); // NOI18N
        reassignFromrb.setName("reassignFromrb"); // NOI18N
        reassignFromrb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ReassignView.this.updateGUI();
            }
        });

        fromButtonGroup.add(manualDistrb);
        manualDistrb.setText(resourceMap.getString("manualDistrb.text")); // NOI18N
        manualDistrb.setName("manualDistrb"); // NOI18N
        manualDistrb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ReassignView.this.updateGUI();
            }
        });

        asgnComboBox.setName("asgnComboBox"); // NOI18N
        asgnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                asgnComboBoxActionPerformed(evt);
            }
        });

        cardPanel.setName("cardPanel"); // NOI18N
        cardPanel.setLayout(new java.awt.CardLayout());

        reassignPanel.setName("reassignPanel"); // NOI18N

        assignButton.setText(resourceMap.getString("assignButton.text")); // NOI18N
        assignButton.setName("assignButton");
        assignButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                reassignButtonActionPerformed();
            }
        });

        assignButton.addKeyListener(new KeyAdapter() {
            public void KeyReleased(KeyEvent evt) {
                reassignButtonActionPerformed();
            }
        });

        XMLButtonGroup.add(newXMLRadioButton);
        newXMLRadioButton.setText(resourceMap.getString("newXMLRadioButton.text")); // NOI18N
        newXMLRadioButton.setName("newXMLRadioButton"); // NOI18N

        XMLButtonGroup.add(keepXMLRadioButton);
        keepXMLRadioButton.setSelected(true);
        keepXMLRadioButton.setText(resourceMap.getString("keepXMLRadioButton.text")); // NOI18N
        keepXMLRadioButton.setName("keepXMLRadioButton"); // NOI18N

        javax.swing.GroupLayout reassignPanelLayout = new javax.swing.GroupLayout(reassignPanel);
        reassignPanel.setLayout(reassignPanelLayout);
        reassignPanelLayout.setHorizontalGroup(
            reassignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reassignPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(reassignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(newXMLRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keepXMLRadioButton)
                    .addComponent(assignButton, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        reassignPanelLayout.setVerticalGroup(
            reassignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reassignPanelLayout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addComponent(assignButton)
                .addGap(18, 18, 18)
                .addComponent(keepXMLRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newXMLRadioButton)
                .addContainerGap(130, Short.MAX_VALUE))
        );

        cardPanel.add(reassignPanel, "reassignCard");

        manualDistPanel.setName("manualDistPanel"); // NOI18N
        manualDistPanel.setPreferredSize(new java.awt.Dimension(200, 297));

        numStudentsSpinner.setName("numStudentsSpinner"); // NOI18N

        randomAssignButton.setText(resourceMap.getString("randomAssignButton.text")); // NOI18N
        randomAssignButton.setName("randomAssignButton"); // NOI18N
        randomAssignButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                randomAssignButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        selectedAssign.setText(resourceMap.getString("selectedAssign.text")); // NOI18N
        selectedAssign.setName("selectedAssign"); // NOI18N
        selectedAssign.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                assignButtonActionPerformed();
            }
        });
        selectedAssign.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                assignButtonActionPerformed();
            }
        });

        unassignButton.setText(resourceMap.getString("unassignButton.text")); // NOI18N
        unassignButton.setName("unassignButton"); // NOI18N
        unassignButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                unassignButtonActionPerformed();
            }
        });
        unassignButton.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                unassignButtonActionPerformed();
            }
        });

        remainingLabel.setText(resourceMap.getString("remainingLabel.text")); // NOI18N
        remainingLabel.setName("remainingLabel"); // NOI18N

        jSeparator2.setName("jSeparator2"); // NOI18N

        javax.swing.GroupLayout manualDistPanelLayout = new javax.swing.GroupLayout(manualDistPanel);
        manualDistPanel.setLayout(manualDistPanelLayout);
        manualDistPanelLayout.setHorizontalGroup(
            manualDistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manualDistPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manualDistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(manualDistPanelLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(remainingLabel))
                    .addGroup(manualDistPanelLayout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(manualDistPanelLayout.createSequentialGroup()
                        .addComponent(numStudentsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(randomAssignButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(manualDistPanelLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(manualDistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(selectedAssign, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(unassignButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1))))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        manualDistPanelLayout.setVerticalGroup(
            manualDistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manualDistPanelLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedAssign)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unassignButton)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(manualDistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(randomAssignButton)
                    .addComponent(numStudentsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remainingLabel)
                .addContainerGap(64, Short.MAX_VALUE))
        );

        cardPanel.add(manualDistPanel, "manualDistCard");

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        toStudentList.setName("toStudentList"); // NOI18N
        jScrollPane5.setViewportView(toStudentList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(asgnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reassignFromrb)
                            .addComponent(manualDistrb))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fromStudentLabel)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(studentFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(toTALabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(toStudentsLabel)
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(19, 19, 19))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(asgnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(manualDistrb)
                            .addComponent(fromStudentLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(studentFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reassignFromrb)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(toStudentsLabel)
                        .addComponent(toTALabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addComponent(cardPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void assignButtonActionPerformed() {
        TA newTA = (TA) toTAList.getSelectedValue();
        String student = (String) fromStudentList.getSelectedValue();
        if (student != null) {
            if (this.studentOnTAsBlacklist(student, newTA)) {
                if (JOptionPane.showConfirmDialog(null, "Student " + student + " is on TA "
                                                + newTA.getLogin() + "'s blacklist.  Continue?", 
                                                "Distribute Blacklisted Student?", 
                                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    studentFilter.requestFocus();
                    return;
                }
            }
            if (this.groupMemberOnTAsBlacklist(student, newTA)) {
                if (JOptionPane.showConfirmDialog(null, "A member of " + student + "'s group is on TA "
                                                + newTA.getLogin() + "'s blacklist.  Continue?",
                                                "Distribute Blacklisted Student?",
                                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    studentFilter.requestFocus();
                    return;
                }
            }
            Allocator.getDatabaseIO().assignStudentToGrader(student, _asgn.getHandinPart(), newTA.getLogin());
            Map<String,Collection<String>> dist = new HashMap<String,Collection<String>>();
            Vector<String> assigned = new Vector<String>();
            assigned.add(student);
            dist.put(newTA.getLogin(), assigned);
            Allocator.getRubricManager().distributeRubrics(_asgn.getHandinPart(), dist, Allocator.getCourseInfo().getMinutesOfLeniency());

            _unassignedStudents.remove(student);
            this.updateGUI();
            studentFilter.setText("");
            studentFilter.requestFocus();
        }
    }
    
    private void reassignButtonActionPerformed() {
        TA oldTA = (TA) fromTAList.getSelectedValue();
        TA newTA = (TA) toTAList.getSelectedValue();
        String student = (String) fromStudentList.getSelectedValue();
        if (student != null) {
            if (this.studentOnTAsBlacklist(student, newTA)) {
                if (JOptionPane.showConfirmDialog(null, "Student " + student + " is on TA "
                                                + newTA.getLogin() + "'s blacklist.  Continue?", 
                                                "Distribute Blacklisted Student?", 
                                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    studentFilter.requestFocus();
                    return;
                }
            }
            if (this.groupMemberOnTAsBlacklist(student, newTA)) {
                if (JOptionPane.showConfirmDialog(null, "A member of " + student + "'s group is on TA "
                                                + newTA.getLogin() + "'s blacklist.  Continue?",
                                                "Distribute Blacklisted Student?",
                                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    studentFilter.requestFocus();
                    return;
                }
            }
            Allocator.getDatabaseIO().unassignStudentFromGrader(student, _asgn.getHandinPart(), oldTA.getLogin());
            Allocator.getDatabaseIO().assignStudentToGrader(student, _asgn.getHandinPart(), newTA.getLogin());
        
            if (keepXMLRadioButton.isSelected()) {
                    Allocator.getRubricManager().reassignRubric(_asgn.getHandinPart(), student, newTA.getLogin());
            }
            else {
                Map<String,Collection<String>> dist = new HashMap<String,Collection<String>>();
                Vector<String> assigned = new Vector<String>();
                assigned.add(student);
                dist.put(oldTA.getLogin(), assigned);
                Allocator.getRubricManager().distributeRubrics(_asgn.getHandinPart(), dist, Allocator.getCourseInfo().getMinutesOfLeniency());
            }
            
            this.updateGUI();
        }
    }
    
    private void unassignButtonActionPerformed() {
        TA newTA = (TA) toTAList.getSelectedValue();
        String student = (String) toStudentList.getSelectedValue();
        if (student != null) {
            Allocator.getDatabaseIO().unassignStudentFromGrader(student, _asgn.getHandinPart(), newTA.getLogin());
            _unassignedStudents.add(student);
        }
        this.updateGUI();
        toStudentList.setSelectedIndex(0);
        unassignButton.requestFocus();
    }
    
    
private void studentFilterKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_studentFilterKeyReleased
    //term to filter against
        String filterTerm = studentFilter.getText();

        List<String> matchingLogins;
        //if no filter term, include all logins
        String[] students = _unassignedStudents.toArray(new String[0]);
        if(filterTerm.isEmpty()) {
            matchingLogins = Arrays.asList(students);
        }
        //otherwise compared against beginning of each login
        else {
            matchingLogins = new Vector<String>();
            for(String login : students){
                if(login.startsWith(filterTerm)){
                    matchingLogins.add(login);
                }
            }
        }

        //display matching logins
        fromStudentList.setListData(matchingLogins.toArray());
        fromStudentList.setSelectedIndex(0);

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (matchingLogins.size() > 0) {
                studentFilter.setText(matchingLogins.get(0));
                toTAList.requestFocus();
            }
        }
}//GEN-LAST:event_studentFilterKeyReleased

private void toTAListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_toTAListKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            selectedAssign.requestFocus();
        }
}//GEN-LAST:event_toTAListKeyReleased

private void asgnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_asgnComboBoxActionPerformed
    boolean resolved = Allocator.getGradingUtilities().resolveMissingStudents((Assignment) asgnComboBox.getSelectedItem());

    if (resolved) {
        _asgn = (Assignment) asgnComboBox.getSelectedItem();
        _unassignedStudents = new Vector<String>(_asgn.getHandinPart().getHandinLogins());
        for (TA ta : Allocator.getCourseInfo().getTAs()) {
            for (String login : Allocator.getDatabaseIO().getStudentsAssigned(_asgn.getHandinPart(), ta.getLogin())) {
                _unassignedStudents.remove(login);
            }
        }
        this.updateGUI();
    } else {
        asgnComboBox.setSelectedItem(_asgn);
    }
}//GEN-LAST:event_asgnComboBoxActionPerformed

private void randomAssignButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_randomAssignButtonActionPerformed
    TA ta = (TA) toTAList.getSelectedValue();
    Collections.shuffle(_unassignedStudents);
    Deque<String> students = new ArrayDeque<String>(_unassignedStudents);
    String firstStud = students.getFirst();
    int timesSeenFirst = 0;
    for (int i = 0; i < (Integer) numStudentsSpinner.getValue(); i++) {
        if (!students.isEmpty()) {
            while (this.studentOnTAsBlacklist(students.getFirst(), ta) || this.groupMemberOnTAsBlacklist(students.getFirst(), ta)) {
                if (students.getFirst().equals(firstStud)) {
                    timesSeenFirst++;
                }
                if (timesSeenFirst == 2) {
                    new ErrorView(new Exception(), "Cannot assign this many students " +
                                  "without violating the blacklist.\nIf you would like to " +
                                  "override the blacklist, please manually select students" +
                                  "to be distributed.\n");
                    this.updateGUI();
                    return;
                }
                students.addLast(students.removeFirst());
            }
            Allocator.getDatabaseIO().assignStudentToGrader(students.removeFirst(), _asgn.getHandinPart(), ta.getLogin());
        }
    }
    
    this.updateGUI();
}//GEN-LAST:event_randomAssignButtonActionPerformed

private void fromTAListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fromTAListValueChanged
    if (fromTAList.isEnabled()) {
            fromStudentList.setListData(Allocator.getDatabaseIO().getStudentsAssigned(_asgn.getHandinPart(),
                                        ((TA)fromTAList.getSelectedValue()).getLogin()).toArray(new String[0]));
        }
}//GEN-LAST:event_fromTAListValueChanged

private void toTAListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_toTAListValueChanged
    toStudentList.setListData(Allocator.getDatabaseIO().getStudentsAssigned(_asgn.getHandinPart(),
            ((TA) toTAList.getSelectedValue()).getLogin()).toArray(new String[0]));
}//GEN-LAST:event_toTAListValueChanged

    private void updateGUI() {
        if (toTAList.getModel().getSize() > 0) {
            if (toTAList.getSelectedValue() == null) {
                toTAList.setSelectedIndex(0);
            }
            toStudentList.setListData(Allocator.getDatabaseIO().getStudentsAssigned(
                        _asgn.getHandinPart(), ((TA)toTAList.getSelectedValue()).getLogin()).toArray(new String[0]));
        }

        CardLayout cl = (CardLayout) cardPanel.getLayout();

        if (manualDistrb.isSelected()) {
            cl.show(cardPanel, "manualDistCard");
            fromTAList.setEnabled(false);
            String[] students = _unassignedStudents.toArray(new String[0]);
            Arrays.sort(students);
            fromStudentList.setListData(students);
            remainingLabel.setText(students.length + " students remaining");
            studentFilter.requestFocus();
        }
        else {
            cl.show(cardPanel, "reassignCard");
            fromTAList.setEnabled(true);
            
            if (fromTAList.getModel().getSize() > 0) {
                if (fromTAList.getSelectedValue() == null) {
                    fromTAList.setSelectedIndex(0);
                }
                fromStudentList.setListData(Allocator.getDatabaseIO().getStudentsAssigned(
                        _asgn.getHandinPart(), ((TA)fromTAList.getSelectedValue()).getLogin()).toArray(new String[0]));
            }
        }
    }

    private boolean studentOnTAsBlacklist(String studentLogin, TA ta) {
        for (String blacklistedStudent : Allocator.getDatabaseIO().getTABlacklist(ta.getLogin())) {
            if (studentLogin.equals(blacklistedStudent)) {
                return true;
            }
        }
        return false;
    }

    private boolean groupMemberOnTAsBlacklist(String studentLogin, TA ta) {
        Collection<String> blackList = Allocator.getDatabaseIO().getTABlacklist(ta.getLogin());
        Collection<String> group = Allocator.getDatabaseIO().getGroup(_asgn.getHandinPart(), studentLogin);
            if (Allocator.getGeneralUtilities().containsAny(blackList, group)) {
                return true;
            }
        return false;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup XMLButtonGroup;
    private javax.swing.JComboBox asgnComboBox;
    private javax.swing.JButton assignButton;
    private javax.swing.JPanel cardPanel;
    private javax.swing.ButtonGroup fromButtonGroup;
    private javax.swing.JLabel fromStudentLabel;
    private javax.swing.JList fromStudentList;
    private javax.swing.JList fromTAList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JRadioButton keepXMLRadioButton;
    private javax.swing.JPanel manualDistPanel;
    private javax.swing.JRadioButton manualDistrb;
    private javax.swing.JRadioButton newXMLRadioButton;
    private javax.swing.JSpinner numStudentsSpinner;
    private javax.swing.JButton randomAssignButton;
    private javax.swing.JRadioButton reassignFromrb;
    private javax.swing.JPanel reassignPanel;
    private javax.swing.JLabel remainingLabel;
    private javax.swing.JButton selectedAssign;
    private javax.swing.JTextField studentFilter;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JList toStudentList;
    private javax.swing.JLabel toStudentsLabel;
    private javax.swing.JLabel toTALabel;
    private javax.swing.JList toTAList;
    private javax.swing.JButton unassignButton;
    // End of variables declaration//GEN-END:variables

}
