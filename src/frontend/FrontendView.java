package frontend;

import backend.OldDatabaseOps;
import config.Assignment;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import utils.Allocator;
import utils.ErrorView;

/**
 * This class provides the frontend iterface for TAs when grading.  From here,
 * TAs can open, run, test, and grade student code, etc.
 *
 * @author jeldridg
 * @author jak2
 */
public class FrontendView extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private JButton[] _codeButtons, _rubricButtons, _studentButtons;

    /** Creates new form FrontendView */
    public FrontendView() {
        initComponents();

        try {
            //randomly selects one of 5 icons to display in menu bar (because Paul wanted to, that's why)
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
        }
        catch (IOException e) { }

        
        this.addWindowListener(new WindowAdapter() {
            @Override //remove user grading directory when frontend is closed
            public void windowClosing(WindowEvent e) {
                Allocator.getGradingUtilities().removeUserGradingDirectory();
            }
        });

        _codeButtons = new JButton[]{ runDemoButton, printAllButton, openButton, printButton, runTesterButton, runButton };
        _rubricButtons = new JButton[]{ gradeButton, submitGradesButton };
        _studentButtons = new JButton[]{ viewReadmeButton, openButton, printButton, runTesterButton, runButton, gradeButton };
        OldDatabaseOps.open();
        
        Allocator.getGradingUtilities().makeUserGradingDirectory();
        
        this.updateFormComponents();

        this.setTitle(Allocator.getGeneralUtilities().getUserLogin() + " - " + Allocator.getCourseInfo().getCourse() + " Grader");
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
    }

    /**
     * Returns the currently selected assignment as a String.
     * 
     * @return
     */
    private Assignment getSelectedAssignment(){
        return (Assignment) assignmentList.getSelectedValue();
    }

    /**
     * Returns the currently selected student as a String.
     *
     * @return
     */
    private String getSelectedStudent(){
        String selectedStudent = (String) studentList.getSelectedValue();
        if(!(selectedStudent == null || selectedStudent.isEmpty())){
            return selectedStudent;
        }
        else{
            return null;
        }
    }

    /**
     * Called when a different assignment is selected from the assignmentList to update other GUI components
     */
    private void updateAssignmentList() {
        //need to create directory for the assignment so GRD files can be created
        //even if no assignments have been untarred
        Allocator.getGeneralUtilities().makeDirectory(Allocator.getGradingUtilities().getUserGradingDirectory() 
                                                        + this.getSelectedAssignment().getName());
        
        this.populateStudentList();

        this.updateButtonStates();
    }

    /**
     * Called on startup to initalize assignmentList and studentList
     */
    private void updateFormComponents() {
        //Populate assignment list with all those that have handins
        assignmentList.setListData(Allocator.getCourseInfo().getHandinAssignments().toArray());
        if (assignmentList.getModel().getSize() > 0) {
            assignmentList.setSelectedIndex(0);
            Allocator.getGeneralUtilities().makeDirectory(Allocator.getGradingUtilities().getUserGradingDirectory() 
                                                        + this.getSelectedAssignment().getName());
        }
        this.populateStudentList();
        
        this.updateButtonStates();
    }

    /**
     * Enable or disable buttons based on the assignment selected. If there is
     * no students available to select for the assignment then all student
     * specific buttons are disabled.
     */
    private void updateButtonStates() {
        for(JButton button : _codeButtons) {
            button.setEnabled(this.getSelectedAssignment().getHandinPart().hasCode());
        }

        if(this.getSelectedStudent() != null) {
            viewReadmeButton.setEnabled(this.getSelectedAssignment().getHandinPart().hasReadme(this.getSelectedStudent()));
        }

        if(this.getSelectedAssignment().getHandinPart().hasCode()) {
            runTesterButton.setEnabled(this.getSelectedAssignment().getHandinPart().hasTester());
        }

        for(JButton button : _rubricButtons){
            button.setEnabled(this.getSelectedAssignment().getHandinPart().hasRubric());
        }

        viewGradingStandardsButton.setEnabled(this.getSelectedAssignment().getHandinPart().hasDeductionList());

        //If no student is selected, disable all student buttons
        if(this.getSelectedStudent() == null){
            for(JButton button : _studentButtons){
                button.setEnabled(false);
            }
        }
    }

    /**
     * TODO: Update this to the new database code. This is a huge hack right now.
     * 
     * This method populates the studentList list with the logins of the students that the TA has been
     * assigned to grade (as recorded in the database) for the selected assignment.
     */
    private void populateStudentList() {
        String user = Allocator.getGeneralUtilities().getUserLogin();

        try {
            ISqlJetCursor cursor = OldDatabaseOps.getItemWithFilter("assignment_dist", "ta_login_dist", user);
            if (cursor.eof()) {
                cursor.close();
                return;
            }
            try {
                //TODO: Hack because CS015 database has Final instead of Adventure, Othello, Indy, & Sketchy
                String prjName = this.getSelectedAssignment().getName();
                if(prjName.equals("Sketchy") || prjName.equals("Adventure")
                   || prjName.equals("Othello") || prjName.equals("Indy")){
                    prjName = "Final";
                }

                String s = cursor.getString(prjName);
                if (s == null) {
                    s = "";
                }
                String[] ss = s.split(", ");
                studentList.setListData(ss);
                if (studentList.getModel().getSize() > 0) {
                    studentList.setSelectedIndex(0);
                    currentInfo.setText(this.getSelectedStudent());
                }

            }
            finally {
                cursor.close();
            }
        }
        catch (Exception e) {
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
        gradeButton = new javax.swing.JButton();
        runButton = new javax.swing.JButton();
        viewGradingStandardsButton = new javax.swing.JButton();
        viewReadmeButton = new javax.swing.JButton();
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
                .addGroup(assignmentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(assignmentListPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(assignmentListPanelLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)))
                .addContainerGap())
        );
        assignmentListPanelLayout.setVerticalGroup(
            assignmentListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, assignmentListPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        runButton.setIcon(resourceMap.getIcon("runButton.icon")); // NOI18N
        runButton.setMnemonic('R');
        runButton.setText(resourceMap.getString("runButton.text")); // NOI18N
        runButton.setFocusable(false);
        runButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        runButton.setIconTextGap(10);
        runButton.setName("runButton"); // NOI18N
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
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

        viewReadmeButton.setIcon(resourceMap.getIcon("viewReadmeButton.icon")); // NOI18N
        viewReadmeButton.setMnemonic('V');
        viewReadmeButton.setText(resourceMap.getString("viewReadmeButton.text")); // NOI18N
        viewReadmeButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        viewReadmeButton.setIconTextGap(10);
        viewReadmeButton.setName("viewReadmeButton"); // NOI18N
        viewReadmeButton.setPressedIcon(resourceMap.getIcon("viewReadmeButton.pressedIcon")); // NOI18N
        viewReadmeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewReadmeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(generalCommandsLabel)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(selectedStudentLabel)
                            .addComponent(runTesterButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(viewReadmeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(viewGradingStandardsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(runDemoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                            .addComponent(gradeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(runButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(printAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(submitGradesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(openButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(printButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(generalCommandsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runDemoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewGradingStandardsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .addComponent(submitGradesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(selectedStudentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewReadmeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .addComponent(openButton, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runTesterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printButton, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gradeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(runButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30))
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
                .addContainerGap()
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE))
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
                .addContainerGap(468, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(currentInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGap(4, 4, 4)
                .addComponent(studentListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, 0, 358, Short.MAX_VALUE))
                    .addComponent(studentListPanel, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(assignmentListPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * ActionPerformed method for the quitMenuItem.  Removes all code directories
     * and then exits the program.
     * @param evt
     */
    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        Allocator.getGradingUtilities().removeUserGradingDirectory();
        System.exit(0);
}//GEN-LAST:event_quitMenuItemActionPerformed

    /**
     * Called when the assignmentList is clicked; calls updateAssignmentList() to
     * update GUI components
     * 
     * @param evt
     */
    private void assignmentListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_assignmentListMouseClicked
        this.updateAssignmentList();
}//GEN-LAST:event_assignmentListMouseClicked

    /**
     * Called when the assignmentList selection is changed using arrow keys;
     * calls updateAssignmentList() to update GUI components
     *
     * @param evt
     */
    private void assignmentListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_assignmentListKeyReleased
        this.updateAssignmentList();
}//GEN-LAST:event_assignmentListKeyReleased

    /**
     * actionPerformed method for openButton
     * Calls method in FrontendUtilities to open the code of the currently selected student
     * for the currently selected project
     * @param evt
     */
    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        this.getSelectedAssignment().getHandinPart().openCode(this.getSelectedStudent());
}//GEN-LAST:event_openButtonActionPerformed

    /**
     * actionPerformed method for printButton
     * Calls method in FrontendUtilities to print the code of the currently selected student
     * for the currently selected project
     * @param evt
     */
    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        String student = this.getSelectedStudent();
        Assignment asgn = this.getSelectedAssignment();

        asgn.getHandinPart().printCode(student, Allocator.getGradingUtilities().getPrinter());
}//GEN-LAST:event_printButtonActionPerformed

    private void runTesterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runTesterButtonActionPerformed
        this.getSelectedAssignment().getHandinPart().runTester(this.getSelectedStudent());
}//GEN-LAST:event_runTesterButtonActionPerformed

    /**
     * actionPerformed method for runDemoButton
     * Calls method in FrontendUtilities to demo the currently selected project
     * @param evt
     */
    private void runDemoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runDemoButtonActionPerformed
        this.getSelectedAssignment().getHandinPart().runDemo();
}//GEN-LAST:event_runDemoButtonActionPerformed

    /**
     * actionPerformed method for printAllButton
     * Calls method in FrontendUtilities to print code for all students assgined to the
     * user TA to grade for the currently selected project
     * @param evt
     */
    private void printAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printAllButtonActionPerformed
        this.getSelectedAssignment().getHandinPart().printCode(this.getCurrentStudents(),
                                                             Allocator.getGradingUtilities().getPrinter());
}//GEN-LAST:event_printAllButtonActionPerformed

    /**
     * Creates a new SubmitDialog to allow user to choose grade submitting options.
     * Calls appropriate methods on FrontendUtilities based on the options selected.
     * @param evt
     */
    private void submitGradesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitGradesButtonActionPerformed
        if(this.getSelectedAssignment() != null) {
            SubmitDialog sd = new SubmitDialog(studentList);
            if (sd.showDialog() == JOptionPane.OK_OPTION) {
                String asgnName = this.getSelectedAssignment().getName();

                Allocator.getRubricManager().convertToGRD(this.getSelectedAssignment().getHandinPart(), this.getCurrentStudents());
                
                Vector<String> selectedStudents = sd.getSelectedStudents();

                if (sd.printChecked()) {
                    Allocator.getGradingUtilities().printGRDFiles(this.getCurrentStudents(), asgnName);
                }

                if (sd.notifyChecked()) {
                    if (sd.emailChecked())
                        Allocator.getGradingUtilities().notifyStudents(asgnName, selectedStudents, true);
                    else
                        Allocator.getGradingUtilities().notifyStudents(asgnName, selectedStudents, false);
                }
            }
        }
}//GEN-LAST:event_submitGradesButtonActionPerformed

    /**
     * Called when the selected login is chaned studentList using they keyboard; simply updates
     * the "currently grading" message to display the login of the newly selected student
     * @param evt
     */
    private void studentListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_studentListMouseClicked
        currentInfo.setText(this.getSelectedStudent());
}//GEN-LAST:event_studentListMouseClicked

    /**
     * Called when a student login is clicked in the studentList; simply updates
     * the "currently grading" message to display the login of the newly selected student
     * @param evt
     */
    private void studentListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_studentListKeyReleased
        currentInfo.setText(this.getSelectedStudent());
}//GEN-LAST:event_studentListKeyReleased

    /**
     * actionPerformed method for viewGradingStandardsButton
     * Instantiates a new FileViewerView and tells it to open the deductions list for the currently
     * selected project
     * @param evt
     */
    private void viewGradingStandardsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewGradingStandardsButtonActionPerformed
        this.getSelectedAssignment().getHandinPart().viewDeductionList();
}//GEN-LAST:event_viewGradingStandardsButtonActionPerformed

    /**
     * actionPerformed method for runButton; first compiles and then runs the currently
     * selected student's code for the currently selected project
     * @param evt
     */
    /**
     * TODO: prevent users from opening more than one grading GUI
     *
     * actionPerformed method for gradeButton
     * Instantiates a new GradingVisualizer for the selected student for the selected assignment
     * @param evt
     */
    private void gradeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gradeButtonActionPerformed
        Allocator.getRubricManager().view(this.getSelectedAssignment().getHandinPart(), this.getSelectedStudent());
    }//GEN-LAST:event_gradeButtonActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        this.getSelectedAssignment().getHandinPart().run(this.getSelectedStudent());
}//GEN-LAST:event_runButtonActionPerformed

    private void viewReadmeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewReadmeButtonActionPerformed
        this.getSelectedAssignment().getHandinPart().viewReadme(this.getSelectedStudent());
    }//GEN-LAST:event_viewReadmeButtonActionPerformed

    private Iterable<String> getCurrentStudents() {
        ArrayList<String> studs = new ArrayList<String>();
        for (int i = 0; i < studentList.getModel().getSize(); i++) {
            studs.add((String) studentList.getModel().getElementAt(i));
        }
        return studs;
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
    private javax.swing.JButton gradeButton;
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
    private javax.swing.JButton runButton;
    private javax.swing.JButton runDemoButton;
    private javax.swing.JButton runTesterButton;
    private javax.swing.JLabel selectedStudentLabel;
    private javax.swing.JList studentList;
    private javax.swing.JPanel studentListPanel;
    private javax.swing.JButton submitGradesButton;
    private javax.swing.JButton viewGradingStandardsButton;
    private javax.swing.JButton viewReadmeButton;
    // End of variables declaration//GEN-END:variables
}