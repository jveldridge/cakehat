package gradesystem.views.backend;

import gradesystem.views.backend.stathist.AssignmentChartPanel;
import gradesystem.views.backend.stathist.StudentChartPanel;
import com.google.common.collect.Iterables;
import gradesystem.config.Assignment;
import gradesystem.config.Part;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.html.HTMLEditorKit;
import gradesystem.Allocator;
import gradesystem.database.Group;
import gradesystem.views.shared.ErrorView;
import java.util.List;

/**
 * Interface for sending grade reports to students.  Grade reports will be sent
 * to the students passed in Collection<String> students for the Assignment Parts
 * in the Map asgnParts.  Histograms for each project, and a graph showing the student's
 * performance over time, can be included on the outgoing email (included by default).
 * 
 * @author psastras
 * @author jeldridg
 */
public class GradeReportView extends javax.swing.JFrame {

    /** Creates new form GradeReportView */
    private Collection<String> _students;
    private Map<Assignment, List<Part>> _asgnParts;
    private Vector<Assignment> _sortedAssignments;

    public GradeReportView(Map<Assignment, List<Part>> asgnParts, Collection<String> students) {
        initComponents();

        _asgnParts = asgnParts;
        _students = students;

        //sort the assignments by assignment number
        _sortedAssignments = new Vector<Assignment>(_asgnParts.keySet());
        Collections.sort(_sortedAssignments);

        HTMLEditorKit k = new HTMLEditorKit();

        _previewPane.setEditorKit(k);
        _previewPane.setDocument(k.createDefaultDocument());
        String course = Allocator.getCourseInfo().getCourse();
        _messageText.setText("<p>Here are your current grades for the course.<br />"
                + "</p>\n<p>-The "+ course + " TAs</p>\n");
        if (_students.size() <= 5) {
            _toText.setText(_students.toString());
        } else {
            _toText.setText(_students.size() + " students");
        }

        _fromText.setText(course + "headtas@cs.brown.edu");
        updatePreview();

        new File(".tmpdata").mkdirs();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    /** 
     * Displays the grade report as it would be sent to the course's test account
     */
    public void updatePreview() {
        String student = Iterables.get(_students, 0);
        _previewPane.setText(htmlBuilder(student));
    }

    /**
     * Constructs the body of the email message for the given student, adding
     * the student's grades (or extensions/exemptions as appropriate) for each Part
     * of each Assignment.
     * 
     * @param student
     * @return
     */
    public String htmlBuilder(String student) {
        String htmlString = "<body style='font-family: sans-serif; font-size: 10pt'>"
                + "<h1 style='font-weight: bold; font-size:11pt'>"
                + "[" + Allocator.getCourseInfo().getCourse() + "] Grade Report - " + student + "</h1>"
                + "<hr />" + _messageText.getText();

        //constructing the message body
        for (Assignment a : _sortedAssignments) {
            Group group;
            try {
                group = Allocator.getDatabaseIO().getStudentsGroup(a, student);
            } catch (SQLException ex) {
                new ErrorView(ex, "Could not get group for student " + student + " on "
                        + "assignment " + a + ".");
                return null;
            }

            htmlString += "<hr /><table cellspacing='0' cellpadding='5' style='width: 100%'>"
                    + "<tbody><tr style='font-weight: bold; background: #F0F0F0'><td>"
                    + a.getName() + "</td><td>Earned Points</td><td>Total Points</td></tr>";

            if (group == null) {
                htmlString += "<tr colspan=3 style='background: #FFFFFF" + "'><td>No grades recorded.</td></tr>";
                continue;
            }

            for (Part p : _asgnParts.get(a)) {
                Calendar extension = null;
                try {
                    extension = Allocator.getDatabaseIO().getExtension(group, a.getHandin());
                } catch (SQLException e) {
                    new ErrorView(e, "Could not determine if student " + student + " has "
                            + "an extension for assignment " + a + ".  It will be assumed that "
                            + "the student does not have an extension.");
                }

                String exemptionNote = null;
                try {
                    exemptionNote = Allocator.getDatabaseIO().getExemptionNote(group, p);
                } catch (SQLException e) {
                    new ErrorView(e, "Could not determine if student " + student + " has "
                            + "an exemption for part " + p + ".  It will be assumed that "
                            + "the student does not have an exemption.");
                }

                Double studentScore = null;
                String scoreString;
                try {
                    studentScore = Allocator.getDatabaseIO().getGroupScore(group, p);
                } catch (SQLException e) {
                    new ErrorView(e, "Could not retrieve the socre for student " + student
                            + "on part " + p + ".  The student's score will be displayed "
                            + "as \"UNKNOWN\".");
                    scoreString = "UNKNOWN";
                }

                if (exemptionNote != null) {
                    htmlString += "<tr style='background: #FFFFFF" + "'><td>"
                            + p.getName() + "</td><td>Exemption Granted</td><td>" + p.getPoints() + "</td></tr>";
                } else if (extension != null && studentScore == null && (extension.getTimeInMillis() > System.currentTimeMillis())) {
                    htmlString += "<tr style='background: #FFFFFF" + "'><td>"
                            + p.getName() + "</td><td>Extension until: "
                            + extension.getTime() + "</td><td>" + p.getPoints() + "</td></tr>";
                } else {
                    if (studentScore != null) {
                        scoreString = Double.toString(studentScore);
                    } else {
                        scoreString = "0 (No grade recorded)";
                    }

                    htmlString += "<tr style='background: #FFFFFF" + "'><td>"
                            + p.getName() + "</td><td>" + scoreString + "</td><td>"
                            + p.getPoints() + "</td></tr>";
                }
            }

            htmlString += "</tbody></table>";
        }

        return htmlString;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        _messageText = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        _fromText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        _previewPane = new javax.swing.JEditorPane();
        jLabel5 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        sendToStudsButton = new javax.swing.JButton();
        attachScoreGraphButton = new javax.swing.JCheckBox();
        attachHistButton = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        _toText = new javax.swing.JTextField();
        sendToOtherButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        closeMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        jLabel1.setText("<html><b>Enter Message</b></html>");
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        _messageText.setColumns(20);
        _messageText.setRows(5);
        _messageText.setName("_messageText"); // NOI18N
        _messageText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                _messageTextKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(_messageText);

        jLabel2.setText("<html><b>From</b></html>");
        jLabel2.setName("jLabel2"); // NOI18N

        _fromText.setEditable(false);
        _fromText.setName("_fromText"); // NOI18N

        jLabel3.setText("<html><b>Grade Report</b></html>");
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        _previewPane.setEditable(false);
        _previewPane.setName("_previewPane"); // NOI18N
        jScrollPane3.setViewportView(_previewPane);

        jLabel5.setText("<html><b>Preview</b></html>");
        jLabel5.setName("jLabel5"); // NOI18N

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setName("jSeparator1"); // NOI18N

        sendToStudsButton.setText("Email to Students");
        sendToStudsButton.setName("sendToStudsButton"); // NOI18N
        sendToStudsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendToStudsButtonActionPerformed(evt);
            }
        });

        attachScoreGraphButton.setText("Attach Student Score Graph");
        attachScoreGraphButton.setName("attachScoreGraphButton"); // NOI18N

        attachHistButton.setText("Attach Assignment Historgrams");
        attachHistButton.setName("attachHistButton"); // NOI18N

        jLabel4.setText("<html><b>To</b></html>");
        jLabel4.setName("jLabel4"); // NOI18N

        _toText.setEditable(false);
        _toText.setName("_toText"); // NOI18N

        sendToOtherButton.setText("Email to Other Address");
        sendToOtherButton.setName("sendToOtherButton"); // NOI18N
        sendToOtherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendToOtherButtonActionPerformed(evt);
            }
        });

        menuBar.setName("jMenuBar1"); // NOI18N

        fileMenu.setText("File");
        fileMenu.setName("jMenu1"); // NOI18N

        closeMenuItem.setText("Close");
        closeMenuItem.setName("closeMenuItem"); // NOI18N
        closeMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                closeMenuItemMousePressed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.setName("jMenu2"); // NOI18N
        menuBar.add(editMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                                .addGap(305, 305, 305))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(attachHistButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                            .addComponent(_toText)
                                                            .addComponent(_fromText, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE))))
                                                .addGap(25, 25, 25)))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(attachScoreGraphButton)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(360, 360, 360))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane3)
                                .addContainerGap())))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(sendToOtherButton, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sendToStudsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_toText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_fromText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(attachScoreGraphButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(attachHistButton))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sendToOtherButton)
                    .addComponent(sendToStudsButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _messageTextKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__messageTextKeyReleased
        updatePreview();
    }//GEN-LAST:event__messageTextKeyReleased

    private void sendToStudsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendToStudsButtonActionPerformed

        this.sendToStudsButton.setEnabled(false);

        ArrayDeque<File> fullFileList = new ArrayDeque<File>();

        //generate Assignment histograms
        if (attachHistButton.isSelected()) {
            for (Assignment a : _sortedAssignments) {
                Collection<Group> groups;
                try {
                    groups = Allocator.getDatabaseIO().getGroupsForAssignment(a);
                } catch (SQLException ex) {
                    new ErrorView(ex, "Could not get read groups from database for assignment " + a + "." +
                                      "Charts for this assignment will not be included.");
                    continue;
                }

                try {
                    AssignmentChartPanel acp = new AssignmentChartPanel();
                    acp.updateChartData(a, groups);
                    File tmp = new File(Allocator.getPathServices().getUserWorkspaceDir(), a.getName() + ".png");
                    fullFileList.add(tmp);
                    ImageIO.write(acp.getImage(600, 250), "png", fullFileList.peekLast());
                } catch (IOException ex) {
                    new ErrorView(ex, "Could not generate histogram image for assignment " +
                                       a.getName() + ".");
                }
            }
        }

        String[] attachPaths = new String[fullFileList.size() + 1];
        int size = fullFileList.size();
        for (int i = 0; i < size; i++) {
            attachPaths[i] = fullFileList.removeFirst().getAbsolutePath();
        }
        attachPaths[attachPaths.length - 1] = null;                   //last will be for student chart

        //send email to each student
        StudentChartPanel scp = new StudentChartPanel();
        for (String student : _students) {

            //create student score chart
            if (attachScoreGraphButton.isSelected()) {
                try {
                    Assignment[] asgns = _asgnParts.keySet().toArray(new Assignment[0]);
                    Arrays.sort(asgns);
                    scp.updateChart(student, asgns);
                    File tmp = new File(Allocator.getPathServices().getUserWorkspaceDir(), student + ".png");
                    fullFileList.add(tmp);
                    ImageIO.write(scp.getImage(600, 250), "png", fullFileList.peekLast());
                    attachPaths[attachPaths.length - 1] = tmp.getAbsolutePath();
                } catch (IOException ex) {
                    new ErrorView(ex, "Could not generate graph for student " + student + ".");
                }
            }

            Allocator.getConfigurationInfo().getEmailAccount().sendMail(Allocator.getCourseInfo().getCourse()
                    + "headtas@cs.brown.edu",
                    new String[]{student + "@cs.brown.edu"},
                    null, null,
                    "[" + Allocator.getCourseInfo().getCourse()
                    + "] Grade Report", htmlBuilder(student),
                    attachPaths);
            this.sendToStudsButton.setEnabled(false);
            this.setVisible(false);
            this.dispose();
        }
    }//GEN-LAST:event_sendToStudsButtonActionPerformed

    private void sendToOtherButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendToOtherButtonActionPerformed
        JTextField addressBox = new JTextField();
        int res = JOptionPane.showConfirmDialog(this, addressBox, "Enter email address", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            for (String student : _students) {
                Allocator.getConfigurationInfo().getEmailAccount().sendMail(Allocator.getCourseInfo().getCourse()
                        + "headtas@cs.brown.edu",
                        new String[]{addressBox.getText()},
                        null, null,
                        "[" + Allocator.getCourseInfo().getCourse()
                        + "] Grade Report", htmlBuilder(student),
                        null);
            }
        }
    }//GEN-LAST:event_sendToOtherButtonActionPerformed

    private void closeMenuItemMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMenuItemMousePressed
        this.dispose();
    }//GEN-LAST:event_closeMenuItemMousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField _fromText;
    private javax.swing.JTextArea _messageText;
    private javax.swing.JEditorPane _previewPane;
    private javax.swing.JTextField _toText;
    private javax.swing.JCheckBox attachHistButton;
    private javax.swing.JCheckBox attachScoreGraphButton;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton sendToOtherButton;
    private javax.swing.JButton sendToStudsButton;
    // End of variables declaration//GEN-END:variables
}
