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

import backend.DatabaseIO;
import frontend.grader.rubric.RubricManager;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JList;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import utils.Assignment;
import utils.AssignmentType;
import utils.ConfigurationManager;
import utils.Constants;
import utils.ErrorView;
import utils.Project;
import utils.ProjectManager;
import utils.Utils;

/**
 *
 * @author jonathan
 */
public class FinalProjectAssigner extends javax.swing.JFrame {

    private String[] _talogins;
    private Map<String, String> _tas;
    private Map<String, String> _studs;
    private HashMap<String, String[]> _blists;
    private HashMap<String, LinkedList<String>> _projHandins;
    private String[] _projNames;
    private int[] _projTotals;
    private Collection<String> _handinsTograde;
    private Thread _namethread;

    /** Creates new form ReassignView */
    public FinalProjectAssigner() {

        initComponents();
        this.setTitle("Final Projects Distributorizer");
        _talogins = DatabaseIO.getTANames();
        _projNames = getFinalProjects().toArray(new String[0]);
        _blists = new HashMap<String, String[]>(_talogins.length);
        try {
            for (String name : _talogins) {
                ISqlJetCursor cursor = DatabaseIO.getData("blacklist", "ta_blist_logins", name);
                String s = cursor.getString("studLogins");
                if (s != null) {
                    String[] blisted = cursor.getString("studLogins").replace(" ", "").split(",");
                    Arrays.sort(blisted);
                    _blists.put(name, blisted);
                } else {
                    _blists.put(name, new String[0]);
                }
                cursor.close();
            }
        } catch (Exception e) {
            new ErrorView(e);
        }

        //add all of the submitted projects to the data structrure
        _projHandins = new HashMap<String, LinkedList<String>>();
        Collection allhandins = Arrays.asList(DatabaseIO.getStudentsAssigned("Final"));
        _projTotals = new int[_projNames.length];
        _handinsTograde = Collections.synchronizedCollection(new LinkedList<String>());
        int i = 0;
        for (String name : _projNames) {
            Collection<String> c = ProjectManager.getHandinLogins(Project.getInstance(name));
            _projTotals[i++] = c.size();
            c.removeAll(allhandins); //Remove students already assigned to tas
            _handinsTograde.addAll(c);
            _projHandins.put(name, new LinkedList<String>(c));
        }
        _tas = Collections.synchronizedMap(new HashMap<String, String>(_talogins.length));
        _studs = Collections.synchronizedMap(new HashMap<String, String>(_handinsTograde.size()));

        //parse names in background
        _namethread = new Thread(new Runnable() {

            public void run() {
                float progress = 0, inc = 100.0f / (float) (_talogins.length + _handinsTograde.size());
                updateStatus("Parsing ta logins and names.", 0, null);
                for (int i = 0; i < _talogins.length; i++) {
                    updateStatus("Parsing ta logins and names (" + _talogins[i] + ")", 0, null);
                    if (_tas.get(_talogins[i]) == null) {
                        _tas.put(_talogins[i], Utils.getUserName(_talogins[i]));
                    }
                    jProgressBar1.setValue((int) (progress += inc));
                }
                updateStatus("Done", 0, null);
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                Iterator i = _handinsTograde.iterator();
                while (i.hasNext()) {
                    String name = (String) i.next();
                    updateStatus("Parsing student logins and names (" + name + ")", 0, null);
                    if (_studs.get(name) == null) {
                        _studs.put(name, Utils.getUserName(name));
                    }
                    jProgressBar1.setValue((int) (progress += inc));
                }
                jProgressBar1.setValue(100);
                updateStatus("Done", 0, null);
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                jProgressBar1.setValue(0);
                updateStatus("Ready", 0, null);
            }
        });
        _namethread.start();

        this.populateLists();
        updateFormComponents();
    }

    public synchronized void updateStatus(String message, int timeout, Color c) {
        jLabel7.setText(message);
        if (c == null) {
            c = java.awt.SystemColor.controlText;
        }
        jLabel7.setForeground(c);
        Timer t = new Timer();
        class NewTask extends TimerTask {

            @Override
            public void run() {
                jLabel7.setText("Ready");
                jLabel7.setForeground(java.awt.SystemColor.controlText);
                this.cancel();
            }
        }
        if (timeout > 0) {
            t.schedule(new NewTask(), timeout);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        assignmentList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        reassignButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        toTAList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        toStudentList = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel8 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        assignmentList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        assignmentList.setName("assignmentList"); // NOI18N
        assignmentList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                assignmentListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(assignmentList);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(FinalProjectAssigner.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        reassignButton.setIcon(resourceMap.getIcon("reassignButton.icon")); // NOI18N
        reassignButton.setText(resourceMap.getString("reassignButton.text")); // NOI18N
        reassignButton.setIconTextGap(10);
        reassignButton.setName("reassignButton"); // NOI18N
        reassignButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reassignButtonActionPerformed(evt);
            }
        });

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        toTAList.setName("toTAList"); // NOI18N
        toTAList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                toTAListMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(toTAList);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        toStudentList.setName("toStudentList"); // NOI18N
        jScrollPane5.setViewportView(toStudentList);

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel6.setText(resourceMap.getString("lbl_numassigned.text")); // NOI18N
        jLabel6.setName("lbl_numassigned"); // NOI18N

        jLabel2.setText(resourceMap.getString("lblStats.text")); // NOI18N
        jLabel2.setName("lblStats"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setName("jList1"); // NOI18N
        jScrollPane2.setViewportView(jList1);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setFocusCycleRoot(true);
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout());

        jLabel7.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel7.setForeground(resourceMap.getColor("jLabel7.foreground")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        jPanel2.add(jLabel7, java.awt.BorderLayout.CENTER);

        jProgressBar1.setFont(resourceMap.getFont("jProgressBar1.font")); // NOI18N
        jProgressBar1.setName("jProgressBar1"); // NOI18N
        jProgressBar1.setStringPainted(true);
        jPanel2.add(jProgressBar1, java.awt.BorderLayout.LINE_END);

        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jSpinner1.setName("jSpinner1"); // NOI18N
        jSpinner1.setValue(1);

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
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
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addComponent(jLabel6)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(reassignButton, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 675, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reassignButton))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 475, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void assignmentListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_assignmentListMouseClicked
        this.updateFormComponents();

    }//GEN-LAST:event_assignmentListMouseClicked

    private void reassignButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reassignButtonActionPerformed
        String taLogin = (String) toTAList.getSelectedValue();
        String projName = (String) assignmentList.getSelectedValue();
        for (int i = 0; i < (Integer)jSpinner1.getValue(); i++) {
            LinkedList<String> logins = (LinkedList<String>) _projHandins.get(projName).clone();
            if (logins.isEmpty()) {
                updateStatus("No more students to assign for the current project", 2000, null);
                return;
            }
            Collections.shuffle(logins);
            while (!logins.isEmpty()) {
                String studLogin = logins.remove();
                if (Arrays.binarySearch(_blists.get(taLogin), studLogin) < 0) { //not in the blacklist so exit
                    _projHandins.get(projName).remove(studLogin); //remove the person!
                    try {
                        ISqlJetCursor cursor = DatabaseIO.getData(DatabaseIO.DISTRIBUTION_TABLE, "ta_login_dist", taLogin);
                        if (cursor != null) {
                            cursor.getRowId();
                            String[] data = (String[]) DatabaseIO.getDataRow(DatabaseIO.DISTRIBUTION_TABLE, cursor.getRowId());
                            if (data[data.length - 1] != null && data[data.length - 1].trim().length() > 1) {
                                data[data.length - 1] += ", ";
                            }
                            data[data.length - 1] += studLogin;
                            DatabaseIO.update(cursor.getRowId(), DatabaseIO.DISTRIBUTION_TABLE, (Object[]) data);
                            String taName = _tas.get(taLogin);
                            if (taName == null || taName.trim().length() == 0) {// if null add to synchronized hash map
                                taName = Utils.getUserName(taLogin);
                                _tas.put(taLogin, taName);
                            }
                            String studName = _studs.get(studLogin);
                            if (studName == null || studName.trim().length() == 0) { // if null add to synchronized hash map
                                studName = Utils.getUserName(studLogin);
                                _studs.put(studLogin, studName);
                            }
                            RubricManager.assignXMLToGrader(Project.getInstance(projName), Project.getInstance("Final"), studLogin, taLogin, studName, taName, DatabaseIO.getStudentDQScore("Final", studLogin), Constants.MINUTES_OF_LENIENCY);
                        }
                    } catch (Exception e) {
                        new ErrorView(e);
                        return;
                    }
                    updateFormComponents();
                    updateStatus("Student successfully assigned to grader", 2000, null);
                    break;
                }
            }
            if (logins.isEmpty()) { //if we couldn't add anyone because they're all blacklisted
                updateStatus("The selected grader has blacklisted all of the remaining students for the remaining project.", 3000, Color.red);
                return;
            }
        }

    }//GEN-LAST:event_reassignButtonActionPerformed

    private void toTAListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toTAListMouseClicked
        this.updateFormComponents();
    }//GEN-LAST:event_toTAListMouseClicked

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new FinalProjectAssigner().setVisible(true);
            }
        });
    }

    /**
     * Returns a sorted list of final projects
     * @return
     */
    private ArrayList<String> getFinalProjects() {
        Assignment[] asgns = ConfigurationManager.getAssignments();
        ArrayList<String> finalprojects = new ArrayList<String>();
        for (Assignment a : asgns) {
            if (a.Type.compareTo(AssignmentType.FINAL) == 0) {
                finalprojects.add(a.Name);
            }
        }
        Collections.sort(finalprojects);
        return finalprojects;
    }

    private void populateLists() {
        String[] finalprojs = getFinalProjects().toArray(new String[0]);
        assignmentList.setListData(finalprojs);
        if (assignmentList.getModel().getSize() > 0) {
            assignmentList.setSelectedIndex(0);
        }
        toTAList.setListData(_talogins);
        if (toTAList.getModel().getSize() > 0) {
            toTAList.setSelectedIndex(0);
        }
        this.populateStudentList(toStudentList, (String) toTAList.getSelectedValue());
    }

    private void populateStudentList(JList list, String user) {
        list.setListData(DatabaseIO.getStudentsToGrade(user, "Final"));
    }

    private void updateFormComponents() {
        String[] studs = _projHandins.get(assignmentList.getSelectedValue()).toArray(new String[0]);
        Arrays.sort(studs);
        jList1.setListData(studs);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String projName : _projNames) {
            sb.append(projName);
            sb.append(": ");
            sb.append(_projHandins.get(projName).size() + " / " + _projTotals[i++]);
            sb.append("<br />");
        }
        jLabel2.setText("<html>" + sb.toString() + "</html>");
        this.populateStudentList(toStudentList, (String) toTAList.getSelectedValue());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList assignmentList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JButton reassignButton;
    private javax.swing.JList toStudentList;
    private javax.swing.JList toTAList;
    // End of variables declaration//GEN-END:variables
}
