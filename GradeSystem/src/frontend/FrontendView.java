/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FrontendView.java
 *
 * Created on Sep 25, 2009, 4:38:21 PM
 */
package frontend;

import backend.DatabaseIO;
import frontend.fileviewer.FileViewerView;
import frontend.grader.Grader;
import frontend.grader.rubric.RubricManager;
import frontend.tester.TesterImpl;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.imageio.ImageIO;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import utils.Constants;
import utils.ErrorView;
import utils.Utils;


/**
 *
 * @author psastras
 */
public class FrontendView extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private Vector<String> _selected;

    /** Creates new form FrontendView */
    public FrontendView() {
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
        updateFormComponents();

        _selected = new Vector<String>();
        _selected.add((String) assignmentList.getSelectedValue());

        this.setTitle(Utils.getUserLogin() + " - cs015 Grader");

        //untar all students' code for the initially selected project
        FUtils.untar(assignmentList, studentList);


        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                FUtils.removeCodeDirectories(_selected);
            }
        });
    }

    private void updateAssignmentList() {
        this.populateStudentList();

        //must also inform Tester button of the newly selected current assignment
        if (FUtils.hasTester((String) assignmentList.getSelectedValue())) {
            runTesterButton.setEnabled(true);
        } else {
            runTesterButton.setEnabled(false);
        }

        //and untar all students' code for the newly selected current assignment
        FUtils.untar(assignmentList, studentList);

        //and add this current assignment to the vector of assignments that have been selected
        _selected.add((String) assignmentList.getSelectedValue());
    }

    private void updateFormComponents() {
        assignmentList.setListData(DatabaseIO.getProjectNames());
        if (assignmentList.getModel().getSize() > 0) {
            assignmentList.setSelectedIndex(0);
        }

        populateStudentList();

        /* check whether runTesterButton should be enabled (if the assignment selected on startup has
         * a tester to run) or not
         */
        if (FUtils.hasTester((String) assignmentList.getSelectedValue())) {
            runTesterButton.setEnabled(true);
        } else {
            runTesterButton.setEnabled(false);
        }
    }

    /**
     * This method populates the StudentList list with the logins of the students that the TA has been
     * assigned to grade (as recorded in the database) for the selected assignment.
     */
    private void populateStudentList() {

        String user = System.getProperty("user.name");
        try {
            ISqlJetCursor cursor = DatabaseIO.getItemWithFilter("assignment_dist", "ta_login_dist", user);
            if (cursor.eof()) {
                cursor.close();
                return;
            }
            try {
                String s = cursor.getString((String) assignmentList.getSelectedValue());
                if (s == null) {
                    s = "";
                }
                String[] ss = s.split(", ");
                studentList.setListData(ss);
                if (studentList.getModel().getSize() > 0) {
                    studentList.setSelectedIndex(0);
                    currentInfo.setText((String) studentList.getSelectedValue());
                }

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

        assignmentListPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        assignmentList = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        generalCommandsLabel = new javax.swing.JLabel();
        selectedStudentLabel = new javax.swing.JLabel();
        openButton = new javax.swing.JButton();
        printButton = new javax.swing.JButton();
        runTesterButton = new javax.swing.JButton();
        runDemoButton = new javax.swing.JButton();
        printAllButton = new javax.swing.JButton();
        submitGradesButton = new javax.swing.JButton();
        runButton1 = new javax.swing.JButton();
        submitGradesButton1 = new javax.swing.JButton();
        viewGradingStandardsButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        studentListPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        studentList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        currentInfo = new javax.swing.JLabel();
        mainMenuBar = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        quitMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(FrontendView.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        assignmentListPanel.setName("assignmentListPanel"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        assignmentList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        assignmentList.setDoubleBuffered(true);
        assignmentList.setName("assignmentList"); // NOI18N
        assignmentList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                assignmentListMouseClicked(evt);
            }
        });
        assignmentList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                assignmentListKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(assignmentList);

        javax.swing.GroupLayout assignmentListPanelLayout = new javax.swing.GroupLayout(assignmentListPanel);
        assignmentListPanel.setLayout(assignmentListPanelLayout);
        assignmentListPanelLayout.setHorizontalGroup(
            assignmentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(assignmentListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(assignmentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        assignmentListPanelLayout.setVerticalGroup(
            assignmentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(assignmentListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
        );

        jPanel1.setName("jPanel1"); // NOI18N

        generalCommandsLabel.setFont(resourceMap.getFont("generalCommandsLabel.font")); // NOI18N
        generalCommandsLabel.setText(resourceMap.getString("generalCommandsLabel.text")); // NOI18N
        generalCommandsLabel.setName("generalCommandsLabel"); // NOI18N

        selectedStudentLabel.setFont(resourceMap.getFont("selectedStudentLabel.font")); // NOI18N
        selectedStudentLabel.setText(resourceMap.getString("selectedStudentLabel.text")); // NOI18N
        selectedStudentLabel.setName("selectedStudentLabel"); // NOI18N

        openButton.setIcon(resourceMap.getIcon("openButton.icon")); // NOI18N
        openButton.setMnemonic('O');
        openButton.setText(resourceMap.getString("openButton.text")); // NOI18N
        openButton.setFocusable(false);
        openButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        openButton.setIconTextGap(10);
        openButton.setName("openButton"); // NOI18N
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        printButton.setIcon(resourceMap.getIcon("printButton.icon")); // NOI18N
        printButton.setMnemonic('P');
        printButton.setText(resourceMap.getString("printButton.text")); // NOI18N
        printButton.setFocusable(false);
        printButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        printButton.setIconTextGap(10);
        printButton.setName("printButton"); // NOI18N
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        runTesterButton.setIcon(resourceMap.getIcon("runTesterButton.icon")); // NOI18N
        runTesterButton.setMnemonic('T');
        runTesterButton.setText(resourceMap.getString("runTesterButton.text")); // NOI18N
        runTesterButton.setFocusable(false);
        runTesterButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        runTesterButton.setIconTextGap(10);
        runTesterButton.setName("runTesterButton"); // NOI18N
        runTesterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runTesterButtonActionPerformed(evt);
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

        printAllButton.setIcon(resourceMap.getIcon("printAllButton.icon")); // NOI18N
        printAllButton.setMnemonic('A');
        printAllButton.setText(resourceMap.getString("printAllButton.text")); // NOI18N
        printAllButton.setFocusable(false);
        printAllButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        printAllButton.setIconTextGap(10);
        printAllButton.setName("printAllButton"); // NOI18N
        printAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printAllButtonActionPerformed(evt);
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

        runButton1.setIcon(resourceMap.getIcon("runButton1.icon")); // NOI18N
        runButton1.setMnemonic('G');
        runButton1.setText(resourceMap.getString("runButton1.text")); // NOI18N
        runButton1.setFocusable(false);
        runButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        runButton1.setIconTextGap(10);
        runButton1.setName("runButton1"); // NOI18N
        runButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButton1gradeButtonActionPerformed(evt);
            }
        });

        submitGradesButton1.setIcon(resourceMap.getIcon("submitGradesButton1.icon")); // NOI18N
        submitGradesButton1.setMnemonic('R');
        submitGradesButton1.setText(resourceMap.getString("submitGradesButton1.text")); // NOI18N
        submitGradesButton1.setFocusable(false);
        submitGradesButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        submitGradesButton1.setIconTextGap(10);
        submitGradesButton1.setName("submitGradesButton1"); // NOI18N
        submitGradesButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitGradesButton1runButton(evt);
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

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(generalCommandsLabel)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectedStudentLabel)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(runTesterButton, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                                    .addComponent(openButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(submitGradesButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                                    .addComponent(printButton, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))))
                        .addGap(9, 9, 9))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(runDemoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                            .addComponent(viewGradingStandardsButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(submitGradesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                            .addComponent(printAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))
                        .addGap(9, 9, 9))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(runButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                        .addGap(9, 9, 9))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalCommandsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(printAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(runDemoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(submitGradesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(viewGradingStandardsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedStudentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openButton, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                    .addComponent(printButton, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runTesterButton, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .addComponent(submitGradesButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(45, 45, 45))
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

        javax.swing.GroupLayout studentListPanelLayout = new javax.swing.GroupLayout(studentListPanel);
        studentListPanel.setLayout(studentListPanelLayout);
        studentListPanelLayout.setHorizontalGroup(
            studentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentListPanelLayout.createSequentialGroup()
                .addGroup(studentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(jLabel2))
                .addContainerGap())
        );
        studentListPanelLayout.setVerticalGroup(
            studentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
        );

        jPanel2.setName("jPanel2"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        currentInfo.setText(resourceMap.getString("currentInfo.text")); // NOI18N
        currentInfo.setName("currentInfo"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(currentInfo))
                .addContainerGap(501, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(currentInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(assignmentListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(studentListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(assignmentListPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(studentListPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 429, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        FUtils.removeCodeDirectories(_selected);
        System.exit(0);
}//GEN-LAST:event_quitMenuItemActionPerformed

    private void assignmentListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_assignmentListMouseClicked
        this.updateAssignmentList();
}//GEN-LAST:event_assignmentListMouseClicked

    private void assignmentListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_assignmentListKeyReleased
        this.updateAssignmentList();
}//GEN-LAST:event_assignmentListKeyReleased

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        FUtils.openStudentProject((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
}//GEN-LAST:event_openButtonActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        FUtils.printStudentProject((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue(), null, true);
}//GEN-LAST:event_printButtonActionPerformed

    private void runTesterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runTesterButtonActionPerformed
        //FUtils.runTester((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
        TesterImpl t = new TesterImpl((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
        //t.setLocationRelativeTo(null);
        //t.setVisible(true);
}//GEN-LAST:event_runTesterButtonActionPerformed

    private void runDemoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runDemoButtonActionPerformed
        FUtils.demoProject((String) assignmentList.getSelectedValue());
}//GEN-LAST:event_runDemoButtonActionPerformed

    private void printAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printAllButtonActionPerformed
        FUtils.printAll((String) assignmentList.getSelectedValue(), studentList);
}//GEN-LAST:event_printAllButtonActionPerformed

    private void submitGradesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitGradesButtonActionPerformed
        RubricManager.convertAllToGrd((String) assignmentList.getSelectedValue(), Utils.getUserLogin());

        SubmitDialog sd = new SubmitDialog();

        if (sd.submitChecked()) {
            FUtils.submitXMLFiles((String) assignmentList.getSelectedValue());
        }
        if (sd.printChecked()) {
            FUtils.printGRDFiles((String) assignmentList.getSelectedValue());
            if (sd.notifyChecked()) {
                FUtils.notifyStudents(assignmentList, studentList);
            }
        } else if (sd.notifyChecked()) {
            FUtils.notifyStudents(assignmentList, studentList);
        }
}//GEN-LAST:event_submitGradesButtonActionPerformed

    private void runButton1gradeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButton1gradeButtonActionPerformed
        //TODO: prevent students from opening more than one grading GUI
        Grader g = new Grader((String) assignmentList.getSelectedValue(), Utils.getUserLogin(), (String) studentList.getSelectedValue());
        g.setLocationRelativeTo(null);
        g.setVisible(true);
}//GEN-LAST:event_runButton1gradeButtonActionPerformed

    private void submitGradesButton1runButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitGradesButton1runButton
        FUtils.compileStudentProject((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
        FUtils.runStudentProject((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
}//GEN-LAST:event_submitGradesButton1runButton

    private void studentListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_studentListMouseClicked
        currentInfo.setText((String) studentList.getSelectedValue());
}//GEN-LAST:event_studentListMouseClicked

    private void studentListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_studentListKeyReleased
        currentInfo.setText((String) studentList.getSelectedValue());
}//GEN-LAST:event_studentListKeyReleased

    private void viewGradingStandardsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewGradingStandardsButtonActionPerformed
        //FileViewer f = new FileViewer(Utils.getProjectDirectory(Project.getInstance((String)assignmentList.getSelectedValue())), Constants.DEDUCTIONS_LIST_FILENAME);
        //f.setVisible(true);
        FileViewerView  fvv = new FileViewerView();
        String project = (String) assignmentList.getSelectedValue();
        if (project.equals("TASafehouse")) {
            project = "TASafeHouse";
        }
        //fvv.openFile(new File(Utils.getProjectDirectory(Project.getInstance((String)assignmentList.getSelectedValue())) + Constants.DEDUCTIONS_LIST_FILENAME));
        fvv.openFile(new File(Constants.TEMPLATE_GRADE_SHEET_DIR + project + "/" + Constants.DEDUCTIONS_LIST_FILENAME));
        fvv.setLocationRelativeTo(null);
        fvv.setVisible(true);
}//GEN-LAST:event_viewGradingStandardsButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        //print GFX code
        FUtils.printAllGFX((String) assignmentList.getSelectedValue(), studentList);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        FUtils.openGFX((String) assignmentList.getSelectedValue(), (String) studentList.getSelectedValue());
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new FrontendView().setVisible(true);
            }
        });
    }

    @Override
    public void paintComponents(Graphics g) {
        super.paintComponents(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF );
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList assignmentList;
    private javax.swing.JPanel assignmentListPanel;
    private javax.swing.JLabel currentInfo;
    private javax.swing.JLabel generalCommandsLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JButton openButton;
    private javax.swing.JButton printAllButton;
    private javax.swing.JButton printButton;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JButton runButton1;
    private javax.swing.JButton runDemoButton;
    private javax.swing.JButton runTesterButton;
    private javax.swing.JLabel selectedStudentLabel;
    private javax.swing.JList studentList;
    private javax.swing.JPanel studentListPanel;
    private javax.swing.JButton submitGradesButton;
    private javax.swing.JButton submitGradesButton1;
    private javax.swing.JButton viewGradingStandardsButton;
    // End of variables declaration//GEN-END:variables
}
