package gradesystem.views.backend;

import gradesystem.config.Assignment;
import gradesystem.config.Part;
import gradesystem.database.CakeHatDBIOException;
import gradesystem.services.ServicesException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;
import gradesystem.Allocator;
import gradesystem.config.TA;
import gradesystem.services.UserServices.ValidityCheck;
import gradesystem.views.shared.ErrorView;
import java.util.Collection;
import java.util.LinkedList;
import utils.system.NativeException;

/**
 *
 * @author jeldridg
 */
public class ConfigManager extends javax.swing.JFrame {

    /** Creates new form ConfigManager */
    public ConfigManager() {
        initComponents();

        for (Assignment s : Allocator.getCourseInfo().getAssignments()) {
            asgnCombo.insertItemAt(s, asgnCombo.getItemCount());
        }

        asgnCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                partCombo.setModel(new DefaultComboBoxModel());
                for (Part p : ((Assignment)asgnCombo.getSelectedItem()).getParts()) {
                    partCombo.insertItemAt(p, partCombo.getItemCount());
                }
            }
        });

        addStudentsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (allStudentsRB.isSelected()) {
                    Collection<String> studentsNotAdded = new LinkedList<String>();
                    try {
                        for (String login : Allocator.getUserServices().getStudentLogins()) {
                            try {
                                Allocator.getUserServices().addStudent(login, ValidityCheck.BYPASS);
                            } catch (ServicesException ex) {
                                studentsNotAdded.add(login);
                            }
                        }

                        if (!studentsNotAdded.isEmpty()) {
                            new ErrorView("The following students were not added to the database: " +
                                          studentsNotAdded + ".");
                        }
                    } catch(NativeException ex) {
                        new ErrorView(ex, "Unable to add students because student logins could not be retrieved");
                    }
                }
                else {
                    String login = loginText.getText();
                    try {
                        if (firstNameText.getText().equals("") || lastNameText.getText().equals("")) {
                            Allocator.getUserServices().addStudent(login,
                                    ValidityCheck.CHECK);
                        }
                        else {
                            Allocator.getUserServices().addStudent(login,
                                    firstNameText.getText(),
                                    lastNameText.getText(),
                                    ValidityCheck.CHECK);
                        }
                    } catch (ServicesException ex) {
                        new ErrorView(ex, "Adding student " + login + " to the database failed.");
                    }
                }
            }
        } );

        addAsgnsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Assignment asgn = (Assignment) asgnCombo.getSelectedItem();
                try {
                    Allocator.getDatabaseIO().addAssignment(asgn);
                } catch (SQLException ex) {
                    new ErrorView(ex, "Adding assignment " + asgn + " to the database failed.");
                }

                if (allPartsRB.isSelected()) {
                    for (Part p : asgn.getParts()) {
                        try {
                            Allocator.getDatabaseIO().addAssignmentPart(p);
                        } catch (CakeHatDBIOException ex) {
                            new ErrorView(ex, "Adding assignment part " + p + " to the database failed.");
                        } catch (SQLException ex) {
                            new ErrorView(ex, "Adding assignment part " + p + " to the database failed.");
                        }
                    }
                } else {
                    if (partCombo.getSelectedItem() != null) {
                        try {
                            Allocator.getDatabaseIO().addAssignmentPart((Part) partCombo.getSelectedItem());
                        } catch (SQLException ex) {
                            new ErrorView(ex, "Adding assignment " + asgn + " to the database failed.");
                        } catch (CakeHatDBIOException ex) {
                            new ErrorView(ex, "Adding assignment " + asgn + " to the database failed.");
                        }
                    }
                }
            }
        });

        addTAsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (TA ta : Allocator.getCourseInfo().getTAs()) {
                    try {
                        Allocator.getDatabaseIO().addTA(ta);
                    } catch (SQLException ex) {
                        new ErrorView(ex, "Adding TA " + ta + " to the database failed.");
                    }
                }
            }
        } );

        allStudentsRB.setSelected(true);
        allPartsRB.setSelected(true);

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

        whichStudentsBG = new javax.swing.ButtonGroup();
        whichPartsBG = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        databasePanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        allStudentsRB = new javax.swing.JRadioButton();
        oneStudentRB = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        loginText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        firstNameText = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        lastNameText = new javax.swing.JTextField();
        addStudentsButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        asgnCombo = new javax.swing.JComboBox();
        allPartsRB = new javax.swing.JRadioButton();
        onePartRB = new javax.swing.JRadioButton();
        partCombo = new javax.swing.JComboBox();
        addAsgnsButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        addTAsButton = new javax.swing.JButton();
        configPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        databasePanel.setName("databasePanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(ConfigManager.class);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        whichStudentsBG.add(allStudentsRB);
        allStudentsRB.setText(resourceMap.getString("allStudentsRB.text")); // NOI18N
        allStudentsRB.setName("allStudentsRB"); // NOI18N

        whichStudentsBG.add(oneStudentRB);
        oneStudentRB.setText(resourceMap.getString("oneStudentRB.text")); // NOI18N
        oneStudentRB.setName("oneStudentRB"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        loginText.setText(resourceMap.getString("loginText.text")); // NOI18N
        loginText.setName("loginText"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        firstNameText.setText(resourceMap.getString("firstNameText.text")); // NOI18N
        firstNameText.setName("firstNameText"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        lastNameText.setText(resourceMap.getString("lastNameText.text")); // NOI18N
        lastNameText.setName("lastNameText"); // NOI18N

        addStudentsButton.setText(resourceMap.getString("addStudentsButton.text")); // NOI18N
        addStudentsButton.setName("addStudentsButton"); // NOI18N

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setName("jSeparator1"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        asgnCombo.setName("asgnCombo"); // NOI18N

        whichPartsBG.add(allPartsRB);
        allPartsRB.setText(resourceMap.getString("allPartsRB.text")); // NOI18N
        allPartsRB.setName("allPartsRB"); // NOI18N

        whichPartsBG.add(onePartRB);
        onePartRB.setText(resourceMap.getString("onePartRB.text")); // NOI18N
        onePartRB.setName("onePartRB"); // NOI18N

        partCombo.setName("partCombo"); // NOI18N

        addAsgnsButton.setText(resourceMap.getString("addAsgnsButton.text")); // NOI18N
        addAsgnsButton.setName("addAsgnsButton"); // NOI18N

        jSeparator2.setName("jSeparator2"); // NOI18N

        addTAsButton.setText(resourceMap.getString("addTAsButton.text")); // NOI18N
        addTAsButton.setName("addTAsButton"); // NOI18N

        javax.swing.GroupLayout databasePanelLayout = new javax.swing.GroupLayout(databasePanel);
        databasePanel.setLayout(databasePanelLayout);
        databasePanelLayout.setHorizontalGroup(
            databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databasePanelLayout.createSequentialGroup()
                .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(databasePanelLayout.createSequentialGroup()
                        .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(databasePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(allStudentsRB)
                                    .addComponent(oneStudentRB)))
                            .addGroup(databasePanelLayout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(databasePanelLayout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(loginText, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                                    .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, databasePanelLayout.createSequentialGroup()
                                            .addComponent(jLabel5)
                                            .addGap(18, 18, 18)
                                            .addComponent(firstNameText))
                                        .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, databasePanelLayout.createSequentialGroup()
                                            .addComponent(jLabel6)
                                            .addGap(18, 18, 18)
                                            .addComponent(lastNameText))))))
                        .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(databasePanelLayout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(databasePanelLayout.createSequentialGroup()
                                            .addComponent(onePartRB)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(partCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, databasePanelLayout.createSequentialGroup()
                                            .addComponent(addAsgnsButton)
                                            .addGap(62, 62, 62))))
                                .addGroup(databasePanelLayout.createSequentialGroup()
                                    .addGap(36, 36, 36)
                                    .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(databasePanelLayout.createSequentialGroup()
                                            .addComponent(jLabel8)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(asgnCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(databasePanelLayout.createSequentialGroup()
                                            .addGap(12, 12, 12)
                                            .addComponent(allPartsRB))))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, databasePanelLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(103, 103, 103)))
                            .addGroup(databasePanelLayout.createSequentialGroup()
                                .addGap(72, 72, 72)
                                .addComponent(addTAsButton))))
                    .addGroup(databasePanelLayout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(addStudentsButton)))
                .addContainerGap())
        );
        databasePanelLayout.setVerticalGroup(
            databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(allStudentsRB)
                    .addComponent(jLabel8)
                    .addComponent(asgnCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(oneStudentRB)
                    .addComponent(allPartsRB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(databasePanelLayout.createSequentialGroup()
                        .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(loginText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(firstNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(lastNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(addStudentsButton))
                    .addGroup(databasePanelLayout.createSequentialGroup()
                        .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(onePartRB)
                            .addComponent(partCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(addAsgnsButton)
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addTAsButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(databasePanelLayout.createSequentialGroup()
                .addGap(106, 106, 106)
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                .addGap(160, 160, 160))
        );

        jTabbedPane1.addTab(resourceMap.getString("databasePanel.TabConstraints.tabTitle"), databasePanel); // NOI18N

        configPanel.setName("configPanel"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout configPanelLayout = new javax.swing.GroupLayout(configPanel);
        configPanel.setLayout(configPanelLayout);
        configPanelLayout.setHorizontalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addGap(328, 328, 328)
                .addComponent(jLabel1)
                .addContainerGap(137, Short.MAX_VALUE))
        );
        configPanelLayout.setVerticalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addGap(147, 147, 147)
                .addComponent(jLabel1)
                .addContainerGap(136, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("configPanel.TabConstraints.tabTitle"), configPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ConfigManager().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAsgnsButton;
    private javax.swing.JButton addStudentsButton;
    private javax.swing.JButton addTAsButton;
    private javax.swing.JRadioButton allPartsRB;
    private javax.swing.JRadioButton allStudentsRB;
    private javax.swing.JComboBox asgnCombo;
    private javax.swing.JPanel configPanel;
    private javax.swing.JPanel databasePanel;
    private javax.swing.JTextField firstNameText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField lastNameText;
    private javax.swing.JTextField loginText;
    private javax.swing.JRadioButton onePartRB;
    private javax.swing.JRadioButton oneStudentRB;
    private javax.swing.JComboBox partCombo;
    private javax.swing.ButtonGroup whichPartsBG;
    private javax.swing.ButtonGroup whichStudentsBG;
    // End of variables declaration//GEN-END:variables

}
