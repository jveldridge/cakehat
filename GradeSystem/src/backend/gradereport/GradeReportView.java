/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GradeReportView.java
 *
 * Created on Nov 7, 2009, 11:22:20 PM
 */
package backend.gradereport;

import backend.DatabaseIO;
import backend.histogram.ChartPanel;
import backend.histogram.StudentDataPanel;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import utils.BashConsole;
import utils.Utils;

/**
 *
 * @author psastras
 */
public class GradeReportView extends javax.swing.JFrame {

    /** Creates new form GradeReportView */
    private String[] _projectNames,  _projectPointsTotal,  _labNames,  _labPointsTotal,  _homeworkNames,  _homeworkPointsTotal,  _pointsEarned;

    public GradeReportView() {
        initComponents();
        HTMLEditorKit k = new HTMLEditorKit();
        //_projectList = new JList(DatabaseIO.getAssignmentNames());
        _projectNames = DatabaseIO.getProjectNames();
        _projectList.setListData(_projectNames);
        _labNames = DatabaseIO.getLabNames();
        _labList.setListData(_labNames);
        _homeworkNames = DatabaseIO.getHomeworkNames();
        _labPointsTotal = new String[_labNames.length];
        _projectPointsTotal = new String[_projectNames.length];
        _homeworkPointsTotal = new String[_homeworkNames.length];
        for (int i = 0; i < _projectNames.length; i++) {
            _projectPointsTotal[i] = Integer.toString(DatabaseIO.getAssignmentTotal(_projectNames[i]));
        }
        for (int i = 0; i < _labNames.length; i++) {
            _labPointsTotal[i] = Integer.toString(DatabaseIO.getAssignmentTotal(_labNames[i]));
        }
        for (int i = 0; i < _homeworkNames.length; i++) {
            _homeworkPointsTotal[i] = Integer.toString(DatabaseIO.getAssignmentTotal(_homeworkNames[i]));
        }
        //DatabaseIO.getAssignmentNames();

        _previewPane.setEditorKit(k);
        _previewPane.setDocument(k.createDefaultDocument());

        _messageText.setText("<p>Here are you current grades for the course.<br />\n<i>Histograms for each of the projects are attached.</i></p>\n<p>-The cs015 TAs</p>\n");
        _fromText.setText("cs015headtas@cs.brown.edu");
        updatePreview();
        new File(".tmpdata").mkdirs();
    }

    public void updatePreview() {
        int[] projIndex = _projectList.getSelectedIndices();
        int[] labIndex = _labList.getSelectedIndices();

        String[] projNames = new String[projIndex.length];
        Object[] projObjects = _projectList.getSelectedValues();
        for (int i = 0; i < projNames.length; i++) {
            projNames[i] = (String) projObjects[i];
        }
        String[] labNames = new String[labIndex.length];
        Object[] labObjects = _labList.getSelectedValues();
        for (int i = 0; i < labNames.length; i++) {
            labNames[i] = (String) labObjects[i];
        }
//        Object[] homeworkObjects = DatabaseIO.getHomeworkNames();
//        String[] homeworkNames = new String[homeworkObjects.length];
//        for (int i = 0; i < homeworkNames.length; i++) {
//            homeworkNames[i] = (String) homeworkObjects[i];
//        }

        //

        String[] projTotals = new String[projIndex.length];
        String[] projEarned = new String[projIndex.length];
        for (int i = 0; i < projIndex.length; i++) {
            projTotals[i] = _projectPointsTotal[projIndex[i]];
            projEarned[i] = "0";
        }
        String[] labTotals = new String[labIndex.length];
        String[] labEarned = new String[labIndex.length];
        for (int i = 0; i < labIndex.length; i++) {
            labTotals[i] = _labPointsTotal[labIndex[i]];
            labEarned[i] = "0";
        }

        String[] homeworkEarned = new String[_homeworkNames.length];
        Arrays.fill(homeworkEarned, "0");

//        String[] homeworkTotals = new String[homeworkNames.length];

//        DatabaseIO.getAssignmentTotal(assignmentName)
//        String[] homeworkEarned = new String[homeworkNames.length];
//        for (int i = 0; i < homeworkNames.length; i++) {
//            homeworkTotals[i] =
//        }
        _previewPane.setText(htmlBuilder(_messageText.getText(), projNames, projEarned, projTotals, labNames, labEarned, labTotals, _homeworkNames, homeworkEarned, _homeworkPointsTotal));
    }

    public String htmlBuilder(String body, String[] projectNames, String[] pointsEarned, String[] pointsTotal) {
        String stringBuilder = "<body style='font-family: sans-serif; font-size: 10pt'><h1 style='font-weight: bold; font-size:11pt'>[cs015] Grade Report</h1>" +
                "<hr />" + body;
        stringBuilder += "<hr /><table cellspacing='0' cellpadding='5' style='width: 100%'><tbody><tr style='font-weight: bold; background: #F0F0F0'><td>Project Name</td><td>Earned Points</td><td>Total Points</td></tr>";
        for (int i = 0; i < projectNames.length; i++) {
            stringBuilder += "<tr><td>" + projectNames[i] + "</td><td>" + pointsEarned[i] + "</td><td>" + pointsTotal[i] + "</td></tr>";
        }
        stringBuilder += "</tbody></table></body>";
        return stringBuilder;
    }

    public String htmlBuilder(String body, String[] projectNames, String[] projPointsEarned, String[] projPointsTotal, String[] labNames, String[] labPointsEarned, String[] labPointsTotal, String[] homeworkNames, String[] homeworkPointsEarned, String[] homeworkPointsTotal) {
        String stringBuilder = "<body style='font-family: sans-serif; font-size: 10pt'><h1 style='font-weight: bold; font-size:11pt'>[cs015] Grade Report</h1>" +
                "<hr />" + body;
        stringBuilder += "<hr /><table cellspacing='0' cellpadding='5' style='width: 100%'><tbody><tr style='font-weight: bold; background: #F0F0F0'><td>Project Name</td><td>Earned Points</td><td>Total Points</td></tr>";
        for (int i = 0; i < projectNames.length; i++) {
            stringBuilder += "<tr style='background:" + ((i % 2 == 0) ? "#FFFFFF" : "#FDFDFD") + "'><td>" + projectNames[i] + "</td><td>" + projPointsEarned[i] + "</td><td>" + projPointsTotal[i] + "</td></tr>";
        }
        stringBuilder += "</tbody></table>";

        stringBuilder += "<hr /><table cellspacing='0' cellpadding='5' style='width: 100%'><tbody><tr style='font-weight: bold; background: #F0F0F0'><td>Homework Name</td><td>Earned Points</td><td>Total Points</td></tr>";
        for (int i = 0; i < homeworkNames.length; i++) {
            stringBuilder += "<tr style='background:" + ((i % 2 == 0) ? "#FFFFFF" : "#FDFDFD") + "'><td>" + homeworkNames[i] + "</td><td>" + homeworkPointsEarned[i] + "</td><td>" + homeworkPointsTotal[i] + "</td></tr>";
        }
        stringBuilder += "</tbody></table>";

        stringBuilder += "<hr /><table cellspacing='0' cellpadding='5' style='width: 100%'><tbody><tr style='font-weight: bold; background: #F0F0F0'><td>Lab Name</td><td>Earned Points</td><td>Total Points</td></tr>";
        for (int i = 0; i < labNames.length; i++) {
            stringBuilder += "<tr style='background:" + ((i % 2 == 0) ? "#FFFFFF" : "#FDFDFD") + "'><td>" + labNames[i] + "</td><td>" + labPointsEarned[i] + "</td><td>" + labPointsTotal[i] + "</td></tr>";
        }

        stringBuilder += "</tbody></table><hr /></body>";
        return stringBuilder;
    }

    public String htmlBuilder(String body, String[] projectNames, String[] projPointsEarned, String[] projPointsTotal, String[] labNames, String[] labPointsEarned, String[] labPointsTotal) {
        String stringBuilder = "<body style='font-family: sans-serif; font-size: 10pt'><h1 style='font-weight: bold; font-size:11pt'>[cs015] Grade Report</h1>" +
                "<hr />" + body;
        stringBuilder += "<hr /><table cellspacing='0' cellpadding='5' style='width: 100%'><tbody><tr style='font-weight: bold; background: #F0F0F0'><td>Project Name</td><td>Earned Points</td><td>Total Points</td></tr>";
        for (int i = 0; i < projectNames.length; i++) {
            stringBuilder += "<tr><td>" + projectNames[i] + "</td><td>" + projPointsEarned[i] + "</td><td>" + projPointsTotal[i] + "</td></tr>";
        }
        stringBuilder += "</tbody></table>";

        stringBuilder += "<hr /><table cellspacing='0' cellpadding='5' style='width: 100%'><tbody><tr style='font-weight: bold; background: #F0F0F0'><td>Lab Name</td><td>Earned Points</td><td>Total Points</td></tr>";
        for (int i = 0; i < labNames.length; i++) {
            stringBuilder += "<tr><td>" + labNames[i] + "</td><td>" + labPointsEarned[i] + "</td><td>" + labPointsTotal[i] + "</td></tr>";
        }
        stringBuilder += "</tbody></table></body>";
        return stringBuilder;
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
        jScrollPane2 = new javax.swing.JScrollPane();
        _projectList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        _previewPane = new javax.swing.JEditorPane();
        jLabel5 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        _sendButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        _labList = new javax.swing.JList();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(GradeReportView.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
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

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        _fromText.setEditable(false);
        _fromText.setText(resourceMap.getString("_fromText.text")); // NOI18N
        _fromText.setName("_fromText"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        _projectList.setName("_projectList"); // NOI18N
        _projectList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                _projectListValueChanged(evt);
            }
        });
        _projectList.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                _projectListPropertyChange(evt);
            }
        });
        jScrollPane2.setViewportView(_projectList);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        _previewPane.setEditable(false);
        _previewPane.setName("_previewPane"); // NOI18N
        jScrollPane3.setViewportView(_previewPane);

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setName("jSeparator1"); // NOI18N

        _sendButton.setText(resourceMap.getString("_sendButton.text")); // NOI18N
        _sendButton.setName("_sendButton"); // NOI18N
        _sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _sendButtonActionPerformed(evt);
            }
        });

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        _labList.setName("_labList"); // NOI18N
        _labList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                _labListValueChanged(evt);
            }
        });
        _labList.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                _labListPropertyChange(evt);
            }
        });
        jScrollPane4.setViewportView(_labList);

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
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                                .addGap(305, 305, 305))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(_fromText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel4))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel6)
                                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))))
                                .addGap(25, 25, 25)
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
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_sendButton)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_fromText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_sendButton)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _projectListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event__projectListValueChanged
        updatePreview();
    }//GEN-LAST:event__projectListValueChanged

    private void _projectListPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event__projectListPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event__projectListPropertyChange

    private void _messageTextKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__messageTextKeyReleased
        updatePreview();
    }//GEN-LAST:event__messageTextKeyReleased
    private ArrayDeque<File> _filesToDelete;

    private void _sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__sendButtonActionPerformed

        String[] studNames = DatabaseIO.getStudentNames();

        //JOptionPane.showMessageDialog(this, "Generating histograms and sending emails...", "Please Wait", JOptionPane.INFORMATION_MESSAGE);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JOptionPane.showMessageDialog(null, new String("Finished sending " + DatabaseIO.getStudentNames().length + " emails.  Make sure to delete your sent file or you fail next login."));
            }
        });

        ArrayDeque<File> fullFileList = new ArrayDeque<File>();
        ArrayList<String> histogramFileNames = new ArrayList<String>();

        StudentDataPanel sdp = new StudentDataPanel();
        int[] projIndex = _projectList.getSelectedIndices();
        int[] labIndex = _labList.getSelectedIndices();
        ChartPanel p = new ChartPanel();
        String[] projNames = new String[projIndex.length];
        Object[] projObjects = _projectList.getSelectedValues();
        for (int i = 0; i < projNames.length; i++) {
            projNames[i] = (String) projObjects[i];
        }
        String[] labNames = new String[labIndex.length];
        Object[] labObjects = _labList.getSelectedValues();
        for (int i = 0; i < labNames.length; i++) {
            labNames[i] = (String) labObjects[i];
        }

        String[] projTotals = new String[projIndex.length];
        String[] projEarned = new String[projIndex.length];
        for (int i = 0; i < projIndex.length; i++) {
            projTotals[i] = _projectPointsTotal[projIndex[i]];
        //projEarned[i] = "0";
        }
        String[] labTotals = new String[labIndex.length];
        String[] labEarned = new String[labIndex.length];
        for (int i = 0; i < labIndex.length; i++) {
            labTotals[i] = _labPointsTotal[labIndex[i]];
        //labEarned[i] = "0";
        }

        String[] homeworkEarned = new String[_homeworkNames.length];
        try {
            for (int i = 0; i < projNames.length; i++) {
                double d = Double.parseDouble(projTotals[i]);
                List<Double> l = new ArrayList<Double>();
                ISqlJetCursor cursor = DatabaseIO.getAllData("grades_" + projNames[i]);
                while (!cursor.eof()) {
                    double earned = (cursor.getString(DatabaseIO.GRADE_RUBRIC_FIELDS[1]).length() == 0) ? 0.0 : Double.parseDouble(cursor.getString(DatabaseIO.GRADE_RUBRIC_FIELDS[1]));
                    if (earned / d * 100 != 0) { //ignore zero handins
                        l.add(earned / d * 100);
                    }
                    cursor.next();
                }
                double[] data = new double[l.size()];
                for (int j = 0; j < data.length; j++) {
                    data[j] = l.get(j);
                }
                p.loadData(projNames[i], data);
                fullFileList.add(new File(".tmpdata/" + projNames[i] + ".png"));
                histogramFileNames.add(fullFileList.peekLast().getAbsolutePath());
                ImageIO.write(p.getImage(600, 250), "png", fullFileList.peekLast());
            }
            for (String s : studNames) {
                sdp.updateChart(s, projNames);
                fullFileList.add(new File(".tmpdata/" + s + ".png"));
                //fNames.add(fList.peekLast().getAbsolutePath());
                ImageIO.write(sdp.getImage(600, 250), "png", fullFileList.peekLast());

            }

            //NOW SEND THE EMAILS we do this after the image creation loop so that we can guarantee that all the images have been made
            //otherwise mutt will fail at sending the message wihtout giving a warning.
            for (String s : studNames) {
                ArrayList<String> files = new ArrayList<String>(histogramFileNames);
                files.add(".tmpdata/" + s + ".png");
                for (int i = 0; i < projIndex.length; i++) {
                    projEarned[i] = Double.toString(DatabaseIO.getStudentEarnedScore(projNames[i], s));
                }
                for (int i = 0; i < labIndex.length; i++) {
                    labEarned[i] = Double.toString(DatabaseIO.getStudentEarnedScore(labNames[i], s));
                }
                for(int i = 0; i < homeworkEarned.length; i++){
                    homeworkEarned[i] = Double.toString(DatabaseIO.getStudentEarnedScore(_homeworkNames[i], s));
                }
//             Utils.sendMail("cs015headtas@cs.brown.edu", "cs015 Head TAs", new String[]{Utils.getUserLogin() + "@cs.brown.edu"}, new String[]{}, new String[]{}, "[cs015] Grade Report", htmlBuilder(_messageText.getText(), projNames, projEarned, projTotals, labNames, labEarned, labTotals, _homeworkNames, homeworkEarned, _homeworkPointsTotal),
//                     files.toArray(new String[0]));
//             Utils.sendMail("cs015headtas@cs.brown.edu", "cs015 Head TAs", new String[]{s + "@cs.brown.edu"}, new String[]{}, new String[]{}, "[cs015] Grade Report", htmlBuilder(_messageText.getText(), projNames, projEarned, projTotals, labNames, labEarned, labTotals, _homeworkNames, homeworkEarned, _homeworkPointsTotal),
//                     files.toArray(new String[0]));
             Thread.sleep(2500);
            }
            File dir1 = new File(".");
            BashConsole.write("chmod 660 " + dir1.getCanonicalPath() + "/.tmpdata/*");

        } catch (Exception e) {
        }
    }//GEN-LAST:event__sendButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        ArrayDeque<File> fList = new ArrayDeque<File>();
        ArrayList<String> fNames = new ArrayList<String>();

        StudentDataPanel sdp = new StudentDataPanel();
        int[] projIndex = _projectList.getSelectedIndices();
        int[] labIndex = _labList.getSelectedIndices();
        ChartPanel p = new ChartPanel();
        String[] projNames = new String[projIndex.length];
        Object[] projObjects = _projectList.getSelectedValues();
        for (int i = 0; i < projNames.length; i++) {
            projNames[i] = (String) projObjects[i];
        }
        String[] labNames = new String[labIndex.length];
        Object[] labObjects = _labList.getSelectedValues();
        for (int i = 0; i < labNames.length; i++) {
            labNames[i] = (String) labObjects[i];
        }

        String[] projTotals = new String[projIndex.length];
        String[] projEarned = new String[projIndex.length];
        for (int i = 0; i < projIndex.length; i++) {
            projTotals[i] = _projectPointsTotal[projIndex[i]];
            projEarned[i] = "0";
        }
        String[] labTotals = new String[labIndex.length];
        String[] labEarned = new String[labIndex.length];
        for (int i = 0; i < labIndex.length; i++) {
            labTotals[i] = _labPointsTotal[labIndex[i]];
            labEarned[i] = "0";
        }

        String[] homeworkEarned = new String[_homeworkNames.length];
        Arrays.fill(homeworkEarned, "0");
        try {
            for (int i = 0; i < projNames.length; i++) {
                double d = Double.parseDouble(projTotals[i]);
                List<Double> l = new ArrayList<Double>();
                ISqlJetCursor cursor = DatabaseIO.getAllData("grades_" + projNames[i]);
                while (!cursor.eof()) {
                    double earned = (cursor.getString(DatabaseIO.GRADE_RUBRIC_FIELDS[1]).length() == 0) ? 0.0 : Double.parseDouble(cursor.getString(DatabaseIO.GRADE_RUBRIC_FIELDS[1]));
                    if (earned / d * 100 != 0) { //ignore zero handins
                        l.add(earned / d * 100);
                    }
                    cursor.next();
                }
                double[] data = new double[l.size()];
                for (int j = 0; j < data.length; j++) {
                    data[j] = l.get(j);
                }
                p.loadData(projNames[i], data);
                fList.add(new File(".tmpdata/" + projNames[i] + ".png"));
                fNames.add(fList.peekLast().getAbsolutePath());
                ImageIO.write(p.getImage(600, 250), "png", fList.peekLast());
            }
            sdp.updateChart("", projNames);
            fList.add(new File(".tmpdata/" + "test.png"));
            fNames.add(fList.peekLast().getAbsolutePath());
            ImageIO.write(sdp.getImage(600, 250), "png", fList.peekLast());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.sendMail("cs015headtas@cs.brown.edu", "cs015 Head TAs", new String[]{Utils.getUserLogin() + "@cs.brown.edu"}, new String[]{}, new String[]{}, "[cs015] Grade Report", htmlBuilder(_messageText.getText(), projNames, projEarned, projTotals, labNames, labEarned, labTotals, _homeworkNames, homeworkEarned, _homeworkPointsTotal), fNames.toArray(new String[0]));
        File dir1 = new File(".");
        try {
            BashConsole.write("chmod 660 " + dir1.getCanonicalPath() + "/.tmpdata/*");
        } catch (Exception e) {
            e.printStackTrace();
        }

    //while(!fList.isEmpty()) fList.pollFirst().delete();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void _labListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event__labListValueChanged
        updatePreview();
}//GEN-LAST:event__labListValueChanged

    private void _labListPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event__labListPropertyChange
//        updatePreview();
}//GEN-LAST:event__labListPropertyChange

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new GradeReportView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField _fromText;
    private javax.swing.JList _labList;
    private javax.swing.JTextArea _messageText;
    private javax.swing.JEditorPane _previewPane;
    private javax.swing.JList _projectList;
    private javax.swing.JButton _sendButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
