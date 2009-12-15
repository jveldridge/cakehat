/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * 
 *
 * 
 */
package frontend;

import backend.DatabaseIO;
import backend.database.DatabaseWatch;
import frontend.fileviewer.FileViewerView;
import frontend.grader.Grader;
import frontend.grader.rubric.RubricManager;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.tmatesoft.sqljet.core.SqlJetException;
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
 * @author psastras
 */
public class FinalProjectFrontendView extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private HashMap<String, Project> _studentsToProjects = new HashMap<String, Project>();
    private Vector<Project> _finalProjects = new Vector<Project>();
    private HashSet<String> _studentsUntarred = new HashSet<String>();

    /**
     * Constructor necessary for design view. Intentionally private so that it
     * is not called elsewhere. - Changed it to public so the button can actually make one (psastras)
     *
     */
    public FinalProjectFrontendView() {
        initComponents();

        try {
            switch ((int) (Math.random() * 5)) {
                case 0:
                    this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-devilish.png")));
                    break;
                case 1:
                    this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-angel.png")));
                    break;
                case 2:
                    this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-surprise.png")));
                    break;
                case 3:
                    this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-crying.png")));
                    break;
                case 4:
                    this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-monkey.png")));
                    break;
                case 5:
                    this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/face-glasses.png")));
                    break;
            }
        } catch (IOException e) {
        }



        this.setTitle(Utils.getUserLogin() + " - cs015 Grader");

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                removeCodeDirectories();
            }
        });

        //Open database
        try {
            DatabaseIO.open();
        } catch (SqlJetException ex) {
            Logger.getLogger(FinalProjectFrontendView.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Load final projects
        for (Assignment asgn : ConfigurationManager.getAssignments()) {
            if (asgn.Type == AssignmentType.FINAL) {
                _finalProjects.add(Project.getInstance(asgn.Name));
            }
        }

        this.populateStudentList();

        //untar all students' code for the initially selected project
        this.untarCode();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        initDatabaseWatch();
    }

    private void untarCode() {
        //Get logins
        Vector<String> studentLogins = new Vector<String>();
        int size = studentList.getModel().getSize();

        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            String login = (String) studentList.getModel().getElementAt(i);
            if (login != null && login.trim().length() != 0) {
                studentLogins.add(login);
            }
        }

        //Untar all the code that has yet to be untarred
        for (String login : studentLogins) {
            if (!_studentsUntarred.contains(login)) {
                ProjectManager.untar(getStudentsProject(login), login);
                System.out.println("Untarring: " + login);
                _studentsUntarred.add(login);
            }
        }
    }

    private boolean isStudentUntarred(String studentLogin) {
        for (String login : _studentsUntarred) {
            if (login.equals(studentLogin)) {
                return true;
            }
        }

        return false;
    }

    private void removeCodeDirectories() {
        //Get the projects used
        HashSet<Project> prjsTouched = new HashSet<Project>(_studentsToProjects.values());

        for (Project prj : prjsTouched) {
            ProjectManager.removeCodeDirectory(prj);
        }
    }

    private Project getSelectedStudentsProject() {
        return getStudentsProject(getSelectedStudent());
    }

    private Project getStudentsProject(String studentLogin) {
        if (studentLogin == null) {
            return null;
        }

        if (!_studentsToProjects.containsKey(studentLogin)) {
            for (Project prj : _finalProjects) {
                if (prj.containsStudent(studentLogin)) {
                    _studentsToProjects.put(studentLogin, prj);
                }
            }
        }

        return _studentsToProjects.get(studentLogin);
    }

    private String getSelectedStudent() {
        return (String) studentList.getSelectedValue();
    }

    private Timer _dbTimer = new Timer();

    public void initDatabaseWatch() {
        TimerTask task = new DatabaseWatch(new File(Constants.DATABASE_FILE)) {

            protected void onChange(File file) {
                removeDatabaseWatch();
                populateStudentList();
                untarCode();
                initDatabaseWatch();
            }
        };
        _dbTimer.schedule(task, new Date(), 1000);
    }

    public void removeDatabaseWatch() {
        _dbTimer.cancel();
        _dbTimer.purge();
        _dbTimer = new Timer();
    }

    /*
     * This method populates the StudentList list with the logins of the students that the TA has been
     * assigned to grade (as recorded in the database) for the selected assignment.
     */
    private void populateStudentList() {
        String user = System.getProperty("user.name");
        int index = studentList.getSelectedIndex();
        try {
            ISqlJetCursor cursor = DatabaseIO.getItemWithFilter("assignment_dist", "ta_login_dist", user);
            if (cursor.eof()) {
                cursor.close();
                return;
            }
            try {
                String s = cursor.getString(Constants.FINAL_PROJECT);
                if (s == null) {
                    s = "";
                }
                String[] ss = s.split(", ");
                studentList.setListData(ss);
                if (index >= 0 && index < studentList.getModel().getSize()) {
                    studentList.setSelectedIndex(index);
                } else if (studentList.getModel().getSize() > 0) {
                    studentList.setSelectedIndex(0);
                }
                updateCurrentInfo();
            } finally {
                cursor.close();
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

        jPanel1 = new javax.swing.JPanel();
        generalCommandsLabel = new javax.swing.JLabel();
        selectedStudentLabel = new javax.swing.JLabel();
        openProjectButton = new javax.swing.JButton();
        runDemoButton = new javax.swing.JButton();
        submitGradesButton = new javax.swing.JButton();
        gradeButton = new javax.swing.JButton();
        runCodeButton = new javax.swing.JButton();
        viewGradingStandardsButton = new javax.swing.JButton();
        opengfxButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        currentInfo = new javax.swing.JLabel();
        studentListPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        studentList = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        mainMenuBar = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        quitMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(FinalProjectFrontendView.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        generalCommandsLabel.setFont(resourceMap.getFont("generalCommandsLabel.font")); // NOI18N
        generalCommandsLabel.setText(resourceMap.getString("generalCommandsLabel.text")); // NOI18N
        generalCommandsLabel.setName("generalCommandsLabel"); // NOI18N

        selectedStudentLabel.setFont(resourceMap.getFont("selectedStudentLabel.font")); // NOI18N
        selectedStudentLabel.setText(resourceMap.getString("selectedStudentLabel.text")); // NOI18N
        selectedStudentLabel.setName("selectedStudentLabel"); // NOI18N

        openProjectButton.setIcon(resourceMap.getIcon("openProjectButton.icon")); // NOI18N
        openProjectButton.setMnemonic('O');
        openProjectButton.setText(resourceMap.getString("openProjectButton.text")); // NOI18N
        openProjectButton.setFocusable(false);
        openProjectButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        openProjectButton.setIconTextGap(10);
        openProjectButton.setName("openProjectButton"); // NOI18N
        openProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProjectButtonActionPerformed(evt);
            }
        });

        runDemoButton.setIcon(resourceMap.getIcon("runDemoButton.icon")); // NOI18N
        runDemoButton.setMnemonic('D');
        runDemoButton.setText(resourceMap.getString("runDemoButton.text")); // NOI18N
        runDemoButton.setFocusable(false);
        runDemoButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        runDemoButton.setIconTextGap(10);
        runDemoButton.setName("runDemoButton"); // NOI18N
        runDemoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runDemoButtonActionPerformed(evt);
            }
        });

        submitGradesButton.setIcon(resourceMap.getIcon("submitGradesButton.icon")); // NOI18N
        submitGradesButton.setMnemonic('S');
        submitGradesButton.setText(resourceMap.getString("submitGradesButton.text")); // NOI18N
        submitGradesButton.setFocusable(false);
        submitGradesButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        submitGradesButton.setIconTextGap(10);
        submitGradesButton.setName("submitGradesButton"); // NOI18N
        submitGradesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitGradesButtonActionPerformed(evt);
            }
        });

        gradeButton.setIcon(resourceMap.getIcon("gradeButton.icon")); // NOI18N
        gradeButton.setMnemonic('G');
        gradeButton.setText(resourceMap.getString("gradeButton.text")); // NOI18N
        gradeButton.setFocusable(false);
        gradeButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        gradeButton.setIconTextGap(10);
        gradeButton.setName("gradeButton"); // NOI18N
        gradeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gradeButtonActionPerformed(evt);
            }
        });

        runCodeButton.setIcon(resourceMap.getIcon("runCodeButton.icon")); // NOI18N
        runCodeButton.setMnemonic('R');
        runCodeButton.setText(resourceMap.getString("runCodeButton.text")); // NOI18N
        runCodeButton.setFocusable(false);
        runCodeButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        runCodeButton.setIconTextGap(10);
        runCodeButton.setName("runCodeButton"); // NOI18N
        runCodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runCodeButtonrunButton(evt);
            }
        });

        viewGradingStandardsButton.setIcon(resourceMap.getIcon("viewGradingStandardsButton.icon")); // NOI18N
        viewGradingStandardsButton.setMnemonic('V');
        viewGradingStandardsButton.setText(resourceMap.getString("viewGradingStandardsButton.text")); // NOI18N
        viewGradingStandardsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        viewGradingStandardsButton.setIconTextGap(10);
        viewGradingStandardsButton.setName("viewGradingStandardsButton"); // NOI18N
        viewGradingStandardsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewGradingStandardsButtonActionPerformed(evt);
            }
        });

        opengfxButton.setIcon(resourceMap.getIcon("opengfxButton.icon")); // NOI18N
        opengfxButton.setMnemonic('O');
        opengfxButton.setText(resourceMap.getString("opengfxButton.text")); // NOI18N
        opengfxButton.setFocusable(false);
        opengfxButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        opengfxButton.setIconTextGap(10);
        opengfxButton.setName("opengfxButton"); // NOI18N
        opengfxButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opengfxButtonActionPerformed(evt);
            }
        });

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        currentInfo.setText(resourceMap.getString("currentInfo.text")); // NOI18N
        currentInfo.setName("currentInfo"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(currentInfo)
                        .addContainerGap(551, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addContainerGap(472, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(runDemoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                                    .addComponent(openProjectButton, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                                    .addComponent(opengfxButton, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(gradeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                                    .addComponent(runCodeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                                    .addComponent(viewGradingStandardsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(selectedStudentLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 405, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(generalCommandsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 461, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(submitGradesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(currentInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generalCommandsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runDemoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(viewGradingStandardsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedStudentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(runCodeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(openProjectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(opengfxButton, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                    .addComponent(gradeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitGradesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(138, 138, 138))
        );

        studentListPanel.setName("studentListPanel"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        studentList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        studentList.setName("studentList"); // NOI18N
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

        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setIconTextGap(10);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonAcionPerformed(evt);
            }
        });

        javax.swing.GroupLayout studentListPanelLayout = new javax.swing.GroupLayout(studentListPanel);
        studentListPanel.setLayout(studentListPanelLayout);
        studentListPanelLayout.setHorizontalGroup(
            studentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentListPanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addContainerGap(72, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, studentListPanelLayout.createSequentialGroup()
                .addGroup(studentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
                .addContainerGap())
        );
        studentListPanelLayout.setVerticalGroup(
            studentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        mainMenuBar.setName("mainMenuBar"); // NOI18N

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        quitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        quitMenuItem.setText(resourceMap.getString("quitMenuItem.text")); // NOI18N
        quitMenuItem.setName("quitMenuItem"); // NOI18N
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(quitMenuItem);

        mainMenuBar.add(jMenu1);

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(studentListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(studentListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        removeCodeDirectories();
        System.exit(0);
}//GEN-LAST:event_quitMenuItemActionPerformed

    private void openProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openProjectButtonActionPerformed
        Project prj = getSelectedStudentsProject();
        if (prj == null) {
            return;
        }

        FUtils.openStudentProject(prj.getName(), getSelectedStudent());
}//GEN-LAST:event_openProjectButtonActionPerformed

    private void runDemoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runDemoButtonActionPerformed
        Project prj = getSelectedStudentsProject();
        if (prj == null) {
            JOptionPane.showMessageDialog(this, "To run a demo, you must have a student's project selected \n" +
                    "so that a demo for that Final project can be run.");
            return;
        }

        FUtils.demoProject(prj.getName());
}//GEN-LAST:event_runDemoButtonActionPerformed

    private void submitGradesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitGradesButtonActionPerformed
        //Get the projects used
        HashSet<Project> prjsTouched = new HashSet<Project>(_studentsToProjects.values());

        //For each project
        for (Project prj : prjsTouched) {
            RubricManager.convertAllToGrd(prj.getName(), Utils.getUserLogin());
        //TODO: Submit XMLs into the Final directory
        //TODO: E-mail all students their grd files
        }

}//GEN-LAST:event_submitGradesButtonActionPerformed

    private void gradeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gradeButtonActionPerformed
        Project prj = getSelectedStudentsProject();
        if (prj == null) {
            return;
        }

        Grader g = new Grader(prj.getName(), Utils.getUserLogin(), getSelectedStudent());
        g.setLocationRelativeTo(null);
        g.setVisible(true);
}//GEN-LAST:event_gradeButtonActionPerformed

    private void runCodeButtonrunButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runCodeButtonrunButton
        Project prj = getSelectedStudentsProject();
        if (prj == null) {
            return;
        }

        FUtils.compileStudentProject(prj.getName(), getSelectedStudent());
        FUtils.runStudentProject(prj.getName(), getSelectedStudent());
}//GEN-LAST:event_runCodeButtonrunButton

    private void studentListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_studentListMouseClicked
        updateCurrentInfo();
}//GEN-LAST:event_studentListMouseClicked

    private void updateCurrentInfo() {
        Project prj = getSelectedStudentsProject();
        if (prj == null) {
            return;
        }

        currentInfo.setText(getSelectedStudent() + " (" + prj.getName() + ")");
    }

    private void studentListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_studentListKeyReleased
        updateCurrentInfo();
}//GEN-LAST:event_studentListKeyReleased

    private void viewGradingStandardsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewGradingStandardsButtonActionPerformed
        Project prj = getSelectedStudentsProject();
        if (prj == null) {
            return;
        }

        FileViewerView fvv = new FileViewerView();
        fvv.openFile(new File(Constants.TEMPLATE_GRADE_SHEET_DIR + prj.getName() + "/" + Constants.DEDUCTIONS_LIST_FILENAME));
        fvv.setLocationRelativeTo(null);
        fvv.setVisible(true);
}//GEN-LAST:event_viewGradingStandardsButtonActionPerformed

    private void opengfxButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opengfxButtonActionPerformed
        Project prj = getSelectedStudentsProject();
        if (prj == null) {
            return;
        }

        FUtils.openGFX(prj.getName(), getSelectedStudent());
}//GEN-LAST:event_opengfxButtonActionPerformed

    private void refreshButtonAcionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonAcionPerformed
        this.populateStudentList();
        this.untarCode();
    }//GEN-LAST:event_refreshButtonAcionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new FinalProjectFrontendView();
            }
        });
    }

    @Override
    public void paintComponents(Graphics g) {
        super.paintComponents(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel currentInfo;
    private javax.swing.JLabel generalCommandsLabel;
    private javax.swing.JButton gradeButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JButton openProjectButton;
    private javax.swing.JButton opengfxButton;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JButton runCodeButton;
    private javax.swing.JButton runDemoButton;
    private javax.swing.JLabel selectedStudentLabel;
    private javax.swing.JList studentList;
    private javax.swing.JPanel studentListPanel;
    private javax.swing.JButton submitGradesButton;
    private javax.swing.JButton viewGradingStandardsButton;
    // End of variables declaration//GEN-END:variables
}
