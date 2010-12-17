/*
 * ExemptionView.java
 *
 * Created on January 19, 2010, 12:49 AM
 */

package gradesystem.views.backend;

import gradesystem.config.Assignment;
import gradesystem.config.Part;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import gradesystem.Allocator;

/**
 *
 * @author  jveldridge
 */
public class ExemptionView extends javax.swing.JFrame {

    private Iterable<Assignment> _assignments;
    private Iterable<String> _students;
    private Collection<Part> _parts;
    
    /** Creates new form ExemptionView */
    public ExemptionView(Iterable<Assignment> assignments, Iterable<String> students) {
        initComponents();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
       
        _assignments = assignments;
        _students = students;
        _parts = new Vector<Part>();
        
        this.updateGrantGUI();
        
        
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

        studentSelectionBG = new javax.swing.ButtonGroup();
        tabbedPane = new javax.swing.JTabbedPane();
        grantViewPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        grantButton = new javax.swing.JButton();
        grantSP = new javax.swing.JScrollPane();
        grantPanel = new javax.swing.JPanel();
        viewPanel = new javax.swing.JPanel();
        viewByCombo = new javax.swing.JComboBox();
        viewCardPanel = new javax.swing.JPanel();
        assignmentSP = new javax.swing.JScrollPane();
        asgnPanel = new javax.swing.JPanel();
        studsSelectedButton = new javax.swing.JRadioButton();
        allStudsButton = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form"); // NOI18N

        tabbedPane.setName("tabbedPane"); // NOI18N
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _parts = new Vector<Part>();
                updateGUI();
            }
        });

        grantViewPanel.setName("grantViewPanel"); // NOI18N

        jPanel1.setName("grantViewPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(ExemptionView.class);
        grantButton.setText(resourceMap.getString("grantButton.text")); // NOI18N
        grantButton.setName("grantButton"); // NOI18N
        grantButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grantButtonActionPerformed(evt);
            }
        });

        grantSP.setName("grantSP"); // NOI18N

        grantPanel.setName("grantPanel"); // NOI18N

        javax.swing.GroupLayout grantPanelLayout = new javax.swing.GroupLayout(grantPanel);
        grantPanel.setLayout(grantPanelLayout);
        grantPanelLayout.setHorizontalGroup(
            grantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
        );
        grantPanelLayout.setVerticalGroup(
            grantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 338, Short.MAX_VALUE)
        );

        grantSP.setViewportView(grantPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(211, 211, 211)
                .addComponent(grantButton)
                .addContainerGap(257, Short.MAX_VALUE))
            .addComponent(grantSP, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(grantSP, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(grantButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout grantViewPanelLayout = new javax.swing.GroupLayout(grantViewPanel);
        grantViewPanel.setLayout(grantViewPanelLayout);
        grantViewPanelLayout.setHorizontalGroup(
            grantViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        grantViewPanelLayout.setVerticalGroup(
            grantViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tabbedPane.addTab(resourceMap.getString("grantViewPanel.TabConstraints.tabTitle"), grantViewPanel); // NOI18N

        viewPanel.setName("viewPanel"); // NOI18N

        viewByCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "View By Assignment", "View By Student" }));
        viewByCombo.setName("viewByCombo"); // NOI18N

        viewCardPanel.setName("viewCardPanel"); // NOI18N
        viewCardPanel.setLayout(new java.awt.CardLayout());

        assignmentSP.setName("assignmentSP"); // NOI18N

        asgnPanel.setName("asgnPanel"); // NOI18N

        javax.swing.GroupLayout asgnPanelLayout = new javax.swing.GroupLayout(asgnPanel);
        asgnPanel.setLayout(asgnPanelLayout);
        asgnPanelLayout.setHorizontalGroup(
            asgnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 566, Short.MAX_VALUE)
        );
        asgnPanelLayout.setVerticalGroup(
            asgnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 331, Short.MAX_VALUE)
        );

        assignmentSP.setViewportView(asgnPanel);

        viewCardPanel.add(assignmentSP, "card2");

        studentSelectionBG.add(studsSelectedButton);
        studsSelectedButton.setText(resourceMap.getString("studsSelectedButton.text")); // NOI18N
        studsSelectedButton.setName("studsSelectedButton"); // NOI18N

        studentSelectionBG.add(allStudsButton);
        allStudsButton.setText(resourceMap.getString("allStudsButton.text")); // NOI18N
        allStudsButton.setName("allStudsButton"); // NOI18N

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(viewPanelLayout.createSequentialGroup()
                        .addComponent(viewByCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 138, Short.MAX_VALUE)
                        .addComponent(allStudsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(studsSelectedButton))
                    .addGroup(viewPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(viewCardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)))
                .addContainerGap())
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewByCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(studsSelectedButton)
                    .addComponent(allStudsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewCardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab(resourceMap.getString("viewPanel.TabConstraints.tabTitle"), viewPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void grantButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grantButtonActionPerformed
    for (Part p : _parts) {
        for (String student : _students) {
            JTextField reason = new JTextField();
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0,1));
            panel.add(new JLabel("Enter reason for granting extension for student " 
                                  + student + " for " + p.getAssignment().getName()
                                  + ": " + p.getName()));
            panel.add(reason);
            int num = JOptionPane.showConfirmDialog(null, panel, "Enter reason for exemption", JOptionPane.OK_CANCEL_OPTION);
            if (num == JOptionPane.OK_OPTION) {
                Allocator.getDatabaseIO().grantExemption(student, p, reason.getText());
            }
        }
    }
}//GEN-LAST:event_grantButtonActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ExemptionView(Allocator.getCourseInfo().getHandinAssignments(), Allocator.getDatabaseIO().getEnabledStudents().keySet()).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allStudsButton;
    private javax.swing.JPanel asgnPanel;
    private javax.swing.JScrollPane assignmentSP;
    private javax.swing.JButton grantButton;
    private javax.swing.JPanel grantPanel;
    private javax.swing.JScrollPane grantSP;
    private javax.swing.JPanel grantViewPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.ButtonGroup studentSelectionBG;
    private javax.swing.JRadioButton studsSelectedButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JComboBox viewByCombo;
    private javax.swing.JPanel viewCardPanel;
    private javax.swing.JPanel viewPanel;
    // End of variables declaration//GEN-END:variables

    private void updateGUI() {
        if (tabbedPane.getSelectedComponent().getName().equals("grantViewPanel")) {
            this.updateGrantGUI();
        }
        else {
            this.updateViewGUI();
        }
    }
    
    private void updateGrantGUI() {
        if (_assignments != null) {
        javax.swing.GroupLayout grantLayout = new javax.swing.GroupLayout(grantPanel);
        grantPanel.setLayout(grantLayout);
        ParallelGroup pg = grantLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
        
        
        Vector<JCheckBox> boxes = new Vector<JCheckBox>();
        for (Assignment a : _assignments) {
            for (final Part p : a.getParts()) {
                _parts.add(p);
                final JCheckBox box = new JCheckBox(a.getName() + ": " + p.getName());
                box.setSelected(true);
                box.setVisible(true);
                boxes.add(box);
                pg.addComponent(box, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE);

                box.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (box.isSelected()) {
                            _parts.add(p);
                        }
                        else {
                            _parts.remove(p);
                        }
                    }
                });
            }
        }
        
        grantLayout.setHorizontalGroup(pg);
        SequentialGroup sg = grantLayout.createSequentialGroup();
        
        for (JCheckBox box : boxes) {
                sg.addComponent(box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
                sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        }
        
        grantLayout.setVerticalGroup(grantLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(sg));
        }
    }
    
    private void updateViewGUI() {
        asgnPanel.removeAll();
        javax.swing.GroupLayout asgnLayout = new javax.swing.GroupLayout(asgnPanel);
        asgnPanel.setLayout(asgnLayout);
        ParallelGroup pg = asgnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);

        Map<Assignment,JLabel> asgnLabelMap = new HashMap<Assignment,JLabel>();
        Map<Part,JLabel> partLabelMap = new HashMap<Part,JLabel>();
        Map<Part,Collection<JPanel>> studLabelMap = new HashMap<Part,Collection<JPanel>>();
        
        if (_assignments != null) {
        for (Assignment a : _assignments) {
            JLabel aLab = new JLabel(a.getName());
            pg.addComponent(aLab, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE);
            asgnLabelMap.put(a, aLab);
            for (final Part p : a.getParts()) {
                JLabel pLab = new JLabel("\t" + p.getName());
                pg.addComponent(pLab, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE);
                partLabelMap.put(p, pLab);
                Vector<JPanel> v = new Vector<JPanel>();
                for (final String s : _students) {
                    String note = Allocator.getDatabaseIO().getExemptionNote(s, p);
                    if (note != null) {
                        JPanel sPan = new JPanel();
                        JLabel sLab = new JLabel("\t\t" + s + ": " + note);
                        pg.addComponent(sPan, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE);
                        sPan.add(sLab);
                        JButton delete = new JButton("Delete");
                        delete.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                Allocator.getDatabaseIO().removeExemption(s, p);
                                asgnPanel.removeAll();
                                updateViewGUI();
                            }
                            
                        });
                        sPan.add(delete);
                        v.add(sPan);
                    }
                }
                studLabelMap.put(p, v);
            }
        
        }

        asgnLayout.setHorizontalGroup(pg);
        SequentialGroup sg = asgnLayout.createSequentialGroup();
        
        for (Assignment a : asgnLabelMap.keySet()) {
            sg.addComponent(asgnLabelMap.get(a), javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
            for (Part p : a.getParts()) {
                sg.addComponent(partLabelMap.get(p), javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
                sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
                for (JPanel sLab : studLabelMap.get(p)) {
                    sg.addComponent(sLab, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
                    sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
                }
            }
        }

        asgnLayout.setVerticalGroup(asgnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(sg));
        }
    }

}
