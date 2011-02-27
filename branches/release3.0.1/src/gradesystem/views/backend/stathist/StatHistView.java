package gradesystem.views.backend.stathist;

import gradesystem.config.Assignment;
import gradesystem.config.Part;
import java.awt.CardLayout;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import gradesystem.Allocator;
import gradesystem.database.Group;
import gradesystem.resources.icons.IconLoader;
import gradesystem.resources.icons.IconLoader.IconImage;
import gradesystem.resources.icons.IconLoader.IconSize;
import gradesystem.views.shared.ErrorView;

/**
 *
 * @author psastras
 * @author jeldridg
 */
public class StatHistView extends javax.swing.JFrame {

    private Map<Assignment, AssignmentChartPanel> _asgnChartMap;
    private Map<Part, AssignmentChartPanel> _partChartMap;
    private Map<String, StudentChartPanel> _studChartMap;
    private Collection<String> _enabledStudents;
    private List<Assignment> _assignments;
    private Collection<Part> _parts;

    /** Creates new form HistogramView */
    public StatHistView(Collection<Assignment> assignments) {
        _assignments  = new Vector<Assignment>(assignments) {};
        Collections.sort(_assignments);
        _asgnChartMap = new HashMap<Assignment,AssignmentChartPanel>();
        _partChartMap = new HashMap<Part,AssignmentChartPanel>();
        _studChartMap = new HashMap<String,StudentChartPanel>();
        try {
            this.setIconImage(IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.X_OFFICE_DRAWING));
        } catch (Exception e) {}
        try {
            _enabledStudents = Allocator.getDatabaseIO().getEnabledStudents().keySet();
        } catch (SQLException ex) {
            new ErrorView(ex, "Could not retrieve enabled students from the database.");
            _enabledStudents = Collections.emptyList();
        }
        
        initComponents();
        domoreinit();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void domoreinit() {
        
        _parts = new Vector<Part>();
        
        //create AssignmentChartPanels for each Assignment and Part
        for (Assignment a : _assignments) {
            _asgnChartMap.put(a, new AssignmentChartPanel());
            
            for (Part p : a.getParts()) {
                _partChartMap.put(p, new AssignmentChartPanel());
                _parts.add(p);
            }
        }
        
        //create StudentChartPanels for each student
        for (String student : _enabledStudents) {
            _studChartMap.put(student, new StudentChartPanel());
        }
        
        
        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
        chartPanel.setLayout(chartPanelLayout);
        ParallelGroup pg = chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);

        
        for (Assignment a : _assignments) {
            pg.addComponent(_asgnChartMap.get(a), javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE);
            
            for (Part p : a.getParts()) {
                pg.addComponent(_partChartMap.get(p), javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE);
            }
        }

        for (StudentChartPanel scp : _studChartMap.values()) {
            pg.addComponent(scp, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE);
        }

        chartPanelLayout.setHorizontalGroup(pg);
        SequentialGroup sg = chartPanelLayout.createSequentialGroup();
        
        for (Assignment a : _assignments) {
            sg.addComponent(_asgnChartMap.get(a), javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
            
            for (Part p : a.getParts()) {
                sg.addComponent(_partChartMap.get(p), javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
                sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
            }
        }
        
        for (StudentChartPanel scp : _studChartMap.values()) {
            sg.addComponent(scp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        }
        
        
        chartPanelLayout.setVerticalGroup(chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(sg));

        selectViewBox.setSelectedIndex(0);
        assignmentsRb.setSelected(true);
        
        this.updateCharts();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        asgnButtonGroup = new javax.swing.ButtonGroup();
        studButtonGroup = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        chartPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        selectViewBox = new javax.swing.JComboBox();
        cardPanel = new javax.swing.JPanel();
        asgnControlPanel = new javax.swing.JPanel();
        assignmentsRb = new javax.swing.JRadioButton();
        partsRb = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        studentControlPanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(StatHistView.class);
        chartPanel.setBackground(resourceMap.getColor("chartPanel.background")); // NOI18N
        chartPanel.setName("chartPanel"); // NOI18N

        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
        chartPanel.setLayout(chartPanelLayout);
        chartPanelLayout.setHorizontalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1215, Short.MAX_VALUE)
        );
        chartPanelLayout.setVerticalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1654, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(chartPanel);

        optionsPanel.setName("optionsPanel"); // NOI18N

        selectViewBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "By Assignment", "By Student" }));
        selectViewBox.setName("selectViewBox"); // NOI18N
        selectViewBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectViewBoxActionPerformed(evt);
            }
        });

        cardPanel.setName("cardPanel"); // NOI18N
        cardPanel.setLayout(new java.awt.CardLayout());

        asgnControlPanel.setName("asgnControlPanel"); // NOI18N

        asgnButtonGroup.add(assignmentsRb);
        assignmentsRb.setText(resourceMap.getString("assignmentsRb.text")); // NOI18N
        assignmentsRb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateCharts();
            }
        });

        asgnButtonGroup.add(partsRb);
        partsRb.setText(resourceMap.getString("partsRb.text")); // NOI18N
        partsRb.setName("partsRb"); // NOI18N
        partsRb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateCharts();
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout asgnControlPanelLayout = new javax.swing.GroupLayout(asgnControlPanel);
        asgnControlPanel.setLayout(asgnControlPanelLayout);
        asgnControlPanelLayout.setHorizontalGroup(
            asgnControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(asgnControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(asgnControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(assignmentsRb)
                    .addComponent(partsRb))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        asgnControlPanelLayout.setVerticalGroup(
            asgnControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(asgnControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(assignmentsRb)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(partsRb)
                .addContainerGap(326, Short.MAX_VALUE))
        );

        cardPanel.add(asgnControlPanel, "assignment");

        studentControlPanel.setName("studentControlPanel"); // NOI18N

        javax.swing.GroupLayout studentControlPanelLayout = new javax.swing.GroupLayout(studentControlPanel);
        studentControlPanel.setLayout(studentControlPanelLayout);
        studentControlPanelLayout.setHorizontalGroup(
            studentControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 191, Short.MAX_VALUE)
        );
        studentControlPanelLayout.setVerticalGroup(
            studentControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 409, Short.MAX_VALUE)
        );

        cardPanel.add(studentControlPanel, "student");

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cardPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                    .addComponent(selectViewBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 191, Short.MAX_VALUE))
                .addContainerGap())
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(selectViewBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE))
        );

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
            .addGap(0, 1040, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(46, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 518, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(1, 1, 1)
                    .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(20, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void selectViewBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectViewBoxActionPerformed
    this.updateCharts();
}//GEN-LAST:event_selectViewBoxActionPerformed


    private void updateCharts() {
        CardLayout cl = (CardLayout) cardPanel.getLayout();

        //see assignment histograms
        if (selectViewBox.getSelectedItem().equals("By Assignment")) {
            cl.show(cardPanel, "assignment");
            for (StudentChartPanel scp : _studChartMap.values()) {
                scp.setVisible(false);
            }
            
            if (assignmentsRb.isSelected()) {                           //show assignments
                for (Part p : _parts) {
                    _partChartMap.get(p).setVisible(false);
                }
                for (Assignment a : _assignments) {
                    Collection<Group> groups;
                    try {
                        groups = Allocator.getDatabaseIO().getGroupsForAssignment(a);
                    } catch (SQLException ex) {
                        new ErrorView(ex, "Could not get groups for assignment " + a + ".");
                        groups = Collections.emptyList();
                    }

                    AssignmentChartPanel chart = _asgnChartMap.get(a);
                    chart.updateChartData(a, groups);
                    chart.setVisible(true);
                }
            }
            else {                                                      //show parts
                for (Assignment a : _assignments) {
                    _asgnChartMap.get(a).setVisible(false);
                }
                for (Part p : _parts) {
                    Collection<Group> groups;
                    try {
                        groups = Allocator.getDatabaseIO().getGroupsForAssignment(p.getAssignment());
                    } catch (SQLException ex) {
                        new ErrorView(ex, "Could not get groups for assignment " + p.getAssignment() + ".");
                        groups = Collections.emptyList();
                    }

                    AssignmentChartPanel chart = _partChartMap.get(p);
                    chart.updateChartData(p, groups);
                    chart.setVisible(true);
                }
            }
        }

        //see student performance
        else if (selectViewBox.getSelectedItem().equals("By Student")) {
            cl.show(cardPanel, "student");
            for (Assignment a : _assignments) {
                _asgnChartMap.get(a).setVisible(false);
            }
            for (Part p : _parts) {
                _partChartMap.get(p).setVisible(false);
            }
            
            for (String student : _enabledStudents) {
                StudentChartPanel chart = _studChartMap.get(student);
                chart.updateChart(student, _assignments.toArray(new Assignment[0]));
                chart.setVisible(true);
            }
        }
        
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new StatHistView(Allocator.getConfigurationInfo().getHandinAssignments()).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup asgnButtonGroup;
    private javax.swing.JPanel asgnControlPanel;
    private javax.swing.JRadioButton assignmentsRb;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JRadioButton partsRb;
    private javax.swing.JComboBox selectViewBox;
    private javax.swing.ButtonGroup studButtonGroup;
    private javax.swing.JPanel studentControlPanel;
    // End of variables declaration//GEN-END:variables
}
